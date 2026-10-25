/*******************************************************************************************************
 *
 * PreferenceCommands.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.java_websocket.WebSocket;

import gama.api.GAMA;
import gama.api.gaml.types.IType;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.date.GamaDateFactory;
import gama.api.types.date.IDate;
import gama.api.types.file.GenericFile;
import gama.api.types.file.IGamaFile;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.font.IFont;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.utils.prefs.IGamaPreferenceStore;
import gama.api.utils.prefs.Pref;
import gama.dev.DEBUG;

/** Experiment-independent access to GAML-visible platform preferences. */
public final class PreferenceCommands {

	private PreferenceCommands() {}

	public static GamaServerMessage get(final IGamaServer server, final WebSocket socket,
			final ReceivedMessage request) {
		return execute(request, false);
	}

	public static GamaServerMessage set(final IGamaServer server, final WebSocket socket,
			final ReceivedMessage request) {
		return execute(request, true);
	}

	private static GamaServerMessage execute(final ReceivedMessage request, final boolean update) {
		final AtomicReference<GamaServerMessage> response = new AtomicReference<>();
		// Preference listeners may update widgets; never hold the store lock while waiting for the UI thread.
		GAMA.getGui().run("Accessing platform preferences", () -> {
			try {
				final IGamaPreferenceStore store = GAMA.getPreferenceStore();
				synchronized (store) {
					final Object content = update ? update(store, request) : read(store, request);
					response.set(new CommandResponse(MessageType.CommandExecutedSuccessfully, content, request, false));
				}
			} catch (final IllegalArgumentException e) {
				response.set(new CommandResponse(MessageType.MalformedRequest, e.getMessage(), request, false));
			} catch (final IllegalStateException e) {
				response.set(new CommandResponse(MessageType.UnableToExecuteRequest, e.getMessage(), request, false));
			} catch (final RuntimeException e) {
				DEBUG.ERR("Unable to execute preference command", e);
				response.set(new CommandResponse(MessageType.UnableToExecuteRequest, e.getMessage(), request, false));
			}
		}, false);
		if (response.get() == null) throw new IllegalStateException("Preference command did not complete");
		return response.get();
	}

	static Map<String, Object> read(final IGamaPreferenceStore store, final Map<String, Object> request) {
		final List<Pref<?>> selected = new ArrayList<>();
		if (request.containsKey("names")) {
			if (!(request.get("names") instanceof List<?> names))
				throw new IllegalArgumentException("'names' must be an array of preference names");
			for (final Object name : names) { selected.add(resolve(store, name)); }
		} else {
			for (final Pref<?> pref : store.getPreferences()) {
				if (pref.inGaml()) { selected.add(pref); }
			}
		}
		final Map<String, Object> result = new LinkedHashMap<>();
		for (final Pref<?> pref : selected) { result.put(pref.getName(), describe(pref)); }
		return result;
	}

	static Map<String, Object> update(final IGamaPreferenceStore store, final Map<String, Object> request) {
		final Pref<?> pref = resolve(store, request.get("name"));
		if (!request.containsKey("value")) throw new IllegalArgumentException("Missing 'value'");
		if (request.containsKey("persist") && !(request.get("persist") instanceof Boolean))
			throw new IllegalArgumentException("'persist' must be a JSON boolean");
		final Object value = decode(pref.getTypeId(), request.get("value"));
		validate(pref, value);
		try {
			pref.setValue(null, value);
			if (!Objects.equals(pref.getValue(), value))
				throw new IllegalStateException("Preference change rejected: " + pref.getName());
			if (Boolean.TRUE.equals(request.get("persist"))) { store.writeAndFlush(pref); }
		} catch (final RuntimeException e) {
			DEBUG.ERR("Unable to update preference '" + pref.getName() + "'", e);
			throw new IllegalStateException("Unable to update preference '" + pref.getName()
					+ "': " + e.getMessage() + ". The in-memory value may already have changed.", e);
		}
		final Map<String, Object> result = describe(pref);
		result.put("name", pref.getName());
		result.put("persisted", Boolean.TRUE.equals(request.get("persist")));
		return result;
	}

	private static Pref<?> resolve(final IGamaPreferenceStore store, final Object name) {
		if (!(name instanceof String key) || key.isBlank())
			throw new IllegalArgumentException("'name' must be a non-empty preference name");
		final Pref<?> pref = store.get(key);
		if (pref == null || !pref.inGaml())
			throw new IllegalArgumentException("Unknown or unexposed preference: " + key);
		return pref;
	}

