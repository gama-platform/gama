/*******************************************************************************************************
 *
 * DefaultExperimentController.java, in gama.api, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import java.util.concurrent.ArrayBlockingQueue;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IStatusMessage;
import gama.api.utils.tests.ITestAgent;

/**
 * Default controller for GUI-based experiment execution in GAMA.
 * 
 * <p>
 * This controller manages interactive experiments where users can control execution through the GAMA user interface.
 * It implements a dual-thread architecture with one thread processing user commands and another executing simulation
 * steps.
 * </p>
 * 
 * <h3>Architecture</h3>
 * <p>
 * The controller uses two concurrent threads:
 * </p>
 * <ul>
 * <li><b>Command Thread:</b> Processes user commands (_OPEN, _START, _PAUSE, _STEP, _BACK, _RELOAD, _CLOSE)</li>
 * <li><b>Execution Thread:</b> Continuously executes simulation steps when not paused</li>
 * </ul>
 * 
 * <h3>Execution Model</h3>
 * <p>
 * The execution thread runs in a loop controlled by the {@code experimentAlive} flag:
 * </p>
 * 
 * <pre>
 * <code>
 * while (experimentAlive) {
 *     if (paused) {
 *         lock.acquire();  // Block until released by START or STEP
 *     }
 *     step();  // Execute one simulation step
 * }
 * </code>
 * </pre>
 * 
 * <h3>Command Processing</h3>
 * <table border="1">
 * <tr>
 * <th>Command</th>
 * <th>Action</th>
 * <th>State Change</th>
 * </tr>
 * <tr>
 * <td>_OPEN</td>
 * <td>Opens experiment, initializes simulation</td>
 * <td>NONE → NOTREADY → (RUNNING or PAUSED)</td>
 * </tr>
 * <tr>
 * <td>_START</td>
 * <td>Releases execution lock, continuous stepping</td>
 * <td>PAUSED → RUNNING</td>
 * </tr>
 * <tr>
 * <td>_PAUSE</td>
 * <td>Sets paused flag</td>
 * <td>RUNNING → PAUSED</td>
 * </tr>
 * <tr>
 * <td>_STEP</td>
 * <td>Executes one step then pauses</td>
 * <td>PAUSED → (step) → PAUSED</td>
 * </tr>
 * <tr>
 * <td>_BACK</td>
 * <td>Steps backward in recorded states</td>
 * <td>Maintains PAUSED</td>
 * </tr>
 * <tr>
 * <td>_RELOAD</td>
 * <td>Reloads experiment, preserving running state</td>
 * <td>Any → NOTREADY → previous state</td>
 * </tr>
 * <tr>
 * <td>_CLOSE</td>
 * <td>Closes experiment</td>
 * <td>Any → NONE</td>
 * </tr>
 * </table>
 * 
 * <h3>State Synchronization</h3>
 * <p>
 * Two synchronizers coordinate execution:
 * </p>
 * <ul>
 * <li><b>lock:</b> Acquired when paused, released to start/step. Controls execution thread blocking.</li>
 * <li><b>previouslock:</b> Ensures step completion. Acquired during STEP, released after step completes.</li>
 * </ul>
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>
 * <code>
 * // Create and start controller
 * IExperimentSpecies experiment = ...;
 * DefaultExperimentController controller = new DefaultExperimentController(experiment);
 * 
 * // Open the experiment
 * controller.processOpen(true);  // Synchronous
 * 
 * // Start execution
 * controller.processStart(false);  // Asynchronous - returns immediately
 * 
 * // Pause after some time
 * controller.processPause(true);
 * 
 * // Execute single step
 * controller.processStep(true);
 * 
 * // Reload experiment
 * controller.processReload(true);
 * 
 * // Close when done
 * controller.close();
 * </code>
 * </pre>
 * 
 * <h3>Error Handling</h3>
 * <p>
 * When exceptions occur during command processing:
 * </p>
 * <ol>
 * <li>Exception is wrapped in {@link GamaRuntimeException} if needed</li>
 * <li>Error is displayed in GUI status bar</li>
 * <li>Experiment is closed via {@link #notifyExceptionAndCloseExperiment(Throwable)}</li>
 * <li>State updated to NONE</li>
 * </ol>
 * 
 * <h3>Lifecycle</h3>
 * <ol>
 * <li><b>Construction:</b> Both threads start, execution thread blocks on lock</li>
 * <li><b>Scheduling:</b> Agent scheduled, scope initialized, auto-run if configured</li>
 * <li><b>Execution:</b> Steps execute based on commands and pause state</li>
 * <li><b>Disposal:</b> Threads signaled to stop, resources cleaned up</li>
 * </ol>
 * 
 * <h3>Thread Safety</h3>
 * <ul>
 * <li>Command queue limited to 10 items to prevent memory issues</li>
 * <li>Uncaught exceptions handled by {@link GamaExecutorService.EXCEPTION_HANDLER}</li>
 * <li>Lock acquired in constructor to ensure controlled start</li>
 * <li>Volatile flags for state coordination</li>
 * </ul>
 * 
 * <h3>Test Integration</h3>
 * <p>
 * Automatically starts execution for {@link ITestAgent} instances, enabling automated test runs.
 * </p>
 * 
 * <h3>Server Mode</h3>
 * <p>
 * Supports server configuration for remote/headless operation while maintaining GUI controller semantics.
 * </p>
 * 
 * @see AbstractExperimentController
 * @see IExperimentController
 * @see IExperimentAgent
 * @see IExperimentStateListener.State
 * @author GAMA Team
 */
