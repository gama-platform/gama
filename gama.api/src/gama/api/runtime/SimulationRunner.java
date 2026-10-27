/*******************************************************************************************************
 *
 * SimulationRunner.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import static gama.api.runtime.GamaExecutorService.EXCEPTION_HANDLER;
import static gama.api.runtime.GamaExecutorService.getParallelism;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.GamaExecutorService.Caller;
import gama.api.runtime.scope.IScope;
import gama.dev.DEBUG;

/**
 * Default implementation of {@link ISimulationRunner}: one simulation = one thread.
 *
 * <p>
 * Each simulation agent owns a dedicated {@link SimulationThread}, created when the simulation is added (i.e. before
 * its initialization) and terminated when it is removed (i.e. when it dies) or when the runner is disposed. The thread
 * consumes tasks from its own mailbox, which guarantees that the lifecycle of a simulation (initialization, steps,
 * restoration, collection of results...) is always executed by the same thread, sequentially, whatever the thread that
 * requested it.
 * </p>
 *
 * <p>
 * Execution flow of a synchronized step:
 * </p>
 *
 * <pre>
 * 1. The experiment calls step()
 * 2. step() posts one step task to the mailbox of each scheduled simulation
 * 3. Each simulation thread executes its own step task (and only its own)
 * 4. step() returns once all the step tasks are over
 * </pre>
 *
 * <p>
 * Lifecycle tasks are submitted with {@link #runInThreadOf(ISimulationAgent, Supplier)}, which waits for their result,
 * or {@link #post(ISimulationAgent, Runnable)}, which does not. A simulation thread waiting for another one keeps
 * processing its own mailbox in the meantime, so that two simulations requesting something from each other cannot
 * deadlock. The display thread never waits for a simulation thread (simulations may themselves wait for the displays):
 * tasks requested from it are run directly.
 * </p>
 *
 * <p>
 * The number of simulations stepping at the same time is limited by the concurrency level of the experiment (1 if
 * simulations are not allowed to run in parallel). Each simulation thread binds its simulation in
 * {@link SimulationContext} for its whole life.
 * </p>
 *
 * @see ISimulationRunner
 * @see ISimulationAgent
 */
public class SimulationRunner implements ISimulationRunner {

	static {
		DEBUG.OFF();
	}

	/** Sentinel posted to a mailbox to terminate its thread. */
	private static final Runnable POISON = () -> {};

	/** Maps each simulation agent to its dedicated thread. */
	final Map<ISimulationAgent, SimulationThread> threads = new ConcurrentHashMap<>();

	/** The concurrency level (number of simulations that can step concurrently, at least 1). */
	final int concurrency;

	/** The permits to step, limiting the number of simulations stepping concurrently to the concurrency level. */
	private final Semaphore stepPermits;

	/** Flag indicating if the runner has been shut down. */
	volatile boolean shutdown = false;

	/** Maximum time spent by {@link #dispose()} waiting for the simulation threads to finish their current task. */
	private static final long DISPOSE_TIMEOUT_MS = 5000;

	/**
	 * Creates a SimulationRunner configured for the given simulation population.
	 *
	 * <p>
	 * The concurrency level is determined based on:
	 * </p>
	 * <ul>
	 * <li>For headless non-batch experiments: concurrency = 1 (sequential)</li>
	 * <li>Otherwise: determined by experiment's concurrency expression and preferences (0, i.e. no concurrency, meaning
	 * that simulations step one after the other)</li>
	 * </ul>
	 *
	 * @param pop
	 *            the simulation population whose simulations will be managed
	 * @return a new SimulationRunner configured appropriately for the experiment type
	 */
	public static SimulationRunner of(final IPopulation pop) {
		int concurrency = 0;
		final IExperimentSpecies plan = (IExperimentSpecies) pop.getHost().getSpecies();
		if (plan.isHeadless() && !plan.isBatch()) {
			concurrency = 1;
		} else {
			concurrency = getParallelism(pop.getHost().getScope(), plan.getConcurrency(), Caller.SIMULATION);
		}
		return new SimulationRunner(concurrency);
	}

	/**
	 * Constructs a new SimulationRunner with the specified concurrency level.
	 *
	 * @param concurrency
	 *            the maximum number of simulations that can step concurrently (values below 1 mean 1)
	 */
	private SimulationRunner(final int concurrency) {
		this.concurrency = Math.max(1, concurrency);
		this.stepPermits = new Semaphore(this.concurrency, true);
	}

	/** The way an autonomous simulation is run. */
	private record Autonomy(Predicate<ISimulationAgent> isOver, BooleanSupplier isPaused,
			Consumer<ISimulationAgent> onOver) {}

