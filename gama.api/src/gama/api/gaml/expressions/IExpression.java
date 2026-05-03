/*******************************************************************************************************
 *
 * IExpression.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.expressions;

import java.util.function.Predicate;

import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITyped;
import gama.api.runtime.scope.IScope;
import gama.api.utils.interfaces.IDisposable;

/**
 * Core interface representing executable expressions in the GAML modeling language. IExpression represents the
 * functional, runtime-evaluable component produced by compiling an {@link IExpressionDescription} within a
 * {@link IScope}.
 * 
 * <p>
 * Expressions are the fundamental building blocks of GAML execution. They encapsulate compiled code that can be
 * evaluated at runtime to produce values. Every facet value, variable assignment, condition, and computed value in
 * GAML is represented as an IExpression at runtime.
 * </p>
 * 
 * <h3>Expression Lifecycle:</h3>
 * <ol>
 * <li>GAML source code is parsed into expression descriptions ({@link IExpressionDescription})</li>
 * <li>Expression descriptions are compiled into executable expressions (IExpression) by
 * {@link IExpressionCompiler}</li>
 * <li>Expressions are evaluated within a scope ({@link IScope}) to produce runtime values</li>
 * <li>Expressions can be disposed when no longer needed ({@link IDisposable})</li>
 * </ol>
 * 
 * <h3>Expression Types:</h3>
 * <p>
 * IExpression has several specialized sub-interfaces for different expression kinds:
 * </p>
 * <ul>
 * <li>{@link Unit} - Unit expressions (distance, time, weight units)</li>
 * <li>{@link List} - List literal expressions with multiple elements</li>
 * <li>{@link Map} - Map literal expressions with key-value pairs</li>
 * <li>{@link Species} - Species reference expressions</li>
 * <li>{@link IVarExpression} - Variable reference and assignment expressions</li>
 * <li>{@link IOperator} - Operator expressions with arguments</li>
 * </ul>
 * 
 * <h3>Constant Expressions:</h3>
 * <p>
 * Expressions that return a constant value (independent of scope) should return {@code true} from
 * {@link #isConst()}. Such expressions can be evaluated without a scope using {@link #getConstValue()}, enabling
 * compile-time optimizations and validation.
 * </p>
 * 
 * <h3>Type Information:</h3>
 * <p>
 * As an {@link ITyped}, every expression has an associated GAML type ({@link #getGamlType()}) determined during
 * compilation. Some expressions (like type literals) can denote types via {@link #getDenotedType()}.
 * </p>
 * 
 * <h3>Context Dependency:</h3>
 * <p>
 * Expressions can depend on various contextual elements:
 * </p>
 * <ul>
 * <li><b>Scope-dependent:</b> Require a scope for evaluation (most expressions)</li>
 * <li><b>Context-independent:</b> Don't use attributes, variables, or species ({@link #isContextIndependant()})</li>
 * <li><b>Time-dependent:</b> Value changes based on simulation time ({@link #isTimeDependent()})</li>
 * <li><b>Pixel-dependent:</b> Contains pixel units that depend on display context ({@link #containsPixels()})</li>
 * </ul>
 * 
 * <h3>Serialization:</h3>
 * <p>
 * Expressions can be serialized back to GAML source code via {@link IGamlDescription#serializeToGaml(boolean)}. The
 * {@link #literalValue()} provides a simplified string representation, while {@link #shouldBeParenthesized()}
 * indicates if parentheses are needed when serializing within larger expressions.
 * </p>
 * 
 * <h3>Expression Analysis:</h3>
 * <p>
 * The {@link #findAny(Predicate)} method enables recursive searching for sub-expressions matching specific criteria,
 * useful for validation, optimization, and dependency analysis.
 * </p>
 * 
 * <h3>Usage Examples:</h3>
 * 
 * <pre>
 * // Evaluating an expression
 * IExpression expr = compiler.compile("2 + 2", context);
 * Object result = expr.value(scope); // Returns 4
 * 
 * // Constant expression optimization
 * if (expr.isConst()) {
 * 	Object constValue = expr.getConstValue(); // No scope needed
 * 	// Can cache or optimize at compile time
 * }
 * 
 * // Type checking
 * IType<?> exprType = expr.getGamlType();
 * if (exprType.isNumber()) {
 * 	// Handle numeric expression
 * }
 * 
 * // Finding sub-expressions
 * boolean hasVarRef = expr.findAny(e -> e instanceof IVarExpression);
 * 
 * // Serialization
 * String gamlCode = expr.serializeToGaml(false);
 * </pre>
 * 
 * <h3>Thread Safety:</h3>
 * <p>
 * Expression instances are generally NOT thread-safe. Each simulation thread should use its own expression instances
 * or ensure proper synchronization. The expression tree structure itself is immutable after compilation, but
 * evaluation state is not.
 * </p>
 * 
 * <h3>Resource Management:</h3>
 * <p>
 * As {@link IDisposable}, expressions should be disposed when no longer needed to release resources. However, most
 * expressions have minimal resource requirements and default disposal is typically a no-op.
 * </p>
 * 
 * @author A. Drogoul
 * @since 25 dec. 2010
 * @since August 2018, IExpression is a @FunctionalInterface in some contexts
 * @see IExpressionDescription
 * @see IExpressionCompiler
 * @see IScope
 * @see ITyped
 */

