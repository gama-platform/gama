/*******************************************************************************************************
 *
 * IAgentConstructor.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

/**
 * Functional interface for constructing agent instances.
 * 
 * <p>IAgentConstructor defines the contract for creating agents within a population.
 * It is used by the GAMA platform to instantiate agents with the correct Java class
 * and initialization parameters.</p>
 * 
 * <h2>Usage</h2>
 * 
 * <p>Implementations typically delegate to a constructor or factory method of the
 * agent class. The constructor receives a reference to the managing population and
 * a unique index for the new agent.</p>
 * 
 * <h2>Examples</h2>
 * 
 * <h3>Method Reference</h3>
 * <pre>{@code
 * // Simple constructor reference
 * IAgentConstructor constructor = MyAgent::new;
 * 
 * // Registration with GamaMetaModel
 * GamaMetaModel.addSpecies("my_species", MyAgent.class, 
 *     MyAgent::new, new String[]{"moving"});
 * }</pre>
 * 
 * <h3>Lambda Expression</h3>
 * <pre>{@code
 * IAgentConstructor constructor = (pop, index) -> {
 *     MyAgent agent = new MyAgent(pop, index);
 *     // Additional initialization
 *     return agent;
 * };
 * }</pre>
 * 
 * <h3>Custom Factory</h3>
 * <pre>{@code
 * public class CustomAgentFactory implements IAgentConstructor {
 *     @Override
 *     public IAgent createOneAgent(IPopulation manager, int index) {
 *         // Custom agent creation logic
 *         CustomAgent agent = new CustomAgent(manager, index);
 *         agent.setSpecialProperty(computeProperty());
 *         return agent;
 *     }
 * }
 * }</pre>
 * 
 * @see IAgent
 * @see IPopulation
 * @see GamaMetaModel#addSpecies
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
@FunctionalInterface
public interface IAgentConstructor {

	/**
	 * Creates a single agent instance within the specified population.
	 * 
	 * <p>The created agent should be initialized with at minimum:
	 * <ul>
	 *   <li>A reference to its managing population</li>
	 *   <li>A unique index within that population</li>
	 * </ul>
	 * 
	 * <p>Further initialization (attributes, location, etc.) is typically
	 * performed by the population after construction.</p>
	 *
	 * @param <T> the specific agent type being created
	 * @param manager the population that will manage this agent
	 * @param index the unique index for this agent within its population
	 * @return the newly created agent instance
	 * 
	 * @see IPopulation#createAgents(gama.api.runtime.scope.IScope, int, java.util.List, boolean, boolean)
	 */
	IAgent createOneAgent(IPopulation manager, int index);

}
