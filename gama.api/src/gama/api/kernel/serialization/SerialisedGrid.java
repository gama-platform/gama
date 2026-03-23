/*******************************************************************************************************
 *
 * SerialisedGrid.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.serialization;

import java.util.ArrayList;
import java.util.List;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.topology.IGrid;
import gama.api.runtime.scope.IScope;

/**
 * The Class SerialisedGrid.
 * 
 * <p>
 * Specialized serialization implementation for grid-based agent populations in GAMA. This class extends the standard
 * population serialization to preserve the grid structure (topology matrix) along with the individual cell agents.
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * Grid populations require special handling because:
 * </p>
 * <ul>
 * <li>Grid agents have fixed positions determined by the grid structure</li>
 * <li>The grid topology (matrix) must be preserved along with agent data</li>
 * <li>Grid dimensions and neighborhood relationships are implicit in the structure</li>
 * <li>Spatial indexing and lookup depends on the grid organization</li>
 * </ul>
 * 
 * <h3>Structure</h3>
 * <p>
 * SerialisedGrid is a Java record with three components:
 * </p>
 * <ul>
 * <li><b>speciesName:</b> The name of the grid species</li>
 * <li><b>agents:</b> List of all serialized grid cell agents</li>
 * <li><b>matrix:</b> The IGrid topology object containing spatial structure</li>
 * </ul>
 * 
 * <h3>Usage in GAML Context</h3>
 * 
 * <h4>Grid Population Example</h4>
 * 
 * <pre>
 * <code>
 * grid cell width: 50 height: 50 {
 *     rgb color;
 *     float temperature;
 *     
 *     reflex update {
 *         temperature <- mean(neighbors collect each.temperature) + rnd(5.0);
 *         color <- gradient([#blue, #red], temperature / 100);
 *     }
 * }
 * 
 * global {
 *     reflex save_grid when: cycle = 1000 {
 *         // Grid structure and all cell states are preserved
 *         save simulation to: "grid_state.json" format: json;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Java Usage</h3>
 * 
 * <pre>
 * <code>
 * // Serialize a grid population
 * IPopulation.Grid gridPop = ...;
 * SerialisedGrid serialized = new SerialisedGrid(gridPop);
 * 
 * // Access components
 * String species = serialized.speciesName();
 * List&lt;ISerialisedAgent&gt; cells = serialized.agents();
 * IGrid topology = serialized.matrix();
 * 
 * // Check if grid
 * assert serialized.isGrid() == true;
 * 
 * // Restore grid population
 * IPopulation.Grid targetGrid = ...;
 * serialized.restoreAs(scope, targetGrid);
 * </code>
 * </pre>
 * 
 * <h3>Restoration Process</h3>
 * <ol>
 * <li><b>Restore Grid Structure:</b> The IGrid matrix is set first, establishing the topology</li>
 * <li><b>Restore Cell Agents:</b> Each cell agent is located by its index</li>
 * <li><b>Update Attributes:</b> Cell attributes (color, value, bands, etc.) are restored</li>
 * <li><b>Preserve Relationships:</b> Neighbor relationships are implicit in the grid structure</li>
 * </ol>
 * 
 * <h3>What Gets Serialized</h3>
 * <ul>
 * <li><b>Grid Structure:</b> The IGrid topology with dimensions and organization</li>
 * <li><b>Cell States:</b> Each cell's color, value, bands, and custom attributes</li>
 * <li><b>NOT Serialized:</b> grid_x, grid_y (implicit in position), neighbors (implicit in structure)</li>
 * </ul>
 * 
 * <h3>Differences from Regular Population</h3>
 * <table border="1">
 * <tr>
 * <th>Aspect</th>
 * <th>Regular Population</th>
 * <th>Grid Population</th>
 * </tr>
 * <tr>
 * <td>Structure</td>
 * <td>Arbitrary collection</td>
 * <td>Fixed 2D matrix</td>
 * </tr>
 * <tr>
 * <td>Agent Creation</td>
 * <td>Dynamic (can add/remove)</td>
 * <td>Static (fixed at initialization)</td>
 * </tr>
 * <tr>
 * <td>Position</td>
 * <td>Stored in location attribute</td>
 * <td>Implicit in grid coordinates</td>
 * </tr>
 * <tr>
 * <td>Topology</td>
 * <td>Optional, flexible</td>
 * <td>Required grid topology</td>
 * </tr>
 * </table>
 * 
 * <h3>Implementation Notes</h3>
 * <ul>
 * <li>Grid agents must match the grid dimensions (no missing or extra cells)</li>
 * <li>The matrix is stored as a reference to the IGrid object</li>
 * <li>Grid coordinates (grid_x, grid_y) are not serialized as they're derived from index</li>
 * <li>Restoration assumes target population has compatible grid dimensions</li>
 * </ul>
 * 
 * @param speciesName
 *            The name of the grid species
 * @param agents
 *            List of serialized grid cell agents
 * @param matrix
 *            The grid topology structure
 * @see ISerialisedPopulation
 * @see SerialisedAgent
 * @see IPopulation.Grid
 * @see IGrid
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.9
 * @date 27 août 2023
 */
public record SerialisedGrid(String speciesName, List<ISerialisedAgent> agents, IGrid matrix)
		implements ISerialisedPopulation {

	/**
	 * Instantiates a new serialised grid.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param pop
	 *            the pop
	 * @date 27 août 2023
	 */
	public SerialisedGrid(final IPopulation.Grid pop) {
		this(pop.getSpecies().getName(), new ArrayList<>(), pop.getGrid());
		for (IAgent a : pop) { agents.add(SerialisedAgent.of(a, true)); }
	}

	/**
	 * Checks if is grid.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is grid
	 * @date 27 août 2023
	 */
	@Override
	public boolean isGrid() { return true; }
	//
	// @Override
	// public JsonObject serializeToJson(final Json json) {
	// return ISerialisedPopulation.super.serializeToJson(json).add("cols", matrix.getCols(null)).add("rows",
	// matrix.getRows(null));
	// }

	/**
	 * Restore as.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param pop
	 *            the pop
	 * @date 31 oct. 2023
	 */
	public void restoreAs(final IScope scope, final IPopulation<? extends IAgent> pop) {
		IPopulation.Grid grid = (IPopulation.Grid) pop;
		grid.setGrid(matrix());
		for (ISerialisedAgent a : agents()) {
			IAgent agent = pop.getAgent(a.getIndex());
			a.attributes().forEach((name, v) -> { agent.setDirectVarValue(scope, name, v); });
		}

	}

}