	/**
	 * A task running a simulation autonomously. Distinguished from other tasks so that it is never run while the thread
	 * is busy stepping or waiting (it would otherwise be nested in a step).
	 */
	private record AutonomyTask(SimulationThread thread, Autonomy autonomy) implements Runnable {

		@Override
		public void run() {
			thread.runFreely(autonomy);
		}
	}

	/** A task stepping a simulation once, as part of a synchronized step of all the simulations. */
	private final class StepTask implements Runnable {

		/** The agent. */
		private final ISimulationAgent agent;

		/** Counted down once the step is over (or skipped). */
		private final CountDownLatch latch;

		/**
		 * Instantiates a new step task.
		 *
		 * @param agent
		 *            the agent
		 * @param latch
		 *            the latch
		 */
		StepTask(final ISimulationAgent agent, final CountDownLatch latch) {
			this.agent = agent;
			this.latch = latch;
		}

		@Override
		public void run() {
			try {
				if (!shutdown && !agent.dead()) { ((SimulationThread) Thread.currentThread()).stepWithPermit(); }
			} finally {
				// Counted down whatever the outcome: a step that throws would otherwise be waited for forever
				latch.countDown();
			}
		}

		/** Releases the experiment without stepping (the simulation does not take part in synchronized steps). */
		void skip() {
			latch.countDown();
		}
	}

	/** The thread owning one simulation, which executes all the tasks posted to its mailbox. */
	private final class SimulationThread extends Thread {

		/** The agent. */
		private final ISimulationAgent agent;

		/** The tasks to execute, in order. */
		private final BlockingDeque<Runnable> mailbox = new LinkedBlockingDeque<>();

		/** Whether the simulation takes part in the synchronized steps. */
		volatile boolean scheduled;

		/** Set while the simulation runs on its own, without synchronization. */
		volatile Autonomy autonomy;

		/** Set once no more task can be posted. Written under the lock of this thread. */
		private volatile boolean terminated;

		/**
		 * Instantiates a new simulation thread.
		 *
		 * @param agent
		 *            the agent
		 */
		SimulationThread(final ISimulationAgent agent) {
			super("Thread of " + agent.getName());
			this.agent = agent;
			setDaemon(true);
			setUncaughtExceptionHandler(EXCEPTION_HANDLER);
		}

		/**
		 * Posts a task to the mailbox.
		 *
		 * @return false if the thread is terminated, in which case the task will never be run
		 */
		synchronized boolean post(final Runnable task) {
			if (terminated) return false;
			mailbox.add(task);
			return true;
		}

		/** Prevents new tasks from being posted. The tasks already posted are still run before the thread ends. */
		void terminate() {
			synchronized (this) {
				if (terminated) return;
				terminated = true;
			}
			mailbox.add(POISON);
		}

		@Override
		public void run() {
			// The scope of the simulation has been created by another thread: this one executes in it from now on
			final IScope scope = agent.getScope();
			if (scope != null) { scope.bindToCurrentThread(); }
			SimulationContext.runFor(agent, this::processMailbox);
		}

		/** Executes the tasks posted to the mailbox until the thread is terminated. */
		private void processMailbox() {
			while (true) {
				final Runnable task;
				try {
					task = mailbox.take();
				} catch (final InterruptedException e) {
					continue;
				}
				if (task == POISON) { break; }
				// A stale interruption must not affect the next task
				Thread.interrupted();
				runTask(task);
			}
		}

		/** Runs a task, never letting an exception escape. */
		private void runTask(final Runnable task) {
			try {
				task.run();
			} catch (final Throwable t) {
				EXCEPTION_HANDLER.uncaughtException(this, t);
			}
		}

		/**
		 * Handles a task taken from the mailbox while the thread is busy (running autonomously): synchronized steps are
		 * skipped, other tasks are run, except the termination sentinel and requests to run autonomously (which are
		 * put back in the mailbox, to be processed once the current autonomous run is over).
		 *
		 * @return false if the task has been put back in the mailbox
		 */
		private boolean handleWhileBusy(final Runnable task) {
			if (task == POISON || task instanceof AutonomyTask) {
				mailbox.addFirst(task);
				return false;
			}
			if (task instanceof StepTask s) {
				s.skip();
			} else {
				runTask(task);
			}
			return true;
		}

		/** Runs the tasks waiting in the mailbox, without blocking. */
		private void processPendingTasks() {
			Runnable task;
			while ((task = mailbox.poll()) != null) { if (!handleWhileBusy(task)) return; }
		}