public interface IExpression extends IGamlDescription, ITyped, IDisposable, IVarDescriptionUser {

	/**
	 * Specialized expression interface for unit expressions. Unit expressions represent physical measurement units
	 * (like meters, seconds, kilograms) that can be used in GAML to express dimensional quantities.
	 * 
	 * <p>
	 * This interface combines IExpression with IExpressionDescription, allowing unit expressions to serve both as
	 * executable expressions and as descriptions of themselves.
	 * </p>
	 * 
	 * <p>
	 * Examples of unit expressions: {@code #m}, {@code #km}, {@code #sec}, {@code #kg}
	 * </p>
	 * 
	 * @see GamlCoreUnits
	 */
	interface Unit extends IExpression, IExpressionDescription {

		/**
		 * Indicates whether this unit expression represents a constant value. Unit expressions can be constant (like
		 * {@code #m = 1.0}) or dynamic depending on their definition.
		 * 
		 * @return true if this unit expression is constant, false otherwise
		 */
		@Override
		default boolean isConst() { return IExpression.super.isConst(); }

	}

	/**
	 * Specialized expression interface for list literal expressions. List expressions represent literal list
	 * constructions in GAML, containing multiple element expressions.
	 * 
	 * <p>
	 * List expressions are created from GAML list literals like {@code [1, 2, 3]} or {@code [agent1, agent2]} and
	 * provide access to their constituent element expressions.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>
	 * // GAML: list<int> numbers <- [1, 2, 3, 4, 5];
	 * // The right-hand side is compiled to an IExpression.List
	 * IExpression.List listExpr = ...;
	 * IExpression[] elements = listExpr.getElements(); // Returns 5 expressions
	 * </pre>
	 * 
	 * @see Map
	 */
	interface List extends IExpression {

		/**
		 * Returns all element expressions contained in this list literal. The order of elements matches the order in
		 * the GAML source code.
		 * 
		 * @return an array of expressions representing the list elements, never null but may be empty
		 */
		IExpression[] getElements();

		/**
		 * Checks if this list literal contains a constant element with the specified value. This is useful for
		 * compile-time validation and optimization.
		 * 
		 * @param init
		 *            the value to search for among constant elements
		 * @return true if any element is a constant expression whose value equals the given object, false otherwise
		 */
		boolean containsConstValue(Object init);
	}

	/**
	 * Specialized expression interface for map literal expressions. Map expressions represent literal map
	 * constructions in GAML, containing key-value pair expressions.
	 * 
	 * <p>
	 * Map expressions are created from GAML map literals like {@code ["a"::1, "b"::2]} or {@code [key1::value1]} and
	 * provide access to their constituent key and value expressions.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>
	 * // GAML: map<string, int> lookup <- ["a"::1, "b"::2, "c"::3];
	 * // The right-hand side is compiled to an IExpression.Map
	 * IExpression.Map mapExpr = ...;
	 * java.util.Map<IExpression, IExpression> pairs = mapExpr.getElements();
	 * IExpression[] values = mapExpr.getValues();
	 * </pre>
	 * 
	 * @see List
	 */
	interface Map extends IExpression {

		/**
		 * Returns all value expressions contained in this map literal. The order may not be guaranteed depending on
		 * the map implementation.
		 * 
		 * @return an array of expressions representing the map values, never null but may be empty
		 */
		IExpression[] getValues();

		/**
		 * Returns all key-value pairs as a map of expressions. Each entry maps a key expression to its corresponding
		 * value expression.
		 * 
		 * @return a Java map containing all key-value expression pairs, never null but may be empty
		 */
		java.util.Map<IExpression, IExpression> getElements();
	}

