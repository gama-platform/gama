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
package gaml.compiler.expressions;

import java.util.concurrent.TimeUnit;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import gama.api.GAMA;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionCompiler;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Arguments;
import gama.api.runtime.scope.IExecutionContext;
import gaml.compiler.descriptions.StringBasedExpressionDescription;
import gaml.compiler.gaml.Expression;
import gaml.compiler.gaml.Facet;
import gaml.compiler.resource.GamlSyntheticResourcesServices;

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
 * This class is designed to be stateless. All mutable compilation state is maintained in
 * {@link ExpressionCompilationContext} instances that are created per compilation session and passed through method
 * calls. This design enables:
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
 * <li>Session-level caches in ExpressionCompilationContext for species, skills, and types</li>
 * <li>Efficient data structures for iterator context management</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * This class is fully thread-safe as a stateless singleton. Each compilation request creates its own
 * {@link ExpressionCompilationContext} which is not shared across threads.
 * </p>
 *
 * <h2>Usage Pattern:</h2>
 * <p>
 * Typically invoked through an IExpressionFactory (the default being GAML.getExpressionFactory()). A
 * ExpressionCompilationContext is created per compilation request and maintains compilation state including current
 * species, types manager, and validation context.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see IExpressionCompiler
 * @see IExpression
 * @see IExpressionFactory
 * @see ExpressionCompilationContext
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
		try (ExpressionCompilationContext ctx = new ExpressionCompilationContext(parsingContext)) {
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
		try (ExpressionCompilationContext ctx = new ExpressionCompilationContext(parsingContext)) {
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
	private IExpression compile(final EObject s, final ExpressionCompilationContext ctx) {
		// Create a switch instance with the context and delegate to it
		final ExpressionCompilationSwitch compilationSwitch = new ExpressionCompilationSwitch(ctx);
		return compilationSwitch.compile(s);
	}

	/**
	 * Parses and validates arguments for action calls - delegates to compilation switch.
	 */
	@Override
	public Arguments compileArguments(final IActionDescription action, final EObject o, final IDescription command,
			final boolean compileArgValues) {
		try (final ExpressionCompilationContext ctx = new ExpressionCompilationContext(command)) {
			return new ExpressionCompilationSwitch(ctx).parseArguments(action, o, command, compileArgValues);
		}
	}

	/**
	 * Compile field access.
	 *
	 * @param ownerExpr
	 *            the owner expr
	 * @param fieldExpr
	 *            the field expr
	 * @param parsingContext
	 *            the parsing context
	 * @return the i expression
	 */
	@Override
	public IExpression compileActionCall(final EObject owner, final EObject field, final IDescription parsingContext) {
		if (owner instanceof Expression ownerExpr && field instanceof Expression fieldExpr) {
			try (final ExpressionCompilationContext ctx = new ExpressionCompilationContext(parsingContext)) {
				return new ExpressionCompilationSwitch(ctx).compileFieldAccess(ownerExpr, fieldExpr, null);
			}
		}
		return null;
	}

	/**
	 * Compiles an action call where the receiver has already been compiled to an {@link IExpression} — i.e. when
	 * there is no backing {@link EObject} for the target (e.g. a synthesised {@code self} or {@code super}
	 * represented by a {@link gaml.compiler.expressions.SelfOrSuperExpressionDescription}).
	 *
	 * @param owner
	 *            the pre-compiled receiver expression; must not be {@code null}
	 * @param field
	 *            the {@link EObject} representing the field / action call (must be an {@link Expression})
	 * @param parsingContext
	 *            the description providing the compilation context
	 * @return the compiled {@link ActionCallOperator}, or {@code null} on failure
	 */
	public IExpression compileActionCall(final IExpression owner, final EObject field,
			final IDescription parsingContext) {
		if (field instanceof Expression fieldExpr) {
			try (final ExpressionCompilationContext ctx = new ExpressionCompilationContext(parsingContext)) {
				return new ExpressionCompilationSwitch(ctx).compileFieldAccess(owner, fieldExpr, null);
			}
		}
		return null;
	}

	/**
	 * Compiles an action call for the deprecated facet-based {@code do} form without creating any synthetic EMF nodes.
	 *
	 * <p>
	 * Unlike {@link #compileActionCall} which requires a {@code Function} EMF node (whose construction involves
	 * reparenting the facet value expressions into synthetic {@link gaml.compiler.gaml.Parameter} nodes, mutating the
	 * EMF AST), this method accepts the raw {@link Facet} list directly and never touches containment references.
	 * Each facet's value expression is compiled by reference only.
	 * </p>
	 *
	 * <p>
	 * The target is always the implicit {@code self} or {@code super} receiver resolved from
	 * {@code parsingContext}'s type context — this method is only intended for the deprecated implicit-self form.
	 * </p>
	 *
	 * @param nameExpr
	 *            the action-name {@link Expression} node from the live parse tree (used as error / hover anchor)
	 * @param facets
	 *            the raw facet list from the {@code S_Do} statement; must not be {@code null}
	 * @param parsingContext
	 *            the description providing the compilation context
	 * @return the compiled {@link ActionCallOperator}, or {@code null} on failure
	 */
	public IExpression compileActionCallFromFacets(final Expression nameExpr, final EList<Facet> facets,
			final IDescription parsingContext) {
		if (nameExpr == null) return null;
		try (final ExpressionCompilationContext ctx = new ExpressionCompilationContext(parsingContext)) {
			final ExpressionCompilationSwitch sw = new ExpressionCompilationSwitch(ctx);
			final boolean isSuper = parsingContext instanceof gaml.compiler.descriptions.DoDescription dd
					&& dd.isSuperInvocation();
			// Compile the implicit self/super target using the action-name node as the EMF anchor.
			final IExpression target = sw.caseVar(isSuper ? "super" : "self", nameExpr);
			if (target == null) return null;
			final gama.api.compilation.descriptions.ITypeDescription species =
					parsingContext.getTypeContext();
			if (species == null) return null;
			return sw.compileActionCallFromFacets(nameExpr, target, facets, species);
		}
	}
}
