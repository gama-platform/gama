/*******************************************************************************************************
 *
 * Comparison.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import static org.locationtech.jts.index.quadtree.IntervalSize.isZeroWidth;

import org.eclipse.emf.ecore.EObject;
import org.locationtech.jts.index.quadtree.IntervalSize;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.metamodel.shape.GamaPoint;
import gama.gaml.compilation.IOperatorValidator;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 10 dec. 2010
 *
 * @todo Description
 *
 */
public class Comparison {

	/**
	 * The Class EqualValidator.
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
	 * @param a
	 *            the a
	 * @param inf
	 *            the inf
	 * @param sup
	 *            the sup
	 * @return the boolean
	 */
	@operator (
			value = "between",
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "returns true the first operand is bigger than the second operand and smaller than the third operand",
			masterDoc = true,
			examples = @example (
					value = "between(5, 1, 10)",
					equals = "true"))
	@test ("0 between(-2,4) = true")
	@test ("-12 between(-22,-10)")
	@test ("not(1 between(1,4))")
	@test ("not(2 between(4,1))")
	public static Boolean between(final Integer a, final Integer inf, final Integer sup) {
		if (inf > sup) return false;
		return a >= sup ? false : a > inf;
	}

	/**
	 * Between.
	 *
	 * @param a
	 *            the a
	 * @param inf
	 *            the inf
	 * @param sup
	 *            the sup
	 * @return the boolean
	 */
	@operator (
			value = "between",
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "returns true if the first float operand is bigger than the second float operand and smaller than the third float operand",
			examples = @example (
					value = "between(5.0, 1.0, 10.0)",
					equals = "true"))
	@test ("0.0 between(-2,4)")
	@test ("-12.5 between(-22.0,-10.0)")
	@test ("not(1.0 between(1.0,4.0))")
	@test ("not(2.2 between(4.0,1.9))")
	public static Boolean between(final Double a, final Double inf, final Double sup) {
		if (inf > sup) return false;
		return a >= sup ? false : a > inf;
	}

