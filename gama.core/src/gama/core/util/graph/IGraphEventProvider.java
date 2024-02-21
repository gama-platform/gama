/*******************************************************************************************************
 *
 * IGraphEventProvider.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util.graph;

import gama.core.runtime.IScope;

/**
 * The Interface IGraphEventProvider.
 */
public interface IGraphEventProvider {

	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	public void addListener(IGraphEventListener listener);

	/**
	 * Removes the listener.
	 *
	 * @param listener the listener
	 */
	public void removeListener(IGraphEventListener listener);

	/**
	 * Dispatch event.
	 *
	 * @param scope the scope
	 * @param event the event
	 */
	public void dispatchEvent(final IScope scope, GraphEvent event);

}
