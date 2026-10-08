/*******************************************************************************************************
 *
 * SpecialContext.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

import java.util.HashMap;
import java.util.Map;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.ITypesManager;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.types.topology.ITopology;
import gama.api.ui.IGui;

/**
 * Internal context storage for special scope data and runtime services.
 * 
 * <p>
 * SpecialContext is a package-private class used internally by {@link ExecutionScope} to manage various special
 * context data that doesn't fit into the regular variable or agent context structures. It holds references to:
 * </p>
 * <ul>
 * <li>Topology - Spatial structure and agent positioning</li>
 * <li>Root Agent - Top-level agent (experiment or simulation)</li>
 * <li>GUI - User interface services</li>
 * <li>Types Manager - GAML type system</li>
 * <li>Current Error - Active exception/error</li>
 * <li>Data Map - Arbitrary key-value storage</li>
 * <li>Each Map - Iterator variable storage</li>
 * </ul>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Service References:</b> Maintains references to core GAMA services</li>
 * <li><b>Arbitrary Data Storage:</b> Generic key-value map for custom data</li>
 * <li><b>Iterator Support:</b> Special storage for "each" variables in loops</li>
 * <li><b>Scope Copying:</b> Provides copyFrom() for scope duplication</li>
 * <li><b>Resource Cleanup:</b> Clears all references when scope is disposed</li>
 * </ul>
 * 
 * <h2>Internal Structure</h2>
 * 
 * <pre>
 * SpecialContext {
 *   - data: Map&lt;String, Object&gt;     // Custom data storage
 *   - each: Map&lt;String, Object&gt;     // Iterator variables
 *   - topology: ITopology            // Spatial structure
 *   - rootAgent: ITopLevelAgent      // Experiment/simulation root
 *   - gui: IGui                      // User interface
 *   - types: ITypesManager           // Type system
 *   - currentError: GamaRuntimeException  // Active error
 * }
 * </pre>
 * 
 * <h2>Usage in ExecutionScope</h2>
 * 
 * <p>
 * SpecialContext is used internally by ExecutionScope to manage these special values:
 * </p>
 * 
 * <pre>{@code
 * public class ExecutionScope implements IScope {
 *     protected final SpecialContext additionalContext = new SpecialContext();
 *     
 *     @Override
 *     public IGui getGui() {
 *         return additionalContext.gui;
 *     }
 *     
 *     @Override
 *     public ITopology getTopology() {
 *         return additionalContext.topology;
 *     }
 *     
 *     @Override
 *     public Object getData(String key) {
 *         return additionalContext.getData(key);
 *     }
 * }
 * }</pre>
 * 
 * <h2>Data Storage</h2>
 * 
 * <h3>Arbitrary Data Map</h3>
 * <p>
 * The data map allows storing arbitrary key-value pairs in a scope:
 * </p>
 * <pre>{@code
 * // Internal usage
 * specialContext.setData("custom_key", customValue);
 * Object value = specialContext.getData("custom_key");
 * 
 * // Setting null removes the key
 * specialContext.setData("custom_key", null);
 * }</pre>
 * 
 * <h3>Each Variables</h3>
 * <p>
 * The each map stores iterator variables for loops:
 * </p>
 * <pre>{@code
 * // In loop implementation
 * specialContext.setEach("each", currentElement);
 * 
 * // In expression evaluation
 * Object currentElement = specialContext.getEach("each");
 * }</pre>
 * 
 * <h2>Context Copying</h2>
 * 
 * <p>
 * When creating a new scope from an existing one, copyFrom() is used:
 * </p>
 * 
 * <pre>{@code
 * // In ExecutionScope constructor
 * public ExecutionScope(ITopLevelAgent root, String name, 
 *                       SpecialContext specialContext) {
 *     this.additionalContext.copyFrom(specialContext);
 * }
 * }</pre>
 * 
 * <p>
 * The copyFrom() method:
 * </p>
 * <ul>
 * <li>Does NOT copy the "each" map (addresses issue #725 - prevents pollution)</li>
 * <li>DOES copy data, topology, rootAgent, gui, types, and currentError</li>
 * </ul>
 * 
 * <h2>Lifecycle Management</h2>
 * 
 * <h3>Initialization</h3>
 * <pre>{@code
 * SpecialContext context = new SpecialContext();
 * context.rootAgent = experimentAgent;
 * context.gui = GAMA.getGui();
 * context.types = typesManager;
 * context.topology = simulation.getTopology();
 * }</pre>
 * 
 * <h3>Cleanup</h3>
 * <pre>{@code
 * // When scope is disposed
 * specialContext.clear();
 * // All references are set to null
 * }</pre>
 * 
 * <h2>Memory Considerations</h2>
 * 
 * <p>
 * SpecialContext uses lazy initialization for maps:
 * </p>
 * <ul>
 * <li>data and each maps are only created when first needed</li>
 * <li>Reduces memory overhead for scopes that don't use these features</li>
 * <li>clear() sets maps to null rather than just clearing them</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>
 * SpecialContext is NOT thread-safe. It should only be accessed from the thread that owns the containing scope.
 * </p>
 * 
 * @see ExecutionScope
 * @see IScope
 */
class SpecialContext {

	/** The data map - arbitrary key-value storage for custom scope data */
	Map<String, Object> data;

	/** The each map - storage for iterator variables like "each" in loops */
	Map<String, Object> each;

