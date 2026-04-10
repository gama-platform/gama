/*******************************************************************************************************
 *
 * Maths.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.matrix.IField;
import gama.api.types.matrix.IMatrix;
import gama.api.utils.MathUtils;

/**
 * Provides all mathematical and arithmetic operators for the GAML language.
 *
 * <p>This class covers the following operator families:</p>
 * <ul>
 *   <li><b>Arithmetic:</b> {@code +}, {@code -}, {@code *}, {@code /}, {@code ^}, {@code mod}, {@code div}</li>
 *   <li><b>Absolute/Sign:</b> {@code abs}, unary {@code -}</li>
 *   <li><b>Rounding:</b> {@code round}, {@code floor}, {@code ceil}, {@code truncated}, {@code with_precision}</li>
 *   <li><b>Trigonometry (degrees):</b> {@code sin}, {@code cos}, {@code tan}, {@code asin}, {@code acos}, {@code atan}, {@code atan2}</li>
 *   <li><b>Trigonometry (radians):</b> {@code sin_rad}, {@code cos_rad}, {@code tan_rad}</li>
 *   <li><b>Hyperbolic:</b> {@code tanh}</li>
 *   <li><b>Exponential/Logarithm:</b> {@code exp}, {@code ln}, {@code log}, {@code sqrt}, {@code ^}</li>
 *   <li><b>Combinatorics:</b> {@code fact}, {@code even}</li>
 *   <li><b>Extrema:</b> {@code min}, {@code max}</li>
 *   <li><b>Predicates:</b> {@code is_number}, {@code is_finite}, {@code is_infinite}</li>
 *   <li><b>Matrix arithmetic:</b> scalar–matrix multiplication and addition</li>
 * </ul>
 *
 * <p><b>Angle convention:</b> unless the operator name ends in {@code _rad}, all
 * trigonometric arguments and results are in <em>decimal degrees</em>.</p>
 *
 * <p><b>Domain errors:</b> operators like {@code sqrt}, {@code ln}, and {@code log}
 * report a runtime warning (via {@link gama.api.GAMA#reportAndThrowIfNeeded}) when
 * called with out-of-domain values and return {@code NaN} or throw an exception
 * depending on the error-handling policy of the current scope.</p>
 *
 * @author Alexis Drogoul
 * @see Comparison
 * @see Random
 */
@SuppressWarnings ({ "rawtypes" })
public class Maths {

	/**
	 * Pow.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = { "^" },
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (always a float) of the left operand raised to the power of the right operand.",
			masterDoc = true,
			returns = "a {@code float}.",
			special_cases = {
					"If the right-hand operand is 0, returns 1.0 regardless of the left operand (including 0^0 = 1.0).",
					"If the right-hand operand is 1, returns the left-hand operand cast to float.",
					"If the left operand is negative and the right operand is a non-integer float (e.g. -4^0.5), returns NaN."
			},
			usages = { @usage ("if the right-hand operand is equal to 0, returns 1"),
					@usage ("if it is equal to 1, returns the left-hand operand."), @usage (
							value = "Various examples of power",
							examples = { @example (
									value = "2 ^ 3",
									equals = "8.0") }) },
			see = { "*", "sqrt" })
	@test ("8^0 = 1.0")
	@test ("2^2 = 4.0")
	@test ("0^0 = 1.0")
	@test ("(-2)^2 = 4.0")
	@test ("2^1 = 2.0")
	public static Double pow(final Integer a, final Integer b) {
		return pow(a.doubleValue(), b.doubleValue());
	}

	/**
	 * Pow.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = { "^" },
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the value (always a float) of the left operand raised to the power of the right operand.")
	@test ("4.0^2 = 16.0")
	@test ("8.0^0 = 1.0")
	@test ("8.0^1 = 8.0")
	public static Double pow(final Double a, final Integer b) {
		return pow(a, b.doubleValue());
	}

	/**
	 * Pow.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = { "^" },
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the value (always a float) of the left operand raised to the power of the right operand.")

	@test ("2^0.5 = sqrt(2)")
	@test ("2^0.0 = 1.0")
	@test ("2^1.0 = 2.0")
	public static Double pow(final Integer a, final Double b) {
		return pow(a.doubleValue(), b);
	}

	/**
	 * Pow.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = { "^" },
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the value (always a float) of the left operand raised to the power of the right operand.",
			usages = { @usage (
					value = "",
					examples = { @example (
							value = "4.84 ^ 0.5",
							equals = "2.2") }) })
	@test ("16.81^0.5 = sqrt(16.81)")
	public static Double pow(final Double a, final Double b) {
		return Math.pow(a, b);
	}

	// ==== Operators

	/**
	 * Abs.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "abs",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the absolute value of the operand (so a positive int or float depending on the type of the operand).",
			masterDoc = true,
			usages = { @usage (
					value = "",
					examples = { @example (
							value = "abs (200 * -1 + 0.5)",
							equals = "199.5") }) })
	@test ("abs(1.9) = 1.9")
	@test ("abs(-2.0) = 2.0")
	@test ("abs(0.0) = 0.0")
	@test ("abs(-0.0) = 0.0")
	public static Double abs(final Double rv) {
		return Math.abs(rv);
	}

	/**
	 * Abs.
	 *
	 * @param rv
	 *            the rv
	 * @return the integer
	 */
	@operator (
			value = "abs",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the absolute value of the operand (so a positive int or float depending on the type of the operand).",
			usages = { @usage (
					value = "",
					examples = { @example (
							value = "abs (-10)",
							equals = "10"),
							@example (
									value = "abs (10)",
									equals = "10") }) })
	@test ("abs(1) = 1")
	@test ("abs(-2) = 2")
	@test ("abs(0) = 0")
	@test ("abs(-0) = 0")
	public static Integer abs(final Integer rv) {
		int a = rv.intValue();
		return (a ^ a >> 31) - (a >> 31);
	}

