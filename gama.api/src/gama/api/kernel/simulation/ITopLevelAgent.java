/*******************************************************************************************************
 *
 * ITopLevelAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScopedStepable;
import gama.api.types.color.IColor;
import gama.api.ui.IOutputManager;
import gama.api.utils.random.IRandom;

/**
 * The Interface ITopLevelAgent.
 * 
 * <p>
 * Represents an agent at the top level of GAMA's agent hierarchy. Top-level agents have special capabilities beyond
 * regular agents, including clock management, output management, and action scheduling. This interface is implemented
 * by simulation agents, experiment agents, and the platform agent.
 * </p>
 * 
 * <h3>Top-Level Agent Hierarchy</h3>
 * <ul>
 * <li><b>Platform Agent:</b> The ultimate top-level agent representing the GAMA platform itself</li>
 * <li><b>Experiment Agent:</b> Manages experiments and their simulations</li>
 * <li><b>Simulation Agent:</b> Represents a running simulation instance</li>
 * </ul>
 * 
 * <h3>Core Capabilities</h3>
 * <ul>
 * <li><b>Time Management:</b> Each top-level agent has its own clock</li>
 * <li><b>Output Management:</b> Controls displays, monitors, and file outputs</li>
 * <li><b>Random Generation:</b> Manages independent random number generators</li>
 * <li><b>Action Scheduling:</b> Can schedule actions for execution at specific times</li>
 * <li><b>User Interaction:</b> Can be paused/held by user</li>
 * </ul>
 * 
 * <h3>Hierarchy Example</h3>
 * 
 * <pre>
 * Platform (ITopLevelAgent.Platform)
 *   └─ Experiment (IExperimentAgent extends ITopLevelAgent)
 *        ├─ Simulation 1 (ISimulationAgent extends ITopLevelAgent)
 *        │    ├─ Species populations
 *        │    └─ Individual agents
 *        └─ Simulation 2 (for batch experiments)
 *             ├─ Species populations
 *             └─ Individual agents
 * </pre>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <h4>1. Accessing Top-Level Information</h4>
 * 
 * <pre>
 * <code>
 * global {
 *     init {
 *         // Access experiment from simulation
 *         IExperimentAgent exp <- experiment;
 *         
 *         // Get simulation's own color
 *         rgb my_color <- color;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Scheduling Actions</h4>
 * 
 * <pre>
 * <code>
 * species manager {
 *     reflex schedule_work {
 *         // Schedule action to run after current cycle
 *         do post_one_shot_action {
 *             create worker number: 10;
 *         }
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Using the Clock</h4>
 * 
 * <pre>
 * <code>
 * global {
 *     reflex monitor {
 *         IClock clock <- self.getClock();
 *         write "Cycle: " + clock.cycle;
 *         write "Time: " + clock.time;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Java Usage</h3>
 * 
 * <pre>
 * <code>
 * ITopLevelAgent topAgent = ...;
 * 
 * // Time management
 * IClock clock = topAgent.getClock();
 * int cycle = clock.getCycle();
 * 
 * // Random generation
 * IRandom rng = topAgent.getRandomGenerator();
 * double randomValue = rng.next();
 * 
 * // Output management
 * IOutputManager outputs = topAgent.getOutputManager();
 * 
 * // Appearance
 * IColor color = topAgent.getColor();
 * 
 * // Action scheduling
 * topAgent.postOneShotAction(() -> {
 *     // Execute once
 * });
 * 
 * topAgent.postEndAction(() -> {
 *     // Execute at end of cycle
 * });
 * 
 * topAgent.postDisposeAction(() -> {
 *     // Execute when disposed
 * });
 * 
 * topAgent.executeAction(() -> {
 *     // Execute immediately
 * });
 * 
 * // User interaction
 * boolean onHold = topAgent.isOnUserHold();
 * topAgent.setOnUserHold(true);
 * 
 * // Hierarchy
 * IExperimentAgent exp = topAgent.getExperiment();
 * String family = topAgent.getFamilyName(); // "simulation", "experiment", or "platform"
 * </code>
 * </pre>
 * 
 * <h3>Action Scheduling Types</h3>
 * <table border="1">
 * <tr>
 * <th>Method</th>
 * <th>Execution Time</th>
 * <th>Repeat</th>
 * </tr>
 * <tr>
 * <td>executeAction</td>
 * <td>Immediately</td>
 * <td>Once</td>
 * </tr>
 * <tr>
 * <td>postOneShotAction</td>
 * <td>Next available slot</td>
 * <td>Once</td>
 * </tr>
 * <tr>
 * <td>postEndAction</td>
 * <td>End of current cycle</td>
 * <td>Once</td>
 * </tr>
 * <tr>
 * <td>postDisposeAction</td>
 * <td>When agent is disposed</td>
 * <td>Once</td>
 * </tr>
 * </table>
 * 
 * <h3>Platform Agent</h3>
 * <p>
 * The Platform inner interface represents the special top-level agent for the entire GAMA platform:
 * </p>
 * <ul>
 * <li>Exists outside any simulation or experiment</li>
 * <li>Provides workspace path and machine time</li>
 * <li>Manages preferences and global settings</li>
 * <li>Implements both ITopLevelAgent and IExpression</li>
 * </ul>
 * 
 * <h3>Family Names</h3>
 * <ul>
 * <li><b>"platform":</b> Platform agent</li>
 * <li><b>"experiment":</b> Experiment agent</li>
 * <li><b>"simulation":</b> Simulation agent</li>
 * </ul>
 * 
 * <h3>Implementation Notes</h3>
 * <ul>
 * <li>Extends both IMacroAgent (can contain agents) and IScopedStepable (can be stepped with scope)</li>
 * <li>Each top-level agent maintains its own independent clock</li>
 * <li>Random generators are seeded independently per top-level agent</li>
 * <li>User hold state allows pausing execution without stopping scheduler</li>
 * <li>Scheduled actions are queued and executed at appropriate times</li>
 * </ul>
 * 
 * @see ISimulationAgent
 * @see IExperimentAgent
 * @see IMacroAgent
 * @see IScopedStepable
 * @see IClock
 * @author drogoul
 * @since GAMA 1.6
 * @date 27 janv. 2016
 */
