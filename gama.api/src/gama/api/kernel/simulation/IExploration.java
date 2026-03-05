/*******************************************************************************************************
 *
 * IExploration.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import java.util.List;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.symbols.ISymbol;
import gama.api.runtime.scope.IScope;

/**
 * Interface for parameter exploration and optimization strategies in GAMA batch experiments.
 *
 * <p>
 * This interface defines the contract for exploration methods that systematically vary experiment parameters to explore
 * the parameter space, optimize outcomes, or conduct systematic sensitivity analysis. It is central to GAMA's batch
 * experimentation capabilities.
 * </p>
 *
 * <h3>Purpose</h3>
 * <p>
 * Explorations enable automated execution of multiple simulation runs with different parameter combinations to:
 * </p>
 * <ul>
 * <li><b>Explore:</b> Systematically sample the parameter space</li>
 * <li><b>Optimize:</b> Find parameter values that maximize/minimize objective functions</li>
 * <li><b>Analyze:</b> Understand parameter sensitivity and interactions</li>
 * <li><b>Validate:</b> Test model behavior across parameter ranges</li>
 * </ul>
 *
 * <h3>Exploration Types</h3>
 * <p>
 * GAMA provides several built-in exploration methods:
 * </p>
 *
 * <table border="1">
 * <tr>
 * <th>Method</th>
 * <th>Strategy</th>
 * <th>Fitness-Based</th>
 * <th>Use Case</th>
 * </tr>
 * <tr>
 * <td>Exhaustive</td>
 * <td>Test all combinations</td>
 * <td>No</td>
 * <td>Small parameter spaces, thorough analysis</td>
 * </tr>
 * <tr>
 * <td>Factorial</td>
 * <td>Multi-level factorial design</td>
 * <td>No</td>
 * <td>Experimental design, interaction effects</td>
 * </tr>
 * <tr>
 * <td>Latin Hypercube</td>
 * <td>Space-filling SAMPLING</td>
 * <td>No</td>
 * <td>Large spaces, uniform coverage</td>
 * </tr>
 * <tr>
 * <td>Sobol</td>
 * <td>Quasi-random sequences</td>
 * <td>No</td>
 * <td>Sensitivity analysis, low-discrepancy</td>
 * </tr>
 * <tr>
 * <td>Genetic Algorithm</td>
 * <td>Evolutionary optimization</td>
 * <td>Yes</td>
 * <td>Multi-objective optimization</td>
 * </tr>
 * <tr>
 * <td>Simulated Annealing</td>
 * <td>Probabilistic optimization</td>
 * <td>Yes</td>
 * <td>Global optimization, avoid local optima</td>
 * </tr>
 * <tr>
 * <td>Tabu Search</td>
 * <td>Local search with memory</td>
 * <td>Yes</td>
 * <td>Combinatorial optimization</td>
 * </tr>
 * <tr>
 * <td>PSO</td>
 * <td>Particle swarm optimization</td>
 * <td>Yes</td>
 * <td>Continuous optimization, fast convergence</td>
 * </tr>
 * </table>
 *
 * <h3>GAML Usage</h3>
 *
 * <h4>1. Exhaustive Exploration</h4>
 *
 * <pre>
 * <code>
 * experiment batch_exploration type: batch {
 *     parameter "Population" var: nb_people among: [100, 500, 1000];
 *     parameter "Threshold" var: threshold min: 0.1 max: 0.9 step: 0.2;
 *
 *     method exhaustive;  // Test all combinations
 *
 *     reflex save_results {
 *         save [nb_people, threshold, mean(person.age)]
 *              to: "results.csv" type: csv;
 *     }
 * }
 * </code>
 * </pre>
 *
 * <h4>2. Genetic Algorithm Optimization</h4>
 *
 * <pre>
 * <code>
 * experiment optimize type: batch {
 *     parameter "Resource allocation" var: resources min: 0.0 max: 1.0;
 *     parameter "Growth rate" var: growth min: 0.01 max: 0.1;
 *
 *     method genetic
 *         minimize: total_cost
 *         maximize: total_benefit
 *         population: 50
 *         crossover: 0.7
 *         mutation: 0.1
 *         generations: 100;
 * }
 * </code>
 * </pre>
 *
 * <h4>3. Factorial Design</h4>
 *
 * <pre>
 * <code>
 * experiment factorial_design type: batch {
 *     parameter "Factor A" var: factorA among: [low, medium, high];
 *     parameter "Factor B" var: factorB among: [0, 1];
 *
 *     method factorial
 *         levels: 3  // Number of levels per parameter
 *         sample: 27;  // 3^3 combinations
 * }
 * </code>
 * </pre>
 *
 * <h4>4. From File or List</h4>
 *
 * <pre>
 * <code>
 * experiment custom_scenarios type: batch {
 *     parameter "Config" var: config_name among: ["scenario1", "scenario2", "scenario3"];
 *
 *     method explicit
 *         from: "parameter_sets.csv";  // Load combinations from file
 * }
 * </code>
 * </pre>
 *
 * <h3>Interface Methods</h3>
 *
 * <h4>{@link #initializeFor(IScope, IExperimentAgent.Batch)}</h4>
 * <p>
 * Called once before exploration begins. Should:
 * </p>
 * <ul>
 * <li>Validate exploration parameters</li>
 * <li>Initialize data structures</li>
 * <li>Generate parameter combinations (for SAMPLING methods)</li>
 * <li>Setup optimization algorithms (for fitness-based methods)</li>
 * </ul>
 *
 * <h4>{@link #addParametersTo(List, IExperimentAgent.Batch)}</h4>
 * <p>
 * Populates the list of parameters to explore. Should:
 * </p>
 * <ul>
 * <li>Extract parameter definitions from experiment</li>
 * <li>Add them to the provided list</li>
 * <li>Configure parameter ranges and values</li>
 * </ul>
 *
 * <h4>{@link #run(IScope)}</h4>
 * <p>
 * Executes the exploration strategy. Should:
 * </p>
 * <ul>
 * <li>Iterate through parameter combinations</li>
 * <li>Create and run simulations for each combination</li>
 * <li>Collect results and fitness values</li>
 * <li>Update optimization state (for fitness-based methods)</li>
 * <li>Report progress</li>
 * </ul>
 *
 * <h4>{@link #isFitnessBased()}</h4>
 * <p>
 * Indicates whether the exploration uses fitness functions for optimization.
 * </p>
 * <ul>
 * <li><b>true:</b> GA, PSO, Simulated Annealing, Tabu Search</li>
 * <li><b>false:</b> Exhaustive, Factorial, Latin Hypercube, Sobol</li>
 * </ul>
 *
 * <h4>{@link #getOutputs()}</h4>
 * <p>
 * Returns the expression defining requested outputs/fitness values.
 * </p>
 *
 * <h3>Constants</h3>
 * <ul>
 * <li><b>{@link #SAMPLING}:</b> Property name for SAMPLING method ("SAMPLING")</li>
 * <li><b>{@link #SAMPLE_SIZE}:</b> Property name for sample size ("sample")</li>
 * <li><b>{@link #SAMPLE_FACTORIAL}:</b> Identifier for factorial SAMPLING</li>
 * <li><b>{@link #DEFAULT_FACTORIAL}:</b> Default factorial levels (9)</li>
 * <li><b>{@link #NB_LEVELS}:</b> Property name for number of levels</li>
 * <li><b>{@link #ITERATIONS}:</b> Property name for iteration count</li>
 * <li><b>{@link #FROM_FILE}:</b> Identifier for file-based parameter sets</li>
 * <li><b>{@link #FROM_LIST}:</b> Identifier for list-based parameter sets</li>
 * <li><b>{@link #DEFAULT_SAMPLING}:</b> Default method name ("Exhaustive")</li>
 * </ul>
 *
 * <h3>Implementation Example</h3>
 *
 * <pre>
 * <code>
 * public class RandomExploration implements IExploration {
 *     private int sampleSize = 100;
 *     private List&lt;IParameter.Batch&gt; parameters;
 *
 *     public void initializeFor(IScope scope, IExperimentAgent.Batch agent) {
 *         // Get sample size from experiment facets
 *         sampleSize = getFacetValue(SAMPLE_SIZE, 100);
 *         parameters = new ArrayList&lt;&gt;();
 *     }
 *
 *     public void addParametersTo(List&lt;IParameter.Batch&gt; exp, IExperimentAgent.Batch agent) {
 *         parameters = exp;
 *     }
 *
 *     public void run(IScope scope) {
 *         for (int i = 0; i &lt; sampleSize; i++) {
 *             // Generate random parameter values
 *             Map&lt;String, Object&gt; values = new HashMap&lt;&gt;();
 *             for (IParameter.Batch param : parameters) {
 *                 values.put(param.getName(), param.getRandomValue(scope));
 *             }
 *
 *             // Create and run simulation
 *             ISimulationAgent sim = agent.createSimulation(values, true);
 *             sim.step(scope);
 *
 *             // Collect results
 *             collectResults(sim);
 *         }
 *     }
 *
 *     public boolean isFitnessBased() {
 *         return false;  // Pure SAMPLING, no optimization
 *     }
 *
 *     public IExpression getOutputs() {
 *         return null;  // No specific outputs needed
 *     }
 * }
 * </code>
 * </pre>
 *
 * <h3>Parameter Space</h3>
 * <p>
 * Parameters define the exploration space:
 * </p>
 * <ul>
 * <li><b>Discrete:</b> {@code among: [value1, value2, ...]}</li>
 * <li><b>Continuous:</b> {@code min: x max: y step: z}</li>
 * <li><b>Mixed:</b> Some discrete, some continuous</li>
 * </ul>
 *
 * <h3>Best Practices</h3>
 * <ul>
 * <li>Report progress for long-running explorations</li>
 * <li>Allow early termination (check scope interruption)</li>
 * <li>Validate parameter constraints before running</li>
 * <li>Save results incrementally (don't wait until end)</li>
 * <li>Handle simulation failures gracefully</li>
 * <li>Provide meaningful stopping criteria for optimization</li>
 * </ul>
 *
 * <h3>Performance Considerations</h3>
 * <ul>
 * <li>Exhaustive exploration grows exponentially with parameters</li>
 * <li>Use SAMPLING methods for large parameter spaces</li>
 * <li>Consider parallel execution for independent simulations</li>
 * <li>Balance exploration depth vs. computational cost</li>
 * </ul>
 *
 * @see IExperimentAgent.Batch
 * @see IParameter.Batch
 * @see ISymbol
 * @author drogoul
 * @since 26 déc. 2011
 */
