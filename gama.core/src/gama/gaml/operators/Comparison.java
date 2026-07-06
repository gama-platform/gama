/*******************************************************************************************************
 *
 * Comparison.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import static gama.annotations.constants.IKeyword.EQUALS;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.validation.IOperatorValidator;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.types.geometry.IPoint;
import gama.api.utils.MathUtils;

/**
 * Provides all comparison operators for the GAML language.
 *
 * <p>This class implements the standard binary comparison operators for numeric, string,
 * point, and generic object operands:</p>
 * <ul>
 *   <li><b>between</b>  – tests strict membership in an open interval {@code (inf, sup)}</li>
 *   <li><b>&gt;</b>     – strictly greater than</li>
 *   <li><b>&lt;</b>     – strictly less than</li>
 *   <li><b>&gt;=</b>    – greater than or equal to</li>
 *   <li><b>&lt;=</b>    – less than or equal to</li>
 *   <li><b>=</b>        – equality (uses JTS {@code IntervalSize.isZeroWidth} for doubles)</li>
 *   <li><b>!=</b>       – inequality</li>
 * </ul>
 *
 * <p><b>Nil handling:</b> for all numeric and point comparisons, a {@code nil} operand causes
 * the operator to return {@code false} immediately.</p>
 *
 * <p><b>Floating-point equality:</b> the {@code =} operator for {@code float} values uses
 * JTS's {@link org.locationtech.jts.index.quadtree.IntervalSize#isZeroWidth} to determine
 * equality within a tolerance based on the magnitude of the operands. Two {@code NaN} values
 * are considered equal by this implementation.</p>
 *
 * <p><b>String comparisons:</b> all string comparison operators use Java's natural
 * lexicographic order (case-sensitive, Unicode code-point order).</p>
 *
 * <p><b>Point comparisons:</b> point comparison uses a <em>partial order</em> where
 * {@code p1 &lt; p2} iff {@code p1.x &lt; p2.x} AND {@code p1.y &lt; p2.y}.</p>
 *
 * @author Alexis Drogoul
 * @see Logic
 */
public class Comparison {

	/**
	 * Validates the operand types of an equality expression and emits a warning when the
	 * two types can never be equal (e.g. comparing an {@code int} with a {@code geometry}).
	 *
	 * <p>The validator does not prevent compilation; it only issues a
	 * {@link IGamlIssue#UNMATCHED_OPERANDS} warning so that the user is informed that
	 * the expression will always evaluate to {@code false}.</p>
	 */
	public static class EqualValidator implements IOperatorValidator {

		@Override
		public boolean validate(final IDescription context, final EObject emfContext, final IExpression... arguments) {
			if (arguments.length > 1) {
				final IType<?> t1 = arguments[0].getGamlType();
				final IType<?> t2 = arguments[1].getGamlType();
				if (t1.id() == IType.NONE || t2.id() == IType.NONE || t1.isTranslatableInto(t2)
						|| t2.isTranslatableInto(t1))
					return true;
				context.warning(
						"This equality will always return false because you are comparing a " + t1 + " with a " + t2,
						IGamlIssue.UNMATCHED_OPERANDS, emfContext);
			}
			return true;
		}

	}

	/** The Constant GT. */
	public final static String GT = ">";

	/** The Constant LT. */
	public final static String LT = "<";

	/** The Constant GTE. */
	public final static String GTE = ">=";

	/** The Constant LTE. */
	public final static String LTE = "<=";

	
	/**
	 * Between.
	 *
	 * @param a   the value to test
	 * @param inf the lower bound (exclusive)
	 * @param sup the upper bound (exclusive)
	 * @return the boolean
	 */
	@operator (
			value = "between",
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "Returns {@code true} if the first operand is strictly greater than the second operand "
					+ "and strictly less than the third operand. Both bounds are <b>exclusive</b>.",
			masterDoc = true,
			returns = "a {@code bool}: {@code true} iff {@code inf < a < sup}.",
			special_cases = {
					"If {@code inf > sup} (inverted bounds), the operator always returns {@code false}.",
					"If {@code a} equals {@code inf} or {@code sup} exactly, returns {@code false} (open interval)."
			},
			examples = {
					@example (value = "between(5, 1, 10)",  equals = "true"),
					@example (value = "between(1, 1, 10)",  equals = "false",  isTestOnly = false),
					@example (value = "between(10, 1, 10)", equals = "false",  isTestOnly = false),
					@example (value = "between(0, 5, 1)",   equals = "false",  isTestOnly = false)
			},
			see = { GT, LT, GTE, LTE })
	@test ("0 between(-2,4) = true")
	@test ("-12 between(-22,-10)")
	@test ("not(1 between(1,4))")
	@test ("not(2 between(4,1))")
	@test ("not(5 between(5,10))")
	@test ("not(10 between(1,10))")
	@test ("not(0 between(0,0))")
	@test ("between(5, 1, 10)")
	@test ("!between(1, 1, 10)")
	@test ("!between(0, 1, 10)")
	public static Boolean between(final Integer a, final Integer inf, final Integer sup) {
		if (inf > sup) return false;
		return a >= sup ? false : a > inf;
	}

