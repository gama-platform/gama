/*******************************************************************************************************
 *
 * IParameter.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.symbols;

import java.util.List;
import java.util.Set;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IExperimentDisplayable;

/**
 * The interface for GAML parameters - variables that can be configured and controlled from experiment interfaces.
 * 
 * <p>
 * Parameters are special variables that can be displayed and modified in experiment user interfaces, allowing users to
 * explore different model configurations without modifying code. They support various UI widgets (sliders, choosers,
 * text fields, file pickers, etc.) based on their type and constraints.
 * </p>
 * 
 * <h2>Parameter Features</h2>
 * <ul>
 * <li><strong>UI Integration</strong> - Automatic generation of UI controls in experiment panels</li>
 * <li><strong>Constraints</strong> - Support for min/max values, step sizes, and discrete value sets</li>
 * <li><strong>Change Notifications</strong> - Listeners can observe and react to parameter value changes</li>
 * <li><strong>Editability Control</strong> - Parameters can be marked as read-only or editable</li>
 * <li><strong>Inter-parameter Dependencies</strong> - Parameters can enable/disable/refresh other parameters</li>
 * </ul>
 * 
 * <h2>UI Widget Types</h2>
 * <p>
 * The UI widget is determined by the parameter's type and facets:
 * </p>
 * <ul>
 * <li><strong>Slider</strong> - Numeric parameters with min/max/step</li>
 * <li><strong>Chooser</strong> - Parameters with an 'among' list of discrete values</li>
 * <li><strong>Switch</strong> - Boolean parameters (True/False toggle)</li>
 * <li><strong>File Picker</strong> - File/directory parameters with extension filters</li>
 * <li><strong>Text Field</strong> - General input for strings and other types</li>
 * </ul>
 * 
 * <h2>Batch Experiments</h2>
 * <p>
 * The {@link Batch} inner interface extends parameters for use in batch/exploration experiments, adding support for
 * parameter space exploration, random initialization, and neighbor generation for optimization algorithms.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see IVariable
 * @see IExperimentDisplayable
 */
public interface IParameter extends IExperimentDisplayable {

	/**
	 * Listener interface for observing parameter value changes.
	 * 
	 * <p>
	 * Classes implementing this interface can register with a parameter to be notified whenever the parameter's value
	 * changes. This is useful for UI updates, logging, validation, or triggering dependent computations.
	 * </p>
	 * 
	 * <p>
	 * Listeners are typically registered using {@link #addChangedListener(ParameterChangeListener)}.
	 * </p>
	 */
	public interface ParameterChangeListener {

		/**
		 * Called when the parameter value has changed.
		 *
		 * @param scope
		 *            the execution scope in which the change occurred
		 * @param newValue
		 *            the new parameter value
		 */
		void changed(IScope scope, Object newValue);
	}

	/** Empty string array constant for parameters without special UI options. */
	String[] EMPTY_STRINGS = {};

	/** String array for boolean switch parameters (True/False). */
	String[] SWITCH_STRINGS = { "True", "False" };

	/**
	 * Sets the value of this parameter.
	 * 
	 * <p>
	 * This method performs type checking and validation before setting the value. It also triggers change notifications
	 * to registered listeners and executes any on_change actions defined for the parameter.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param value
	 *            the new parameter value
	 */
	void setValue(IScope scope, Object value);

	/**
	 * Sets the parameter value without validation or notifications.
	 * 
	 * <p>
	 * This method bypasses type checking and does not trigger listeners or on_change actions. It should only be used in
	 * special circumstances where the overhead of normal validation is not needed, such as during initialization or
	 * batch loading.
	 * </p>
	 *
	 * @param value
	 *            the new value to set directly
	 */
	void setValueNoCheckNoNotification(Object value);

	/**
	 * Gets the current value of this parameter.
	 * 
	 * <p>
	 * For regular parameters, this returns the stored value. For computed parameters, this may evaluate an expression.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the current parameter value
	 * @throws GamaRuntimeException
	 *             if reading the value fails
	 */
	Object value(IScope scope) throws GamaRuntimeException;

	/**
	 * Gets the type of this parameter.
	 * 
	 * <p>
	 * The type determines how the parameter value is validated, displayed, and what UI widget is appropriate.
	 * </p>
	 *
	 * @return the GAML type of this parameter
	 */
	@SuppressWarnings ("rawtypes")
	IType getType();

	/**
	 * Serializes this parameter to GAML source code.
	 *
	 * @param includingBuiltIn
	 *            whether to include built-in parameters in the serialization
	 * @return the GAML source code representation
	 */
	@Override
	String serializeToGaml(boolean includingBuiltIn);

