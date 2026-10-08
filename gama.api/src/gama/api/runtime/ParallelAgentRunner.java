/*******************************************************************************************************
 *
 * ParallelAgentRunner.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.runtime;

import java.util.Spliterator;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;

/**
 * Abstract base class for parallel execution of agent-based operations using the Fork/Join framework.
 * 
 * <p>
 * ParallelAgentRunner extends {@link RecursiveTask} to provide a recursive divide-and-conquer strategy for processing
 * agent collections in parallel. It works with {@link AgentSpliterator} to partition agent arrays and execute
 * operations across multiple threads managed by {@link GamaExecutorService}.
 * </p>
 * 
 * <p>
 * The class implements a work-stealing algorithm:
 * </p>
 * <ol>
 * <li>Attempts to split the agent collection in half using {@link AgentSpliterator#trySplit()}</li>
 * <li>If successful, creates a sub-task for the first half and forks it to another thread</li>
 * <li>Continues recursively processing the second half in the current thread</li>
 * <li>When the collection is too small to split (below threshold), processes agents sequentially</li>
 * </ol>
 * 
 * <p>
 * Concrete subclasses:
 * </p>
 * <ul>
 * <li>{@link ParallelAgentStepper}: Steps agents through one simulation cycle</li>
 * <li>{@link ParallelAgentExecuter}: Executes a specific {@link IExecutable} on each agent</li>
 * </ul>
 * 
 * <p>
 * This class maintains a copy of the original scope to ensure thread-safe execution, as each forked task operates in
 * its own scope instance.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * // Step all agents in parallel
 * Boolean success = ParallelAgentRunner.step(scope, agentArray, threshold);
 * 
 * // Execute an action on all agents in parallel
 * ParallelAgentRunner.execute(scope, executable, agentArray, threshold);
 * </pre>
 * 
 * @param <T>
 *            the return type of the parallel operation
 * 
 * @see RecursiveTask
 * @see AgentSpliterator
 * @see GamaExecutorService
 */
public abstract class ParallelAgentRunner<T> extends RecursiveTask<T> implements IExecutable {

	/** The spliterator managing this runner's partition of agents. */
	final Spliterator<IAgent> agents;
	
	/** The original scope, copied to ensure thread safety. */
	final IScope originalScope;

	/**
	 * Executes a ForkJoinTask in the GAMA agent executor pool.
	 * 
	 * <p>
	 * This method submits the task to {@link GamaExecutorService#AGENT_PARALLEL_EXECUTOR} and waits for its
	 * completion, returning the result.
	 * </p>
	 * 
	 * @param <T>
	 *            the task's result type
	 * @param task
	 *            the task to execute
	 * @return the task's result, or null if the task is null
	 * @throws GamaRuntimeException
	 *             if an error occurs during task execution
	 */
	public static <T> T execute(final ForkJoinTask<T> task) throws GamaRuntimeException {
		if (task == null) { return null; }
		return GamaExecutorService.AGENT_PARALLEL_EXECUTOR.invoke(task);
	}

	/**
	 * Steps all agents in an array in parallel, executing their behaviors for one simulation cycle.
	 * 
	 * <p>
	 * This method creates a {@link ParallelAgentStepper} to step each agent. If the array size is below the threshold,
	 * agents are stepped sequentially instead to avoid fork/join overhead.
	 * </p>
	 * 
	 * @param <A>
	 *            the agent type (must extend IShape)
	 * @param scope
	 *            the execution scope providing context for stepping
	 * @param array
	 *            the array of agents to step
	 * @param threshold
	 *            the minimum array size for parallel execution
	 * @return true if all agents stepped successfully, false if any agent failed
	 * @throws GamaRuntimeException
	 *             if an error occurs during agent stepping
	 */
	public static <A extends IShape> Boolean step(final IScope scope, final A[] array, final int threshold)
			throws GamaRuntimeException {
		final ParallelAgentStepper runner = from(scope, array, threshold);
		if (array.length <= threshold) { return runner.executeOn(scope); }
		return execute(runner);
	}

	/**
	 * Executes a specific executable on all agents in an array in parallel.
	 * 
	 * <p>
	 * This method creates a {@link ParallelAgentExecuter} to execute the given executable on each agent. If the array
	 * size is below the threshold, execution is sequential to avoid overhead.
	 * </p>
	 * 
	 * @param <A>
	 *            the agent type (must extend IShape)
	 * @param scope
	 *            the execution scope providing context
	 * @param executable
	 *            the executable (action, statement) to execute on each agent
	 * @param array
	 *            the array of agents to process
	 * @param threshold
	 *            the minimum array size for parallel execution
	 * @throws GamaRuntimeException
	 *             if an error occurs during execution
	 */
	public static <A extends IShape> void execute(final IScope scope, final IExecutable executable, final A[] array,
			final int threshold) throws GamaRuntimeException {
		final ParallelAgentRunner<?> runner = from(scope, executable, array, threshold);
		if (array.length <= threshold) {
			runner.executeOn(scope);
		} else {
			execute(runner);
		}
	}

