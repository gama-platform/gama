/*******************************************************************************************************
 *
 * ParallelAgentStepper.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.runtime;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * Parallel executor for stepping a collection of agents through one simulation cycle.
 * 
 * <p>
 * ParallelAgentStepper extends {@link ParallelAgentRunner} to execute the {@link IStepable#step(IScope)} method on
 * multiple agents concurrently. It's used by {@link GamaExecutorService} when parallel execution is enabled for
 * species or grid populations.
 * </p>
 * 
 * <p>
 * The stepping process:
 * </p>
 * <ol>
 * <li>Each agent in the collection has its step() method called</li>
 * <li>Agents execute their reflexes and scheduled behaviors</li>
 * <li>Stepping continues until all agents complete or one fails</li>
 * <li>Returns true if all agents stepped successfully, false if any failed</li>
 * <li>Uses {@link AtomicBoolean} for thread-safe result accumulation</li>
 * </ol>
 * 
 * <p>
 * <b>Important:</b> Parallel stepping can improve performance for large populations but may affect reproducibility of
 * simulations due to non-deterministic execution order. Users should be aware that enabling parallel species/grid
 * execution trades reproducibility for speed.
 * </p>
 * 
 * <p>
 * Usage example (typically internal):
 * </p>
 * 
 * <pre>
 * Spliterator&lt;IAgent&gt; agents = AgentSpliterator.of(population, threshold);
 * ParallelAgentStepper stepper = new ParallelAgentStepper(scope, agents);
 * Boolean success = GamaExecutorService.AGENT_PARALLEL_EXECUTOR.invoke(stepper);
 * </pre>
 * 
 * @see ParallelAgentRunner
 * @see IStepable
 * @see GamaExecutorService
 */
public class ParallelAgentStepper extends ParallelAgentRunner<Boolean> {

	/**
	 * Constructs a new ParallelAgentStepper to step agents through a simulation cycle.
	 * 
	 * @param scope
	 *            the execution scope providing simulation context
	 * @param agents
	 *            the spliterator managing the agents to step
	 */
	public ParallelAgentStepper(final IScope scope, final Spliterator<IAgent> agents) {
		super(scope, agents);
	}

	/**
	 * Steps all agents in this partition through one simulation cycle.
	 * 
	 * <p>
	 * Iterates through the agents and calls {@link IScope#step(IAgent)} on each one, which executes the agent's
	 * reflexes and other scheduled behaviors. Stops on the first failure. Uses an {@link AtomicBoolean} to safely
	 * accumulate results across parallel executions.
	 * </p>
	 * 
	 * @param scope
	 *            the execution scope to use for stepping
	 * @return true if all agents stepped successfully, false if any agent's step failed
	 * @throws GamaRuntimeException
	 *             if an error occurs during agent stepping
	 */
	@Override
	public Boolean executeOn(final IScope scope) throws GamaRuntimeException {
		final AtomicBoolean result = new AtomicBoolean(true);
		agents.forEachRemaining(each -> {
			if (result.get()) {
				result.set(scope.step(each).passed());
			}
		});
		return result.get();
	}

	/**
	 * Creates a sub-task for stepping a subset of agents.
	 * 
	 * @param sub
	 *            the spliterator for the agent subset
	 * @return a new ParallelAgentStepper for the subset
	 */
	@Override
	ParallelAgentRunner<Boolean> subTask(final Spliterator<IAgent> sub) {
		return new ParallelAgentStepper(originalScope, sub);
	}

}