	/**
	 * Gets the initial value for this parameter.
	 * 
	 * <p>
	 * This is the value specified in the 'init' facet, evaluated in the provided scope. For batch experiments, this may
	 * be a starting point for exploration.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the initial value, or null if not defined
	 */
	Object getInitialValue(IScope scope);

	/**
	 * Gets the minimum allowed value for this parameter.
	 * 
	 * <p>
	 * For numeric parameters, this defines the lower bound for UI widgets like sliders and for validation. Specified
	 * using the 'min' facet.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the minimum value, or null if not constrained
	 */
	Object getMinValue(IScope scope);

	/**
	 * Gets the maximum allowed value for this parameter.
	 * 
	 * <p>
	 * For numeric parameters, this defines the upper bound for UI widgets like sliders and for validation. Specified
	 * using the 'max' facet.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the maximum value, or null if not constrained
	 */
	Object getMaxValue(IScope scope);

	/**
	 * Gets the list of allowed discrete values for this parameter.
	 * 
	 * <p>
	 * When specified using the 'among' facet, this parameter can only take values from this list. The UI will typically
	 * display a chooser/dropdown widget.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the list of allowed values, or null if not constrained
	 */
	@SuppressWarnings ("rawtypes")
	List getAmongValue(IScope scope);

	/**
	 * Checks if this parameter can be edited in the UI.
	 * 
	 * <p>
	 * Non-editable parameters are displayed but cannot be modified by the user. This is useful for showing computed
	 * values or read-only information.
	 * </p>
	 *
	 * @return true if the parameter can be edited, false otherwise
	 */
	boolean isEditable();

	/**
	 * Checks if this parameter can be displayed with a slider widget.
	 * 
	 * <p>
	 * Typically true for numeric parameters that have min and max values defined. The 'slider' facet can also force or
	 * prevent slider display.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope for evaluating constraints
	 * @return true if a slider is appropriate, false otherwise
	 */
	boolean acceptsSlider(IScope scope);

	/**
	 * Gets the step size for slider or stepper widgets.
	 * 
	 * <p>
	 * Defines the increment/decrement amount when using slider or arrow controls. Specified using the 'step' facet.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the step value, or null for default behavior
	 */
	Comparable getStepValue(IScope scope);

	/**
	 * Checks if this parameter has been defined (given a value).
	 * 
	 * <p>
	 * This is used to distinguish between parameters that have never been set and those set to null or a default value.
	 * </p>
	 *
	 * @return true if the parameter has been defined, false otherwise
	 */
	boolean isDefined();

	/**
	 * Gets the list of parameter names that this parameter enables when set to true.
	 * 
	 * <p>
	 * Only valid for boolean parameters. When this parameter is set to true, the listed parameters become enabled in
	 * the UI. This is specified using the 'enables' facet.
	 * </p>
	 *
	 * @return array of parameter names to enable, or empty array if none
	 */
	default String[] getEnablement() { return EMPTY_STRINGS; }

	/**
	 * Gets the list of parameter names that this parameter disables when set to true.
	 * 
	 * <p>
	 * Only valid for boolean parameters. When this parameter is set to true, the listed parameters become disabled in
	 * the UI. This is specified using the 'disables' facet.
	 * </p>
	 *
	 * @return array of parameter names to disable, or empty array if none
	 */
	default String[] getDisablement() { return EMPTY_STRINGS; }

	/**
	 * Gets the list of parameter names that should be refreshed when this parameter changes.
	 * 
	 * <p>
	 * When this parameter's value changes, the listed parameters will have their values recomputed and their UI
	 * representations updated. This is useful for dependent parameters. Specified using the 'refreshes' facet.
	 * </p>
	 *
	 * @return array of parameter names to refresh, or empty array if none
	 */
	default String[] getRefreshment() { return EMPTY_STRINGS; }

	/**
	 * Gets the allowed file extensions for file/directory parameters.
	 * 
	 * <p>
	 * For parameters with type 'file', this specifies which file types can be selected in the file picker dialog.
	 * Extensions should be in the format "txt", "shp", "csv", etc. (without the dot).
	 * </p>
	 *
	 * @return array of file extensions, or empty array for all files
	 */
	default String[] getFileExtensions() { return EMPTY_STRINGS; }

	/**
	 * Adds a listener to be notified when this parameter's value changes.
	 * 
	 * <p>
	 * The listener's {@link ParameterChangeListener#changed(IScope, Object)} method will be called whenever the
	 * parameter value is modified.
	 * </p>
	 *
	 * @param listener
	 *            the listener to register
	 */
	default void addChangedListener(final ParameterChangeListener listener) {
		// Nothing to do by default
	}

