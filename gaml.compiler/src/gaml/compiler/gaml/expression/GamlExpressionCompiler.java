/*******************************************************************************************************
 *
 * GamlExpressionCompiler.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import gama.api.GAMA;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionCompiler;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Arguments;
import gama.api.runtime.IExecutionContext;
import gaml.compiler.gaml.Expression;
import gaml.compiler.gaml.descriptions.StringBasedExpressionDescription;
import gaml.compiler.gaml.resource.GamlSyntheticResourcesServices;

/**
 * The GamlExpressionCompiler transforms GAML language constructs (strings or XText Expression objects) into executable
 * IExpression objects. This class serves as the core compiler for GAML expressions, handling parsing, type checking,
 * and compilation of various GAML language constructs including:
 *
 * <ul>
 * <li>Variable references and field access</li>
 * <li>Function calls and operator expressions</li>
 * <li>Type casting and validation</li>
 * <li>Iterator expressions with 'each' variable handling</li>
 * <li>Literal values (integers, floats, strings, booleans)</li>
 * <li>Complex expressions (arrays, points, conditionals)</li>
 * </ul>
 *
 * <h2>Stateless Architecture:</h2>
 * <p>
 * This class is designed to be stateless. All mutable compilation state is maintained in {@link CompilationContext}
 * instances that are created per compilation session and passed through method calls. This design enables:
 * </p>
 * <ul>
 * <li>Thread-safe operation without ThreadLocal or synchronization</li>
 * <li>Better isolation between compilation sessions</li>
 * <li>Easier testing and debugging</li>
 * <li>Single compiler instance shared across all threads</li>
 * </ul>
 *
 * <h2>Performance Characteristics:</h2>
 * <p>
 * This class implements several performance optimizations:
 * </p>
 * <ul>
 * <li>High-performance Guava cache with automated eviction for context-independent expressions</li>
 * <li>Size-based eviction (LRU) and time-based expiration to prevent memory leaks</li>
 * <li>Session-level caches in CompilationContext for species, skills, and types</li>
 * <li>Efficient data structures for iterator context management</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * This class is fully thread-safe as a stateless singleton. Each compilation request creates its own
 * {@link CompilationContext} which is not shared across threads.
 * </p>
 *
 * <h2>Usage Pattern:</h2>
 * <p>
 * Typically invoked through an IExpressionFactory (the default being GAML.getExpressionFactory()). A CompilationContext
 * is created per compilation request and maintains compilation state including current species, types manager, and
 * validation context.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see IExpressionCompiler
 * @see IExpression
 * @see IExpressionFactory
 * @see CompilationContext
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlExpressionCompiler implements IExpressionCompiler<Expression> {

	/** Maximum size for the expression cache to prevent unbounded memory growth */
	private static final int MAX_CACHE_SIZE = 1000;

	/** Maximum idle time for cached expressions (in minutes) */
	private static final int CACHE_EXPIRE_MINUTES = 30;

	/**
	 * High-performance Guava cache for constant synthetic expressions with automated eviction. Features: - Size-based
	 * eviction (LRU when cache exceeds MAX_CACHE_SIZE) - Time-based eviction (entries expire after CACHE_EXPIRE_MINUTES
	 * of inactivity) - Thread-safe concurrent access - Automatic cache statistics and monitoring
	 */
	private static final Cache<String, IExpression> CONSTANT_SYNTHETIC_EXPRESSIONS = CacheBuilder.newBuilder()
			.maximumSize(MAX_CACHE_SIZE).expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES).build();

	/** Singleton instance of the stateless compiler */
	private static final GamlExpressionCompiler INSTANCE = new GamlExpressionCompiler();

	/**
	 * Private constructor to enforce singleton pattern.
	 */
	private GamlExpressionCompiler() {}

	/**
	 * Gets the singleton instance of the compiler.
	 *
	 * @return the singleton compiler instance
	 */
	public static GamlExpressionCompiler getInstance() { return INSTANCE; }

	@Override
	public IExpression compile(final IExpressionDescription s, final IDescription parsingContext) {
		try (CompilationContext ctx = new CompilationContext(parsingContext)) {
			// Cf. Issue 782. Returns the expression if an expression needs its
			// compiled version to be compiled.
			// if (s == ctx.getCurrentExpressionDescription()) return s.getExpression();
			// If it is a literal const (integer, float, bool...)
			EObject expression = s.getTarget();
			if (s.isConst()) {
				ctx.getDocumentationContext().document(expression, ctx.getContext());
				return s.getExpression();
			}
			// ctx.setCurrentExpressionDescription(s);
			// It is an expression entered by the user at runtime (in a monitor, for instance)
			if (expression == null && s instanceof StringBasedExpressionDescription) {
				final IExecutionContext context =
						GAMA.getExperiment() == null ? null : GAMA.getRuntimeScope().getExecutionContext();
				return compile(s.toString(), ctx.getContext(), context);
			}
			return compile(expression, ctx);
		}
	}

	/**
	 * Compiles a string expression within the given compilation context.
	 *
	 * @param expression
	 *            the string expression to compile
	 * @param ctx
	 *            the compilation context
	 * @param tempContext
	 *            the execution context for error handling
	 * @return the compiled expression, or null if compilation fails
	 */
	@Override
	public IExpression compile(final String expression, final IDescription parsingContext,
			final IExecutionContext tempContext) {
		// Check cache first for performance
		IExpression result = CONSTANT_SYNTHETIC_EXPRESSIONS.getIfPresent(expression);
		if (result != null) return result;
		final EObject o = GamlSyntheticResourcesServices.getEObjectOf(expression, tempContext, parsingContext);
		try (CompilationContext ctx = new CompilationContext(parsingContext)) {
			result = compile(o, ctx);
		}
		// Cache context-independent expressions - Guava handles eviction automatically
		if (result != null && result.isContextIndependant()) { CONSTANT_SYNTHETIC_EXPRESSIONS.put(expression, result); }
		return result;
	}

	/**
	 * Compiles an EObject into an IExpression using the compilation switch.
	 *
	 * @param s
	 *            the EObject to compile (can be null for error handling)
	 * @param ctx
	 *            the compilation context
	 * @return the compiled expression, or null if compilation fails
	 */
	private IExpression compile(final EObject s, final CompilationContext ctx) {
		// Create a switch instance with the context and delegate to it
		final GamlExpressionCompilationSwitch compilationSwitch = new GamlExpressionCompilationSwitch(ctx);
		return compilationSwitch.compile(s);
	}

	/**
	 * Parses and validates arguments for action calls - delegates to compilation switch.
	 */
	@Override
	public Arguments compileArguments(final IActionDescription action, final EObject o, final IDescription command,
			final boolean compileArgValues) {
		try (final CompilationContext ctx = new CompilationContext(command)) {
			return new GamlExpressionCompilationSwitch(ctx).parseArguments(action, o, command, compileArgValues);
		}
	}

}
