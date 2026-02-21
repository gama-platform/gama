/*******************************************************************************************************
 *
 * IExperimentRecorder.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

/**
 * Interface for recording and restoring simulation states in GAMA experiments.
 * 
 * <p>
 * This interface enables the "step back" functionality in GAMA, allowing users to rewind simulation execution to
 * previous states. It defines the contract for implementations that capture simulation snapshots and restore them on
 * demand.
 * </p>
 * 
 * <h3>Core Functionality</h3>
 * <ul>
 * <li><b>Record:</b> Capture current simulation state as a snapshot</li>
 * <li><b>Restore:</b> Rewind simulation to a previously recorded state</li>
 * <li><b>Check:</b> Determine if stepping backward is possible</li>
 * </ul>
 * 
 * <h3>Use Cases</h3>
 * <p>
 * Recording simulation states enables:
 * </p>
 * <ul>
 * <li><b>Debugging:</b> Step backward to investigate unexpected behavior</li>
 * <li><b>Exploration:</b> Try different paths from the same starting point</li>
 * <li><b>Analysis:</b> Compare states at different time points</li>
 * <li><b>Teaching:</b> Demonstrate simulation dynamics by stepping forward and backward</li>
 * </ul>
 * 
 * <h3>GAML Usage</h3>
 * <p>
 * Enable recording in experiment declaration:
 * </p>
 * 
 * <pre>
 * <code>
 * experiment myExp type: gui record: true {
 *     // When record: true, GAMA automatically:
 *     // 1. Creates a recorder for this experiment
 *     // 2. Calls record() after each cycle
 *     // 3. Enables the "Step Back" button in UI
 *     
 *     parameter "population" var: nb_people;
 *     
 *     output {
 *         display map { species person; }
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Typical Workflow</h3>
 * <ol>
 * <li><b>Initialization:</b> Recorder created when experiment starts with record: true</li>
 * <li><b>Recording:</b> After each simulation step, {@link #record(ISimulationAgent)} is called</li>
 * <li><b>Storage:</b> Implementation stores a snapshot of the simulation state</li>
 * <li><b>User Action:</b> User clicks "Step Back" or calls programmatic step back</li>
 * <li><b>Restoration:</b> {@link #restore(ISimulationAgent)} loads previous state</li>
 * <li><b>Continue:</b> Simulation can continue from restored state</li>
 * </ol>
 * 
 * <h3>Implementation Considerations</h3>
 * <p>
 * When implementing this interface, consider:
 * </p>
 * 
 * <h4>What to Record</h4>
 * <ul>
 * <li>Agent states (attributes, locations, etc.)</li>
 * <li>Population structures (agent lists, counts)</li>
 * <li>Global variables</li>
 * <li>Simulation clock state</li>
 * <li>Random number generator state</li>
 * <li>Spatial structures (grids, graphs, topologies)</li>
 * </ul>
 * 
 * <h4>Storage Strategies</h4>
 * <ul>
 * <li><b>Full snapshots:</b> Copy entire simulation state (memory intensive)</li>
 * <li><b>Differential:</b> Only store changes from previous state</li>
 * <li><b>Circular buffer:</b> Limit number of stored states</li>
 * <li><b>Compression:</b> Reduce memory footprint</li>
 * </ul>
 * 
 * <h4>Performance Trade-offs</h4>
 * <ul>
 * <li><b>Memory vs. Time:</b> More states = more memory, faster restoration</li>
 * <li><b>Copy depth:</b> Deep copy = safer but slower</li>
 * <li><b>Buffer size:</b> Limits how far back you can step</li>
 * </ul>
 * 
 * <h3>Example Implementation</h3>
 * 
 * <pre>
 * <code>
 * public class SimpleRecorder implements IExperimentRecorder {
 *     private final Deque&lt;SimulationSnapshot&gt; snapshots = new LinkedList&lt;&gt;();
 *     private final int maxSnapshots = 100;
 *     
 *     public void record(ISimulationAgent sim) {
 *         // Create deep copy of simulation state
 *         SimulationSnapshot snapshot = new SimulationSnapshot(sim);
 *         snapshots.push(snapshot);
 *         
 *         // Limit memory usage
 *         if (snapshots.size() > maxSnapshots) {
 *             snapshots.removeLast();
 *         }
 *     }
 *     
 *     public void restore(ISimulationAgent sim) {
 *         if (!canStepBack(sim)) return;
 *         
 *         // Remove current state
 *         snapshots.pop();
 *         
 *         // Get previous state
 *         SimulationSnapshot previous = snapshots.peek();
 *         
 *         // Restore simulation from snapshot
 *         previous.restoreTo(sim);
 *     }
 *     
 *     public boolean canStepBack(ISimulationAgent sim) {
 *         return snapshots.size() > 1;  // Need at least 2: current + previous
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Method Details</h3>
 * 
 * <h4>{@link #record(ISimulationAgent)}</h4>
 * <p>
 * Called automatically after each simulation step when recording is enabled. Should:
 * </p>
 * <ul>
 * <li>Capture complete simulation state</li>
 * <li>Store in efficient data structure</li>
 * <li>Manage memory (e.g., limit buffer size)</li>
 * <li>Be fast enough not to slow down simulation significantly</li>
 * </ul>
 * 
 * <h4>{@link #restore(ISimulationAgent)}</h4>
 * <p>
 * Called when user steps backward. Should:
 * </p>
 * <ul>
 * <li>Validate that previous state exists</li>
 * <li>Remove current state from buffer</li>
 * <li>Load and apply previous state</li>
 * <li>Update simulation clock appropriately</li>
 * </ul>
 * 
 * <h4>{@link #canStepBack(ISimulationAgent)}</h4>
 * <p>
 * Determines UI state (Step Back button enabled/disabled). Should return:
 * </p>
 * <ul>
 * <li><b>true:</b> If at least one previous state is available</li>
 * <li><b>false:</b> If at beginning of simulation or buffer empty</li>
 * </ul>
 * 
 * <h3>Integration with Experiment</h3>
 * <p>
 * The experiment agent checks if recording is enabled:
 * </p>
 * 
 * <pre>
 * <code>
 * if (experiment.isRecord()) {
 *     IExperimentRecorder recorder = experiment.getRecorder();
 *     recorder.record(simulation);
 * }
 * </code>
 * </pre>
 * 
 * <h3>UI Integration</h3>
 * <p>
 * The GAMA UI uses {@link #canStepBack(ISimulationAgent)} to:
 * </p>
 * <ul>
 * <li>Enable/disable the "Step Back" toolbar button</li>
 * <li>Update the {@link IExperimentStateListener#CAN_STEP_BACK} state</li>
 * <li>Show/hide step back menu items</li>
 * </ul>
 * 
 * <h3>Memory Management</h3>
 * <p>
 * Best practices for managing recorder memory:
 * </p>
 * <ul>
 * <li>Implement circular buffer with configurable size</li>
 * <li>Provide option to clear history</li>
 * <li>Consider weak references for large objects</li>
 * <li>Allow disabling recording during intensive computation</li>
 * </ul>
 * 
 * <h3>Thread Safety</h3>
 * <p>
 * Implementations should be thread-safe if simulations can be accessed from multiple threads (e.g., batch
 * experiments).
 * </p>
 * 
 * @see IExperimentAgent#isRecord()
 * @see IExperimentAgent#canStepBack()
 * @see IExperimentStateListener
 * @see ISimulationAgent
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 août 2023
 */
public interface IExperimentRecorder {

	/**
	 * Record.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the sim
	 * @date 8 août 2023
	 */
	void record(ISimulationAgent sim);

	/**
	 * Restore.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the sim
	 * @date 8 août 2023
	 */
	void restore(ISimulationAgent sim);

	/**
	 * Can step back.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 * @return true, if successful
	 * @date 9 août 2023
	 */
	boolean canStepBack(ISimulationAgent sim);

}