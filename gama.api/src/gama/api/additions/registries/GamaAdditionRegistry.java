/*******************************************************************************************************
 *
 * GamaAdditionRegistry.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;

import gama.api.additions.delegates.ICreateDelegate;
import gama.api.additions.delegates.IDrawDelegate;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.additions.delegates.ISaveDelegate;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.ui.displays.IDisplayCreator;
import gama.dev.DEBUG;

/**
 *
 */
public class GamaAdditionRegistry {

	/** The Constant DRAW_DELEGATES. */
	private static final Map<IType, IDrawDelegate> DRAW_DELEGATES = new HashMap<>();

	/** The delegates. */
	private static final List<ICreateDelegate> CREATE_DELEGATES = new ArrayList<>();

	/** The delegate types. */
	private static final List<IType> CREATE_DELEGATE_TYPES = new ArrayList<>();

	/** The delegates. */
	private static final List<IEventLayerDelegate> EVENT_LAYER_DELEGATES = new ArrayList<>();

	/** The Constant DELEGATES_BY_GAML_TYPE. */
	private static final Map<String, Map<IType, ISaveDelegate>> SAVE_DELEGATES = new HashMap<>();

	/** The Constant SYNONYMS. */
	private static final SetMultimap<String, String> SAVE_SYNONYMS = TreeMultimap.create();

	/** The displays. */
	private static final Map<String, IDisplayCreator> DISPLAYS = new LinkedHashMap<>();

	/**
	 * @param createExecutableExtension
	 */
	public static void addDelegate(final IDrawDelegate delegate) {
		final IType t = delegate.typeDrawn();
		if (t != null) { DRAW_DELEGATES.put(t, delegate); }
	}

	/**
	 * @param createExecutableExtension
	 */
	public static void addDelegate(final ICreateDelegate delegate) {
		CREATE_DELEGATES.add(delegate);
		final IType delegateType = delegate.fromFacetType();
		if (delegateType != null && delegateType != Types.NO_TYPE) {
			CREATE_DELEGATE_TYPES.add(delegate.fromFacetType());
		}
	}

	/**
	 * Removes the delegate.
	 *
	 * @param cd
	 *            the cd
	 */
	public static void removeDelegate(final ICreateDelegate cd) {
		CREATE_DELEGATES.remove(cd);
	}

	/**
	 * @param createExecutableExtension
	 */
	public static void addDelegate(final IEventLayerDelegate delegate) {
		EVENT_LAYER_DELEGATES.add(delegate);
	}

	/**
	 * Adds the delegate.
	 *
	 * @param delegate
	 *            the delegate
	 */
	public static void addDelegate(final ISaveDelegate delegate) {
		Set<String> files = delegate.getFileTypes();

		delegate.getSynonyms().forEach((k, v) -> {
			SAVE_SYNONYMS.put(k, v);
			SAVE_SYNONYMS.put(v, k);
		});
		final IType t = delegate.getDataType();
		for (String f : files) {
			Map<IType, ISaveDelegate> map = SAVE_DELEGATES.get(f);
			if (map == null) {
				map = new HashMap<>();
				SAVE_DELEGATES.put(f, map);
			}
			if (map.containsKey(t)) {
				DEBUG.LOG("WARNING: Extensions to SaveStatement already registered for file type " + f
						+ " and data type " + t);
			}
			map.put(t, delegate);

		}

	}

	/**
	 * Adds the delegate.
	 *
	 * @param name
	 *            the name
	 * @param delegate
	 *            the delegate
	 */
	public static void addDelegate(final String name, final IDisplayCreator delegate) {
		DISPLAYS.put(name, delegate);
	}

	/**
	 * @param name
	 * @return
	 */
	public static IDisplayCreator getDisplay(final String name) {
		return DISPLAYS.get(name);
	}

	/**
	 * Gets the save delegate.
	 *
	 * @param fileFormat
	 *            the file format
	 * @param dataType
	 *            the data type
	 * @return the save delegate
	 */
	public static ISaveDelegate getSaveDelegate(final String fileFormat, final IType dataType) {
		Map<IType, ISaveDelegate> map = SAVE_DELEGATES.get(fileFormat);
		if (map == null) return null;
		int distance = Integer.MAX_VALUE;
		ISaveDelegate closest = null;
		for (Entry<IType, ISaveDelegate> entry : map.entrySet()) {
			if (entry.getValue().handlesDataType(dataType)) {
				@SuppressWarnings ("unchecked") int d = dataType.distanceTo(entry.getKey());
				if (d < distance) {
					distance = d;
					closest = entry.getValue();
				}
			}
		}
		return closest;
	}

	/**
	 * Contains save delegate.
	 *
	 * @param fileFormat
	 *            the file format
	 * @return true, if successful
	 */
	public static boolean containsSaveDelegate(final String fileFormat) {
		return SAVE_DELEGATES.containsKey(fileFormat);
	}

	/**
	 * Gets the save file formats.
	 *
	 * @return the save file formats
	 */
	public static List<String> getSaveFileFormats() { return SAVE_DELEGATES.keySet().stream().sorted().toList(); }

	/**
	 * Are synonyms.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param ext
	 *            the ext
	 * @param id
	 *            the id
	 * @return true, if successful
	 * @date 13 oct. 2023
	 */
	public static boolean areFormatSynonyms(final String ext, final String id) {
		return SAVE_SYNONYMS.containsKey(ext) ? SAVE_SYNONYMS.get(ext).contains(id) : false;
	}

	/**
	 * @return
	 */
	public static Iterable<IEventLayerDelegate> getEventLayerDelegates() { return EVENT_LAYER_DELEGATES; }

	/**
	 * @return
	 */
	public static Map<IType, IDrawDelegate> getDrawDelegates() { return DRAW_DELEGATES; }

	/**
	 * @return
	 */
	public static Iterable<ICreateDelegate> getCreateDelegates() { return CREATE_DELEGATES; }

	/**
	 * @return
	 */
	public static Map<String, IDisplayCreator> getDisplays() { return DISPLAYS; }

	/**
	 * @return
	 */
	public static Iterable<IType> getCreateDelegateTypes() { return CREATE_DELEGATE_TYPES; }

}
