/*******************************************************************************************************
 *
 * LabelProviderFactory.java, in gama.ui.shared.modeling, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.compiler.ui.outline;

import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.xtext.resource.IResourceServiceProvider;

import gama.dev.DEBUG;

/**
 * A factory for creating LabelProvider objects.
 */
public class LabelProviderFactory extends AbstractServiceFactory {

	/** The service provider. */
	private static IResourceServiceProvider serviceProvider;

	/**
	 * Instantiates a new label provider factory.
	 */
	public LabelProviderFactory() {}

	@SuppressWarnings ("unchecked")
	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		if (serviceProvider == null) {
			// if (dependencyInjector != null)
			// return dependencyInjector.getInstance(c);
			try {
				serviceProvider = IResourceServiceProvider.Registry.INSTANCE
						.getResourceServiceProvider(URI.createPlatformResourceURI("dummy/dummy.gaml", false));
			} catch (final Exception e) {
				DEBUG.ERR("Exception in initializing injector: " + e.getMessage());
			}
		}
		return serviceProvider.get(serviceInterface);
	}

}
