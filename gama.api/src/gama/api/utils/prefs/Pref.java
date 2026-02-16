/*******************************************************************************************************
 *
 * Pref.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.prefs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.utils.StringUtils;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.dev.COUNTER;
import one.util.streamex.StreamEx;

/**
 * The Class Pref.
 *
 * @param <T>
 *            the generic type
 */
public class Pref<T> implements IParameter {

	/** The order. */
	private final int order = COUNTER.COUNT();

	/** The in gaml. */
	private final boolean inGaml;

	/** The comment. */
	String key, title;

	/** The tab. */
	private String tab;

	/** The group. */
	private String group;

	/** The comment. */
	String comment;

	/** The disabled. */
	boolean disabled = false; // by default

	/** The hidden. */
	boolean hidden = false; // by default

	/** The restart required. */
	boolean restartRequired = false; // by default

	/** The is workspace. */
	boolean isWorkspace = false;

	/** The initial provider. */
	Supplier<T> valueProvider;

	/** The initial. */
	// T value, initial;

	/** The type. */
	final int type;

	/** The values provider. */
	Supplier<List<T>> valuesProvider;

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
	Supplier<List<IColor>> colors = Collections::emptyList;

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
	public Pref(final String key, final int type, final boolean inGaml) {
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
	public Pref<T> among(final Supplier<List<T>> v) {
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
		this.setTab(category);
		this.setGroup(aGroup);
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
		this.valueProvider = () -> v;
		// this.initial = v;
		// this.value = v;
		return this;
	}

	/**
	 * Inits the.
	 *
	 * @param p
	 *            the p
	 * @return the pref
	 */
	public Pref<T> init(final Supplier<T> p) {
		valueProvider = p;
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
			this.valueProvider = () -> value;
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
		return valueProvider == null ? newValue != null : !Objects.equals(valueProvider.get(), newValue);

		// return value == null ? newValue != null : !value.equals(newValue);
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

		return valueProvider == null ? null : valueProvider.get();

		// if (valueProvider != null) {
		// init(valueProvider.get());
		// valueProvider = null;
		// }
		// return value;
	}

	/**
	 * Gets the type id.
	 *
	 * @return the type id
	 */
	public int getTypeId() { return type; }

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
	public String getCategory() { return getGroup(); }

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
		return StringUtils.toGaml(valueProvider.get(), includingBuiltIn);
	}

	@Override
	public Object getInitialValue(final IScope scope) {
		return getValue();
		// if (valueProvider != null) {
		// init(valueProvider.get());
		// valueProvider = null;
		// }
		// return initial;
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
		valueProvider = null;
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
		final Map<String, Object> map = new HashMap<>();
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
	public List<IColor> getColors(final IScope scope) {
		return colors.get();
	}

	@Override
	public IColor getColor(final IScope scope) {
		List<IColor> cc = colors.get();
		return cc == null || cc.isEmpty() ? null : cc.get(0);
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
	public void setValueNoCheckNoNotification(final Object value) { this.valueProvider = () -> (T) value; }

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
	@SafeVarargs
	public final Pref<T> withColors(final Supplier<IColor>... gcs) {
		colors = () -> StreamEx.of(gcs).map(Supplier::get).toList();
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

	/**
	 * Gets the tab.
	 *
	 * @return the tab
	 */
	public String getTab() { return tab; }

	/**
	 * Sets the tab.
	 *
	 * @param tab
	 *            the new tab
	 */
	public void setTab(final String tab) { this.tab = tab; }

	/**
	 * Gets the group.
	 *
	 * @return the group
	 */
	public String getGroup() { return group; }

	/**
	 * Sets the group.
	 *
	 * @param group
	 *            the new group
	 */
	public void setGroup(final String group) { this.group = group; }

	/**
	 * Gets the initial provider.
	 *
	 * @return the initial provider
	 */
	public Supplier<T> getValueProvider() { return valueProvider; }

}