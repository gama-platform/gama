/*******************************************************************************************************
 *
 * GraphEvent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import gama.api.runtime.scope.IScope;

/**
 * Record representing an event that occurred on a graph.
 * 
 * <p>
 * GraphEvent is an immutable record that captures all relevant information about a modification
 * to a graph structure. Events are fired when vertices or edges are added, removed, or modified,
 * or when the graph itself is cleared or changed.
 * </p>
 * 
 * <p>
 * Event listeners can subscribe to graph events using {@link IGraphEventListener} and
 * receive notifications through the {@link IGraphEventProvider} interface.
 * </p>
 * 
 * @param scope the execution scope in which the event occurred
 * @param sender the object that generated the event (typically the graph itself)
 * @param edge the edge involved in the event, or null if not applicable
 * @param vertex the vertex involved in the event, or null if not applicable
 * @param eventType the type of event that occurred
 * 
 * @see IGraphEventListener
 * @see IGraphEventProvider
 * @see GraphEventType
 * @author drogoul
 */
@SuppressWarnings ({ "rawtypes" })
public record GraphEvent(IScope scope, Object sender, Object edge, Object vertex, GraphEventType eventType) {

	/**
	 * Enumeration of all possible graph event types.
	 * 
	 * <p>
	 * Each event type represents a specific kind of modification that can occur
	 * on a graph structure. Listeners can use these types to filter and handle
	 * only the events they are interested in.
	 * </p>
	 */
	public enum GraphEventType {

		/**
		 * The graph was completely cleared (all vertices and edges removed).
		 * When this event is fired, both edge and vertex will be null.
		 */
		GRAPH_CLEARED,

		/**
		 * The graph properties changed (for instance, attributes or metadata).
		 * This event is not thrown for individual vertex or edge changes,
		 * as those have their own specific event types.
		 */
		GRAPH_CHANGED,

		/**
		 * A new vertex was added to the graph.
		 * The vertex field will contain the added vertex.
		 */
		VERTEX_ADDED,

		/**
		 * A vertex was removed from the graph.
		 * The vertex field will contain the removed vertex.
		 */
		VERTEX_REMOVED,

		/**
		 * A vertex's properties changed (for instance, its weight or attributes).
		 * The vertex field will contain the modified vertex.
		 */
		VERTEX_CHANGED,

		/**
		 * A new edge was added to the graph.
		 * The edge field will contain the added edge.
		 */
		EDGE_ADDED,

		/**
		 * An edge was removed from the graph.
		 * The edge field will contain the removed edge.
		 */
		EDGE_REMOVED,

		/**
		 * An edge's properties changed (for instance, its weight or attributes).
		 * The edge field will contain the modified edge.
		 */
		EDGE_CHANGED;

	}

	@Override
	public String toString() {
		return new StringBuffer().append("graph event ").append(eventType).append(", edge=").append(edge)
				.append(", vertex=").append(vertex).append(", sender=").append(sender).toString();
	}

}
