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
 * The listener interface for receiving IGraphEvent events. The class that is interested in processing a IGraphEvent
 * event implements this interface, and the object created with that class is registered with a component using the
 * component's <code>addIGraphEventListener<code> method. When the IGraphEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see IGraphEventEvent
 */
public interface IGraphEventListener {

	/**
	 * Receive event.
	 *
	 * @param scope
	 *            the scope
	 * @param event
	 *            the event
	 */
	void receiveEvent(final IScope scope, GraphEvent event);

}
