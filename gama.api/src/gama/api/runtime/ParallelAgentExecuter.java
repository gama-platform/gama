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
 * <li>Each partition is consumed sequentially on one thread; no shared state is needed</li>
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
	 * execution doesn't pass). This partition is consumed by a single thread, so a plain {@code boolean} is used
	 * instead of an {@link java.util.concurrent.atomic.AtomicBoolean}.
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
		boolean result = true;
		// Plain boolean is sufficient: forEachRemaining runs sequentially on this single thread
		for (final IAgent[] box = { null }; agents.tryAdvance(a -> box[0] = a);) {
			if (!result) break;
			result = scope.execute(executable, box[0], null).passed();
		}
		return result;
	}

	/**
	 * Merges the Object (Boolean) results of two parallel partitions by performing a logical AND.
	 * 
	 * <p>
	 * Both halves must have succeeded for the overall execution to be considered successful.
	 * </p>
	 * 
	 * @param leftResult
	 *            result from the forked left sub-task
	 * @param rightResult
	 *            result from the current thread's computation
	 * @return {@code true} only if both halves returned {@code true}
	 */
	@Override
	protected Object mergeResults(final Object leftResult, final Object rightResult) {
		if (leftResult instanceof Boolean lb && rightResult instanceof Boolean rb) return lb && rb;
		return rightResult;
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