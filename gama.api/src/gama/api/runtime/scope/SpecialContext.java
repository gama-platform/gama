/**
 * 
 */
package gama.api.runtime.scope;

import java.util.HashMap;
import java.util.Map;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.ITypesManager;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.topology.ITopology;
import gama.api.ui.IGui;

/**
 * The Class SpecialContext.
 */
class SpecialContext {

	/** The data map. A structure that can store arbitrary data */
	Map<String, Object> data;

	/** The each. */
	Map<String, Object> each;

	/** The topology. */
	public ITopology topology;

	/** The root agent. */
	ITopLevelAgent rootAgent;

	/** The gui. */
	IGui gui;

	/** The types. */
	ITypesManager types;

	/** The current error. */
	GamaRuntimeException currentError;

	/**
	 * Clear.
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
	 * Copy from.
	 *
	 * @param specialContext
	 *            the special context
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
	 * Gets the data.
	 *
	 * @param key
	 *            the key
	 * @return the data
	 */
	Object getData(final String key) {
		return data == null ? null : data.get(key);
	}

	/**
	 * Sets the data.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
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
	 * Gets the each.
	 *
	 * @param name
	 *            the name
	 * @return the each
	 */
	Object getEach(final String name) {
		return each == null ? null : each.get(name);
	}

	/**
	 * Sets the each.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	void setEach(final String key, final Object value) {
		if (each == null) { each = new HashMap<>(); }
		each.put(key, value);
	}

}