public interface IExploration extends ISymbol {// , Runnable {

	/** The annealing. */
	String ANNEALING = "annealing";

	/** The beta^d coefficient */
	String BETAD = "betad";

	/** The exploration. */
	String EXPLORATION = "exploration";

	/** The genetic. */
	String GENETIC = "genetic";

	/** The hill climbing. */
	String HILL_CLIMBING = "hill_climbing";

	/** The Morris method */
	String MORRIS = "morris";

	/** The pso. */
	String PSO = "pso";

	/** The reactive tabu. */
	String REACTIVE_TABU = "reactive_tabu";

	/** The sobol exploration method */
	String SOBOL = "sobol";

	/** The Stochasticity Analysis */
	String STO = "stochanalyse";

	/** The tabu. */
	String TABU = "tabu";

	/** The methods. */
	String[] METHODS =
			{ GENETIC, ANNEALING, HILL_CLIMBING, TABU, REACTIVE_TABU, EXPLORATION, PSO, SOBOL, MORRIS, STO, BETAD };

	/** The Constant Method */
	String SAMPLING = "SAMPLING";

	/** The Constant SAMPLE_SIZE */
	String SAMPLE_SIZE = "sample";

	/** The factorial SAMPLING */
	String SAMPLE_FACTORIAL = "factorial";

