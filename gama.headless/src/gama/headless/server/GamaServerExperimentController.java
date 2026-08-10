/*******************************************************************************************************
 *
 * GamaServerExperimentController.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.java_websocket.WebSocket;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.kernel.simulation.AbstractExperimentController;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentStateListener;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.server.CommandResponse;
import gama.api.utils.server.GamaServerExperimentConfiguration;
import gama.api.utils.server.GamaServerMessage;
import gama.api.utils.server.MessageType;
import gama.dev.DEBUG;

/**
 * The Class ExperimentController.
 */
public class GamaServerExperimentController extends AbstractExperimentController {

	/** The parameters. */
	final IList parameters;

	/** The stop condition. */
	final String stopCondition;

	/** The execution thread. */
	public ExecutionRunnable executionThread;

	/** The job. */
	private final GamaServerExperimentJob _job;

	/**
	 * Maps each experiment agent to the controller driving it. Several controllers can share the same experiment
	 * species over the life of a server, so the agent is the only reliable key for the error path to find the
	 * controller that is currently running the command. Weak keys, so a disposed experiment drops its entry.
	 */
	private static final Map<IExperimentAgent, GamaServerExperimentController> BY_AGENT =
			Collections.synchronizedMap(new WeakHashMap<>());

	/**
	 * Returns the controller driving the given experiment agent.
	 *
	 * @param agent
	 *            the experiment agent
	 * @return the controller, or null if this agent is not driven by a server controller
	 */
	static GamaServerExperimentController of(final IExperimentAgent agent) { return BY_AGENT.get(agent); }

	/** Set once {@link ExecutionRunnable#run()} has left its loop. No step will ever be executed again after that. */
	protected volatile boolean executionFinished = false;

	/**
	 * The runtime error raised by the execution in progress, if any. Set by
	 * {@link GamaHeadlessServerGUIEventHandler#runtimeError}, as GAML errors do not propagate out of the execution.
	 */
	private volatile GamaRuntimeException lastRuntimeError;

	/**
	 * Records a runtime error raised while executing this experiment.
	 *
	 * @param error
	 *            the error reported by the runtime
	 */
	public void recordRuntimeError(final GamaRuntimeException error) {
		// First error wins: over a series of steps, the one that matters is the one that broke the series.
		if (lastRuntimeError == null) { lastRuntimeError = error; }
	}

	/**
	 * Recovers the runtime error of an init that failed without notifying the GUI, which happens while the controller
	 * is not yet the frontmost one. The scopes keep the error, so they are probed instead.
	 */
	private void captureInitError() {
		if (lastRuntimeError != null) return;
		GamaRuntimeException found = null;
		try {
			final ISimulationAgent sim = _job.simulator == null ? null : _job.simulator.getSimulation();
			if (sim != null && sim.getScope() != null) { found = sim.getScope().getCurrentError(); }
			if (found == null && scope != null) { found = scope.getCurrentError(); }
		} catch (Throwable t) {
			DEBUG.OUT("Could not probe the scopes for an init error: " + t);
		}
		lastRuntimeError = found;
	}

	@Override
	public GamaRuntimeException consumeLastRuntimeError() {
		final GamaRuntimeException error = lastRuntimeError;
		lastRuntimeError = null;
		return error;
	}

	static {
		DEBUG.ON();
	}

	/**
	 * The Class OwnRunnable.
	 */
	public class ExecutionRunnable implements Runnable {

		/** The sim. */
		final GamaServerExperimentJob mexp;

		/**
		 * Instantiates a new own runnable.
		 *
		 * @param s
		 *            the s
		 */
		ExecutionRunnable(final GamaServerExperimentJob s) {
			mexp = s;
		}

		/**
		 * Run.
		 */
		@Override
		public void run() {
			try {

				while (experimentAlive) {
					if (mexp.simulator.isInterrupted()) { break; }
					final ISimulationAgent sim = mexp.simulator.getSimulation();
					final IExperimentAgent exp = mexp.simulator.getExperimentPlan().getAgent();
					final IScope scope = sim == null ? exp.getScope() : sim.getScope();
					if (Cast.asBool(scope, exp.getStopCondition().value(scope))) {
						if (!"".equals(stopCondition)) {
							mexp.socket.send(GAMA.getJsonEncoder()
									.valueOf(new CommandResponse(MessageType.SimulationEnded, "",
											(IMap<String, Object>) exp.getAttribute("%%playCommand%%"), false))
									.toString());

						}
						break;
					}
					step();
				}
			} catch (Exception e) {
				DEBUG.OUT(e);
			} finally {
				// Releasing previouslock unblocks any step waiting on the iteration that was not run.
				executionFinished = true;
				previouslock.release();
			}
		}
	}

	/**
	 * Instantiates a new experiment controller.
	 *
	 * @param socket
	 *
	 * @param experiment
	 *            the experiment
	 */
	public GamaServerExperimentController(final GamaServerExperimentJob j, final IList parameters,
			final String stopCondition, final WebSocket sock, final boolean console, final boolean status,
			final boolean dialog, final boolean runtime) {
		_job = j;
		serverConfiguration = new GamaServerExperimentConfiguration(sock, "Unknown", console, status, dialog, runtime);
		this.parameters = parameters;
		this.stopCondition = stopCondition;
		executionThread = new ExecutionRunnable(j);

		commandThread.setUncaughtExceptionHandler(GamaExecutorService.EXCEPTION_HANDLER);
		lock.acquire();
		previouslock.acquire();
		commandThread.start();
	}

