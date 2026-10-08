/*******************************************************************************************************
 *
 * IGraphEventProvider.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
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
 * Interface for objects that provide graph event notifications.
 * 
 * <p>
 * This interface allows objects (typically graphs) to notify registered listeners
 * when graph modifications occur. It provides methods to register and unregister
 * listeners, as well as to dispatch events to all registered listeners.
 * </p>
 * 
 * <p>
 * The {@link IGraph} interface extends this interface, making all graphs capable
 * of sending event notifications to interested parties.
 * </p>
 * 
 * <h3>Event Types:</h3>
 * Events are dispatched for various graph modifications including:
 * <ul>
 * <li>Vertex additions and removals</li>
 * <li>Edge additions and removals</li>
 * <li>Graph structure changes</li>
 * <li>Property modifications</li>
 * </ul>
 * 
 * @see IGraphEventListener
 * @see GraphEvent
 * @see IGraph
 * @author drogoul
 */
public interface IGraphEventProvider {

	/**
	 * Registers a listener to receive graph events.
	 * 
	 * <p>
	 * The listener will be notified of all subsequent graph modifications
	 * until it is removed using {@link #removeListener(IGraphEventListener)}.
	 * </p>
	 * 
	 * <p>
	 * If the same listener is added multiple times, it will receive multiple
	 * notifications for each event (once per registration).
	 * </p>
	 *
	 * @param listener the listener to add (must not be null)
	 */
	void addListener(IGraphEventListener listener);

	/**
	 * Unregisters a listener from receiving graph events.
	 * 
	 * <p>
	 * After this method is called, the listener will no longer receive
	 * notifications about graph modifications. If the listener was not
	 * previously registered, this method has no effect.
	 * </p>
	 * 
	 * <p>
	 * If the listener was registered multiple times, only one registration
	 * is removed per call to this method.
	 * </p>
	 *
	 * @param listener the listener to remove
	 */
	void removeListener(IGraphEventListener listener);

	/**
	 * Dispatches an event to all registered listeners.
	 * 
	 * <p>
	 * This method is typically called internally by the graph implementation
	 * when a modification occurs. All registered listeners will have their
	 * {@link IGraphEventListener#receiveEvent(IScope, GraphEvent)} method
	 * invoked with the provided event.
	 * </p>
	 * 
	 * <p>
	 * Listeners are notified synchronously in the order they were registered.
	 * </p>
	 *
	 * @param scope the execution scope in which the event occurred
	 * @param event the event to dispatch to listeners
	 */
	void dispatchEvent(final IScope scope, GraphEvent event);

}
