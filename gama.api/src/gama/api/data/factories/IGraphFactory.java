/**
 * 
 */
package gama.api.data.factories;

import gama.api.data.objects.IGraph;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * 
 */
public interface IGraphFactory {

	/**
	 * Creates an undirected graph view or copy of a source graph.
	 *
	 * @param source
	 *            the source graph
	 * @return an undirected graph
	 */
	IGraph asUndirectedGraph(final IGraph source);

	/**
	 * Creates a directed graph view or copy of a source graph.
	 *
	 * @param source
	 *            the source graph
	 * @return a directed graph
	 */
	IGraph asDirectedGraph(final IGraph source);

	/**
	 * Creates a graph from a list of edges or vertices.
	 *
	 * @param scope
	 *            the scope
	 * @param edgesOrVertices
	 *            list of objects to be interpreted as edges or vertices
	 * @param byEdge
	 *            if true, the list contains edges; otherwise, vertices
	 * @param directed
	 *            whether the created graph should be directed
	 * @param spatial
	 *            whether the graph should be spatial
	 * @param nodeType
	 *            the type constraint for nodes
	 * @param edgeType
	 *            the type constraint for edges
	 * @return the created graph
	 */
	IGraph createFromList(final IScope scope, final IList edgesOrVertices, final boolean byEdge, final boolean directed, final boolean spatial, final IType nodeType, final IType edgeType);

	/**
	 * Creates a graph from a list, guessing the structure (edges or vertices).
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the list of objects
	 * @param spatial
	 *            whether the graph is spatial
	 * @return the created graph
	 */
	IGraph createFromList(final IScope scope, final IList obj, final boolean spatial);

	/**
	 * Creates a graph from a map where keys are source nodes and values are target nodes (or edges).
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the map defining connections
	 * @param spatial
	 *            whether the graph is spatial
	 * @return the created graph
	 */
	IGraph createFromMap(final IScope scope, final IMap<?, ?> obj, final boolean spatial);

	/**
	 * General purpose creation/casting method to create a graph from an arbitrary object.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the source object (could be another graph, list, map, file...)
	 * @param param
	 *            additional parameters for creation (e.g. node type)
	 * @param copy
	 *            whether to copy the data or reference it
	 * @return the created graph
	 */
	IGraph createFrom(final IScope scope, final Object obj, final Object param, final boolean copy);

}