		/**
		 * Steps the simulation once, waiting for a permit if the maximum number of simulations are already stepping.
		 * Tasks posted to this thread meanwhile are processed, so that a stepping simulation requesting something from
		 * this one cannot deadlock.
		 */
		void stepWithPermit() {
			helpUntil(stepPermits::tryAcquire);
			try {
				agent.step();
			} finally {
				stepPermits.release();
			}
		}

		/**
		 * Waits until a condition is met, while processing the tasks posted to this thread (except synchronized steps,
		 * which are deferred), so that two simulations waiting for each other do not deadlock. Used to wait for a task
		 * posted to another simulation thread or for a permit to step.
		 *
		 * @param done
		 *            the condition, evaluated repeatedly until it returns true
		 */
		void helpUntil(final BooleanSupplier done) {
			if (done.getAsBoolean()) return;
			final List<Runnable> deferred = new ArrayList<>();
			boolean poisoned = false, interrupted = false;
			while (!done.getAsBoolean()) {
				final Runnable task;
				try {
					task = mailbox.poll(5, TimeUnit.MILLISECONDS);
				} catch (final InterruptedException e) {
					interrupted = true;
					continue;
				}
				if (task == null) { continue; }
				if (task == POISON) {
					poisoned = true;
				} else if (task instanceof StepTask || task instanceof AutonomyTask) {
					deferred.add(task);
				} else {
					runTask(task);
				}
			}
			for (int i = deferred.size() - 1; i >= 0; i--) { mailbox.addFirst(deferred.get(i)); }
			if (poisoned) { mailbox.addLast(POISON); }
			if (interrupted) { interrupt(); }
		}

		/** Whether the simulation should keep on running autonomously. */
		private boolean isRunning(final Autonomy a) {
			return !shutdown && !terminated && autonomy == a;
		}

		/** Shows the error to the user (and not only in the console) */
		private void report(final Throwable tg) {
			try {
				final var scope = agent.getScope();
				GAMA.reportError(scope, tg instanceof GamaRuntimeException g ? g : GamaRuntimeException.create(tg, scope),
						false);
			} catch (final Throwable ignored) {
				EXCEPTION_HANDLER.uncaughtException(this, tg);
			}
		}

		/** Steps the simulation as fast as possible until it is over, still processing the tasks posted meanwhile. */
		void runFreely(final Autonomy a) {
			boolean over = false, failed = false, notified = false;
			while (!over && isRunning(a)) {
				try {
					processPendingTasks();
					while (isRunning(a) && a.isPaused.getAsBoolean() && !a.isOver.test(agent)) {
						final Runnable task = mailbox.poll(10, TimeUnit.MILLISECONDS);
						if (task != null) { handleWhileBusy(task); }
					}
					if (isRunning(a) && !a.isOver.test(agent)) { stepWithPermit(); }
				} catch (final InterruptedException e) {
					// Only used to wake up the thread
				} catch (final Throwable tg) {
					// A simulation that fails would otherwise fail again at every step, forever
					report(tg);
					failed = true;
				}
				try {
					over = failed || agent.dead() || a.isOver.test(agent);
				} catch (final Throwable tg) {
					EXCEPTION_HANDLER.uncaughtException(this, tg);
					over = true;
				}
				if (over && isRunning(a)) {
					a.onOver.accept(agent);
					notified = true;
				}
			}
			// A simulation that dies (e.g. 'do die') is removed from the runner by its population before this thread
			// notices that it is over: it must be reported all the same, otherwise it is waited for forever
			if (!notified && !shutdown && agent.dead()) { a.onOver.accept(agent); }
			if (autonomy == a) { autonomy = null; }
		}
	}

	@Override
	public void add(final ISimulationAgent agent, final boolean scheduled) {
		if (shutdown || agent == null) return;
		final SimulationThread t = threads.computeIfAbsent(agent, a -> {
			final SimulationThread thread = new SimulationThread(a);
			thread.start();
			return thread;
		});
		if (scheduled) { t.scheduled = true; }
	}

	@Override
	public void unschedule(final ISimulationAgent agent) {
		final SimulationThread t = threads.get(agent);
		if (t == null) return;
		t.scheduled = false;
		t.autonomy = null;
	}

	@Override
	public void remove(final ISimulationAgent agent) {
		final SimulationThread t = threads.remove(agent);
		if (t != null) { t.terminate(); }
	}

	@Override
	public void runAutonomously(final ISimulationAgent agent, final Predicate<ISimulationAgent> isOver,
			final BooleanSupplier isPaused, final Consumer<ISimulationAgent> onOver) {
		// A simulation that died before (e.g. during its initialization) will never step: it is over already
		if (agent.dead()) {
			remove(agent);
			onOver.accept(agent);
			return;
		}
		final SimulationThread t = threads.get(agent);
		if (t == null) return;
		final Autonomy a = new Autonomy(isOver, isPaused, onOver);
		// The simulation does not take part in the synchronized steps anymore
		t.scheduled = false;
		t.autonomy = a;
		// If the thread has been terminated meanwhile (because the simulation died), it is over
		if (!t.post(new AutonomyTask(t, a)) && agent.dead()) { onOver.accept(agent); }
	}

