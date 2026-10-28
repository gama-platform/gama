/*******************************************************************************************************
 *
 * FloatUnitFormatter.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import gama.api.gaml.symbols.IParameter.DisplayUnit;

/** Shared presentation of float units, without changing the values sent to the model. */
final class FloatUnitFormatter {

	private FloatUnitFormatter() {}

	static String format(final Double value, final Number step, final DisplayUnit unit) {
		return format(value, step, unit, false);
	}

	static String formatForSizing(final Double value, final Number step, final DisplayUnit unit) {
		return format(value, step, unit, true);
	}

	private static String format(final Double value, final Number step, final DisplayUnit unit, final boolean padding) {
		if (value == null) return "";
		final double converted = value / unit.factor();
		final int digits = precision(step, unit.factor());
		final boolean scientific = step == null && converted != 0 && Double.isFinite(converted)
				&& (Math.abs(converted) < 1e-6 || Math.abs(converted) >= 1e12);
		final DecimalFormat format = new DecimalFormat(scientific ? "0.######E0" : "0",
				DecimalFormatSymbols.getInstance(Locale.US));
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(digits);
		if (padding) { format.setMinimumFractionDigits(digits); }
		return format.format(converted) + (unit.label().isEmpty() ? "" : " " + unit.label());
	}

	private static int precision(final Number value, final double factor) {
		if (value == null || !Double.isFinite(value.doubleValue())) return 6;
		final BigDecimal step = BigDecimal.valueOf(value.doubleValue()).stripTrailingZeros();
		try {
			return Math.max(2, step.divide(BigDecimal.valueOf(factor)).stripTrailingZeros().scale());
		} catch (final ArithmeticException repeatingDecimal) {
			// Converting e.g. 0.25 seconds into hours must not expose the repeating decimal expansion.
			return Math.max(2, step.scale());
		}
	}
}
