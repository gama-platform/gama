/*******************************************************************************************************
 *
 * IExpressionFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.factories;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.IExecutionContext;
import gama.api.runtime.scope.InScope;

/**
 * Factory interface for creating GAML expression objects with optimized performance and memory management.
 *
 * This interface defines the contract for creating various types of expressions in the GAML language: - Constant
 * expressions (literals, units, species references) - Variable expressions (agent variables, global variables,
 * temporary variables) - Operator expressions (unary, binary, n-ary operators with automatic type coercion) -
 * Collection expressions (lists, maps) - Action expressions (method calls, temporary actions) - Type expressions and
 * casting operations
 *
 * Key Features: - Thread-safe expression creation and compilation - Automatic type coercion and signature matching for
 * operators - Support for both static and dynamic expression creation - Caching capabilities for improved performance -
 * Comprehensive error handling and validation
 *
 * Implementations should provide: - Efficient operator signature matching with caching - Memory management for parser
 * instances - Thread-local storage for thread safety - Comprehensive validation and error reporting
 *
 * @author drogoul
 * @author Modified on 27 déc. 2010
 * @version 3.0 - Enhanced with comprehensive documentation and performance optimizations
 *
 * @see GamlExpressionFactory for the default implementation
 */
@SuppressWarnings ({ "rawtypes" })
public interface IExpressionFactory {

	/** The temporary action name used for dynamically created actions. */
	String TEMPORARY_ACTION_NAME = "__synthetic__action__";

	// public void registerParserProvider(IExpressionCompilerProvider parser);

	/**
	 * Creates a constant expression from a value with automatic type inference. This is a convenience method that
	 * creates a ConstantExpression without explicitly specifying the type - the type will be inferred from the value.
	 *
	 * @param val
	 *            the constant value to wrap in an expression
	 * @return a new ConstantExpression containing the given value
	 */
	IExpression createConst(final Object val);

	/**
	 * Creates a constant expression with the specified value and type. This is a convenience method that calls
	 * createConst(val, type, null).
	 *
	 * @param val
	 *            the constant value, may be null
	 * @param type
	 *            the type of the constant, must not be null
	 * @return a new constant expression, never null
	 * @throws GamaRuntimeException
	 *             if the value cannot be converted to the specified type
	 */
	IExpression createConst(final Object val, final IType type) throws GamaRuntimeException;

	/**
	 * Creates a constant expression with the specified value, type, and optional name. This method handles special
	 * cases for different types and optimizes for common values.
	 *
	 * @param val
	 *            the constant value, may be null
	 * @param type
	 *            the type of the constant, must not be null
	 * @param name
	 *            optional name for the constant, may be null
	 * @return a new constant expression, never null
	 * @throws GamaRuntimeException
	 *             if the value cannot be converted to the specified type
	 */
	IExpression createConst(final Object val, final IType type, String name) throws GamaRuntimeException;

	/**
	 * Creates a species constant expression for the given type. Species constants represent references to agent species
	 * in GAML.
	 *
	 * @param type
	 *            the species type, must have SPECIES as its GAML type
	 * @return a new SpeciesConstantExpression or null if type is invalid
	 */
	IExpression createSpeciesConstant(final IType type);

	/**
	 * @param name
	 * @return
	 */
	IExpression createSkillConstant(String name);

	/**
	 * Creates an expression from an expression description within a given context. This method delegates to the parser
	 * to compile the expression description into an executable expression object.
	 *
	 * @param s
	 *            the expression description to compile, must not be null
	 * @param context
	 *            the compilation context providing variable scope and type information
	 * @return the compiled expression, or null if compilation fails
	 */
	IExpression createExpr(final IExpressionDescription s, final IDescription context);

	/**
	 * Creates an expression from a functional interface that executes within a scope. This method allows creating
	 * expressions from lambda expressions or method references that implement the InScope interface for direct
	 * scope-based execution.
	 *
	 * @param <T>
	 *            the return type of the expression
	 * @param exp
	 *            the functional interface that defines the expression logic
	 * @param type
	 *            the GAML type that this expression will return
	 * @return a new anonymous expression that executes the provided function
	 */
	<T> IExpression createExpr(final InScope<T> exp, final IType type);

	/**
	 * Creates an expression from a string representation within a given context. The string is first wrapped in a
	 * StringBasedExpressionDescription before compilation.
	 *
	 * @param s
	 *            the string representation of the expression, may be null or empty
	 * @param context
	 *            the compilation context providing variable scope and type information
	 * @return the compiled expression, or null if the string is null/empty or compilation fails
	 */
	IExpression createExpr(final String s, IDescription context);

	/**
	 * Creates an expression from a string with additional execution context. This overload allows providing runtime
	 * context information that may be needed during expression compilation for context-sensitive expressions.
	 *
	 * @param s
	 *            the string representation of the expression, may be null or empty
	 * @param context
	 *            the compilation context providing variable scope and type information
	 * @param additionalContext
	 *            additional execution context for runtime information
	 * @return the compiled expression, or null if the string is null/empty or compilation fails
	 */
	IExpression createExpr(final String s, final IDescription context, final IExecutionContext additionalContext);

