/*******************************************************************************************************
 *
 * ISimulationAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import java.util.Map;

import gama.api.kernel.topology.IProjectionFactory;
import gama.api.runtime.scope.IScope;
import gama.api.types.date.IDate;
import gama.api.ui.IOutput;
import gama.api.ui.IOutputManager;
import gama.api.utils.SimulationLocal;
import gama.api.utils.random.IRandom;

/**
 * The Interface ISimulationAgent.
 * 
 * <p>
 * Represents a running simulation instance in GAMA. A simulation agent is the top-level agent that contains all other
 * agents and manages the simulation lifecycle, time, randomness, and outputs. In GAMA, an experiment can run multiple
 * simulations concurrently (for batch experiments or replication).
 * </p>
 * 
 * <h3>Core Responsibilities</h3>
 * <ul>
 * <li><b>Agent Container:</b> Hosts all species populations and their agents</li>
 * <li><b>Time Management:</b> Maintains clock, dates, and temporal state</li>
 * <li><b>Random Number Generation:</b> Provides consistent RNG with seed control</li>
 * <li><b>Output Management:</b> Manages displays, monitors, and file outputs</li>
 * <li><b>Scheduling:</b> Controls agent execution order and timing</li>
 * <li><b>Spatial Context:</b> Defines world geometry and projections</li>
 * </ul>
 * 
 * <h3>Simulation vs Experiment</h3>
 * <table border="1">
 * <tr>
 * <th>Aspect</th>
 * <th>Experiment</th>
 * <th>Simulation</th>
 * </tr>
 * <tr>
 * <td>Role</td>
 * <td>Defines simulation structure</td>
 * <td>Running instance of that structure</td>
 * </tr>
 * <tr>
 * <td>Count</td>
 * <td>One per model</td>
 * <td>Can have multiple (batch mode)</td>
 * </tr>
 * <tr>
 * <td>Lifespan</td>
 * <td>Entire Eclipse session</td>
 * <td>From start to end of run</td>
 * </tr>
 * <tr>
 * <td>Contains</td>
 * <td>Parameters, specifications</td>
 * <td>Actual agents and their state</td>
 * </tr>
 * </table>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <h4>1. Accessing Simulation Attributes</h4>
 * 
 * <pre>
 * <code>
 * global {
 *     reflex monitor {
 *         write "Cycle: " + cycle;
 *         write "Time: " + time;
 *         write "Current date: " + current_date;
 *         write "Seed: " + seed;
 *         write "Last step took: " + duration + " ms";
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Controlling Random Number Generation</h4>
 * 
 * <pre>
 * <code>
 * experiment myExp {
 *     float seed <- 42.0;  // Fixed seed for reproducibility
 *     string rng <- "mersenne";  // RNG algorithm
 * }
 * 
 * global {
 *     init {
 *         // All random operations use this seed
 *         create person number: 100 {
 *             age <- rnd(80);  // Reproducible with same seed
 *         }
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Working with Dates</h4>
 * 
 * <pre>
 * <code>
 * global {
 *     date starting_date <- date("2020-01-01");
 *     float step <- 1 #hour;
 *     
 *     reflex monitor_date {
 *         write "Current simulation date: " + current_date;
 *         // Advances by 1 hour each cycle
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>4. Managing Outputs</h4>
 * 
 * <pre>
 * <code>
 * experiment myExp {
 *     output {
 *         display map {
 *             species person;
 *         }
 *         
 *         monitor "Population" value: length(person);
 *     }
 * }
 * 
 * // Outputs are managed by the simulation agent
 * </code>
 * </pre>
 * 
 * <h4>5. Micro-Simulations</h4>
 * 
 * <pre>
 * <code>
 * // Simulations can contain other simulations
 * species meta_model {
 *     species sub_simulation parent: simulation {
 *         // Nested simulation with its own time and agents
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Java Usage</h3>
 * 
 * <pre>
 * <code>
 * ISimulationAgent simulation = ...;
 * 
 * // Time and dates
 * IDate currentDate = simulation.getCurrentDate();
 * IDate startDate = simulation.getStartingDate();
 * double timeStep = simulation.getTimeStep(scope);
 * Integer cycle = simulation.getCycle(scope);
 * 
 * // Random number generation
 * Double seed = simulation.getSeed();
 * IRandom rng = simulation.getRandomGenerator();
 * simulation.generateRandomGenerator(42.0, "mersenne");
 * 
 * // Outputs
 * simulation.addOutput(monitorOutput);
 * simulation.initOutputs();
 * IOutputManager outputs = simulation.getOutputManager();
 * 
 * // Spatial
 * IProjectionFactory projFactory = simulation.getProjectionFactory();
 * simulation.adoptTopologyOf(otherSimulation);
 * 
 * // Initialization
 * Map&lt;String, Object&gt; externalInits = simulation.getExternalInits();
 * simulation.setExternalInits(initValues);
 * 
 * // State
 * boolean isMicro = simulation.isMicroSimulation();
 * simulation.setScheduled(true);
 * </code>
 * </pre>
 * 
 * <h3>Lifecycle</h3>
 * <ol>
 * <li><b>Creation:</b> Simulation created from experiment specification</li>
 * <li><b>Initialization:</b> External inits applied, populations created, global init executed</li>
 * <li><b>Output Setup:</b> Displays and monitors initialized</li>
 * <li><b>Scheduling:</b> Simulation added to scheduler if toBeScheduled=true</li>
 * <li><b>Execution:</b> Cycles execute until stop condition or user intervention</li>
 * <li><b>Disposal:</b> Agents cleaned up, outputs closed, resources released</li>
 * </ol>
 * 
 * <h3>Special Attributes</h3>
 * <ul>
 * <li><b>DURATION:</b> Last cycle execution time (milliseconds)</li>
 * <li><b>TOTAL_DURATION:</b> Cumulative execution time</li>
 * <li><b>AVERAGE_DURATION:</b> Average cycle duration</li>
 * <li><b>TIME:</b> Elapsed model time (seconds)</li>
 * <li><b>CURRENT_DATE:</b> Current simulation date</li>
 * <li><b>STARTING_DATE:</b> Simulation start date</li>
 * <li><b>PAUSED:</b> Whether simulation is paused</li>
 * <li><b>USAGE:</b> RNG usage count</li>
 * </ul>
 * 
 * <h3>Implementation Notes</h3>
 * <ul>
 * <li>Each simulation has its own independent RNG for reproducibility</li>
 * <li>Micro-simulations can be nested within macro-agents</li>
 * <li>Outputs are owned by the simulation, not shared across simulations</li>
 * <li>External inits allow parameter passing from experiment to simulation</li>
 * <li>Topology can be adopted from another simulation for consistency</li>
 * </ul>
 * 
 * @see ITopLevelAgent
 * @see IExperimentAgent
 * @see IClock
 * @see IRandom
 * @see IOutputManager
 * @author drogoul
 * @since GAMA 1.0
 */
