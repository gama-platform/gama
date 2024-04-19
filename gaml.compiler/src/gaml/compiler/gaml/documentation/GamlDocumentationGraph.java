/*******************************************************************************************************
 *
 * GamlDocumentationGraph.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.documentation;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.emf.common.util.URI;
import org.jgrapht.GraphType;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.graph.FastLookupGraphSpecificsStrategy;
import org.jgrapht.graph.IntrusiveEdgesSpecifics;
import org.jgrapht.graph.UniformIntrusiveEdgesSpecifics;

import gama.dev.DEBUG;

/**
 * The Class GamlResourceGraph.
 */
public class GamlDocumentationGraph {

	static {
		DEBUG.ON();
	}

	/**
	 * The Class Graph.
	 */
	class InternalGraph extends AbstractBaseGraph<URI, Edge> {

		/**
		 * Instantiates a new graph.
		 */
		public InternalGraph() {
			super(null, null,
					new DefaultGraphType.Builder().undirected().allowMultipleEdges(true).allowSelfLoops(false)
							.weighted(false).allowCycles(true).build(),
					new FastLookupGraphSpecificsStrategy<URI, Edge>() {
						@Override
						public Function<GraphType, IntrusiveEdgesSpecifics<URI, Edge>>
								getIntrusiveEdgesSpecificsFactory() {
							return type -> new UniformIntrusiveEdgesSpecifics<>(new ConcurrentHashMap<>()) {
								public URI getEdgeTarget(final Edge e) {
									try {
										return super.getEdgeTarget(e);
									} catch (Exception e1) {
										return null;
									}
								}
							};

						}

						@Override
						public EdgeSetFactory<URI, Edge> getEdgeSetFactory() {
							return vertex -> Collections.newSetFromMap(new ConcurrentHashMap<Edge, Boolean>());
						}
					});
		}

		@Override
		public boolean addEdge(final URI sourceVertex, final URI targetVertex, final Edge e) {
			addVertex(sourceVertex);
			addVertex(targetVertex);
			removeEdge(sourceVertex, targetVertex);
			return super.addEdge(sourceVertex, targetVertex, e);
		}

	}

	/** The regular imports. */
	InternalGraph internal = new InternalGraph();

	/**
	 * Reset.
	 */
	void reset() {
		internal = new InternalGraph();
	}

	/**
	 * The Class Edge.
	 */
	class Edge {}

	/**
	 * Predecessors of.
	 *
	 * @param newURI
	 *            the new URI
	 * @return the sets the
	 */
	public Set<URI> predecessorsOf(final URI uri) {
		if (!internal.containsVertex(uri)) return Collections.EMPTY_SET;

		return Graphs.neighborSetOf(internal, uri);

		// Set<Edge> incoming = internal.incomingEdgesOf(uri);
		//
		// if (incoming.isEmpty()) return Collections.EMPTY_SET;
		// Set<URI> result = new HashSet<>();
		// for (Edge edge : incoming) { result.add(Graphs.getOppositeVertex(internal, edge, uri)); }
		// return result;
	}

	/**
	 * Successors of.
	 *
	 * @param newURI
	 *            the new URI
	 * @return the sets the
	 */
	public Set<URI> successorsOf(final URI uri) {
		if (!internal.containsVertex(uri)) return Collections.EMPTY_SET;
		return Graphs.neighborSetOf(internal, uri);
		// Set<Edge> outgoing = internal.outgoingEdgesOf(uri);
		// if (outgoing.isEmpty()) return Collections.EMPTY_SET;
		// Set<URI> result = new HashSet<>();
		// for (Edge edge : outgoing) { result.add(Graphs.getOppositeVertex(internal, edge, uri)); }
		// return result;
	}

	/**
	 * Adds the edge.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param label
	 *            the label
	 */
	public void addEdge(final URI from, final URI to) {
		internal.addEdge(from, to, new Edge());
	}

	/**
	 * Removes the node.
	 *
	 * @param uri
	 *            the uri
	 */
	public void removeNode(final URI uri) {
		internal.removeVertex(uri);
	}

}
