/*******************************************************************************************************
 *
 * GamaExecutorService.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import static gama.api.utils.prefs.GamaPreferences.create;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.FlowStatus;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.benchmark.StopWatch;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.Pref;

/**
 * Central service for managing concurrent execution of agents in GAMA simulations.
 * 
 * <p>
 * GamaExecutorService provides a unified framework for parallel execution of agent-based operations using Java's
 * Fork/Join framework. It manages thread pools, concurrency preferences, and provides utilities for executing agent
 * steps and actions in parallel when beneficial.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Manages a custom {@link ForkJoinPool} optimized for agent-based simulations</li>
 * <li>Provides configurable concurrency levels for model-declared species, grids, and simulations</li>
 * <li>Implements threshold-based parallelism to avoid overhead on small populations</li>
 * <li>Caches parallelism decisions for constant expressions to improve performance</li>
 * <li>Handles out-of-memory errors gracefully with user notifications</li>
 * <li>Supports both sequential and parallel execution strategies based on population size</li>
 * </ul>
 * 
 * <p>
 * Species, grid, and experiment scheduling are selected by each model's {@code parallel} facet. The thread-count
 * preference controls the worker count and the default concurrency for {@code parallel: true}:
 * </p>
 * <ul>
 * <li>{@link #THREADS_NUMBER}: Worker count and default concurrency for {@code parallel: true}</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * // Execute all agents in a population
 * GamaExecutorService.step(scope, agentList, species);
 * 
 * // Execute a specific action on all agents
 * GamaExecutorService.execute(scope, executable, agentArray, parallelExpression);
 * 
 * // Configure concurrency level
 * GamaExecutorService.setConcurrencyLevel(8); // Use 8 threads
 * </pre>
 * 
 * @see ParallelAgentRunner
 * @see AgentSpliterator
 * @see ForkJoinPool
 */
public abstract class GamaExecutorService {

	/** Batch experiment facet selecting grouped parameter-set evaluation. */
	public static final String GROUP_PARAMETER_SETS = "group_parameter_sets";

	/**
	 * Custom exception handler for managing out-of-memory errors during simulation execution.
	 * 
	 * <p>
	 * This handler intercepts {@link OutOfMemoryError} exceptions thrown by worker threads in the executor service. It
	 * implements intelligent error handling with rate limiting to avoid overwhelming the user with repeated warnings.
	 * </p>
	 * 
	 * <p>
	 * Behavior:
	 * </p>
	 * <ul>
	 * <li>Detects heap memory exhaustion and offers to close experiments</li>
	 * <li>Rate-limits warnings to once per minute</li>
	 * <li>Provides user-friendly error messages with links to troubleshooting documentation</li>
	 * <li>Handles both heap and non-heap memory issues appropriately</li>
	 * </ul>
	 */
	static class GamaMemoryExceptionHandler implements UncaughtExceptionHandler {

		/** The last warning time stamp. */
		long lastWarningTimeStamp = 0l;

