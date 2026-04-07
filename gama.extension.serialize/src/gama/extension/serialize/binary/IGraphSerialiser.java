/*******************************************************************************************************
 *
 * IGraphSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.graph.IGraph;
import gama.core.util.graph.GamaGraph;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * FST serialiser for {@link IGraph} instances.
 *
 * <p>
 * Persists the graph's structural metadata (vertex type, edge type, directed flag) followed by all
 * vertices with their weights and all edges with their source vertex, target vertex, and weight. On
 * deserialisation the graph is reconstructed as a {@link GamaGraph} using the same scope that was active
 * during serialisation.
 * </p>
 *
 * <p>
 * The on-stream layout is:
 * <ol>
 * <li>vertex {@link IType} (object)</li>
 * <li>edge {@link IType} (object)</li>
 * <li>directed flag (boolean)</li>
 * <li>vertex count (int)</li>
 * <li>for each vertex: vertex object + vertex weight (double)</li>
 * <li>edge count (int)</li>
 * <li>for each edge: edge object + source vertex object + target vertex object + edge weight (double)</li>
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
public class IGraphSerialiser extends FSTIndividualSerialiser<IGraph> {

	/**
	 * Constructs a new {@code IGraphSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser
	 */
	public IGraphSerialiser(final BinarySerialiser serialiser) {
		super(serialiser);
	}

	/**
	 * Returns {@code false}: graphs are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	protected boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises the graph's vertex type, edge type, directed flag, all vertices with their weights, and all edges with
	 * their source, target, and weight.
	 *
	 * @param out
	 *            the FST output stream
	 * @param g
	 *            the graph to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public void serialise(final FSTObjectOutput out, final IGraph g) throws Exception {
		// --- metadata ---
		out.writeObject(g.getGamlType().getKeyType());
		out.writeObject(g.getGamlType().getContentType());
		out.writeBoolean(g.isDirected());
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
	 * Deserialises a graph by reading its vertex type, edge type, directed flag, vertices with weights, and edges with
	 * source, target, and weight. Returns a new {@link GamaGraph} populated with the stored data.
	 *
	 * @param scope
	 *            the current GAMA simulation scope, used to create the graph
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IGraph}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	public IGraph deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
		// --- metadata ---
		IType nodeType = (IType) in.readObject();
		IType edgeType = (IType) in.readObject();
		boolean directed = in.readBoolean();
		GamaGraph result = new GamaGraph<>(scope, directed, nodeType, edgeType);
		// --- vertices ---
		int vertexCount = in.readInt();
		for (int i = 0; i < vertexCount; i++) {
			Object v = in.readObject();
			double weight = in.readDouble();
			result.addVertex(v);
			result.setVertexWeight(v, weight);
		}
		// --- edges ---
		int edgeCount = in.readInt();
		for (int i = 0; i < edgeCount; i++) {
			Object e = in.readObject();
			Object source = in.readObject();
			Object target = in.readObject();
			double weight = in.readDouble();
			result.addEdge(source, target, e);
			result.setEdgeWeight(e, weight);
		}
		return result;
	}

}
