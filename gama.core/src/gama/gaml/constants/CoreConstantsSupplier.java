/*******************************************************************************************************
 *
 * CoreConstantsSupplier.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.constants;

import java.util.Map;

import gama.core.common.interfaces.IKeyword;
import gama.core.outputs.layers.EventLayerStatement;
import gama.core.outputs.layers.properties.ICameraDefinition;
import gama.core.outputs.layers.properties.ILightDefinition;
import gama.core.util.GamaColorFactory;
import gama.core.util.IColor;
import gama.gaml.interfaces.IEventLayerDelegate;

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
		for (final Map.Entry<String, IColor> entry : GamaColorFactory.COLORS.entrySet()) {
			final IColor c = entry.getValue();
			final String doc = "Standard CSS color corresponding to rgb (" + c.red() + ", " + c.green() + ", "
					+ c.blue() + "," + c.alpha() + ")";
			acceptor.accept(entry.getKey(), c, doc, null, false);
		}

		// We browse all the constants declared in the event layer delegates and add them (like mouse or keyboard
		// events)
		for (final IEventLayerDelegate delegate : EventLayerStatement.delegates) {
			browse(delegate.getClass(), acceptor);
		}

	}

}
