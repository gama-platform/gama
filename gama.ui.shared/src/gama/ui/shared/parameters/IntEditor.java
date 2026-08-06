/*******************************************************************************************************
 *
 * IntEditor.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.ui.shared.interfaces.EditorListener;

/**
 * The Class IntEditor.
 */
public class IntEditor extends NumberEditor<Long> {

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
	IntEditor(final IAgent agent, final IParameter param, final boolean canBeNull, final EditorListener<Long> l) {
		super(agent, param, l, canBeNull);
	}

	@Override
	protected Long defaultStepValue() {
		return 1l;
	}

	@Override
	protected Long applyPlus() {
		if (currentValue == null) return 0l;
		final Long i = currentValue;
		return i + getStepValue().longValue();
	}

	@Override
	protected Long applyMinus() {
		if (currentValue == null) return 0l;
		final Long i = currentValue;
		return i - getStepValue().longValue();
	}

	@Override
	protected boolean modifyValue(final Object val) throws GamaRuntimeException {
		final long i = Cast.asInt(getScope(), val);
		if (getMinValue() != null && i < Cast.asInt(getScope(), getMinValue()))
			throw GamaRuntimeException.error("Value " + i + " should be greater than " + getMinValue(), getScope());
		if (getMaxValue() != null && i > Cast.asInt(getScope(), getMaxValue()))
			throw GamaRuntimeException.error("Value " + i + " should be smaller than " + getMaxValue(), getScope());
		return super.modifyValue(i);
	}

	@Override
	protected void updateToolbar() {
		super.updateToolbar();
		// Disable + and - if the value is among a set of values
		if (param.getAmongValue(getScope()) != null) return;
		editorToolbar.enable(PLUS,
				param.isDefined() && (getMaxValue() == null || applyPlus() < Cast.asInt(getScope(), getMaxValue())));
		editorToolbar.enable(MINUS,
				param.isDefined() && (getMinValue() == null || applyMinus() > Cast.asInt(getScope(), getMinValue())));
	}

	@Override
	protected Long normalizeValues() throws GamaRuntimeException {
		final Long valueToConsider = getOriginalValue() == null ? 0l : Cast.asInt(getScope(), getOriginalValue());
		currentValue = getOriginalValue() == null ? null : valueToConsider;
		return valueToConsider;
	}

	@Override
	public IType<Long> getExpectedType() { return Types.INT; }

}
