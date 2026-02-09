/*******************************************************************************************************
 *
 * IPathFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import gama.api.data.objects.IGraph;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPath;
import gama.api.data.objects.IShape;
import gama.api.kernel.topology.ITopology;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public interface IPathFactory {

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
	<V, E> IPath<V, E, IGraph<V, E>> createFrom(IGraph<V, E> g, IList<? extends V> nodes);

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
	<V, E> IPath<V, E, IGraph<V, E>> createFrom(IGraph<V, E> g, V start, V target, IList<E> edges);

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
	<V, E> IPath<V, E, IGraph<V, E>> createFrom(IGraph<V, E> g, V start, V target, IList<E> edges,
			boolean modify_edges);

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
	IPath createFrom(IScope scope, ITopology g, IList<? extends IShape> nodes, double weight);

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
	IPath createFrom(IScope scope, ITopology g, IShape start, IShape target, IList<IShape> edges);

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
	IPath createFrom(IScope scope, ITopology g, IShape start, IShape target, IList<IShape> edges, boolean modify_edges);

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
	IPath createFrom(IScope scope, IList<? extends IShape> edgesNodes, boolean isEdges);

	/**
	 * Creates a new IPath object.
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
	IPath createFrom(final IScope scope, final Object obj, final Object param, final boolean copy);

}