	@Override
	public boolean isInThreadOf(final ISimulationAgent agent) {
		// The thread may have been removed from the map (the simulation is dying) while still running its last tasks
		return agent != null && Thread.currentThread() instanceof SimulationThread t && t.agent == agent;
	}

	@Override
	public boolean post(final ISimulationAgent agent, final Runnable task) {
		if (isInThreadOf(agent)) {
			task.run();
			return true;
		}
		final SimulationThread t = agent == null ? null : threads.get(agent);
		return t != null && t.post(task);
	}

	@Override
	public <T> T runInThreadOf(final ISimulationAgent agent, final Supplier<T> task) {
		if (isInThreadOf(agent)) return task.get();
		final SimulationThread t = agent == null ? null : threads.get(agent);
		final Thread current = Thread.currentThread();
		// No thread (yet or anymore), or in the display thread, which must never wait for a simulation
		if (t == null || GAMA.getGui().isInDisplayThread()) return task.get();
		final FutureTask<T> future = new FutureTask<>(task::get);
		if (!t.post(future)) return task.get();
		if (current instanceof SimulationThread other) {
			other.helpUntil(future::isDone);
		} else {
			boolean interrupted = false;
			while (!future.isDone()) {
				try {
					future.get();
				} catch (final InterruptedException e) {
					// The task cannot be abandoned halfway: the interruption is restored once it is over
					interrupted = true;
				} catch (final ExecutionException | CancellationException e) {
					break;
				}
			}
			if (interrupted) { current.interrupt(); }
		}
		try {
			return future.get();
		} catch (final ExecutionException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof RuntimeException r) throw r;
			if (cause instanceof Error r) throw r;
			throw new RuntimeException(cause);
		} catch (final InterruptedException e) {
			// Cannot happen as the task is done
			current.interrupt();
			return null;
		}
	}

	/**
	 * Executes one synchronized step of all the scheduled simulations, each in its own thread, and waits for all of them
	 * to be over. Returns immediately if the calling thread is interrupted (in which case the interrupt flag is left set
	 * for the caller to detect).
	 */
	@Override
	public void step() {
		final List<SimulationThread> targets = new ArrayList<>();
		for (final Map.Entry<ISimulationAgent, SimulationThread> entry : threads.entrySet()) {
			// Dead simulations never step: counting them would block the experiment forever
			if (entry.getKey().dead()) {
				remove(entry.getKey());
				continue;
			}
			final SimulationThread t = entry.getValue();
			if (t.scheduled && t.autonomy == null) { targets.add(t); }
		}
		if (targets.isEmpty()) return;
		final CountDownLatch latch = new CountDownLatch(targets.size());
		for (final SimulationThread t : targets) { if (!t.post(new StepTask(t.agent, latch))) { latch.countDown(); } }
		try {
			latch.await();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Disposes of the runner: no new task can be posted and every thread terminates once the tasks already posted are
	 * over. Unless called from a simulation thread or from the display thread (which must never wait for simulations),
	 * waits (for a bounded time) for the threads to finish, so that simulations are not disposed of while still
	 * stepping.
	 */
	@Override
	public void dispose() {
		shutdown = true;
		final List<SimulationThread> all = new ArrayList<>(threads.values());
		threads.clear();
		for (final SimulationThread t : all) { t.terminate(); }
		if (Thread.currentThread() instanceof SimulationThread || GAMA.getGui().isInDisplayThread()) return;
		final long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(DISPOSE_TIMEOUT_MS);
		boolean interrupted = false;
		for (final SimulationThread t : all) {
			final long remaining = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
			if (remaining <= 0) { break; }
			try {
				t.join(remaining);
			} catch (final InterruptedException e) {
				interrupted = true;
				break;
			}
		}
		if (interrupted) { Thread.currentThread().interrupt(); }
	}

	/**
	 * Returns the simulations that are stepped by this runner, either synchronously or autonomously.
	 *
	 * @return a snapshot of the running simulations
	 */
	@Override
	public Set<ISimulationAgent> getStepable() {
		final Set<ISimulationAgent> result = new HashSet<>();
		threads.forEach((agent, t) -> { if (t.scheduled || t.autonomy != null) { result.add(agent); } });
		return result;
	}

	@Override
	public int getActiveThreads() { return threads.size(); }

	@Override
	public boolean hasSimulations() {
		return !threads.isEmpty();
	}

}
