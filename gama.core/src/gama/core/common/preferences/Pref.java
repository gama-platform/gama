/*******************************************************************************************************
 *
 * Pref.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.preferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gama.core.common.preferences.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.core.common.util.StringUtils;
import gama.core.kernel.experiment.IParameter;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaMapFactory;
import gama.dev.COUNTER;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Pref.
 *
 * @param <T>
 *            the generic type
 */
public class Pref<T> implements IParameter {

	/**
	 * The Interface ValueProvider.
	 *
	 * @param <T>
	 *            the generic type
	 */
	@FunctionalInterface
	public interface ValueProvider<T> {

		/**
		 * Gets the.
		 *
		 * @return the t
		 */
		T get();
	}

	/** The order. */
	private final int order = COUNTER.COUNT();

	/** The in gaml. */
	private final boolean inGaml;

	/** The comment. */
	String key, title, tab, group, comment;

	/** The disabled. */
	boolean disabled = false; // by default

	/** The hidden. */
	boolean hidden = false; // by default

	/** The restart required. */
	boolean restartRequired = false; // by default

	/** The is workspace. */
	boolean isWorkspace = false;

	/** The initial provider. */
	ValueProvider<T> initialProvider;

	/** The initial. */
	T value, initial;

	/** The type. */
	final int type;

	/** The values provider. */
	ValueProvider<List<T>> valuesProvider;

	/** The values. */
	// List<T> values;

	/** The max. */
	Comparable min, max, step;

	/** The slider. */
	boolean slider = true; // by default

	/** The refreshes. */
	String[] enables = EMPTY_STRINGS, disables = EMPTY_STRINGS, refreshes = EMPTY_STRINGS,
			fileExtensions = EMPTY_STRINGS;

	/** The pref switch strings. */
	static String[] PREF_SWITCH_STRINGS = { "Yes", "No" };
	/** The labels. */
	String[] labels = PREF_SWITCH_STRINGS;

	/** The colors. */
	List<GamaColor> colors = null;

	/** The listeners. */
	Set<IPreferenceChangeListener<T>> listeners = new HashSet<>();
	// private T[] v;

	/**
	 * Instantiates a new pref.
	 *
	 * @param key
	 *            the key
	 * @param type
	 *            the type
	 * @param inGaml
	 *            the in gaml
	 */
	Pref(final String key, final int type, final boolean inGaml) {
		this.type = type;
		this.key = key;
		this.inGaml = inGaml;
	}

	/**
	 * Disabled.
	 *
	 * @return the pref
	 */
	public Pref<T> disabled() {
		disabled = true;
		return this;
	}

	/**
	 * Checks if is disabled.
	 *
	 * @return true, if is disabled
	 */
	public boolean isDisabled() { return disabled; }

	/**
	 * On change.
	 *
	 * @param consumer
	 *            the consumer
	 * @return the pref
	 */
	public Pref<T> onChange(final IPreferenceAfterChangeListener<T> consumer) {
		addChangeListener(consumer);
		return this;
	}

	@Override
	public int getOrder() { return order; }

	/**
	 * Among.
	 *
	 * @param v
	 *            the v
	 * @return the pref
	 */
	public Pref<T> among(@SuppressWarnings ("unchecked") final T... v) {
		return among(Arrays.asList(v));
	}

	/**
	 * Among.
	 *
	 * @param v
	 *            the v
	 * @return the pref
	 */
	public Pref<T> among(final List<T> v) {
		this.valuesProvider = () -> v;
		return this;
	}

	/**
	 * Amoing.
	 *
	 * @param v
	 *            the v
	 * @return the pref
	 */
	public Pref<T> among(final ValueProvider<List<T>> v) {
		this.valuesProvider = v;
		return this;
	}

	/**
	 * Between.
	 *
	 * @param mini
	 *            the mini
	 * @param maxi
	 *            the maxi
	 * @return the pref
	 */
	public Pref<T> between(final Comparable mini, final Comparable maxi) {
		this.min = mini;
		this.max = maxi;
		return this;
	}

