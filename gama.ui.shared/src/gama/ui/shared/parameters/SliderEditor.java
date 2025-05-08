/*******************************************************************************************************
 *
 * SliderEditor.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
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
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import gama.core.kernel.experiment.IParameter;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.dev.DEBUG;
import gama.gaml.operators.Cast;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.controls.SimpleSlider;
import gama.ui.shared.interfaces.EditorListener;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.IGamaColors;

/**
 * A slider for choosing values between a max and a min, with an optional step
 *
 * @author drogoul
 *
 */
public abstract class SliderEditor<T extends Comparable> extends AbstractEditor<T> {

	static {
		DEBUG.OFF();
	}

	/** The nb ints. */
	protected int nbInts;

	/** The slider. */
	SimpleSlider slider;

	/** The formatter. */
	final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);

	/** The Constant ITEMS. */
	private static final int[] ITEMS = { VALUE, REVERT };

	/**
	 * The Class Int.
	 */
	public static class Int extends SliderEditor<Integer> {

		/**
		 * Instantiates a new int.
		 *
		 * @param scope
		 *            the scope
		 * @param a
		 *            the a
		 * @param variable
		 *            the variable
		 * @param l
		 *            the l
		 */
		public Int(final IAgent a, final IParameter variable, final EditorListener<Integer> l) {
			super(a, variable, l);
		}

		@Override
		protected void computeFormatterParameters() {
			super.computeFormatterParameters();
			formatter.setMaximumFractionDigits(0);
			formatter.setMinimumFractionDigits(0);
			formatter.setMaximumIntegerDigits(nbInts);
			formatter.setMinimumIntegerDigits(nbInts);
			formatter.setGroupingUsed(false);
		}

		@Override
		protected Integer defaultStepValue() {
			return 1;
		}

		@Override
		protected Integer computeValue(final double position) {
			return (int) (Cast.asInt(getScope(), getMinValue()) + Math
					.round(position * (Cast.asInt(getScope(), getMaxValue()) - Cast.asInt(getScope(), getMinValue()))));
		}
	}

	/**
	 * The Class Float.
	 */
	public static class Float extends SliderEditor<Double> {

		/** The nb fracs. */
		int nbFracs;

		/**
		 * Instantiates a new float.
		 *
		 * @param scope
		 *            the scope
		 * @param a
		 *            the a
		 * @param variable
		 *            the variable
		 * @param l
		 *            the l
		 */
		public Float(final IAgent a, final IParameter variable, final EditorListener<Double> l) {
			super(a, variable, l);
		}

		@Override
		protected void computeFormatterParameters() {
			super.computeFormatterParameters();
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
			return (Cast.asFloat(getScope(), getMaxValue()) - Cast.asFloat(getScope(), getMinValue())) / 100d;
		}

		@Override
		protected Double computeValue(final double position) {
			return Cast.asFloat(getScope(), getMinValue())
					+ position * (Cast.asFloat(getScope(), getMaxValue()) - Cast.asFloat(getScope(), getMinValue()));
		}

	}

	/**
	 * Instantiates a new slider editor.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param variable
	 *            the variable
	 * @param l
	 *            the l
	 */
	public SliderEditor(final IAgent a, final IParameter variable, final EditorListener<T> l) {
		super(a, variable, l);
		computeFormatterParameters();
	}

	@Override
	protected int[] getToolItems() { return ITEMS; }

	/**
	 * Compute formatter parameters.
	 */
	protected void computeFormatterParameters() {
		final int minChars = String.valueOf(Cast.asInt(getScope(), getMinValue())).length();
		final int maxChars = String.valueOf(Cast.asInt(getScope(), getMaxValue())).length();
		nbInts = Math.max(minChars, maxChars);
	}

	@Override
	protected Control createCustomParameterControl(final Composite comp) throws GamaRuntimeException {
		final List<GamaColor> colors = getParam().getColors(getScope());
		Color left = IGamaColors.OK.color();
		Color backgroundColor = comp.getBackground();
		Color right = ThemeHelper.isDark() ? GamaColors.get(backgroundColor).lighter()
				: GamaColors.get(backgroundColor).darker();
		Color thumb = left;
		if (colors != null) {
			if (colors.size() == 1) {
				left = thumb = GamaColors.get(colors.get(0)).color();
			} else if (colors.size() == 2) {
				left = GamaColors.get(colors.get(0)).color();
				right = GamaColors.get(colors.get(1)).color();
			} else if (colors.size() >= 3) {
				left = GamaColors.get(colors.get(0)).color();
				thumb = GamaColors.get(colors.get(1)).color();
				right = GamaColors.get(colors.get(2)).color();
			}
		}
		slider = new SimpleSlider(comp, left, right, thumb, false);

		computeSliderStep();

		slider.addPositionChangeListener((s, position) -> modifyAndDisplayValue(computeValue(position)));
		slider.pack(true);
		return slider;
	}

	/**
	 * Compute slider step.
	 */
	private void computeSliderStep() {
		if (getStepValue() != null) {
			final Double realStep = ((Number) getStepValue()).doubleValue()
					/ (Cast.asFloat(getScope(), getMaxValue()) - Cast.asFloat(getScope(), getMinValue()));
			slider.setStep(realStep);
		}
	}

	@Override
	protected void computeMaxMinAndStepValues() {
		super.computeMaxMinAndStepValues();
		if (slider != null) {
			computeSliderStep();
			computeFormatterParameters();
			displayParameterValue();
			updateToolbar();
		}
	}

	@Override
	protected Object getEditorControlGridData() {
		final Object result = super.getEditorControlGridData();
		if (result instanceof GridData gd) { gd.heightHint = 18 /* SimpleSlider.THUMB_HEIGHT + 5 */; }
		return result;
	}

	/**
	 * Compute value.
	 *
	 * @param position
	 *            the position
	 * @return the t
	 */
	protected abstract T computeValue(final double position);

	@Override
	protected void displayParameterValue() {
		final T p = currentValue;
		final double position = (Cast.asFloat(getScope(), p) - Cast.asFloat(getScope(), getMinValue()))
				/ (Cast.asFloat(getScope(), getMaxValue()) - Cast.asFloat(getScope(), getMinValue()));
		slider.updateSlider(position, false);
		composite.layout();
	}

	@Override
	protected void updateToolbar() {
		super.updateToolbar();
		// Avoids invisible exception (see #2044)
		// DEBUG.OUT(
		// "Value of " + currentValue + " formatted: " + formatter.format(currentValue == null ? 0 : currentValue)
		// + " / Max Fracs : " + formatter.getMaximumFractionDigits());
		editorToolbar.updateValue(formatter.format(currentValue == null ? 0 : currentValue));
	}

}
