/*******************************************************************************************************
 *
 * IExperimentStateListener.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import gama.api.kernel.species.IExperimentSpecies;

/**
 * Observer interface for monitoring and reacting to experiment state changes in GAMA.
 * 
 * <p>
 * This interface implements the Observer pattern to notify interested parties (typically the UI) when an experiment
 * transitions between different execution states. It enables the GAMA interface to update controls, displays, and
 * status indicators in response to experiment lifecycle events.
 * </p>
 * 
 * <h3>Observer Pattern</h3>
 * <ul>
 * <li><b>Subject:</b> {@link IExperimentSpecies} and experiment controllers</li>
 * <li><b>Observer:</b> UI components implementing this interface</li>
 * <li><b>Events:</b> State transitions (NONE, NOTREADY, PAUSED, RUNNING, FINISHED)</li>
 * <li><b>Notification:</b> Via {@link #updateStateTo(IExperimentSpecies, State)}</li>
 * </ul>
 * 
 * <h3>Experiment States</h3>
 * <p>
 * The {@link State} enum defines the experiment lifecycle:
 * </p>
 * 
 * <table border="1">
 * <tr>
 * <th>State</th>
 * <th>Meaning</th>
 * <th>UI Impact</th>
 * </tr>
 * <tr>
 * <td>NONE</td>
 * <td>Not launched or already closed</td>
 * <td>All controls disabled, clean state</td>
 * </tr>
 * <tr>
 * <td>NOTREADY</td>
 * <td>Opening/initializing</td>
 * <td>Show loading indicator, disable controls</td>
 * </tr>
 * <tr>
 * <td>PAUSED</td>
 * <td>Open but execution stopped</td>
 * <td>Enable step/start, show pause icon</td>
 * </tr>
 * <tr>
 * <td>RUNNING</td>
 * <td>Actively executing cycles</td>
 * <td>Enable pause/stop, show play icon</td>
 * </tr>
 * <tr>
 * <td>FINISHED</td>
 * <td>Completed (batch experiments)</td>
 * <td>Show completion status, disable step</td>
 * </tr>
 * </table>
 * 
 * <h3>Experiment Types</h3>
 * <p>
 * The {@link Type} enum categorizes experiments for UI customization:
 * </p>
 * 
 * <table border="1">
 * <tr>
 * <th>Type</th>
 * <th>Description</th>
 * <th>UI Features</th>
 * </tr>
 * <tr>
 * <td>NONE</td>
 * <td>Unknown or not launched</td>
 * <td>Default/minimal UI</td>
 * </tr>
 * <tr>
 * <td>REGULAR</td>
 * <td>Standard GUI experiment</td>
 * <td>Full controls: play, pause, step, reload</td>
 * </tr>
 * <tr>
 * <td>BATCH</td>
 * <td>Automated parameter exploration</td>
 * <td>Progress bar, stop button, results view</td>
 * </tr>
 * <tr>
 * <td>RECORD</td>
 * <td>Recording-enabled experiment</td>
 * <td>Step back button, timeline controls</td>
 * </tr>
 * <tr>
 * <td>TEST</td>
 * <td>Automated testing</td>
 * <td>Test results, assertions, pass/fail status</td>
 * </tr>
 * </table>
 * 
 * <h3>State Transition Diagram</h3>
 * 
 * <pre>
 * NONE
 *   |
 *   | (open experiment)
 *   v
 * NOTREADY
 *   |
 *   | (initialization complete)
 *   v
 * PAUSED ←→ RUNNING
 *   |         |
 *   |         | (stop condition met)
 *   |         v
 *   |      FINISHED
 *   |         |
 *   +----+----+
 *        | (close)
 *        v
 *      NONE
 * </pre>
 * 
 * <h3>Eclipse Properties</h3>
 * <p>
 * State changes are propagated via Eclipse properties for loose coupling:
 * </p>
 * <ul>
 * <li><b>{@link #EXPERIMENT_RUNNING_STATE}:</b> Broadcasts state changes (State enum value)</li>
 * <li><b>{@link #EXPERIMENT_TYPE}:</b> Announces experiment type (Type enum value)</li>
 * <li><b>{@link #EXPERIMENT_STEPBACK}:</b> Notifies step-back capability changes</li>
 * </ul>
 * 
 * <h3>Usage in GAMA</h3>
 * 
 * <h4>1. Implementing a Listener (UI Component)</h4>
 * 
 * <pre>
 * <code>
 * public class ExperimentToolbar implements IExperimentStateListener {
 *     
 *     private Button playButton;
 *     private Button pauseButton;
 *     private Button stepButton;
 *     private Button stepBackButton;
 *     
 *     public void updateStateTo(IExperimentSpecies experiment, State state) {
 *         Display.getDefault().asyncExec(() -> {
 *             switch (state) {
 *                 case NONE:
 *                     // Experiment closed
 *                     disableAllControls();
 *                     break;
 *                     
 *                 case NOTREADY:
 *                     // Initializing
 *                     showLoadingIndicator();
 *                     disableAllControls();
 *                     break;
 *                     
 *                 case PAUSED:
 *                     // Paused - enable start/step
 *                     playButton.setEnabled(true);
 *                     pauseButton.setEnabled(false);
 *                     stepButton.setEnabled(true);
 *                     stepBackButton.setEnabled(experiment.canStepBack());
 *                     break;
 *                     
 *                 case RUNNING:
 *                     // Running - enable pause/stop
 *                     playButton.setEnabled(false);
 *                     pauseButton.setEnabled(true);
 *                     stepButton.setEnabled(false);
 *                     stepBackButton.setEnabled(false);
 *                     break;
 *                     
 *                 case FINISHED:
 *                     // Completed
 *                     showCompletionStatus();
 *                     disableStepControls();
 *                     break;
 *             }
 *         });
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Notifying State Changes (Controller)</h4>
 * 
 * <pre>
 * <code>
 * // In experiment controller
 * public void processStart(boolean andWait) {
 *     paused = false;
 *     lock.release();
 *     
 *     // Notify observers
 *     GAMA.updateExperimentState(experiment, IExperimentStateListener.State.RUNNING);
 * }
 * 
 * public void processPause(boolean andWait) {
 *     paused = true;
 *     
 *     // Notify observers  
 *     GAMA.updateExperimentState(experiment, IExperimentStateListener.State.PAUSED);
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Type-Specific UI Customization</h4>
 * 
 * <pre>
 * <code>
 * public void setupUIForExperiment(IExperimentSpecies experiment) {
 *     Type type = experiment.isBatch() ? Type.BATCH :
 *                 experiment.isRecord() ? Type.RECORD :
 *                 experiment.isTest() ? Type.TEST :
 *                 Type.REGULAR;
 *     
 *     switch (type) {
 *         case BATCH:
 *             showBatchProgressBar();
 *             hideStepControls();
 *             break;
 *         case RECORD:
 *             showTimelineControls();
 *             enableStepBackButton();
 *             break;
 *         case TEST:
 *             showTestResults();
 *             showAssertionPanel();
 *             break;
 *         case REGULAR:
 *             showStandardControls();
 *             break;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Step Back Support</h3>
 * <p>
 * Special constants for step-back capability:
 * </p>
 * <ul>
 * <li><b>{@link #CAN_STEP_BACK}:</b> Property value when step-back is available</li>
 * <li><b>{@link #CANNOT_STEP_BACK}:</b> Property value when step-back is not available</li>
 * </ul>
 * 
 * <pre>
 * <code>
 * // Update step-back capability
 * String stepBackState = experiment.canStepBack() ? 
 *     IExperimentStateListener.CAN_STEP_BACK :
 *     IExperimentStateListener.CANNOT_STEP_BACK;
 *     
 * propertyService.setProperty(
 *     IExperimentStateListener.EXPERIMENT_STEPBACK, 
 *     stepBackState
 * );
 * </code>
 * </pre>
 * 
 * <h3>Thread Safety</h3>
 * <p>
 * State updates may come from background threads. UI implementations should use appropriate threading mechanisms:
 * </p>
 * <ul>
 * <li><b>Eclipse/SWT:</b> {@code Display.asyncExec()} or {@code Display.syncExec()}</li>
 * <li><b>Swing:</b> {@code SwingUtilities.invokeLater()}</li>
 * <li><b>JavaFX:</b> {@code Platform.runLater()}</li>
 * </ul>
 * 
 * <h3>Best Practices</h3>
 * <ul>
 * <li>Update UI asynchronously to avoid blocking simulation thread</li>
 * <li>Check if experiment is still valid before updating UI</li>
 * <li>Handle rapid state changes gracefully (debounce if needed)</li>
 * <li>Provide visual feedback for all state transitions</li>
 * <li>Consider accessibility in state-dependent UI changes</li>
 * </ul>
 * 
 * @see IExperimentSpecies
 * @see IExperimentController
 * @see State
 * @see Type
 * @author drogoul
 * @since 14 déc. 2011
 */