	/**
	 * Step.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param step
	 *            the step
	 * @return the pref
	 * @date 13 août 2023
	 */
	public Pref<T> step(final Comparable step) {
		this.step = step;
		return this;
	}

	/**
	 * In.
	 *
	 * @param category
	 *            the category
	 * @param aGroup
	 *            the a group
	 * @return the pref
	 */
	public Pref<T> in(final String category, final String aGroup) {
		this.tab = category;
		this.group = aGroup;
		return this;
	}

	/**
	 * With comment.
	 *
	 * @param aComment
	 *            the a comment
	 * @return the pref
	 */
	public Pref<T> withComment(final String aComment) {
		setUnitLabel(aComment);
		return this;
	}

	/**
	 * Named.
	 *
	 * @param t
	 *            the t
	 * @return the pref
	 */
	public Pref<T> named(final String t) {
		this.title = t;
		// this.title = t + " [" + key + "]";
		return this;
	}

	/**
	 * Inits the.
	 *
	 * @param v
	 *            the v
	 * @return the pref
	 */
	public Pref<T> init(final T v) {
		this.initial = v;
		this.value = v;
		return this;
	}

	/**
	 * Inits the.
	 *
	 * @param p
	 *            the p
	 * @return the pref
	 */
	public Pref<T> init(final ValueProvider<T> p) {
		initialProvider = p;
		return this;
	}

	/**
	 * Sets the.
	 *
	 * @param value
	 *            the value
	 * @return the pref
	 */
	public Pref<T> set(final T value) {
		if (isValueChanged(value) && acceptChange(value)) {
			this.value = value;
			afterChange(value);
		}
		return this;
	}

	/**
	 * Checks if is value changed.
	 *
	 * @param newValue
	 *            the new value
	 * @return true, if is value changed
	 */
	private boolean isValueChanged(final T newValue) {
		return value == null ? newValue != null : !value.equals(newValue);
	}

	/**
	 * Activates.
	 *
	 * @param link
	 *            the link
	 * @return the pref
	 */
	public Pref<T> activates(final String... link) {
		enables = link;
		return this;
	}

	/**
	 * Deactivates.
	 *
	 * @param link
	 *            the link
	 * @return the pref
	 */
	public Pref<T> deactivates(final String... link) {
		disables = link;
		return this;
	}

