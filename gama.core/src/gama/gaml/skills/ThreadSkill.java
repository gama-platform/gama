/*******************************************************************************************************
 *
 * ThreadSkill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.skills;

import java.util.concurrent.TimeUnit;

import gama.annotations.action;
import gama.annotations.arg;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.skill;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.skill.Skill;
import gama.api.runtime.SimulationContext;
import gama.api.runtime.scope.IScope;

/**
 * ThreadSkill : This class is intended to define the minimal set of behaviours required from an agent that is able to
 * run an action in a thread. Each member that has a meaning in GAML is annotated with the respective tags (vars,
 * getter, setter, init, action & args)
 *
 * @author taillandier 30 August 2022
 * @revised AD 25 September 2022 regarding the unit of the interval, the names and the management of multiple threads
 */

@doc ("The thread skill is intended to define the minimal set of behaviours required for agents that are able to run an action in a thread")
@skill (
		name = IKeyword.THREAD_SKILL,
		concept = { IConcept.SKILL, IConcept.SYSTEM })
public class ThreadSkill extends Skill {

	/** The Constant ACTION_NAME. */
	private static final String ACTION_NAME = "thread_action";

	/** The Constant START_THREAD. */
	private static final String START_THREAD = "run_thread";

	/** The Constant END_THREAD. */
	private static final String END_THREAD = "end_thread";

	/** The Constant INTERVAL. */
	private static final String EVERY = "every";

	/** The Constant INTERVAL. */
	private static final String INTERVAL = "interval";

	/** The Constant THREAD_MEMORY. */
	private static final String THREAD_MEMORY = "%%thread_memory%%";

	/**
	 * primStartThread
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = START_THREAD,
			args = { @arg (
					name = EVERY,
					type = IType.FLOAT,
					optional = true,
					doc = @doc ("Rate in machine time at which this action is run. Default unit is in seconds, use explicit units to specify another, like 10 #ms. If no rate (and no interval) is specified, the action is run once. If the action takes longer than the interval to run, it it run immediately after the previous execution")),
					@arg (
							name = INTERVAL,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("Interval -- or delay -- between two executions of the action. Default unit is in seconds, use explicit units to specify another, like 10 #ms. If no interval (and no rate) is specified, the action is run once. An interval of 0 will make the action run continuously without delays")) },

			doc = @doc (
					examples = { @example ("do run_thread every: 10#ms;") },
					returns = "true if the thread was well created and started, false otherwise",
					value = "Start a new thread that will run the 'thread_action' either once if no facets are defined, of at a fixed rate if 'every:' is defined or with a fixed delay if 'interval:' is defined."))
	public Boolean primStartThread(final IScope scope) throws GamaRuntimeException {
		// First we kill every existing task
		primEndThread(scope);
		double interval = scope.hasArg(INTERVAL) ? scope.getFloatArg(INTERVAL) : -1d;
		double rate = scope.hasArg(EVERY) ? scope.getFloatArg(EVERY) : -1d;
		ControlSubThread currentThread =
				new ControlSubThread(scope.getAgent(), (int) (interval * 1000), (int) (rate * 1000));
		scope.getAgent().setAttribute(THREAD_MEMORY, currentThread);
		currentThread.start();

		return true;
	}

	/**
	 * Prim end thread.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = END_THREAD,
			doc = @doc (
					examples = { @example ("do end_thread;") },
					returns = "true if the thread was well stopped, false otherwise",
					value = "End the current thread."))
	public Boolean primEndThread(final IScope scope) throws GamaRuntimeException {
		ControlSubThread currentThread = (ControlSubThread) scope.getAgent().getAttribute(THREAD_MEMORY);
		if (currentThread != null) {
			currentThread.stop();
			scope.getAgent().setAttribute(THREAD_MEMORY, null);
			return true;
		}
		return false;
	}

	/**
	 * primExternalFactorOnRemainingTime
	 *
	 * @param scope
	 *            the scope
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			doc = @doc (
					value = "A virtual action, which contains what to execute in the thread. It needs to be redefined in the species that implement the `thread` skill"),
			name = ACTION_NAME,
			virtual = true)
	public void threadAction(final IScope scope) throws GamaRuntimeException {
		// return null;
	}

	/**
	 * Runs the 'thread_action' of an agent in its own virtual thread, in parallel with the simulation. Virtual threads
	 * are cheap to create and to park (while waiting for the next execution), so no pool needs to be shared or kept
	 * alive between uses: the thread ends when the task is stopped, when the agent dies or when its scope is
	 * interrupted.
	 */
	public static class ControlSubThread implements Runnable {

		/** The delay between the end of an execution and the start of the next one (fixed delay), in ms. */
		private final int interval;

		/** The period between the starts of two executions (fixed rate), in ms. */
		private final int rate;

		/** The agent. */
		private final IAgent agent;

		/** Whether this task has been stopped. */
		private volatile boolean stopped;

		/** The virtual thread running this task. */
		private volatile Thread thread;

		/**
		 * Instantiates a new control sub thread.
		 *
		 * @param ag
		 *            the agent
		 * @param interval
		 *            the fixed delay in ms (0 for continuous execution, negative if not defined)
		 * @param rate
		 *            the fixed rate in ms (negative if not defined)
		 */
		public ControlSubThread(final IAgent ag, final int interval, final int rate) {
			this.interval = interval;
			this.rate = rate;
			agent = ag;
		}

		/**
		 * Starts the virtual thread, bound to the simulation of the agent (see {@link SimulationContext}).
		 */
		public void start() {
			final IScope scope = agent.getScope();
			final ISimulationAgent simulation = scope == null ? null : scope.getSimulation();
			thread = Thread.ofVirtual().name("Thread skill of " + agent.getName())
					.start(() -> SimulationContext.runFor(simulation, this));
		}

		/**
		 * Stop.
		 */
		public void stop() {
			stopped = true;
			Thread t = thread;
			if (t != null && t != Thread.currentThread()) { t.interrupt(); }
		}

		@Override
		public void run() {
			final boolean once = rate <= 0 && interval < 0;
			long next = System.nanoTime();
			try {
				while (!stopped && executeOnce() && !once) {
					long delay;
					if (rate > 0) {
						// Fixed rate: if late, run immediately after the previous execution without trying to catch up
						next = Math.max(next + TimeUnit.MILLISECONDS.toNanos(rate), System.nanoTime());
						delay = next - System.nanoTime();
					} else {
						delay = TimeUnit.MILLISECONDS.toNanos(interval);
					}
					if (delay > 0) {
						TimeUnit.NANOSECONDS.sleep(delay);
					} else {
						Thread.yield();
					}
				}
			} catch (InterruptedException e) {
				// Stopped while waiting
			} finally {
				stopped = true;
			}
		}

		/**
		 * Executes the thread action once.
		 *
		 * @return true if the execution can go on, false if the agent is dead or its scope interrupted or if an
		 *         error occurred
		 */
		private boolean executeOnce() {
			if (agent.dead()) return false;
			IScope scope = agent.getScope();
			if (scope == null || scope.interrupted()) return false;
			IStatement action = agent.getSpecies().getAction(ACTION_NAME);
			try (IScope copy = scope.copy("ThreadScope")) {
				copy.execute(action, agent, null);
			} catch (RuntimeException e) {
				// Errors happening because the thread has been stopped while executing the action are ignored
				if (!stopped) { GAMA.reportError(scope, GamaRuntimeException.create(e, scope), false); }
				return false;
			}
			return true;
		}

	}

}
