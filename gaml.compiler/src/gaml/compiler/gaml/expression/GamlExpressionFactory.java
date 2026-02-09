
/*******************************************************************************************************
 *
 * GamlExpressionFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import static com.google.common.collect.Iterables.any;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionCompiler;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.IExecutionContext;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.InScope;
import gama.dev.DEBUG;
import gaml.compiler.gaml.descriptions.ConstantExpressionDescription;
import gaml.compiler.gaml.prototypes.OperatorProto;

/**
 * The static class ExpressionFactory.
 *
 * @author drogoul
 */

/**
 * A factory for creating GamlExpression objects with optimized performance, memory management, and comprehensive
 * documentation.
 *
 * This factory implements various optimizations including: - Guava Cache-based operator signature caching with
 * automatic eviction - Memory-efficient object creation patterns with time-based expiration - Thread-safe singleton
 * implementation - Comprehensive cache statistics for monitoring performance
 *
 * Cache Configuration: - Operator Cache: 1000 entries, 30 minutes access expiration - Exact Operator Cache: 10000
 * entries, 1 hour access expiration - Signature Match Cache: 10000 entries, 1 hour access expiration
 *
 * @author drogoul
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 28 déc. 2023
 * @version 3.0 - Enhanced with Guava Cache for improved performance and automatic memory management
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlExpressionFactory implements IExpressionFactory {

	/** The singleton instance. */
	private static final AtomicReference<GamlExpressionFactory> INSTANCE_REF = new AtomicReference<>();

	/** Cache for operator signature matching to improve performance. */
	private static final Cache<String, Cache<Signature, IArtefactProto.Operator>> operatorCache =
			CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30, TimeUnit.MINUTES).recordStats().build();

	/** Cache for exact operator matching results. */
	private static final Cache<String, Boolean> exactOperatorCache =
			CacheBuilder.newBuilder().maximumSize(10000).expireAfterAccess(1, TimeUnit.HOURS).recordStats().build();

	/** Cache for signature matching results. */
	private static final Cache<String, Boolean> signatureMatchCache =
			CacheBuilder.newBuilder().maximumSize(10000).expireAfterAccess(1, TimeUnit.HOURS).recordStats().build();

	/**
	 * Gets the single instance of GamlExpressionFactory using double-checked locking for thread-safe lazy
	 * initialization.
	 *
	 * @return single instance of GamlExpressionFactory, never null
	 */
	public static GamlExpressionFactory getInstance() {
		GamlExpressionFactory instance = INSTANCE_REF.get();
		if (instance == null) {
			instance = new GamlExpressionFactory();
			if (!INSTANCE_REF.compareAndSet(null, instance)) {
				// Another thread created the instance
				instance = INSTANCE_REF.get();
			}
		}
		return instance;
	}

	/**
	 * Instantiates a new GamlExpressionFactory with proper initialization.
	 */
	private GamlExpressionFactory() {
		// Initialize caches with reasonable initial capacities
		initializeCaches();
	}

	/**
	 * Initializes internal caches for performance optimization.
	 */
	private void initializeCaches() {
		// The caches are already initialized as static fields
		// This method is reserved for future initialization logic
	}

	/**
	 * Clears all internal caches to free memory. This should be called when the factory is no longer needed or during
	 * system cleanup.
	 */
	public void clearCaches() {
		operatorCache.invalidateAll();
		exactOperatorCache.invalidateAll();
		signatureMatchCache.invalidateAll();
	}

	/**
	 * Returns the current cache statistics for monitoring purposes.
	 *
	 * @return a map containing cache names and their sizes and statistics
	 */
	public Map<String, Object> getCacheStatistics() {
		Map<String, Object> stats = new java.util.concurrent.ConcurrentHashMap<>();

		CacheStats operatorStats = operatorCache.stats();
		stats.put("operatorCache_size", operatorCache.size());
		stats.put("operatorCache_hitRate", operatorStats.hitRate());
		stats.put("operatorCache_missRate", operatorStats.missRate());
		stats.put("operatorCache_evictionCount", operatorStats.evictionCount());

		CacheStats exactOperatorStats = exactOperatorCache.stats();
		stats.put("exactOperatorCache_size", exactOperatorCache.size());
		stats.put("exactOperatorCache_hitRate", exactOperatorStats.hitRate());
		stats.put("exactOperatorCache_missRate", exactOperatorStats.missRate());
		stats.put("exactOperatorCache_evictionCount", exactOperatorStats.evictionCount());

		CacheStats signatureMatchStats = signatureMatchCache.stats();
		stats.put("signatureMatchCache_size", signatureMatchCache.size());
		stats.put("signatureMatchCache_hitRate", signatureMatchStats.hitRate());
		stats.put("signatureMatchCache_missRate", signatureMatchStats.missRate());
		stats.put("signatureMatchCache_evictionCount", signatureMatchStats.evictionCount());

		return stats;
	}

	/**
	 * Returns a simplified view of cache performance metrics for quick monitoring.
	 *
	 * @return formatted string containing cache hit rates and sizes
	 */
	public String getCachePerformanceSummary() {
		CacheStats operatorStats = operatorCache.stats();
		CacheStats exactOperatorStats = exactOperatorCache.stats();
		CacheStats signatureMatchStats = signatureMatchCache.stats();

		return String.format("""
				Cache Performance Summary:
				- Operator Cache: %d entries, %.2f%% hit rate
				- Exact Operator Cache: %d entries, %.2f%% hit rate
				- Signature Match Cache: %d entries, %.2f%% hit rate""", operatorCache.size(),
				operatorStats.hitRate() * 100, exactOperatorCache.size(), exactOperatorStats.hitRate() * 100,
				signatureMatchCache.size(), signatureMatchStats.hitRate() * 100);
	}

	static {
		DEBUG.OFF();
	}

	/**
	 * Functional interface for providing expression compiler instances. This interface allows dependency injection of
	 * parser implementations while maintaining thread-local storage for parser instances.
	 */
	public interface ParserProvider {

		/**
		 * Returns an expression compiler instance. Implementations should return a new or appropriately configured
		 * compiler instance for the current context.
		 *
		 * @return a new IExpressionCompiler instance, never null
		 */
		IExpressionCompiler get();
	}

	/** Thread-local storage for expression compiler instances to ensure thread safety. */
	static ThreadLocal<IExpressionCompiler> parser;

	/**
	 * Registers a parser provider for creating expression compiler instances. This method sets up thread-local storage
	 * to ensure each thread gets its own parser instance, which is crucial for thread safety in multi-threaded
	 * environments.
	 *
	 * @param f
	 *            the parser provider that will supply compiler instances, must not be null
	 */
	public static void registerParserProvider(final ParserProvider f) {
		parser = new ThreadLocal() {
			@Override
			protected IExpressionCompiler initialValue() {
				return f.get();
			}
		};
	}

	/**
	 * Retrieves the thread-local expression compiler instance. Each thread maintains its own parser instance to ensure
	 * thread safety during concurrent compilation operations.
	 *
	 * @return the expression compiler for the current thread, never null
	 * @throws IllegalStateException
	 *             if no parser provider has been registered
	 */
	// @Override
	private IExpressionCompiler getParser() { return parser.get(); }

	/**
	 * Resets and cleans up the thread-local parser instance. This method properly disposes of parser resources and
	 * removes the thread-local reference to prevent memory leaks. Should be called when the current thread is done with
	 * expression compilation.
	 */
	@Override
	public void resetParser() {
		if (parser != null && parser.get() != null) {
			parser.get().dispose();
			parser.remove();
		}
	}

	/**
	 * Cleans up all resources associated with this factory, including parser resources and caches. Should be called
	 * during application shutdown to prevent memory leaks.
	 */
	public void cleanup() {
		resetParser();
		clearCaches();
	}

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
	 *
	 * @see gama.api.compilation.factories.IExpressionFactory#createUnit(java.lang.Object, gama.api.gaml.types.IType,
	 *      java.lang.String)
	 */
	@Override
	public UnitConstantExpression createUnit(final Object value, final IType t, final String name, final String doc,
			final String deprecated, final boolean isTime, final String[] names) {
		final UnitConstantExpression exp = UnitConstantExpression.create(value, t, name, doc, isTime, names);
		if (deprecated != null && !deprecated.isEmpty()) { exp.setDeprecated(deprecated); }
		return exp;

	}

	/**
	 * Creates a constant expression with the specified value and type. This is a convenience method that calls
	 * createConst(val, type, null).
	 *
	 * @param val
	 *            the constant value, may be null
	 * @param type
	 *            the type of the constant, must not be null
	 * @return a new constant expression, never null
	 */
	@Override
	public IExpression createConst(final Object val, final IType type) {
		return createConst(val, type, null);
	}

	/**
	 * Creates a species constant expression for the given type. Species constants represent references to agent species
	 * in GAML.
	 *
	 * @param type
	 *            the species type, must have SPECIES as its GAML type
	 * @return a new SpeciesConstantExpression or null if type is invalid
	 */
	@Override
	public SpeciesConstantExpression createSpeciesConstant(final IType type) {
		if (type.getGamlType() != Types.SPECIES) return null;
		final ISpeciesDescription sd = type.getContentType().getSpecies();
		if (sd == null) return null;
		return new SpeciesConstantExpression(sd.getName(), type, sd);
	}

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
	 */
	@Override
	public IExpression createConst(final Object val, final IType type, final String name) {
		if (type.getGamlType() == Types.SPECIES) return createSpeciesConstant(type);
		if (type == Types.SKILL) return new SkillConstantExpression((String) val, type);
		if (val == null) return getNil();
		if (val instanceof Boolean) return (Boolean) val ? getTrue() : getFalse();
		return new ConstantExpression(val, type, name);
	}

	/**
	 * Creates an expression from an expression description within a given context. This method delegates to the parser
	 * to compile the expression description into an executable expression object.
	 *
	 * @param ied
	 *            the expression description to compile, must not be null
	 * @param context
	 *            the compilation context providing variable scope and type information
	 * @return the compiled expression, or null if compilation fails
	 */
	@Override
	public IExpression createExpr(final IExpressionDescription ied, final IDescription context) {
		return getParser().compile(ied, context);
	}

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
	@Override
	public IExpression createExpr(final String s, final IDescription context) {
		if (s == null || s.isEmpty()) return null;
		return getParser().compile(GAML.getExpressionDescriptionFactory().createStringBased(s), context);
	}

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
	@Override
	public IExpression createExpr(final String s, final IDescription context,
			final IExecutionContext additionalContext) {
		if (s == null || s.isEmpty()) return null;
		return getParser().compile(s, context, additionalContext);
	}

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
	@Override
	public Arguments createArgumentMap(final IActionDescription action, final IExpressionDescription args,
			final IDescription context) {
		if (args == null) return null;
		return getParser().parseArguments(action, args.getTarget(), context, false);
	}

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
	@Override
	public IExpression createVar(final String name, final IType type, final boolean isConst, final int scope,
			final IDescription definitionDescription) {
		return switch (scope) {
			case IVarExpression.GLOBAL -> GlobalVariableExpression.create(name, type, isConst,
					definitionDescription.getModelDescription());
			case IVarExpression.AGENT -> new AgentVariableExpression(name, type, isConst, definitionDescription);
			case IVarExpression.TEMP -> new TempVariableExpression(name, type, definitionDescription);
			case IVarExpression.EACH -> new EachExpression(name, type);
			case IVarExpression.SELF -> new SelfExpression(type);
			case IVarExpression.SUPER -> new SuperExpression(type);
			case IVarExpression.MYSELF -> new MyselfExpression(type, definitionDescription);
			default -> null;
		};
	}

	/**
	 * Creates a list expression from an iterable collection of expressions. The resulting expression will evaluate to a
	 * GAML list containing the values of all provided expressions when executed.
	 *
	 * @param elements
	 *            the collection of expressions to include in the list, may be null or empty
	 * @return a new ListExpression containing all provided expressions
	 */
	@Override
	public IExpression createList(final Iterable<? extends IExpression> elements) {
		return ListExpression.create(elements);
	}

	/**
	 * Creates a list expression from an array of expressions. This is a convenience method for creating lists from
	 * expression arrays. The resulting expression will evaluate to a GAML list containing the values of all provided
	 * expressions when executed.
	 *
	 * @param elements
	 *            the array of expressions to include in the list, may be null or empty
	 * @return a new ListExpression containing all provided expressions
	 */
	public IExpression createList(final IExpression[] elements) {
		return ListExpression.create(elements);
	}

	/**
	 * Creates a map expression from an iterable collection of expressions. The elements should contain key-value pairs
	 * that will be used to construct a GAML map when the expression is evaluated.
	 *
	 * @param elements
	 *            the collection of expressions representing key-value pairs, may be null or empty
	 * @return a new MapExpression that will evaluate to a GAML map
	 */
	@Override
	public IExpression createMap(final Iterable<? extends IExpression> elements) {
		return MapExpression.create(elements);
	}

	@Override
	public boolean hasExactOperator(final String op, final IExpression arg) {
		// If arguments are invalid, we have no match
		if (arg == null || op == null || op.isEmpty()) return false;

		// Create cache key for this operation
		String cacheKey = op + "#" + (arg.getGamlType() != null ? arg.getGamlType().toString() : "null");

		// Check cache first
		Boolean cachedResult = exactOperatorCache.getIfPresent(cacheKey);
		if (cachedResult != null) return cachedResult;

		// If the operator is not known, we have no match
		Map<Signature, IArtefactProto.Operator> variants = GAML.OPERATORS.get(op);
		if (variants == null) {
			exactOperatorCache.put(cacheKey, false);
			return false;
		}

		boolean result = variants.containsKey(new Signature(arg).simplified());

		// Cache the result (Guava Cache handles size limits automatically)
		exactOperatorCache.put(cacheKey, result);

		return result;
	}

	/**
	 * Checks for operator with the given signature, using caching for performance.
	 *
	 * @param op
	 *            the operator name
	 * @param s
	 *            the signature to match
	 * @return true if an operator exists that can handle the signature, false otherwise
	 */
	@Override
	public boolean hasOperator(final String op, final Signature s) {
		// If arguments are invalid, we have no match
		if (s == null || s.size() == 0 || op == null || op.isEmpty() || !GAML.OPERATORS.containsKey(op)) return false;

		// Create cache key for this operation
		String cacheKey = op + "#" + s.simplified().toString();

		// Check cache first
		Boolean cachedResult = signatureMatchCache.getIfPresent(cacheKey);
		if (cachedResult != null) return cachedResult;

		final Map<Signature, IArtefactProto.Operator> ops = GAML.OPERATORS.get(op);
		Signature sig = s.simplified();

		// Does any known operator signature match with the signature of the expressions?
		boolean matches = any(ops.keySet(), si -> sig.matchesDesiredSignature(si));
		if (!matches) {
			// Check if a varArg is not a possibility
			matches = any(ops.keySet(), si -> Signature.varArgFrom(sig).matchesDesiredSignature(si));
		}

		// Cache the result
		signatureMatchCache.put(cacheKey, matches);

		return matches;
	}

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
	 * @param eObject
	 *            the source EObject for error location reporting
	 * @param args
	 *            the array of argument expressions, must not be null or contain null elements
	 * @return a new operator expression, or null if no suitable operator found or error occurred
	 */
	@Override
	public IExpression createOperator(final String op, final IDescription context, final EObject eObject,
			final IExpression... args) {
		if (args == null || args.length == 0 || !GAML.OPERATORS.containsKey(op))
			return emitError(op, context, eObject, args == null ? new IExpression[0] : args);
		for (final IExpression exp : args) { if (exp == null) return emitError(op, context, eObject, args); }
		// if (!hasOperator(op, userSignature)) return emitError(op, context, eObject, args);
		// We get the possible sets of types registered in OPERATORS
		final Map<Signature, IArtefactProto.Operator> ops = GAML.OPERATORS.get(op);
		// We create the signature corresponding to the arguments
		// 19/02/14 Only the simplified signature is used now
		Signature userSignature = Signature.createSimplified(args);
		// If the signature is not present in the registry
		if (!ops.containsKey(userSignature)) {
			final Signature originalUserSignature = userSignature;
			int distance = Integer.MAX_VALUE;
			// We browse all the entries of the operators with this name
			for (Map.Entry<Signature, IArtefactProto.Operator> entry : ops.entrySet()) {
				Signature formalParametersSignature = entry.getKey();

				if (originalUserSignature.matchesDesiredSignature(formalParametersSignature)) {
					final int dist = Signature.distanceBetween(formalParametersSignature, originalUserSignature);
					if (dist == 0) {
						distance = 0;
						userSignature = formalParametersSignature;
						break;
					}
					if (dist < distance) {
						distance = dist;
						userSignature = formalParametersSignature;
					}
				}
			}

			if (distance == Integer.MAX_VALUE) { // Not found - try varArg
				Signature varArg = Signature.varArgFrom(originalUserSignature);
				for (Map.Entry<Signature, IArtefactProto.Operator> entry : ops.entrySet()) {
					Signature s = entry.getKey();
					if (varArg.matchesDesiredSignature(s))
						return createOperator(op, context, eObject, createList(args));
				}
				return emitError(op, context, eObject, args);
			}

			// We coerce the types if necessary, by wrapping the original
			// expressions in a casting expression

			for (int i = 0; i < args.length; i++) {
				IType originalType = originalUserSignature.get(i);
				IType newType = userSignature.get(i);
				IType coercingType = findCoercingType(context, eObject, originalType, newType, args[i]);
				if (coercingType != null) { args[i] = createAs(context, args[i], createTypeExpression(coercingType)); }
			}
		}

		final IArtefactProto proto = ops.get(userSignature);
		// We finally make an INSTANCE of the operator and init it with the arguments
		final IExpression operator = createOperator(proto, context, eObject, args);
		if (operator != null) {
			// We verify that it is not deprecated
			final String ged = proto.getDeprecated();
			if (ged != null) {
				context.warning(proto.getName() + " is deprecated: " + ged, IGamlIssue.DEPRECATED, eObject);
			}
		}
		return operator;
	}

	/**
	 * Determines the appropriate coercing type for automatic type conversion between original and target types. This
	 * method handles common type conversions like int to float and float to int (with truncation warning).
	 *
	 * @param context
	 *            the compilation context for issuing warnings
	 * @param eObject
	 *            the source EObject for warning location reporting
	 * @param originalType
	 *            the original type of the argument expression
	 * @param newType
	 *            the required type according to the operator signature
	 * @param argument
	 *            the argument expression being converted
	 * @return the coercing type to use, or null if no coercion is needed/possible
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 9 janv. 2024
	 */
	private IType findCoercingType(final IDescription context, final EObject eObject, final IType originalType,
			final IType newType, final IExpression argument) {
		if (originalType == Types.INT && newType == Types.FLOAT) return Types.FLOAT;
		if (originalType == Types.FLOAT && newType == Types.INT) {
			// Emits an info when a float is truncated. See Issue 735.
			context.info("'" + argument.serializeToGaml(false) + "' will be  truncated to int.",
					IGamlIssue.UNMATCHED_OPERANDS, eObject);
			return Types.INT;
		}
		return null;
	}

	/**
	 * Emits a compilation error when no suitable operator can be found for the given operator name and argument
	 * signature. The error message includes the operator name, the signature that was attempted, and lists available
	 * operator signatures if any exist.
	 *
	 * @param op
	 *            the operator name that couldn't be matched
	 * @param context
	 *            the compilation context for error reporting
	 * @param eObject
	 *            the source EObject for error location reporting
	 * @param args
	 *            the argument expressions that couldn't be matched
	 * @return always returns null to indicate compilation failure
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 14 nov. 2023
	 */
	private IExpression emitError(final String op, final IDescription context, final EObject eObject,
			final IExpression... args) {
		final Map<Signature, IArtefactProto.Operator> ops = GAML.OPERATORS.get(op);
		final Signature userSignature = new Signature(args).simplified();
		StringBuilder msg =
				new StringBuilder("No operator found for applying '").append(op).append("' to ").append(userSignature);
		if (ops != null) {
			msg.append(" (operators available for ").append(Arrays.toString(ops.keySet().toArray())).append(")");
		}
		context.error(msg.toString(), IGamlIssue.UNMATCHED_OPERANDS, eObject);
		return null;
	}

	/**
	 * Creates a type casting expression that converts the first expression to the type specified by the second
	 * expression. This is equivalent to the "as" operator in GAML.
	 *
	 * @param context
	 *            the compilation context for type resolution and error reporting
	 * @param toCast
	 *            the expression whose value will be cast to the target type
	 * @param type
	 *            the expression that evaluates to the target type for casting
	 * @return a new casting expression, or null if the casting operation cannot be created
	 */
	@Override
	public IExpression createAs(final IDescription context, final IExpression toCast, final IExpression type) {
		return createOperator(OperatorProto.AS, context, null, toCast, type);
	}

	/**
	 * Creates an operator expression from a prototype and expressions. This method handles the actual instantiation of
	 * different operator types: - Unary operators (1 argument): TypeFieldExpression or UnaryOperator - Binary operators
	 * (2 arguments): BinaryVarOperator or BinaryOperator - N-ary operators (3+ arguments): NAryOperator
	 *
	 * Special handling is provided for variable/field operators and type casting scenarios.
	 *
	 * @param artefact
	 *            the operator prototype containing signature and implementation details
	 * @param context
	 *            the compilation context for type resolution and validation
	 * @param currentEObject
	 *            the source EObject for error location reporting
	 * @param exprs
	 *            the argument expressions for the operator
	 * @return a new operator expression of the appropriate type, or null if validation fails
	 */
	@Override
	public IExpression createOperator(final IArtefactProto artefact, final IDescription context,
			final EObject currentEObject, final IExpression... exprs) {
		if (artefact instanceof OperatorProto proto && proto.getValidator().validate(context, currentEObject, exprs)) {
			switch (proto.getSignature().size()) {
				case 1:
					if (proto.isVarOrField()) return new TypeFieldExpression(proto, context, exprs[0]);
					return UnaryOperator.create(proto, context, exprs[0]);
				case 2:
					if (proto.isVarOrField()) {
						if (exprs[1] instanceof BinaryOperator bo && IKeyword.AS.equals(bo.getName())) {
							// Case of experiment.simulation and experiment.simulations (see #3621)
							TypeExpression typeExpr = (TypeExpression) bo.arg(1);
							IVarExpression var = (IVarExpression) bo.arg(0);
							return BinaryOperator.create(OperatorProto.AS, context,
									new BinaryOperator.BinaryVarOperator(proto, context, exprs[0], var), typeExpr);

						}
						return new BinaryOperator.BinaryVarOperator(proto, context, exprs[0],
								(IVarExpression) exprs[1]);
					}
					return BinaryOperator.create(proto, context, exprs);
				default:
					return NAryOperator.create(proto, context, exprs);
			}
		}
		return null;
	}

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
	@Override
	public IExpression createAction(final String op, final IDescription callerContext, final IActionDescription action,
			final IExpression call, final Arguments arguments) {
		if (action.verifyArgs(callerContext, arguments))
			return new PrimitiveOperator(callerContext, action, call, arguments, call instanceof SuperExpression);
		return null;
	}

	/**
	 * Creates a type expression that represents a GAML type as an expression. Type expressions are used in casting
	 * operations and type checking contexts. This method implements caching to reuse existing type expressions.
	 *
	 * @param type
	 *            the GAML type to create an expression for
	 * @return a TypeExpression representing the given type, never null
	 *
	 * @see gama.api.compilation.factories.IExpressionFactory#createCastingExpression(gama.api.gaml.types.IType)
	 */
	@Override
	public IExpression createTypeExpression(final IType type) {
		IExpression exp = type.getExpression();
		if (exp == null) { exp = new TypeExpression(type); }
		type.setExpression(exp);
		return exp;
	}

	/**
	 * Creates a temporary action expression for dynamic action execution on an agent. This method compiles action code
	 * at runtime and creates a temporary action that can be executed immediately. The action is added to the agent's
	 * species temporarily.
	 *
	 * @param agent
	 *            the target agent on which the action will be available
	 * @param action
	 *            the string containing the action code to compile
	 * @param tempContext
	 *            the execution context for compilation and variable resolution
	 * @return an expression that calls the compiled temporary action, or null if compilation fails
	 */
	@Override
	public IExpression createTemporaryActionForAgent(final IAgent agent, final String action,
			final IExecutionContext tempContext) {
		final ISpeciesDescription context = agent.getSpecies().getDescription();
		final IActionDescription desc = (IActionDescription) GAML.getDescriptionFactory().create(IKeyword.ACTION,
				context, Collections.EMPTY_LIST, IKeyword.TYPE, IKeyword.UNKNOWN, IKeyword.NAME, TEMPORARY_ACTION_NAME);
		final List<IDescription> children = getParser().compileBlock(action, context, tempContext);
		for (final IDescription child : children) { desc.addChild(child); }
		desc.validate();
		context.addChild(desc);
		final IStatement.Action a = (IStatement.Action) desc.compile();
		agent.getSpecies().addTemporaryAction(a);
		return getParser().compile(TEMPORARY_ACTION_NAME + "()", context, null);
	}

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
	@Override
	public <T> IExpression createExpr(final InScope<T> exp, final IType type) {
		return new IExpression() {

			@Override
			public IType<?> getGamlType() { return type; }

			@Override
			public T value(final IScope scope) throws GamaRuntimeException {
				return exp.run(scope);
			}

		};
	}

	/**
	 * Returns the shared constant expression for the boolean value 'true'. This method provides access to a singleton
	 * expression to avoid creating multiple instances for the same constant.
	 *
	 * @return the constant expression representing 'true', never null
	 */
	@Override
	public ConstantExpressionDescription getTrue() {
		return (ConstantExpressionDescription) GAML.getExpressionDescriptionFactory().createConstant(true);
	}

	/**
	 * Returns the shared constant expression for the boolean value 'false'. This method provides access to a singleton
	 * expression to avoid creating multiple instances for the same constant.
	 *
	 * @return the constant expression representing 'false', never null
	 */
	@Override
	public ConstantExpressionDescription getFalse() {
		return (ConstantExpressionDescription) GAML.getExpressionDescriptionFactory().createConstant(false);
	}

	/**
	 * Returns the shared constant expression for the null/nil value. This method provides access to a singleton
	 * expression to avoid creating multiple instances for the same constant.
	 *
	 * @return the constant expression representing null/nil, never null
	 */
	@Override
	public ConstantExpressionDescription getNil() {
		return (ConstantExpressionDescription) GAML.getExpressionDescriptionFactory().createConstant((Object) null);
	}

	/**
	 * Creates an expression that denotes (refers to) a description object. This is typically used for creating
	 * references to actions, variables, or other named elements within the GAML model structure.
	 *
	 * @param desc
	 *            the description object to create an expression for
	 * @return a new DenotedActionExpression that references the given description
	 */
	@Override
	public IExpression getExpressionDenoting(final IDescription desc) {
		return new DenotedActionExpression(desc);
	}

	/**
	 * Creates a constant expression from a value with automatic type inference. This is a convenience method that
	 * creates a ConstantExpression without explicitly specifying the type - the type will be inferred from the value.
	 *
	 * @param val
	 *            the constant value to wrap in an expression
	 * @return a new ConstantExpression containing the given value
	 */
	@Override
	public IExpression createConst(final Object val) {
		return new ConstantExpression(val);
	}

}