public class DefaultExperimentController extends AbstractExperimentController {

	/** The execution thread. */

	/** The agent. */
	private IExperimentAgent agent;

	/** The r. */

	/** The command thread. */
	private final Thread executionThread;

	/**
	 * Instantiates a new experiment controller.
	 *
	 * @param experiment
	 *            the experiment
	 */
	public DefaultExperimentController(final IExperimentSpecies experiment) {
		commands = new ArrayBlockingQueue<>(10);
		this.experiment = experiment;
		executionThread = new Thread(() -> { while (experimentAlive) { step(); } }, "Front end scheduler");
		executionThread.setUncaughtExceptionHandler(GamaExecutorService.EXCEPTION_HANDLER);
		commandThread.setUncaughtExceptionHandler(GamaExecutorService.EXCEPTION_HANDLER);
		lock.acquire();
		commandThread.start();
		executionThread.start();
	}

	/**
	 * Process user command.
	 *
	 * @param command
	 *            the command
	 */
	@Override
	protected boolean processUserCommand(final ExperimentCommand command) {
		final IScope scope = getScope();
		switch (command) {
			case _CLOSE:
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NONE);
				return true;
			case _OPEN:
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NOTREADY);
				try {
					experiment.open();
					return true;
					// Following a comment made here: #3925 and in
					// https://github.com/gama-platform/gama/commit/8068457d11d25289bf001bb6f29553e4037f1cda#r130876638,
					// removes the thread
					// new Thread(() -> experiment.open()).start();
				} catch (final Exception e) {
					notifyExceptionAndCloseExperiment(e);
					return false;
				}
			case _START:
				try {
					paused = false;
					lock.release();
					return true;
				} catch (final GamaRuntimeException e) {
					notifyExceptionAndCloseExperiment(e);
					return false;
				} finally {
					GAMA.updateExperimentState(experiment, IExperimentStateListener.State.RUNNING);
				}
			case _PAUSE:
				paused = true;
				if (!disposing) { GAMA.updateExperimentState(experiment, IExperimentStateListener.State.PAUSED); }
				return true;
			case _STEP:
				previouslock.acquire();
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.PAUSED);
				paused = true;
				lock.release();
				return true;
			case _BACK:
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.PAUSED);
				paused = true;
				experiment.getAgent().backward(getScope());// ?? scopes[0]);
				return true;
			case _RELOAD:
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NOTREADY);
				try {
					final boolean wasRunning = !isPaused() && !experiment.isAutorun();
					paused = true;
					scope.getGui().getStatus().waitStatus("Reloading...", IStatusMessage.SIMULATION_ICON,
							() -> experiment.reload());
					if (wasRunning) return processUserCommand(ExperimentCommand._START);
					scope.getGui().getStatus().informStatus("Experiment reloaded", IStatusMessage.SIMULATION_ICON);
					return true;
				} catch (final Throwable e) {
					notifyExceptionAndCloseExperiment(e);
					return false;
				} finally {
					GAMA.updateExperimentState(experiment);
				}
		}
		return false;
	}

	@Override
	public void dispose() {
		scope = null;
		agent = null;
		if (experiment != null) {
			try {
				paused = true;
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NOTREADY);
				getScope().getGui().closeDialogs(getScope());
				// Dec 2015 This method is normally now called from
				// ExperimentPlan.dispose()
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NONE);
			} finally {
				acceptingCommands = false;
				experimentAlive = false;
				lock.release();
				if (commandThread != null && commandThread.isAlive()) {
					// Disposing, so no need to pay attention to the result of offer()
					commands.offer(ExperimentCommand._CLOSE);
				}
			}
		}
	}

	@Override
	public void close() {
		disposing = true;
		experiment.dispose(); // will call own dispose() later
	}

	/**
	 * Notify exception.
	 *
	 * @param e
	 *            the e
	 */
	public void notifyExceptionAndCloseExperiment(final Throwable e) {
		if (e != null) { getScope().getGui().getStatus().errorStatus(GamaRuntimeException.create(e, scope)); }
		GAMA.closeExperiment(experiment);
		GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NONE);
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
	public void schedule(final IExperimentAgent agent) {
		this.agent = agent;
		scope = agent.getScope();
		serverConfiguration = GAMA.getServer() != null ? GAMA.getServer().obtainGuiServerConfiguration() : null;
		scope.setServerConfiguration(serverConfiguration);
		try {
			if (!scope.init(agent).passed()) {
				scope.setDisposeStatus();
			} else if (agent instanceof ITestAgent || agent.getSpecies().isAutorun()) { asynchronousStart(); }
		} catch (final Throwable e) {
			if (scope != null && scope.interrupted()) {} else if (!(e instanceof GamaRuntimeException)) {
				GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
			}
		}
	}

	/**
	 * Step.
	 */
	protected void step() {
		if (paused) {
			lock.acquire();
			// experimentAlive = false;
		}
		try {
			if (scope == null) return;
			IScope savedScope = scope;
			if (!savedScope.step(agent).passed()) {
				savedScope.setDisposeStatus();
				paused = true;
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		} finally {
			previouslock.release();
		}
	}

}
