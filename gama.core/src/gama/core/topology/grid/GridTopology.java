/*******************************************************************************************************
 *
 * GridTopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology.grid;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.agent.IPopulationSet;
import gama.api.kernel.topology.IGrid;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.IPath;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.topology.ITopology;
import gama.api.utils.collections.Collector;
import gama.api.utils.collections.ICollector;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.interfaces.IAgentFilter;
import gama.core.topology.AbstractTopology;
import gama.core.topology.filter.Different;
import gama.core.topology.filter.DifferentList;
import gama.core.util.file.GamaGridFile;

/**
 * The Class GridTopology.
 */
public class GridTopology extends AbstractTopology {

	/**
	 * Instantiates a new grid topology.
	 *
	 * @param scope
	 *            the scope
	 * @param matrix
	 *            the matrix
	 */
	public GridTopology(final IScope scope, final IGrid matrix) {
		super(scope, matrix.getEnvironmentFrame(), null);
		places = matrix;
	}

	@Override
	public void updateAgent(final IEnvelope previous, final IAgent agent) {}

	@Override
	public void initialize(final IScope scope, final IPopulation<? extends IAgent> pop) throws GamaRuntimeException {
		getPlaces().setCellSpecies(pop);
		// ((ISpatialIndex.Compound) getSpatialIndex()).add(getPlaces(), pop);
		super.initialize(scope, pop);
	}

	@Override
	protected boolean canCreateAgents() {
		return true;
	}

	@Override
	public boolean isContinuous() { return false; }

	/**
	 * Instantiates a new grid topology.
	 *
	 * @param scope
	 *            the scope
	 * @param environment
	 *            the environment
	 * @param rows
	 *            the rows
	 * @param columns
	 *            the columns
	 * @param isTorus
	 *            the is torus
	 * @param usesVN
	 *            the uses VN
	 * @param isHexagon
	 *            the is hexagon
	 * @param horizontalOrientation
	 *            the horizontal orientation
	 * @param useIndividualShapes
	 *            the use individual shapes
	 * @param useNeighborsCache
	 *            the use neighbors cache
	 * @param optimizer
	 *            the optimizer
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GridTopology(final IScope scope, final IShape environment, final int rows, final int columns,
			final boolean isTorus, final boolean usesVN, final boolean isHexagon, final boolean horizontalOrientation,
			final boolean useIndividualShapes, final boolean useNeighborsCache, final String optimizer)
			throws GamaRuntimeException {
		super(scope, environment, null);
		if (isHexagon) {
			places = new GamaSpatialMatrix(scope, environment, rows, columns, isTorus, usesVN, isHexagon,
					horizontalOrientation, useIndividualShapes, useNeighborsCache, optimizer);
		} else {
			places = new GamaSpatialMatrix(scope, environment, rows, columns, isTorus, usesVN, useIndividualShapes,
					useNeighborsCache, optimizer);
		}
		// FIXME Not sure it needs to be set
		// root.setTorus(isTorus);

	}

	/**
	 * Instantiates a new grid topology.
	 *
	 * @param scope
	 *            the scope
	 * @param environment
	 *            the environment
	 * @param file
	 *            the file
	 * @param isTorus
	 *            the is torus
	 * @param usesVN
	 *            the uses VN
	 * @param useIndividualShapes
	 *            the use individual shapes
	 * @param useNeighborsCache
	 *            the use neighbors cache
	 * @param optimizer
	 *            the optimizer
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GridTopology(final IScope scope, final IShape environment, final GamaGridFile file, final boolean isTorus,
			final boolean usesVN, final boolean useIndividualShapes, final boolean useNeighborsCache,
			final String optimizer) throws GamaRuntimeException {
		super(scope, environment, null);
		places = new GamaSpatialMatrix(scope, file, isTorus, usesVN, useIndividualShapes, useNeighborsCache, optimizer);
		// FIXME Not sure it needs to be set

		// root.setTorus(isTorus);
	}

	/**
	 * Instantiates a new grid topology.
	 *
	 * @param scope
	 *            the scope
	 * @param environment
	 *            the environment
	 * @param files
	 *            the files
	 * @param isTorus
	 *            the is torus
	 * @param usesVN
	 *            the uses VN
	 * @param useIndividualShapes
	 *            the use individual shapes
	 * @param useNeighborsCache
	 *            the use neighbors cache
	 * @param optimizer
	 *            the optimizer
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GridTopology(final IScope scope, final IShape environment, final IList<GamaGridFile> files,
			final boolean isTorus, final boolean usesVN, final boolean useIndividualShapes,
			final boolean useNeighborsCache, final String optimizer) throws GamaRuntimeException {
		super(scope, environment, null);
		places = new GamaSpatialMatrix(scope, files, isTorus, usesVN, useIndividualShapes, useNeighborsCache,
				optimizer);
		// FIXME Not sure it needs to be set

		// root.setTorus(isTorus);
	}

	@Override
	public IAgent getAgentClosestTo(final IScope scope, final IShape source, final IAgentFilter filter) {
		return ((GamaSpatialMatrix) getPlaces()).getAgentClosestTo(scope, source, filter);

	}

	/**
	 * @see gama.api.types.misc.interfaces.IValue#stringValue()
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "Grid topology in " + environment.toString() + " as " + places.toString();
	}

	/**
	 * @see gama.environment.AbstractTopology#_toGaml()
	 */
	@Override
	protected String _toGaml(final boolean includingBuiltIn) {
		return IKeyword.TOPOLOGY + " (" + places.serializeToGaml(includingBuiltIn) + ")";
	}