	/**
	 * Extended interface for parameters used in batch and exploration experiments.
	 * 
	 * <p>
	 * Batch parameters support parameter space exploration, enabling systematic or stochastic search through parameter
	 * combinations. They are used in calibration, optimization, and sensitivity analysis experiments.
	 * </p>
	 * 
	 * <h3>Key Features</h3>
	 * <ul>
	 * <li>Category-based organization for exploration algorithms</li>
	 * <li>Random initialization for stochastic exploration</li>
	 * <li>Neighbor generation for local search and hill climbing</li>
	 * <li>Exploration enablement control</li>
	 * </ul>
	 */
	public interface Batch extends IParameter {

		/**
		 * Gets the current value without requiring a scope.
		 * 
		 * <p>
		 * Batch experiments may need to access parameter values outside of normal execution contexts, particularly when
		 * generating parameter combinations or during exploration setup.
		 * </p>
		 *
		 * @return the current parameter value
		 */
		Object value();

		/**
		 * Sets the exploration category for this parameter.
		 * 
		 * <p>
		 * Categories are used by exploration algorithms to group parameters and control exploration strategies. Common
		 * categories include those explored together or with specific algorithms.
		 * </p>
		 *
		 * @param cat
		 *            the category name
		 */
		void setCategory(String cat);

		/**
		 * Reinitializes this parameter with a random value.
		 * 
		 * <p>
		 * Used in stochastic exploration algorithms to generate random starting points or to restart exploration from
		 * different initial conditions. The random value respects the parameter's constraints (min, max, among, etc.).
		 * </p>
		 *
		 * @param scope
		 *            the execution scope
		 */
		void reinitRandomly(IScope scope);

		/**
		 * Generates neighboring values for this parameter.
		 * 
		 * <p>
		 * Used in local search and hill-climbing algorithms to explore the neighborhood around the current value. The
		 * definition of "neighbor" depends on the parameter type:
		 * </p>
		 * <ul>
		 * <li>Numeric: values within a step distance</li>
		 * <li>Discrete: adjacent values in the 'among' list</li>
		 * <li>Boolean: the opposite value</li>
		 * </ul>
		 *
		 * @param scope
		 *            the execution scope
		 * @return the set of neighbor values
		 * @throws GamaRuntimeException
		 *             if neighbor generation fails
		 */
		Set<Object> neighborValues(IScope scope) throws GamaRuntimeException;

		/**
		 * Sets whether this parameter can be edited during batch execution.
		 *
		 * @param b
		 *            true to allow editing, false to make read-only
		 */
		void setEditable(boolean b);

		/**
		 * Checks if this parameter can be explored (included in parameter space exploration).
		 * 
		 * <p>
		 * Some parameters may be fixed during exploration or excluded from the search space. This method indicates
		 * whether exploration algorithms should vary this parameter.
		 * </p>
		 *
		 * @return true if the parameter can be explored, false if it should remain fixed
		 */
		boolean canBeExplored();

	}

	/**
	 * Checks if file/directory selection should be restricted to the workspace.
	 * 
	 * <p>
	 * Only valid for file and directory parameters. When true, the file picker dialog will only allow selection of
	 * files within the current GAMA workspace, preventing access to arbitrary filesystem locations.
	 * </p>
	 *
	 * @return true if restricted to workspace, false to allow any location
	 */
	default boolean isWorkspace() { return false; }

	/**
	 * Sets whether this parameter has been defined (given a value).
	 * 
	 * <p>
	 * This is used internally to track parameter initialization state. A defined parameter has had a value set, even if
	 * that value is null.
	 * </p>
	 *
	 * @param b
	 *            true to mark as defined, false to mark as undefined
	 */
	void setDefined(boolean b);

	/**
	 * Gets the display labels for parameter values.
	 * 
	 * <p>
	 * For parameters with discrete values, this provides human-readable labels for each value. For boolean parameters,
	 * this defaults to ["True", "False"]. Can be customized to show more descriptive text in the UI.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return array of labels corresponding to parameter values
	 */
	default String[] getLabels(final IScope scope) {
		return SWITCH_STRINGS;
	}

	/**
	 * Checks if the parameter value should be kept as an expression rather than evaluated.
	 * 
	 * <p>
	 * Some parameters may need to store the expression itself rather than its evaluated result. This is useful for
	 * meta-programming scenarios or when the expression needs to be evaluated in different contexts.
	 * </p>
	 *
	 * @return true if the value is kept as an expression, false if it should be evaluated
	 */
	default boolean isExpression() { return false; }

}