	/**
	 * Between.
	 *
	 * @param a   the value to test
	 * @param inf the lower bound (exclusive)
	 * @param sup the upper bound (exclusive)
	 * @return the boolean
	 */
	@operator (
			value = "between",
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the first float operand is strictly greater than the second float operand "
					+ "and strictly less than the third float operand. Both bounds are <b>exclusive</b>.",
			returns = "a {@code bool}: {@code true} iff {@code inf < a < sup}.",
			special_cases = {
					"If {@code inf > sup} (inverted bounds), the operator always returns {@code false}.",
					"If {@code a} equals {@code inf} or {@code sup} exactly, returns {@code false} (open interval)."
			},
			examples = {
					@example (value = "between(5.0, 1.0, 10.0)",    equals = "true"),
					@example (value = "between(1.0, 1.0, 10.0)",    equals = "false", isTestOnly = false),
					@example (value = "between(10.0, 1.0, 10.0)",   equals = "false", isTestOnly = false)
			})
	@test ("0.0 between(-2,4)")
	@test ("-12.5 between(-22.0,-10.0)")
	@test ("not(1.0 between(1.0,4.0))")
	@test ("not(2.2 between(4.0,1.9))")
	@test ("not(5.0 between(5.0,10.0))")
	@test ("not(10.0 between(1.0,10.0))")
	public static Boolean between(final Double a, final Double inf, final Double sup) {
		if (inf > sup) return false;
		return a >= sup ? false : a > inf;
	}

