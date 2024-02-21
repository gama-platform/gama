/*******************************************************************************************************
 *
 * IAgentConstructor.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.compilation;

import java.util.HashMap;
import java.util.Map;

import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.MinimalAgent;
import gama.core.metamodel.population.IPopulation;

/**
 * Written by drogoul Modified on 20 ao�t 2010
 *
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
@FunctionalInterface
public interface IAgentConstructor<T extends IAgent> {

	/**
	 * The Class Minimal.
	 */
	public static class Minimal implements IAgentConstructor<MinimalAgent> {

		/**
		 * Method createOneAgent()
		 *
		 * @see gama.gaml.compilation.IAgentConstructor#createOneAgent(gama.core.metamodel.population.IPopulation)
		 */

		@Override
		public MinimalAgent createOneAgent(final IPopulation manager, final int index) {
			return new MinimalAgent(manager, index);
		}

	}

	/**
	 * The Class Gaml.
	 */
	public static class Gaml implements IAgentConstructor<GamlAgent> {

		@Override
		public GamlAgent createOneAgent(final IPopulation manager, final int index) {
			return new GamlAgent(manager, index);
		}

	}

	/** The constructors. */
	Map<Class<? extends IAgent>, IAgentConstructor<? extends IAgent>> CONSTRUCTORS =
			new HashMap<Class<? extends IAgent>, IAgentConstructor<? extends IAgent>>() {

				{
					put(GamlAgent.class, new Gaml());
					put(MinimalAgent.class, new Minimal());
				}
			};

	/**
	 * Creates the one agent.
	 *
	 * @param <T> the generic type
	 * @param manager the manager
	 * @param index the index
	 * @return the t
	 */
	<T extends IAgent> T createOneAgent(IPopulation<T> manager, int index);

}