	/** The Constant DEFAULT_FACTORIAL. */
	int DEFAULT_FACTORIAL = 9;

	/** The Constant NB_LEVELS */
	String NB_LEVELS = "levels";

	/** The Constant ITERATIONS */
	String ITERATIONS = "iterations";

	/** The Constant FROM_FILE. */
	String FROM_FILE = "FROMFILE";

	/** The Constant FROM_LIST. */
	String FROM_LIST = "FROMLIST";

	/** The Constant DEFAULT_SAMPLING */
	String DEFAULT_SAMPLING = "Exhaustive";

	/**
	 * Initialize for.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void initializeFor(IScope scope, final IExperimentAgent.Batch agent) throws GamaRuntimeException;

	/**
	 * Adds the parameters to.
	 *
	 * @param exp
	 *            the exp
	 * @param agent
	 *            the agent
	 */
	void addParametersTo(final List<IParameter.Batch> exp, IExperimentAgent.Batch agent);

	/**
	 * Run.
	 *
	 * @param scope
	 *            the scope
	 */
	void run(IScope scope);

	/**
	 * If the exploration is based on the optimization of a fitness or not
	 *
	 * @return {@link Boolean}, true if based on fitness, false otherwise
	 */
	boolean isFitnessBased();

	/**
	 * The expression that represents the requested outputs
	 *
	 * @return
	 */
	IExpression getOutputs();

}