		@Override
		public void uncaughtException(final Thread t, final Throwable e) {
			if (e instanceof OutOfMemoryError) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastWarningTimeStamp > 60000l) {
					// 1 minute between warnings
					lastWarningTimeStamp = currentTime;
					String msg = e.getMessage();
					msg = msg == null ? "" : msg.toLowerCase();
					if (GamaPreferences.Runtime.CORE_MEMORY_ACTION.getValue() && GAMA.getExperiment() != null
							&& msg.contains("heap")) {
						GAMA.getGui().getDialogFactory().warning(
								"GAMA is out of memory. Experiment will be closed now. Please consult: https://gama-platform.org/wiki/Troubleshooting#memory-problems");
						GAMA.closeAllExperiments(true, true);
					} else {
						if (GAMA.getExperiment() != null && !msg.contains("heap")) {
							GAMA.getGui().getDialogFactory().warning(
									"GAMA cannot allocate more memory for displaying this experiment. The platform will exit now. Please try to quit other applications and relaunch it");
						} else {
							GAMA.getGui().getDialogFactory().warning(
									"Your system is running out of memory. GAMA will exit now. Please try to quit other applications and relaunch it");
						}
						System.exit(0);
					}
				}
			} else {
				e.printStackTrace();
			}
		}

	}

	/** The Constant EXCEPTION_HANDLER. */
	public static final UncaughtExceptionHandler EXCEPTION_HANDLER = new GamaMemoryExceptionHandler();

	/** The agent parallel executor. */
	public static volatile ForkJoinPool AGENT_PARALLEL_EXECUTOR;

	/**
	 * Identity-based cache for constant, non-boolean parallelism expressions. Boolean expressions also depend on
	 * the current thread-count preference and are evaluated on each call.
	 *
	 * <p>
	 * Keyed by {@code (System.identityHashCode(expr) as long) << 32 | caller.ordinal()} so that we avoid the
	 * cost of {@link IExpression#serializeToGaml(boolean)} (which may involve string allocation and traversal)
	 * on every call. Constant expressions are compiled once per model and their identity never changes, so
	 * identity-hash-code is a stable, collision-rare key for a small number of species.
	 * </p>
	 */
	private static final ConcurrentHashMap<Long, Integer> PARALLELISM_IDENTITY_CACHE = new ConcurrentHashMap<>();

	/** The Constant THREADS_NUMBER. */
	public static final Pref<Integer> THREADS_NUMBER =
			create("pref_parallel_threads",
					"Max. number of threads to use (available processors: " + Runtime.getRuntime().availableProcessors()
							+ ")",
					4, IType.INT, true).between(1, null)
							.in(GamaPreferences.Runtime.NAME, GamaPreferences.Runtime.CONCURRENCY)
							.onChange(newValue -> {
								reset();
								System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
										String.valueOf(newValue));
							});

	/**
	 * Reset.
	 */
	public static void reset() {
		// Called by the activator to init the preferences and executor services
		setConcurrencyLevel(THREADS_NUMBER.getValue());
	}

	/**
	 * Sets the concurrency level.
	 *
	 * @param nb
	 *            the new concurrency level
	 */
	public static void setConcurrencyLevel(final int nb) {
		if (AGENT_PARALLEL_EXECUTOR != null) {
			AGENT_PARALLEL_EXECUTOR.shutdown();
			try {
				// Wait for tasks to complete with timeout
				if (!AGENT_PARALLEL_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
					AGENT_PARALLEL_EXECUTOR.shutdownNow();
				}
			} catch (InterruptedException e) {
				AGENT_PARALLEL_EXECUTOR.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}

		// Create custom thread factory for better thread management
		final ForkJoinWorkerThreadFactory factory = pool -> {
			final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
			worker.setName("GAMA-Agent-Worker-" + worker.getPoolIndex());
			return worker;
		};

		// Use FIFO (asyncMode = false) for better throughput with RecursiveTask-based
		// divide-and-conquer (ParallelAgentRunner). FIFO keeps forked subtasks closer in
		// cache and reduces work-stealing distance compared to the previous LIFO async mode.
		AGENT_PARALLEL_EXECUTOR = new ForkJoinPool(nb, factory, EXCEPTION_HANDLER, false);
	}

	/**
	 * The Enum Caller.
	 */
	public enum Caller {

		/** The species. */
		SPECIES,
		/** The grid. */
		GRID,
		/** The none. */
		NONE,
		/** The simulation. */
		SIMULATION
	}

	/**
	 * Returns the level of parallelism from an explicit model expression, or zero when it is omitted.
	 *
	 * @param scope
	 *            the scope in which the facet is evaluated
	 * @param concurrency
	 *            The facet passed to the statement or species
	 * @param caller
	 *            the kind of operation requesting parallelism
	 * @return 0 for sequential execution, or a positive concurrency level or splitting threshold
	 */
	public static int getParallelism(final IScope scope, final IExpression concurrency, final Caller caller) {
		if (concurrency != null && concurrency.isConst() && GamaType.of(concurrency).id() != IType.BOOL) {
			// Use the expression's object identity as cache key. Constant expressions are
			// compiled once; using System.identityHashCode avoids the serializeToGaml()
			// string allocation of the previous string-concatenation approach.
			final long cacheKey =
					((long) System.identityHashCode(concurrency) << 32) | (caller.ordinal() & 0xFFFFFFFFL);
			return PARALLELISM_IDENTITY_CACHE.computeIfAbsent(cacheKey,
					k -> computeParallelism(scope, concurrency));
		}
		return computeParallelism(scope, concurrency);
	}

	/**
	 * Computes the parallelism level based on the expression.
	 */
	private static int computeParallelism(final IScope scope, final IExpression concurrency) {
		switch (GamaType.of(concurrency).id()) {
			case IType.BOOL: {
				Boolean c = (Boolean) concurrency.value(scope);
				if (Boolean.FALSE.equals(c)) return 0;
				return THREADS_NUMBER.getValue();
			}
			case IType.INT:
				return Math.abs((Integer) concurrency.value(scope));
			default:
				break;
		}
		return 0;
	}

	/**
	 * Execute threaded.
	 *
	 * @param r
	 *            the r
	 */
	public static void executeThreaded(final Runnable r) {
		AGENT_PARALLEL_EXECUTOR.invoke(ForkJoinTask.adapt(r));
	}

	/**
	 * Step.
	 *
	 * @param <A>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param pop
	 *            the pop
	 * @param species
	 *            the species
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static <A extends IAgent> Boolean step(final IScope scope, final IList<A> pop, final ISpecies species)
			throws GamaRuntimeException {
		final IExpression schedule = species.getSchedule();
		final IList<? extends IAgent> agents =
				schedule == null ? pop : GamaListFactory.castToList(scope, schedule.value(scope));
		final int threshold =
				getParallelism(scope, species.getConcurrency(), species.isGrid() ? Caller.GRID : Caller.SPECIES);
		return doStep(scope, agents.toArray(new IAgent[0]), threshold, species);
	}

	/**
	 * Step.
	 *
	 * @param <A>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param array
	 *            the array
	 * @param species
	 *            the species
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static <A extends IShape> Boolean step(final IScope scope, final A[] array, final ISpecies species)
			throws GamaRuntimeException {
		final IExpression schedule = species.getSchedule();
		final IShape[] scheduledAgents;
		if (schedule == null) {
			scheduledAgents = array;
		} else {
			final List<IAgent> agents = GamaListFactory.castToList(scope, schedule.value(scope));
			scheduledAgents = agents.toArray(new IAgent[0]);
		}
		final int threshold =
				getParallelism(scope, species.getConcurrency(), species.isGrid() ? Caller.GRID : Caller.SPECIES);
		return doStep(scope, scheduledAgents, threshold, species);
	}

	/**
	 * Do step.
	 *
	 * @param <A>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param array
	 *            the array
	 * @param threshold
	 *            the threshold
	 * @param species
	 *            the species
	 * @return the boolean
	 */
	private static <A extends IShape> Boolean doStep(final IScope scope, final A[] array, final int threshold,
			final ISpecies species) {
		try (final StopWatch w = GAMA.benchmark(scope, species)) {
			int concurrency = threshold;
			if (array.length <= threshold) { concurrency = 0; }
			switch (concurrency) {
				case 0:
					for (final A aa : array) {
						final IAgent agent = (IAgent) aa;
						if (agent == null || agent.dead()) {
							continue; // add this condition to avoid the activation
							// of dead agents
						}
						if (!scope.step(agent).passed()) return false;
					}
					break;
				case 1: {
					// Each agent is submitted as a separate task; each task gets its own scope copy to
					// avoid sharing mutable scope state across concurrent threads. All tasks are
					// submitted to the pool and joined before returning so that the caller always sees
					// completed results rather than fire-and-forget side effects.
					// A worker can help a nested run while joining: its task list must be invocation-local.
					final ArrayList<ForkJoinTask<?>> tasks = new ArrayList<>(array.length);
					try {
						for (final A aa : array) {
							final IAgent agent = (IAgent) aa;
							if (agent == null || agent.dead()) { continue; }
							submitFork(scope, tasks, fork -> fork.step(agent));
						}
					} catch (final RuntimeException | Error failure) {
						joinTasks(tasks, failure);
						throw failure;
					}
					joinTasks(tasks, null);
					break;
				}
				default:
					ParallelAgentRunner.step(scope, array, threshold);
			}
		}
		return true;
	}

	/**
	 * Execute.
	 *
	 * @param <A>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param executable
	 *            the executable
	 * @param array
	 *            the array
	 * @param parallel
	 *            the parallel
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static <A extends IShape> void execute(final IScope scope, final IExecutable executable, final A[] array,
			final IExpression parallel) throws GamaRuntimeException {
		int threshold = getParallelism(scope, parallel, Caller.NONE);
		if (array.length <= threshold) { threshold = 0; }
		switch (threshold) {
			case 0:
				for (final A agent : array) {
					scope.execute(executable, (IAgent) agent, true, null);
					if (scope.getAndClearBreakStatus() == FlowStatus.BREAK) { break; }
				}
				return;
			// Break doesnt really make sense for parallel execution
		case 1: {
			// Each agent is submitted as a separate task; each task gets its own scope copy to
			// avoid sharing mutable scope state across concurrent threads. All tasks are
			// submitted to the pool and joined before returning.
			final ArrayList<ForkJoinTask<?>> tasks = new ArrayList<>(array.length);
			try {
				for (final A aa : array) {
					final IAgent agent = (IAgent) aa;
					submitFork(scope, tasks, fork -> fork.execute(executable, agent, true, null));
				}
			} catch (final RuntimeException | Error failure) {
				joinTasks(tasks, failure);
				throw failure;
			}
			joinTasks(tasks, null);
			return;
		}
			default:
				ParallelAgentRunner.execute(scope, executable, array, threshold);
		}
	}

	private static void submitFork(final IScope scope, final ArrayList<ForkJoinTask<?>> tasks,
			final java.util.function.Consumer<IScope> action) {
		final IScope fork = scope.fork(" - parallel - ");
		final ForkJoinTask<?> task = ForkJoinTask.adapt(() -> {
			try {
				action.accept(fork);
			} finally {
				fork.clear();
			}
		});
		try {
			AGENT_PARALLEL_EXECUTOR.execute(task);
		} catch (final RuntimeException | Error failure) {
			fork.clear();
			throw failure;
		}
		tasks.add(task);
	}

	/** Even a failed run must finish all siblings before the caller can reuse its frames. */
	private static void joinTasks(final ArrayList<ForkJoinTask<?>> tasks, final Throwable initialFailure) {
		Throwable failure = initialFailure;
		for (final ForkJoinTask<?> task : tasks) {
			try {
				task.join();
			} catch (final RuntimeException | Error e) {
				if (failure == null) {
					failure = e;
				} else if (failure != e) {
					failure.addSuppressed(e);
				}
			}
		}
		if (failure instanceof RuntimeException e) throw e;
		if (failure instanceof Error e) throw e;
	}

	/**
	 * Execute.
	 *
	 * @param scope
	 *            the scope
	 * @param executable
	 *            the executable
	 * @param list
	 *            the list
	 * @param parallel
	 *            the parallel
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static void execute(final IScope scope, final IExecutable executable, final List<? extends IAgent> list,
			final IExpression parallel) throws GamaRuntimeException {
		execute(scope, executable, list.toArray(new IAgent[0]), parallel);
	}

}
