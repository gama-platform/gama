/*******************************************************************************************************
 *
 * SyntaxHighlightingPreferences.java, in gama.ui.shared, is part of the source code of the GAMA modeling and
 * simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.utils;

import static gama.api.utils.prefs.GamaPreferences.create;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import gama.api.gaml.types.IType;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.utils.prefs.GamaPreferences.Theme;
import gama.api.utils.prefs.Pref;

/** Shared persistence for the editor and its preferences UI, without a dependency on Xtext. */
public final class SyntaxHighlightingPreferences {

	public static final String CONFIGURATION_GROUP = "Syntax highlighting configurations";
	public static final List<String> THEMES = List.of("light", "dark");
	public static final List<String> BUILT_INS = List.of("GAMA 2026", "GAMA 2025");
	public static final String CONFIGURATION_KEY = "pref_syntax_configuration";

	public record Style(IColor color, boolean bold, boolean italic) {}

	public record Configuration(String name, Map<String, Map<String, Style>> themes) {}

	private static final Map<String, Pref<?>> PREFERENCES = new LinkedHashMap<>();
	private static final Map<String, Configuration> BUILT_IN_CONFIGURATIONS = new LinkedHashMap<>();
	private static Map<String, String> titles = Map.of();
	private static Configuration defaults;

	private SyntaxHighlightingPreferences() {}

	public static String group(final String theme) {
		return "Syntax coloring (" + theme + " theme)";
	}

	public static String key(final String id, final String property, final String theme) {
		return "pref_" + id + "_" + property + "_" + theme;
	}

	public static String legacyKey(final String theme) {
		return "pref_syntax_use_default_colors_" + theme;
	}

	public static void initialize(final Map<String, String> categories) {
		if (defaults != null) return;
		titles = new LinkedHashMap<>(categories);
		defaults = builtIn("GAMA 2026");
		PREFERENCES.put(CONFIGURATION_KEY, create(CONFIGURATION_KEY, "Configuration", defaults.name(),
				IType.STRING, false).in(Theme.NAME, CONFIGURATION_GROUP));
		for (final String theme : THEMES) {
			// Retain the old switch so existing custom colors are read without changing their meaning.
			PREFERENCES.put(legacyKey(theme), create(legacyKey(theme), "Use default syntax colors", true,
					IType.BOOL, false).hidden());
			titles.forEach((id, title) -> {
				final Style style = defaults.themes().get(theme).get(id);
				final String colorKey = key(id, "color", theme);
				PREFERENCES.put(colorKey, create(colorKey, title, style.color(), IType.COLOR, false)
						.in(Theme.NAME, group(theme)));
				for (final String property : List.of("bold", "italic")) {
					final String styleKey = key(id, property, theme);
					PREFERENCES.put(styleKey, create(styleKey, title + " " + property,
							"bold".equals(property) ? style.bold() : style.italic(), IType.BOOL, false).hidden());
				}
			});
		}
	}

	public static Map<String, String> categories() { return Map.copyOf(titles); }

	public static boolean isThemeGroup(final String name) {
		return THEMES.stream().anyMatch(theme -> group(theme).equals(name));
	}

	public static Configuration builtIn(final String name) {
		if (!BUILT_INS.contains(name)) throw new IllegalArgumentException("Unknown syntax configuration: " + name);
		return BUILT_IN_CONFIGURATIONS.computeIfAbsent(name, SyntaxHighlightingPreferences::readBuiltIn);
	}

	private static Configuration readBuiltIn(final String name) {
		final String resource = "/syntax/" + name.toLowerCase().replace(' ', '-') + ".properties";
		try (InputStream input = SyntaxHighlightingPreferences.class.getResourceAsStream(resource)) {
			if (input == null) throw new IOException("Missing resource " + resource);
			return read(input);
		} catch (final IOException e) {
			throw new IllegalStateException("Cannot load built-in syntax configuration " + name, e);
		}
	}

	public static Configuration load(final Path path) throws IOException {
		try (InputStream input = Files.newInputStream(path)) {
			return read(input);
		}
	}