	/**
	 * Specialized expression interface for species reference expressions. This is a tagging interface used for type
	 * identification via {@code instanceof} checks.
	 * 
	 * <p>
	 * Species expressions represent references to GAML species (agent types) in contexts where a species type is
	 * expected, such as in {@code create} statements or type declarations.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>
	 * // GAML: create my_species number: 10;
	 * // "my_species" is compiled to an IExpression.Species
	 * </pre>
	 */
	interface Species extends IExpression {
		// Tagging interface for instanceof for the moment
	}

	/**
	 * Convenience method for obtaining the constant value without passing a scope. Should be invoked after testing the
	 * expression with {@link IExpression#isConst()}. All runtime exceptions are caught and the method returns null in
	 * case of exceptions. Typically useful in validation contexts
	 *
	 * @return
	 */
	default Object getConstValue() {
		try {
			return value(null);
		} catch (final RuntimeException e) {
			return null;
		}
	}

	/**
	 * Evaluates this expression within the given scope and returns the resulting value. This is the core method for
	 * expression evaluation at runtime.
	 * 
	 * <p>
	 * The evaluation process may involve:
	 * </p>
	 * <ul>
	 * <li>Reading variable values from the scope</li>
	 * <li>Calling operators with sub-expression results</li>
	 * <li>Accessing agent attributes</li>
	 * <li>Performing type conversions</li>
	 * <li>Executing nested expressions</li>
	 * </ul>
	 * 
	 * <p>
	 * The returned value type should match the expression's GAML type ({@link #getGamlType()}), though runtime type
	 * coercion may occur in some cases.
	 * </p>
	 * 
	 * <h3>Examples:</h3>
	 * 
	 * <pre>
	 * // Simple constant expression
	 * IExpression constExpr = ...; // represents "5"
	 * Object result = constExpr.value(scope); // Returns Integer 5
	 * 
	 * // Variable reference expression
	 * IExpression varExpr = ...; // represents "my_var"
	 * Object value = varExpr.value(scope); // Returns current value of my_var in scope
	 * 
	 * // Operator expression
	 * IExpression opExpr = ...; // represents "2 + 3"
	 * Object sum = opExpr.value(scope); // Returns Integer 5
	 * </pre>
	 * 
	 * @param scope
	 *            the current GAMA execution scope providing access to variables, agents, and runtime context. May be
	 *            null only for constant expressions.
	 * @return the result of evaluating this expression. The type depends on the expression's GAML type.
	 * @throws GamaRuntimeException
	 *             if an error occurs during evaluation (e.g., undefined variable, type mismatch, division by zero)
	 * @see #isConst()
	 * @see #getConstValue()
	 */
	Object value(final IScope scope) throws GamaRuntimeException;

	/**
	 * Indicates whether this expression is constant, meaning it does not depend on runtime scope and always evaluates
	 * to the same value.
	 * 
	 * <p>
	 * Constant expressions include:
	 * </p>
	 * <ul>
	 * <li>Literal values (numbers, strings, booleans)</li>
	 * <li>Constant references (#pi, #e, etc.)</li>
	 * <li>Expressions composed entirely of other constant expressions</li>
	 * </ul>
	 * 
	 * <p>
	 * Non-constant expressions include:
	 * </p>
	 * <ul>
	 * <li>Variable references</li>
	 * <li>Agent attribute accesses</li>
	 * <li>Expressions involving variables or agents</li>
	 * <li>Time-dependent expressions</li>
	 * </ul>
	 * 
	 * <p>
	 * Constant expressions can be evaluated without a scope using {@link #getConstValue()} and may be optimized or
	 * pre-computed at compilation time.
	 * </p>
	 * 
	 * @return true if this expression is constant and scope-independent, false otherwise
	 * @see #getConstValue()
	 */
	default boolean isConst() {
		// By default
		return false;
	}

	/**
	 * Returns a simplified literal string representation of this expression. The format depends on the expression
	 * type and may not always be valid GAML code.
	 * 
	 * <p>
	 * Common literal value representations:
	 * </p>
	 * <ul>
	 * <li>Constant expressions: their string representation (e.g., "5", "\"hello\"", "true")</li>
	 * <li>Variable expressions: the variable name (e.g., "my_var")</li>
	 * <li>Operator expressions: a simplified form of the operation</li>
	 * </ul>
	 * 
	 * <p>
	 * For proper GAML serialization, use {@link #serializeToGaml(boolean)} instead.
	 * </p>
	 * 
	 * @return a human-readable string representation of this expression, defaults to {@link #getName()} if not
	 *         overridden
	 * @see #serializeToGaml(boolean)
	 */
	default String literalValue() {
		return getName();
	}

