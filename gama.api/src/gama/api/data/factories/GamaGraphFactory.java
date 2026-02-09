/*******************************************************************************************************
 *
 * GamaGraphFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import gama.api.data.objects.IGraph;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * A static factory for creating and managing {@link IGraph} instances.
 * This class provides methods to create graphs from various data structures (lists, maps)
 * and to perform basic graph transformations (directed/undirected). It delegates to an {@link IGraphFactory}.
 */
public class GamaGraphFactory implements IFactory<IGraph> {

	/**
	 * The internal factory used for creating graph instances.
	 */
	private static IGraphFactory InternalFactory;

	/**
	 * Configures the internal factory implementation.
	 *
	 * @param builder
	 *            the {@link IGraphFactory} to be used as the internal builder.
	 */
	public static void setBuilder(final IGraphFactory builder) { InternalFactory = builder; }

	/**
	 * Creates a graph from a generic object, handling various input types.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param obj
	 *            the source object (e.g., list of edges, map of adjacencies).
	 * @param param
	 *            additional parameters influencing creation (optional).
	 * @param copy
	 *            whether to copy the source if it is already a graph.
	 * @return the created {@link IGraph} instance.
	 */
	public static IGraph createFrom(final IScope scope, final Object obj, final Object param, final boolean copy) {
		return InternalFactory.createFrom(scope, obj, param, copy);
	}

	/**
	 * Creates a graph from a map representation.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param obj
	 *            the map defining the graph structure (e.g., node to neighbors).
	 * @param spatial
	 *            whether the graph should be spatial (nodes have locations).
	 * @return the created {@link IGraph} instance.
	 */
	public static IGraph createFromMap(final IScope scope, final IMap<?, ?> obj, final boolean spatial) {
		return InternalFactory.createFromMap(scope, obj, spatial);
	}

	/**
	 * Creates a graph from a list of elements (either vertices or edges).
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param obj
	 *            the list of elements.
	 * @param spatial
	 *            whether the graph should be spatial.
	 * @return the created {@link IGraph} instance.
	 */
	public static IGraph createFromList(final IScope scope, final IList obj, final boolean spatial) {
		return InternalFactory.createFromList(scope, obj, spatial);
	}

	/**
	 * Creates a graph from a list with detailed configuration.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param edgesOrVertices
	 *            the list of edges or vertices.
	 * @param byEdge
	 *            true if the list contains edges, false if it contains vertices.
	 * @param directed
	 *            true if the graph should be directed.
	 * @param spatial
	 *            true if the graph should be spatial.
	 * @param nodeType
	 *            the type content of the nodes.
	 * @param edgeType
	 *            the type content of the edges.
	 * @return the created {@link IGraph} instance.
	 */
	public static IGraph createFromList(final IScope scope, final IList edgesOrVertices, final boolean byEdge,
			final boolean directed, final boolean spatial, final IType nodeType, final IType edgeType) {
		return InternalFactory.createFromList(scope, edgesOrVertices, byEdge, directed, spatial, nodeType, edgeType);
	}

	/**
	 * Converts a graph to a directed graph (in place modification).
	 *
	 * @param source
	 *            the graph to modify.
	 * @return the modified graph (now directed).
	 */
	public static IGraph asDirectedGraph(final IGraph source) {
		source.setDirected(true);
		return source;
	}

	/**
	 * Converts a graph to an undirected graph (in place modification).
	 *
	 * @param source
	 *            the graph to modify.
	 * @return the modified graph (now undirected).
	 */
	public static IGraph asUndirectedGraph(final IGraph source) {
		source.setDirected(false);
		return source;
	}

}