	private static Map<String, Object> describe(final Pref<?> pref) {
		final Map<String, Object> result = new LinkedHashMap<>();
		result.put("value", encode(pref.getValue()));
		result.put("type", pref.getType().toString());
		result.put("description", pref.getTitle());
		result.put("restart_required", pref.isRestartRequired());
		result.put("min", encode(pref.getMinValue(null)));
		result.put("max", encode(pref.getMaxValue(null)));
		final List<?> among = pref.getAmongValue(null);
		result.put("among", among == null ? null : among.stream().map(PreferenceCommands::encode).toList());
		return result;
	}

	private static Object encode(final Object value) {
		return switch (value) {
			case IColor color -> List.of(color.red(), color.green(), color.blue(), color.alpha());
			case IFont font -> Map.of("name", font.getName(), "style", font.getStyle(), "size", font.getSize());
			case IPoint point -> List.of(point.getX(), point.getY(), point.getZ());
			case IDate date -> date.toISOString();
			case IGamaFile file -> file.getPath(null);
			case null, default -> value;
		};
	}

	private static Object decode(final int type, final Object value) {
		switch (type) {
			case IType.BOOL:
				if (value instanceof Boolean) return value;
				break;
			case IType.INT:
				return integer(value);
			case IType.FLOAT:
				return number(value);
			case IType.STRING:
				if (value instanceof String) return value;
				break;
			case IType.COLOR:
				if (value instanceof List<?> components && (components.size() == 3 || components.size() == 4)) {
					final List<Integer> rgba = components.stream().map(PreferenceCommands::integer).toList();
					if (rgba.stream().anyMatch(c -> c < 0 || c > 255))
						throw new IllegalArgumentException("Color components must be integers between 0 and 255");
					return GamaColorFactory.createWithRGBA(rgba.get(0), rgba.get(1), rgba.get(2),
							rgba.size() == 4 ? rgba.get(3) : 255);
				}
				break;
			case IType.FONT:
				if (value instanceof Map<?, ?> font && font.get("name") instanceof String name && !name.isBlank()) {
					final int style = integer(font.get("style"));
					final int size = integer(font.get("size"));
					if (style < 0 || style > 3 || size < 1)
						throw new IllegalArgumentException("Font style must be 0..3 and size must be positive");
					return GamaFontFactory.createFontFrom(new Font(name, style, size));
				}
				break;
			case IType.POINT:
				if (value instanceof List<?> coordinates && (coordinates.size() == 2 || coordinates.size() == 3))
					return GamaPointFactory.create(number(coordinates.get(0)), number(coordinates.get(1)),
							coordinates.size() == 3 ? number(coordinates.get(2)) : 0);
				break;
			case IType.DATE:
				if (value instanceof String date) {
					try {
						return GamaDateFactory.createFromISOString(date);
					} catch (final RuntimeException e) {
						throw new IllegalArgumentException("Invalid date: " + date, e);
					}
				}
				break;
			case IType.FILE:
				if (value instanceof String path && !path.isBlank()) return new GenericFile(path, false);
				break;
			default:
				throw new IllegalArgumentException("Unsupported preference type: " + type);
		}
		throw new IllegalArgumentException("Invalid JSON value for preference type " + type);
	}

	private static double number(final Object value) {
		if (value instanceof Number n && Double.isFinite(n.doubleValue())) return n.doubleValue();
		throw new IllegalArgumentException("Expected a finite JSON number");
	}

	private static int integer(final Object value) {
		final double n = number(value);
		if (n != Math.rint(n) || n < Integer.MIN_VALUE || n > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Expected a 32-bit integer");
		return (int) n;
	}

	private static void validate(final Pref<?> pref, final Object value) {
		final List<?> among = pref.getAmongValue(null);
		if (among != null && !among.contains(value))
			throw new IllegalArgumentException("Value is not among the allowed values for " + pref.getName());
		if (value instanceof Number n) {
			final Comparable<?> min = pref.getMinValue(null);
			final Comparable<?> max = pref.getMaxValue(null);
			if (min instanceof Number lower && n.doubleValue() < lower.doubleValue()
					|| max instanceof Number upper && n.doubleValue() > upper.doubleValue())
				throw new IllegalArgumentException("Value is outside the allowed range for " + pref.getName());
		}
	}
}
