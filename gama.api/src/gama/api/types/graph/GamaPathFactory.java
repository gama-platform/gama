/*******************************************************************************************************
 *
 * GamaPathFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.types.topology.ITopology;

/**
 * Static factory for creating {@link IPath} instances.
 * 
 * <p>
 * GamaPathFactory provides a unified entry point for creating paths in GAMA. It supports
 * creating paths from various inputs including vertex lists, edge lists, and topologies.
 * The factory delegates to an {@link IPathFactory} implementation that must be configured
 * before use.
 * </p>
 * 
 * <h2>Path Creation Methods</h2>
 * <p>
 * The factory provides several ways to create paths:
 * </p>
 * 
 * <h3>From Graph and Vertices</h3>
 * <ul>
 * <li>Create a path by specifying a list of vertices in order</li>
 * <li>Edges are inferred from consecutive vertices in the graph</li>
 * <li>Useful when you know the vertex sequence but not the specific edges</li>
 * </ul>
 * 
 * <h3>From Graph and Edges</h3>
 * <ul>
 * <li>Create a path by specifying start, target, and a list of edges</li>
 * <li>Edges must form a valid path from start to target</li>
 * <li>Optionally modify edges to ensure path validity</li>
 * <li>Typical output from pathfinding algorithms</li>
 * </ul>
 * 
 * <h3>From Topology (Spatial Paths)</h3>
 * <ul>
 * <li>Create paths in spatial topologies with geometric shapes</li>
 * <li>Vertices and edges are {@link IShape} objects with locations</li>
 * <li>Path has geometric properties (length, shape, segments)</li>
 * <li>Used for agent movement in spatial environments</li>
 * </ul>
 * 
 * <h3>From Generic Objects (Casting)</h3>
 * <ul>
 * <li>Convert various objects to paths (lists, geometries, etc.)</li>
 * <li>Flexible conversion with optional parameters</li>
 * <li>Used by GAML type casting system</li>
 * </ul>
 * 
 * <h2>Factory Configuration</h2>
 * <p>
 * Before use, an {@link IPathFactory} implementation must be registered using
 * {@link #setBuilder(IPathFactory)}. This is typically done during system initialization.
 * </p>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Creating a path from vertices:</h3>
 * <pre>
 * IGraph graph = ...;
 * IList vertices = GamaListFactory.create(scope, Types.NO_TYPE);
 * vertices.add(v1); vertices.add(v2); vertices.add(v3);
 * IPath path = GamaPathFactory.createFrom(graph, vertices);
 * </pre>
 * 
 * <h3>Creating a path from edges:</h3>
 * <pre>
 * IGraph graph = ...;
 * IList edges = ...; // from pathfinding algorithm
 * IPath path = GamaPathFactory.createFrom(graph, source, target, edges);
 * </pre>
 * 
 * <h3>Creating a spatial path:</h3>
 * <pre>
 * ITopology topology = ...;
 * IList shapeNodes = ...;
 * IPath path = GamaPathFactory.createFrom(scope, topology, shapeNodes, weight);
 * </pre>
 * 
 * @see IPath
 * @see IPathFactory
 * @see IGraph
 * @see ITopology
 * @author drogoul
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class GamaPathFactory {

	/** The Internal factory. */
	static IPathFactory InternalFactory;

	/**
	 * Sets the builder.
	 *
	 * @param builder
	 *            the new builder
	 */
	public static void setBuilder(final IPathFactory builder) { InternalFactory = builder; }

	/**
	 * New instance.
	 *
	 * @param <V>
	 *            the value type
	 * @param <E>
	 *            the element type
	 * @param g
	 *            the g
	 * @param nodes
	 *            the nodes
	 * @return the gama path
	 */
	public static <V, E> IPath<V, E, IGraph<V, E>> createFrom(final IGraph<V, E> g, final IList<? extends V> nodes) {
		return InternalFactory.createFrom(g, nodes);
	}

	/**
	 * New instance.
	 *
	 * @param <V>
	 *            the value type
	 * @param <E>
	 *            the element type
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param edges
	 *            the edges
	 * @return the gama path
	 */
	public static <V, E> IPath<V, E, IGraph<V, E>> createFrom(final IGraph<V, E> g, final V start, final V target,
			final IList<E> edges) {
		return InternalFactory.createFrom(g, start, target, edges);
	}

	/**
	 * New instance.
	 *
	 * @param <V>
	 *            the value type
	 * @param <E>
	 *            the element type
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param edges
	 *            the edges
	 * @param modify_edges
	 *            the modify edges
	 * @return the gama path
	 */
	public static <V, E> IPath<V, E, IGraph<V, E>> createFrom(final IGraph<V, E> g, final V start, final V target,
			final IList<E> edges, final boolean modify_edges) {
		return InternalFactory.createFrom(g, start, target, edges, modify_edges);
	}

	/**
	 * New instance.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param nodes
	 *            the nodes
	 * @param weight
	 *            the weight
	 * @return the gama spatial path
	 */
	// With Topology
	public static IPath createFrom(final IScope scope, final ITopology g, final IList<? extends IShape> nodes,
			final double weight) {
		return InternalFactory.createFrom(scope, g, nodes, weight);
	}

	/**
	 * New instance.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param edges
	 *            the edges
	 * @return the gama spatial path
	 */
	public static IPath createFrom(final IScope scope, final ITopology g, final IShape start, final IShape target,
			final IList<IShape> edges) {
		return InternalFactory.createFrom(scope, g, start, target, edges);
	}

	/**
	 * New instance.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param edges
	 *            the edges
	 * @param modify_edges
	 *            the modify edges
	 * @return the gama spatial path
	 */
	public static IPath createFrom(final IScope scope, final ITopology g, final IShape start, final IShape target,
			final IList<IShape> edges, final boolean modify_edges) {
		return InternalFactory.createFrom(scope, g, start, target, edges, modify_edges);
	}

	/**
	 * New instance.
	 *
	 * @param scope
	 *            the scope
	 * @param edgesNodes
	 *            the edges nodes
	 * @param isEdges
	 *            the is edges
	 * @return the i path
	 */
	public static IPath createFrom(final IScope scope, final IList<? extends IShape> edgesNodes,
			final boolean isEdges) {
		return InternalFactory.createFrom(scope, edgesNodes, isEdges);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the i path
	 */
	public static IPath castToPath(final IScope scope, final Object obj, final Object param, final boolean copy) {
		return InternalFactory.createFrom(scope, obj, param, copy);
	}

}
