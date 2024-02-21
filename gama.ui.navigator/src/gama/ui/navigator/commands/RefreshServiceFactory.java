/*******************************************************************************************************
 *
 * RefreshServiceFactory.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.commands;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import gama.ui.shared.interfaces.IRefreshHandler;

/**
 * A factory for creating RefreshService objects.
 */
public class RefreshServiceFactory extends AbstractServiceFactory {

	/**
	 * Instantiates a new refresh service factory.
	 */
	public RefreshServiceFactory() {}

	@Override
	public IRefreshHandler create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return new RefreshHandler();
	}

}
