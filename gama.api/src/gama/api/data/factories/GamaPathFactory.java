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
package gama.api.data.factories;

import gama.api.data.objects.IGraph;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPath;
import gama.api.data.objects.IShape;
import gama.api.kernel.topology.ITopology;
import gama.api.runtime.scope.IScope;

/**
 * A factory for creating Path objects.
 */

/**
 * A factory for creating GamaPath objects.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class GamaPathFactory implements IFactory<IPath> {

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
	public static IPath createFrom(final IScope scope, final Object obj, final Object param, final boolean copy) {
		return InternalFactory.createFrom(scope, obj, param, copy);
	}

}
