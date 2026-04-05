/*******************************************************************************************************
 *
 * IGraphEventListener.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import gama.api.runtime.scope.IScope;

/**
 * Listener interface for receiving graph events.
 * 
 * <p>
 * Classes that are interested in being notified when a graph is modified should implement
 * this interface. The implementing class should then register itself with a graph using
 * the {@link IGraphEventProvider#addListener(IGraphEventListener)} method.
 * </p>
 * 
 * <p>
 * When a graph modification occurs (vertex added/removed, edge added/removed, etc.),
 * the {@link #receiveEvent(IScope, GraphEvent)} method is invoked with details about
 * the modification.
 * </p>
 * 
 * <h3>Usage Example:</h3>
 * <pre>
 * IGraph graph = ...;
 * graph.addListener(new IGraphEventListener() {
 *     public void receiveEvent(IScope scope, GraphEvent event) {
 *         switch (event.eventType()) {
 *             case VERTEX_ADDED:
 *                 // Handle vertex addition
 *                 break;
 *             case EDGE_REMOVED:
 *                 // Handle edge removal
 *                 break;
 *             // ... other cases
 *         }
 *     }
 * });
 * </pre>
 * 
 * @see GraphEvent
 * @see IGraphEventProvider
 * @see IGraph
 * @author drogoul
 */
public interface IGraphEventListener {

	/**
	 * Invoked when a graph event occurs.
	 * 
	 * <p>
	 * This method is called synchronously when the graph is modified. Implementations
	 * should be efficient and avoid blocking operations to prevent delays in graph
	 * operations.
	 * </p>
	 *
	 * @param scope the execution scope in which the event occurred
	 * @param event the graph event containing details about the modification
	 */
	void receiveEvent(final IScope scope, GraphEvent event);

}
