/*******************************************************************************************************
 *
 * CleanAndRebuildHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.devtools.jobs.CleanAndRebuildJob;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.Notifier;

/**
 * Resets annotation processing and rebuilds the GAMA plugins, without any confirmation dialog. With the parameter
 * {@value #SCOPE} set to {@code selection}, only the GAMA plugins of the current selection are reset; otherwise, all
 * of them are.
 */
public class CleanAndRebuildHandler extends AbstractHandler {

	/** The id of the command parameter. */
	public static final String SCOPE = "gama.ui.devtools.scope";

	@Override
	public Object execute(final ExecutionEvent event) {
		boolean selection = "selection".equals(event.getParameter(SCOPE));
		List<IProject> targets = selection ? GamaProjects.fromSelection(HandlerUtil.getCurrentSelection(event))
				: GamaProjects.all();
		if (targets.isEmpty()) {
			Notifier.show("GAMA", selection ? "No GAMA plugin selected" : "No GAMA plugin in the workspace");
			return null;
		}
		PlatformUI.getWorkbench().saveAllEditors(false);
		new CleanAndRebuildJob(targets).schedule();
		return null;
	}

}
