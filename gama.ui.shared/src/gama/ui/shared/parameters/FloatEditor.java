/*******************************************************************************************************
 *
 * FloatEditor.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.InputParameter;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.ui.shared.interfaces.EditorListener;

/**
 * The Class FloatEditor.
 */
public class FloatEditor extends NumberEditor<Double> {

	/**
	 * Instantiates a new float editor.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param param
	 *            the param
	 * @param canBeNull
	 *            the can be null
	 * @param l
	 *            the l
	 */
	FloatEditor(final IAgent agent, final IParameter param, final boolean canBeNull, final EditorListener<Double> l) {
		super(agent, param, l, canBeNull);
	}

	/**
	 * Instantiates a new float editor.
	 *
	 * @param scope
	 *            the scope
	 * @param parent
	 *            the parent
	 * @param title
	 *            the title
	 * @param value
	 *            the value
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param step
	 *            the step
	 * @param canBeNull
	 *            the can be null
	 * @param whenModified
	 *            the when modified
	 */
	FloatEditor(final IScope scope, final EditorsGroup parent, final String title, final Double value, final Double min,
			final Double max, final Double step, final boolean canBeNull, final EditorListener<Double> whenModified) {
		// Convenience method
		super(scope.getAgent(), new InputParameter(title, value, min, max, step), whenModified, canBeNull);
		this.createControls(parent);
	}

	@Override
	protected Double defaultStepValue() {
		return 0.1d;
	}

	@Override
	protected boolean modifyValue(final Object val) throws GamaRuntimeException {
		Double i = Cast.asFloat(getScope(), val);
		if (acceptNull && val == null) {
			i = null;
		} else {
			if (getMinValue() != null && i < Cast.asFloat(getScope(), getMinValue()))
				throw GamaRuntimeException.error("Value " + i + " should be greater than " + getMinValue(), getScope());
			if (getMaxValue() != null && i > Cast.asFloat(getScope(), getMaxValue()))
				throw GamaRuntimeException.error("Value " + i + " should be smaller than " + getMaxValue(), getScope());
		}
		return super.modifyValue(i);

	}

	@Override
	protected Double normalizeValues() throws GamaRuntimeException {
		final Double valueToConsider = getOriginalValue() == null ? 0.0 : Cast.asFloat(getScope(), getOriginalValue());
		currentValue = getOriginalValue() == null ? null : valueToConsider;
		return valueToConsider;
	}

	@Override
	public IType<Double> getExpectedType() { return Types.FLOAT; }

	@Override
	protected Double applyPlus() {
		if (currentValue == null) return 0.0;
		final Double i = currentValue;
		return i + getStepValue().doubleValue();
	}

	@Override
	protected Double applyMinus() {
		if (currentValue == null) return 0.0;
		final Double i = currentValue;
		return i - getStepValue().doubleValue();
	}

	@Override
	protected void updateToolbar() {
		super.updateToolbar();
		editorToolbar.enable(PLUS,
				param.isDefined() && (getMaxValue() == null || applyPlus() < Cast.asFloat(getScope(), getMaxValue())));
		editorToolbar.enable(MINUS,
				param.isDefined() && (getMinValue() == null || applyMinus() > Cast.asFloat(getScope(), getMinValue())));
	}
}