	/**
	 * Creates an argument map for action calls by parsing expression descriptions. This method processes the expression
	 * description containing action parameters and returns a structured Arguments object suitable for action execution.
	 *
	 * @param action
	 *            the action description that defines expected parameters
	 * @param args
	 *            the expression description containing the argument values
	 * @param context
	 *            the compilation context for resolving references
	 * @return a new Arguments object containing parsed parameters, or null if args is null
	 */
	Arguments createArgumentMap(IActionDescription action, IExpressionDescription args, IDescription context);

	// IExpressionCompiler getParser();

	/**
	 * Creates a variable expression with the specified parameters. This method creates different types of variable
	 * expressions based on the scope: - GLOBAL: Variables accessible throughout the model - AGENT: Variables specific
	 * to individual agents - TEMP: Temporary variables with limited lifetime - EACH: Variables used in iterations -
	 * SELF: Reference to the current agent - SUPER: Reference to the parent species - MYSELF: Reference to the agent
	 * calling this action
	 *
	 * @param name
	 *            the name of the variable, must not be null or empty
	 * @param type
	 *            the type of the variable, must not be null
	 * @param isConst
	 *            true if the variable is constant (immutable)
	 * @param scope
	 *            the scope level of the variable (see IVarExpression constants)
	 * @param definitionDescription
	 *            the description context where the variable is defined
	 * @return a new variable expression appropriate for the specified scope, or null for unknown scope
	 */
	IExpression createVar(String name, IType type, boolean isConst, int scope, IDescription definitionDescription);

	/**
	 * Creates a list expression from an iterable collection of expressions. The resulting expression will evaluate to a
	 * GAML list containing the values of all provided expressions when executed.
	 *
	 * @param elements
	 *            the collection of expressions to include in the list, may be null or empty
	 * @return a new ListExpression containing all provided expressions
	 */
	IExpression createList(final Iterable<? extends IExpression> elements);

	/**
	 * Creates a map expression from an iterable collection of expressions. The elements should contain key-value pairs
	 * that will be used to construct a GAML map when the expression is evaluated.
	 *
	 * @param elements
	 *            the collection of expressions representing key-value pairs, may be null or empty
	 * @return a new MapExpression that will evaluate to a GAML map
	 */
	IExpression createMap(final Iterable<? extends IExpression> elements);

	/**
	 * Creates an operator expression with automatic type coercion and signature matching. This is the main method for
	 * creating operator expressions in GAML. It performs: - Signature matching against available operators - Automatic
	 * type coercion when needed (e.g., int to float) - VarArg handling for operators that accept variable arguments -
	 * Deprecation warnings for deprecated operators
	 *
	 * The method uses a sophisticated matching algorithm that finds the best matching operator signature by calculating
	 * type distance and performing necessary conversions.
	 *
	 * @param op
	 *            the operator name (e.g., "+", "-", "and", "or")
	 * @param context
	 *            the compilation context for error reporting and type resolution
	 * @param currentEObject
	 *            the source EObject for error location reporting
	 * @param args
	 *            the array of argument expressions, must not be null or contain null elements
	 * @return a new operator expression, or null if no suitable operator found or error occurred
	 */
	IExpression createOperator(String op, IDescription context, EObject currentEObject, IExpression... args);

	/**
	 * Creates an operator expression from a prototype and expressions. This method handles the actual instantiation of
	 * different operator types: - Unary operators (1 argument): TypeFieldExpression or UnaryOperator - Binary operators
	 * (2 arguments): BinaryVarOperator or BinaryOperator - N-ary operators (3+ arguments): NAryOperator
	 *
	 * Special handling is provided for variable/field operators and type casting scenarios.
	 *
	 * @param proto
	 *            the operator prototype containing signature and implementation details
	 * @param context
	 *            the compilation context for type resolution and validation
	 * @param currentEObject
	 *            the source EObject for error location reporting
	 * @param exprs
	 *            the argument expressions for the operator
	 * @return a new operator expression of the appropriate type, or null if validation fails
	 */
	IExpression createOperator(final IArtefactProto proto, final IDescription context, final EObject currentEObject,
			final IExpression... exprs);

	/**
	 * Creates a type expression that represents a GAML type as an expression. Type expressions are used in casting
	 * operations and type checking contexts. This method implements caching to reuse existing type expressions.
	 *
	 * @param type
	 *            the GAML type to create an expression for, must not be null
	 * @return a TypeExpression representing the given type, never null
	 */
	IExpression createTypeExpression(IType type);

