/*******************************************************************************************************
 *
 * IGrid.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import java.util.Map;
import java.util.Set;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.IPath;
import gama.api.types.list.IList;
import gama.api.types.matrix.IMatrix;
import gama.api.types.topology.ITopology;
import gama.api.utils.interfaces.IAgentFilter;
import gama.api.utils.interfaces.IDiffusionTarget;

/**
 * Interface IGrid.
 *
 * @author Alexis Drogoul
 * @since 13 mai 2013
 *
 */
public interface IGrid extends IMatrix<IShape>, ISpatialIndex, IDiffusionTarget {

	/**
	 * Gets the agents.
	 *
	 * @return the agents
	 */
	IList<IAgent> getAgents();

	/**
	 * Checks if is hexagon.
	 *
	 * @return the boolean
	 */
	Boolean isHexagon();

	/**
	 * Checks if is horizontal orientation.
	 *
	 * @return the boolean
	 */
	Boolean isHorizontalOrientation();

	/**
	 * Sets the cell species.
	 *
	 * @param pop
	 *            the new cell species
	 */
	void setCellSpecies(final IPopulation<? extends IAgent> pop);

	/**
	 * Gets the agent at.
	 *
	 * @param c
	 *            the c
	 * @return the agent at
	 */
	IAgent getAgentAt(final IPoint c);

	/**
	 * Compute shortest path between.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param topo
	 *            the topo
	 * @param on
	 *            the on
	 * @return the gama spatial path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	IPath computeShortestPathBetween(final IScope scope, final IShape source, final IShape target, final ITopology topo,
			final IList<IAgent> on) throws GamaRuntimeException;

	/**
	 * Compute shortest path between weighted.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param topo
	 *            the topo
	 * @param on
	 *            the on
	 * @return the gama spatial path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	IPath computeShortestPathBetweenWeighted(final IScope scope, final IShape source, final IShape target,
			final ITopology topo, final Map<IAgent, Object> on) throws GamaRuntimeException;

	// public abstract Iterator<IAgent> getNeighborsOf(final IScope scope, final
	// IPoint shape, final Double
	// distance,
	// IAgentFilter filter);

	/**
	 * Gets the neighbors of.
	 *
	 * @param scope
	 *            the scope
	 * @param shape
	 *            the shape
	 * @param distance
	 *            the distance
	 * @param filter
	 *            the filter
	 * @return the neighbors of
	 */
	Set<IAgent> getNeighborsOf(final IScope scope, final IShape shape, final Double distance, IAgentFilter filter);

	/**
	 * Manhattan distance between.
	 *
	 * @param g1
	 *            the g 1
	 * @param g2
	 *            the g 2
	 * @return the int
	 */
	int manhattanDistanceBetween(final IShape g1, final IShape g2);

	/**
	 * Gets the place at.
	 *
	 * @param c
	 *            the c
	 * @return the place at
	 */
	IShape getPlaceAt(final IPoint c);

	/**
	 * Gets the display data.
	 *
	 * @return the display data
	 */
	int[] getDisplayData();

	/**
	 * Gets the grid value.
	 *
	 * @return the grid value
	 */
	double[] getGridValue();

	/**
	 * Computes and returns a double array by applying the expression to each of the agents of the grid
	 *
	 * @param scope
	 *            the current scope
	 * @param expr
	 *            cannot be null
	 * @return a double array the size of the grid
	 */
	double[] getGridValueOf(IScope scope, IExpression expr);

	/**
	 * Checks if is torus.
	 *
	 * @return true, if is torus
	 */
	boolean isTorus();

	/**
	 * Gets the neighborhood.
	 *
	 * @return the neighborhood
	 */
	INeighborhood getNeighborhood();

	/**
	 * Gets the environment frame.
	 *
	 * @return the environment frame
	 */
	IShape getEnvironmentFrame();

	/**
	 * Gets the x.
	 *
	 * @param geometry
	 *            the geometry
	 * @return the x
	 */
	int getX(IShape geometry);

	/**
	 * Gets the y.
	 *
	 * @param geometry
	 *            the geometry
	 * @return the y
	 */
	int getY(IShape geometry);

	/**
	 * Dispose.
	 */
	@Override
	void dispose();

	/**
	 * Uses indiviual shapes.
	 *
	 * @return true, if successful
	 */
	boolean usesIndiviualShapes();

	/**
	 * @return
	 */
	boolean usesNeighborsCache();

	/**
	 * Optimizer.
	 *
	 * @return the string
	 */
	String optimizer();

	/**
	 * @return
	 */
	ISpecies getCellSpecies();

	/**
	 * Sets the grid values.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param gridValues
	 *            the new grid values
	 * @date 27 août 2023
	 */
	void setGridValues(double[] gridValues);

}
