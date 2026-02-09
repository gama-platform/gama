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
 * The Interface IExperimentAgentCreator.
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