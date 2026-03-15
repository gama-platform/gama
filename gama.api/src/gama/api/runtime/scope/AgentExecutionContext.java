/*******************************************************************************************************
 *
 * AgentExecutionContext.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.object.IObject;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.utils.PoolUtils;
import gama.api.utils.interfaces.IDisposable;

/**
 * Manages the agent execution context stack during GAML code execution.
 *
 * <p>
 * AgentExecutionContext maintains a hierarchical chain of agents representing the execution context stack. When code
 * executes in GAMA, there is always a "current agent" - the agent in whose context the code is running. When one agent
 * calls another agent or executes code in a nested context, a new AgentExecutionContext is created and linked to the
 * outer context.
 * </p>
 *
 * <h2>Context Hierarchy</h2>
 *
 * <p>
 * Contexts form a chain from the root (experiment/simulation) to the current agent:
 * </p>
 *
 * <pre>
 * ExperimentAgent context
 *   └─> SimulationAgent context (outer = experiment context)
 *       └─> RegularAgent context (outer = simulation context)
 *           └─> NestedAgent context (outer = regular agent context)
 * </pre>
 *
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Agent Stack Management:</b> Maintains the chain of agent contexts</li>
 * <li><b>Outer Context Access:</b> Provides access to parent execution contexts</li>
 * <li><b>Simulation Access:</b> Quick access to the simulation agent via context chain traversal</li>
 * <li><b>Object Pooling:</b> Uses object pooling for efficient context creation and disposal</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Creating a Context</h3>
 *
 * <pre>{@code
 * // Create a new context for an agent
 * AgentExecutionContext context = AgentExecutionContext.create(agent, outerContext);
 *
 * // Access the agent
 * IAgent currentAgent = context.getAgent();
 *
 * // Access outer context
 * AgentExecutionContext parentContext = context.getOuterContext();
 *
 * // Get simulation through context chain
 * ISimulationAgent simulation = context.getSimulation();
 * }</pre>
 *
 * <h3>Creating Nested Contexts</h3>
 *
 * <pre>{@code
 * // Outer context (e.g., simulation agent)
 * AgentExecutionContext simulationContext = AgentExecutionContext.create(simulation, null);
 *
 * // Agent executing in simulation context
 * AgentExecutionContext agentContext = AgentExecutionContext.create(agent, simulationContext);
 *
 * // Nested execution (e.g., agent calling another agent)
 * AgentExecutionContext nestedContext = AgentExecutionContext.create(otherAgent, agentContext);
 *
 * // Traverse the context chain
 * assert nestedContext.getAgent() == otherAgent;
 * assert nestedContext.getOuterContext() == agentContext;
 * assert nestedContext.getOuterContext().getAgent() == agent;
 * }</pre>
 *
 * <h3>Copying a Context</h3>
 *
 * <pre>{@code
 * // Create a copy of a context (e.g., for parallel execution)
 * AgentExecutionContext copy = originalContext.createCopy();
 *
 * // Copy has same agent and copies outer context chain
 * assert copy.getAgent() == originalContext.getAgent();
 * assert copy.getOuterContext() != originalContext.getOuterContext(); // Different instance
 * }</pre>
 *
 * <h3>Proper Disposal</h3>
 *
 * <pre>{@code
 * AgentExecutionContext context = AgentExecutionContext.create(agent, outerContext);
 * try {
 * 	// Use the context for execution
 * 	Object result = executeInContext(context);
 * } finally {
 * 	// Always dispose to return to pool
 * 	context.dispose();
 * }
 * }</pre>
 *
 * <h2>Integration with ExecutionScope</h2>
 *
 * <p>
 * AgentExecutionContext is used internally by {@link ExecutionScope} to manage the agent stack:
 * </p>
 *
 * <pre>{@code
 * // In ExecutionScope
 * public boolean push(IAgent agent) {
 * 	// Create new child context with current context as outer
 * 	agentContext = AgentExecutionContext.create(agent, agentContext);
 * 	return true;
 * }
 *
 * public void pop(IAgent agent) {
 * 	// Dispose current context and revert to outer
 * 	AgentExecutionContext previous = agentContext;
 * 	agentContext = agentContext.getOuterContext();
 * 	previous.dispose(); // Return to pool
 * }
 * }</pre>
 *
 * <h2>Object Pooling</h2>
 *
 * <p>
 * AgentExecutionContext uses an object pool to reduce allocation overhead during frequent context creation/destruction.
 * Always call {@link #dispose()} when done with a context to return it to the pool for reuse.
 * </p>
 *
 * <p>
 * The pool is managed automatically:
 * </p>
 * <ul>
 * <li>{@link #create(IAgent, AgentExecutionContext)} retrieves a context from the pool</li>
 * <li>{@link #dispose()} returns the context to the pool after clearing references</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>
 * AgentExecutionContext instances are not thread-safe. Each thread should maintain its own context chain. The object
 * pool is thread-safe and can be shared across threads.
 * </p>
 *
 * @see ExecutionScope
 * @see IAgent
 * @see IScope#push(IAgent)
 * @see IScope#pop(IAgent)
 */
public class AgentExecutionContext implements IDisposable {

	/**
	 * Object pool for efficient context allocation and reuse.
	 *
	 * <p>
	 * The pool reduces garbage collection pressure by reusing AgentExecutionContext instances. Currently activated for
	 * performance optimization.
	 * </p>
	 */
	private static PoolUtils.ObjectPool<AgentExecutionContext> POOL =
			PoolUtils.create("Agent Execution Context", true, AgentExecutionContext::new, null, null);

