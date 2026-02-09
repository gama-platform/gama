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
package gama.api.utils.collections;

import gama.api.runtime.scope.IScope;

/**
 * The Interface IGraphEventProvider.
 */
public interface IGraphEventProvider {

	/**
	 * Adds the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void addListener(IGraphEventListener listener);

	/**
	 * Removes the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void removeListener(IGraphEventListener listener);

	/**
	 * Dispatch event.
	 *
	 * @param scope
	 *            the scope
	 * @param event
	 *            the event
	 */
	void dispatchEvent(final IScope scope, GraphEvent event);

}
