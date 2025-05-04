/*******************************************************************************************************
 *
 * ThreadSkill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.skills;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.IConcept;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IStatement;
import gama.gaml.types.IType;

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

	/**
	 * @param desc
	 */
	public ThreadSkill(final IDescription desc) {
		super(desc);
	}

	/** The executor. */
	ScheduledExecutorService executor = Executors.newScheduledThreadPool(GamaExecutorService.THREADS_NUMBER.getValue());

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
	public Object primExternalFactorOnRemainingTime(final IScope scope) throws GamaRuntimeException {
		return null;
	}

	/**
	 * The Class ControlSubThread.
	 */
	public class ControlSubThread implements Runnable {

		/** The sf. */
		ScheduledFuture sf;

		/** The interval. */
		private final int interval, rate;

		/** The agent. */
		private final IAgent agent;

		/**
		 * Instantiates a new control sub thread.
		 *
		 * @param ag
		 *            the ag
		 * @param cont
		 *            the cont
		 * @param sleepInterval
		 *            the sleep interval
		 */
		public ControlSubThread(final IAgent ag, final int interval, final int rate) {
			this.interval = interval;
			this.rate = rate;
			agent = ag;
		}

		/**
		 * Start.
		 */
		public void start() {
			if (rate > 0) {
				sf = executor.scheduleAtFixedRate(this, 0, rate, TimeUnit.MILLISECONDS);
			} else if (interval > 0) {
				sf = executor.scheduleWithFixedDelay(this, 0, interval, TimeUnit.MILLISECONDS);
			} else if (interval == 0) {
				sf = executor.scheduleWithFixedDelay(this, 0, 1, TimeUnit.NANOSECONDS);
			} else {
				executor.execute(this);
			}
		}

		/**
		 * Stop.
		 */
		public void stop() {
			if (sf != null && !sf.isCancelled()) { sf.cancel(true); }
		}

		@Override
		public void run() {
			if (agent.dead()) {
				stop();
				return;
			}
			IScope scope = agent.getScope();
			if (scope != null && !scope.interrupted()) {
				ISpecies context = agent.getSpecies();
				IStatement action = context.getAction(ACTION_NAME);
				scope.copy("ThreadScope").execute(action, agent, null);
			} else {
				stop();
			}
		}

	}

}