	/**
	 * Creates a new agent execution context or retrieves one from the pool.
	 *
	 * <p>
	 * This is the factory method for creating AgentExecutionContext instances. It retrieves a context from the object
	 * pool and initializes it with the specified agent and outer context.
	 * </p>
	 *
	 * <h3>Example</h3>
	 *
	 * <pre>{@code
	 * // Create root context
	 * AgentExecutionContext rootContext = AgentExecutionContext.create(simulation, null);
	 *
	 * // Create nested context
	 * AgentExecutionContext childContext = AgentExecutionContext.create(agent, rootContext);
	 * }</pre>
	 *
	 * @param agent
	 *            the agent for this context
	 * @param outer
	 *            the outer (parent) context, or null if this is a root context
	 * @return a new or reused AgentExecutionContext instance
	 */
	public static AgentExecutionContext create(final IObject agent, final AgentExecutionContext outer) {
		final AgentExecutionContext result = POOL.get();
		result.agent = agent;
		result.outer = outer;
		return result;
	}

	/** The current agent in this context. */
	IObject agent;

	/** The outer (parent) execution context. */
	AgentExecutionContext outer;

	/**
	 * Private constructor - use {@link #create(IAgent, AgentExecutionContext)} instead.
	 */
	private AgentExecutionContext() {}

	/**
	 * Returns the agent associated with this execution context.
	 *
	 * <p>
	 * This is the agent in whose context code is currently executing. All variable accesses, action executions, and
	 * attribute reads are performed in the context of this agent.
	 * </p>
	 *
	 * @return the current agent, may be null if the context has been disposed
	 */
	public IObject getCurrentObjectOrAgent() { return agent; }

	/**
	 * Gets the first agent.
	 *
	 * @return the first agent
	 */
	public IAgent getFirstAgent() {
		if (agent instanceof IAgent a) return a;
		if (outer != null) return outer.getFirstAgent();
		return null;
	}

	/**
	 * Returns a string representation of this context.
	 *
	 * @return a string describing this context and its agent
	 */
	@Override
	public String toString() {
		return "context of " + agent;
	}

	/**
	 * Returns the outer (parent) execution context.
	 *
	 * <p>
	 * The outer context represents the execution context that was active before this context was created. This forms a
	 * chain back to the root context (typically the experiment or simulation agent).
	 * </p>
	 *
	 * <h3>Example - Traversing Context Chain</h3>
	 *
	 * <pre>{@code
	 * AgentExecutionContext current = context;
	 * while (current != null) {
	 * 	System.out.println("Agent: " + current.getAgent().getName());
	 * 	current = current.getOuterContext();
	 * }
	 * }</pre>
	 *
	 * @return the outer context, or null if this is the root context
	 */
	public AgentExecutionContext getOuterContext() { return outer; }

	/**
	 * Disposes this context and returns it to the object pool.
	 *
	 * <p>
	 * This method clears all references from this context and returns it to the pool for reuse. Always call dispose()
	 * when done with a context to prevent memory leaks and enable object reuse.
	 * </p>
	 *
	 * <p>
	 * After calling dispose(), this context should not be used further as its state has been cleared.
	 * </p>
	 */
	@Override
	public void dispose() {
		agent = null;
		outer = null;
		POOL.release(this);
	}

	/**
	 * Creates a deep copy of this execution context.
	 *
	 * <p>
	 * This method creates a new context with the same agent and a copy of the entire outer context chain. This is
	 * useful when you need to preserve a context for later use or create an independent copy for parallel execution.
	 * </p>
	 *
	 * <p>
	 * Note: The copy includes copies of all outer contexts in the chain to avoid side effects.
	 * </p>
	 *
	 * <h3>Example</h3>
	 *
	 * <pre>{@code
	 * // Original context chain
	 * AgentExecutionContext original = context;
	 *
	 * // Create independent copy
	 * AgentExecutionContext copy = original.createCopy();
	 *
	 * // Both have same agents but independent context chains
	 * assert copy.getAgent() == original.getAgent();
	 * assert copy.getOuterContext() != original.getOuterContext();
	 * }</pre>
	 *
	 * @return a new AgentExecutionContext that is a deep copy of this context
	 */
	public AgentExecutionContext createCopy() {
		// return create(agent, outer);
		// Possible to copy for avoiding side effects ?
		return create(agent, outer == null ? null : outer.createCopy());
	}

	/**
	 * Returns the simulation agent by traversing the context chain.
	 *
	 * <p>
	 * This is a convenience method that walks up the context chain to find the simulation agent. If the current agent
	 * is a simulation agent, it is returned directly. Otherwise, the method delegates to the agent's own
	 * getSimulation() method.
	 * </p>
	 *
	 * <h3>Example</h3>
	 *
	 * <pre>{@code
	 * // Get simulation from any context
	 * ISimulationAgent simulation = context.getSimulation();
	 *
	 * // Access simulation-level services
	 * IClock clock = simulation.getClock();
	 * ITopology topology = simulation.getTopology();
	 * }</pre>
	 *
	 * @return the simulation agent, or null if no agent is set in this context
	 */
	public ISimulationAgent getSimulation() {
		IAgent agent = getFirstAgent();
		if (agent == null) return null;
		return agent.getSimulation();
	}

}