/*******************************************************************************************************
 *
 * IntEditor.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import gama.core.kernel.experiment.IParameter;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.ui.shared.interfaces.EditorListener;

/**
 * The Class IntEditor.
 */
public class IntEditor extends NumberEditor<Integer> {

	/**
	 * Instantiates a new int editor.
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
	IntEditor(final IAgent agent, final IParameter param, final boolean canBeNull, final EditorListener<Integer> l) {
		super(agent, param, l, canBeNull);
	}

	@Override
	protected Integer defaultStepValue() {
		return 1;
	}

	@Override
	protected Integer applyPlus() {
		if (currentValue == null) return 0;
		final Integer i = currentValue;
		return i + getStepValue().intValue();
	}

	@Override
	protected Integer applyMinus() {
		if (currentValue == null) return 0;
		final Integer i = currentValue;
		return i - getStepValue().intValue();
	}

	@Override
	protected boolean modifyValue(final Object val) throws GamaRuntimeException {
		final int i = Cast.asInt(getScope(), val);
		if (getMinValue() != null && i < Cast.asInt(getScope(), getMinValue()))
			throw GamaRuntimeException.error("Value " + i + " should be greater than " + getMinValue(), getScope());
		if (getMaxValue() != null && i > Cast.asInt(getScope(), getMaxValue()))
			throw GamaRuntimeException.error("Value " + i + " should be smaller than " + getMaxValue(), getScope());
		return super.modifyValue(i);
	}

	@Override
	protected void updateToolbar() {
		super.updateToolbar();
		editorToolbar.enable(PLUS,
				param.isDefined() && (getMaxValue() == null || applyPlus() < Cast.asInt(getScope(), getMaxValue())));
		editorToolbar.enable(MINUS,
				param.isDefined() && (getMinValue() == null || applyMinus() > Cast.asInt(getScope(), getMinValue())));
	}

	@Override
	protected Integer normalizeValues() throws GamaRuntimeException {
		final Integer valueToConsider = getOriginalValue() == null ? 0 : Cast.asInt(getScope(), getOriginalValue());
		currentValue = getOriginalValue() == null ? null : valueToConsider;
		return valueToConsider;
	}

	@Override
	public IType<Integer> getExpectedType() { return Types.INT; }

}