	/**
	 * Creates a ParallelAgentStepper for stepping agents.
	 * 
	 * @param <A>
	 *            the agent type
	 * @param scope
	 *            the execution scope
	 * @param array
	 *            the array of agents
	 * @param threshold
	 *            the splitting threshold
	 * @return a new ParallelAgentStepper instance
	 */
	private static <A extends IShape> ParallelAgentStepper from(final IScope scope, final A[] array,
			final int threshold) {
		return new ParallelAgentStepper(scope, AgentSpliterator.of(array, threshold));
	}

	/**
	 * Creates a ParallelAgentExecuter for executing an action on agents.
	 * 
	 * @param <A>
	 *            the agent type
	 * @param scope
	 *            the execution scope
	 * @param executable
	 *            the executable to run on each agent
	 * @param array
	 *            the array of agents
	 * @param threshold
	 *            the splitting threshold
	 * @return a new ParallelAgentExecuter instance
	 */
	private static <A extends IShape> ParallelAgentExecuter from(final IScope scope, final IExecutable executable,
			final A[] array, final int threshold) {
		return new ParallelAgentExecuter(scope, executable, AgentSpliterator.of(array, threshold));
	}

	/**
	 * Constructs a new ParallelAgentRunner with the given scope and agent spliterator.
	 * 
	 * <p>
	 * The scope is copied (forked) to ensure thread-safe parallel execution. Each parallel task operates with its own
	 * scope instance derived from the original.
	 * </p>
	 * 
	 * @param <A>
	 *            the agent type
	 * @param scope
	 *            the original execution scope to copy
	 * @param agents
	 *            the spliterator managing the agent partition for this runner
	 */
	protected <A extends IShape> ParallelAgentRunner(final IScope scope, final Spliterator<IAgent> agents) {
		this.agents = agents;
		this.originalScope = scope.copy(" - forked - ");
	}

	/**
	 * Creates a sub-task for processing a subset of agents.
	 * 
	 * <p>
	 * This abstract method is implemented by subclasses to create a new runner instance of the same type but for a
	 * different agent partition. It's called during the recursive splitting process.
	 * </p>
	 * 
	 * @param sub
	 *            the spliterator for the subset of agents this sub-task should process
	 * @return a new runner instance for the subset
	 */
	abstract ParallelAgentRunner<T> subTask(Spliterator<IAgent> sub);

	/**
	 * Computes the result by recursively dividing the agent collection.
	 * 
	 * <p>
	 * This method implements the Fork/Join divide-and-conquer strategy:
	 * </p>
	 * <ol>
	 * <li>Attempts to split the agent spliterator</li>
	 * <li>If split successful, creates a sub-task for the first half and forks it</li>
	 * <li>Recursively computes the result for the second half in the current thread</li>
	 * <li>Waits for the forked sub-task to complete and merges both results via {@link #mergeResults}</li>
	 * <li>If no split possible (below threshold), executes sequentially via {@link #executeOn(IScope)}</li>
	 * </ol>
	 * 
	 * @return the computation result (type depends on the concrete implementation)
	 * @throws GamaRuntimeException
	 *             if an error occurs during computation
	 */
	@Override
	protected T compute() throws GamaRuntimeException {
		final Spliterator<IAgent> sub = agents.trySplit();
		if (sub == null) { return executeOn(originalScope); }
		final ParallelAgentRunner<T> left = subTask(sub);
		left.fork();
		final T rightResult = compute();
		final T leftResult = left.join();
		return mergeResults(leftResult, rightResult);
	}

	/**
	 * Merges the results of two parallel sub-tasks into a combined result.
	 * 
	 * <p>
	 * The default implementation returns the right (current-thread) result. Subclasses that need to combine results
	 * from both halves (e.g., collect all values) should override this method. Boolean subclasses should override to
	 * return {@code leftResult &amp;&amp; rightResult}.
	 * </p>
	 * 
	 * @param leftResult
	 *            the result produced by the forked left sub-task
	 * @param rightResult
	 *            the result produced by the current thread's recursive computation
	 * @return the combined result
	 */
	protected T mergeResults(final T leftResult, final T rightResult) {
		return rightResult;
	}

	/**
	 * Executes the operation on all agents in this runner's partition.
	 * 
	 * <p>
	 * This method is called when the agent collection is too small to split further or when sequential execution is
	 * requested. It processes all agents in the current partition sequentially. Subclasses must implement this to
	 * define the actual operation (stepping, executing an action, etc.).
	 * </p>
	 * 
	 * @param scope
	 *            the execution scope to use for processing agents
	 * @return the result of processing all agents (interpretation depends on the subclass)
	 * @throws GamaRuntimeException
	 *             if an error occurs during execution
	 */
	@Override
	public abstract T executeOn(IScope scope) throws GamaRuntimeException;

}
