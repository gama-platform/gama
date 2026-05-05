/*******************************************************************************************************
 *
 * ExpressionCompilationContext.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IVarDescriptionProvider;
import gama.api.compilation.validation.IDocumentationContext;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gaml.compiler.validation.DocumentationContext;

/**
 * ExpressionCompilationContext encapsulates all mutable state required during expression compilation. This class is
 * designed to be created per compilation session and passed through the compilation process, enabling the
 * GamlExpressionCompiler to be stateless.
 *
 * <h2>Purpose:</h2>
 * <p>
 * By extracting all mutable state from GamlExpressionCompiler into this context object, we achieve:
 * </p>
 * <ul>
 * <li>Stateless compiler that can be safely shared across threads</li>
 * <li>Better isolation of compilation sessions</li>
 * <li>Easier testing and debugging</li>
 * <li>Clearer separation of compilation state vs. compiler logic</li>
 * </ul>
 *
 * <h2>Lifecycle:</h2>
 * <p>
 * A new ExpressionCompilationContext is created for each top-level compilation request and is discarded after the
 * compilation completes. The context is not meant to be reused across multiple independent compilations.
 * </p>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * This class is NOT thread-safe. Each thread should have its own ExpressionCompilationContext instance. However, since
 * contexts are short-lived (one per compilation), this is not a concern in practice.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 2.0
 */
public final class ExpressionCompilationContext implements Closeable {

	/**
	 * The iterator contexts stack for managing 'each' variables in iterator operations. Using ArrayDeque for better
	 * performance than LinkedList.
	 */
	private final Deque<IVarExpression> iteratorContexts = new ArrayDeque<>();

	/**
	 * The current types manager for type resolution in the current compilation context.
	 */
	private final ITypesManager currentTypesManager;

	/**
	 * The current parsing context (IDescription) in which the compiler operates. If none is given, the global context
	 * of the current simulation is used.
	 */
	private final IDescription currentContext;

	/**
	 * An optional secondary variable provider consulted when the description hierarchy cannot resolve a variable name.
	 * Used by the interactive console to expose persistent REPL variables to the expression compiler.
	 */
	private final IVarDescriptionProvider tempVarsProvider;

	/** The documentation context. */
	private final IDocumentationContext documentationContext;

	/**
	 * Creates a new compilation context with the specified initial description context. If the description hierarchy
	 * contains a {@link gama.api.compilation.descriptions.ITypeDescription} with an alternate variable-description
	 * provider attached (e.g. by the interactive console), that provider is automatically used as the
	 * {@code tempVarsProvider}. This ensures that REPL variables are visible during the validation of statement
	 * descriptions even when no explicit {@code tempVarsProvider} is passed (e.g. from
	 * {@link gama.api.gaml.expressions.IExpressionDescription#compile(IDescription)}).
	 *
	 * @param context
	 *            the initial description context for compilation
	 */
	public ExpressionCompilationContext(final IDescription context) {
		this(context, findAlternateVarProvider(context));
	}

	/**
	 * Walks up the description hierarchy starting from {@code context} and returns the first alternate variable
	 * description provider attached to a {@link gama.api.compilation.descriptions.ITypeDescription}, or {@code null}
	 * if none is found. This is used by the no-{@code tempVarsProvider} constructor so that REPL variables are always
	 * visible during expression compilation triggered by description validation.
	 *
	 * @param context
	 *            the description to start from; may be {@code null}
	 * @return the first alternate {@link IVarDescriptionProvider} found in the hierarchy, or {@code null}
	 */
	static IVarDescriptionProvider findAlternateVarProvider(final IDescription context) {
		IDescription current = context;
		while (current != null) {
			final IVarDescriptionProvider avp = current.getAlternateVarProvider();
			if (avp != null) return avp;
			current = current.getEnclosingDescription();
		}
		return null;
	}

	/**
	 * Creates a new compilation context with the specified description context and an optional secondary variable
	 * provider. The secondary provider is consulted when the description hierarchy cannot resolve a variable name (e.g.,
	 * in the interactive console where persistent REPL variables live outside the species description).
	 *
	 * @param context
	 *            the initial description context for compilation
	 * @param tempVarsProvider
	 *            an optional {@link IVarDescriptionProvider} used as a fallback for variable resolution, or
	 *            {@code null}
	 */
	public ExpressionCompilationContext(final IDescription context, final IVarDescriptionProvider tempVarsProvider) {
		this.currentContext = context;
		this.currentTypesManager = Types.findTypesManager(context);
		this.documentationContext = findDocumentationContext(context);
		this.tempVarsProvider = tempVarsProvider;
	}

	/**
	 * Find documentation context.
	 *
	 * @param context
	 *            the context
	 * @return the i documentation context
	 */
	public static IDocumentationContext findDocumentationContext(final IDescription context) {
		if (context == null) return DocumentationContext.NULL;
		final IDocumentationContext dc = context.getDocumentationContext();
		return dc != null ? dc : DocumentationContext.NULL;
	}

	/**
	 * Gets the current description context.
	 *
	 * @return the current description context
	 */
	public IDescription getContext() { return currentContext; }

	/**
	 * Returns the optional secondary variable provider used as a fallback when the description hierarchy cannot resolve
	 * a variable name.
	 *
	 * @return the temp-vars provider, or {@code null} if none was supplied
	 */
	public IVarDescriptionProvider getTempVarsProvider() { return tempVarsProvider; }

	/**
	 * Gets the current types manager.
	 *
	 * @return the current types manager
	 */
	public ITypesManager getTypesManager() { return currentTypesManager; }

	/**
	 * Pushes an iterator variable onto the iterator context stack.
	 *
	 * @param iteratorVar
	 *            the iterator variable expression to push
	 */
	public void pushIteratorContext(final IVarExpression iteratorVar) {
		iteratorContexts.push(iteratorVar);
	}

	/**
	 * Pops an iterator variable from the iterator context stack.
	 *
	 * @return the popped iterator variable expression
	 */
	public IVarExpression popIteratorContext() {
		return iteratorContexts.pop();
	}

	/**
	 * Peeks at the current iterator variable without removing it from the stack.
	 *
	 * @return the current iterator variable expression, or null if stack is empty
	 */
	public IVarExpression peekIteratorContext() {
		return iteratorContexts.peek();
	}

	/**
	 * Checks if the given iterator variable name exists in the current iterator contexts.
	 *
	 * @param name
	 *            the variable name to check
	 * @return the iterator variable expression if found, null otherwise
	 */
	public IVarExpression findIteratorVariable(final String name) {
		for (final IVarExpression it : iteratorContexts) { if (it.getName().equals(name)) return it; }
		return null;
	}

	/**
	 * Clears all caches in this context. This is typically called when the context is no longer needed to help garbage
	 * collection.
	 */
	@Override
	public void close() {
		iteratorContexts.clear();
	}

	/**
	 * @return
	 */
	public IDocumentationContext getDocumentationContext() { return documentationContext; }

}
