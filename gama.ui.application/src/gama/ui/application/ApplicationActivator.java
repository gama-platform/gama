/*******************************************************************************************************
 *
 * ApplicationActivator.java, in gama.ui.application, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.application;

import org.osgi.framework.BundleContext;

import gama.api.runtime.SystemInfo;
import gama.core.CoreActivator;
import gama.dependencies.GamaBundleActivator;
import gama.dev.DEBUG;
import gama.workspace.WorkspaceActivator;

/**
 * The Class ApplicationActivator.
 */
public class ApplicationActivator extends GamaBundleActivator {

	static {
		DEBUG.OFF();
	}

	/**
	 * Start.
	 *
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void initialize(final BundleContext context) {
        if (SystemInfo.isLinux() && System.getenv("WAYLAND_DISPLAY") != null) {
        	System.out.println("> GAMA  : Linux Wayland detected, forcing XWayland rendering");
            System.setProperty("org.eclipse.swt.internal.gtk.disableWayland", "true");
        }
		DEBUG.OUT("Activating the application plugin");
		WorkspaceActivator.load();
		CoreActivator.load();
	}

}
