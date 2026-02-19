/*******************************************************************************************************
 *
 * ExpressionCompilationContext.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.validation.IDocumentationContext;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gaml.compiler.gaml.validation.DocumentationContext;

/**
 * ExpressionCompilationContext encapsulates all mutable state required during expression compilation. This class is designed to
 * be created per compilation session and passed through the compilation process, enabling the GamlExpressionCompiler to
 * be stateless.
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
 * A new ExpressionCompilationContext is created for each top-level compilation request and is discarded after the compilation
 * completes. The context is not meant to be reused across multiple independent compilations.
 * </p>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * This class is NOT thread-safe. Each thread should have its own ExpressionCompilationContext instance. However, since contexts
 * are short-lived (one per compilation), this is not a concern in practice.
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
	 * The current expression description being compiled. Used to disable reentrant parsing (Issue 782).
	 */
	// private IExpressionDescription currentExpressionDescription;

	/**
	 * The current types manager for type resolution in the current compilation context.
	 */
	private ITypesManager currentTypesManager;

	/**
	 * The current parsing context (IDescription) in which the compiler operates. If none is given, the global context
	 * of the current simulation is used.
	 */
	private IDescription currentContext;

	/** The documentation context. */
	private final IDocumentationContext documentationContext;

	/**
	 * Creates a new compilation context with the specified initial description context.
	 *
	 * @param initialContext
	 *            the initial description context for compilation
	 */
	public ExpressionCompilationContext(final IDescription context) {
		this.currentContext = context;
		this.currentTypesManager = findTypesManager(context);
		this.documentationContext = findDocumentationContext(context);
	}

	/**
	 * Find types manager.
	 *
	 * @param context
	 *            the context
	 * @return the i types manager
	 */
	public static ITypesManager findTypesManager(final IDescription context) {
		if (context == null) return Types.builtInTypes;
		final IModelDescription md = context.getModelDescription();
		if (md == null) return Types.builtInTypes;
		final ITypesManager tm = md.getTypesManager();
		return tm != null ? tm : Types.builtInTypes;
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
	 * Gets the current types manager.
	 *
	 * @return the current types manager
	 */
	public ITypesManager getTypesManager() { return currentTypesManager; }
	//
	// /**
	// * Gets the current expression description being compiled.
	// *
	// * @return the current expression description, or null if none
	// */
	// public IExpressionDescription getCurrentExpressionDescription() { return currentExpressionDescription; }
	//
	// /**
	// * Sets the current expression description being compiled.
	// *
	// * @param description
	// * the expression description to set
	// */
	// public void setCurrentExpressionDescription(final IExpressionDescription description) {
	// this.currentExpressionDescription = description;
	// }

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
	 * Sets the context.
	 *
	 * @param context
	 *            the context
	 * @return the i description
	 */
	public IDescription setContext(final IDescription context) {
		IDescription previous = currentContext;
		this.currentContext = context;
		this.currentTypesManager = findTypesManager(context);
		return previous;
	}

	/**
	 * @return
	 */
	public IDocumentationContext getDocumentationContext() { return documentationContext; }

}
