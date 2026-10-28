/*******************************************************************************************************
 *
 * RegenerateGrammarHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.ResourceUtils;

/** Runs the MWE2 workflow that generates the GAML grammar artifacts. */
public final class RegenerateGrammarHandler extends AbstractHandler {

	private static final String[] PROJECTS = { "gaml.grammar", "gaml.compiler", "gama.ui.editor" };
	private static final String CONFIGURATION_NAME = "Regenerate GAML Grammar";
	private static final String WORKFLOW_MAIN = "org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher";
	private static final String WORKFLOW_MODULE = "gaml.compiler.gaml.GenerateGaml";

	@Override
	public Object execute(final ExecutionEvent event) {
		var shell = HandlerUtil.getActiveShell(event);
		var workspace = GamaProjects.root();
		for (String name : PROJECTS) {
			IProject project = workspace.getProject(name);
			if (!project.isOpen()) {
				MessageDialog.openError(shell, "Cannot Regenerate GAML Grammar",
						"Import and open the '" + name + "' project first.");
				return null;
			}
		}

		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			if (type == null) {
				MessageDialog.openError(shell, "Cannot Regenerate GAML Grammar",
						"The Eclipse Java Application launch type is not available.");
				return null;
			}
			ILaunchConfiguration previous = existing(manager, type);
			ILaunchConfigurationWorkingCopy copy = previous == null ? type.newInstance(null, CONFIGURATION_NAME)
					: previous.getWorkingCopy();
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "gaml.grammar");
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, WORKFLOW_MAIN);
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, WORKFLOW_MODULE);
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					"${workspace_loc:/gaml.grammar}");
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
			copy.setMappedResources(new IResource[] { workspace.getProject("gaml.grammar") });
			ILaunchConfiguration configuration = copy.doSave();

			ILaunchesListener2 listener = new ILaunchesListener2() {
				@Override
				public void launchesTerminated(final ILaunch[] launches) {
					for (ILaunch launch : launches) {
						if (configuration.equals(launch.getLaunchConfiguration())) {
							manager.removeLaunchListener(this);
							refreshGeneratedProjects();
							return;
						}
					}
				}

				@Override
				public void launchesAdded(final ILaunch[] launches) {}

				@Override
				public void launchesChanged(final ILaunch[] launches) {}

				@Override
				public void launchesRemoved(final ILaunch[] launches) {}
			};
			manager.addLaunchListener(listener);
			try {
				DebugUITools.launch(configuration, ILaunchManager.RUN_MODE);
			} catch (RuntimeException e) {
				manager.removeLaunchListener(listener);
				throw e;
			}
		} catch (CoreException e) {
			ResourceUtils.log(e);
			MessageDialog.openError(shell, "Cannot Regenerate GAML Grammar", e.getStatus().getMessage());
		}
		return null;
	}

	private static ILaunchConfiguration existing(final ILaunchManager manager,
			final ILaunchConfigurationType type) throws CoreException {
		for (ILaunchConfiguration configuration : manager.getLaunchConfigurations(type)) {
			if (CONFIGURATION_NAME.equals(configuration.getName())) { return configuration; }
		}
		return null;
	}

	private static void refreshGeneratedProjects() {
		org.eclipse.core.runtime.jobs.Job.create("Refresh generated GAML grammar artifacts", monitor -> {
			try {
				for (int i = 1; i < PROJECTS.length; i++) {
					GamaProjects.root().getProject(PROJECTS[i]).refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				return Status.OK_STATUS;
			} catch (CoreException e) {
				ResourceUtils.log(e);
				return e.getStatus();
			}
		}).schedule();
	}
}