	/**
	 * Acos.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "acos",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in the interval [0,180], in decimal degrees) of the arccos of the operand (which should be in [-1,1]).",
			masterDoc = true,
			usages = { @usage (
					value = "if the right-hand operand is outside of the [-1,1] interval, returns NaN") },
			examples = @example (
					value = "acos (0)",
					equals = "90.0"),
			see = { "asin", "atan", "cos" })
	@test ("acos(0.0) = 90.0")
	@test ("acos(-1.0) = 180.0")
	@test ("acos(1.0) = 0.0")
	@test ("not(is_number(acos(-10.0)))")
	@test ("not(is_number(acos(10.0)))")
	public static Double acos(final Double rv) {
		return Math.acos(rv) * toDeg;
	}

	/**
	 * Acos.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "acos",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the arccos of the operand (result in decimal degrees in [0, 180])",
			special_cases = { "if the operand is outside of [-1,1], returns NaN." })
	@test ("acos(0) = 90.0")
	@test ("acos(-1) = 180.0")
	@test ("acos(1) = 0.0")
	@test ("not(is_number(acos(-10)))")
	@test ("not(is_number(acos(10)))")
	public static Double acos(final Integer rv) {
		return Math.acos(rv) * toDeg;
	}

	/**
	 * Asin.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "asin",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in the interval [-90,90], in decimal degrees) of the arcsin of the operand (which should be in [-1,1]).",
			usages = { @usage (
					value = "if the right-hand operand is outside of the [-1,1] interval, returns NaN") },
			examples = @example (
					value = "asin (0)",
					equals = "0.0"),
			see = { "acos", "atan", "sin" })
	@test ("asin(0.0) = 0.0")
	@test ("asin(-1.0) = -90.0")
	@test ("asin(1.0) = 90.0")
	@test ("not(is_number(asin(-10.0)))")
	@test ("not(is_number(asin(10.0)))")
	public static Double asin(final Double rv) {
		return Math.asin(rv) * toDeg;
	}

	/**
	 * Asin.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "asin",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the arcsin of the operand",
			masterDoc = true,
			examples = @example (
					value = "asin (90)",
					equals = "#nan",
					test = false),
			see = { "acos", "atan" })
	@test ("asin(0) = 0.0")
	@test ("asin(-1) = -90.0")
	@test ("asin(1) = 90.0")
	@test ("not(is_number(asin(-10)))")
	@test ("not(is_number(asin(10)))")
	public static Double asin(final Integer rv) {
		return Math.asin(rv) * toDeg;
	}

	/**
	 * Atan.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "atan",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in the interval [-90,90], in decimal degrees) of the arctan of the operand (which can be any real number).",
			masterDoc = true,
			examples = @example (
					value = "atan (1)",
					equals = "45.0"),
			see = { "acos", "asin", "tan" })
	@test ("atan(0.0) = 0.0")
	@test ("atan(-1.0) = -45.0")
	@test ("atan(1.0) = 45.0")
	public static Double atan(final Double rv) {
		return Math.atan(rv) * toDeg;
	}

	/**
	 * Atan.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "atan",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the arctan of the operand (result in decimal degrees in [-90, 90]).",
			returns = "a {@code float} in [-90, 90].")
	@test ("atan(0) = 0.0")
	@test ("atan(-1) = -45.0")
	@test ("atan(1) = 45.0")
	public static Double atan(final Integer rv) {
		return Math.atan(rv) * toDeg;
	}

	/**
	 * Tanh.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "tanh",
			can_be_const = true,
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in the interval [-1,1]) of the hyperbolic tangent of the operand (which can be any real number, expressed in decimal degrees).",
			masterDoc = true,
			returns = "a {@code float} in [-1.0, 1.0].",
			special_cases = {
					"tanh(0) = 0.0",
					"As the operand tends to +∞, tanh approaches 1.0; as it tends to -∞, it approaches -1.0."
			},
			examples = { @example (
					value = "tanh(0)",
					equals = "0.0"),
					@example (
							value = "tanh(100)",
							equals = "1.0") })
	@test ("tanh(0.0) = 0.0")
	@test ("tanh(100.0) = 1.0")
	@test ("tanh(-100.0) = -1.0")
	public static Double tanh(final Double rv) {
		return Math.tanh(rv);
	}

	/**
	 * Tanh.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "tanh",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the hyperbolic tangent of the operand (which has to be expressed in decimal degrees).",
			returns = "a {@code float} in [-1.0, 1.0].")
	@test ("tanh(100) = 1.0")
	@test ("tanh(0) = 0.0")
	@test ("tanh(-100) = -1.0")
	public static Double tanh(final Integer rv) {
		return Math.tanh(rv);
	}

	/**
	 * Cos rad.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	// hqnghi & Tri 14/04/2013
	@operator (
			value = "cos_rad",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in [-1,1]) of the cosinus of the operand (in radians). ",
			masterDoc = true,
			special_cases = "Operand values out of the range [0-359] are normalized.",
			see = { "sin", "tan" },
			examples = { @example (
					value = "cos_rad(0.0)",
					equals = "1.0"),
					@example (
							value = "cos_rad(#pi)",
							equals = "-1.0") })
	public static Double cos_rad(final Double rv) {
		return Math.cos(rv);
	}

	/**
	 * Sin rad.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "sin_rad",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in [-1,1]) of the sinus of the operand (in radians). ",
			masterDoc = true,
			examples = { @example (
					value = "sin_rad(0)",
					equals = "0.0"),
					@example (
							value = "sin_rad(#pi/2)",
							equals = "1.0") },
			see = { "cos_rad", "tan_rad" })
	public static Double sin_rad(final Double rv) {
		return Math.sin(rv);
	}

	/**
	 * Tan rad.
	 *
	 * @param v
	 *            the v
	 * @return the double
	 */
	@operator (
			value = "tan_rad",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in [-1,1]) of the trigonometric tangent of the operand (in radians). ",
			masterDoc = true,
			examples = { @example (
					value = "tan_rad(0)",
					equals = "0.0") },
			see = { "cos_rad", "sin_rad" })
	public static Double tan_rad(final Double v) {
		return Math.tan(v);
	}

	// end hqnghi & Tri 14/04/2013

	/**
	 * Cos.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "cos",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in [-1,1]) of the cosinus of the operand (in decimal degrees).  The argument is casted to an int before being evaluated.",
			masterDoc = true,
			returns = "a {@code float} in [-1.0, 1.0].",
			special_cases = "Operand values out of the range [0-359] are normalized.",
			examples = { @example (
					value = "cos (0.0)",
					equals = "1.0"),
					@example (
							value = "cos(360.0)",
							equals = "1.0"),
					@example (
							value = "cos(-720.0)",
							equals = "1.0") },
			see = { "sin", "tan" })
	@test ("cos(0.0) = 1.0")
	@test ("cos(90.0) with_precision 10 = 0.0")
	@test ("cos(180.0) = -1.0")
	@test ("cos(360.0) = 1.0")
	@test ("cos(-180.0) = -1.0")
	public static Double cos(final Double rv) {
		return Math.cos(rv * toRad);
	}

	/**
	 * Cos.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "cos",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the cosinus of the operand in decimal degrees.",
			examples = { @example (
					value = "cos (0)",
					equals = "1.0"),
					@example (
							value = "cos(360)",
							equals = "1.0"),
					@example (
							value = "cos(-720)",
							equals = "1.0") })
	public static Double cos(final Integer rv) {
		return Math.cos(rv * toRad);
		// double rad = toRad * rv;
		// return Math.cos(rad);
	}

	/**
	 * Sin.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "sin",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in [-1,1]) of the sinus of the operand (in decimal degrees). The argument is casted to an int before being evaluated.",
			masterDoc = true,
			returns = "a {@code float} in [-1.0, 1.0].",
			usages = @usage ("Operand values out of the range [0-359] are normalized."),
			examples = { @example (
					value = "sin(360) with_precision 10 with_precision 10",
					equals = "0.0") },
			see = { "cos", "tan" })
	@test ("sin(0.0) = 0.0")
	@test ("sin(90.0) = 1.0")
	@test ("sin(-90.0) = -1.0")
	@test ("sin(180.0) with_precision 10 = 0.0")
	@test ("sin(270.0) = -1.0")
	public static Double sin(final Double rv) {
		return Math.sin(rv * toRad);
	}

	/**
	 * Sin.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "sin",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the sinus of the operand (in decimal degrees).",
			examples = { @example (
					value = "sin (0)",
					equals = "0.0") })
	public static Double sin(final Integer rv) {
		// double rad = toRad * rv;
		return Math.sin(rv * toRad);
		// double rad = rv / 180 * Math.PI;
		// return Math.sin(rad);
	}

	/**
	 * Tan.
	 *
	 * @param v
	 *            the v
	 * @return the double
	 */
	@operator (
			value = "tan",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the value (in [-1,1]) of the trigonometric tangent of the operand (in decimal degrees). ",
			masterDoc = true,
			usages = { @usage (
					value = "Operand values out of the range [0-359] are normalized. Notice that tan(360) does not return 0.0 but -2.4492935982947064E-16"),
					@usage (
							value = "The tangent is only defined for any real number except 90 + k `*` 180 (k an positive or negative integer). Nevertheless notice that tan(90) returns 1.633123935319537E16 (whereas we could except infinity).") },
			see = { "cos", "sin" })
	@test ("tan(90.0) = 1.633123935319537E16")
	@test ("tan(0.0) = 0.0")

	public static Double tan(final Double v) {
		return Math.tan(toRad * v);
	}

	/**
	 * Tan.
	 *
	 * @param v
	 *            the v
	 * @return the double
	 */
	@operator (
			value = "tan",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the value (in [-1,1]) of the trigonometric tangent of the operand (in decimal degrees). The argument is casted to an int before being evaluated.",
			examples = { @example (
					value = "tan (0)",
					equals = "0.0"),
					@example (
							value = "tan(90)",
							equals = "1.633123935319537E16") })
	public static Double tan(final Integer v) {
		return Math.tan(toRad * v);
	}

	/**
	 * Even.
	 *
	 * @param rv
	 *            the rv
	 * @return the boolean
	 */
	@operator (
			value = "even",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns true if the operand is even and false if it is odd.",
			returns = "a {@code bool}.",
			usages = { @usage (
					value = "if the operand is equal to 0, it returns true."),
					@usage (
							value = "if the operand is a float, it is truncated before") },
			examples = { @example (
					value = "even (3)",
					equals = "false"),
					@example (
							value = "even(-12)",
							equals = "true") })
	@test ("even(0)")
	@test ("even(2)")
	@test ("even(-4)")
	@test ("!even(1)")
	@test ("!even(-3)")
	public static Boolean even(final Integer rv) {
		return (rv.intValue() & 1) == 0;
	}

	/**
	 * Exp.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "exp",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns Euler's number e raised to the power of the operand.",
			masterDoc = true,
			returns = "a positive {@code float}. The result is always > 0.",
			special_cases = {
					"exp(0) = 1.0",
					"exp(1) = e ≈ 2.718281828459045",
					"For negative operands, returns a small positive value approaching 0."
			},
			usages = @usage (
					value = "the operand is casted to a float before being evaluated."),
			examples = @example (
					value = "exp (0.0)",
					equals = "1.0"),
			see = "ln")
	@test ("exp(0.0) = 1.0")
	@test ("exp(1.0) with_precision 5 = 2.71828")
	@test ("exp(-1.0) = 1.0 / exp(1.0)")
	@test ("ln(exp(1.0)) with_precision 10 = 1.0")
	public static Double exp(final Double rv) {
		return Math.exp(rv);
	}

	/**
	 * Exp.
	 *
	 * @param rv
	 *            the rv
	 * @return the double
	 */
	@operator (
			value = "exp",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "returns Euler's number e raised to the power of the operand.")
	@test ("exp (0) = 1.0")
	public static Double exp(final Integer rv) {
		return Math.exp(rv.doubleValue());
	}

	/**
	 * Fact.
	 *
	 * @param n
	 *            the n
	 * @return the double
	 */
	@operator (
			value = "fact",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the factorial of the operand.",
			returns = "a non-negative {@code float}. fact(0) = 1.0.",
			special_cases = {
					"If the operand is 0, returns 1.0 (by convention: 0! = 1).",
					"If the operand is negative, returns 0.0.",
					"For large values (e.g. n > 170), the result overflows to Infinity."
			},
			usages = @usage ("if the operand is less than 0, fact returns 0."),
			examples = {
					@example (value = "fact(4)",  equals = "24"),
					@example (value = "fact(0)",  equals = "1.0"),
					@example (value = "fact(-1)", equals = "0.0")
			})
	@test ("fact(0) = 1.0")
	@test ("fact(1) = 1.0")
	@test ("fact(4) = 24.0")
	@test ("fact(-1) = 0.0")
	@test ("fact(10) = 3628800.0")
	public static Double fact(final Integer n) {
		if (n < 0) return 0.0;
		double product = 1;
		for (int i = 2; i <= n; i++) { product *= i; }
		return product;
	}

	/**
	 * Ln.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "ln",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the natural logarithm (base e) of the operand.",
			masterDoc = true,
			returns = "a {@code float}. Returns -∞ if the operand equals 0 in strict Java math; "
					+ "in GAMA a warning is issued and -∞ may be returned depending on the error policy.",
			special_cases = {
					"ln(1) = 0.0",
					"ln(e) = 1.0",
					"If the operand is 0 or negative, a runtime warning is raised."
			},
			usages = @usage (
					value = "an exception is raised if the operand is less than zero."),
			examples = @example (
					value = "ln(exp(1))",
					equals = "1.0"),
			see = "exp")
	@test ("ln(1.0) = 0.0")
	@test ("ln(exp(1.0)) with_precision 10 = 1.0")
	@test ("ln(exp(3.0)) with_precision 5 = 3.0")
	public static Double ln(final IScope scope, final Double x) {
		if (x <= 0) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning("The ln operator cannot accept negative or null inputs", scope),
					false);
		}
		return Math.log(x);
	}

	/**
	 * Ln.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "ln",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the natural logarithm (base e) of the integer operand.",
			returns = "a {@code float}.",
			special_cases = { "If the operand is 0 or negative, a runtime warning is raised." },
			examples = {
					@example (value = "ln(1)",   equals = "0.0"),
					@example (value = "ln(10)",  equals = "2.302585092994046", test = false)
			})
	@test ("ln(1) = 0.0")
	@test ("ln(10) with_precision 5 = 2.30259")
	public static Double ln(final IScope scope, final Integer x) {
		if (x <= 0) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning("The ln operator cannot accept negative or null inputs", scope),
					false);
		}
		return Math.log(x);
	}

	/**
	 * Log.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "log",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the logarithm (base 10) of the operand.",
			masterDoc = true,
			returns = "a {@code float}. log(1) = 0.0, log(10) = 1.0, log(100) = 2.0.",
			special_cases = {
					"log(1) = 0.0",
					"If the operand is 0 or negative, a runtime warning is raised."
			},
			usages = @usage ("an exception is raised if the operand is equals or less than zero."),
			examples = {
					@example (value = "log(10)",  equals = "1.0"),
					@example (value = "log(1)",   equals = "0.0"),
					@example (value = "log(100)", equals = "2.0")
			},
			see = "ln")
	@test ("log(10.0) = 1.0")
	@test ("log(1.0) = 0.0")
	@test ("log(100.0) = 2.0")
	public static Double log(final IScope scope, final Double x) {
		if (x <= 0) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning("The log operator cannot accept negative or null inputs", scope),
					false);
		}
		return Math.log10(x.doubleValue());
	}

	/**
	 * Log.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "log",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the logarithm (base 10) of the integer operand.",
			returns = "a {@code float}.",
			special_cases = { "If the operand is 0 or negative, a runtime warning is raised." },
			examples = {
					@example (value = "log(1)",   equals = "0.0"),
					@example (value = "log(10)",  equals = "1.0")
			})
	@test ("log(1) = 0.0")
	@test ("log(10) = 1.0")
	@test ("log(100) = 2.0")
	public static Double log(final IScope scope, final Integer x) {
		if (x <= 0) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning("The log operator cannot accept negative or null inputs", scope),
					false);
		}
		return Math.log10(x);
	}

	/**
	 * Log.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = "log",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the logarithm in base {@code b} of the integer operand {@code x}.",
			returns = "a {@code float}.",
			special_cases = {
					"If x is 0 or negative, a runtime warning is raised.",
					"If b equals 1, the result is ±Infinity (undefined)."
			},
			examples = {
					@example (value = "log(100, 100)",  equals = "1.0"),
					@example (value = "log(8, 2)",      equals = "3.0"),
					@example (value = "log(1, 10)",     equals = "0.0")
			})
	@test ("log(100, 100) = 1.0")
	@test ("log(8, 2) with_precision 10 = 3.0")
	@test ("log(1, 10) = 0.0")
	public static Double log(final IScope scope, final Integer x, final Integer b) {
		if (x <= 0) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning("The log operator cannot accept negative or null inputs", scope),
					false);
		}
		return Math.log(x) / Math.log(b);
	}

	/**
	 * Log.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = "log",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "returns the logarithm in base b of the operand.",
			examples = @example (
					value = "log(100, 100.0)",
					equals = "1.0"))
	public static Double log(final IScope scope, final Integer x, final Double b) {
		if (x <= 0) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning("The log operator cannot accept negative or null inputs", scope),
					false);
		}
		return Math.log(x) / Math.log(b);
	}

	/**
	 * Log.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = "log",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "returns the logarithm in base b of the operand.",
			examples = @example (
					value = "log(100.0, 100.0)",
					equals = "1.0"))
	public static Double log(final IScope scope, final Double x, final Double b) {
		if (x <= 0) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning("The log operator cannot accept negative or null inputs", scope),
					false);
		}
		return Math.log(x) / Math.log(b);
	}

	/**
	 * Log.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = "log",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "returns the logarithm in base b of the operand.",
			examples = @example (
					value = "log(100.0, 100)",
					equals = "1.0"))
	public static Double log(final IScope scope, final Double x, final Integer b) {
		if (x <= 0) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning("The log operator cannot accept negative or null inputs", scope),
					false);
		}
		return Math.log(x) / Math.log(b);
	}

	/**
	 * Negate.
	 *
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "-",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "If it is used as a unary operator, it returns the arithmetic negation (opposite) of the operand.",
			masterDoc = true,
			returns = "a {@code float} whose value is {@code -operand}.",
			special_cases = { "-0.0 = 0.0 (IEEE 754 negative zero is equal to zero)." },
			examples = {
					@example (value = "-(3.5)",  equals = "-3.5"),
					@example (value = "-(-3.5)", equals = "3.5"),
					@example (value = "-(0.0)",  equals = "0.0")
			})
	@test ("-(-90.0) = 90.0")
	@test ("-(3.5) = -3.5")
	@test ("-(0.0) = 0.0")
	public static Double negate(final Double x) {
		return -x;
	}

	/**
	 * Negate.
	 *
	 * @param x
	 *            the x
	 * @return the integer
	 */
	@operator (
			value = "-",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the opposite of the integer operand.",
			returns = "an {@code int} equal to {@code -operand}.",
			special_cases = { "Negating Integer.MIN_VALUE overflows to Integer.MIN_VALUE." },
			examples = {
					@example (value = "- (-56)", equals = "56"),
					@example (value = "-(0)",    equals = "0")
			})
	@test ("-(-56) = 56")
	@test ("-(0) = 0")
	@test ("-(5) = -5")
	public static Integer negate(final Integer x) {
		return -x;
	}

	/**
	 * Round.
	 *
	 * @param v
	 *            the v
	 * @return the integer
	 */
	@operator (
			value = "round",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the nearest integer value of the operand, rounding half-up (away from zero for negative values).",
			masterDoc = true,
			returns = "an {@code int}.",
			special_cases = {
					"round(0.5) = 1 (half-up convention).",
					"round(-0.5) = -1 (half-away-from-zero: -0.5 rounds down to -1).",
					"round(0.0) = 0."
			},
			examples = { @example (
					value = "round (0.51)",
					equals = "1"),
					@example (
							value = "round (100.2)",
							equals = "100"),
					@example (
							value = "round(-0.51)",
							equals = "-1") },
			see = { "int", "with_precision" })
	@test ("round(0.51) = 1")
	@test ("round(0.5) = 1")
	@test ("round(-0.5) = -1")
	@test ("round(-0.51) = -1")
	@test ("round(0.0) = 0")
	@test ("round(-2.3) = -2")
	@test ("round(2.7) = 3")
	public static Integer round(final Double v) {
		int i;
		if (v >= 0) {
			i = (int) (v + .5);
		} else {
			i = (int) (v - .5);
		}
		return i;
	}

	/**
	 * Round.
	 *
	 * @param v
	 *            the v
	 * @return the integer
	 */
	@operator (
			value = "round",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the operand unchanged (integers are already rounded by definition).",
			returns = "an {@code int} equal to the operand.",
			special_cases = "if the operand is an int, round returns it unchanged.")
	@test ("round(100) = 100")
	@test ("round(0) = 0")
	@test ("round(-5) = -5")

	public static Integer round(final Integer v) {
		return v;
	}

	/**
	 * Sqrt.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "sqrt",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the square root of the operand.",
			masterDoc = true,
			returns = "a non-negative {@code float}.",
			special_cases = {
					"sqrt(0) = 0.0",
					"sqrt(1) = 1.0",
					"If the operand is negative, a runtime exception is raised."
			},
			usages = @usage (
					value = "if the operand is negative, an exception is raised"),
			examples = {
					@example (value = "sqrt(4)",  equals = "2.0"),
					@example (value = "sqrt(0)",  equals = "0.0"),
					@example (value = "sqrt(2)",  equals = "1.4142135623730951")
			})
	@test ("sqrt(4) = 2.0")
	@test ("sqrt(0) = 0.0")
	@test ("sqrt(1) = 1.0")
	@test ("sqrt(9) = 3.0")
	@test ("is_error(sqrt(-1))")
	public static Double sqrt(final IScope scope, final Integer v) throws GamaRuntimeException {
		if (v < 0) GAMA.reportAndThrowIfNeeded(scope,
				GamaRuntimeException.error("The sqrt operator cannot accept negative inputs", scope),
				true);
		return Math.sqrt(v);
	}

	/**
	 * Sqrt.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "sqrt",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the square root of the float operand.",
			returns = "a non-negative {@code float}.",
			special_cases = {
					"sqrt(0.0) = 0.0",
					"If the operand is negative, a runtime exception is raised."
			},
			examples = {
					@example (value = "sqrt(4.0)",  equals = "2.0"),
					@example (value = "sqrt(0.0)",  equals = "0.0")
			})
	@test ("sqrt(4.0) = 2.0")
	@test ("sqrt(0.0) = 0.0")
	@test ("sqrt(1.0) = 1.0")
	@test ("is_error(sqrt(-1.0))")
	public static Double sqrt(final IScope scope, final Double v) throws GamaRuntimeException {
		if (v < 0) GAMA.reportAndThrowIfNeeded(scope,
				GamaRuntimeException.error("The sqrt operator cannot accept negative inputs", scope),
				true);
		return Math.sqrt(v);
	}

	/**
	 * Op divide.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the division of the two operands.",
			masterDoc = true,
			usages = { @usage (
					value = "if both operands are numbers (float or int), performs a normal arithmetic division and returns a float.",
					examples = { @example (
							value = "3 / 5.0",
							equals = "0.6") }) },
			special_cases = "if the right-hand operand is equal to zero, raises a \"Division by zero\" exception",
			see = { IKeyword.PLUS, IKeyword.MINUS, IKeyword.MULTIPLY })
	@test ("0/1=0")
	@test ("is_error(1/0)")
	@test ("3/5=0.6")
	public static Double opDivide(final IScope scope, final Integer a, final Integer b) throws GamaRuntimeException {
		if (b == 0) throw GamaRuntimeException.error("Division by zero", scope);
		return a.doubleValue() / b.doubleValue();
	}

	/**
	 * Op divide.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns a float, equal to the division of the left-hand operand by the right-hand operand.",
			see = "*")
	@test ("0.2/2=0.1")
	@test ("is_error(1.5/0)")
	@test ("0.0/5=0.0")
	public static Double opDivide(final IScope scope, final Double a, final Integer b) throws GamaRuntimeException {
		if (b == 0) throw GamaRuntimeException.error("Division by zero", scope);
		return a / b.doubleValue();
	}

	/**
	 * Op divide.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns a float, equal to the division of the left-hand operand by the right-hand operand.",
			see = "*")
	@test ("0.2/0.5=0.4")
	@test ("is_error(1.5/0.0)")
	@test ("0.0/1.0=0.0")
	public static Double opDivide(final IScope scope, final Double a, final Double b) throws GamaRuntimeException {
		if (b.equals(0.0)) throw GamaRuntimeException.error("Division by zero", scope);
		return a / b;
	}

	/**
	 * Op divide.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns a float, equal to the division of the left-hand operand by the right-hand operand.",
			see = "*")
	@test ("1/0.5=2.0")
	@test ("is_error(2/0.0)")
	@test ("0/0.3=0.0")
	public static Double opDivide(final IScope scope, final Integer a, final Double b) throws GamaRuntimeException {
		if (b.equals(0.0)) throw GamaRuntimeException.error("Division by zero", scope);
		return a.doubleValue() / b.doubleValue();
	}

	/**
	 * Op times.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the integer
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the product of the two operands.",
			masterDoc = true,
			usages = @usage (
					value = "if both operands are numbers (float or int), performs a normal arithmetic product and returns a float if one of them is a float.",
					examples = @example (
							value = "1 * 1",
							equals = "1")),
			see = { IKeyword.PLUS, IKeyword.MINUS, IKeyword.DIVIDE })
	public static Integer opTimes(final Integer a, final Integer b) {
		return a * b;
	}

	/**
	 * Op times.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the product of the two operands",
			examples = { @example (
					value = "2.5 * 2",
					equals = "5.0") },
			see = "/")
	public static Double opTimes(final Double a, final Integer b) {
		return a * b;
	}

	/**
	 * Op times.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the product of the two operands",
			examples = {},
			see = "/")
	@test ("2.0 * 2.0 = 4.0")
	@test ("1.5 * (- 1.0) = -1.5")
	@test ("1.5 * 0.0 = 0.0")

	public static Double opTimes(final Double a, final Double b) {
		return a * b;
	}

	/**
	 * Op times.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			concept = {})
	@doc (
			value = "Returns the product of the two operands",
			examples = {},
			see = "/")
	@test ("2 * 2.0 = 4.0")
	@test ("1 * (- 1.0) = -1.0")
	@test ("1 * 0.0 = 0.0")
	public static Double opTimes(final Integer a, final Double b) {
		return a * b;
	}

	/**
	 * Op times.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i matrix
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			usages = { @usage (
					value = "if one operand is a matrix and the other a number (float or int), performs a normal arithmetic product of the number with each element of the matrix (results are float if the number is a float.",
					examples = { @example (
							value = "2 * matrix([[2,5],[3,4]])",
							equals = "matrix([[4,10],[6,8]])") }) })
	public static IMatrix opTimes(final Integer a, final IMatrix b) {
		return b.times(a);
	}

	/**
	 * Op times.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i field
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			usages = { @usage (
					value = "if one operand is a matrix and the other a number (float or int), performs a normal arithmetic product of the number with each element of the matrix (results are float if the number is a float.",
					examples = { @example (
							value = "2 * matrix([[2,5],[3,4]])",
							equals = "matrix([[4,10],[6,8]])") }) })
	public static IField opTimes(final Integer a, final IField b) {
		return b.times(a);
	}

	/**
	 * Op times.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i matrix
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATRIX },
			doc = @doc ("Multiply all the elements in the matrix operand by the first operand"))
	@test ("2.0 * matrix([[2,5],[3,4]]) =  matrix([[4.0,10.0],[6.0,8.0]])")
	public static IMatrix opTimes(final Double a, final IMatrix b) {
		return b.times(a);
	}

	/**
	 * Op plus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the integer
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the sum, union or concatenation of the two operands.",
			masterDoc = true,
			usages = { @usage (
					value = "if both operands are numbers (float or int), performs a normal arithmetic sum and returns a float if one of them is a float.",
					examples = { @example (
							value = "1 + 1",
							equals = "2") }) },
			see = { IKeyword.MINUS, IKeyword.MULTIPLY, IKeyword.DIVIDE })
	public static Integer opPlus(final Integer a, final Integer b) {
		return a + b;
	}

	/**
	 * Op plus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the sum, union or concatenation of the two operands.",
			examples = { @example (
					value = "1.0 + 1",
					equals = "2.0"),
					@example (
							value = "1.0 + 2.5",
							equals = "3.5") })
	public static Double opPlus(final Double a, final Integer b) {
		return a + b;
	}

	/**
	 * Op plus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the sum, union or concatenation of the two operands.")
	@test ("1.0 + (- 1.0) = 0.0")
	@test ("1.0 + 1.0 = 2.0")
	public static Double opPlus(final Double a, final Double b) {
		return a + b;
	}

	/**
	 * Op plus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the sum, union or concatenation of the two operands.")
	@test ("1 + (- 1.0) = 0.0")
	@test ("1 + 1.0 = 2.0")
	public static Double opPlus(final Integer a, final Double b) {
		return a + b;
	}

	/**
	 * Op plus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i matrix
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			usages = { @usage (
					value = "if one operand is a matrix and the other a number (float or int), performs a normal arithmetic sum of the number with each element of the matrix (results are float if the number is a float.",
					examples = { @example (
							value = "3.5 + matrix([[2,5],[3,4]])",
							equals = "matrix([[5.5,8.5],[6.5,7.5]])") }) })
	// TODO check update
	public static IMatrix opPlus(final Integer a, final IMatrix b) {
		return b.plus(a);
	}

	/**
	 * Op plus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i matrix
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "Returns the sum of the two operands",
			examples = {},
			see = "/")
	@test ("1.0 + matrix([[5.5,8.5],[6.5,7.5]]) = matrix([[6.5,9.5],[7.5,8.5]])")
	public static IMatrix opPlus(final Double a, final IMatrix b) {
		return b.plus(a);
	}

	/**
	 * Op minus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the integer
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the difference of the two operands.",
			masterDoc = true,
			usages = { @usage (
					value = "if both operands are numbers, performs a normal arithmetic difference and returns a float if one of them is a float.",
					examples = { @example (
							value = "1 - 1",
							equals = "0") }) },
			see = { IKeyword.PLUS, IKeyword.MULTIPLY, IKeyword.DIVIDE })
	public static Integer opMinus(final Integer a, final Integer b) {
		return a - b;
	}

	/**
	 * Op minus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the difference of the two operands",
			examples = { @example (
					value = "1.0 - 1",
					equals = "0.0"),
					@example (
							value = "3.7 - 1",
							equals = "2.7"),
					@example (
							value = "3.0 - 1",
							equals = "2.0") })
	public static Double opMinus(final Double a, final Integer b) {
		return a - b;
	}

	/**
	 * Op minus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the difference of the two operands",
			examples = { @example (
					value = "1.0 - 1.0",
					equals = "0.0"),
					@example (
							value = "3.7 - 1.2",
							equals = "2.5"),
					@example (
							value = "3.0 - 1.2",
							equals = "1.8") })
	public static Double opMinus(final Double a, final Double b) {
		return a - b;
	}

	/**
	 * Op minus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the double
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the difference of the two operands",
			examples = { @example (
					value = "1 - 1.0",
					equals = "0.0"),
					@example (
							value = "3 - 1.2",
							equals = "1.8") })
	public static Double opMinus(final Integer a, final Double b) {
		return a - b;
	}

	/**
	 * Op minus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i matrix
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the difference of the two operands",
			usages = { @usage (
					value = "if one operand is a matrix and the other a number (float or int), performs a normal arithmetic difference of the number with each element of the matrix (results are float if the number is a float.",
					examples = { @example (
							value = "3.5 - matrix([[2,5],[3,4]])",
							equals = "matrix([[1.5,-1.5],[0.5,-0.5]])") }) })
	// TODO check update
	public static IMatrix opMinus(final Integer a, final IMatrix b) {
		return b.times(-1).plus(a);
	}

	/**
	 * Op minus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i matrix
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "the difference of the two operands",
			examples = { @example (
					value = "(10.0 - (3.0 as_matrix({2,3})))",
					equals = "matrix([[7.0,7.0,7.0],[7.0,7.0,7.0]])") })
	public static IMatrix opMinus(final Double a, final IMatrix b) {
		return b.times(-1).plus(a);
	}

	// @operator(value = "with_precision", can_be_const = true)
	// @doc(value =
	// "round off the value of left-hand operand to the precision given by the
	// value of right-hand operand",
	// examples = {
	// "12345.78943 with_precision 2 --: 12345.79", "123 with_precision 2 --:
	// 123.00" }, see =
	/**
	 * Op truncate.
	 *
	 * @param x
	 *            the x
	 * @param precision
	 *            the precision
	 * @return the double
	 */
	// "round")
	public static Double opTruncate(final Double x, final Integer precision) {
		final double x1 = x.doubleValue();
		final int precision1 = precision.intValue();
		double fract;
		double whole;
		double mult;
		if (x1 > 0) {
			whole = floor(x1);
			mult = pow(10.0, precision1);
			fract = floor((x1 - whole) * mult) / mult;
		} else {
			whole = ceil(x1);
			mult = pow(10, precision1);
			fract = ceil((x1 - whole) * mult) / mult;
		}
		return whole + fract;
	}

	/**
	 * Round.
	 *
	 * @param v
	 *            the v
	 * @param precision
	 *            the precision
	 * @return the double
	 */
	@operator (
			value = "with_precision",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Rounds off the value of left-hand operand to the precision given by the value of right-hand operand",
			examples = { @example (
					value = "12345.78943 with_precision 2",
					equals = "12345.79"),
					@example (
							value = "123 with_precision 2",
							equals = "123.00") },
			see = "round")
	public static double round(final Double v, final Integer precision) {
		return MathUtils.round(v, precision);
	}

	/**
	 * Floor.
	 *
	 * @param d
	 *            the d
	 * @return the double
	 */
	@operator (
			value = "floor",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Maps the operand to the largest previous following integer, i.e. the largest integer not greater than x.",
			returns = "an {@code int} (the floor value of the operand).",
			special_cases = {
					"For negative non-integer values, floor rounds towards negative infinity: floor(-4.7) = -5.",
					"For exact integers, floor returns the integer itself: floor(3.0) = 3."
			},
			examples = { @example (
					value = "floor(3)",
					equals = "3"),
					@example (
							value = "floor(3.5)",
							equals = "3"),
					@example (
							value = "floor(-4.7)",
							equals = "-5") },
			see = { "ceil", "round" })
	@test ("floor(3.5) = 3")
	@test ("floor(-4.7) = -5")
	@test ("floor(3.0) = 3")
	@test ("floor(-2.0) = -2")
	@test ("floor(0.0) = 0")
	public static final int floor(final double x) {
		// This method is a *lot* faster than using (int)Math.floor(x)
		int xi = (int) x;
		return x < xi ? xi - 1 : xi;
	}

	/**
	 * Ceil.
	 *
	 * @param d
	 *            the d
	 * @return the double
	 */
	@operator (
			value = { "ceil", "ceiling" },
			can_be_const = true,
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Maps the operand to the smallest following integer, i.e. the smallest integer not less than x.",
			returns = "an {@code int} (the ceiling value of the operand).",
			special_cases = {
					"For negative non-integer values, ceil rounds towards zero: ceil(-4.7) = -4.",
					"For exact integers, ceil returns the integer itself: ceil(3.0) = 3."
			},
			examples = { @example (
					value = "ceil(3)",
					equals = "3.0"),
					@example (
							value = "ceil(3.5)",
							equals = "4.0"),
					@example (
							value = "ceil(-4.7)",
							equals = "-4.0") },
			see = { "floor", "round" })
	@test ("ceil(3.5) = 4")
	@test ("ceil(-4.7) = -4")
	@test ("ceil(3.0) = 3")
	@test ("ceil(-2.0) = -2")
	@test ("ceil(0.0) = 0")
	public static final int ceil(final double d) {
		return (int) Math.ceil(d);
	}

	/**
	 * Op mod.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the integer
	 */
	@operator (
			value = "mod",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the remainder of the integer division of the left-hand operand by the right-hand operand.",
			returns = "an {@code int}. The sign of the result matches the sign of the dividend (Java semantics).",
			special_cases = {
					"For a negative dividend, the result is negative: (-7) mod 3 = -1.",
					"If the right-hand operand is equal to zero, a runtime exception is raised."
			},
			usages = { @usage (
					value = "if operands are float, they are truncated"),
					@usage (
							value = "if the right-hand operand is equal to zero, raises an exception.") },
			examples = { @example (
					value = "40 mod 3",
					equals = "1"),
					@example (value = "(-7) mod 3", equals = "-1"),
					@example (value = "7 mod 3",    equals = "1")
			},
			see = "div")
	@test ("40 mod 3 = 1")
	@test ("7 mod 3 = 1")
	@test ("6 mod 3 = 0")
	@test ("(-7) mod 3 = -1")
	@test ("is_error(5 mod 0)")
	public static Integer opMod(final IScope scope, final Integer a, final Integer b) {
		return a % b;
	}

	/**
	 * Div.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the integer
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "div",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns the truncated integer division of the left-hand operand by the right-hand operand.",
			masterDoc = true,
			returns = "an {@code int}. The result is truncated towards zero (Java semantics).",
			special_cases = {
					"For a negative dividend or divisor, truncation is towards zero: (-7) div 2 = -3.",
					"If the right-hand operand is equal to zero, a runtime exception is raised."
			},
			usages = @usage (
					value = "if the right-hand operand is equal to zero, raises an exception."),
			examples = {
					@example (value = "40 div 3",   equals = "13"),
					@example (value = "(-7) div 2", equals = "-3"),
					@example (value = "7 div 2",    equals = "3")
			},
			see = "mod")
	@test ("40 div 3 = 13")
	@test ("7 div 2 = 3")
	@test ("(-7) div 2 = -3")
	@test ("6 div 3 = 2")
	@test ("is_error(5 div 0)")
	public static Integer div(final IScope scope, final Integer a, final Integer b) throws GamaRuntimeException {
		if (b == 0) throw GamaRuntimeException.error("Division by zero", scope);
		return a / b;
	}

	/**
	 * Div.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the integer
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "div",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "an int, equal to the truncation of the division of the left-hand operand by the right-hand operand.",
			examples = @example (
					value = "40.5 div 3",
					equals = "13"),
			see = "mod")
	public static Integer div(final IScope scope, final Double a, final Integer b) throws GamaRuntimeException {
		if (b == 0) throw GamaRuntimeException.error("Division by zero", scope);
		return (int) (a / b);
	}

	/**
	 * Div.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the integer
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "div",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "an int, equal to the truncation of the division of the left-hand operand by the right-hand operand.",
			examples = @example (
					value = "40 div 4.1",
					equals = "9"))
	public static Integer div(final IScope scope, final Integer a, final Double b) throws GamaRuntimeException {
		if (b.equals(0.0)) throw GamaRuntimeException.error("Division by zero", scope);
		return (int) (a / b);
	}

	/**
	 * Div.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the integer
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "div",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = {})
	@doc (
			value = "an int, equal to the truncation of the division of the left-hand operand by the right-hand operand.",
			examples = @example (
					value = "40.1 div 4.5",
					equals = "8"))
	public static Integer div(final IScope scope, final Double a, final Double b) throws GamaRuntimeException {
		if (b.equals(0.0)) throw GamaRuntimeException.error("Division by zero", scope);
		return (int) (a / b);
	}

	/** Constant field PI. */
	public static final double PI = java.lang.Math.PI;
	/** Constant field PI_4. */
	public final static double PI_4 = PI / 4d;
	/** Constant field PRECISION. */
	public final static int PRECISION = 360;
	/** Constant field PI_2. */
	public static final double PI_2 = PI * 2;
	/** Constant field PI_2_OVER1. */
	public final static double PI_2_OVER1 = 1f / PI_2;
	/** Constant field PI_2_OVER1_P. */
	public final static double PI_2_OVER1_P = PI_2_OVER1 * PRECISION;
	/** Constant field PI_34. */
	public final static double PI_34 = PI_4 * 3d;
	/** Constant field PREC_MIN_1. */
	public final static int PREC_MIN_1 = PRECISION - 1;

	/** The Constant SQRT2. */
	public static final double SQRT2 = Math.sqrt(2);
	/** Constant field toDeg. */
	public static final double toDeg = 180d / Math.PI;
	/** Constant field toRad. */
	public static final double toRad = Math.PI / 180d;

	/**
	 * Atan 2.
	 *
	 * @param y
	 *            the y
	 * @param x
	 *            the x
	 * @return the double
	 */
	@operator (
			value = "atan2",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "the atan2 value of the two operands.",
			comment = "The function atan2 is the arctangent function with two arguments. The purpose of using two arguments instead of one is to gather information on the signs of the inputs in order to return the appropriate quadrant of the computed angle, which is not possible for the single-argument arctangent function. Beware: the first argument is y and the second is x",
			masterDoc = true,
			examples = { @example (
					value = "atan2 (0,0)",
					equals = "0.0"),
					@example (
							value = "atan2 (0,1)",
							equals = "0.0"),
					@example (
							value = "atan2 (0,-1)",
							equals = "180.0"),
					@example (
							value = "atan2 (1,0)",
							equals = "90.0"),
					@example (
							value = "atan2 (1,1)",
							equals = "45.0"),
					@example (
							value = "atan2 (1,-1)",
							equals = "135.0"),
					@example (
							value = "atan2 (-1,0)",
							equals = "-90.0"),
					@example (
							value = "atan2 (-1,1)",
							equals = "-45.0"),
					@example (
							value = "atan2 (-1,-1)",
							equals = "-135.0"), },
			see = { "atan", "acos", "asin" })
	public static double atan2(final double y, final double x) {
		return Math.atan2(y, x) * toDeg;
	}

	/**
	 * Check heading : keep it in the 0 - 360 degrees interval.
	 *
	 * @param newHeading
	 *            the new heading
	 *
	 * @return the integer
	 */
	public static double checkHeading(final int newHeading) {
		double result = newHeading;
		while (result < 0) { result += PRECISION; }
		return result % PRECISION;
	}

	/**
	 * Check heading : keep it in the 0 - 360 degrees interval.
	 *
	 * @param newHeading
	 *            the new heading
	 *
	 * @return the double
	 */
	public static double checkHeading(final double newHeading) {
		double result = newHeading;
		while (result < 0) { result += PRECISION; }
		while (result > 360) { result -= PRECISION; }
		return result;
	}

	/**
	 * Hypot.
	 *
	 * @param scope
	 *            the scope
	 * @param x1
	 *            the x 1
	 * @param x2
	 *            the x 2
	 * @param y1
	 *            the y 1
	 * @param y2
	 *            the y 2
	 * @return the double
	 */
	@operator (
			value = "hypot",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns sqrt(x2 +y2) without intermediate overflow or underflow.",
			special_cases = "If either argument is infinite, then the result is positive infinity. If either argument is NaN and neither argument is infinite, then the result is NaN.",
			examples = @example (
					value = "hypot(0,1,0,1)",
					equals = "sqrt(2)"))
	public static double hypot(final IScope scope, final double x1, final double x2, final double y1, final double y2) {
		// return Math.hypot(x2 - x1, y2 - y1); VERY SLOW !
		final double dx = x2 - x1;
		final double dy = y2 - y1;
		return sqrt(scope, dx * dx + dy * dy);
	}

	/**
	 * Checks if is number.
	 *
	 * @param d
	 *            the d
	 * @return the boolean
	 */
	@operator (
			value = "is_number",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC, IConcept.TYPE })
	@doc (
			value = "Returns whether the argument is a real number or not",
			examples = { @example (
					value = "is_number(4.66)",
					equals = "true"),
					@example (
							value = "is_number(#infinity)",
							equals = "true"),
					@example (
							value = "is_number(#nan)",
							equals = "false") })
	public static Boolean is_number(final Double d) {
		return !Double.isNaN(d);
	}

	/**
	 * Checks if is finite.
	 *
	 * @param d
	 *            the d
	 * @return the boolean
	 */
	@operator (
			value = "is_finite",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns whether the argument is a finite number or not",
			examples = { @example (
					value = "is_finite(4.66)",
					equals = "true"),
					@example (
							value = "is_finite(#infinity)",
							equals = "false") })
	public static Boolean is_finite(final Double d) {
		return !Double.isInfinite(d);
	}

	/**
	 * Signum.
	 *
	 * @param d
	 *            the d
	 * @return the integer
	 */
	@operator (
			value = "signum",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns -1 if the argument is negative, +1 if it is positive, 0 if it is equal to zero or not a number",
			examples = { @example (
					value = "signum(-12.8)",
					equals = "-1"),
					@example (
							value = "signum(14.5)",
							equals = "1"),
					@example (
							value = "signum(0.0)",
							equals = "0") })
	public static Integer signum(final Double d) {
		if (d == null || d.isNaN() || Comparison.equal(d, 0d)) return 0;
		if (d < 0) return -1;
		return 1;
	}

	/**
	 * Signum.
	 *
	 * @param d
	 *            the d
	 * @return the integer
	 */
	@operator (
			value = "signum",
			can_be_const = true,
			category = { IOperatorCategory.ARITHMETIC },
			concept = { IConcept.MATH, IConcept.ARITHMETIC })
	@doc (
			value = "Returns -1 if the argument is negative, +1 if it is positive, 0 if it is equal to zero or not a number",
			examples = { @example (
					value = "signum(-12)",
					equals = "-1"),
					@example (
							value = "signum(14)",
							equals = "1"),
					@example (
							value = "signum(0)",
							equals = "0") })
	public static Integer signum(final Integer d) {
		int a = d.intValue();
		return a < 0 ? -1 : a == 0 ? 0 : 1;
	}

}
