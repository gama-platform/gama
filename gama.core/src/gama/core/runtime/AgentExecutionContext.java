/*******************************************************************************************************
 *
 * AgentExecutionContext.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime;

import gama.core.common.interfaces.IDisposable;
import gama.core.common.util.PoolUtils;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IObject;

/**
 * The Class AgentExecutionContext.
 */
public class AgentExecutionContext implements IDisposable {

	/** The Constant POOL. */
	// Disactivated for the moment
	private static final PoolUtils.ObjectPool<AgentExecutionContext> POOL =
			PoolUtils.create("Agent Execution Context", true, AgentExecutionContext::new, null, null);

	/** The Constant POOL_ACTIVE. */
	private static final boolean POOL_ACTIVE = false;

	/**
	 * Creates the.
	 *
	 * @param agent
	 *            the agent
	 * @param outer
	 *            the outer
	 * @return the agent execution context
	 */
	public static AgentExecutionContext create(final IObject agent, final AgentExecutionContext outer) {

		final AgentExecutionContext result;
		if (POOL_ACTIVE) {
			result = POOL.get();
		} else {
			result = new AgentExecutionContext();
		}
		result.agent = agent;
		result.outer = outer;
		return result;
	}

	/** The agent. */
	IObject agent;

	/** The outer. */
	AgentExecutionContext outer;

	/**
	 * Instantiates a new agent execution context.
	 */
	private AgentExecutionContext() {}

	/**
	 * Gets the agent.
	 *
	 * @return the agent
	 */
	public IObject getCurrentObjectOrAgent() { return agent; }

	/**
	 * Gets the first agent.
	 *
	 * @return the first agent
	 */
	public IAgent getFirstAgent() {
		if (agent instanceof IAgent) return (IAgent) agent;
		if (outer != null) return outer.getFirstAgent();
		return null;
	}

	@Override
	public String toString() {
		return "context of " + agent;
	}

	/**
	 * Gets the outer context.
	 *
	 * @return the outer context
	 */
	public AgentExecutionContext getOuterContext() { return outer; }

	@Override
	public void dispose() {
		agent = null;
		outer = null;
		if (POOL_ACTIVE) { POOL.release(this); }
	}

	/**
	 * Creates the copy.
	 *
	 * @return the agent execution context
	 */
	public AgentExecutionContext createCopy() {
		// return create(agent, outer);
		// Possible to copy for avoiding side effects ?
		return create(agent, outer == null ? null : outer.createCopy());
	}

	/**
	 * Gets the simulation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the simulation
	 * @date 1 oct. 2023
	 */
	public SimulationAgent getSimulation() {
		IAgent agent = getFirstAgent();
		if (agent == null) return null;
		return agent.getSimulation();
	}

}