	/**
	 * Returns a new expression where all temporary variables from the given scope are resolved to constant
	 * expressions representing their current values. This is used for expression optimization and closure creation.
	 * 
	 * <p>
	 * Temporary variables (like loop variables 'each', or let-bound variables) are replaced by constant expressions
	 * containing their current values from the scope. This allows creating expressions that can be evaluated
	 * independently of the original scope.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>
	 * // Original expression: "each + offset"
	 * // where 'each' is 5 and 'offset' is a regular variable
	 * IExpression resolved = expr.resolveAgainst(scope);
	 * // Result: "5 + offset" (each is replaced by constant 5)
	 * </pre>
	 * 
	 * @param scope
	 *            the scope providing temporary variable values
	 * @return a new expression with temporary variables resolved, or this expression if no resolution is needed
	 */
	default IExpression resolveAgainst(final IScope scope) {
		return this;
	}

	/**
	 * Indicates whether this expression should be enclosed in parentheses when serialized as part of a larger
	 * expression. This is used to maintain correct operator precedence and expression structure during serialization.
	 * 
	 * <p>
	 * Expressions that typically require parenthesization:
	 * </p>
	 * <ul>
	 * <li>Binary operations when used as operands to other operators</li>
	 * <li>Complex expressions to clarify precedence</li>
	 * </ul>
	 * 
	 * <p>
	 * Expressions that typically don't require parenthesization:
	 * </p>
	 * <ul>
	 * <li>Literal values and constants</li>
	 * <li>Variable references</li>
	 * <li>Function calls (already have parentheses)</li>
	 * </ul>
	 * 
	 * @return true if parentheses should be added when serializing, false otherwise (default is true for safety)
	 * @see #serializeToGaml(boolean)
	 */

	default boolean shouldBeParenthesized() {
		return true;
	}

	/**
	 * Indicates whether this expression is independent of execution context, meaning it doesn't access variables,
	 * agent attributes, or species-specific information.
	 * 
	 * <p>
	 * Context-independent expressions can be safely evaluated in any scope without concern for variable availability
	 * or agent state. However, they may still require a scope for evaluation (unlike constant expressions).
	 * </p>
	 * 
	 * <p>
	 * Context-independent examples:
	 * </p>
	 * <ul>
	 * <li>Pure mathematical operations on constants: {@code 2 + 2}</li>
	 * <li>Constant references: {@code #pi * 2}</li>
	 * <li>Literal values</li>
	 * </ul>
	 * 
	 * <p>
	 * Context-dependent examples:
	 * </p>
	 * <ul>
	 * <li>Variable references: {@code my_variable}</li>
	 * <li>Agent attributes: {@code self.location}</li>
	 * <li>Species queries: {@code my_species at_distance 10}</li>
	 * </ul>
	 * 
	 * @return true if the expression doesn't use any attributes, variables or species, false otherwise
	 * @see #isConst()
	 */
	default boolean isContextIndependant() { return true; }

	/**
	 * Returns the GAML type denoted by this expression. For most expressions, this is the same as their evaluation
	 * type ({@link #getGamlType()}), but for type literal expressions, it returns the type they represent.
	 * 
	 * <p>
	 * The distinction is important for expressions that reference types themselves:
	 * </p>
	 * <ul>
	 * <li>For normal expressions: {@code getDenotedType() == getGamlType()}</li>
	 * <li>For type expressions: {@code getDenotedType()} returns the referenced type</li>
	 * </ul>
	 * 
	 * <h3>Examples:</h3>
	 * 
	 * <pre>
	 * // Expression: 5
	 * // getGamlType() -> int, getDenotedType() -> int
	 * 
	 * // Expression: int (type literal)
	 * // getGamlType() -> type<int>, getDenotedType() -> int
	 * 
	 * // Expression: list<agent>
	 * // getGamlType() -> type<list<agent>>, getDenotedType() -> list<agent>
	 * </pre>
	 * 
	 * @return the type denoted by this expression, defaults to the expression's GAML type
	 * @see #getGamlType()
	 */
	default IType<?> getDenotedType() { return getGamlType(); }

