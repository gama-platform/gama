/*******************************************************************************************************
 *
 * LaunchGamaHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.devtools.launch.GamaLaunchDialog;
import gama.ui.devtools.launch.GamaLaunches;
import gama.ui.devtools.launch.GamaLaunches.Flavor;
import gama.ui.devtools.util.ResourceUtils;

/** Run and Debug share the same configurations and use Eclipse's normal launch infrastructure. */
public final class LaunchGamaHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		var window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) { return null; }
		var shell = window.getShell();
		String requested = event.getParameter("gama.ui.devtools.launchFlavor");
		Flavor flavor = requested == null ? Flavor.GUI : Flavor.valueOf(requested);
		try {
			if ("configure".equals(event.getParameter("gama.ui.devtools.launchMode"))) {
				new GamaLaunchDialog(shell, flavor).open();
				return null;
			}
			ILaunchConfiguration config = GamaLaunches.mapped(flavor);
			if (config == null) {
				GamaLaunchDialog dialog = new GamaLaunchDialog(shell, flavor);
				if (dialog.open() != Window.OK) { return null; }
				config = dialog.result();
				flavor = dialog.selectedFlavor();
			}
			GamaLaunches.validate(config, flavor);
			if (flavor == Flavor.HEADLESS) {
				String args = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
				InputDialog input = new InputDialog(shell, "GAMA Headless Arguments",
						"Choose the headless mode and arguments, for example:\n"
						+ "\"/path/experiment.xml\" \"/path/output\"\n"
						+ "-batch experimentName \"/path/model.gaml\"\n"
						+ "-help, -validate, or -socket port\n\nThese arguments are saved in the configuration.",
						args, value -> value.isBlank() ? "Enter headless arguments (or -help)." : null);
				if (input.open() != Window.OK) { return null; }
				var copy = config.getWorkingCopy();
				copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, input.getValue());
				config = copy.doSave();
			}
			String mode = "debug".equals(event.getParameter("gama.ui.devtools.launchMode"))
					? ILaunchManager.DEBUG_MODE : ILaunchManager.RUN_MODE;
			DebugUITools.launch(config, mode);
		} catch (CoreException e) {
			ResourceUtils.log(e);
			MessageDialog.openError(shell, "Cannot launch GAMA", e.getStatus().getMessage());
		}
		return null;
	}
}
