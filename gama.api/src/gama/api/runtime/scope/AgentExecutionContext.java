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

/**
 * Manages the agent execution context stack during GAML code execution.
 *
 * <p>
 * AgentExecutionContext maintains a hierarchical chain of agents representing the execution context stack. When code
 * executes in GAMA, there is always a "current agent" - the agent in whose context the code is running. When one
 * agent calls another agent or executes code in a nested context, a new AgentExecutionContext is created and linked
 * to the outer context.
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
 * <li><b>Immutability:</b> Contexts never change once created and can be shared by several scopes</li>
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
 * 	agentContext = agentContext.getOuterContext();
 * }
 * }</pre>
 *
 * <h2>Immutability and Thread Safety</h2>
 *
 * <p>
 * AgentExecutionContext instances are immutable: popping a context from a scope does not alter it. A context chain can
 * therefore be shared, without copying it, by several scopes and threads (forked scopes of a parallel execution, copies
 * of a scope made by other threads...), even after the scope that created it has popped it. Contexts are not pooled, as
 * a pool would recycle (and alter) contexts still referenced by other scopes, and as the allocation of such small,
 * short-lived, objects is cheaper than the synchronization required by a pool.
 * </p>
 *
 * @see ExecutionScope
 * @see IAgent
 * @see IScope#push(IAgent)
 * @see IScope#pop(IAgent)
 */
public record AgentExecutionContext(IObject agent, AgentExecutionContext outer) {

	/**
	 * Returns the agent associated with this execution context.
	 *
	 * <p>
	 * This is the agent in whose context code is currently executing. All variable accesses, action executions, and
	 * attribute reads are performed in the context of this agent.
	 * </p>
	 *
	 * @return the current agent, may be null
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