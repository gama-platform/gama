/*******************************************************************************************************
 *
 * IGridAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.list.IList;

/**
 * The Interface IGridAgent.
 * 
 * <p>
 * Represents an agent that is a cell within a grid-based topology in GAMA. Grid agents are specialized agents that
 * have fixed positions in a 2D grid structure and provide direct access to grid-specific properties like coordinates,
 * color, value, and neighboring cells.
 * </p>
 * 
 * <h3>Core Characteristics</h3>
 * <ul>
 * <li><b>Fixed Position:</b> Grid agents have fixed x,y coordinates within the grid</li>
 * <li><b>Visual Representation:</b> Each cell has a color for visualization</li>
 * <li><b>Data Storage:</b> Can store numerical values and multi-band data (e.g., for images)</li>
 * <li><b>Neighborhood:</b> Direct access to neighboring cells</li>
 * </ul>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <h4>1. Creating a Grid Species</h4>
 * 
 * <pre>
 * <code>
 * grid cell width: 50 height: 50 {
 *     rgb color <- #white;
 *     float value <- 0.0;
 *     
 *     reflex update {
 *         value <- mean(neighbors collect each.value) + rnd(1.0);
 *         color <- rgb(255 * value, 0, 0);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Loading Grid from Image</h4>
 * 
 * <pre>
 * <code>
 * grid elevation_grid file: "dem.tif" {
 *     init {
 *         float altitude <- bands[0]; // Access elevation data
 *         color <- gradient([#green, #yellow, #red], altitude / 255);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Working with Neighbors</h4>
 * 
 * <pre>
 * <code>
 * grid terrain width: 100 height: 100 neighbors: 8 {
 *     reflex spread {
 *         list&lt;terrain&gt; neighbor_cells <- self.neighbors(self);
 *         ask neighbor_cells {
 *             value <- myself.value * 0.9;
 *         }
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>4. Using Grid Coordinates</h4>
 * 
 * <pre>
 * <code>
 * grid data_grid width: 20 height: 20 {
 *     init {
 *         // Access grid position
 *         int my_x <- grid_x;
 *         int my_y <- grid_y;
 *         
 *         // Use position for initialization
 *         value <- (my_x + my_y) / 40.0;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * @see IAgent
 * @see gama.api.kernel.topology.IGrid
 * @author drogoul
 * @since GAMA 1.0
 */
public interface IGridAgent extends IAgent {

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	IColor getColor();

	/**
	 * Sets the color.
	 *
	 * @param color
	 *            the new color
	 */
	void setColor(final IColor color);

	/**
	 * Gets the x.
	 *
	 * @return the x
	 */
	int getX();

	/**
	 * Gets the y.
	 *
	 * @return the y
	 */
	int getY();

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	double getValue();

	/**
	 * Gets the bands.
	 *
	 * @return the bands
	 */
	IList<Double> getBands();

	/**
	 * Gets the neighbors.
	 *
	 * @param scope
	 *            the scope
	 * @return the neighbors
	 */
	IList<IAgent> getNeighbors(IScope scope);

	/**
	 * Sets the value.
	 *
	 * @param d
	 *            the new value
	 */
	void setValue(final double d);
}