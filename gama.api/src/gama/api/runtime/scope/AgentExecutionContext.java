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
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.utils.IDisposable;
import gama.api.utils.PoolUtils;

/**
 * The Class AgentExecutionContext.
 */
public class AgentExecutionContext implements IDisposable {

	/** The Constant POOL. */
	// Disactivated for the moment
	private static PoolUtils.ObjectPool<AgentExecutionContext> POOL;

	/** The Constant POOL_ACTIVE. */
	private static final boolean POOL_ACTIVE = false;

	/**
	 * Gets the pool.
	 *
	 * @return the pool
	 */
	private static PoolUtils.ObjectPool<AgentExecutionContext> getPOOL() {
		if (POOL == null) {
			POOL = PoolUtils.create("Agent Execution Context", true, AgentExecutionContext::new, null, null);
		}
		return POOL;
	}

	/**
	 * Creates the.
	 *
	 * @param agent
	 *            the agent
	 * @param outer
	 *            the outer
	 * @return the agent execution context
	 */
	public static AgentExecutionContext create(final IAgent agent, final AgentExecutionContext outer) {

		final AgentExecutionContext result;
		if (POOL_ACTIVE) {
			result = getPOOL().get();
		} else {
			result = new AgentExecutionContext();
		}
		result.agent = agent;
		result.outer = outer;
		return result;
	}

	/** The agent. */
	IAgent agent;

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
	public IAgent getAgent() { return agent; }

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
		if (POOL_ACTIVE) { getPOOL().release(this); }
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
	public ISimulationAgent getSimulation() {
		if (agent == null) return null;
		return agent.getSimulation();
	}

}