	/**
	 * Greater.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "Returns {@code true} if the left-hand operand is strictly greater than the right-hand operand, {@code false} otherwise.",
			masterDoc = true,
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}.",
					"Comparing equal values returns {@code false} (strict inequality)." },
			examples = {
					@example (value = "13.0 > 7.0", equals = "true"),
					@example (value = "7 > 7",      equals = "false"),
					@example (value = "6 > 7",      equals = "false")
			},
			see = { LT, GTE, LTE, EQUALS, "!=" })
	@test ("bool val <- (3 > 17); val = false ")
	@test ("val <- (13 > 7); val = true")
	@test ("!(7 > 7)")
	@test ("!(3 > 3)")
	@test ("2 > 1")
	@test ("!(1 > 1)")
	@test ("!(0 > 1)")
	public static Boolean greater(final Integer a, final Integer b) {
		if (a == null || b == null) return false;
		return a > b;
	}

	/**
	 * Greater.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand integer operand is strictly greater than the right-hand float operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3 > 2.5", equals = "true"),
					@example (value = "3 > 3.0", equals = "false")
			})
	@test ("3 > 2.5")
	@test ("!(3 > 3.0)")
	@test ("!(2 > 3.5)")
	public static Boolean greater(final Integer a, final Double b) {
		if (a == null || b == null) return false;
		return a > b;
	}

	/**
	 * Greater.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand float operand is strictly greater than the right-hand integer operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3.5 > 7",   equals = "false"),
					@example (value = "7.5 > 7",   equals = "true"),
					@example (value = "7.0 > 7",   equals = "false")
			})
	@test ("!(3.5 > 7)")
	@test ("7.5 > 7")
	@test ("!(7.0 > 7)")
	public static Boolean greater(final Double a, final Integer b) {
		if (a == null || b == null) return false;
		return a > b;
	}

	/**
	 * Greater.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand float operand is strictly greater than the right-hand float operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3.5 > 7.6",   equals = "false"),
					@example (value = "7.6 > 3.5",   equals = "true"),
					@example (value = "3.5 > 3.5",   equals = "false")
			})
	@test ("!(3.5 > 7.6)")
	@test ("7.6 > 3.5")
	@test ("!(3.5 > 3.5)")
	@test ("1.1 > 1.0")
	public static Boolean greater(final Double a, final Double b) {
		if (a == null || b == null) return false;
		return a > b;
	}

	/**
	 * Less.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "Returns {@code true} if the left-hand operand is strictly less than the right-hand operand, {@code false} otherwise.",
			masterDoc = true,
			returns = "a {@code bool}.",
			special_cases = {
					"If one of the operands is {@code nil}, returns {@code false}.",
					"Comparing equal values returns {@code false} (strict inequality)."
			},
			examples = {
					@example (value = "3 < 7", equals = "true"),
					@example (value = "7 < 7", equals = "false"),
					@example (value = "8 < 7", equals = "false")
			},
			see = { GT, GTE, LTE, EQUALS, "!=" })
	@test ("3 < 7")
	@test ("!(7 < 7)")
	@test ("!(8 < 7)")
	@test ("1 < 2")
	public static Boolean less(final Integer a, final Integer b) {
		if (a == null || b == null) return false;
		return a < b;
	}

	/**
	 * Less.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand integer operand is strictly less than the right-hand float operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3 < 2.5",  equals = "false"),
					@example (value = "2 < 2.5",  equals = "true"),
					@example (value = "3 < 3.0",  equals = "false")
			})
	@test ("!(3 < 2.5)")
	@test ("2 < 2.5")
	@test ("!(3 < 3.0)")
	public static Boolean less(final Integer a, final Double b) {
		if (a == null || b == null) return false;
		return a < b;
	}

	/**
	 * Less.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand float operand is strictly less than the right-hand integer operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3.5 < 7",   equals = "true"),
					@example (value = "7.0 < 7",   equals = "false"),
					@example (value = "8.5 < 7",   equals = "false")
			})
	@test ("3.5 < 7")
	@test ("!(7.0 < 7)")
	@test ("!(8.5 < 7)")
	public static Boolean less(final Double a, final Integer b) {
		if (a == null || b == null) return false;
		return a < b;
	}

	/**
	 * Less.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand float operand is strictly less than the right-hand float operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3.5 < 7.6",   equals = "true"),
					@example (value = "7.6 < 3.5",   equals = "false"),
					@example (value = "3.5 < 3.5",   equals = "false")
			})
	@test ("3.5 < 7.6")
	@test ("!(7.6 < 3.5)")
	@test ("!(3.5 < 3.5)")
	public static Boolean less(final Double a, final Double b) {
		if (a == null || b == null) return false;
		return a < b;
	}

	/**
	 * Greater or equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "Returns {@code true} if the left-hand operand is greater than or equal to the right-hand operand, {@code false} otherwise.",
			masterDoc = true,
			returns = "a {@code bool}.",
			special_cases = {
					"If one of the operands is {@code nil}, returns {@code false}.",
					"Returns {@code true} when both operands are equal."
			},
			examples = {
					@example (value = "3 >= 7",  equals = "false"),
					@example (value = "7 >= 7",  equals = "true"),
					@example (value = "8 >= 7",  equals = "true")
			},
			see = { GT, LT, LTE, EQUALS, "!=" })
	@test ("!(3 >= 7)")
	@test ("7 >= 7")
	@test ("8 >= 7")
	public static Boolean greaterOrEqual(final Integer a, final Integer b) {
		if (a == null || b == null) return false;
		return a >= b;
	}

	/**
	 * Greater or equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand integer operand is greater than or equal to the right-hand float operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3 >= 2.5",   equals = "true"),
					@example (value = "3 >= 3.0",   equals = "true"),
					@example (value = "2 >= 3.0",   equals = "false")
			})
	@test ("3 >= 2.5")
	@test ("3 >= 3.0")
	@test ("!(2 >= 3.0)")
	public static Boolean greaterOrEqual(final Integer a, final Double b) {
		if (a == null || b == null) return false;
		return a >= b;
	}

	/**
	 * Greater or equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand float operand is greater than or equal to the right-hand integer operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3.5 >= 7",   equals = "false"),
					@example (value = "7.0 >= 7",   equals = "true"),
					@example (value = "8.5 >= 7",   equals = "true")
			})
	@test ("!(3.5 >= 7)")
	@test ("7.0 >= 7")
	@test ("8.5 >= 7")
	public static Boolean greaterOrEqual(final Double a, final Integer b) {
		if (a == null || b == null) return false;
		return a >= b;
	}

	/**
	 * Greater or equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand float operand is greater than or equal to the right-hand float operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3.5 >= 3.5",   equals = "true"),
					@example (value = "3.5 >= 7.6",   equals = "false"),
					@example (value = "7.6 >= 3.5",   equals = "true")
			})
	@test ("3.5 >= 3.5")
	@test ("!(3.5 >= 7.6)")
	@test ("7.6 >= 3.5")
	public static Boolean greaterOrEqual(final Double a, final Double b) {
		if (a == null || b == null) return false;
		return a >= b;
	}

	/**
	 * Op less than or equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "Returns {@code true} if the left-hand operand is less than or equal to the right-hand operand, {@code false} otherwise.",
			masterDoc = true,
			returns = "a {@code bool}.",
			special_cases = {
					"If one of the operands is {@code nil}, returns {@code false}.",
					"Returns {@code true} when both operands are equal."
			},
			examples = {
					@example (value = "3 <= 7",  equals = "true"),
					@example (value = "7 <= 7",  equals = "true"),
					@example (value = "8 <= 7",  equals = "false")
			},
			see = { GT, LT, GTE, EQUALS, "!=" })
	@test ("3 <= 7")
	@test ("7 <= 7")
	@test ("!(8 <= 7)")
	public static Boolean opLessThanOrEqual(final Integer a, final Integer b) {
		if (a == null || b == null) return false;
		return a <= b;
	}

	/**
	 * Less or equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand integer operand is less than or equal to the right-hand float operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3 <= 2.5",   equals = "false"),
					@example (value = "3 <= 3.0",   equals = "true"),
					@example (value = "2 <= 3.0",   equals = "true")
			},
			see = { GT, LT, GTE, EQUALS, "!=" })
	@test ("!(3 <= 2.5)")
	@test ("3 <= 3.0")
	@test ("2 <= 3.0")
	public static Boolean lessOrEqual(final Integer a, final Double b) {
		if (a == null || b == null) return false;
		return a <= b;
	}

	/**
	 * Less or equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand float operand is less than or equal to the right-hand integer operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "7.0 <= 7",   equals = "true"),
					@example (value = "6.5 <= 7",   equals = "true"),
					@example (value = "8.5 <= 7",   equals = "false")
			})
	@test ("7.0 <= 7")
	@test ("6.5 <= 7")
	@test ("!(8.5 <= 7)")
	public static Boolean lessOrEqual(final Double a, final Integer b) {
		if (a == null || b == null) return false;
		return a <= b;
	}

	/**
	 * Less or equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand float operand is less than or equal to the right-hand float operand, {@code false} otherwise.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, returns {@code false}." },
			examples = {
					@example (value = "3.5 <= 3.5",   equals = "true"),
					@example (value = "3.5 <= 7.6",   equals = "true"),
					@example (value = "7.6 <= 3.5",   equals = "false")
			})
	@test ("3.5 <= 3.5")
	@test ("3.5 <= 7.6")
	@test ("!(7.6 <= 3.5)")
	public static Boolean lessOrEqual(final Double a, final Double b) {
		if (a == null || b == null) return false;
		return a <= b;
	}

	/**
	 * Equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { EQUALS },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "Returns {@code true} if both float operands are equal, {@code false} otherwise.",
			masterDoc = true,
			returns = "a {@code bool}.",
			comment = "Float equality is determined using JTS's {@code IntervalSize.isZeroWidth}, "
					+ "which applies a magnitude-relative epsilon rather than strict bit equality. "
					+ "This means that two very close floating-point values may compare as equal.",
		special_cases = {
				"Two {@code NaN} values are considered equal by this implementation.",
				"Two floats that differ by less than the machine epsilon relative to their magnitude may be considered equal.",
				"If both operands are {@code nil}, returns {@code true}.",
				"If one operand is {@code nil} and the other is not, returns {@code false}."
		},
			examples = {
					@example (value = "4.5 = 4.7",   equals = "false"),
					@example (value = "4.5 = 4.5",   equals = "true"),
					@example (value = "0.0 = 0.0",   equals = "true")
			},
			see = { GT, LT, GTE, LTE, "!=" })
	@test ("4.5 = 4.5")
	@test ("!(4.5 = 4.7)")
	@test ("0.0 = 0.0")
	@test ("1.0 = 1.0")
	public static Boolean equal(final Double a, final Double b) {
		if (a == b) return true;
		if (a == null || b == null) return false;
		if (Double.isNaN(a) && Double.isNaN(b)) {
	        return true;
	    } 
		return MathUtils.isZeroWidth(a, b);
	}
	
	
	/**
	 * Equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { EQUALS },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if both integer operands are equal, {@code false} otherwise.",
			masterDoc = true,
			returns = "a {@code bool}.",
			special_cases = {
					"If both operands are {@code nil}, returns {@code true}.",
					"If one operand is {@code nil} and the other is not, returns {@code false}."
			},
			examples = {
					@example (value = "4 = 5",   equals = "false"),
					@example (value = "4 = 4",   equals = "true"),
					@example (value = "0 = 0",   equals = "true")
			},
			see = { "!=" })
	@test ("4 = 4")
	@test ("!(4 = 5)")
	@test ("0 = 0")
	public static Boolean equal(final Integer a, final Integer b) {
		return a == null ? b == null : a.intValue() == b.intValue();
		// return !(a < b) && !(a > b);
	}

	/**
	 * Equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { EQUALS },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the integer operand equals the float operand, {@code false} otherwise. "
					+ "The integer is widened to {@code double} before comparison.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, uses the same nil-equality semantics as the double overload." },
			examples = {
					@example (value = "3 = 3.0",   equals = "true"),
					@example (value = "4 = 4.7",   equals = "false"),
					@example (value = "0 = 0.0",   equals = "true")
			},
			see = { "!=" })
	@test ("3 = 3.0")
	@test ("!(4 = 4.7)")
	@test ("0 = 0.0")
	public static Boolean equal(final Integer a, final Double b) {
		return a == null ? b == null : Comparison.equal(a.doubleValue(), b);
		// return !(a < b) && !(a > b);
	}

	/**
	 * Equal.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { EQUALS },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the float operand equals the integer operand, {@code false} otherwise. "
					+ "The integer is widened to {@code double} before comparison.",
			returns = "a {@code bool}.",
			special_cases = { "If one of the operands is {@code nil}, uses the same nil-equality semantics as the double overload." },
			examples = {
					@example (value = "4.7 = 4",   equals = "false"),
					@example (value = "4.0 = 4",   equals = "true"),
					@example (value = "0.0 = 0",   equals = "true")
			},
			see = { "!=" })
	@test ("!(4.7 = 4)")
	@test ("4.0 = 4")
	@test ("0.0 = 0")
	public static Boolean equal(final Double a, final Integer b) {
		return a == null ? b == null : Comparison.equal(a, b.doubleValue());
		// return !(a < b) && !(a > b);
	}

	/**
	 * Different.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "Returns {@code true} if both float operands are different, {@code false} otherwise.",
			masterDoc = true,
			returns = "a {@code bool}.",
			special_cases = {
					"If both operands are {@code nil}, returns {@code false} (they are equal).",
					"If one operand is {@code nil} and the other is not, returns {@code true}.",
					"Two {@code NaN} values are considered equal and therefore {@code !=} returns {@code false} for them."
			},
			examples = {
					@example (value = "3.0 != 3.0",   equals = "false"),
					@example (value = "4.0 != 4.7",   equals = "true"),
					@example (value = "0.0 != 0.0",   equals = "false")
			},
			see = { EQUALS, GT, LT, GTE, LTE })
	@test ("!(3.0 != 3.0)")
	@test ("4.0 != 4.7")
	@test ("!(0.0 != 0.0)")
	public static Boolean different(final Double a, final Double b) {
		if (a == null) return b != null;
		if (b == null) return false;
		return !Comparison.equal(a, b);
		// return a < b || a > b;
	}

	/**
	 * Different.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "Returns {@code true} if both integer operands are different, {@code false} otherwise.",
			masterDoc = true,
			returns = "a {@code bool}.",
			special_cases = {
					"If both operands are {@code nil}, returns {@code false} (they are equal).",
					"If one operand is {@code nil} and the other is not, returns {@code true}."
			},
			examples = {
					@example (value = "3 != 3",   equals = "false"),
					@example (value = "4 != 5",   equals = "true"),
					@example (value = "0 != 0",   equals = "false")
			},
			see = { EQUALS, GT, LT, GTE, LTE })
	@test ("!(3 != 3)")
	@test ("4 != 5")
	@test ("!(0 != 0)")
	public static Boolean different(final Integer a, final Integer b) {
		if (a == null) return b != null;
		if (b == null) return false;
		return a.intValue() != b.intValue();
	}

	/**
	 * Different.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the integer operand and the float operand are different, {@code false} otherwise.",
			returns = "a {@code bool}.",
			examples = {
					@example (value = "3 != 3.0",   equals = "false"),
					@example (value = "4 != 4.7",   equals = "true"),
					@example (value = "0 != 0.0",   equals = "false")
			},
			see = { EQUALS })
	@test ("!(3 != 3.0)")
	@test ("4 != 4.7")
	@test ("!(0 != 0.0)")
	public static Boolean different(final Integer a, final Double b) {
		return a == null ? b == null : !Comparison.equal(a.doubleValue(), b);
		// return !(a < b) && !(a > b);
	}

	/**
	 * Different.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code true} if the float operand and the integer operand are different, {@code false} otherwise.",
			returns = "a {@code bool}.",
			examples = {
					@example (value = "3.0 != 3",   equals = "false"),
					@example (value = "4.7 != 4",   equals = "true"),
					@example (value = "0.0 != 0",   equals = "false")
			},
			see = { EQUALS })
	@test ("!(3.0 != 3)")
	@test ("4.7 != 4")
	@test ("!(0.0 != 0)")
	public static Boolean different(final Double a, final Integer b) {
		return a == null ? b == null : !Comparison.equal(a, b.doubleValue());
	}

	/**
	 * Less or equal (String).
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns {@code true} if the left-hand string operand is lexicographically smaller than or equal to the right-hand string operand.",
			returns = "a {@code bool}.",
			comment = "Comparison is case-sensitive and uses Unicode code-point order.",
			special_cases = {
					"If the left operand is {@code nil}, returns {@code false}.",
					"An empty string is lexicographically smaller than any non-empty string."
			},
			usages = @usage (
					value = "if both operands are String, uses a lexicographic comparison of two strings",
					examples = {
							@example (value = "'abc' <= 'aeb'",  equals = "true"),
							@example (value = "'abc' <= 'abc'",  equals = "true"),
							@example (value = "'aeb' <= 'abc'",  equals = "false"),
							@example (value = "'' <= 'a'",       equals = "true")
					}))
	@test ("'abc' <= 'aeb'")
	@test ("'abc' <= 'abc'")
	@test ("!('aeb' <= 'abc')")
	@test ("'' <= 'a'")
	@test ("'a' <= 'a'")
	public static Boolean lessOrEqual(final String a, final String b) {
		if (a == null) return false;
		final int i = a.compareTo(b);
		return i <= 0;
	}

	/**
	 * Greater or equal (String).
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns {@code true} if the left-hand string operand is lexicographically greater than or equal to the right-hand string operand.",
			returns = "a {@code bool}.",
			comment = "Comparison is case-sensitive and uses Unicode code-point order.",
			special_cases = {
					"If the left operand is {@code nil}, returns {@code false}.",
					"An empty string is lexicographically smaller than any non-empty string, so '' >= 'a' returns false."
			},
			usages = @usage (
					value = "if both operands are string, uses a lexicographic comparison of the two strings",
					examples = {
							@example (value = "'abc' >= 'aeb'",  equals = "false"),
							@example (value = "'abc' >= 'abc'",  equals = "true"),
							@example (value = "'aeb' >= 'abc'",  equals = "true"),
							@example (value = "'' >= 'a'",       equals = "false")
					}))
	@test ("!('abc' >= 'aeb')")
	@test ("'abc' >= 'abc'")
	@test ("'aeb' >= 'abc'")
	@test ("!('' >= 'a')")
	@test ("'a' >= 'a'")
	public static Boolean greaterOrEqual(final String a, final String b) {
		if (a == null) return false;
		final int i = a.compareTo(b);
		return i >= 0;
	}

	/**
	 * Less (String).
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.STRING },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand string operand is lexicographically strictly smaller than the right-hand string operand.",
			returns = "a {@code bool}.",
			comment = "Comparison is case-sensitive and uses Unicode code-point order.",
			special_cases = {
					"If the left operand is {@code nil}, returns {@code false}.",
					"An empty string is lexicographically smaller than any non-empty string.",
					"Comparing a string with itself always returns {@code false} (strict inequality)."
			},
			usages = @usage (
					value = "if both operands are String, uses a lexicographic comparison of two strings",
					examples = {
							@example (value = "'abc' < 'aeb'",  equals = "true"),
							@example (value = "'abc' < 'abc'",  equals = "false"),
							@example (value = "'' < 'a'",       equals = "true")
					}))
	@test ("'abc' < 'aeb'")
	@test ("!('abc' < 'abc')")
	@test ("'' < 'a'")
	@test ("!('aeb' < 'abc')")
	public static Boolean less(final String a, final String b) {
		if (a == null) return false;
		final int i = a.compareTo(b);
		return i < 0;
	}

	/**
	 * Greater (String).
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.STRING },
			concept = {})
	@doc (
			value = "Returns {@code true} if the left-hand string operand is lexicographically strictly greater than the right-hand string operand.",
			returns = "a {@code bool}.",
			comment = "Comparison is case-sensitive and uses Unicode code-point order.",
			special_cases = {
					"If the left operand is {@code nil}, returns {@code false}.",
					"Comparing a string with itself always returns {@code false} (strict inequality).",
					"Upper-case letters precede lower-case letters in Unicode order: 'Z' > 'a' is false."
			},
			usages = @usage (
					value = "if both operands are String, uses a lexicographic comparison of two strings",
					examples = {
							@example (value = "'abc' > 'aeb'",  equals = "false"),
							@example (value = "'aeb' > 'abc'",  equals = "true"),
							@example (value = "'abc' > 'abc'",  equals = "false")
					}))
	@test ("!('abc' > 'aeb')")
	@test ("'aeb' > 'abc'")
	@test ("!('abc' > 'abc')")
	@test ("!('' > 'a')")
	@test ("'b' > 'a'")
	public static Boolean greater(final String a, final String b) {
		if (a == null) return false;
		final int i = a.compareTo(b);
		return i > 0;
	}

	/**
	 * Equal (Object).
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { EQUALS },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@validator (EqualValidator.class)
	@doc (
			value = "Returns {@code true} if both operands are equal using Java's {@code equals} method, "
					+ "or if both are {@code nil}.",
			returns = "a {@code bool}.",
			special_cases = {
					"If both operands are {@code nil}, returns {@code true}.",
					"If one operand is {@code nil} and the other is not, returns {@code false}.",
					"For agent operands, two references to the same agent instance are equal; "
							+ "dead agents are not equal to any living agent."
			},
			usages = @usage (
					value = "if both operands are any kind of objects, returns true if they are identical (i.e., the same object) or equal (comparisons between nil values are permitted)",
					examples = {
							@example (value = "[2,3] = [2,3]",   equals = "true"),
							@example (value = "[2,3] = [2,4]",   equals = "false"),
							@example (value = "'hello' = 'hello'", equals = "true")
					}))
	@test ("[2,3] = [2,3]")
	@test ("!([2,3] = [2,4])")
	@test ("'hello' = 'hello'")
	@test ("!('hello' = 'world')")
	@test ("!('abc' = 'ABC')")
	@test ("'' = ''")
	public static Boolean equal(final Object a, final Object b) {
		return a == null ? b == null : a.equals(b);
	}

	/**
	 * Different (Object).
	 *
	 * @param a the a
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "Returns {@code false} if the two operands are identical (the same object) or equal. "
					+ "Comparisons between {@code nil} values are permitted.",
			returns = "a {@code bool}.",
			special_cases = {
					"If both operands are {@code nil}, returns {@code false} (they are equal).",
					"If one operand is {@code nil} and the other is not, returns {@code true}."
			},
			examples = {
					@example (value = "[2,3] != [2,3]",   equals = "false"),
					@example (value = "[2,4] != [2,3]",   equals = "true"),
					@example (value = "'hi' != 'hello'",  equals = "true")
			})
	@test ("!([2,3] != [2,3])")
	@test ("[2,4] != [2,3]")
	@test ("'hi' != 'hello'")
	@test ("!('abc' != 'abc')")
	public static Boolean different(final Object a, final Object b) {
		return a == null ? b != null : !a.equals(b);
	}

	/**
	 * Less (Point).
	 *
	 * @param p1 the p1
	 * @param p  the p
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.POINT },
			concept = { IConcept.POINT })
	@doc (
			value = "Returns {@code true} if the left-hand point is strictly dominated by the right-hand point "
					+ "in the partial order defined by {@code p1.x < p2.x AND p1.y < p2.y}.",
			returns = "a {@code bool}.",
			comment = "Point comparison uses a <b>partial order</b>, not a total order. "
					+ "Two points can be incomparable (neither {@code p1 < p2} nor {@code p2 < p1} holds) "
					+ "when one coordinate dominates and the other does not.",
		special_cases = {
				"Point comparison uses a partial order: p1 &lt; p2 iff p1.x &lt; p2.x AND p1.y &lt; p2.y.",
				"Two points where p1.x &lt; p2.x but p1.y &gt; p2.y are incomparable (neither {@code <} nor {@code >}) so both return false.",
				"Points with equal x or equal y coordinates are never strictly less.",
				"The z component is ignored in this comparison."
		},
		usages = { @usage (
				value = "if both operands are points, returns true if and only if both x and y of the left operand are strictly less than x and y of the right operand.",
				examples = {
						@example (value = "{3,5} < {4,6}",  equals = "true"),
						@example (value = "{5,7} < {4,6}",  equals = "false"),
						@example (value = "{5,7} < {4,8}",  equals = "false"),
						@example (value = "{3,3} < {3,5}",  equals = "false")
				}) })
	@test ("{3,5} < {4,6}")
	@test ("!({5,7} < {4,6})")
	@test ("!({5,7} < {4,8})")
	@test ("!({3,3} < {3,5})")
	@test ("{1,1} < {2,2}")
	@test ("!({2,1} < {1,2})")
	public static Boolean less(final IPoint p1, final IPoint p) {
		return p1.smallerThan(p);
	}

	/**
	 * Greater (Point).
	 *
	 * @param p1 the p1
	 * @param p  the p
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.POINT },
			concept = { IConcept.POINT })
	@doc (
			value = "Returns {@code true} if the left-hand point strictly dominates the right-hand point "
					+ "in the partial order defined by {@code p1.x > p2.x AND p1.y > p2.y}.",
			returns = "a {@code bool}.",
			comment = "Point comparison uses a <b>partial order</b>, not a total order. "
					+ "Two points can be incomparable when one coordinate dominates and the other does not.",
		special_cases = {
				"Point comparison uses a partial order: p1 &gt; p2 iff p1.x &gt; p2.x AND p1.y &gt; p2.y.",
				"Two points where p1.x &gt; p2.x but p1.y &lt; p2.y are incomparable (neither {@code >} nor {@code <}) so both return false.",
				"Points with equal x or equal y coordinates are never strictly greater.",
				"The z component is ignored in this comparison."
		},
			usages = { @usage (
					value = "if both operands are points, returns true if and only if both x and y of the left operand are strictly greater than x and y of the right operand.",
					examples = {
							@example (value = "{5,7} > {4,6}",  equals = "true"),
							@example (value = "{5,7} > {4,8}",  equals = "false"),
							@example (value = "{4,6} > {4,6}",  equals = "false")
					}) })
	@test ("{5,7} > {4,6}")
	@test ("!({5,7} > {4,8})")
	@test ("!({4,6} > {4,6})")
	public static Boolean greater(final IPoint p1, final IPoint p) {
		return p1.biggerThan(p);
	}

	/**
	 * Less or equal (Point).
	 *
	 * @param p1 the p1
	 * @param p  the p
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.POINT },
			concept = { IConcept.POINT })
	@doc (
			value = "Returns {@code true} if the left-hand point is dominated by or equal to the right-hand point "
					+ "in the partial order defined by {@code p1.x <= p2.x AND p1.y <= p2.y}.",
			returns = "a {@code bool}.",
			comment = "Point comparison uses a <b>partial order</b>, not a total order.",
			special_cases = {
					"If both points are identical, returns {@code true}.",
					"The z component is ignored in this comparison."
			},
			usages = { @usage (
					value = "if both operands are points, returns true if and only if both x and y of the left operand are less than or equal to x and y of the right operand.",
					examples = {
							@example (value = "{3,5} <= {4,6}",  equals = "true"),
							@example (value = "{4,6} <= {4,6}",  equals = "true"),
							@example (value = "{5,7} <= {4,6}",  equals = "false"),
							@example (value = "{5,7} <= {4,8}",  equals = "false")
					}) })
	@test ("{3,5} <= {4,6}")
	@test ("{4,6} <= {4,6}")
	@test ("!({5,7} <= {4,6})")
	@test ("!({5,7} <= {4,8})")
	public static Boolean lessOrEqual(final IPoint p1, final IPoint p) {
		return p1.smallerThanOrEqualTo(p);
	}

	/**
	 * Greater or equal (Point).
	 *
	 * @param p1 the p1
	 * @param p  the p
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.POINT },
			concept = { IConcept.POINT })
	@doc (
			value = "Returns {@code true} if the left-hand point dominates or equals the right-hand point "
					+ "in the partial order defined by {@code p1.x >= p2.x AND p1.y >= p2.y}.",
			returns = "a {@code bool}.",
			comment = "Point comparison uses a <b>partial order</b>, not a total order.",
			special_cases = {
					"If both points are identical, returns {@code true}.",
					"The z component is ignored in this comparison."
			},
			usages = { @usage (
					value = "if both operands are points, returns true if and only if both x and y of the left operand are greater than or equal to x and y of the right operand.",
					examples = {
							@example (value = "{5,7} >= {4,6}",  equals = "true"),
							@example (value = "{4,6} >= {4,6}",  equals = "true"),
							@example (value = "{5,7} >= {4,8}",  equals = "false"),
							@example (value = "{3,5} >= {4,6}",  equals = "false")
					}) })
	@test ("{5,7} >= {4,6}")
	@test ("{4,6} >= {4,6}")
	@test ("!({5,7} >= {4,8})")
	@test ("!({3,5} >= {4,6})")
	public static Boolean greaterOrEqual(final IPoint p1, final IPoint p) {
		return p1.biggerThanOrEqualTo(p);
	}

}