	/**
	 * @throws GamaRuntimeException
	 * @see gama.environment.AbstractTopology#_copy()
	 */
	@Override
	protected ITopology _copy(final IScope scope) throws GamaRuntimeException {
		final IGrid grid = (IGrid) places;
		return new GridTopology(scope, environment, grid.getRows(scope), grid.getCols(scope), grid.isTorus(),
				grid.getNeighborhood().isVN(), grid.isHexagon(), grid.isHorizontalOrientation(),
				grid.usesIndiviualShapes(), grid.usesNeighborsCache(), grid.optimizer());
	}

	@Override
	public IGrid getPlaces() { return (IGrid) super.getPlaces(); }

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param on
	 *            the on
	 * @return the gama spatial path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public IPath pathBetween(final IScope scope, final IShape source, final IShape target, final IList<IAgent> on)
			throws GamaRuntimeException {
		return getPlaces().computeShortestPathBetween(scope, source, target, this, on);
	}

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param on
	 *            the on
	 * @return the gama spatial path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public IPath pathBetween(final IScope scope, final IShape source, final IShape target, final Map<IAgent, Object> on)
			throws GamaRuntimeException {
		return getPlaces().computeShortestPathBetweenWeighted(scope, source, target, this, on);
	}

	@Override
	public IPath pathBetween(final IScope scope, final IShape source, final IShape target) throws GamaRuntimeException {
		return getPlaces().computeShortestPathBetween(scope, source, target, this, null);
	}

	/**
	 * @see gama.api.types.topology.environment.ITopology#isValidLocation(gama.core.util.GamaPoint)
	 */
	@Override
	public boolean isValidLocation(final IScope scope, final IPoint p) {
		return getPlaces().getPlaceAt(p) != null;

	}

	@Override
	public boolean isValidGeometry(final IScope scope, final IShape g) {
		return isValidLocation(scope, g.getLocation());
	}

	@Override
	public Double distanceBetween(final IScope scope, final IShape source, final IShape target) {
		if (!isValidGeometry(scope, source) || !isValidGeometry(scope, target)) return Double.MAX_VALUE;
		// TODO null or Double.MAX_VALUE ?
		return (double) getPlaces().manhattanDistanceBetween(source, target);
	}

	@Override
	public Double distanceBetween(final IScope scope, final IPoint source, final IPoint target) {
		if (!isValidLocation(scope, source) || !isValidLocation(scope, target)) return Double.MAX_VALUE;
		// TODO null or Double.MAX_VALUE ?
		return (double) getPlaces().manhattanDistanceBetween(source, target);
	}

	@Override
	public Double directionInDegreesTo(final IScope scope, final IShape source, final IShape target) {
		// TODO compute from the path
		return root.directionInDegreesTo(scope, source, target);
	}

	@Override
	public Collection<IAgent> getNeighborsOf(final IScope scope, final IShape source, final Double distance,
			final IAgentFilter filter) throws GamaRuntimeException {
		// We compute the neighboring cells of the "source" shape

		final Set<IAgent> placesConcerned =
				getPlaces().getNeighborsOf(scope, source, distance, getPlaces().getCellSpecies());
		// If we only accept cells from this topology, no need to look for other
		// agents
		if (filter.getSpecies() == getPlaces().getCellSpecies()) {
			// case where the filter is the complete population set
			if (filter instanceof IPopulationSet) return placesConcerned;
			// otherwise, we return only the accepted cells
			try (ICollector<IAgent> agents = Collector.getOrderedSet()) {
				for (final IAgent ag : placesConcerned) { if (filter.accept(scope, null, ag)) { agents.add(ag); } }
				return agents.items();
			}

		}
		// Otherwise, we return all the agents that intersect the geometry
		// formed by the shapes of the cells (incl. the
		// cells themselves) and that are accepted by the filter
		final boolean normalFilter = filter.getSpecies() != null || !(filter instanceof Different);
		final IAgentFilter fDL = normalFilter ? filter
				: new DifferentList(getPlaces().getCellSpecies().listValue(scope, Types.NO_TYPE, false));
		final Collection<IAgent> agents = getAgentsIn(scope,
				GamaShapeFactory.geometriesToGeometry(scope, GamaListFactory.wrap(Types.AGENT, placesConcerned)), fDL,
				SpatialRelation.OVERLAP);
		if (!normalFilter) { agents.addAll(placesConcerned); }
		return agents;

	}

	@Override
	public void dispose() {
		super.dispose();
		getPlaces().dispose();
	}

	/**
	 * Sets the places.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param matrix
	 *            the new places
	 * @date 30 août 2023
	 */
	public void setPlaces(final IGrid matrix) { places = matrix; }

	// @Override
	// public IList<GamaSpatialPath> KpathsBetween(final IScope scope, final IShape source, final IShape target,
	// final int k) {
	// // TODO for the moment, returns only 1 shortest path.... need to fix it!
	// return super.KpathsBetween(scope, source, target, k);
	// }
	//
	// @Override
	// public IList<GamaSpatialPath> KpathsBetween(final IScope scope, final IPoint source, final IPoint target,
	// final int k) {
	// // TODO for the moment, returns only 1 shortest path.... need to fix it!
	// return super.KpathsBetween(scope, source, target, k);
	// }

}
