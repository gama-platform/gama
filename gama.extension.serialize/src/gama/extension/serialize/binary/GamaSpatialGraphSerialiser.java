/*******************************************************************************************************
 *
 * GamaSpatialGraphSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.core.topology.graph.GamaSpatialGraph;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * FST serialiser for {@link GamaSpatialGraph} instances.
 *
 * <p>
 * Extends the generic graph serialisation with the two fields that are specific to
 * {@link GamaSpatialGraph}: the snap {@code tolerance} used when locating vertices by coordinate,
 * and the {@code verticesBuilt} lookup table that maps location hash-codes to vertex shapes. The
 * table is rebuilt transparently during deserialisation by calling
 * {@link GamaSpatialGraph#addBuiltVertex(IShape)} for every restored vertex, so it does not need to
 * be written to the stream explicitly.
 * </p>
 *
 * <p>
 * This serialiser is registered for the concrete class {@link GamaSpatialGraph} and therefore takes
 * priority over the more generic {@link IGraphSerialiser} (which is registered for {@link
 * gama.api.types.graph.IGraph}) when FST walks the class lineage.
 * </p>
 *
 * <p>
 * On-stream layout:
 * <ol>
 * <li>vertex {@link IType} (object)</li>
 * <li>edge {@link IType} (object)</li>
 * <li>directed flag (boolean)</li>
 * <li>tolerance (double)</li>
 * <li>vertex count (int)</li>
 * <li>for each vertex: vertex {@link IShape} (object) + vertex weight (double)</li>
 * <li>edge count (int)</li>
 * <li>for each edge: edge {@link IShape} (object) + source {@link IShape} (object) + target
 * {@link IShape} (object) + edge weight (double)</li>
 * </ol>
 * </p>
 *
 * <p>
 * Objects deserialised by this serialiser are not registered for FST back-reference tracking.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 avril 2026
 */
class GamaSpatialGraphSerialiser extends FSTIndividualSerialiser<GamaSpatialGraph> {

	/**
	 * Constructs a new {@code GamaSpatialGraphSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser
	 */
	GamaSpatialGraphSerialiser(final BinarySerialiser serialiser) {
		super(serialiser);
	}

	/**
	 * Returns {@code false}: spatial graphs are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	protected boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises the spatial graph's vertex type, edge type, directed flag, tolerance, all vertices with their weights,
	 * and all edges with their source, target, and weight.
	 *
	 * @param out
	 *            the FST output stream
	 * @param g
	 *            the spatial graph to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final FSTObjectOutput out, final GamaSpatialGraph g) throws Exception {
		// --- metadata ---
		out.writeObject(g.getGamlType().getKeyType());
		out.writeObject(g.getGamlType().getContentType());
		out.writeBoolean(g.isDirected());
		out.writeDouble(g.getTolerance());
		// --- vertices ---
		out.writeInt(g.vertexSet().size());
		for (Object v : g.vertexSet()) {
			out.writeObject(v);
			out.writeDouble(g.getVertexWeight(v));
		}
		// --- edges ---
		out.writeInt(g.edgeSet().size());
		for (Object e : g.edgeSet()) {
			out.writeObject(e);
			out.writeObject(g.getEdgeSource(e));
			out.writeObject(g.getEdgeTarget(e));
			out.writeDouble(g.getEdgeWeight(e));
		}
	}

	/**
	 * Deserialises a spatial graph by reading its vertex type, edge type, directed flag, tolerance, vertices with
	 * weights, and edges with source, target, and weight.
	 *
	 * <p>
	 * The {@code verticesBuilt} lookup table of the reconstructed graph is repopulated by calling
	 * {@link GamaSpatialGraph#addBuiltVertex(IShape)} for every restored vertex, so that any subsequent calls to
	 * {@link GamaSpatialGraph#addEdgeWithNodes} will find the correct vertex shapes by coordinate.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA simulation scope, used to create the graph and its edges
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link GamaSpatialGraph}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public GamaSpatialGraph deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
		// --- metadata ---
		IType nodeType = (IType) in.readObject();
		IType edgeType = (IType) in.readObject();
		boolean directed = in.readBoolean();
		double tolerance = in.readDouble();
		GamaSpatialGraph result = new GamaSpatialGraph(scope, nodeType, edgeType);
		result.setDirected(directed);
		result.setTolerance(tolerance);
		// --- vertices ---
		// vertexRelation is null after the bare constructor, so GamaSpatialGraph.addVertex
		// will not attempt to create automatic edges – it simply delegates to super.addVertex.
		// We call addBuiltVertex explicitly to rebuild the coordinate → shape lookup table.
		int vertexCount = in.readInt();
		for (int i = 0; i < vertexCount; i++) {
			IShape v = (IShape) in.readObject();
			double weight = in.readDouble();
			result.addVertex(v);
			result.addBuiltVertex(v);
			result.setVertexWeight(v, weight);
		}
		// --- edges ---
		int edgeCount = in.readInt();
		for (int i = 0; i < edgeCount; i++) {
			IShape e = (IShape) in.readObject();
			IShape source = (IShape) in.readObject();
			IShape target = (IShape) in.readObject();
			double weight = in.readDouble();
			result.addEdge(source, target, e);
			result.setEdgeWeight(e, weight);
		}
		return result;
	}

}
