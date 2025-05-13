/*******************************************************************************************************
 *
 * GamlResourceGraph.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.indexer;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.graph.FastLookupGraphSpecificsStrategy;

import com.google.common.collect.Maps;

import gama.core.util.GamaMapFactory;
import gama.dev.DEBUG;

/**
 * The Class GamlResourceGraph.
 */
public class GamlResourceGraph {

	static {
		DEBUG.OFF();
	}

	/**
	 * The Class Graph.
	 */
	class Imports extends AbstractBaseGraph<URI, LabeledEdge> {

		/**
		 * Instantiates a new graph.
		 */
		public Imports() {
			super(null, null,
					new DefaultGraphType.Builder().directed().allowMultipleEdges(false).allowSelfLoops(false)
							.weighted(false).allowCycles(true).build(),
					new FastLookupGraphSpecificsStrategy<URI, LabeledEdge>() {
						@Override
						public EdgeSetFactory<URI, LabeledEdge> getEdgeSetFactory() {
							return vertex -> Collections.newSetFromMap(new ConcurrentHashMap<LabeledEdge, Boolean>());
						}
					});
		}

		@Override
		public boolean addEdge(final URI sourceVertex, final URI targetVertex, final LabeledEdge e) {
			addVertex(sourceVertex);
			addVertex(targetVertex);
			removeEdge(sourceVertex, targetVertex);
			return super.addEdge(sourceVertex, targetVertex, e);
		}

	}

	/** The regular imports. */
	Imports imports = new Imports();

	/**
	 * Reset.
	 */
	void reset() {
		imports = new Imports();
	}

	/**
	 * The Class Edge.
	 */
	class LabeledEdge implements Comparable<LabeledEdge> {

		/** The label. */
		String label;

		/** The target. */
		final URI target;

		/**
		 * Instantiates a new edge.
		 *
		 * @param l
		 *            the l
		 * @param target
		 *            the target
		 */
		LabeledEdge(final String l, final URI target) {
			this.label = l;
			this.target = target;
		}

		@Override
		public int compareTo(final LabeledEdge o) {
			return target.toString().compareTo(o.target.toString());
		}

	}

	/**
	 * Predecessors of.
	 *
	 * @param newURI
	 *            the new URI
	 * @return the sets the
	 */
	public Set<URI> predecessorsOf(final URI uri) {
		if (!imports.containsVertex(uri)) return Collections.EMPTY_SET;
		Set<LabeledEdge> incoming = imports.incomingEdgesOf(uri);
		if (incoming.isEmpty()) return Collections.EMPTY_SET;
		Set<URI> result = new HashSet<>();
		for (LabeledEdge edge : incoming) { result.add(Graphs.getOppositeVertex(imports, edge, uri)); }
		return result;
	}

	/**
	 * Successors of.
	 *
	 * @param newURI
	 *            the new URI
	 * @return the sets the
	 */
	public Set<URI> successorsOf(final URI uri) {
		if (!imports.containsVertex(uri)) return Collections.EMPTY_SET;
		Set<LabeledEdge> outgoing = imports.outgoingEdgesOf(uri);
		if (outgoing.isEmpty()) return Collections.EMPTY_SET;
		Set<URI> result = new HashSet<>();
		for (LabeledEdge edge : outgoing) { result.add(Graphs.getOppositeVertex(imports, edge, uri)); }
		return result;
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
	public void addEdge(final URI from, final URI to, final String label) {
		imports.addEdge(from, to, new LabeledEdge(label, to));
	}

	/**
	 * Removes the all edges.
	 *
	 * @param edges
	 *            the edges
	 */
	public void removeAllEdges(final URI source, final Map<URI, String> edges) {
		if (edges.isEmpty()) return;
		edges.forEach((uri, v) -> { imports.removeEdge(source, uri); });
	}

	/**
	 * Outgoing edges of.
	 *
	 * @param uri
	 *            the uri
	 * @return the map
	 */
	public Map<URI, String> outgoingEdgesOf(final URI uri) {
		if (!imports.containsVertex(uri)) return Collections.EMPTY_MAP;
		Set<LabeledEdge> outgoing = imports.outgoingEdgesOf(uri);
		if (outgoing.isEmpty()) return Collections.EMPTY_MAP;
		Map<URI, String> result = GamaMapFactory.createOrdered();
		for (LabeledEdge o : outgoing) { result.put(o.target, o.label); }
		return result;
	}

	/**
	 * Sorted imports of.
	 *
	 * @param uri
	 *            the uri
	 * @return the map
	 */
	@SuppressWarnings ("null")
	public Map<URI, String> sortedDepthFirstSearchWithLabels(final URI uri) {
		LinkedHashMap<URI, String> result = Maps.newLinkedHashMap();
		searchImports(uri, null, result);
		result.remove(uri);
		return result;
	}

	/**
	 * All imports of. A simple depth first visit that keeps track of the labels
	 *
	 * @param uri
	 *            the uri
	 * @param currentLabel
	 *            the current label
	 * @param result
	 *            the result
	 */
	private void searchImports(final URI uri, final String currentLabel, final LinkedHashMap<URI, String> result) {
		if (!result.containsKey(uri)) {
			result.put(uri, currentLabel);
			if (imports.containsVertex(uri)) {
				for (LabeledEdge edge : imports.outgoingEdgesOf(uri)) {
					searchImports(edge.target, edge.label == null ? currentLabel : edge.label, result);
				}
			}
		} else {
			// if already there we re-insert it to keep the last occurence
			result.remove(uri);
			result.put(uri, currentLabel);
		}

	}
}