	/**
	 * Process user command.
	 *
	 * @param command
	 *            the command
	 */
	@Override
	protected boolean processUserCommand(final ExperimentCommand command) {
		switch (command.type()) {
			case _OPEN:
				lastRuntimeError = null;
				try {
					final boolean opened = _job.loadAndBuildWithJson(parameters, stopCondition).passed();
					if (!opened) { captureInitError(); }
					return opened;
				} catch (Exception e) {
					DEBUG.OUT(e);
					final GamaRuntimeException gre =
							e instanceof GamaRuntimeException g ? g : GamaRuntimeException.create(e, scope);
					lastRuntimeError = gre;
					GAMA.reportError(scope, gre, true);
					return false;
				}
			case _START:
				paused = false;
				lock.release();
				return true;
			case _PAUSE:
				paused = true;
				return true;
			case _STEP:
				// Refused rather than blocking forever on previouslock, which nobody will release any more.
				if (executionFinished) return false;
				lastRuntimeError = null;
				for(int i = 0; i < command.quantity(); i++) {
					paused = true;
					lock.release();
					previouslock.acquire();
					if (executionFinished) return false;
					// A series stops on its first error, which the caller is then told about.
					if (lastRuntimeError != null) { break; }
				}
				return true;
			case _BACK:
				if (executionFinished || !experiment.isMemorize()) return false;
				for(int i = 0; i < command.quantity(); i++) {
					paused = true;
					// backward() returns false when there is no recorded state left to go back to.
					if (!experiment.getAgent().backward(getScope())) return false;
				}
				return true;
			case _RELOAD:
				lastRuntimeError = null;
				try {
					experiment.reload();
				} catch (final GamaRuntimeException e) {
					e.printStackTrace();
					closeExperiment(e);
					GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
					return false;
				} catch (final Throwable e) {
					closeExperiment(GamaRuntimeException.create(e, scope));
					GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
					return false;
				} finally {
					// scope.getGui().updateExperimentState(scope);
				}
				return true;
			case _CLOSE:
				return true;
			default:
				return true;
		}
	}

	@Override
	public void dispose() {
		scope = null;
		if (experiment != null) {
			try {
				paused = true;
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NOTREADY);
				getScope().getGui().closeDialogs(getScope());
				// Dec 2015 This method is normally now called from
				// ExperimentSpecies.dispose()
			} finally {
				acceptingCommands = false;
				experimentAlive = false;
				lock.release();
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NONE);
				if (commandThread != null && commandThread.isAlive()) { commands.offer(_CLOSE_CMD); }
			}
		}
	}

	@Override
	public void close() {
		closeExperiment(null);
	}

	/**
	 * Close experiment.
	 *
	 * @param e
	 *            the e
	 */
	public void closeExperiment(final Exception e) {
		disposing = true;
		if (e != null) { getScope().getGui().getStatus().errorStatus(GamaRuntimeException.create(e, getScope())); }
		experiment.dispose(); // will call own dispose() later
	}

	/**
	 * Checks if is paused.
	 *
	 * @return true, if is paused
	 */
	@Override
	public boolean isPaused() { return paused; }

	/**
	 * Schedule.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 */
	@Override
	public IExecutionResult schedule(final IExperimentAgent agent) {
		BY_AGENT.put(agent, this);
		scope = agent.getScope();
		serverConfiguration = serverConfiguration.withExpId(_job.getExperimentID());
		scope.setServerConfiguration(serverConfiguration);
		IExecutionResult res = IExecutionResult.FAILED;
		try {
			res = scope.init(agent);
			if (!res.passed()) { scope.setDisposeStatus(); }
			
		} catch (final Throwable e) {
			if (scope != null && scope.interrupted()) {} else if (!(e instanceof GamaRuntimeException)) {
				GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
			}
		}
		return res;
	}

	/**
	 * Step.
	 */
	public void step() {
		if (paused) {
			lock.acquire();
			// experimentAlive = false;
		}
		try {
			_job.doStep();
		} catch (Throwable e) {
			// e.printStackTrace();
			serverConfiguration.socket()
					.send(GAMA.getJsonEncoder().valueOf(new GamaServerMessage(MessageType.RuntimeError, e)).toString());
		} finally {
			previouslock.release();
		}
	}

	/**
	 * Whether the simulation has already reached its stop condition. Evaluated on demand, as {@link #executionFinished}
	 * is only set once the execution loop has re-evaluated the condition, which happens after a step has returned.
	 * Mirrors the test performed by {@link ExecutionRunnable#run()}.
	 *
	 * @return true if no further step should be accepted
	 */
	private boolean hasReachedStopCondition() {
		try {
			final IExperimentAgent exp = _job.simulator.getExperimentPlan().getAgent();
			if (exp == null || exp.getStopCondition() == null) return false;
			final ISimulationAgent sim = _job.simulator.getSimulation();
			final IScope scope = sim == null ? exp.getScope() : sim.getScope();
			return Cast.asBool(scope, exp.getStopCondition().value(scope));
		} catch (Throwable e) {
			// If the condition cannot be evaluated, let the step through.
			DEBUG.OUT("Unable to evaluate the stop condition: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean processStep(final int nbSteps, final boolean andWait) {
			// Checked here and not only in processUserCommand, which the asynchronous path runs after answering.
			if (executionFinished || hasReachedStopCondition()) return false;
			return super.processStep(nbSteps, andWait);
	}

	@Override
	public boolean processBack(final int nbSteps, final boolean andWait) {
		// Guards repeated here as the synchronous branch below bypasses processUserCommand(_BACK).
		if (executionFinished || !experiment.isMemorize()) return false;
		paused = true;
		if (andWait) {
			for(int i = 0; i < nbSteps; i++) {
				// backward() rather than _job.doBackStep(), which discards its result.
				if (!experiment.getAgent().backward(getScope())) return false;
			}
			return true;
		}
		return super.processBack(nbSteps, andWait);
	}

}
