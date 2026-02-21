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
 * Central registry for GAMA platform additions and extensions.
 * 
 * <p>This registry manages all pluggable extensions to the GAMA platform, including:</p>
 * <ul>
 *   <li><b>Draw Delegates</b> - Custom rendering for specific data types in displays</li>
 *   <li><b>Create Delegates</b> - Custom agent creation from various sources</li>
 *   <li><b>Save Delegates</b> - Custom file format support for saving data</li>
 *   <li><b>Event Layer Delegates</b> - Custom event sources for interactive displays</li>
 *   <li><b>Display Creators</b> - Custom display implementations (Java2D, OpenGL, etc.)</li>
 * </ul>
 * 
 * <h2>Delegate Registration</h2>
 * <p>Delegates are typically registered during platform initialization by extension plugins.
 * The registry uses type-based selection to find the most appropriate delegate for a given
 * operation.</p>
 * 
 * <h2>Type-Based Selection</h2>
 * <p>Many delegates are selected based on GAML type compatibility:</p>
 * <ul>
 *   <li><b>Draw Delegates</b> - Selected by the type being drawn</li>
 *   <li><b>Save Delegates</b> - Selected by file extension and data type, using type distance for best match</li>
 *   <li><b>Create Delegates</b> - Selected by calling {@code acceptSource()} on each delegate</li>
 * </ul>
 * 
 * <h2>File Type Synonyms</h2>
 * <p>The registry maintains a bidirectional map of file extension synonyms (e.g., "jpg" ↔ "jpeg")
 * to ensure consistent file type handling across all save delegates.</p>
 * 
 * <h2>Thread Safety</h2>
 * <p>The registry is populated during platform initialization and is read-only during normal
 * operation, making it safe for concurrent access from multiple simulations.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Get a save delegate for CSV files
 * ISaveDelegate delegate = GamaAdditionRegistry.getSaveDelegate("csv", Types.LIST);
 * if (delegate != null) {
 *     delegate.save(scope, data, file, options);
 * }
 * 
 * // Get all create delegates
 * for (ICreateDelegate delegate : GamaAdditionRegistry.getCreateDelegates()) {
 *     if (delegate.acceptSource(scope, source)) {
 *         delegate.createFrom(scope, inits, max, source, init, statement);
 *         break;
 *     }
 * }
 * 
 * // Get a display creator
 * IDisplayCreator creator = GamaAdditionRegistry.getDisplay("java2D");
 * }</pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see IDrawDelegate
 * @see ICreateDelegate
 * @see ISaveDelegate
 * @see IEventLayerDelegate
 */
public class GamaAdditionRegistry {

	/** Map of GAML types to their draw delegates. */
	private static final Map<IType, IDrawDelegate> DRAW_DELEGATES = new HashMap<>();

	/** List of all registered create delegates. */
	private static final List<ICreateDelegate> CREATE_DELEGATES = new ArrayList<>();

	/** List of types handled by create delegates (for type validation). */
	private static final List<IType> CREATE_DELEGATE_TYPES = new ArrayList<>();

	/** List of all registered event layer delegates. */
	private static final List<IEventLayerDelegate> EVENT_LAYER_DELEGATES = new ArrayList<>();

	/** Map of file types to save delegates, organized by data type. */
	private static final Map<String, Map<IType, ISaveDelegate>> SAVE_DELEGATES = new HashMap<>();

	/** Bidirectional map of file type synonyms (e.g., "txt" ↔ "text"). */
	private static final SetMultimap<String, String> SAVE_SYNONYMS = TreeMultimap.create();

	/** Map of display type names to their creator implementations. */
	private static final Map<String, IDisplayCreator> DISPLAYS = new LinkedHashMap<>();

	/**
	 * Registers a draw delegate for a specific GAML type.
	 * 
	 * <p>The delegate will be used when drawing objects of the specified type in displays.</p>
	 *
	 * @param delegate the draw delegate to register
	 */
	public static void addDelegate(final IDrawDelegate delegate) {
		final IType t = delegate.typeDrawn();
		if (t != null) { DRAW_DELEGATES.put(t, delegate); }
	}

	/**
	 * Registers a create delegate for custom agent creation sources.
	 * 
	 * <p>The delegate will be consulted when a 'create' statement includes a 'from:' facet.</p>
	 *
	 * @param delegate the create delegate to register
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
	 * Registers an event layer delegate for custom event sources.
	 * 
	 * <p>The delegate will be consulted when event layers are processed in displays.</p>
	 *
	 * @param delegate the event layer delegate to register
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
	 * Retrieves the most appropriate save delegate for a file format and data type.
	 * 
	 * <p>This method selects the delegate with the closest type match to the requested
	 * data type among all delegates that support the specified file format. Type distance
	 * is calculated using {@link IType#distanceTo(IType)}.</p>
	 * 
	 * <p>The selection process:</p>
	 * <ol>
	 *   <li>Find all delegates supporting the file format</li>
	 *   <li>Filter delegates that accept the data type via {@link ISaveDelegate#handlesDataType(IType)}</li>
	 *   <li>Select the delegate with minimum type distance</li>
	 * </ol>
	 *
	 * @param fileFormat the file extension (e.g., "csv", "json", "shp")
	 * @param dataType the GAML type of the data to save
	 * @return the best matching save delegate, or null if none found
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