	/**
	 * Greater.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "true if the left-hand operand is greater than the right-hand operand, false otherwise.",
			masterDoc = true,
			usages = { @usage ("if one of the operands is nil, returns false") },
			examples = @example (
					value = "13.0 > 7.0",
					equals = "true"),
			see = { LT, GTE, LTE, "=", "!=" })
	@test (
			value = "bool val <- (3 > 17); val = false ")
	@test (
			value = "val <- (13 > 7); val = true")
	public static Boolean greater(final Integer a, final Integer b) {
		if (a == null || b == null) return false;
		return a > b;
	}

	/**
	 * Greater.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is greater than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3 > 2.5",
					equals = "true") })
	public static Boolean greater(final Integer a, final Double b) {
		if (a == null || b == null) return false;
		return a > b;
	}

	/**
	 * Greater.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is greater than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3.5 > 7",
					equals = "false") })
	public static Boolean greater(final Double a, final Integer b) {
		if (a == null || b == null) return false;
		return a > b;
	}

	/**
	 * Greater.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is greater than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3.5 > 7.6",
					equals = "false") })
	public static Boolean greater(final Double a, final Double b) {
		if (a == null || b == null) return false;
		return a > b;
	}

	/**
	 * Less.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "true if the left-hand operand is less than the right-hand operand, false otherwise.",
			masterDoc = true,
			special_cases = { "if one of the operands is nil, returns false" },
			examples = { @example (
					value = "3 < 7",
					equals = "true") },
			see = { GT, GTE, LTE, "=", "!=" })
	public static Boolean less(final Integer a, final Integer b) {
		if (a == null || b == null) return false;
		return a < b;
	}

	/**
	 * Less.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is less than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3 < 2.5",
					equals = "false") })
	public static Boolean less(final Integer a, final Double b) {
		if (a == null || b == null) return false;
		return a < b;
	}

	/**
	 * Less.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is less than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3.5 < 7",
					equals = "true") })
	public static Boolean less(final Double a, final Integer b) {
		if (a == null || b == null) return false;
		return a < b;
	}

	/**
	 * Less.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is less than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3.5 < 7.6",
					equals = "true") })
	public static Boolean less(final Double a, final Double b) {
		if (a == null || b == null) return false;
		return a < b;
	}

	/**
	 * Greater or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "true if the left-hand operand is greater or equal than the right-hand operand, false otherwise.",
			masterDoc = true,
			usages = { @usage ("if one of the operands is nil, returns false") },
			examples = { @example (
					value = "3 >= 7",
					equals = "false") },
			see = { GT, LT, LTE, "=", "!=" })
	public static Boolean greaterOrEqual(final Integer a, final Integer b) {
		if (a == null || b == null) return false;
		return a >= b;
	}

	/**
	 * Greater or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is greater or equal than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3 >= 2.5",
					equals = "true") })
	public static Boolean greaterOrEqual(final Integer a, final Double b) {
		if (a == null || b == null) return false;
		return a >= b;
	}

	/**
	 * Greater or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is greater or equal than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3.5 >= 7",
					equals = "false") })
	public static Boolean greaterOrEqual(final Double a, final Integer b) {
		if (a == null || b == null) return false;
		return a >= b;
	}

	/**
	 * Greater or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is greater or equal than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3.5 >= 3.5",
					equals = "true") })
	public static Boolean greaterOrEqual(final Double a, final Double b) {
		if (a == null || b == null) return false;
		return a >= b;
	}

	/**
	 * Op less than or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is less or equal than the right-hand operand, false otherwise.",
			masterDoc = true,
			usages = { @usage ("if one of the operands is nil, returns false") },
			examples = { @example (
					value = "3 <= 7",
					equals = "true") })
	public static Boolean opLessThanOrEqual(final Integer a, final Integer b) {
		if (a == null || b == null) return false;
		return a <= b;
	}

	/**
	 * Less or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is less or equal than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3 <= 2.5",
					equals = "false") },
			see = { GT, LT, GTE, "=", "!=" })
	public static Boolean lessOrEqual(final Integer a, final Double b) {
		if (a == null || b == null) return false;
		return a <= b;
	}

	/**
	 * Less or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is less or equal than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "7.0 <= 7",
					equals = "true") })
	public static Boolean lessOrEqual(final Double a, final Integer b) {
		if (a == null || b == null) return false;
		return a <= b;
	}

	/**
	 * Less or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "true if the left-hand operand is less or equal than the right-hand operand, false otherwise.",
			examples = { @example (
					value = "3.5 <= 3.5",
					equals = "true") })
	public static Boolean lessOrEqual(final Double a, final Double b) {
		if (a == null || b == null) return false;
		return a <= b;
	}

	/**
	 * Equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "returns true if both operands are equal, false otherwise",
			masterDoc = true,
			examples = { @example (
					value = "4.5 = 4.7",
					equals = "false") },
			see = { GT, LT, GTE, LTE, "!=" })
	public static Boolean equal(final Double a, final Double b) {
		return a == null ? b == null : isZeroWidth(a, b);
		// return !(a < b) && !(a > b);
	}

	/**
	 * Equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "returns true if both operands are equal, false otherwise",
			masterDoc = true,
			examples = { @example (
					value = "4 = 5",
					equals = "false") },
			see = { "!=" })
	public static Boolean equal(final Integer a, final Integer b) {
		return a == null ? b == null : a.intValue() == b.intValue();
		// return !(a < b) && !(a > b);
	}

	/**
	 * Equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "returns true if both operands are equal, false otherwise",
			examples = { @example (
					value = "3 = 3.0",
					equals = "true"),
					@example (
							value = "4 = 4.7",
							equals = "false") },
			see = { "!=" })
	public static Boolean equal(final Integer a, final Double b) {
		return a == null ? b == null : isZeroWidth(a.doubleValue(), b);
		// return !(a < b) && !(a > b);
	}

	/**
	 * Equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "returns true if both operands are equal, false otherwise",
			examples = { @example (
					value = "4.7 = 4",
					equals = "false") },
			see = { "!=" })
	public static Boolean equal(final Double a, final Integer b) {
		return a == null ? b == null : isZeroWidth(a, b.doubleValue());
		// return !(a < b) && !(a > b);
	}

	/**
	 * Different.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "true if both operands are different, false otherwise",
			masterDoc = true,
			examples = { @example (
					value = "3.0 != 3.0",
					equals = "false"),
					@example (
							value = "4.0 != 4.7",
							equals = "true") },
			see = { "=", GT, LT, GTE, LTE, "=" })
	public static Boolean different(final Double a, final Double b) {
		if (a == null) return b != null;
		if (b == null) return false;
		return !IntervalSize.isZeroWidth(a, b);
		// return a < b || a > b;
	}

	/**
	 * Different.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = { IConcept.COMPARISON })
	@doc (
			value = "true if both operands are different, false otherwise",
			masterDoc = true,
			examples = { @example (
					value = "3 != 3",
					equals = "false"),
					@example (
							value = "4 != 5",
							equals = "true") },
			see = { "=", GT, LT, GTE, LTE, "=" })
	public static Boolean different(final Integer a, final Integer b) {
		if (a == null) return b != null;
		if (b == null) return false;
		return a.intValue() != b.intValue();
	}

	/**
	 * Different.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "returns true if both operands are different, false otherwise",
			examples = { @example (
					value = "3 != 3.0",
					equals = "false"),
					@example (
							value = "4 != 4.7",
							equals = "true") },
			see = { "=" })
	public static Boolean different(final Integer a, final Double b) {
		return a == null ? b == null : !isZeroWidth(a.doubleValue(), b);
		// return !(a < b) && !(a > b);
	}

	/**
	 * Different.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = "returns true if both operands are different, false otherwise",
			examples = { @example (
					value = "3.0 != 3",
					equals = "false"),
					@example (
							value = "4.7 != 4",
							equals = "true") },
			see = { "=" })
	public static Boolean different(final Double a, final Integer b) {
		return a == null ? b == null : !isZeroWidth(a, b.doubleValue());
	}

	/**
	 * Less or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns true if the left-hand operand is smaller than or equal to the right-hand operand, false otherwise.",
			usages = @usage (
					value = "if both operands are String, uses a lexicographic comparison of two strings",
					examples = { @example (
							value = "'abc' <= 'aeb'",
							equals = "true") }))
	public static Boolean lessOrEqual(final String a, final String b) {
		if (a == null) return false;
		final int i = a.compareTo(b);
		return i <= 0;
	}

	/**
	 * Greater or equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns true if the left-hand operand is greater than or equal to the right-hand operand, false otherwise.",
			usages = @usage (
					value = "if both operands are string, uses a lexicographic comparison of the two strings",
					examples = { @example (
							value = "'abc' >= 'aeb'",
							equals = "false"),
							@example (
									value = "'abc' >= 'abc'",
									equals = "true") }))
	public static Boolean greaterOrEqual(final String a, final String b) {
		if (a == null) return false;
		final int i = a.compareTo(b);
		return i >= 0;
	}

	/**
	 * Less.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.STRING },
			concept = {})
	@doc (
			value = "A lexicographic comparison of two strings. Returns true if the left-hand operand is smaller than the right-hand operand, false otherwise.",
			usages = @usage (
					value = "if both operands are String, uses a lexicographic comparison of two strings",
					examples = { @example (
							value = "'abc' < 'aeb'",
							equals = "true") }))
	public static Boolean less(final String a, final String b) {
		if (a == null) return false;
		final int i = a.compareTo(b);
		return i < 0;
	}

	/**
	 * Greater.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.STRING },
			concept = {})
	@doc (
			value = "Returns true if the left-hand operand is greater than the right-hand operand, false otherwise.",
			usages = @usage (
					value = "if both operands are String, uses a lexicographic comparison of two strings",
					examples = { @example (
							value = "'abc' > 'aeb'",
							equals = "false") }))
	public static Boolean greater(final String a, final String b) {
		if (a == null) return false;
		final int i = a.compareTo(b);
		return i > 0;
	}

	/**
	 * Equal.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@validator (EqualValidator.class)
	@doc (
			usages = @usage (
					value = "if both operands are any kind of objects, returns true if they are identical (i.e., the same object) or equal (comparisons between nil values are permitted)",
					examples = @example (
							value = "[2,3] = [2,3]",
							equals = "true")))
	public static Boolean equal(final Object a, final Object b) {
		return a == null ? b == null : a.equals(b);
	}

	/**
	 * Different.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 */
	@operator (
			value = { "!=" },
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON },
			concept = {})
	@doc (
			value = " Returns false if the two operands are identical (i.e., the same object) or equal. Comparisons between nil values are permitted.",
			examples = { @example (
					value = "[2,3] != [2,3]",
					equals = "false"),
					@example (
							value = "[2,4] != [2,3]",
							equals = "true") })
	public static Boolean different(final Object a, final Object b) {
		return a == null ? b != null : !a.equals(b);
	}

	/**
	 * Less.
	 *
	 * @param p1
	 *            the p 1
	 * @param p
	 *            the p
	 * @return the boolean
	 */
	@operator (
			value = LT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.POINT },
			concept = { IConcept.POINT })
	@doc (
			value = "true if the left-hand operand is lower than the right-hand operand, false otherwise.",
			usages = { @usage (
					value = "if both operands are points, returns true if and only if the left component (x) of the left operand if less than or equal to x of the right one and if the right component (y) of the left operand is greater than or equal to y of the right one.",
					examples = { @example (
							value = "{5,7} < {4,6}",
							equals = "false"),
							@example (
									value = "{5,7} < {4,8}",
									equals = "false") }) })
	public static Boolean less(final GamaPoint p1, final GamaPoint p) {
		return p1.x < p.x && p1.y < p.y;
	}

	/**
	 * Greater.
	 *
	 * @param p1
	 *            the p 1
	 * @param p
	 *            the p
	 * @return the boolean
	 */
	@operator (
			value = GT,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.POINT },
			concept = { IConcept.POINT })
	@doc (
			value = "true if the left-hand operand is greater than the right-hand operand, false otherwise.",
			usages = { @usage (
					value = "if both operands are points, returns true if and only if the left component (x) of the left operand if greater than x of the right one and if the right component (y) of the left operand is greater than y of the right one.",
					examples = { @example (
							value = "{5,7} > {4,6}",
							equals = "true"),
							@example (
									value = "{5,7} > {4,8}",
									equals = "false") }) })
	public static Boolean greater(final GamaPoint p1, final GamaPoint p) {
		return p1.x > p.x && p1.y > p.y;
	}

	/**
	 * Less or equal.
	 *
	 * @param p1
	 *            the p 1
	 * @param p
	 *            the p
	 * @return the boolean
	 */
	@operator (
			value = LTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.POINT },
			concept = { IConcept.POINT })
	@doc (
			value = "true if the left-hand operand is lower or equals than the right-hand operand, false otherwise.",
			usages = { @usage (
					value = "if both operands are points, returns true if and only if the left component (x) of the left operand if less than or equal to x of the right one and if the right component (y) of the left operand is greater than or equal to y of the right one.",
					examples = { @example (
							value = "{5,7} <= {4,6}",
							equals = "false"),
							@example (
									value = "{5,7} <= {4,8}",
									equals = "false") }) })
	public static Boolean lessOrEqual(final GamaPoint p1, final GamaPoint p) {
		return p1.x <= p.x && p1.y <= p.y;
	}

	/**
	 * Greater or equal.
	 *
	 * @param p1
	 *            the p 1
	 * @param p
	 *            the p
	 * @return the boolean
	 */
	@operator (
			value = GTE,
			can_be_const = true,
			category = { IOperatorCategory.COMPARISON, IOperatorCategory.POINT },
			concept = { IConcept.POINT })
	@doc (
			value = "true if the left-hand operand is greater or equals than the right-hand operand, false otherwise.",
			usages = { @usage (
					value = "if both operands are points, returns true if and only if the left component (x) of the left operand if greater or equal than x of the right one and if the right component (y) of the left operand is greater than or equal to y of the right one.",
					examples = { @example (
							value = "{5,7} >= {4,6}",
							equals = "true"),
							@example (
									value = "{5,7} >= {4,8}",
									equals = "false") }) })
	public static Boolean greaterOrEqual(final GamaPoint p1, final GamaPoint p) {
		return p1.x >= p.x && p1.y >= p.y;
	}

}
