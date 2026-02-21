/*******************************************************************************************************
 *
 * ISerialisedPopulation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.serialization;

import java.util.List;

/**
 * The Interface ISerialisedPopulation.
 * 
 * <p>
 * Represents a serialized snapshot of an entire agent population in GAMA. This interface provides a lightweight
 * representation of a population that can be stored, transmitted, or used to restore a population's state.
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * ISerialisedPopulation enables:
 * </p>
 * <ul>
 * <li><b>Population Snapshots:</b> Capture the state of all agents in a population</li>
 * <li><b>Batch Save/Restore:</b> Efficiently serialize/deserialize entire populations</li>
 * <li><b>Population Transfer:</b> Move populations between simulations or machines</li>
 * <li><b>Multi-Level Serialization:</b> Handle populations within macro-agents</li>
 * </ul>
 * 
 * <h3>Core Components</h3>
 * <ul>
 * <li><b>Species Name:</b> Identifier of the species this population belongs to</li>
 * <li><b>Agents List:</b> Collection of all serialized agents in the population</li>
 * <li><b>Grid Flag:</b> Indicates if this is a grid population (requires special handling)</li>
 * </ul>
 * 
 * <h3>Population Types</h3>
 * <ul>
 * <li><b>Regular Population:</b> Standard collection of agents (SerialisedPopulation)</li>
 * <li><b>Grid Population:</b> Grid-based agents with spatial structure (SerialisedGrid)</li>
 * </ul>
 * 
 * <h3>Usage in Context</h3>
 * 
 * <h4>Multi-Level Models</h4>
 * <p>
 * In multi-level models, populations can be nested:
 * </p>
 * 
 * <pre>
 * <code>
 * species city {
 *     species building {
 *         species room {
 *             // Three-level population hierarchy
 *         }
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <p>
 * When serializing a city agent, its building population is serialized, and each building's room population is
 * serialized recursively.
 * </p>
 * 
 * <h3>Java Usage</h3>
 * 
 * <pre>
 * <code>
 * // Serialize a population
 * IPopulation&lt;IAgent&gt; population = ...;
 * ISerialisedPopulation serialized = new SerialisedPopulation(population);
 * 
 * // Access serialized data
 * String speciesName = serialized.speciesName();
 * List&lt;ISerialisedAgent&gt; agents = serialized.agents();
 * boolean isGrid = serialized.isGrid();
 * 
 * // Restore population state
 * if (serialized instanceof SerialisedPopulation sp) {
 *     sp.restoreAs(scope, targetPopulation);
 * }
 * 
 * // Handle grid populations
 * if (serialized.isGrid() && serialized instanceof SerialisedGrid sg) {
 *     sg.restoreAs(scope, gridPopulation);
 * }
 * </code>
 * </pre>
 * 
 * <h3>Restoration Behavior</h3>
 * <p>
 * When restoring a population:
 * </p>
 * <ol>
 * <li>Existing agents with matching indices are updated with serialized values</li>
 * <li>Missing agents (in serialized data but not in target) are created</li>
 * <li>Extra agents (in target but not in serialized data) are removed/killed</li>
 * <li>For grid populations, the grid structure is restored first</li>
 * </ol>
 * 
 * <h3>Implementation Notes</h3>
 * <ul>
 * <li>Grid populations require special handling to preserve grid structure</li>
 * <li>Agent order is preserved through index-based restoration</li>
 * <li>Nested populations (in macro-agents) are handled recursively</li>
 * <li>Population size can change during restoration (agents added/removed)</li>
 * </ul>
 * 
 * @see ISerialisedAgent
 * @see SerialisedPopulation
 * @see SerialisedGrid
 * @see IPopulation
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.9
 * @date 27 août 2023
 */
public interface ISerialisedPopulation {

	/**
	 * Checks if is grid.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is grid
	 * @date 27 août 2023
	 */
	default boolean isGrid() { return false; }

	/**
	 * Gets the agents.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the agents
	 * @date 29 oct. 2023
	 */
	List<ISerialisedAgent> agents();

	/**
	 * Serialize to json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	// @Override
	// default JsonObject serializeToJson(final Json json) {
	// return json.object("population", speciesName(), "agents", json.array(agents()));
	// }

	/**
	 * Species name.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @date 29 oct. 2023
	 */
	String speciesName();

}
