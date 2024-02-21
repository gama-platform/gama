/*******************************************************************************************************
 *
 * GridTopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.topology.grid;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.population.IPopulationSet;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.AbstractTopology;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.filter.Different;
import gama.core.metamodel.topology.filter.DifferentList;
import gama.core.metamodel.topology.filter.IAgentFilter;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.core.util.GamaListFactory;
import gama.core.util.ICollector;
import gama.core.util.IList;
import gama.core.util.file.GamaGridFile;
import gama.core.util.path.GamaSpatialPath;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.Types;

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
	public void updateAgent(final Envelope3D previous, final IAgent agent) {}

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
	 * @see gama.interfaces.IValue#stringValue()
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
	public GamaSpatialPath pathBetween(final IScope scope, final IShape source, final IShape target,
			final IList<IAgent> on) throws GamaRuntimeException {
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
	public GamaSpatialPath pathBetween(final IScope scope, final IShape source, final IShape target,
			final Map<IAgent, Object> on) throws GamaRuntimeException {
		return getPlaces().computeShortestPathBetweenWeighted(scope, source, target, this, on);
	}

	@Override
	public GamaSpatialPath pathBetween(final IScope scope, final IShape source, final IShape target)
			throws GamaRuntimeException {
		return getPlaces().computeShortestPathBetween(scope, source, target, this, null);
	}

	/**
	 * @see gama.environment.ITopology#isValidLocation(gama.core.util.GamaPoint)
	 */
	@Override
	public boolean isValidLocation(final IScope scope, final GamaPoint p) {
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
	public Double distanceBetween(final IScope scope, final GamaPoint source, final GamaPoint target) {
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
				GamaGeometryType.geometriesToGeometry(scope, GamaListFactory.wrap(Types.AGENT, placesConcerned)), fDL,
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
	// public IList<GamaSpatialPath> KpathsBetween(final IScope scope, final GamaPoint source, final GamaPoint target,
	// final int k) {
	// // TODO for the moment, returns only 1 shortest path.... need to fix it!
	// return super.KpathsBetween(scope, source, target, k);
	// }

}
