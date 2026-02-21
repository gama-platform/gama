/*******************************************************************************************************
 *
 * ExecutionContext.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gama.api.gaml.symbols.ISymbol;

/**
 * Implementation of {@link IExecutionContext} for hierarchical variable management during GAML execution.
 * 
 * <p>
 * ExecutionContext manages local and temporary variables within a specific execution context, typically corresponding
 * to a GAML statement, action, or block. Contexts are organized hierarchically, allowing inner contexts to access
 * variables from outer contexts while maintaining their own local scope.
 * </p>
 * 
 * <h2>Context Hierarchy</h2>
 * 
 * <p>
 * ExecutionContext instances form a chain from the root scope to the current execution point:
 * </p>
 * 
 * <pre>
 * Experiment Context (depth 0, outer = null)
 *   └─> Simulation Context (depth 1)
 *       └─> Agent Action Context (depth 2)
 *           └─> Loop Statement Context (depth 3)
 *               └─> Inner Statement Context (depth 4)
 * </pre>
 * 
 * <h2>Variable Types</h2>
 * 
 * <h3>Local Variables</h3>
 * <p>
 * Local variables are defined only in the current context and are not accessible from child contexts. Examples include:
 * </p>
 * <ul>
 * <li>Action parameters</li>
 * <li>Loop iteration variables (loop counter, each value)</li>
 * <li>Temporary computation results</li>
 * </ul>
 * 
 * <h3>Temporary Variables</h3>
 * <p>
 * Temporary variables can be accessed recursively through the context chain. When setting or getting a temporary
 * variable, the context first checks if it exists locally. If not, it delegates to the outer context. This allows:
 * </p>
 * <ul>
 * <li>Inner contexts to read variables from outer contexts</li>
 * <li>Inner contexts to modify variables in outer contexts</li>
 * <li>Sharing state across nested execution levels</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Creating Contexts</h3>
 * <pre>{@code
 * // Create root context
 * ExecutionContext rootContext = ExecutionContext.create(scope, null);
 * 
 * // Create child context for a statement
 * ISymbol statement = ...;
 * ExecutionContext childContext = ExecutionContext.create(rootContext, statement);
 * 
 * // Or create from existing context
 * ExecutionContext anotherChild = rootContext.createChildContext(statement);
 * }</pre>
 * 
 * <h3>Managing Local Variables</h3>
 * <pre>{@code
 * ExecutionContext context = ExecutionContext.create(scope, statement);
 * 
 * // Set local variables (only in this context)
 * context.putLocalVar("i", 0);
 * context.putLocalVar("temp", "value");
 * 
 * // Check if variable exists locally
 * if (context.hasLocalVar("i")) {
 *     Object value = context.getLocalVar("i");
 * }
 * 
 * // Remove local variable
 * context.removeLocalVar("temp");
 * 
 * // Get all local variables
 * Map<String, Object> locals = context.getLocalVars();
 * 
 * // Clear all local variables
 * context.clearLocalVars();
 * }</pre>
 * 
 * <h3>Managing Temporary Variables</h3>
 * <pre>{@code
 * // Outer context
 * ExecutionContext outerContext = ExecutionContext.create(scope, null);
 * outerContext.putLocalVar("shared", "value");
 * 
 * // Inner context
 * ExecutionContext innerContext = outerContext.createChildContext(statement);
 * 
 * // Set temp variable - if "shared" exists locally, updates it
 * // Otherwise, propagates to outer context
 * innerContext.setTempVar("shared", "new value");
 * 
 * // Get temp variable - searches current context, then outer contexts
 * Object value = innerContext.getTempVar("shared");
 * // Returns "new value" from outer context
 * }</pre>
 * 
 * <h3>Creating Copies</h3>
 * <pre>{@code
 * ExecutionContext original = ExecutionContext.create(scope, statement);
 * original.putLocalVar("x", 10);
 * original.putLocalVar("y", 20);
 * 
 * // Create a copy with same local variables
 * ExecutionContext copy = original.createCopy(newStatement);
 * 
 * // Copy has same variables but is independent
 * assert copy.getLocalVar("x").equals(10);
 * copy.putLocalVar("x", 15); // Doesn't affect original
 * assert original.getLocalVar("x").equals(10);
 * }</pre>
 * 
 * <h3>Integration with Scope</h3>
 * <pre>{@code
 * // In ExecutionScope
 * public void push(ISymbol statement) {
 *     setCurrentSymbol(statement);
 *     if (executionContext != null) {
 *         // Create child context
 *         setExecutionContext(executionContext.createChildContext(statement));
 *     } else {
 *         // Create root context
 *         setExecutionContext(ExecutionContext.create(this, statement));
 *     }
 * }
 * 
 * public void pop(ISymbol statement) {
 *     if (executionContext != null) {
 *         IExecutionContext previous = executionContext;
 *         setExecutionContext(executionContext.getOuterContext());
 *         previous.dispose(); // Clean up
 *     }
 * }
 * }</pre>
 * 
 * <h3>Typical Usage Pattern in Statements</h3>
 * <pre>{@code
 * public class LoopStatement implements IStatement {
 *     
 *     @Override
 *     public Object privateExecuteIn(IScope scope) {
 *         // Push this statement onto execution context stack
 *         scope.push(this);
 *         try {
 *             IExecutionContext context = scope.getExecutionContext();
 *             
 *             // Set loop variable as local (not accessible to outer contexts)
 *             for (int i = 0; i < 10; i++) {
 *                 context.putLocalVar("i", i);
 *                 
 *                 // Execute loop body
 *                 scope.execute(bodyStatement);
 *             }
 *         } finally {
 *             // Pop this statement from context stack
 *             scope.pop(this);
 *         }
 *         return null;
 *     }
 * }
 * }</pre>
 * 
 * <h2>Context Depth</h2>
 * 
 * <p>
 * The depth of a context indicates how many levels deep it is in the hierarchy:
 * </p>
 * <pre>{@code
 * ExecutionContext root = ExecutionContext.create(scope, null);
 * ExecutionContext level1 = root.createChildContext(stmt1);
 * ExecutionContext level2 = level1.createChildContext(stmt2);
 * 
 * assert root.depth() == 0;
 * assert level1.depth() == 1;
 * assert level2.depth() == 2;
 * }</pre>
 * 
 * <h2>Memory Management</h2>
 * 
 * <p>
 * Always call {@link #dispose()} when done with a context to release resources:
 * </p>
 * <pre>{@code
 * ExecutionContext context = ExecutionContext.create(scope, statement);
 * try {
 *     // Use the context
 * } finally {
 *     context.dispose();
 * }
 * }</pre>
 * 
 * @see IExecutionContext
 * @see ExecutionScope
 * @see IScope#getExecutionContext()
 */
