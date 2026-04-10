/*******************************************************************************************************
 *
 * Logic.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators;

import java.util.Objects;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.types.Cast;
import gama.api.runtime.scope.IScope;

/**
 * Provides the core boolean and ternary logic operators for the GAML language.
 *
 * <p>This class implements the following GAML operators:</p>
 * <ul>
 *   <li><b>xor</b> – exclusive-or of two boolean operands</li>
 *   <li><b>or</b>  – short-circuit logical OR (right operand evaluated lazily)</li>
 *   <li><b>and</b> – short-circuit logical AND (right operand evaluated lazily)</li>
 *   <li><b>!</b> / <b>not</b> – boolean negation</li>
 *   <li><b>?</b>  – ternary conditional (used together with <b>:</b>)</li>
 *   <li><b>:</b>  – ternary branch selector (used together with <b>?</b>)</li>
 * </ul>
 *
 * <p>All boolean operands are cast to {@code bool} before evaluation, so non-boolean
 * values (e.g. integers) are accepted wherever a boolean is expected.</p>
 *
 * <p>Short-circuit semantics apply to {@code or} and {@code and}: the right-hand
 * operand is only evaluated when the left-hand operand does not already determine
 * the result.</p>
 *
 * @author Alexis Drogoul
 * @see Comparison
 */
public class Logic {

	/**
	 * Xor.
	 *
	 * @param scope the scope
	 * @param left the left
	 * @param right the right
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "xor",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Returns the exclusive-or (XOR) of the two boolean operands. "
					+ "The result is {@code true} when exactly one operand is {@code true}, and {@code false} when both operands are equal.",
			returns = "a {@code bool} equal to {@code true} iff exactly one operand is {@code true}.",
			comment = "Both operands are always cast to {@code bool} before the operator is applied. "
					+ "An expression such as {@code 1 xor 0} is therefore accepted and returns {@code true}.",
			special_cases = {
					"If either operand is {@code nil}, it is first cast to {@code false} before evaluation.",
					"xor(nil, false) is equivalent to xor(false, false) and therefore returns false.",
					"xor(nil, true) is equivalent to xor(false, true) and therefore returns true."
			},
			see = { "or", "and", "!" },
			examples = {
					@example (value = "xor(true,false)",  equals = "true"),
					@example (value = "xor(false,false)", equals = "false"),
					@example (value = "xor(false,true)",  equals = "true"),
					@example (value = "xor(true,true)",   equals = "false"),
					@example (value = "true xor true",    equals = "false"),
					@example (value = "true xor false",   equals = "true")
			})
	@test ("xor(true,false)")
	@test ("xor(false,true)")
	@test ("!xor(false,false)")
	@test ("!xor(true,true)")
	public static Boolean xor(final IScope scope, final Boolean left, final Boolean right) throws GamaRuntimeException {
		return !Objects.equals(left, right);
	}

	/**
	 * Or.
	 *
	 * @param scope the scope
	 * @param left the left
	 * @param right the right
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "or",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Returns the logical OR of the two boolean operands.",
			returns = "a {@code bool} equal to {@code true} if at least one of the operands is {@code true}.",
			comment = "Both operands are cast to {@code bool} before the operator is applied. "
					+ "An expression like {@code 1 or 0} is therefore accepted and returns {@code true}. "
					+ "The operator uses <b>short-circuit</b> evaluation: if the left operand is already {@code true}, "
					+ "the right operand is <em>not</em> evaluated.",
			special_cases = {
					"If the left operand is {@code nil}, it is treated as {@code false} and the right operand is evaluated.",
					"If the right operand is {@code nil}, it is cast to {@code false}."
			},
			see = { "bool", "and", "!" },
			examples = {
					@example (value = "true or false",  equals = "true"),
					@example (value = "false or true",  equals = "true"),
					@example (value = "false or false", equals = "false"),
					@example (value = "true or true",   equals = "true"),
					@example (" int a <- 3 ; int b <- 4; int c <- 7;"),
					@example (value = "((a+b) = c ) or ((a+b) > c )", equals = "true")
			})
	@test ("false or false = false")
	@test ("false or true")
	@test ("true or false")
	@test ("true or true")
	public static Boolean or(final IScope scope, final Boolean left, final IExpression right)
			throws GamaRuntimeException {
		return left != null && left || right != null && Cast.asBool(scope, right.value(scope));
	}

	/**
	 * And.
	 *
	 * @param scope the scope
	 * @param left the left
	 * @param right the right
	 * @return the boolean
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "and",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Returns the logical AND of the two boolean operands.",
			returns = "a {@code bool} equal to {@code true} only if both operands are {@code true}.",
			comment = "Both operands are cast to {@code bool} before the operator is applied. "
					+ "An expression such as {@code (1 and 0)} is therefore accepted and returns {@code false}. "
					+ "The operator uses <b>short-circuit</b> evaluation: if the left operand is {@code false} or {@code nil}, "
					+ "the right operand is <em>not</em> evaluated.",
			special_cases = {
					"If the left operand is {@code nil} or {@code false}, the right operand is never evaluated and the result is {@code false}.",
					"If the right operand is {@code nil}, it is cast to {@code false}."
			},
			see = { "bool", "or", "!" },
			examples = {
					@example (value = "true and false",  equals = "false"),
					@example (value = "false and false", equals = "false"),
					@example (value = "false and true",  equals = "false"),
					@example (value = "true and true",   equals = "true"),
					@example (" int a <- 3 ; int b <- 4; int c <- 7;"),
					@example (value = "((a+b) = c ) and ((a+b) > c )", equals = "false")
			})
	@test ("true and true")
	@test ("!(true and false)")
	@test ("!(false and true)")
	@test ("!(false and false)")
	public static Boolean and(final IScope scope, final Boolean left, final IExpression right)
			throws GamaRuntimeException {
		return left != null && left && right != null && Cast.asBool(scope, right.value(scope));
	}

	/**
	 * Not.
	 *
	 * @param b the b
	 * @return the boolean
	 */
	@operator (
			value = { "!", "not" },
			can_be_const = true,
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Returns the logical negation (NOT) of the boolean operand.",
			returns = "a {@code bool} equal to {@code true} if the operand is {@code false}, and vice-versa.",
			special_cases = {
					"If the operand is not a boolean, it is cast to a boolean value before negation: "
							+ "most non-nil, non-zero values cast to {@code true}, so their negation is {@code false}."
			},
			examples = {
					@example (value = "! (true)",  equals = "false"),
					@example (value = "! (false)", equals = "true"),
					@example (value = "not(true)",  equals = "false"),
					@example (value = "not(false)", equals = "true")
			},
			see = { "bool", "and", "or" })
	@test ("!(true) = false")
	@test ("!(false) = true")
	@test ("not(true) = false")
	@test ("not(false) = true")
	public static Boolean not(final Boolean b) {
		return !b;
	}

