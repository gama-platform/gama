/*******************************************************************************************************
 *
 * FloatEditor.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import gama.core.kernel.experiment.IParameter;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.ui.shared.interfaces.EditorListener;

/**
 * The Class FloatEditor.
 */
public class FloatEditor extends NumberEditor<Double> {

	/** The formatter. */
	final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);

	/** The nb ints. */
	int nbInts, nbFracs;

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
		computeFormatterParameters();
	}

	/**
	 * Compute formatter parameters.
	 */
	protected void computeFormatterParameters() {
		final int minChars = String.valueOf(Cast.asInt(getScope(), getMinValue())).length();
		final int maxChars = String.valueOf(Cast.asInt(getScope(), getMaxValue())).length();
		nbInts = Math.max(minChars, maxChars);
		formatter.setMaximumIntegerDigits(nbInts);
		formatter.setMinimumIntegerDigits(nbInts);
		String s = String.valueOf(getStepValue());
		s = s.contains(".") ? s.replaceAll("0*$", "").replaceAll("\\.$", "") : s;
		final String[] segments = s.split("\\.");
		if (segments.length > 1) {
			nbFracs = segments[1].length();
		} else {
			nbFracs = 1;
		}
		formatter.setMaximumFractionDigits(nbFracs);
		formatter.setMinimumFractionDigits(nbFracs);
		formatter.setGroupingUsed(false);

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

	@Override
	public boolean formatsValue() {
		return true;
	}

	/**
	 * Value formatted.
	 *
	 * @param value
	 *            the value
	 * @return the string
	 */
	@Override
	public String valueFormatted(final Object value) {
		return formatter.format(value == null ? 0 : value);
	}
}
