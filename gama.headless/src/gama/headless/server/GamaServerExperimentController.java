/*******************************************************************************************************
 *
 * GamaServerExperimentController.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import java.io.IOException;

import org.java_websocket.WebSocket;

import gama.core.kernel.experiment.AbstractExperimentController;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IExperimentStateListener;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.runtime.server.CommandResponse;
import gama.core.runtime.server.GamaServerExperimentConfiguration;
import gama.core.runtime.server.GamaServerMessage;
import gama.core.runtime.server.MessageType;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.file.json.Json;
import gama.dev.DEBUG;
import gama.gaml.compilation.GamaCompilationFailedException;
import gama.gaml.operators.Cast;

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
					final SimulationAgent sim = mexp.simulator.getSimulation();
					final IExperimentAgent exp = mexp.simulator.getExperimentPlan().getAgent();
					final IScope scope = sim == null ? exp.getScope() : sim.getScope();
					if (Cast.asBool(scope, exp.getStopCondition().value(scope))) {
						if (!"".equals(stopCondition)) {
							mexp.socket.send(Json.getNew()
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
		switch (command) {
			case _OPEN:
				try {
					_job.loadAndBuildWithJson(parameters, stopCondition);
				} catch (Exception e) {
					DEBUG.OUT(e);
					GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
					return false;
				}
				return true;
			case _START:
				paused = false;
				lock.release();
				return true;
			case _PAUSE:
				paused = true;
				return true;
			case _STEP:
				previouslock.acquire();
				paused = true;
				lock.release();
				return true;
			case _BACK:
				paused = true;
				experiment.getAgent().backward(getScope());
				return true;
			case _RELOAD:
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
				// ExperimentPlan.dispose()
			} finally {
				acceptingCommands = false;
				experimentAlive = false;
				lock.release();
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NONE);
				if (commandThread != null && commandThread.isAlive()) { commands.offer(ExperimentCommand._CLOSE); }
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
	public void schedule(final ExperimentAgent agent) {
		scope = agent.getScope();
		serverConfiguration = serverConfiguration.withExpId(_job.getExperimentID());
		scope.setServerConfiguration(serverConfiguration);
		try {
			if (!scope.init(agent).passed()) { scope.setDisposeStatus(); }
		} catch (final Throwable e) {
			if (scope != null && scope.interrupted()) {} else if (!(e instanceof GamaRuntimeException)) {
				GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
			}
		}
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
		} catch (RuntimeException e) {
//			e.printStackTrace();
			serverConfiguration.socket().send(Json.getNew().valueOf(new GamaServerMessage(MessageType.RuntimeError, e)).toString());
		}finally {
			previouslock.release();
		}
	}

	@Override
	public boolean processStep(final boolean andWait) {
		paused = true;
		if (andWait) {
			_job.doStep();
			return true;
		}
		return super.processStep(andWait);
	}

	@Override
	public boolean processBack(final boolean andWait) {
		paused = true;
		if (andWait) {
			_job.doBackStep();
			return true;
		}
		return super.processBack(andWait);
	}

}
