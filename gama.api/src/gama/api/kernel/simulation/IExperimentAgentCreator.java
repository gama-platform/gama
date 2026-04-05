/*******************************************************************************************************
 *
 * IExperimentAgentCreator.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import gama.api.additions.GamlAddition;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;

/**
 * Factory interface for creating experiment agent instances in GAMA.
 * 
 * <p>
 * This functional interface defines the factory pattern for experiment agent instantiation. It allows different
 * experiment types (GUI, batch, test, headless) to provide custom agent implementations while maintaining a uniform
 * creation API.
 * </p>
 * 
 * <h3>Factory Pattern</h3>
 * <p>
 * The interface follows the Factory Method design pattern:
 * </p>
 * <ul>
 * <li><b>Product:</b> {@link IExperimentAgent} instances</li>
 * <li><b>Creator:</b> Implementations of this interface</li>
 * <li><b>Context:</b> Population and index determine which agent to create</li>
 * </ul>
 * 
 * <h3>Usage in GAMA</h3>
 * <p>
 * Different experiment types register their creators:
 * </p>
 * 
 * <pre>
 * <code>
 * // GUI experiment creator
 * IExperimentAgentCreator guiCreator = (pop, index) -> 
 *     new GUIExperimentAgent(pop, index);
 * 
 * // Batch experiment creator
 * IExperimentAgentCreator batchCreator = (pop, index) -> 
 *     new BatchExperimentAgent(pop, index);
 * 
 * // Test experiment creator  
 * IExperimentAgentCreator testCreator = (pop, index) ->
 *     new TestExperimentAgent(pop, index);
 * </code>
 * </pre>
 * 
 * <h3>Registration via ExperimentAgentDescription</h3>
 * <p>
 * The {@link ExperimentAgentDescription} wrapper class allows creators to be registered as GAMA additions:
 * </p>
 * 
 * <pre>
 * <code>
 * // Register a new experiment type
 * IExperimentAgentCreator creator = ...;
 * ExperimentAgentDescription description = new ExperimentAgentDescription(
 *     creator,                        // The factory
 *     MyExperimentAgent.class,        // Agent class
 *     "my_experiment_type",           // Type name
 *     "my.plugin.id"                  // Plugin ID
 * );
 * 
 * // Now available in GAML:
 * // experiment my_exp type: my_experiment_type { ... }
 * </code>
 * </pre>
 * 
 * <h3>Functional Interface</h3>
 * <p>
 * As a {@link FunctionalInterface}, this can be implemented as a lambda:
 * </p>
 * 
 * <pre>
 * <code>
 * IExperimentAgentCreator creator = (population, index) -> {
 *     IExperimentAgent agent = new CustomExperimentAgent(population, index);
 *     agent.setSomeProperty(value);
 *     return agent;
 * };
 * </code>
 * </pre>
 * 
 * <h3>Method Parameters</h3>
 * <ul>
 * <li><b>pop:</b> The population to which the agent belongs. Provides species info and context.</li>
 * <li><b>index:</b> Unique identifier for the agent within its population.</li>
 * </ul>
 * 
 * <h3>GAML Integration</h3>
 * <p>
 * When a GAML model declares an experiment:
 * </p>
 * 
 * <pre>
 * <code>
 * experiment myExp type: gui {
 *     // Experiment parameters and outputs
 * }
 * </code>
 * </pre>
 * 
 * <p>
 * GAMA uses the registered creator for type "gui" to instantiate the agent:
 * </p>
 * 
 * <pre>
 * <code>
 * IExperimentAgentCreator creator = registry.getCreatorFor("gui");
 * IExperimentAgent agent = creator.create(experimentPopulation, 0);
 * </code>
 * </pre>
 * 
 * <h3>Built-in Experiment Types</h3>
 * <p>
 * GAMA provides several built-in experiment types, each with its own creator:
 * </p>
 * <ul>
 * <li><b>gui:</b> Interactive GUI experiments with visual control</li>
 * <li><b>batch:</b> Automated parameter exploration and optimization</li>
 * <li><b>test:</b> Automated testing with assertions</li>
 * <li><b>headless:</b> Command-line execution without UI</li>
 * </ul>
 * 
 * <h3>Custom Experiment Types</h3>
 * <p>
 * Plugins can register custom experiment types:
 * </p>
 * 
 * <pre>
 * <code>
 * // In plugin initialization
 * public class MyPlugin implements IGamaPlugin {
 *     
 *     public void initialize() {
 *         IExperimentAgentCreator creator = (pop, idx) -> 
 *             new MyCustomExperimentAgent(pop, idx);
 *         
 *         ExperimentAgentDescription desc = new ExperimentAgentDescription(
 *             creator,
 *             MyCustomExperimentAgent.class,
 *             "custom",
 *             "my.plugin.id"
 *         );
 *         
 *         // Register with GAMA
 *         ExperimentFactory.register(desc);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>ExperimentAgentDescription</h3>
 * <p>
 * The inner class {@link ExperimentAgentDescription} serves as:
 * </p>
 * <ul>
 * <li><b>Wrapper:</b> Combines creator with metadata (name, support class, plugin)</li>
 * <li><b>Addition:</b> Extends {@link GamlAddition} for GAMA's extension system</li>
 * <li><b>Delegate:</b> Delegates creation to the wrapped creator</li>
 * <li><b>Descriptor:</b> Provides title and documentation info</li>
 * </ul>
 * 
 * <h3>Thread Safety</h3>
 * <p>
 * Implementations should be thread-safe if experiment agents can be created concurrently (e.g., in batch experiments).
 * </p>
 * 
 * <h3>Best Practices</h3>
 * <ul>
 * <li>Keep creator logic simple - complex initialization should be in agent constructor</li>
 * <li>Ensure created agents are properly initialized before returning</li>
 * <li>Use meaningful names for custom experiment types</li>
 * <li>Document custom experiment types in plugin documentation</li>
 * </ul>
 * 
 * @see IExperimentAgent
 * @see GamlAddition
 * @see IPopulation
 * @author GAMA Team
 */
@FunctionalInterface
public interface IExperimentAgentCreator {

	/**
	 * The Class ExperimentAgentDescription.
	 */
	public static class ExperimentAgentDescription extends GamlAddition implements IExperimentAgentCreator {

		/** The original. */
		private final IExperimentAgentCreator delegate;

		/**
		 * Instantiates a new experiment agent description.
		 *
		 * @param original
		 *            the original
		 * @param name
		 *            the name
		 * @param plugin
		 *            the plugin
		 */
		public ExperimentAgentDescription(final IExperimentAgentCreator original,
				final Class<? extends IExperimentAgent> support, final String name, final String plugin) {
			super(name, support, plugin);
			this.delegate = original;
		}

		/**
		 * Method create()
		 *
		 * @see gama.api.kernel.simulation.IExperimentAgentCreator#create(java.lang.Object[])
		 */
		@Override
		public IExperimentAgent create(final IPopulation<? extends IAgent> pop, final int index) {
			return delegate.create(pop, index);
		}

		/**
		 * Method getTitle()
		 *
		 * @see gama.api.compilation.descriptions.IGamlDescription#getTitle()
		 */
		@Override
		public String getTitle() { return "Experiment type " + getName(); }

	}

	/**
	 * Creates the.
	 *
	 * @param pop
	 *            the pop
	 * @param index
	 *            the index
	 * @return the i experiment agent
	 */
	IExperimentAgent create(IPopulation<? extends IAgent> pop, int index);

}