/*******************************************************************************************************
 *
 * InternalTopologyFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology;

import static gama.api.constants.IKeyword.CELL_HEIGHT;
import static gama.api.constants.IKeyword.CELL_WIDTH;
import static gama.api.constants.IKeyword.FILE;
import static gama.api.constants.IKeyword.FILES;
import static gama.api.constants.IKeyword.NEIGHBORS;
import static gama.api.constants.IKeyword.WIDTH;

import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.IGrid;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.ISpatialGraph;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.topology.ITopology;
import gama.api.types.topology.ITopologyFactory;
import gama.api.utils.geometry.IEnvelope;
import gama.core.topology.continuous.ContinuousTopology;
import gama.core.topology.continuous.MultipleTopology;
import gama.core.topology.graph.GraphTopology;
import gama.core.topology.grid.GridTopology;
import gama.core.util.file.GamaGridFile;

/**
 *
 */
public class InternalTopologyFactory implements ITopologyFactory {

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i topology
	 */
	@Override
	public ITopology createFrom(final IScope scope, final IShape obj) {
		return new ContinuousTopology(scope, obj);
	}

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i topology
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public ITopology createFrom(final IScope scope, final IContainer<?, IShape> obj) throws GamaRuntimeException {
		if (obj instanceof ISpatialGraph) return ((ISpatialGraph) obj).getTopology(scope);
		if (obj instanceof IGrid) return new GridTopology(scope, (IGrid) obj);
		return new MultipleTopology(scope, obj);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param copy
	 *            the copy
	 * @return the i topology
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@SuppressWarnings ("rawtypes")
	public ITopology createFrom(final IScope scope, final Object obj, final boolean copy) throws GamaRuntimeException {
		// Many cases.
		return switch (obj) {
			case null -> null;
			case ISpatialGraph is -> is.getTopology(scope);
			case ITopology it -> it;
			case IAgent ia -> ia.getTopology();
			case IPopulation ip -> ip.getTopology();
			case ISpecies is -> createFrom(scope, scope.getAgent().getPopulationFor(is), copy);
			case IShape is -> createFrom(scope, is);
			case IContainer ic -> createFrom(scope, ic);
			default -> createFrom(scope, GamaShapeFactory.castToShape(scope, obj, copy), copy);
		};
	}

	/**
	 * Creates a new GamaTopology object.
	 *
	 * @param scope
	 *            the scope
	 * @param host
	 *            the host
	 * @return the i topology
	 */
	@Override
	public ITopology createContinuous(final IScope scope, final IShape host) {
		return new ContinuousTopology(scope, host);
	}

	/**
	 * Creates a new GamaTopology object.
	 *
	 * @return the i topology
	 */

	/**
	 * Builds the grid topology.
	 *
	 * @param scope
	 *            the scope
	 * @param species
	 *            the species
	 * @param host
	 *            the host
	 * @return the i topology
	 */
	@Override
	public ITopology createGrid(final IScope scope, final ISpecies species, final IAgent host) {
		IExpression exp = species.getFacet(WIDTH);
		final IEnvelope env = scope.getSimulation().getGeometry().getEnvelope();
		final int rows = exp == null
				? species.hasFacet(CELL_WIDTH)
						? (int) (env.getWidth() / Cast.asFloat(scope, species.getFacet(CELL_WIDTH).value(scope))) : 100
				: Cast.asInt(scope, exp.value(scope));
		exp = species.getFacet(IKeyword.HEIGHT);
		final int columns = exp == null ? species.hasFacet(CELL_HEIGHT)
				? (int) (env.getHeight() / Cast.asFloat(scope, species.getFacet(CELL_HEIGHT).value(scope))) : 100
				: Cast.asInt(scope, exp.value(scope));

		final boolean isTorus = host.getTopology().isTorus();
		exp = species.getFacet("use_individual_shapes");
		final boolean useIndividualShapes = exp == null || Cast.asBool(scope, exp.value(scope));
		exp = species.getFacet("use_neighbors_cache");
		final boolean useNeighborsCache = exp == null || Cast.asBool(scope, exp.value(scope));
		exp = species.getFacet("horizontal_orientation");
		final boolean horizontalOrientation = exp == null || Cast.asBool(scope, exp.value(scope));

		exp = species.getFacet("optimizer");
		final String optimizer = exp == null ? "" : Cast.asString(scope, exp.value(scope));

		exp = species.getFacet(NEIGHBORS);
		final boolean usesVN = exp == null || Cast.asInt(scope, exp.value(scope)) == 4;
		final boolean isHexagon = exp != null && Cast.asInt(scope, exp.value(scope)) == 6;
		exp = species.getFacet(FILES);
		IList<GamaGridFile> files = null;
		if (exp != null) { files = GamaListFactory.castToList(scope, exp.value(scope)); }
		ITopology result;
		if (files != null && !files.isEmpty()) {
			result = new GridTopology(scope, host, files, isTorus, usesVN, useIndividualShapes, useNeighborsCache,
					optimizer);
		} else {
			exp = species.getFacet(FILE);
			final GamaGridFile file = (GamaGridFile) (exp != null ? exp.value(scope) : null);
			if (file == null) {
				result = new GridTopology(scope, host, rows, columns, isTorus, usesVN, isHexagon, horizontalOrientation,
						useIndividualShapes, useNeighborsCache, optimizer);
			} else {
				result = new GridTopology(scope, host, file, isTorus, usesVN, useIndividualShapes, useNeighborsCache,
						optimizer);
			}

		}
		// Reverts the modification of the world envelope (see #1953 and #1939)
		//
		// final GamaEnvelope env =
		// result.getPlaces().getEnvironmentFrame().getEnvelope();
		// final GamaEnvelope world = host.getEnvelope();
		// final GamaEnvelope newEnvelope = new GamaEnvelope(0,
		// Math.max(env.getWidth(), world.getWidth()), 0,
		// Math.max(env.getHeight(), world.getHeight()), 0,
		// Math.max(env.getDepth(), world.getDepth()));
		// host.setGeometry(GamaShapeFactory.createFrom(newEnvelope));
		return result;
	}

	/**
	 * Creates a new GamaTopology object.
	 *
	 * @param scope
	 *            the scope
	 * @param host
	 *            the host
	 * @param graph
	 *            the graph
	 * @return the i topology
	 */
	@Override
	public ITopology createGraph(final IScope scope, final IShape host, final ISpatialGraph graph) {
		return new GraphTopology(scope, host, graph);
	}

}