	/**
	 * Recursively searches this expression tree for any expression (including this one) that matches the given
	 * predicate. The search includes this expression and all sub-expressions.
	 * 
	 * <p>
	 * This method is useful for:
	 * </p>
	 * <ul>
	 * <li>Finding variable references within an expression</li>
	 * <li>Checking if an expression uses certain operators</li>
	 * <li>Validating expression constraints</li>
	 * <li>Analyzing expression dependencies</li>
	 * </ul>
	 * 
	 * <h3>Examples:</h3>
	 * 
	 * <pre>
	 * // Check if expression contains any variable references
	 * boolean hasVars = expr.findAny(e -> e instanceof IVarExpression);
	 * 
	 * // Check if expression uses specific operator
	 * boolean usesPlus = expr.findAny(e -> e instanceof IOperator && "+".equals(e.getName()));
	 * 
	 * // Check if expression is constant throughout
	 * boolean allConst = !expr.findAny(e -> !e.isConst());
	 * </pre>
	 * 
	 * <p>
	 * The default implementation only tests this expression. Subclasses with sub-expressions (like operators) should
	 * override to recursively test their arguments.
	 * </p>
	 * 
	 * @param predicate
	 *            the test to apply to each expression in the tree
	 * @return true if this expression or any sub-expression satisfies the predicate, false otherwise
	 */
	default boolean findAny(final Predicate<IExpression> predicate) {
		return predicate.test(this);
	}

	/**
	 * Indicates whether this expression is allowed to be used in experiment parameters. Some expressions may be
	 * restricted from use in parameter contexts due to scope dependencies or other limitations.
	 * 
	 * @return true if this expression can be used in experiment parameters (default), false if restricted
	 */
	default boolean isAllowedInParameters() { return true; }

	/**
	 * Indicates whether this expression represents an empty value or collection. Used for optimization and validation
	 * of empty literal expressions.
	 * 
	 * @return true if this expression represents an empty value, false otherwise (default)
	 */
	default boolean isEmpty() { return false; }

	/**
	 * Indicates whether this expression uses deprecated features or syntax. Deprecated expressions may generate
	 * warnings during compilation.
	 * 
	 * @return true if this expression is deprecated, false otherwise (default)
	 */
	default boolean isDeprecated() { return false; }

	/**
	 * Indicates whether this expression represents a non-modifiable value, such as a constant or read-only attribute.
	 * Used for validation when the expression appears on the left-hand side of an assignment.
	 * 
	 * @return true if this expression cannot be assigned to, false otherwise (default)
	 * @see IVarExpression#isNotModifiable()
	 */
	default boolean isNotModifiable() { return false; }

	/**
	 * Indicates whether this expression contains pixel units ({@code #pixels} or {@code #px}) that depend on the
	 * current display context. Pixel-dependent expressions require a graphics context to evaluate correctly.
	 * 
	 * @return true if this expression contains pixel units, false otherwise (default)
	 */
	default boolean containsPixels() {
		return false;
	}

	/**
	 * Indicates whether this expression's value depends on simulation time. Time-dependent expressions include
	 * references to the current cycle, simulation time, or date.
	 * 
	 * <p>
	 * Examples of time-dependent expressions:
	 * </p>
	 * <ul>
	 * <li>{@code cycle}</li>
	 * <li>{@code time}</li>
	 * <li>{@code current_date}</li>
	 * <li>{@code #now}</li>
	 * </ul>
	 * 
	 * @return true if the expression's value changes with simulation time, false otherwise (default)
	 */
	default boolean isTimeDependent() { return false; }

	/**
	 * Returns the {@link java.time.temporal.ChronoUnit} used by this expression when it represents a calendar-based
	 * duration (months or years). Returns {@code null} for expressions that do not use calendar units.
	 *
	 * <p>
	 * This is used by the {@code every} operator to detect when calendar-correct arithmetic (adding whole months or
	 * years) should be used instead of fixed-millisecond modular arithmetic, which would otherwise drift because the
	 * length of a month or year varies.
	 * </p>
	 *
	 * <p>
	 * For example, {@code 2#months} returns {@link java.time.temporal.ChronoUnit#MONTHS}, and {@code 3#years} returns
	 * {@link java.time.temporal.ChronoUnit#YEARS}. Fixed-duration expressions like {@code 30#day} return {@code null}.
	 * </p>
	 *
	 * @return {@link java.time.temporal.ChronoUnit#MONTHS}, {@link java.time.temporal.ChronoUnit#YEARS}, or
	 *         {@code null} if this expression does not contain a calendar-based time unit
	 */
	default java.time.temporal.ChronoUnit getCalendarChronoUnit() { return null; }

}