public interface IExperimentStateListener {

    /** The Constant SIMULATION_RUNNING_STATE. */
    String EXPERIMENT_RUNNING_STATE = "gama.ui.experiment.SimulationRunningState";

    /** The Constant SIMULATION_TYPE. */
    String EXPERIMENT_TYPE = "gama.ui.experiment.SimulationType";

    /** The Constant SIMULATION_STEPBACK. */
    String EXPERIMENT_STEPBACK = "gama.ui.experiment.SimulationStepBack";

    /**
     * The Enum State.
     *
     * @author Alexis Drogoul (alexis.drogoul@ird.fr)
     * @date 26 oct. 2023
     */
    public enum State {
	/** The PAUSED state. The experiment is open and has been paused. */
	PAUSED,
	/**
	 * The FINISHED state. The experiment is finished but not closed. Used
	 * at the end of batch experiments
	 */
	FINISHED,
	/** The RUNNING state. The experiment is open and not paused. */
	RUNNING,
	/**
	 * The NOTREADY state. The experiment has been opened but is still
	 * initializing. If it is set to autorun, the next state becomes
	 * RUNNING, otherwise PAUSED.
	 */
	NOTREADY,
	/**
	 * The NONE state. The experiment has not been launched yet or is
	 * already closed.
	 */
	NONE;
    }