public class ExecutionContext implements IExecutionContext {

	/**
	 * Creates the.
	 *
	 * @param outer
	 *            the outer
	 * @return the execution context
	 */
	public static ExecutionContext create(final IExecutionContext outer, final ISymbol command) {
		return create(outer.getScope(), outer, command);
	}

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @return the execution context
	 */
	public static ExecutionContext create(final IScope scope, final ISymbol command) {
		return create(scope, null, command);
	}

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param outer
	 *            the outer
	 * @return the execution context
	 */
	public static ExecutionContext create(final IScope scope, final IExecutionContext outer, final ISymbol command) {
		final ExecutionContext result;
		result = new ExecutionContext(command);
		result.scope = scope;
		result.outer = outer;
		// To do to avoid side effects? result.outer = outer == null ? null :
		// outer.createCopy(outer.getCurrentSymbol());
		return result;
	}

	/** The local. */
	Map<String, Object> local;

	/** The outer. */
	IExecutionContext outer;

	/** The scope. */
	IScope scope;

	/** The command. */
	ISymbol command;

	@Override
	public void dispose() {
		local = null;
		outer = null;
		scope = null;
	}

	@Override
	public IScope getScope() { return scope; }

	/**
	 * Instantiates a new execution context.
	 */
	ExecutionContext(final ISymbol command) {
		this.command = command;
	}

	@Override
	public final IExecutionContext getOuterContext() { return outer; }

	@Override
	public void setTempVar(final String name, final Object value) {
		if (local == null || !local.containsKey(name)) {
			if (outer != null) { outer.setTempVar(name, value); }
		} else {
			local.put(name, value);
		}
	}

	@Override
	public Object getTempVar(final String name) {
		if (local == null || !local.containsKey(name)) return outer == null ? null : outer.getTempVar(name);
		return local.get(name);
	}

	/**
	 * Creates the copy.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param command
	 *            the command
	 * @return the execution context
	 * @date 3 août 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public ExecutionContext createCopy(final ISymbol command) {
		final ExecutionContext r = create(scope, outer, command);
		if (local != null) {
			r.local = Collections.synchronizedMap(new HashMap<>());
			if (local != null) { r.local.putAll(local); }
		}
		return r;
	}

	/**
	 * Creates the child context.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param command
	 *            the command
	 * @return the execution context
	 * @date 3 août 2023
	 */
	@Override
	public ExecutionContext createChildContext(final ISymbol command) {
		return create(this, command);
	}

	@Override
	public Map<? extends String, ? extends Object> getLocalVars() {
		return local == null ? Collections.EMPTY_MAP : local;
	}

	@Override
	public void clearLocalVars() {
		local = null;
	}

	@Override
	public void putLocalVar(final String varName, final Object val) {
		if (local == null) { local = Collections.synchronizedMap(new HashMap<>()); }
		local.put(varName, val);
	}

	@Override
	public Object getLocalVar(final String string) {
		if (local == null) return null;
		return local.get(string);
	}

	@Override
	public boolean hasLocalVar(final String name) {
		if (local == null) return false;
		return local.containsKey(name);
	}

	@Override
	public void removeLocalVar(final String name) {
		if (local == null) return;
		local.remove(name);
	}

	@Override
	public String toString() {
		return "execution context " + local;
	}

	@Override
	public ISymbol getCurrentSymbol() { return command; }

	@Override
	public void setCurrentSymbol(final ISymbol statement) { command = statement; }

}