	private static Configuration read(final InputStream input) throws IOException {
		final Properties properties = new Properties();
		properties.load(input);
		if (!"1".equals(properties.getProperty("version"))) throw new IOException("Unsupported syntax configuration version");
		final String name = properties.getProperty("name", "").trim();
		if (name.isEmpty()) throw new IOException("Missing syntax configuration name");
		final Map<String, Map<String, Style>> themes = new LinkedHashMap<>();
		for (final String theme : THEMES) {
			final Map<String, Style> styles = new LinkedHashMap<>();
			for (final String id : titles.keySet()) {
				final String property = theme + "." + id;
				final String value = properties.getProperty(property, "");
				if (!value.matches("[0-9a-fA-F]{6},(true|false),(true|false)"))
					throw new IOException("Invalid or missing syntax style: " + property);
				final String[] parts = value.split(",");
				final int rgb = Integer.parseInt(parts[0], 16);
				styles.put(id, new Style(GamaColorFactory.get(rgb >> 16, rgb >> 8 & 255, rgb & 255),
						Boolean.parseBoolean(parts[1]), Boolean.parseBoolean(parts[2])));
			}
			themes.put(theme, Map.copyOf(styles));
		}
		return new Configuration(name, Map.copyOf(themes));
	}

	public static void save(final Path path, final Configuration configuration) throws IOException {
		if (configuration.name().isBlank()) throw new IOException("Missing syntax configuration name");
		final Properties properties = new Properties();
		properties.setProperty("version", "1");
		properties.setProperty("name", configuration.name());
		configuration.themes().forEach((theme, styles) -> styles.forEach((id, style) -> {
			final IColor color = style.color();
			properties.setProperty(theme + "." + id, "%02x%02x%02x,%s,%s".formatted(
					color.red(), color.green(), color.blue(), style.bold(), style.italic()));
		}));
		try (OutputStream output = Files.newOutputStream(path)) {
			properties.store(output, "GAMA syntax highlighting configuration");
		}
	}

	public static Configuration current() {
		final Map<String, Map<String, Style>> themes = new LinkedHashMap<>();
		for (final String theme : THEMES) {
			final Map<String, Style> styles = new LinkedHashMap<>();
			for (final String id : titles.keySet()) {
				if (Boolean.TRUE.equals(PREFERENCES.get(legacyKey(theme)).getValue())) {
					styles.put(id, defaults.themes().get(theme).get(id));
				} else {
					styles.put(id, new Style((IColor) PREFERENCES.get(key(id, "color", theme)).getValue(),
							Boolean.TRUE.equals(PREFERENCES.get(key(id, "bold", theme)).getValue()),
							Boolean.TRUE.equals(PREFERENCES.get(key(id, "italic", theme)).getValue())));
				}
			}
			themes.put(theme, Map.copyOf(styles));
		}
		return new Configuration((String) PREFERENCES.get(CONFIGURATION_KEY).getValue(), Map.copyOf(themes));
	}

	public static Configuration staged(final Map<String, Object> values) {
		final Map<String, Map<String, Style>> themes = new LinkedHashMap<>();
		for (final String theme : THEMES) {
			final Map<String, Style> styles = new LinkedHashMap<>();
			for (final String id : titles.keySet()) {
				styles.put(id, new Style((IColor) values.get(key(id, "color", theme)),
						Boolean.TRUE.equals(values.get(key(id, "bold", theme))),
						Boolean.TRUE.equals(values.get(key(id, "italic", theme)))));
			}
			themes.put(theme, Map.copyOf(styles));
		}
		return new Configuration((String) values.get(CONFIGURATION_KEY), Map.copyOf(themes));
	}

	/** Stages both themes in the dialog's model; no live preference is changed until Save. */
	public static void stage(final Configuration configuration, final Map<String, Object> values) {
		values.put(CONFIGURATION_KEY, configuration.name());
		configuration.themes().forEach((theme, styles) -> {
			values.put(legacyKey(theme), styles.equals(defaults.themes().get(theme)));
			styles.forEach((id, style) -> {
				values.put(key(id, "color", theme), style.color());
				values.put(key(id, "bold", theme), style.bold());
				values.put(key(id, "italic", theme), style.italic());
			});
		});
	}

	public static void onChange(final Runnable listener) {
		PREFERENCES.values().forEach(preference -> preference.onChange(value -> listener.run()));
	}
}