	/** The current topology - defines spatial relationships and agent positioning */
	public ITopology topology;

	/** The root agent - typically the experiment or top-level simulation agent */
	ITopLevelAgent rootAgent;

	/** The GUI - provides access to user interface services */
	IGui gui;

	/** The types manager - provides access to the GAML type system */
	ITypesManager types;

	/** The current error - stores the most recent runtime exception if any */
	GamaRuntimeException currentError;

	/**
	 * Clears all context data and releases all references.
	 * 
	 * <p>
	 * This method is called when a scope is disposed. It sets all fields to null, allowing garbage collection of
	 * referenced objects and preventing memory leaks.
	 * </p>
	 * 
	 * <p>
	 * After calling clear(), this context should not be used further.
	 * </p>
	 */
	void clear() {
		each = null;
		data = null;
		topology = null;
		rootAgent = null;
		gui = null;
		types = null;
		currentError = null;
	}

	/**
	 * Copies data from another SpecialContext into this one.
	 * 
	 * <p>
	 * This method is used when creating a new scope based on an existing one. It copies most fields from the source
	 * context, but intentionally does NOT copy the "each" map to prevent scope pollution (addresses issue #725).
	 * </p>
	 * 
	 * <p>
	 * Fields copied:
	 * </p>
	 * <ul>
	 * <li>data (shared reference)</li>
	 * <li>topology (shared reference)</li>
	 * <li>rootAgent (shared reference)</li>
	 * <li>gui (shared reference)</li>
	 * <li>types (shared reference)</li>
	 * <li>currentError (shared reference)</li>
	 * </ul>
	 * 
	 * <p>
	 * Fields NOT copied:
	 * </p>
	 * <ul>
	 * <li>each (set to null to avoid pollution)</li>
	 * </ul>
	 * 
	 * @param specialContext
	 *            the source context to copy from, may be null
	 */
	public void copyFrom(final SpecialContext specialContext) {
		if (specialContext == null) return;
		// Addresses #725 by avoiding the pollution of a scope by another
		// each = specialContext.each;
		each = null;

		data = specialContext.data;
		topology = specialContext.topology;
		rootAgent = specialContext.rootAgent;
		gui = specialContext.gui;
		types = specialContext.types;
		currentError = specialContext.currentError;
	}

	/**
	 * Retrieves a value from the arbitrary data map.
	 * 
	 * <p>
	 * The data map allows storing custom key-value pairs in a scope. This is useful for passing data between different
	 * parts of the execution that don't have direct access to each other.
	 * </p>
	 * 
	 * <h3>Example Usage</h3>
	 * <pre>{@code
	 * // Store custom data
	 * scope.setData("processing_mode", "batch");
	 * 
	 * // Retrieve it later
	 * String mode = (String) scope.getData("processing_mode");
	 * }</pre>
	 * 
	 * @param key
	 *            the key to look up in the data map
	 * @return the value associated with the key, or null if not found or if data map is null
	 */
	Object getData(final String key) {
		return data == null ? null : data.get(key);
	}

	/**
	 * Stores a value in the arbitrary data map.
	 * 
	 * <p>
	 * If the value is null, the key is removed from the map rather than storing null. The data map is created lazily
	 * on first use.
	 * </p>
	 * 
	 * <h3>Example Usage</h3>
	 * <pre>{@code
	 * // Store a value
	 * setData("cache_key", cachedData);
	 * 
	 * // Remove a value
	 * setData("cache_key", null);
	 * }</pre>
	 * 
	 * @param key
	 *            the key under which to store the value
	 * @param value
	 *            the value to store, or null to remove the key
	 */
	void setData(final String key, final Object value) {
		if (value == null) {
			if (data == null) return;
			data.remove(key);
		}
		if (data == null) { data = new HashMap<>(); }
		data.put(key, value);
	}

	/**
	 * Retrieves the value of an iterator variable (e.g., "each" in loops).
	 * 
	 * <p>
	 * The each map stores variables that represent the current element in iterations. The most common use is the
	 * "each" variable in GAML loops, but custom iterator variables are also supported.
	 * </p>
	 * 
	 * <h3>Example Usage in GAML</h3>
	 * <pre>
	 * loop agent over: agents {
	 *     // "each" variable is stored here
	 *     ask each {
	 *         do something;
	 *     }
	 * }
	 * </pre>
	 * 
	 * @param name
	 *            the name of the iterator variable (typically "each")
	 * @return the current value of the iterator variable, or null if not found or if each map is null
	 */
	Object getEach(final String name) {
		return each == null ? null : each.get(name);
	}

	/**
	 * Sets the value of an iterator variable (e.g., "each" in loops).
	 * 
	 * <p>
	 * This method is called by loop implementations to set the current iteration value. The each map is created lazily
	 * on first use.
	 * </p>
	 * 
	 * <h3>Example Usage in Loop Implementation</h3>
	 * <pre>{@code
	 * // In a loop statement
	 * for (Object element : collection) {
	 *     scope.setEach("each", element);
	 *     scope.execute(bodyStatement);
	 * }
	 * }</pre>
	 * 
	 * @param key
	 *            the name of the iterator variable (typically "each")
	 * @param value
	 *            the value to assign to the iterator variable
	 */
	void setEach(final String key, final Object value) {
		if (each == null) { each = new HashMap<>(); }
		each.put(key, value);
	}

}