	/**
	 * Iff.
	 *
	 * @param scope the scope
	 * @param left the left
	 * @param right the right
	 * @return the object
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "?",
			type = ITypeProvider.TYPE_AT_INDEX + 2,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL, IConcept.TERNARY })
	@doc (
			value = "Ternary conditional operator. Used in combination with the {@code :} operator: "
					+ "if the left-hand operand evaluates to {@code true}, returns the value of the left-hand branch of {@code :}; "
					+ "otherwise returns the value of the right-hand branch of {@code :}.",
			returns = "the value of the chosen branch; the type is inferred from the {@code :} expression.",
			comment = "These ternary expressions can be nested and combined freely. "
					+ "Only the chosen branch is evaluated (lazy evaluation of the other branch).",
			special_cases = {
					"If the condition is {@code nil}, it is cast to {@code false} and the else-branch is returned.",
					"Both branches must be of compatible types; a type mismatch will produce a validation warning."
			},
			examples = {
					@example (
							value = "[10, 19, 43, 12, 7, 22] collect ((each > 20) ? 'above' : 'below')",
							returnType = "list<string>",
							equals = "['below', 'below', 'above', 'below', 'below', 'above']"),
					@example (value = "(1 > 0) ? 'yes' : 'no'", equals = "'yes'"),
					@example (value = "(1 < 0) ? 'yes' : 'no'", equals = "'no'"),
					@example ("rgb col <- (flip(0.3) ? #red : (flip(0.9) ? #blue : #green));")
			},
			see = ":")
	@test ("(1 > 0) ? 'yes' : 'no' = 'yes'")
	@test ("(1 < 0) ? 'yes' : 'no' = 'no'")
	@test ("(true ? 1 : 2) = 1")
	@test ("(false ? 1 : 2) = 2")
	public static Object iff(final IScope scope, final Boolean left, final IExpression right)
			throws GamaRuntimeException {
		final IOperator expr = (IOperator) right;
		return left ? expr.arg(0).value(scope) : expr.arg(1).value(scope);
	}

	/**
	 * Then.
	 *
	 * @param scope the scope
	 * @param a the a
	 * @param b the b
	 * @return the object
	 */
	@operator (
			value = ":",
			type = ITypeProvider.BOTH,
			content_type = ITypeProvider.BOTH,
			index_type = ITypeProvider.BOTH,
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL, IConcept.TERNARY })
	@doc (
			value = "Branch-selector operator used exclusively in combination with the {@code ?} ternary operator. "
					+ "If the condition evaluated by {@code ?} is {@code true}, the left-hand operand of {@code :} is returned; "
					+ "otherwise the right-hand operand is returned.",
			returns = "one of the two branch values, depending on the condition evaluated by {@code ?}. "
					+ "The return type is the common supertype of both branches.",
			comment = "This operator is <em>never called directly</em> at runtime. "
					+ "It is only used as a syntactic marker for the GAML parser and is always "
					+ "evaluated in the context of a preceding {@code ?} operator.",
			examples = {
					@example (
							value = "[10, 19, 43, 12, 7, 22] collect ((each > 20) ? 'above' : 'below')",
							returnType = "list<string>",
							equals = "['below', 'below', 'above', 'below', 'below', 'above']"),
					@example (value = "(true ? 42 : 0)", equals = "42")
			},
			see = "?")
	public static Object then(final IScope scope, final Object a, final Object b) {
		return null;
		// should never be called
	}

}