/*******************************************************************************************************
 *
 * InternalGamaGraphFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.graph;

import java.util.Map;

import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaPairFactory;
import gama.api.data.factories.IGraphFactory;
import gama.api.data.objects.IGraph;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPair;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.core.topology.graph.GamaSpatialGraph;

/**
 * A factory for creating GamaGraph objects.
 */
public class InternalGamaGraphFactory implements IGraphFactory {

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
	 * @return the i graph
	 */
	@Override
	public IGraph createFrom(final IScope scope, final Object obj, final Object param, final boolean copy) {
		// param = true : spatial.

		if (obj == null) return null;
		if (obj instanceof IGraph) return (IGraph) obj;
		final boolean spatial = param != null && Cast.asBool(scope, param);
		if (obj instanceof IList) return createFromList(scope, (IList) obj, spatial);
		if (obj instanceof IVarExpression ve) // in this case, attempt to decode it !
			return (IGraph) ve.value(scope);
		if (obj instanceof IMap) return createFromMap(scope, (IMap) obj, spatial);

		return null;
	}

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param spatial
	 *            the spatial
	 * @return the i graph
	 */
	@Override
	public IGraph createFromMap(final IScope scope, final IMap<?, ?> obj, final boolean spatial) {
		final IGraph result = spatial
				? new GamaSpatialGraph(GamaListFactory.create(Types.NO_TYPE), false, false, false, null, null, scope,
						obj.getGamlType().getKeyType(), Types.NO_TYPE)
				: new GamaGraph(scope, GamaListFactory.create(Types.NO_TYPE), false, false, false, null, null,
						obj.getGamlType().getKeyType(), Types.NO_TYPE);
		final IPair p = GamaPairFactory.createNull();
		for (final Map.Entry<?, ?> k : obj.entrySet()) {
			p.setKey(k.getKey());
			p.setValue(k.getValue());
			result.addEdge(p);
		}
		return result;
	}

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param spatial
	 *            the spatial
	 * @return the i graph
	 */
	@Override
	public IGraph createFromList(final IScope scope, final IList obj, final boolean spatial) {
		final IType nodeType = obj.getGamlType().getContentType();
		return spatial ? new GamaSpatialGraph(obj, false, false, false, null, null, scope, nodeType, Types.NO_TYPE)
				: new GamaGraph(scope, obj, false, false, false, null, null, nodeType, Types.NO_TYPE);
	}

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param edgesOrVertices
	 *            the edges or vertices
	 * @param byEdge
	 *            the by edge
	 * @param directed
	 *            the directed
	 * @param spatial
	 *            the spatial
	 * @param nodeType
	 *            the node type
	 * @param edgeType
	 *            the edge type
	 * @return the i graph
	 */
	@Override
	public IGraph createFromList(final IScope scope, final IList edgesOrVertices, final boolean byEdge,
			final boolean directed, final boolean spatial, final IType nodeType, final IType edgeType) {
		return spatial
				? new GamaSpatialGraph(edgesOrVertices, byEdge, directed, false, null, null, scope, nodeType, edgeType)
				: new GamaGraph(scope, edgesOrVertices, byEdge, directed, false, null, null, nodeType, edgeType);
	}

	/**
	 * As directed graph.
	 *
	 * @param source
	 *            the source
	 * @return the i graph
	 */
	@Override
	public IGraph asDirectedGraph(final IGraph source) {
		source.setDirected(true);
		return source;
	}

	/**
	 * As undirected graph.
	 *
	 * @param source
	 *            the source
	 * @return the i graph
	 */
	@Override
	public IGraph asUndirectedGraph(final IGraph source) {
		source.setDirected(false);
		return source;
	}

}
