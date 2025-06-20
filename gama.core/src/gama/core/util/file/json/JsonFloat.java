/*******************************************************************************************************
 *
 * JsonFloat.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file.json;

import java.io.IOException;

import gama.core.runtime.IScope;
import gama.gaml.operators.Cast;

/**
 * The Class JsonNumber.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 29 oct. 2023
 */
@SuppressWarnings ("serial") // use default serial UID
class JsonFloat extends JsonValue {

	/** The Constant DEFAULT_NUMBER_OF_DIGITS. */
	private static final int DEFAULT_NUMBER_OF_DIGITS = 8;
	/** The string. */
	private final String string;

	/**
	 * Instantiates a new json number.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param string
	 *            the string
	 * @date 29 oct. 2023
	 */
	JsonFloat(final double x) {
		this(formatOrdinate(x, DEFAULT_NUMBER_OF_DIGITS));
	}

	/**
	 * Instantiates a new json number.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param string
	 *            the string
	 * @date 29 oct. 2023
	 */
	JsonFloat(final String string) {
		if (string == null) throw new NullPointerException("string is null");
		this.string = string;
	}

	/**
	 * Format ordinate.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param ordinate
	 *            the ordinate
	 * @return the string
	 * @date 4 nov. 2023
	 */
	public static String formatOrdinate(double x, final int numberOfDigits) {
		if (Double.isInfinite(x)) {
			return (x > 0 ? "" : "-") + "Infinity";
		}
		double scale = Math.pow(10, numberOfDigits);
		if (Math.abs(x) >= Math.pow(10, -3) && x < Math.pow(10, 7)) { x = Math.floor(x * scale + 0.5) / scale; }
		return Double.toString(x);

	}

	/**
	 * To string.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @date 29 oct. 2023
	 */
	@Override
	public String toString() {
		return string;
	}

	/**
	 * Write.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param writer
	 *            the writer
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @date 29 oct. 2023
	 */
	@Override
	void write(final JsonWriter writer) throws IOException {
		writer.writeNumber(string);
	}

	/**
	 * Checks if is number.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is number
	 * @date 29 oct. 2023
	 */
	@Override
	public boolean isNumber() { return true; }

	/**
	 * As int.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the int
	 * @date 29 oct. 2023
	 */
	@Override
	public int asInt() {
		return (int) asDouble();
	}

	/**
	 * As long.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the long
	 * @date 29 oct. 2023
	 */
	@Override
	public long asLong() {
		return Long.parseLong(string, 10);
	}

	/**
	 * As float.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the float
	 * @date 29 oct. 2023
	 */
	@Override
	public float asFloat() {
		return (float) asDouble();
	}

	/**
	 * As double.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the double
	 * @date 29 oct. 2023
	 */
	@Override
	public double asDouble() {
		if(string != null && string.contains("Infinity")) {
			return string.startsWith("-") ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		}
		return Cast.asFloat(null, string);
	}

	/**
	 * Hash code.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the int
	 * @date 29 oct. 2023
	 */
	@Override
	public int hashCode() {
		return string.hashCode();
	}

	/**
	 * Equals.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @return true, if successful
	 * @date 29 oct. 2023
	 */
	@Override
	public boolean equals(final Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		JsonFloat other = (JsonFloat) object;
		return string.equals(other.string);
	}

	/**
	 * To gaml value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the object
	 * @date 2 nov. 2023
	 */
	@Override
	public Number toGamlValue(final IScope scope) {
		return asDouble();
	}

}