    /**
     * The Enum Type. Used to modify the UI depending on what type of experiment
     * is displayed.
     *
     * @author Alexis Drogoul (alexis.drogoul@ird.fr)
     * @date 3 nov. 2023
     */
    public enum Type {

	/**
	 * The NONE type. This type of experiment is unknown -- indicates that
	 * the experiment is not launched yet or already closed
	 */
	NONE,

	/**
	 * The BATCH type. This experiment automatically runs multiple
	 * simulations for exploring scenarios or optimizing parameters
	 */
	BATCH,

	/**
	 * The RECORD type. This experiment can record its states and play them
	 * back.
	 */
	RECORD,

	/** The REGULAR type. The classical experiment type. */
	REGULAR,

	/** TEST. This experiment is a specialized BATCH for running tests */

	TEST
    }

    /** The Constant CANNOT_STEP_BACK. */
    String CANNOT_STEP_BACK = "CANNOT_STEP_BACK";

    /** The Constant CAN_STEP_BACK. */
    String CAN_STEP_BACK = "CAN_STEP_BACK";

    /**
     * Change the UI state based on the state of the simulation (NONE, PAUSED,
     * RUNNING, FINISHED or NOTREADY)
     */
    void updateStateTo(IExperimentSpecies experiment, final State state);

}