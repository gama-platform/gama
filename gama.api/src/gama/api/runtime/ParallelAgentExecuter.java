/*******************************************************************************************************
 *
 * ParallelAgentExecuter.java, in gama.core, is part of the source code of the
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
 * Parallel executor for running a specific {@link IExecutable} on a collection of agents.
 * 
 * <p>
 * ParallelAgentExecuter extends {@link ParallelAgentRunner} to execute GAML actions, statements, or other executables
 * on multiple agents in parallel. It's used when a model explicitly requests parallel execution of operations like
 * "ask agents parallel: true" or when {@link GamaExecutorService} determines parallel execution is beneficial.
 * </p>
 * 
 * <p>
 * The execution process:
 * </p>
 * <ol>
 * <li>Each agent in the collection has the executable executed on it</li>
 * <li>Execution continues until all agents are processed or one fails</li>
 * <li>Returns true if all executions succeeded, false if any failed</li>
 * <li>Uses {@link AtomicBoolean} for thread-safe result accumulation across parallel tasks</li>
 * </ol>
 * 
 * <p>
 * Usage example (typically internal):
 * </p>
 * 
 * <pre>
 * IExecutable action = ...; // Some GAML action
 * Spliterator&lt;IAgent&gt; agents = AgentSpliterator.of(agentArray, threshold);
 * ParallelAgentExecuter executer = new ParallelAgentExecuter(scope, action, agents);
 * Boolean success = GamaExecutorService.AGENT_PARALLEL_EXECUTOR.invoke(executer);
 * </pre>
 * 
 * @see ParallelAgentRunner
 * @see IExecutable
 * @see GamaExecutorService
 */
public class ParallelAgentExecuter extends ParallelAgentRunner<Object> {

	/** The executable to run on each agent. */
	final IExecutable executable;

	/**
	 * Constructs a new ParallelAgentExecuter to execute an action on agents.
	 * 
	 * @param scope
	 *            the execution scope providing context
	 * @param executable
	 *            the executable (action, statement, expression) to run on each agent
	 * @param agents
	 *            the spliterator managing the agents to process
	 */
	public ParallelAgentExecuter(final IScope scope, final IExecutable executable, final Spliterator<IAgent> agents) {
		super(scope, agents);
		this.executable = executable;
	}

	/**
	 * Executes the action on all agents in this partition.
	 * 
	 * <p>
	 * Iterates through the agents and executes the stored executable on each one. Stops on the first failure (when an
	 * execution doesn't pass). Uses an {@link AtomicBoolean} to safely accumulate results across potential parallel
	 * executions.
	 * </p>
	 * 
	 * @param scope
	 *            the execution scope to use
	 * @return true if all executions passed, false if any failed
	 * @throws GamaRuntimeException
	 *             if an error occurs during execution
	 */
	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		final AtomicBoolean result = new AtomicBoolean(true);
		agents.forEachRemaining(each -> {
			if (result.get()) {
				result.set(scope.execute(executable, each, null).passed());
			}
		});
		return result.get();
	}

	/**
	 * Creates a sub-task for executing the action on a subset of agents.
	 * 
	 * @param sub
	 *            the spliterator for the agent subset
	 * @return a new ParallelAgentExecuter for the subset
	 */
	@Override
	ParallelAgentExecuter subTask(final Spliterator<IAgent> sub) {
		return new ParallelAgentExecuter(originalScope, executable, sub);
	}

}