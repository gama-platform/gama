/*******************************************************************************************************
 *
 * CoreConstantsSupplier.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.constants;

import gama.annotations.constants.ColorCSS;
import gama.api.additions.IConstantAcceptor;
import gama.api.additions.delegates.IConstantsSupplier;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IColor;
import gama.api.ui.layers.ICameraDefinition;
import gama.api.ui.layers.ILightDefinition;
import gama.api.utils.color.GamaColorFactory;

/**
 * The Class CoreConstantsSupplier.
 */
public class CoreConstantsSupplier implements IConstantsSupplier {

	@Override
	public void supplyConstantsTo(final IConstantAcceptor acceptor) {

		browse(ICameraDefinition.class, acceptor);
		browse(ILightDefinition.class, acceptor);
		browse(GamlCoreUnits.class, acceptor);
		browse(GamlCoreConstants.class, acceptor);

		acceptor.accept(IKeyword.DEFAULT, IKeyword.DEFAULT, "Default value for cameras and lights", null, false);

		// We build constants based on the colors declared in GamaColor / ColorCSS
		for (int i = 0; i < ColorCSS.array.length; i += 2) {
			String name = (String) ColorCSS.array[i];
			IColor c = GamaColorFactory.get(name);
			final String doc =
					"CSS color with rgb (" + c.red() + ", " + c.green() + ", " + c.blue() + "," + c.alpha() + ")";
			acceptor.accept(name, c, doc, null, false);
		}

		// We browse all the constants declared in the event layer delegates and add them (like mouse or keyboard
		// events)
		browse(IEventLayerDelegate.class, acceptor);
		for (final IEventLayerDelegate delegate : GamaAdditionRegistry.getEventLayerDelegates()) {
			browse(delegate.getClass(), acceptor);
		}

	}

}
