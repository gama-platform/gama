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

import gama.dev.DEBUG;

/**
 * A directed graph structure for managing GAML resource dependencies and imports.
 * 
 * <p>
 * This class maintains a graph representation of GAML resources (files) and their import relationships.
 * Each vertex in the graph represents a resource identified by its URI, and each edge represents an import
 * relationship with an optional label (typically the alias or name used in the import statement).
 * </p>
 * 
 * <p>
 * Key features:
 * <ul>
 * <li>Directed graph: imports flow from source to target resources</li>
 * <li>No multiple edges: only one import relationship between any two resources</li>
 * <li>No self-loops: resources cannot import themselves</li>
 * <li>Cycle detection: allows cycles in the dependency graph</li>
 * <li>Thread-safe: uses concurrent data structures for edge storage</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The graph supports:
 * <ul>
 * <li>Finding all predecessors (resources that import a given resource)</li>
 * <li>Finding all successors (resources imported by a given resource)</li>
 * <li>Depth-first search to find transitive imports</li>
 * <li>Labeled edges to track import aliases</li>
 * </ul>
 * </p>
 * 
 * @author GAMA Development Team
 * @since GAMA 2024-06
 */
public class GamlResourceGraph {

	static {
		DEBUG.OFF();
	}

	/**
	 * Internal graph implementation for managing GAML resource imports.
	 * 
	 * <p>
	 * This is a specialized directed graph that extends {@link AbstractBaseGraph} with the following configuration:
	 * <ul>
	 * <li><b>Directed:</b> Edges have a source and target direction</li>
	 * <li><b>No multiple edges:</b> At most one edge between any pair of vertices</li>
	 * <li><b>No self-loops:</b> A resource cannot import itself</li>
	 * <li><b>Allows cycles:</b> Circular dependencies are permitted in the graph structure</li>
	 * <li><b>Thread-safe edges:</b> Uses {@link ConcurrentHashMap} for edge storage</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The graph uses a {@link FastLookupGraphSpecificsStrategy} for optimized vertex and edge lookups,
	 * which is essential for performance when dealing with large codebases.
	 * </p>
	 */
	class Imports extends AbstractBaseGraph<URI, LabeledEdge> {

		/**
		 * Instantiates a new imports graph with optimized configuration.
		 * 
		 * <p>
		 * Initializes the graph with:
		 * <ul>
		 * <li>Custom edge set factory using concurrent hash maps for thread safety</li>
		 * <li>Fast lookup strategy for O(1) vertex/edge operations</li>
		 * <li>Directed graph type with cycles allowed</li>
		 * </ul>
		 * </p>
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

		/**
		 * Adds an edge between two vertices, ensuring both vertices exist first.
		 * 
		 * <p>
		 * This method overrides the default behavior to:
		 * <ul>
		 * <li>Automatically add source and target vertices if they don't exist</li>
		 * <li>Replace any existing edge between the same vertices (since multiple edges are not allowed)</li>
		 * </ul>
		 * </p>
		 * 
		 * <p>
		 * The edge removal before addition is necessary because the graph configuration does not allow
		 * multiple edges between the same vertex pair. This ensures the new edge with updated label
		 * replaces any previous import relationship.
		 * </p>
		 * 
		 * @param sourceVertex
		 *            the source vertex (importing resource)
		 * @param targetVertex
		 *            the target vertex (imported resource)
		 * @param e
		 *            the labeled edge containing import information
		 * @return {@code true} if the edge was added successfully, {@code false} otherwise
		 */
		@Override
		public boolean addEdge(final URI sourceVertex, final URI targetVertex, final LabeledEdge e) {
			addVertex(sourceVertex);
			addVertex(targetVertex);
			// Only remove if an edge exists (since multiple edges are not allowed)
			if (containsEdge(sourceVertex, targetVertex)) {
				removeEdge(sourceVertex, targetVertex);
			}
			return super.addEdge(sourceVertex, targetVertex, e);
		}

	}

	/** 
	 * The internal graph storing import relationships between GAML resources.
	 * Each vertex represents a resource URI, and each edge represents an import dependency.
	 */
	Imports imports = new Imports();

	/**
	 * Resets the graph to an empty state.
	 * 
	 * <p>
	 * Creates a new empty graph instance, effectively clearing all vertices and edges.
	 * This is useful when rebuilding the dependency graph from scratch.
	 * </p>
	 */
	void reset() {
		imports = new Imports();
	}

	/**
	 * A labeled edge representing an import relationship between two GAML resources.
	 * 
	 * <p>
	 * Each edge connects a source resource (the importing file) to a target resource (the imported file).
	 * The edge can optionally carry a label, which typically represents the import alias or name used
	 * in the import statement (e.g., "model MyModel" would have label "MyModel").
	 * </p>
	 * 
	 * <p>
	 * Edges are comparable based on their target URI's string representation, allowing for
	 * consistent ordering when iterating over edge collections.
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b><br>
	 * For an import statement like {@code import "submodel.gaml" as SubModel;}<br>
	 * - The label would be "SubModel"<br>
	 * - The target would be the URI of submodel.gaml
	 * </p>
	 */
	class LabeledEdge implements Comparable<LabeledEdge> {

