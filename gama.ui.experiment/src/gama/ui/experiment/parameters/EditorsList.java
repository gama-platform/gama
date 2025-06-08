/*******************************************************************************************************
 *
 * EditorsList.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.parameters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gama.core.common.interfaces.ItemList;
import gama.core.common.preferences.GamaPreferences;
import gama.ui.shared.interfaces.IParameterEditor;

/**
 * The Class EditorsList.
 *
 * @param <T>
 *            the generic type
 */
public abstract class EditorsList<T> implements ItemList<T> {

	/** The categories. */
	/* Map to associate a category to each parameter */
	protected final Map<T, Map<String, IParameterEditor<?>>> sections = new LinkedHashMap<>();

	@Override
	public List<T> getItems() { return new ArrayList<>(sections.keySet()); }

	@Override
	public abstract String getItemDisplayName(final T obj, final String previousName);

	/**
	 * Adds the.
	 *
	 * @param params
	 *            the params
	 * @param agent
	 *            the agent
	 */
	// public abstract void add(final Collection<? extends IExperimentDisplayable> params, final IAgent agent);

	/**
	 * Gets the categories.
	 *
	 * @return the categories
	 */
	public Map<T, Map<String, IParameterEditor<?>>> getSections() { return sections; }

	/**
	 * Revert to default value.
	 */
	public void revertToDefaultValue() {
		for (final Map<String, IParameterEditor<?>> editors : sections.values()) {
			for (final IParameterEditor<?> ed : editors.values()) { ed.revertToDefaultValue(); }
		}
	}

	@Override
	public void removeItem(final T name) {
		sections.remove(name);
	}

	@Override
	public void pauseItem(final T name) {}

	@Override
	public abstract void updateItemValues(boolean synchronously, boolean retrieveValues);

	@Override
	public void resumeItem(final T obj) {}

	@Override
	public void focusItem(final T obj) {}

	/**
	 * Checks if is enabled.
	 *
	 * @param gpParam
	 *            the gp param
	 * @return true, if is enabled
	 */
	public boolean isEnabled(final IParameterEditor<?> gpParam) {
		return true;
	}

	/**
	 * Gets the item expanded.
	 *
	 * @param object
	 *            the object
	 * @return the item expanded
	 */
	public boolean getItemExpanded(final String object) {
		// By default
		return GamaPreferences.Runtime.CORE_EXPAND_PARAMS.getValue();
	}

}