	/**
	 * Creates a unit constant expression with the specified parameters. Unit expressions represent measurement units in
	 * GAML (e.g., meters, seconds).
	 *
	 * @param value
	 *            the numeric value of the unit
	 * @param t
	 *            the type of the unit expression
	 * @param name
	 *            the name identifier for the unit
	 * @param doc
	 *            documentation string for the unit
	 * @param deprecated
	 *            deprecation message if the unit is deprecated
	 * @param isTime
	 *            true if this unit represents a time measurement
	 * @param names
	 *            alternative names for the unit
	 * @return a new UnitConstantExpression, never null
	 */
	IExpression.Unit createUnit(Object value, IType t, String name, String doc, String deprecated, boolean isTime,
			String[] names);

	/**
	 * Creates an action expression that represents a call to a GAML action. The action can be called on agents and will
	 * execute with the provided arguments. Argument verification is performed before creating the expression.
	 *
	 * @param op
	 *            the operator name (typically the action name)
	 * @param callerContext
	 *            the context from which the action is being called
	 * @param action
	 *            the action description containing signature and implementation details
	 * @param call
	 *            the expression representing the target object (agent) on which to call the action
	 * @param arguments
	 *            the arguments to pass to the action
	 * @return a new PrimitiveOperator expression for the action call, or null if argument verification fails
	 */
	IExpression createAction(String op, IDescription callerContext, IActionDescription action, IExpression call,
			Arguments arguments);

	/**
	 * Creates a temporary action expression for dynamic action execution on an agent. This method compiles action code
	 * at runtime and creates a temporary action that can be executed immediately. The action is added to the agent's
	 * species temporarily.
	 *
	 * @param agent
	 *            the target agent on which the action will be available
	 * @param expression
	 *            the string containing the action code to compile
	 * @param tempContext
	 *            the execution context for compilation and variable resolution
	 * @return an expression that calls the compiled temporary action, or null if compilation fails
	 */
	IExpression createTemporaryActionForAgent(IAgent agent, String expression, IExecutionContext tempContext);

	/**
	 * Checks whether an exact operator exists for the given operator name and argument. The type must exactly
	 * correspond - no automatic type coercion is performed. This method is used for precise operator matching during
	 * compilation.
	 *
	 * @param op
	 *            the operator name to check (e.g., "+", "-", "and", "or")
	 * @param compiledArg
	 *            the compiled argument expression to match against
	 * @return true if an exact operator exists for the given signature, false otherwise
	 */
	boolean hasExactOperator(String op, IExpression compiledArg);

	/**
	 * Creates a type casting expression that converts the first expression to the type specified by the second
	 * expression. This is equivalent to the "as" operator in GAML.
	 *
	 * @param context
	 *            the compilation context for type resolution and error reporting
	 * @param toCast
	 *            the expression whose value will be cast to the target type
	 * @param createTypeExpression
	 *            the expression that evaluates to the target type for casting
	 * @return a new casting expression, or null if the casting operation cannot be created
	 */
	IExpression createAs(IDescription context, IExpression toCast, IExpression createTypeExpression);

	/**
	 * Creates a type casting expression using a direct type instead of a type expression. This is a convenience method
	 * that automatically creates a type expression from the provided type and then calls the main createAs method.
	 *
	 * @param context
	 *            the compilation context for type resolution and error reporting
	 * @param toCast
	 *            the expression whose value will be cast to the target type
	 * @param type
	 *            the target type for casting
	 * @return a new casting expression, or null if the casting operation cannot be created
	 */
	default IExpression createAs(final IDescription context, final IExpression toCast, final IType<?> type) {
		return createAs(context, toCast, createTypeExpression(type));
	}

	/**
	 * Checks for operator with the given signature, with potential type coercion. This method determines if an operator
	 * exists that can handle the provided signature, either through exact matching or through automatic type
	 * conversion.
	 *
	 * @param op
	 *            the operator name to check
	 * @param sig
	 *            the signature containing argument types
	 * @return true if a compatible operator exists, false otherwise
	 */
	boolean hasOperator(String op, Signature sig);

	/**
	 * Returns the shared constant expression for the null/nil value. This method provides access to a singleton
	 * expression to avoid creating multiple instances for the same constant.
	 *
	 * @return the constant expression representing null/nil, never null
	 */
	IExpression getNil();

	/**
	 * Returns the shared constant expression for the boolean value 'false'. This method provides access to a singleton
	 * expression to avoid creating multiple instances for the same constant.
	 *
	 * @return the constant expression representing 'false', never null
	 */
	IExpression getFalse();

	/**
	 * Returns the shared constant expression for the boolean value 'true'. This method provides access to a singleton
	 * expression to avoid creating multiple instances for the same constant.
	 *
	 * @return the constant expression representing 'true', never null
	 */
	IExpression getTrue();

	/**
	 * Creates an expression that denotes (refers to) a description object. This is typically used for creating
	 * references to actions, variables, or other named elements within the GAML model structure.
	 *
	 * @param desc
	 *            the description object to create an expression for
	 * @return a new DenotedActionExpression that references the given description
	 */
	IExpression getExpressionDenoting(final IDescription desc);

	/**
	 *
	 */
	void writeStats();

}