public interface ITopLevelAgent extends IMacroAgent, IScopedStepable {

	/**
	 * The Interface Platform.
	 */
	interface Platform extends ITopLevelAgent, IExpression {

		/** The Constant WORKSPACE_PATH. */
		String WORKSPACE_PATH = "workspace_path";

		/** The Constant MACHINE_TIME. */
		String MACHINE_TIME = "machine_time";

		/**
		 * Dispose.
		 */
		@Override
		default void dispose() {
			IExpression.super.dispose();
		}

		/**
		 *
		 */
		void restorePrefs();

		/**
		 * @param key
		 * @param value
		 */
		void savePrefToRestore(String key, Object value);
	}

	/**
	 * Gets the clock.
	 *
	 * @return the clock
	 */
	IClock getClock();

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	IColor getColor();

	/**
	 * Gets the random generator.
	 *
	 * @return the random generator
	 */
	IRandom getRandomGenerator();

	/**
	 * Gets the output manager.
	 *
	 * @return the output manager
	 */
	IOutputManager getOutputManager();

	/**
	 * Post end action.
	 *
	 * @param executable
	 *            the executable
	 */
	void postEndAction(IExecutable executable);

	/**
	 * Post dispose action.
	 *
	 * @param executable
	 *            the executable
	 */
	void postDisposeAction(IExecutable executable);

	/**
	 * Post one shot action.
	 *
	 * @param executable
	 *            the executable
	 */
	void postOneShotAction(IExecutable executable);

	/**
	 * Execute action.
	 *
	 * @param executable
	 *            the executable
	 */
	void executeAction(IExecutable executable);

	/**
	 * Checks if is on user hold.
	 *
	 * @return true, if is on user hold
	 */
	boolean isOnUserHold();

	/**
	 * Sets the on user hold.
	 *
	 * @param state
	 *            the new on user hold
	 */
	void setOnUserHold(boolean state);

	/**
	 * Gets the experiment.
	 *
	 * @return the experiment
	 */
	IExperimentAgent getExperiment();

	/**
	 * Gets the family name. Means either 'simulation', 'experiment' or 'platform'
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the family name
	 * @date 13 août 2023
	 */
	String getFamilyName();

	/**
	 * Checks if is platform.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is platform
	 * @date 3 sept. 2023
	 */
	default boolean isPlatform() { return false; }

	/**
	 * Checks if is experiment.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is experiment
	 * @date 3 sept. 2023
	 */
	default boolean isExperiment() { return false; }

	/**
	 * Checks if is simulation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is simulation
	 * @date 3 sept. 2023
	 */
	default boolean isSimulation() { return false; }

}
