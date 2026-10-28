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
		final ExecutionContext result = new ExecutionContext(command);
		result.scope = scope;
		result.outer = outer;
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

	/**
	 * The thread executing the statement this context represents: the thread that created it, unless it is the root
	 * context of a scope that has been bound to another thread (see {@link IScope#bindToCurrentThread()}).
	 */
	private volatile Thread thread = Thread.currentThread();

	/**
	 * Whether this context is visible from copies or forks of its scope, in which case its local variables can be
	 * read and written by several threads and their accesses are synchronized. Set by {@link #retain()} before the
	 * scopes are handed to other threads.
	 */
	private volatile boolean shared;

	/** The number of live copies or forks that retain this context. Guarded by this. */
	private int retainers;

	/**
	 * Retains this context and its enclosing frames until {@link #release()} is called by the copy or fork that
	 * shares them. While retained, frames remain readable even after their statement has popped them.
	 */
	void retain() {
		IExecutionContext c = this;
		while (c instanceof ExecutionContext ec) {
			synchronized (ec) {
				ec.retainers++;
				ec.shared = true;
			}
			c = ec.outer;
		}
	}

	/** Releases the frames retained by {@link #retain()}. */
	void release() {
		IExecutionContext c = this;
		while (c instanceof ExecutionContext ec) {
			final IExecutionContext next;
			synchronized (ec) {
				next = ec.outer;
				if (ec.retainers > 0) { ec.retainers--; }
				ec.shared = ec.retainers != 0;
			}
			c = next;
		}
	}

	/**
	 * Whether this context has been created by the current thread.
	 *
	 * @return true if the current thread is the one that created this context
	 */
	boolean isOwnedByCurrentThread() { return thread == Thread.currentThread(); }

	/** Makes the current thread the owner of this context. */
	void bindToCurrentThread() { thread = Thread.currentThread(); }

	@Override
	public void dispose() {
		if (shared) {
			synchronized (this) {
				// Still visible from copies or forks: they keep reading its variables and its enclosing frames
				if (retainers == 0) {
					local = null;
					outer = null;
				}
			}
		} else {
			local = null;
			outer = null;
		}
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
		final IExecutionContext o = outer;
		if (shared) {
			synchronized (this) {
				if (local != null && local.containsKey(name)) {
					local.put(name, value);
					return;
				}
			}
		} else if (local != null && local.containsKey(name)) {
			local.put(name, value);
			return;
		}
		if (o != null) { o.setTempVar(name, value); }
	}

	@Override
	public Object getTempVar(final String name) {
		final IExecutionContext o = outer;
		if (shared) {
			synchronized (this) {
				if (local != null && local.containsKey(name)) return local.get(name);
			}
		} else if (local != null && local.containsKey(name)) return local.get(name);
		return o == null ? null : o.getTempVar(name);
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
	@Override
	public ExecutionContext createCopy(final ISymbol command) {
		return createCopy(scope, command);
	}

	@Override
	public ExecutionContext createCopy(final IScope newScope, final ISymbol command) {
		final ExecutionContext r = create(newScope, outer, command);
		if (shared) {
			synchronized (this) {
				if (local != null) { r.local = new HashMap<>(local); }
			}
		} else if (local != null) { r.local = new HashMap<>(local); }
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

	@SuppressWarnings ("unchecked")
	@Override
	public Map<? extends String, ? extends Object> getLocalVars() {
		if (shared) {
			synchronized (this) {
				return local == null ? Collections.EMPTY_MAP : new HashMap<>(local);
			}
		}
		return local == null ? Collections.EMPTY_MAP : local;
	}

	@Override
	public void clearLocalVars() {
		if (shared) {
			synchronized (this) {
				local = null;
			}
		} else {
			local = null;
		}
	}

	@Override
	public void putLocalVar(final String varName, final Object val) {
		// Unless the context is shared with forked scopes, it is used by a single thread: a plain HashMap is enough
		if (shared) {
			synchronized (this) {
				if (local == null) { local = new HashMap<>(); }
				local.put(varName, val);
			}
		} else {
			if (local == null) { local = new HashMap<>(); }
			local.put(varName, val);
		}
	}

	@Override
	public Object getLocalVar(final String string) {
		if (shared) {
			synchronized (this) {
				return local == null ? null : local.get(string);
			}
		}
		return local == null ? null : local.get(string);
	}

	@Override
	public boolean hasLocalVar(final String name) {
		if (shared) {
			synchronized (this) {
				return local != null && local.containsKey(name);
			}
		}
		return local != null && local.containsKey(name);
	}

	@Override
	public void removeLocalVar(final String name) {
		if (shared) {
			synchronized (this) {
				if (local != null) { local.remove(name); }
			}
		} else if (local != null) { local.remove(name); }
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