package gama.gaml.operators.spatial;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.grid.GridTopology;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.path.GamaSpatialPath;
import gama.core.util.path.IPath;
import gama.core.util.path.PathFactory;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Relations.
 */
public class SpatialRelations {

	/**
	 * Towards.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param target
	 *            the target
	 * @return the double
	 */
	@operator (
			value = { "towards", "direction_to" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_RELATIONS },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.AGENT_LOCATION,
					IConcept.TOPOLOGY })
	@doc (
			value = "The direction (in degree) between the two geometries (geometries, agents, points) considering the topology of the agent applying the operator.",
			examples = { @example (
					value = "ag1 towards ag2",
					equals = "the direction between ag1 and ag2 and ag3 considering the topology of the agent applying the operator",
					isExecutable = false) },
			see = { "distance_between", "distance_to", "direction_between", "path_between", "path_to" })
	@no_test // Test already done in Spatial tests models
	public static Double towards(final IScope scope, final IShape agent, final IShape target) {
		return scope.getTopology().directionInDegreesTo(scope, agent, target);
	}

	/**
	 * Distance between.
	 *
	 * @param scope
	 *            the scope
	 * @param t
	 *            the t
	 * @param geometries
	 *            the geometries
	 * @return the double
	 */
	@operator (
			value = "distance_between",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_RELATIONS },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.AGENT_LOCATION,
					IConcept.TOPOLOGY })
	@doc (
			value = "A distance between a list of geometries (geometries, agents, points) considering a topology.",
			examples = { @example (
					value = "my_topology distance_between [ag1, ag2, ag3]",
					equals = "the distance between ag1, ag2 and ag3 considering the topology my_topology",
					isExecutable = false) },
			see = { "towards", "direction_to", "distance_to", "direction_between", "path_between", "path_to" })
	@no_test // Test already done in Spatial tests models
	public static Double distance_between(final IScope scope, final ITopology t,
			final IContainer<?, IShape> geometries) {
		final int size = geometries.length(scope);
		if (size == 0 || size == 1) return 0d;
		IShape previous = null;
		double distance = 0d;
		for (final IShape obj : geometries.iterable(scope)) {
			if (previous != null) {
				final Double d = t.distanceBetween(scope, previous, obj);
				if (d == null) return null;
				distance += d;
			}
			previous = obj;
		}
		return distance;
	}

	/**
	 * Direction between.
	 *
	 * @param scope
	 *            the scope
	 * @param t
	 *            the t
	 * @param geometries
	 *            the geometries
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "direction_between",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_RELATIONS },
			concept = {})
	@doc (
			value = "A direction (in degree) between a list of two geometries (geometries, agents, points) considering a topology.",
			examples = { @example (
					value = "my_topology direction_between [ag1, ag2]",
					equals = "the direction between ag1 and ag2 considering the topology my_topology",
					isExecutable = false) },
			see = { "towards", "direction_to", "distance_to", "distance_between", "path_between", "path_to" })
	@test ("topology(world) direction_between([{0,0},{50,50}]) = 45.0")
	public static Double direction_between(final IScope scope, final ITopology t,
			final IContainer<?, IShape> geometries) throws GamaRuntimeException {
		final int size = geometries.length(scope);
		if (size == 0 || size == 1) return 0.0;
		final IShape g1 = geometries.firstValue(scope);
		final IShape g2 = geometries.lastValue(scope);
		return t.directionInDegreesTo(scope, g1, g2);
	}

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param graph
	 *            the graph
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "path_between",
			type = IType.PATH,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_RELATIONS, IOperatorCategory.PATH },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.AGENT_LOCATION,
					IConcept.TOPOLOGY })
	@doc (
			value = "A path between two geometries (geometries, agents or points) considering a topology.",
			examples = { @example (
					value = "my_topology path_between (ag1, ag2)",
					equals = "A path between ag1 and ag2",
					isExecutable = false) },
			see = { "towards", "direction_to", "distance_between", "direction_between", "path_to", "distance_to" })
	@no_test // Test already done in Spatial Tests Models.
	public static IPath path_between(final IScope scope, final ITopology graph, final IShape source,
			final IShape target) throws GamaRuntimeException {
		return graph.pathBetween(scope, source, target);

	}

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param topo
	 *            the topo
	 * @param nodes
	 *            the nodes
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "path_between",
			type = IType.PATH,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_RELATIONS, IOperatorCategory.PATH },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.AGENT_LOCATION,
					IConcept.TOPOLOGY })
	@doc (
			value = "A path between a list of several geometries (geometries, agents or points) considering a topology.",
			examples = { @example (
					value = "my_topology path_between [ag1, ag2]",
					equals = "A path between ag1 and ag2",
					isExecutable = false) },
			see = { "towards", "direction_to", "distance_between", "direction_between", "path_to", "distance_to" })
	@no_test // test already done in Spatial tests models
	public static IPath path_between(final IScope scope, final ITopology topo, final IContainer<?, IShape> nodes)
			throws GamaRuntimeException {
		if (nodes.isEmpty(scope)) return null;
		final int n = nodes.length(scope);
		final IShape source = nodes.firstValue(scope);
		if (n == 1) return PathFactory.newInstance(scope, scope.getTopology(), source, source,
				GamaListFactory.<IShape> create(Types.GEOMETRY));
		final IShape target = nodes.lastValue(scope);
		if (n == 2) return topo.pathBetween(scope, source, target);
		final IList<IShape> edges = GamaListFactory.create(Types.GEOMETRY);
		IShape previous = null;
		for (final IShape gg : nodes.iterable(scope)) {
			if (previous != null) {
				// TODO Take the case of GamaPoint
				final GamaSpatialPath path = topo.pathBetween(scope, previous, gg);
				if (path != null && path.getEdgeList() != null) { edges.addAll(path.getEdgeList()); }
			}
			previous = gg;
		}

		final GamaSpatialPath path = PathFactory.newInstance(scope, topo, source, target, edges);
		path.setWeight(path.getVertexList().size());
		return path;
	}

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param cells
	 *            the cells
	 * @param nodes
	 *            the nodes
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "path_between",

			category = { IOperatorCategory.GRID, IOperatorCategory.PATH },
			concept = { IConcept.GRID })
	@doc (
			value = "The shortest path between several objects according to set of cells",
			masterDoc = true,
			examples = { @example (
					value = "path_between (cell_grid where each.is_free, [ag1, ag2, ag3])",
					equals = "A path between ag1 and ag2 and ag3 passing through the given cell_grid agents",
					isExecutable = false) })
	@no_test // test already done in Spatial tests models
	public static IPath path_between(final IScope scope, final IList<IAgent> cells,
			final IContainer<?, IShape> nodes) throws GamaRuntimeException {
		if (cells == null || cells.isEmpty() || nodes.isEmpty(scope)) return null;
		final ITopology topo = cells.get(0).getTopology();

		final int n = nodes.length(scope);
		final IShape source = nodes.firstValue(scope);
		if (n == 1) {
			if (topo instanceof GridTopology gt)
				return gt.pathBetween(scope, source, source, cells);
			return scope.getTopology().pathBetween(scope, source, source);
		}
		final IShape target = nodes.lastValue(scope);
		if (n == 2) {
			if (topo instanceof GridTopology gt)
				return gt.pathBetween(scope, source, target, cells);
			return scope.getTopology().pathBetween(scope, source, target);
		}
		final IList<IShape> edges = GamaListFactory.create(Types.GEOMETRY);
		IShape previous = null;
		double weight = 0;
		for (final IShape gg : nodes.iterable(scope)) {
			if (previous != null) {
				// TODO Take the case of GamaPoint
				if (topo instanceof GridTopology gt) {
					final GamaSpatialPath path = gt.pathBetween(scope, previous, gg, cells);
					edges.addAll(path.getEdgeList());
					weight += path.getWeight();
				} else {
					edges.addAll(scope.getTopology().pathBetween(scope, previous, gg).getEdgeList());
				}
			}
			previous = gg;
		}
		final GamaSpatialPath path = PathFactory.newInstance(scope,
				topo instanceof GridTopology ? topo : scope.getTopology(), source, target, edges);
		path.setWeight(topo instanceof GridTopology ? weight : path.getVertexList().size());
		return path;
	}

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param cells
	 *            the cells
	 * @param nodes
	 *            the nodes
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "path_between",

			category = { IOperatorCategory.GRID, IOperatorCategory.PATH },
			concept = { IConcept.GRID })
	@doc (
			value = "The shortest path between several objects according to set of cells with corresponding weights",
			masterDoc = true,
			examples = { @example (
					value = "path_between (cell_grid as_map (each::each.is_obstacle ? 9999.0 : 1.0), [ag1, ag2, ag3])",
					equals = "A path between ag1 and ag2 and ag3 passing through the given cell_grid agents with minimal cost",
					isExecutable = false) })
	@no_test // test already done in Spatial tests models
	public static IPath path_between(final IScope scope, final IMap<IAgent, Object> cells,
			final IContainer<?, IShape> nodes) throws GamaRuntimeException {
		if (cells == null || cells.isEmpty() || nodes.isEmpty(scope)) return null;
		return path_between(scope, cells.getKeys(), nodes);
	}

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param cells
	 *            the cells
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "path_between",

			category = { IOperatorCategory.GRID, IOperatorCategory.PATH },
			concept = { IConcept.GRID })
	@doc (
			value = "The shortest path between two objects according to set of cells",
			masterDoc = true,
			examples = { @example (
					value = "path_between (cell_grid where each.is_free, ag1, ag2)",
					equals = "A path between ag1 and ag2 passing through the given cell_grid agents",
					isExecutable = false) })
	@no_test // test already done in Spatial tests models
	public static IPath path_between(final IScope scope, final IList<IAgent> cells, final IShape source,
			final IShape target) throws GamaRuntimeException {
		if (cells == null || cells.isEmpty() || source == null || target == null) return null;
		final ITopology topo = cells.get(0).getTopology();
		if (topo instanceof GridTopology gt) return gt.pathBetween(scope, source, target, cells);
		return scope.getTopology().pathBetween(scope, source, target);
	}

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param cells
	 *            the cells
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "path_between",

			category = { IOperatorCategory.GRID, IOperatorCategory.PATH },
			concept = { IConcept.GRID })
	@doc (
			value = "The shortest path between two objects according to set of cells with corresponding weights",
			masterDoc = true,
			examples = { @example (
					value = "path_between (cell_grid as_map (each::each.is_obstacle ? 9999.0 : 1.0), ag1, ag2)",
					equals = "A path between ag1 and ag2 passing through the given cell_grid agents with a minimal cost",
					isExecutable = false) })
	@no_test // test already done in Spatial tests models
	public static IPath path_between(final IScope scope, final IMap<IAgent, Object> cells, final IShape source,
			final IShape target) throws GamaRuntimeException {
		if (cells == null || cells.isEmpty()) return null;
		return path_between(scope, cells.getKeys(), source, target);
	}

	/**
	 * Distance to.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the double
	 */
	@operator (
			value = "distance_to",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_RELATIONS },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.AGENT_LOCATION,
					IConcept.TOPOLOGY })
	@doc (
			value = "A distance between two geometries (geometries, agents or points) considering the topology of the agent applying the operator.",
			masterDoc = true,
			examples = { @example (
					value = "ag1 distance_to ag2",
					equals = "the distance between ag1 and ag2 considering the topology of the agent applying the operator",
					isExecutable = false) },
			see = { "towards", "direction_to", "distance_between", "direction_between", "path_between", "path_to" })
	@no_test // test already done in Spatial tests models
	public static Double distance_to(final IScope scope, final IShape source, final IShape target) {
		return scope.getTopology().distanceBetween(scope, source, target);
	}

	/**
	 * Distance to.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the double
	 */
	@operator (
			value = "distance_to",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_RELATIONS })
	@doc (
			value = "An Euclidean distance between two points.")
	// No documentation because it is same same as the previous one (but
	// optimized for points?)
	@test (" {20,20} distance_to {30,30} = 14.142135623730951")
	public static Double distance_to(final IScope scope, final GamaPoint source, final GamaPoint target) {
		return scope.getTopology().distanceBetween(scope, source, target);
	}

	/**
	 * Path to.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param g1
	 *            the g 1
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "path_to",
			type = IType.PATH,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_RELATIONS, IOperatorCategory.PATH },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.AGENT_LOCATION,
					IConcept.TOPOLOGY })
	@doc (
			value = "A path between two geometries (geometries, agents or points) considering the topology of the agent applying the operator.",
			masterDoc = true,
			examples = { @example (
					value = "ag1 path_to ag2",
					equals = "the path between ag1 and ag2 considering the topology of the agent applying the operator",
					isExecutable = false) },
			see = { "towards", "direction_to", "distance_between", "direction_between", "path_between",
					"distance_to" })
	@no_test // test already done in Spatial tests models
	public static IPath path_to(final IScope scope, final IShape g, final IShape g1) throws GamaRuntimeException {
		if (g == null) return null;
		return scope.getTopology().pathBetween(scope, g, g1);
	}

	/**
	 * Path to.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param g1
	 *            the g 1
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "path_to",
			type = IType.PATH,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_RELATIONS, IOperatorCategory.PATH },
			concept = { IConcept.SHORTEST_PATH })
	@doc (
			value = "A shortest path between two points considering the topology of the agent applying the operator.")
	// No documentation because it is same same as the previous one (but
	// optimized for points?)
	@no_test // test already done in Spatial tests models
	public static IPath path_to(final IScope scope, final GamaPoint g, final GamaPoint g1)
			throws GamaRuntimeException {
		if (g == null) return null;
		return scope.getTopology().pathBetween(scope, g, g1);
	}

}
