package gama.ui.shared.parameters;

import gama.api.gaml.symbols.IParameter.DisplayUnit;

/** Standalone regression checks; no SWT display is needed. */
public final class FloatUnitFormatterTest {

	private static void check(final String expected, final Double value, final Number step,
			final DisplayUnit unit) {
		final String actual = FloatUnitFormatter.format(value, step, unit);
		if (!expected.equals(actual)) throw new AssertionError("Expected '" + expected + "', got '" + actual + "'");
	}

	public static void main(final String[] args) {
		final DisplayUnit hours = new DisplayUnit("h", 3600d);
		check("2 h", 7200d, 3600d, hours);
		check("2.25 h", 8100d, 900d, hours);
		check("-0.5 h", -1800d, 900d, hours);
		check("0 h", 0d, 900d, hours);
		check("1.25 h", 4500d, 1800d, hours);
		check("1.25 h", 4500d, 3600d, hours);
		check("10 h", 36000d, 3600d, hours);
		check("2.25 h", 8100d, null, hours);
		check("1.75 items", 1.75d, null, new DisplayUnit("items", 1d));
		check("7200 h", 7200d, 1d, new DisplayUnit("h", 1d));
		check("1.5 km", 1500d, null, new DisplayUnit("km", 1000d));
		check("1E-20 m", 1e-20, null, new DisplayUnit("m", 1d));
		check("0.00000000000000000001 m", 1e-20, 1e-20, new DisplayUnit("m", 1d));
		check("2", 2d, null, new DisplayUnit("", 1d));
		check("", null, null, hours);
		check("5.59 h", 20121.75d, 0.25d, hours);
		check("9.75 h", 35114.25d, 0.25d, hours);
		check("1.5 km", 1500d, 1d, new DisplayUnit("km", 1000d));
		check("36 km/h", 10d, 0.25d, new DisplayUnit("km/h", 1000d / 3600d));
		check("2.5 items", 2.500000000000001d, 0.1d, new DisplayUnit("items", 1d));
	}
}