	/**
	 * Refreshes.
	 *
	 * @param link
	 *            the link
	 * @return the pref
	 */
	public Pref<T> refreshes(final String... link) {
		refreshes = link;
		return this;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public T getValue() {
		if (initialProvider != null) {
			init(initialProvider.get());
			initialProvider = null;
		}
		return value;
	}

	@Override
	public IType<?> getType() { return Types.get(type); }

	@Override
	public String getTitle() { return title; }

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() { return key; }

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public List<T> getValues() { return valuesProvider == null ? null : valuesProvider.get(); }

	@Override
	public String getName() { return key; }

	@Override
	public String getCategory() { return group; }

	@Override
	public String getUnitLabel(final IScope scope) {
		return comment;
	}

	@Override
	public void setUnitLabel(final String label) { comment = label; }

	@SuppressWarnings ("unchecked")
	@Override
	public void setValue(final IScope scope, final Object value) {
		set((T) value);
	}

	/**
	 * Adds the change listener.
	 *
	 * @param r
	 *            the r
	 * @return the pref
	 */
	public Pref<T> addChangeListener(final IPreferenceChangeListener<T> r) {
		listeners.add(r);
		return this;
	}

	/**
	 * Removes the change listener.
	 *
	 * @param r
	 *            the r
	 */
	public void removeChangeListener(final IPreferenceChangeListener<T> r) {
		listeners.remove(r);
	}

	/**
	 * Removes the change listeners.
	 */
	public void removeChangeListeners() {
		listeners.clear();
	}

	@Override
	public T value(final IScope scope) throws GamaRuntimeException {
		return getValue();
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return StringUtils.toGaml(value, includingBuiltIn);
	}

	@Override
	public Object getInitialValue(final IScope scope) {
		if (initialProvider != null) {
			init(initialProvider.get());
			initialProvider = null;
		}
		return initial;
	}

	@Override
	public Comparable getMinValue(final IScope scope) {
		return min;
	}

	@Override
	public Comparable getMaxValue(final IScope scope) {
		return max;
	}

	@Override
	public List getAmongValue(final IScope scope) {
		return valuesProvider == null ? null : valuesProvider.get();
	}

	@Override
	public boolean isEditable() { return true; }

	@Override
	public boolean isDefined() { return true; }

	@Override
	public void setDefined(final boolean b) {}

	@Override
	public Comparable getStepValue(final IScope scope) {
		return step;
	}

	/**
	 * If the value is modified, this method is called. Should return true to accept the change, false otherwise
	 */
	public boolean acceptChange(final T newValue) {
		for (final IPreferenceChangeListener<T> listener : listeners) {
			if (!listener.beforeValueChange(newValue)) return false;
		}
		return true;
	}

	/**
	 * After change.
	 *
	 * @param newValue
	 *            the new value
	 */
	protected void afterChange(final T newValue) {
		initialProvider = null;
		for (final IPreferenceChangeListener<T> listener : listeners) { listener.afterValueChange(newValue); }
	}

	@Override
	public String[] getEnablement() { return this.enables; }

	@Override
	public String[] getDisablement() { return this.disables; }

	@Override
	public String[] getRefreshment() { return this.refreshes; }

	@Override
	public String[] getFileExtensions() { return this.fileExtensions; }

	/**
	 * Save.
	 */
	public void save() {
		final Map<String, Object> map = GamaMapFactory.createUnordered();
		map.put(getName(), getValue());
		GamaPreferences.setNewPreferences(map);
	}

	@Override
	public boolean acceptsSlider(final IScope scope) {
		return slider;
	}

	/**
	 * Hidden.
	 *
	 * @return the pref
	 */
	public Pref<T> hidden() {
		hidden = true;
		return this;
	}

	/**
	 * Checks if is hidden.
	 *
	 * @return true, if is hidden
	 */
	public boolean isHidden() { return hidden; }

	/**
	 * Restart required.
	 *
	 * @return the pref
	 */
	public Pref<T> restartRequired() {
		restartRequired = true;
		return this;
	}

	/**
	 * Checks if is restart required.
	 *
	 * @return true, if is restart required
	 */
	public boolean isRestartRequired() { return restartRequired; }

	/**
	 * In gaml.
	 *
	 * @return true, if successful
	 */
	public boolean inGaml() {
		return inGaml;
	}

	@Override
	public List<GamaColor> getColors(final IScope scope) {
		return colors;
	}

	@Override
	public GamaColor getColor(final IScope scope) {
		return colors == null || colors.isEmpty() ? null : colors.get(0);
	}

	@Override
	public boolean isDefinedInExperiment() { return false; }

	/**
	 * Checks if is workspace.
	 *
	 * @return true, if is workspace
	 */
	@Override
	public boolean isWorkspace() { return isWorkspace; }

	/**
	 * Restric to workspace.
	 *
	 * @return the pref
	 */
	public Pref<T> restrictToWorkspace() {
		isWorkspace = true;
		return this;
	}

	/**
	 * With extensions.
	 *
	 * @param fileExtensions
	 *            the file extensions
	 * @return the pref
	 */
	public Pref<T> withExtensions(final String... fileExtensions) {
		this.fileExtensions = fileExtensions;
		return this;
	}

	/**
	 * Sets the value no check no notification.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param value
	 *            the new value no check no notification
	 * @date 13 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public void setValueNoCheckNoNotification(final Object value) { this.value = (T) value; }

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	public Pref<T> withLabels(final String... strings) {
		labels = strings;
		return this;
	}

	/**
	 * With colors.
	 *
	 * @param gcs
	 *            the gcs
	 * @return the pref
	 */
	public Pref<T> withColors(final GamaColor... gcs) {
		colors = Arrays.asList(gcs);
		return this;
	}

	/**
	 * Gets the labels.
	 *
	 * @return the labels
	 */
	@Override
	public String[] getLabels(final IScope scope) {
		return labels;
	}

}