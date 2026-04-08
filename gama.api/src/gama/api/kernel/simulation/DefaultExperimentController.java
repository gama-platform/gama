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

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IStatusMessage;
import gama.dev.DEBUG;

/**
 * Default controller for GUI-based experiment execution in GAMA.
 *
 * <p>
 * This controller manages interactive experiments where users can control execution through the GAMA user interface. It
 * implements a dual-thread architecture with one thread processing user commands and another executing simulation
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

	/**
	 * The agent. Volatile so writes from schedule() on the calling thread are immediately visible to the execution
	 * thread.
	 */
	private volatile IExperimentAgent agent;

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
		this.experiment = experiment;

		// Execution thread runs simulation steps when not paused
		executionThread = new Thread(() -> { while (experimentAlive) { step(); } },
				"Experiment Execution Thread [" + experiment.getName() + "]");

		executionThread.setUncaughtExceptionHandler(GamaExecutorService.EXCEPTION_HANDLER);
		commandThread.setUncaughtExceptionHandler(GamaExecutorService.EXCEPTION_HANDLER);

		// Acquire lock initially to ensure execution thread blocks until explicitly started.
		// If acquisition fails due to interruption the thread's interrupt flag is already
		// restored by acquire(); nothing more to do here — the execution thread will exit
		// immediately on its first loop iteration because experimentAlive will be false.
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
	/**
	 * Process user command.
	 *
	 * <p>
	 * Handles experiment control commands. Commands modify execution state and trigger state change notifications.
	 * </p>
	 *
	 * <h3>Optimizations Applied:</h3>
	 * <ul>
	 * <li>Caches scope reference to avoid repeated getScope() calls</li>
	 * <li>Guards lock.release() to prevent exceptions when already released</li>
	 * <li>Adds null checks before scope operations</li>
	 * </ul>
	 *
	 * @param command
	 *            the command to execute
	 * @return true if successful, false otherwise
	 */
	@Override
	protected boolean processUserCommand(final ExperimentCommand command) {
		// Optimization: Cache scope to reduce volatile reads
		final IScope currentScope = getScope();

		switch (command) {
			case _CLOSE:
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NONE);
				return true;

			case _OPEN:
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NOTREADY);
				try {
					experiment.open();
					return true;
				} catch (final Throwable e) {
					// Catch Throwable (not just Exception) to handle JVM Errors such as
					// NoClassDefFoundError or LinkageError that may arise from bad dependencies
					notifyExceptionAndCloseExperiment(e);
					return false;
				}

			case _START:
				try {
					// Optimization: Only change state if actually paused
					// This avoids redundant lock operations
					if (paused) {
						paused = false;
						lock.release();
					}
					return true;
				} catch (final Throwable e) {
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
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.PAUSED);
				paused = true;
				lock.release(); // let the execution thread run one step
				// Wait for that step to complete; abort command if interrupted.
				if (!previouslock.acquire()) return false;
				return true;

			case _BACK:
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.PAUSED);
				paused = true;
				// Optimization: Add null check before operation
				if (experiment.getAgent() != null && currentScope != null) {
					experiment.getAgent().backward(currentScope);
				}
				return true;

			case _RELOAD:
				// Optimization: Early exit if scope not available
				if (currentScope == null) {
					DEBUG.ERR("Cannot reload experiment: scope not initialized");
					return false;
				}

				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NOTREADY);
				try {
					final boolean wasRunning = !isPaused() && !experiment.isAutorun();
					paused = true;
					currentScope.getGui().getStatus().waitStatus("Reloading...", IStatusMessage.SIMULATION_ICON,
							() -> experiment.reload());
					if (wasRunning) return processUserCommand(ExperimentCommand._START);
					currentScope.getGui().getStatus().informStatus("Experiment reloaded",
							IStatusMessage.SIMULATION_ICON);
					return true;
				} catch (final Throwable e) {
					// Improved error handling with proper logging
					DEBUG.ERR("Error during experiment reload", e);
					notifyExceptionAndCloseExperiment(e);
					return false;
				} finally {
					GAMA.updateExperimentState(experiment);
				}
		}
		return false;
	}

	/**
	 * Disposes the controller and releases all resources.
	 *
	 * <p>
	 * This method ensures clean shutdown of both execution and command threads, releases locks, and cleans up
	 * resources. It's safe to call multiple times.
	 * </p>
	 *
	 * <h3>Optimizations Applied:</h3>
	 * <ul>
	 * <li>Sets disposing flag early to prevent new operations</li>
	 * <li>Uses proper thread join with timeout to avoid indefinite blocking</li>
	 * <li>Handles InterruptedException properly</li>
	 * <li>Ensures lock is always released even if exceptions occur</li>
	 * </ul>
	 */
	@Override
	public void dispose() {
		// Set disposing flag early so other threads stop accepting new work
		disposing = true;

		if (experiment != null) {
			try {
				paused = true;
				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NOTREADY);

				// Close dialogs while scope is still valid
				final IScope localScope = scope;
				if (localScope != null) {
					try {
						localScope.getGui().closeDialogs(localScope);
					} catch (final Exception e) {
						DEBUG.ERR("Error closing dialogs during disposal", e);
					}
				}

				GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NONE);
			} finally {
				// Stop accepting new commands
				acceptingCommands = false;

				// Signal execution thread to stop
				experimentAlive = false;

				// Release lock to unblock execution thread if it is waiting
				try {
					lock.release();
				} catch (final Exception e) {
					// Lock might already be released – that's fine
				}

				// Signal command thread to exit its take() loop
				if (commandThread != null && commandThread.isAlive()) {
					commands.offer(ExperimentCommand._CLOSE);
					try {
						commandThread.join(1000);
						if (commandThread.isAlive()) {
							DEBUG.OUT("Command thread did not terminate gracefully, interrupting...");
							commandThread.interrupt();
						}
					} catch (final InterruptedException e) {
						DEBUG.ERR("Interrupted while waiting for command thread termination", e);
						Thread.currentThread().interrupt();
					}
				}

				// Wait for execution thread to finish before nulling shared state
				if (executionThread != null && executionThread.isAlive()) {
					try {
						executionThread.join(1000);
						if (executionThread.isAlive()) {
							DEBUG.OUT("Execution thread did not terminate gracefully, interrupting...");
							executionThread.interrupt();
						}
					} catch (final InterruptedException e) {
						DEBUG.ERR("Interrupted while waiting for execution thread termination", e);
						Thread.currentThread().interrupt();
					}
				}

				// Null references only after threads have stopped to avoid NPE in step()
				scope = null;
				agent = null;
			}
		}
	}

	@Override
	public void close() {
		disposing = true;
		experiment.dispose(); // will call own dispose() later
	}

	/**
	 * Reports a runtime exception both to the errors view and the status bar, then closes the experiment. The error is
	 * reported <em>before</em> any disposal so that {@code isDisposing()} is still {@code false} when
	 * {@code runtimeError} and {@code errorStatus} are called (both check that flag).
	 *
	 * @param e
	 *            the throwable that caused the failure
	 */
	public void notifyExceptionAndCloseExperiment(final Throwable e) {
		final IScope localScope = scope; // capture before concurrent dispose() can null it
		if (e != null && localScope != null) {
			final GamaRuntimeException gre = GamaRuntimeException.create(e, localScope);
			// Report to the errors view BEFORE closing so that isDisposing() is still false
			localScope.getGui().runtimeError(localScope, gre);
			localScope.getGui().getStatus().errorStatus(gre);
		}
		GAMA.closeExperiment(experiment);
		GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NONE);
	}

	/**
	 * Cleans up any partially-created simulation, then reports the runtime exception to the Errors view, the status bar
	 * and opens the Errors view — all while keeping the experiment perspective open so the user can re-run.
	 *
	 * <p>
	 * This is used for initialisation-time errors (thrown from {@link #schedule}) where the simulation has not been
	 * fully created yet but the experiment agent and its perspective are still valid.
	 * </p>
	 *
	 * <p>
	 * The sequence is:
	 * </p>
	 * <ol>
	 * <li><b>Hide the launching overlay</b> — calls {@code gui.hideLaunchingOverlay()} to dismiss the Shell that covers
	 * the workbench during experiment launch. Without this call the overlay stays visible forever because it is
	 * normally hidden by {@code cleanAfterExperiment()}, which is only called on the success path.</li>
	 * <li><b>Dispose the partial simulation</b> — {@code agent.closeSimulations(false)} disposes any partial simulation
	 * while keeping the experiment perspective. Disposing before reporting ensures the subsequent error message
	 * overwrites the "disposing simulation N" status text.</li>
	 * <li><b>Report the error</b> — {@code runtimeError} queues it for the Errors view, {@code errorStatus} sets the
	 * status bar label, and {@code displayErrors} opens/activates the Errors view immediately.</li>
	 * <li><b>Set state to {@code NOTREADY}</b> — disables the Run/Step buttons while leaving Reload active. Calling
	 * {@code experiment.reload()} here would create an infinite loop (reload → open → schedule → init fails → reload →
	 * …), so we do not.</li>
	 * </ol>
	 *
	 * @param e
	 *            the throwable thrown during experiment initialisation
	 */
	protected void notifyExceptionAndReloadExperiment(final Throwable e) {
		final IScope localScope = scope;
		final GamaRuntimeException gre =
				e != null && localScope != null ? GamaRuntimeException.create(e, localScope) : null;

		// Step 1: dismiss the launching overlay.
		// It is normally closed by cleanAfterExperiment(), which is only reached on the
		// success path. On an init error we must close it explicitly here, otherwise the
		// overlay Shell covers the workbench forever.
		if (localScope != null) {
			try {
				localScope.getGui().hideLaunchingOverlay();
			} catch (final Throwable overlayError) {
				DEBUG.ERR("Error hiding launching overlay after init failure", overlayError);
			}
		}

		// Step 2: report the error BEFORE disposing, so the handler's queue and
		// cleanExceptions are populated before cleanAfterExperiment() is called
		// (from within closeSimulations → ExperimentOutputManager.dispose()).
		// cleanAfterExperiment() calls handler.stop() which, if called first, races
		// with displayLatestErrors and leaves the Errors view empty.
		if (gre != null && localScope != null) {
			// Show error text in the status bar.
			localScope.getGui().getStatus().errorStatus(gre);
			// Force-flush the handler's incoming queue → populates cleanExceptions
			// and opens/updates the Errors view right now, before cleanAfterExperiment
			// can interfere.
			localScope.getGui().displayLatestErrors();
		}

		// Step 3: dispose any partially-created simulation, keeping the experiment
		// perspective open (andLeaveExperimentPerspective = false).
		// NOTE: this triggers ExperimentOutputManager.dispose() → cleanAfterExperiment()
		// → handler.stop(). By then displayLatestErrors() has already populated
		// cleanExceptions, so the view content survives the stop.
		// DO NOT call experiment.reload() here — that creates an infinite loop.
		try {
			if (agent != null) { agent.closeSimulations(false); }
		} catch (final Throwable disposeError) {
			DEBUG.ERR("Error while disposing partial simulation after init failure", disposeError);
		}

		// Step 4: put the experiment into a stable not-started state.
		// In NOTREADY state the Run/Step buttons are disabled (isSimulationReady =
		// RUNNING || PAUSED only). The Reload button remains active.
		paused = true;
		GAMA.updateExperimentState(experiment, IExperimentStateListener.State.NOTREADY);
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
			} else if (agent instanceof IExperimentAgent.Test || agent.getSpecies().isAutorun()) {
				asynchronousStart();
			}
		} catch (final Throwable e) {
			// Any throwable during initialization (GamaRuntimeException, Error, etc.) is
			// reported to the errors view and the experiment is reloaded so the user remains
			// in the simulation perspective and can re-run rather than being dumped back to
			// the modelling perspective.
			if (scope != null && !scope.interrupted()) { notifyExceptionAndReloadExperiment(e); }
		}
	}

	/**
	 * Executes one simulation step.
	 *
	 * <p>
	 * This method is called repeatedly by the execution thread. It blocks when paused and executes a single step when
	 * running.
	 * </p>
	 *
	 * <h3>Optimizations:</h3>
	 * <ul>
	 * <li>Caches scope reference to reduce volatile field reads in hot path</li>
	 * <li>Uses proper logging instead of printStackTrace for better debugging</li>
	 * <li>Ensures previouslock is always released via finally block</li>
	 * </ul>
	 */
	protected void step() {
		// Block if paused - wait for START or STEP command to release lock.
		// If the execution thread is interrupted while waiting, return immediately
		// so it can exit the while(experimentAlive) loop cleanly.
		if (paused) {
			// dispose() sets experimentAlive = false BEFORE releasing the lock so that
			// the execution thread can exit cleanly without executing one extra step.
			if (!lock.acquire() || !experimentAlive) return;
		}

		// Cache scope reference to avoid repeated volatile reads
		final IScope currentScope = scope;

		// Early exit if scope is not initialized yet
		if (currentScope == null) {
			// Scope not yet initialized - this can happen during startup
			// Release previouslock to prevent deadlock in STEP command
			previouslock.release();
			return;
		}

		try {
			// Execute one simulation step
			if (!currentScope.step(agent).passed()) {
				// Step failed - mark scope for disposal and pause execution
				currentScope.setDisposeStatus();
				paused = true;
			}
		} catch (final RuntimeException e) {
			// Log exception with proper context instead of printStackTrace
			DEBUG.ERR(
					"Error during experiment step execution for " + (agent != null ? agent.getName() : "unknown agent"),
					e);

			// Optionally notify GUI of the error
			if (!disposing) {
				try {
					currentScope.getGui().getStatus().errorStatus(GamaRuntimeException.create(e, currentScope));
				} catch (final Exception guiError) {
					// GUI notification failed - just log it
					DEBUG.ERR("Failed to notify GUI of step error", guiError);
				}
			}

			// Pause execution on error to prevent error spam
			paused = true;
		} finally {
			// Always release previouslock to unblock STEP command
			// This ensures step-by-step execution can continue even if step fails
			previouslock.release();
		}
	}

}
