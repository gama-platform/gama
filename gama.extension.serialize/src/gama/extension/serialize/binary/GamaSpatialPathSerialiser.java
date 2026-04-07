/*******************************************************************************************************
 *
 * GamaSpatialPathSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.IPath;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.gaml.types.Types;
import gama.core.topology.graph.GamaSpatialGraph;
import gama.core.util.path.GamaSpatialPath;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * FST serialiser for {@link GamaSpatialPath} instances.
 *
 * <p>
 * A {@link GamaSpatialPath} stores several computed fields ({@code segments}, {@code threeD},
 * {@code realObjects}, {@code shape}) that are fully derived from the structural data. This
 * serialiser therefore persists only the minimal structural state required to reconstruct the path:
 * the underlying spatial graph (which may be {@code null}), the source and target vertices, the
 * ordered list of graph edges, and the stored weight. All derived fields are recomputed by the
 * appropriate {@link GamaSpatialPath} constructor during deserialisation.
 * </p>
 *
 * <p>
 * On-stream layout:
 * <ol>
 * <li>graph ({@link GamaSpatialGraph} object — may be {@code null})</li>
 * <li>source vertex ({@link IShape} object)</li>
 * <li>target vertex ({@link IShape} object)</li>
 * <li>edge count (int)</li>
 * <li>for each edge: edge {@link IShape} (object)</li>
 * <li>weight (double)</li>
 * </ol>
 * </p>
 *
 * <p>
 * When the graph is non-{@code null} the path is reconstructed via
 * {@link GamaSpatialPath#GamaSpatialPath(GamaSpatialGraph, IShape, IShape, IList)} (which applies
 * edge-geometry trimming at source/target, matching the original construction). When the graph is
 * {@code null} the constructor
 * {@link GamaSpatialPath#GamaSpatialPath(IShape, IShape, IList)} is used instead (no trimming).
 * In both cases {@link IPath#setWeight(double)} is called afterwards to restore the stored weight.
 * </p>
 *
 * <p>
 * Objects deserialised by this serialiser are not registered for FST back-reference tracking.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 avril 2026
 */
class GamaSpatialPathSerialiser extends FSTIndividualSerialiser<GamaSpatialPath> {

	/**
	 * Constructs a new {@code GamaSpatialPathSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser
	 */
	GamaSpatialPathSerialiser(final BinarySerialiser serialiser) {
		super(serialiser);
	}

	/**
	 * Returns {@code false}: spatial paths are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	protected boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises the spatial path's underlying graph, source, target, edge list, and weight.
	 * Derived fields (segments, threeD, realObjects, shape) are not written; they are recomputed on
	 * deserialisation.
	 *
	 * @param out
	 *            the FST output stream
	 * @param p
	 *            the spatial path to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public void serialise(final FSTObjectOutput out, final GamaSpatialPath p) throws Exception {
		out.writeObject(p.getGraph());          // GamaSpatialGraph or null
		out.writeObject(p.getStartVertex());    // source IShape
		out.writeObject(p.getEndVertex());      // target IShape
		// Write the edge list inline to avoid a dependency on IListSerialiser ordering
		final IList<IShape> edges = p.getEdgeList();
		out.writeInt(edges == null ? 0 : edges.size());
		if (edges != null) {
			for (final IShape e : edges) { out.writeObject(e); }
		}
		out.writeDouble(p.getWeight());
	}

	/**
	 * Deserialises a spatial path by reading the graph, source, target, edges, and weight, then
	 * constructing a new {@link GamaSpatialPath} that recomputes all derived geometric fields.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused — path construction does not require a scope)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link GamaSpatialPath}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public GamaSpatialPath deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
		final GamaSpatialGraph graph = (GamaSpatialGraph) in.readObject();
		final IShape source = (IShape) in.readObject();
		final IShape target = (IShape) in.readObject();
		final int edgeCount = in.readInt();
		final IList<IShape> edges = GamaListFactory.create(Types.GEOMETRY);
		for (int i = 0; i < edgeCount; i++) { edges.add((IShape) in.readObject()); }
		final double weight = in.readDouble();
		// Reconstruct using the appropriate constructor so that segments, threeD and
		// realObjects are recomputed consistently with how the path was originally built.
		final GamaSpatialPath result = graph != null
				? new GamaSpatialPath(graph, source, target, edges)
				: new GamaSpatialPath(source, target, edges);
		result.setWeight(weight);
		return result;
	}

}
