/*******************************************************************************************************
 *
 * GamaPathType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.type;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.graph.GamaPathFactory;
import gama.api.types.graph.IPath;

/**
 * Type representing paths in GAML - ordered sequences representing routes or trajectories in graphs or space.
 * <p>
 * Paths are fundamental for navigation and movement in GAMA models. They represent ordered routes through space or
 * graphs, typically computed by pathfinding algorithms. Paths can be used for agent movement, visualization of routes,
 * and analysis of connectivity.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Ordered sequence of locations or graph edges</li>
 * <li>Integration with graph pathfinding algorithms</li>
 * <li>Distance and weight calculations</li>
 * <li>Support for both spatial and graph-based paths</li>
 * <li>Drawable for visualization</li>
 * <li>Can be used directly for agent movement</li>
 * </ul>
 * 
 * <h2>Path Types:</h2>
 * <ul>
 * <li><b>Spatial paths</b> - sequences of points in continuous space</li>
 * <li><b>Graph paths</b> - sequences of edges/vertices in a graph</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Compute path between two points in a graph
 * graph road_network <- as_edge_graph(roads);
 * path route <- path_between(road_network, source_location, target_location);
 * 
 * // Create path from list of points
 * path trajectory <- path([{10,10}, {20,20}, {30,15}]);
 * 
 * // From agent geometry
 * path agent_shape_path <- path(my_agent);
 * 
 * // Use path for movement
 * do follow path: route speed: 5.0;
 * 
 * // Path properties
 * float path_length <- route.distance;
 * list<point> vertices <- route.vertices;
 * list edges <- route.edges;
 * graph source_graph <- route.graph;
 * 
 * // Shortest path using Dijkstra
 * path shortest <- path_between(my_graph, node1, node2);
 * 
 * // A* pathfinding
 * path optimized <- path_between(my_graph, start, goal) using topology(world);
 * 
 * // Draw path
 * draw route color: #red width: 2;
 * }
 * </pre>
 * 
 * <h2>Pathfinding Integration:</h2>
 * <p>
 * Paths are typically created by pathfinding operators:
 * <ul>
 * <li>path_between - computes shortest path in a graph</li>
 * <li>path_to - computes path to a target using agent's movement graph</li>
 * <li>goto - automatically computes and follows a path</li>
 * </ul>
 * </p>
 * 
 * @author GAMA Development Team
 * @see GamaType
 * @see IPath
 * @see gama.api.types.graph.GamaPathFactory
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.PATH,
		id = IType.PATH,
		wraps = { IPath.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE },
		doc = @doc ("Ordered lists of objects that represent a path in a graph"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPathType extends GamaType<IPath> {

	/**
	 * Constructs a new path type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaPathType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a path.
	 * <p>
	 * This method supports casting from various source types:
	 * <ul>
	 * <li>Path - returns the path itself</li>
	 * <li>Geometry or Agent - creates a path from the list of points defining the shape</li>
	 * <li>List - casts each element to a point and creates a path from these points</li>
	 * </ul>
	 * The param argument can specify the graph context for the path.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a path
	 * @param param
	 *            optional graph parameter for path context
	 * @param copy
	 *            whether to create a copy if obj is already a path
	 * @return the path representation of the object, or null if casting fails
	 * @throws GamaRuntimeException
	 *             if the casting operation encounters an error
	 */
	@doc (
			value = "Cast any object as a path",
			usages = { @usage (
					value = "if the operand is a path, returns this path"),
					@usage (
							value = "if the operand is a geometry of an agent, returns a path from the list of points of the geometry"),
					@usage (
							value = "if the operand is a list, cast each element of the list as a point and create a path from these points",
							examples = { @example ("path p <- path([{12,12},{30,30},{50,50}]);") }) })
	@Override
	public IPath cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaPathFactory.castToPath(scope, obj, param, copy);
	}

	/**
	 * Returns the default value for path type.
	 * <p>
	 * The default path is null, as there is no meaningful default path.
	 * </p>
	 * 
	 * @return null
	 */
	@Override
	public IPath getDefault() { return null; }

	/**
	 * Indicates whether paths can be drawn/visualized.
	 * <p>
	 * Paths are drawable and can be displayed as lines connecting their vertices.
	 * </p>
	 * 
	 * @return true, paths can be visualized
	 */
	@Override
	public boolean isDrawable() { return true; }

	/**
	 * Indicates whether paths can be cast to constant values.
	 * <p>
	 * Paths cannot be constant as they may depend on dynamic graph structures.
	 * </p>
	 * 
	 * @return false, paths are not constant
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

}