		/** 
		 * The label associated with this import edge.
		 * Typically represents the alias or name used in the import statement.
		 * Can be {@code null} if no explicit label was provided.
		 */
		String label;

		/** 
		 * The target resource URI that is being imported.
		 * This is the destination of the import relationship.
		 */
		final URI target;

		/**
		 * Creates a new labeled edge for an import relationship.
		 *
		 * @param l
		 *            the label for this import (can be {@code null})
		 * @param target
		 *            the target resource URI being imported (must not be {@code null})
		 */
		LabeledEdge(final String l, final URI target) {
			this.label = l;
			this.target = target;
		}

		/**
		 * Compares this edge to another based on target URI string representation.
		 * 
		 * <p>
		 * This provides a consistent ordering for edges, which is useful when displaying
		 * or iterating over import relationships in a deterministic manner.
		 * </p>
		 * 
		 * @param o
		 *            the other edge to compare to
		 * @return a negative integer, zero, or positive integer as this edge's target
		 *         is less than, equal to, or greater than the specified edge's target
		 */
		@Override
		public int compareTo(final LabeledEdge o) {
			return target.toString().compareTo(o.target.toString());
		}

	}

	/**
	 * Finds all direct predecessors of a given resource in the import graph.
	 * 
	 * <p>
	 * A predecessor is a resource that directly imports the specified resource. This method returns
	 * the set of all resources that have an import edge pointing to the given URI.
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b><br>
	 * If MainModel.gaml imports SubModel.gaml, then:<br>
	 * {@code predecessorsOf(SubModel.gaml)} returns {@code [MainModel.gaml]}
	 * </p>
	 * 
	 * <p>
	 * Performance: O(n) where n is the number of incoming edges to the resource.
	 * The returned set is pre-sized for optimal memory allocation.
	 * </p>
	 *
	 * @param uri
	 *            the URI of the resource whose predecessors should be found
	 * @return an unmodifiable set of URIs representing direct predecessors, or an empty set
	 *         if the resource has no predecessors or is not in the graph
	 */
	public Set<URI> predecessorsOf(final URI uri) {
		if (!imports.containsVertex(uri)) return Collections.emptySet();
		Set<LabeledEdge> incoming = imports.incomingEdgesOf(uri);
		if (incoming.isEmpty()) return Collections.emptySet();
		Set<URI> result = new HashSet<>(incoming.size());
		for (LabeledEdge edge : incoming) { result.add(Graphs.getOppositeVertex(imports, edge, uri)); }
		return result;
	}

	/**
	 * Finds all direct successors of a given resource in the import graph.
	 * 
	 * <p>
	 * A successor is a resource that is directly imported by the specified resource. This method returns
	 * the set of all resources that the given URI imports (i.e., resources that are targets of outgoing edges).
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b><br>
	 * If MainModel.gaml imports SubModel.gaml and Utils.gaml, then:<br>
	 * {@code successorsOf(MainModel.gaml)} returns {@code [SubModel.gaml, Utils.gaml]}
	 * </p>
	 * 
	 * <p>
	 * Performance: O(n) where n is the number of outgoing edges from the resource.
	 * The returned set is pre-sized for optimal memory allocation. This method is optimized
	 * to access edge targets directly without additional graph lookups.
	 * </p>
	 *
	 * @param uri
	 *            the URI of the resource whose successors (imported resources) should be found
	 * @return a set of URIs representing direct successors, or an empty set
	 *         if the resource imports nothing or is not in the graph
	 */
	public Set<URI> successorsOf(final URI uri) {
		if (!imports.containsVertex(uri)) return Collections.emptySet();
		Set<LabeledEdge> outgoing = imports.outgoingEdgesOf(uri);
		if (outgoing.isEmpty()) return Collections.emptySet();
		Set<URI> result = new HashSet<>(outgoing.size());
		for (LabeledEdge edge : outgoing) { result.add(edge.target); }
		return result;
	}

	/**
	 * Adds an import edge from one resource to another with an optional label.
	 * 
	 * <p>
	 * Creates or replaces an import relationship between two GAML resources. If both vertices
	 * don't exist in the graph, they are automatically added. If an edge already exists between
	 * the same resources, it is replaced with the new edge.
	 * </p>
	 * 
	 * <p>
	 * The label typically represents the import alias or name used in the import statement.
	 * It can be {@code null} if no explicit label is provided.
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b><br>
	 * For {@code import "utils.gaml" as Utils;} in main.gaml:<br>
	 * {@code addEdge(main.gaml_URI, utils.gaml_URI, "Utils")}
	 * </p>
	 *
	 * @param from
	 *            the URI of the source resource (the importing file)
	 * @param to
	 *            the URI of the target resource (the imported file)
	 * @param label
	 *            the label for this import relationship (typically an alias), can be {@code null}
	 */
	public void addEdge(final URI from, final URI to, final String label) {
		imports.addEdge(from, to, new LabeledEdge(label, to));
	}

