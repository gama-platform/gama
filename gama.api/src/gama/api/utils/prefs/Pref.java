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
 * {@code Pref<T>} is the fundamental building block of the GAMA preference system. Each instance represents a single
 * named, typed user preference and implements the {@link IParameter} contract so that preferences can participate in
 * the GAML parameter infrastructure (e.g. appear in the parameters view and be read/written from GAML via the
 * built-in {@code platform} species).
 *
 * <p>
 * A preference has:
 * <ul>
 * <li>A unique string {@link #key} that identifies it in the backing store and in GAML.</li>
 * <li>A {@link #type} encoded as a GAML type identifier (e.g. {@code IType.INT}).</li>
 * <li>A lazily-evaluated {@link #valueProvider} that computes the current value on demand.</li>
 * <li>An optional list of acceptable values ({@link #valuesProvider}), a range
 * ({@link #min}/{@link #max}/{@link #step}), UI labels ({@link #labels}), and colors ({@link #colors}).</li>
 * <li>Placement metadata: a {@link #tab} and a {@link #group} that determine where the preference appears in the
 * preferences dialog.</li>
 * <li>Behavioral flags: {@link #disabled}, {@link #hidden}, {@link #restartRequired}, {@link #isWorkspace},
 * {@link #inGaml}.</li>
 * <li>Cross-preference activation/deactivation/refresh references: {@link #enables}, {@link #disables},
 * {@link #refreshes}.</li>
 * <li>A set of {@link IPreferenceChangeListener}s that are notified before and after each value change, and can
 * veto the change.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Preferences are created exclusively via the static factory methods on {@link GamaPreferences} and are immediately
 * registered with the global {@link IGamaPreferenceStore}.
 * </p>
 *
 * @param <T>
 *            the type of the preference value (e.g. {@code Boolean}, {@code Integer}, {@code IColor})
 */
public class Pref<T> implements IParameter {

	/**
	 * A monotonically increasing creation counter used to preserve the registration order of preferences when
	 * iterating the store. Preferences with a smaller {@code order} value were created earlier.
	 */
	private final long order = COUNTER.COUNT();

	/**
	 * Whether this preference is accessible from GAML as a variable of the built-in {@code platform} species.
	 * Set at construction time and immutable.
	 */
	private final boolean inGaml;

	/**
	 * The unique preference key used as the key in the backing store and in GAML.
	 * Also serves as the {@link IParameter#getName()} value.
	 */
	String key, title;

	/**
	 * The name of the preferences dialog tab this preference belongs to (e.g. {@code "Interface"}).
	 */
	private String tab;

	/**
	 * The name of the group within {@link #tab} this preference belongs to.
	 */
	private String group;

	/**
	 * An optional unit-label or comment displayed next to the preference control in the UI.
	 */
	String comment;

	/**
	 * Whether this preference is currently disabled (grayed out) in the preferences UI. Defaults to {@code false}.
	 */
	boolean disabled = false; // by default

	/**
	 * Whether this preference should be hidden from the preferences UI. Hidden preferences are still registered
	 * and functional, but are not shown to the user. Defaults to {@code false}.
	 */
	boolean hidden = false; // by default

	/**
	 * Whether changing this preference requires a restart of GAMA to take effect. Defaults to {@code false}.
	 */
	boolean restartRequired = false; // by default

	/**
	 * Whether this preference is scoped to the current workspace (i.e. its file-system paths must be children
	 * of the workspace root). Defaults to {@code false}.
	 */
	boolean isWorkspace = false;

	/**
	 * A lazily-evaluated supplier that provides the current value of this preference. May be {@code null} before
	 * the preference has been initialized.
	 */
	Supplier<T> valueProvider;

	/**
	 * The GAML type identifier of this preference's value (one of the {@code IType.*} constants).
	 */
	final int type;

	/**
	 * An optional supplier that returns the list of permitted values for this preference (used for combo-box
	 * controls). {@code null} means any value of type {@code T} is accepted.
	 */
	Supplier<List<T>> valuesProvider;

	/**
	 * The minimum permitted value (inclusive) for numeric preferences. {@code null} means no lower bound.
	 */
	Comparable min, max, step;

	/**
	 * Whether a slider control should be used for numeric preferences when both {@link #min} and {@link #max}
	 * are set. Defaults to {@code true}.
	 */
	boolean slider = true; // by default

	/**
	 * Arrays of preference keys that are enabled, disabled, or refreshed when this preference changes its value.
	 * These are used by the UI to implement conditional activation of related preferences.
	 */
	String[] enables = EMPTY_STRINGS, disables = EMPTY_STRINGS, refreshes = EMPTY_STRINGS,
			fileExtensions = EMPTY_STRINGS;

	/**
	 * The default boolean labels used in the UI when the preference type is {@code IType.BOOL} and no custom
	 * labels have been set via {@link #withLabels(String...)}.
	 */
	static String[] PREF_SWITCH_STRINGS = { "Yes", "No" };

	/**
	 * The UI labels used for the preference's allowed values. Defaults to {@link #PREF_SWITCH_STRINGS} (Yes/No).
	 * Can be overridden via {@link #withLabels(String...)}.
	 */
	String[] labels = PREF_SWITCH_STRINGS;

	/**
	 * A supplier that returns the list of colors associated with each of the allowed values. Used for color-coded
	 * controls. Defaults to an empty list.
	 */
	Supplier<List<IColor>> colors = Collections::emptyList;

	/**
	 * The set of change listeners registered on this preference. Each listener is notified before and after each
	 * value change, and can veto the change in the {@code before} phase.
	 */
	Set<IPreferenceChangeListener<T>> listeners = new HashSet<>();

	/**
	 * Constructs a new {@code Pref} with the given key, GAML type identifier, and GAML visibility flag. The
	 * preference is not yet initialized (no value, no tab, no group); call the builder-style methods
	 * ({@link #named(String)}, {@link #init(Object)}, {@link #in(String, String)}, etc.) to complete
	 * configuration before registering it with the preference store.
	 *
	 * @param key
	 *            the unique string key identifying this preference in the backing store and in GAML; must not be
	 *            {@code null}
	 * @param type
	 *            the GAML type identifier for this preference's value (one of the {@code IType.*} constants)
	 * @param inGaml
	 *            {@code true} if this preference should be accessible from GAML as a variable of the
	 *            {@code platform} built-in species
	 */
	public Pref(final String key, final int type, final boolean inGaml) {
		this.type = type;
		this.key = key;
		this.inGaml = inGaml;
	}

	/**
	 * Marks this preference as disabled in the preferences UI (grayed out and not editable). This is a builder
	 * method that returns {@code this} for fluent chaining.
	 *
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> disabled() {
		disabled = true;
		return this;
	}

	/**
	 * Returns {@code true} if this preference is currently disabled (grayed out) in the preferences UI.
	 *
	 * @return {@code true} if disabled, {@code false} otherwise
	 */
	public boolean isDisabled() { return disabled; }

	/**
	 * Registers an {@link IPreferenceAfterChangeListener} that will be notified each time this preference's
	 * value is successfully committed. This is a convenience shorthand for
	 * {@link #addChangeListener(IPreferenceChangeListener)} that only listens to the "after" phase. Returns
	 * {@code this} for fluent chaining.
	 *
	 * @param consumer
	 *            the listener to call after each successful value change; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> onChange(final IPreferenceAfterChangeListener<T> consumer) {
		addChangeListener(consumer);
		return this;
	}

	@Override
	public long getOrder() { return order; }

	/**
	 * Restricts this preference to the given set of allowed values (varargs overload). The values are wrapped in
	 * a fixed-size list and stored as the {@link #valuesProvider}. Returns {@code this} for fluent chaining.
	 *
	 * @param v
	 *            the allowed values; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> among(@SuppressWarnings ("unchecked") final T... v) {
		return among(Arrays.asList(v));
	}

	/**
	 * Restricts this preference to the given list of allowed values. The list is stored directly in the
	 * {@link #valuesProvider}. Returns {@code this} for fluent chaining.
	 *
	 * @param v
	 *            the list of allowed values; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> among(final List<T> v) {
		this.valuesProvider = () -> v;
		return this;
	}

	/**
	 * Restricts this preference to values supplied lazily by the given {@link Supplier}. The supplier is
	 * invoked each time the list of allowed values is needed (e.g., when the combo box is opened). Returns
	 * {@code this} for fluent chaining.
	 *
	 * @param v
	 *            a supplier that provides the current list of allowed values; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> among(final Supplier<List<T>> v) {
		this.valuesProvider = v;
		return this;
	}

	/**
	 * Sets an inclusive numeric range for this preference. The {@link #min} and {@link #max} fields are updated.
	 * Pass {@code null} for either bound to indicate an open-ended range. Returns {@code this} for fluent chaining.
	 *
	 * @param mini
	 *            the lower bound (inclusive), or {@code null} for no lower bound
	 * @param maxi
	 *            the upper bound (inclusive), or {@code null} for no upper bound
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> between(final Comparable mini, final Comparable maxi) {
		this.min = mini;
		this.max = maxi;
		return this;
	}

	/**
	 * Sets the increment step used when this preference is displayed as a slider or a spinner. Returns
	 * {@code this} for fluent chaining.
	 *
	 * @param step
	 *            the increment step; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> step(final Comparable step) {
		this.step = step;
		return this;
	}

	/**
	 * Places this preference in the given {@code tab} and {@code group} of the preferences dialog. Returns
	 * {@code this} for fluent chaining.
	 *
	 * @param category
	 *            the name of the preferences tab (e.g. {@code "Interface"}); must not be {@code null}
	 * @param aGroup
	 *            the name of the group within that tab; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> in(final String category, final String aGroup) {
		this.setTab(category);
		this.setGroup(aGroup);
		return this;
	}

	/**
	 * Sets an optional comment or unit label displayed next to the preference control in the UI. Returns
	 * {@code this} for fluent chaining.
	 *
	 * @param aComment
	 *            the comment string to display; may be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> withComment(final String aComment) {
		setUnitLabel(aComment);
		return this;
	}

	/**
	 * Sets the human-readable title/label for this preference as shown in the preferences dialog. Returns
	 * {@code this} for fluent chaining.
	 *
	 * @param t
	 *            the display title; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> named(final String t) {
		this.title = t;
		// this.title = t + " [" + key + "]";
		return this;
	}

	/**
	 * Sets the initial (default) value of this preference to an eagerly provided value. The value is wrapped in
	 * a simple lambda supplier. This method is intended for use during preference construction; use
	 * {@link #set(Object)} to change the value and trigger listeners at runtime. Returns {@code this} for fluent
	 * chaining.
	 *
	 * @param v
	 *            the initial value; may be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> init(final T v) {
		this.valueProvider = () -> v;
		return this;
	}

	/**
	 * Sets the initial (default) value of this preference to a lazily evaluated supplier. The supplier is
	 * invoked each time the preference value is read via {@link #getValue()}, deferring potentially expensive
	 * computations until first access. Returns {@code this} for fluent chaining.
	 *
	 * @param p
	 *            a {@link Supplier} that produces the preference value on demand; may be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> init(final Supplier<T> p) {
		valueProvider = p;
		return this;
	}

	/**
	 * Attempts to change the current value of this preference to {@code value}. The change is only applied if:
	 * <ol>
	 * <li>The new value is actually different from the current value (see {@link #isValueChanged(Object)}).</li>
	 * <li>All registered {@link IPreferenceChangeListener}s accept the change in their
	 * {@link IPreferenceChangeListener#beforeValueChange(Object)} phase.</li>
	 * </ol>
	 * If both conditions are met the value provider is updated and all listeners are notified via
	 * {@link #afterChange(Object)}. Returns {@code this} for fluent chaining.
	 *
	 * @param value
	 *            the candidate new value; may be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> set(final T value) {
		if (isValueChanged(value) && acceptChange(value)) {
			this.valueProvider = () -> value;
			afterChange(value);
		}
		return this;
	}

	/**
	 * Returns {@code true} if {@code newValue} is different from the preference's current value. The comparison
	 * uses {@link Objects#equals(Object, Object)} so that {@code null} values are handled correctly.
	 *
	 * @param newValue
	 *            the candidate new value
	 * @return {@code true} if the new value differs from the current value, {@code false} otherwise
	 */
	private boolean isValueChanged(final T newValue) {
		return valueProvider == null ? newValue != null : !Objects.equals(valueProvider.get(), newValue);
	}

	/**
	 * Sets the preference keys that should be <em>enabled</em> in the UI whenever this preference changes its
	 * value (conditional activation). Returns {@code this} for fluent chaining.
	 *
	 * @param link
	 *            the preference keys to enable; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> activates(final String... link) {
		enables = link;
		return this;
	}

	/**
	 * Sets the preference keys that should be <em>disabled</em> in the UI whenever this preference changes its
	 * value (conditional deactivation). Returns {@code this} for fluent chaining.
	 *
	 * @param link
	 *            the preference keys to disable; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> deactivates(final String... link) {
		disables = link;
		return this;
	}

	/**
	 * Sets the preference keys whose UI controls should be <em>refreshed</em> (re-read and re-rendered) whenever
	 * this preference changes its value. Returns {@code this} for fluent chaining.
	 *
	 * @param link
	 *            the preference keys to refresh; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> refreshes(final String... link) {
		refreshes = link;
		return this;
	}

	/**
	 * Returns the current value of this preference by invoking the {@link #valueProvider} supplier. If the
	 * supplier is {@code null} (i.e. the preference has not been initialized), returns {@code null}.
	 *
	 * @return the current preference value, or {@code null} if not yet initialized
	 */
	public T getValue() {
		return valueProvider == null ? null : valueProvider.get();
	}

	/**
	 * Returns the GAML type identifier of this preference's value. The returned integer is one of the
	 * {@code IType.*} constants (e.g. {@link gama.api.gaml.types.IType#INT},
	 * {@link gama.api.gaml.types.IType#BOOL}, etc.).
	 *
	 * @return the GAML type identifier
	 */
	public int getTypeId() { return type; }

	@Override
	public IType<?> getType() { return Types.get(type); }

	@Override
	public String getTitle() { return title; }

	/**
	 * Returns the unique string key that identifies this preference in the backing store and in GAML.
	 *
	 * @return the preference key; never {@code null} after construction
	 */
	public String getKey() { return key; }

	/**
	 * Returns the current list of permitted values for this preference by invoking the
	 * {@link #valuesProvider} supplier, or {@code null} if no restriction has been set via
	 * {@link #among(Object...)}.
	 *
	 * @return the list of allowed values, or {@code null} if unrestricted
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
	 * Registers the given {@link IPreferenceChangeListener} on this preference. The listener will be notified
	 * via {@link IPreferenceChangeListener#beforeValueChange(Object)} and
	 * {@link IPreferenceChangeListener#afterValueChange(Object)} on each value change. Returns {@code this} for
	 * fluent chaining.
	 *
	 * @param r
	 *            the listener to register; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> addChangeListener(final IPreferenceChangeListener<T> r) {
		listeners.add(r);
		return this;
	}

	/**
	 * Removes the specified {@link IPreferenceChangeListener} from this preference. If the listener is not
	 * registered, this method does nothing.
	 *
	 * @param r
	 *            the listener to remove; must not be {@code null}
	 */
	public void removeChangeListener(final IPreferenceChangeListener<T> r) {
		listeners.remove(r);
	}

	/**
	 * Removes all registered {@link IPreferenceChangeListener}s from this preference. After this call, value
	 * changes will not trigger any listener callbacks.
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
	 * Asks each registered {@link IPreferenceChangeListener} whether the proposed value change should be
	 * accepted. Returns {@code false} as soon as any listener returns {@code false} from its
	 * {@link IPreferenceChangeListener#beforeValueChange(Object)} callback; returns {@code true} if all
	 * listeners agree.
	 *
	 * @param newValue
	 *            the candidate new value
	 * @return {@code true} if all listeners accept the change, {@code false} if at least one vetoes it
	 */
	public boolean acceptChange(final T newValue) {
		for (final IPreferenceChangeListener<T> listener : listeners) {
			if (!listener.beforeValueChange(newValue)) return false;
		}
		return true;
	}

	/**
	 * Notifies all registered {@link IPreferenceChangeListener}s that the preference value has changed.
	 * Called after the new value has been committed to {@link #valueProvider}.
	 *
	 * @param newValue
	 *            the new value that has just been assigned
	 */
	protected void afterChange(final T newValue) {
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
	 * Persists the current value of this preference to the global preference store by building a single-entry
	 * map and delegating to {@link GamaPreferences#setNewPreferences(java.util.Map)}.
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
	 * Marks this preference as hidden, meaning it will not be displayed in the GAMA preferences dialog but will
	 * still be registered and functional. Returns {@code this} for fluent chaining.
	 *
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> hidden() {
		hidden = true;
		return this;
	}

	/**
	 * Returns {@code true} if this preference is hidden from the preferences UI.
	 *
	 * @return {@code true} if hidden, {@code false} otherwise
	 */
	public boolean isHidden() { return hidden; }

	/**
	 * Marks this preference as requiring a GAMA restart before its new value takes effect. The preferences UI
	 * will inform the user accordingly. Returns {@code this} for fluent chaining.
	 *
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> restartRequired() {
		restartRequired = true;
		return this;
	}

	/**
	 * Returns {@code true} if a GAMA restart is required before this preference's new value takes effect.
	 *
	 * @return {@code true} if a restart is required, {@code false} otherwise
	 */
	public boolean isRestartRequired() { return restartRequired; }

	/**
	 * Returns {@code true} if this preference is accessible from GAML as a variable of the built-in
	 * {@code platform} species.
	 *
	 * @return {@code true} if the preference is accessible in GAML, {@code false} otherwise
	 */
	public boolean inGaml() {
		return inGaml;
	}

	/**
	 * Returns the list of colors associated with the allowed values of this preference, as provided by the
	 * {@link #colors} supplier. Used by color-coded UI controls (e.g. toggle buttons). Returns an empty list
	 * if no colors have been set via {@link #withColors(Supplier...)}.
	 *
	 * @param scope
	 *            the current GAMA scope (unused, may be {@code null})
	 * @return a list of {@link IColor} instances; never {@code null}
	 */
	@Override
	public List<IColor> getColors(final IScope scope) {
		return colors.get();
	}

	/**
	 * Returns the first color in the colors list, or {@code null} if no colors have been set. Convenience
	 * shorthand for {@code getColors(scope).get(0)} with a null guard.
	 *
	 * @param scope
	 *            the current GAMA scope (unused, may be {@code null})
	 * @return the first {@link IColor}, or {@code null}
	 */
	@Override
	public IColor getColor(final IScope scope) {
		List<IColor> cc = colors.get();
		return cc == null || cc.isEmpty() ? null : cc.get(0);
	}

	@Override
	public boolean isDefinedInExperiment() { return false; }

	/**
	 * Returns {@code true} if this preference represents a file-system path that must be a child of the current
	 * GAMA workspace root. When {@code true}, the file-chooser UI control will restrict navigation to the
	 * workspace.
	 *
	 * @return {@code true} if restricted to the workspace, {@code false} otherwise
	 */
	@Override
	public boolean isWorkspace() { return isWorkspace; }

	/**
	 * Marks this preference as workspace-scoped, meaning its value must be a path within the current GAMA
	 * workspace. Returns {@code this} for fluent chaining.
	 *
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> restrictToWorkspace() {
		isWorkspace = true;
		return this;
	}

	/**
	 * Sets the accepted file extensions for file-chooser controls associated with this preference (only
	 * relevant for preferences of type {@link gama.api.gaml.types.IType#FILE}). Returns {@code this} for
	 * fluent chaining.
	 *
	 * @param fileExtensions
	 *            the accepted file extensions without leading dot (e.g. {@code "gaml"}, {@code "experiment"});
	 *            must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> withExtensions(final String... fileExtensions) {
		this.fileExtensions = fileExtensions;
		return this;
	}

	/**
	 * Bypasses the change-listener pipeline and sets the value provider directly without firing any
	 * {@link IPreferenceChangeListener} callbacks. This is intended for internal use by the preference store
	 * when restoring a saved value without triggering side-effects.
	 *
	 * @param value
	 *            the new raw value to assign; will be cast to {@code T} at runtime
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public void setValueNoCheckNoNotification(final Object value) { this.valueProvider = () -> (T) value; }

	/**
	 * Sets the UI display labels for the allowed values of this preference. For boolean preferences the labels
	 * correspond to the {@code true} and {@code false} states respectively; for list preferences each label
	 * corresponds to the value at the same index. Returns {@code this} for fluent chaining.
	 *
	 * @param strings
	 *            the display labels in the same order as the corresponding values; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	public Pref<T> withLabels(final String... strings) {
		labels = strings;
		return this;
	}

	/**
	 * Sets the colors associated with each allowed value of this preference. Each color is supplied lazily via
	 * a {@link Supplier} to avoid eager construction. The colors list is built by invoking all suppliers when
	 * needed. Returns {@code this} for fluent chaining.
	 *
	 * @param gcs
	 *            the color suppliers, one per allowed value in the same order; must not be {@code null}
	 * @return {@code this} preference, for method chaining
	 */
	@SafeVarargs
	public final Pref<T> withColors(final Supplier<IColor>... gcs) {
		colors = () -> StreamEx.of(gcs).map(Supplier::get).toList();
		return this;
	}

	/**
	 * Returns the UI display labels for the allowed values of this preference. For boolean preferences, index
	 * 0 corresponds to {@code true} and index 1 to {@code false}.
	 *
	 * @param scope
	 *            the current GAMA scope (unused, may be {@code null})
	 * @return the array of display labels; never {@code null}
	 */
	@Override
	public String[] getLabels(final IScope scope) {
		return labels;
	}

	/**
	 * Returns the name of the preferences dialog tab this preference belongs to (e.g. {@code "Interface"}).
	 *
	 * @return the tab name; may be {@code null} if not yet placed via {@link #in(String, String)}
	 */
	public String getTab() { return tab; }

	/**
	 * Sets the name of the preferences dialog tab this preference belongs to.
	 *
	 * @param tab
	 *            the new tab name; must not be {@code null}
	 */
	public void setTab(final String tab) { this.tab = tab; }

	/**
	 * Returns the name of the group within the tab that this preference belongs to.
	 *
	 * @return the group name; may be {@code null} if not yet placed via {@link #in(String, String)}
	 */
	public String getGroup() { return group; }

	/**
	 * Sets the name of the group within the tab that this preference belongs to.
	 *
	 * @param group
	 *            the new group name; must not be {@code null}
	 */
	public void setGroup(final String group) { this.group = group; }

	/**
	 * Returns the {@link Supplier} that provides the current value of this preference. This is the raw
	 * value supplier and does not go through any listener pipeline. Useful for override-resolution code in
	 * {@link GamaPreferenceStore#register(Pref)}.
	 *
	 * @return the value supplier; may be {@code null} if the preference has not been initialized
	 */
	public Supplier<T> getValueProvider() { return valueProvider; }

}