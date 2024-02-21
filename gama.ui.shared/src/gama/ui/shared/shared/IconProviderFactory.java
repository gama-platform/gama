/*******************************************************************************************************
 *
 * IconProviderFactory.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.shared;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import gama.ui.application.workbench.IIconProvider;
import gama.ui.shared.resources.GamaIcon;

/**
 * A factory for creating IconProvider objects.
 */
public class IconProviderFactory extends AbstractServiceFactory {

	/**
	 * Instantiates a new icon provider factory.
	 */
	public IconProviderFactory() {}

	@Override
	public IIconProvider create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return new IIconProvider() {
			@Override
			public ImageDescriptor desc(final String name) {
				final GamaIcon icon = GamaIcon.named(name);
				return icon.descriptor();
			}

			@Override
			public ImageDescriptor disabled(final String name) {
				final GamaIcon icon = GamaIcon.named(name);
				return icon.disabledDescriptor();
			}
		};
	}

}
