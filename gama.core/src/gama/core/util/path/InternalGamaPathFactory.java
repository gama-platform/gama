/*******************************************************************************************************
 *
 * InternalGamaPathFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.path;

import static gama.api.utils.geometry.GeometryUtils.getFirstPointOf;
import static gama.api.utils.geometry.GeometryUtils.getLastPointOf;

import java.util.List;

import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.factories.IPathFactory;
import gama.api.data.objects.IGraph;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPath;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.gaml.types.Types;
import gama.api.kernel.topology.ITopology;
import gama.api.runtime.scope.IScope;
import gama.core.geometry.GamaShape;
import gama.core.topology.graph.GamaSpatialGraph;
import gama.core.topology.graph.GraphTopology;

/**
 * A factory for creating Path objects.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class InternalGamaPathFactory implements IPathFactory {

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
	@Override
	public <V, E> IPath<V, E, IGraph<V, E>> createFrom(final IGraph<V, E> g, final IList<? extends V> nodes) {
		if (nodes.isEmpty() && g instanceof GamaSpatialGraph || nodes.get(0) instanceof IPoint
				|| g instanceof GamaSpatialGraph)
			return (IPath) new GamaSpatialPath((GamaSpatialGraph) g, (IList<IShape>) nodes);
		return new GamaPath<>(g, nodes);
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
	@Override
	public <V, E> IPath<V, E, IGraph<V, E>> createFrom(final IGraph<V, E> g, final V start, final V target,
			final IList<E> edges) {
		if (g instanceof GamaSpatialGraph) {
			edges.removeIf(e -> e == null);
			return (IPath) new GamaSpatialPath((GamaSpatialGraph) g, (IShape) start, (IShape) target,
					(IList<IShape>) edges);
		}
		return new GamaPath<>(g, start, target, edges);
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
	@Override
	public <V, E> IPath<V, E, IGraph<V, E>> createFrom(final IGraph<V, E> g, final V start, final V target,
			final IList<E> edges, final boolean modify_edges) {
		if (g instanceof GamaSpatialGraph) return (IPath) new GamaSpatialPath((GamaSpatialGraph) g, (IShape) start,
				(IShape) target, (IList<IShape>) edges, modify_edges);
		return new GamaPath<>(g, start, target, edges, modify_edges);
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
	@Override
	public IPath createFrom(final IScope scope, final ITopology g, final IList<? extends IShape> nodes,
			final double weight) {
		IPath path;
		if (g instanceof GraphTopology gt) {
			path = createFrom(gt.getPlaces(), nodes);
		} else {
			path = new GamaSpatialPath(null, nodes);
		}
		path.setWeight(weight);
		return path;
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
	@Override
	public IPath createFrom(final IScope scope, final ITopology g, final IShape start, final IShape target,
			final IList<IShape> edges) {
		if (g instanceof GraphTopology) return createFrom(((GraphTopology) g).getPlaces(), start, target, edges);
		return new GamaSpatialPath(start, target, edges);
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
	@Override
	public IPath createFrom(final IScope scope, final ITopology g, final IShape start, final IShape target,
			final IList<IShape> edges, final boolean modify_edges) {
		if (g instanceof GraphTopology)
			return createFrom(((GraphTopology) g).getPlaces(), start, target, edges, modify_edges);
		// AmorphousTopology ) {
		return new GamaSpatialPath(null, start, target, edges, modify_edges);
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
	@Override
	public IPath createFrom(final IScope scope, final IList<? extends IShape> edgesNodes, final boolean isEdges) {
		if (isEdges) {
			final GamaShape shapeS = (GamaShape) edgesNodes.get(0).getGeometry();
			final GamaShape shapeT = (GamaShape) edgesNodes.get(edgesNodes.size() - 1).getGeometry();
			return new GamaSpatialPath(null, getFirstPointOf(shapeS), getLastPointOf(shapeT), edgesNodes, false);
		}
		return new GamaSpatialPath(edgesNodes);
	}

	/**
	 * Creates a new InternalPath object.
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
	public IPath createFrom(final IScope scope, final Object obj, final Object param, final boolean copy) {
		if (obj instanceof IPath p) return p;
		if (obj instanceof IShape shape) return createFrom(scope, shape.getPoints(), false);
		if (obj instanceof List) {
			final List<IShape> list = GamaListFactory.create(Types.GEOMETRY);
			boolean isEdges = true;
			for (final Object p : (List) obj) {
				list.add(GamaPointFactory.toPoint(scope, p));
				if (isEdges && (!(p instanceof IShape) || !((IShape) p).isLine())) { isEdges = false; }
			}
			return createFrom(scope, isEdges ? (IList<IShape>) obj : (IList<IShape>) list, isEdges);
		}
		return null;
	}

}
