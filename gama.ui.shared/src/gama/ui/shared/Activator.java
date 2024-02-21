/*******************************************************************************************************
 *
 * Activator.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.utils.SwtGui;

/**
 * The Class Activator.
 */
public class Activator extends AbstractUIPlugin {

	static {
		DEBUG.OFF();
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		GAMA.setRegularGui(new SwtGui());
		GamaIcon.preloadAllIcons();
	}

}
