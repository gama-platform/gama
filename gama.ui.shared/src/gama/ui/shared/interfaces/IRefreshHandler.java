/*******************************************************************************************************
 *
 * IRefreshHandler.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.interfaces;

import java.util.List;

import org.eclipse.core.resources.IResource;

/**
 * The Interface IRefreshHandler.
 */
public interface IRefreshHandler {

	/**
	 * Complete refresh.
	 *
	 * @param resources the resources
	 */
	void completeRefresh(List<? extends IResource> resources);

	/**
	 * Refresh resource.
	 *
	 * @param resource the resource
	 */
	void refreshResource(final IResource resource);

	// void refreshNavigator();

}