public interface ISimulationAgent extends ITopLevelAgent {

	/** The Constant DURATION. */
	String DURATION = "duration";

	/** The Constant TOTAL_DURATION. */
	String TOTAL_DURATION = "total_duration";

	/** The Constant AVERAGE_DURATION. */
	String AVERAGE_DURATION = "average_duration";

	/** The Constant TIME. */
	String TIME = "time";

	/** The Constant CURRENT_DATE. */
	String CURRENT_DATE = "current_date";

	/** The Constant STARTING_DATE. */
	String STARTING_DATE = "starting_date";

	/** The Constant PAUSED. */
	String PAUSED = "paused";

	/** The Constant USAGE. */
	String USAGE = "rng_usage";

	/**
	 * @return
	 */
	IDate getCurrentDate();

	/**
	 * @return
	 */
	IProjectionFactory getProjectionFactory();

	/**
	 * @param scope
	 * @return
	 */
	double getTimeStep(IScope scope);

	/**
	 * @return
	 */
	IDate getStartingDate();

	/**
	 * @return
	 */
	boolean isMicroSimulation();

	/**
	 * @return
	 */
	Double getSeed();

	/**
	 * @param monitorOutput
	 */
	void addOutput(IOutput monitorOutput);

	/**
	 * @return
	 */
	<T> Map<SimulationLocal<T>, T> getSimulationLocalMap();

	/**
	 * @param map
	 */
	<T> void setSimulationLocalMap(Map<SimulationLocal<T>, T> map);

	/**
	 * @param toBeScheduled
	 */
	void setScheduled(boolean toBeScheduled);

	/**
	 * @param originalSimulationOutputs
	 */
	void setOutputs(IOutputManager originalSimulationOutputs);

	/**
	 *
	 */
	void initOutputs();

	/**
	 * @param simulation
	 */
	void adoptTopologyOf(ISimulationAgent simulation);

	/**
	 * @param firstInitValues
	 */
	void setExternalInits(Map<String, Object> firstInitValues);

	/**
	 * @param randomUtils
	 */
	void setRandomGenerator(IRandom randomUtils);

	/**
	 * Generate random generator.
	 *
	 * @param seed
	 *            the seed
	 * @param rng
	 *            the rng
	 * @return the i random
	 */
	IRandom generateRandomGenerator(final Double seed, final String rng);

	/**
	 * @return
	 */
	Map<String, Object> getExternalInits();

	/**
	 * @param scope
	 * @return
	 */
	Integer getCycle(IScope scope);

	/**
	 * @return
	 */
	boolean getScheduled();

	/**
	 * @param scope
	 */
	Object _init_(IScope scope);

	/**
	 * @param double1
	 */
	void setSeed(Double double1);

	/**
	 * @param usageValue
	 */
	void setUsage(Integer usageValue);

	/**
	 * @return
	 */
	String getRng();

	/**
	 * @return
	 */
	Integer getUsage();

}