	/**
	 * Removes multiple import edges from a source resource.
	 * 
	 * <p>
	 * Efficiently removes all edges from a source resource to the targets specified in the edges map.
	 * This is useful when a resource's import list changes and multiple imports need to be removed at once.
	 * </p>
	 * 
	 * <p>
	 * The method performs no operation if the edges map is null or empty. Only the keys (target URIs)
	 * from the map are used; the values are ignored.
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b><br>
	 * If MainModel.gaml previously imported SubModel.gaml and Utils.gaml, but these imports
	 * are now removed from the source code, this method can efficiently remove both edges.
	 * </p>
	 *
	 * @param source
	 *            the URI of the source resource whose outgoing edges should be removed
	 * @param edges
	 *            a map of target URIs to labels representing the edges to remove (can be {@code null})
	 */
	public void removeAllEdges(final URI source, final Map<URI, String> edges) {
		if (edges == null || edges.isEmpty()) return;
		edges.keySet().forEach(uri -> imports.removeEdge(source, uri));
	}

	/**
	 * Retrieves all outgoing edges from a resource with their labels.
	 * 
	 * <p>
	 * Returns a map where each key is a target URI (imported resource) and each value is the
	 * label associated with that import edge (typically the import alias). This is useful for
	 * understanding both what a resource imports and how those imports are labeled.
	 * </p>
	 * 
	 * <p>
	 * The returned map maintains insertion order (LinkedHashMap) for predictable iteration.
	 * The map is pre-sized for optimal performance.
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b><br>
	 * If MainModel.gaml contains:<br>
	 * {@code import "utils.gaml" as Utils;}<br>
	 * {@code import "helpers.gaml" as Helpers;}<br>
	 * Then {@code outgoingEdgesOf(MainModel.gaml)} returns:<br>
	 * {@code {utils.gaml_URI -> "Utils", helpers.gaml_URI -> "Helpers"}}
	 * </p>
	 *
	 * @param uri
	 *            the URI of the resource whose outgoing edges should be retrieved
	 * @return a map from target URIs to edge labels, or an empty map if the resource
	 *         has no outgoing edges or is not in the graph
	 */
	public Map<URI, String> outgoingEdgesOf(final URI uri) {
		if (!imports.containsVertex(uri)) return Collections.emptyMap();
		Set<LabeledEdge> outgoing = imports.outgoingEdgesOf(uri);
		if (outgoing.isEmpty()) return Collections.emptyMap();
		Map<URI, String> result = Maps.newLinkedHashMapWithExpectedSize(outgoing.size());
		for (LabeledEdge o : outgoing) { result.put(o.target, o.label); }
		return result;
	}

	/**
	 * Performs a depth-first search to find all transitive imports of a resource with their labels.
	 * 
	 * <p>
	 * This method traverses the import graph starting from the given URI and collects all resources
	 * that are transitively imported (directly or indirectly). The search follows import chains to
	 * discover the complete dependency tree.
	 * </p>
	 * 
	 * <p>
	 * The returned map maintains insertion order based on the depth-first traversal, which can be
	 * useful for understanding the import sequence. Each entry maps a resource URI to its associated
	 * label (if any) from the import statement.
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b><br>
	 * If MainModel.gaml imports SubModel.gaml, and SubModel.gaml imports Utils.gaml:<br>
	 * {@code sortedDepthFirstSearchWithLabels(MainModel.gaml)} returns:<br>
	 * {@code {SubModel.gaml_URI -> "SubModel", Utils.gaml_URI -> "Utils"}}
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> The starting resource itself is excluded from the result. If a resource is
	 * encountered multiple times during traversal (due to multiple import paths), the last
	 * occurrence's label is retained.
	 * </p>
	 *
	 * @param uri
	 *            the URI of the starting resource
	 * @return a map of all transitively imported resources with their labels, in depth-first order,
	 *         excluding the starting resource itself
	 */
	@SuppressWarnings ("null")
	public Map<URI, String> sortedDepthFirstSearchWithLabels(final URI uri) {
		LinkedHashMap<URI, String> result = Maps.newLinkedHashMap();
		searchImports(uri, null, result);
		result.remove(uri);
		return result;
	}

	/**
	 * Recursively performs depth-first search traversal through the import graph.
	 * 
	 * <p>
	 * This is a helper method for {@link #sortedDepthFirstSearchWithLabels(URI)} that recursively
	 * explores the import graph. It maintains a LinkedHashMap to preserve the traversal order
	 * and track which resources have been visited.
	 * </p>
	 * 
	 * <p>
	 * The algorithm:
	 * <ol>
	 * <li>If the URI hasn't been visited, add it to the result with its current label</li>
	 * <li>Recursively visit all resources that this URI imports (outgoing edges)</li>
	 * <li>If the URI has been visited before, move it to the end (last occurrence wins)</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>
	 * The "last occurrence wins" behavior ensures that when a resource is reachable through
	 * multiple import paths, its position in the result reflects the last path that reached it
	 * during the depth-first traversal.
	 * </p>
	 *
	 * @param uri
	 *            the current resource URI being visited
	 * @param currentLabel
	 *            the label to associate with this resource (can be {@code null})
	 * @param result
	 *            the accumulated result map maintaining traversal order
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
