/*******************************************************************************************************
 *
 * ReallyRefreshHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.devtools.GamaDevToolsActivator;

/**
 * Command handler that performs a "Really Refresh" of all open GAMA plugin projects in the workspace. This is a
 * multi-step operation designed to resolve persistent build and annotation-processor problems:
 *
 * <ol>
 * <li>Collects all open projects that have the PDE {@code PluginNature}.</li>
 * <li>For each project, deletes the generated-code folder ({@code gaml/}) if it exists.</li>
 * <li>Closes and immediately re-opens each project to flush Eclipse's internal state.</li>
 * <li>Deletes {@code .classpath} and {@code .factorypath} from the project root, then calls
 * {@link IProject#refreshLocal(int, IProgressMonitor)} so that PDE rebuilds them from
 * {@code MANIFEST.MF}.</li>
 * <li>Triggers a full workspace build via
 * {@link IWorkspace#build(int, IProgressMonitor)}.</li>
 * </ol>
 *
 * <p>
 * The entire sequence runs inside an Eclipse {@link Job} so that the UI remains responsive. A confirmation dialog
 * is shown before the operation begins.
 * </p>
 *
 * @author GAMA Development Team
 * @since 2026
 */
public class ReallyRefreshHandler extends AbstractHandler {

	/** The PDE plug-in nature identifier used to identify GAMA plugin projects. */
	private static final String PDE_PLUGIN_NATURE = "org.eclipse.pde.PluginNature";

	/** The name of the generated-source folder produced by gama.processor. */
	private static final String GENERATED_FOLDER = "gaml";

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Shows a confirmation dialog. If the user confirms, schedules a background {@link Job} that performs the full
	 * really-refresh sequence on all open GAMA plugin projects, then triggers a full workspace build.
	 * </p>
	 *
	 * @param event
	 *            the execution event carrying the current workbench state
	 * @return {@code null} always (per Eclipse handler contract)
	 * @throws ExecutionException
	 *             if the handler cannot access the active workbench window
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		boolean confirmed = MessageDialog.openConfirm(HandlerUtil.getActiveShellChecked(event),
				"Really Refresh Workspace",
				"This will delete generated code, close/reopen all GAMA plugin projects, remove .classpath "
						+ "and .factorypath files, and trigger a full workspace rebuild.\n\nContinue?");
		if (!confirmed) { return null; }

		Job job = new Job("Really Refresh GAMA Workspace") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				return doReallyRefresh(monitor);
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	/**
	 * Performs the full really-refresh sequence.
	 *
	 * <p>
	 * Steps:
	 * </p>
	 * <ol>
	 * <li>Collect all open GAMA plugin projects.</li>
	 * <li>Delete generated {@code gaml/} folder in each project.</li>
	 * <li>Close then reopen each project.</li>
	 * <li>Delete {@code .classpath} and {@code .factorypath}, then refresh.</li>
	 * <li>Full workspace build.</li>
	 * </ol>
	 *
	 * @param monitor
	 *            the progress monitor for reporting progress and supporting cancellation
	 * @return a status indicating success or the first error encountered
	 */
	private IStatus doReallyRefresh(final IProgressMonitor monitor) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] allProjects = workspace.getRoot().getProjects();
		List<IProject> gamaProjects = new ArrayList<>();

		for (IProject p : allProjects) {
			try {
				if (p.isOpen() && p.hasNature(PDE_PLUGIN_NATURE)) { gamaProjects.add(p); }
			} catch (CoreException e) {
				// skip projects whose nature cannot be determined
			}
		}

		SubMonitor sub = SubMonitor.convert(monitor, "Really Refreshing GAMA Workspace",
				gamaProjects.size() * 3 + 1);

		for (IProject project : gamaProjects) {
			if (monitor.isCanceled()) { return Status.CANCEL_STATUS; }
			try {
				sub.subTask("Cleaning generated code in " + project.getName());
				deleteGeneratedFolder(project, sub.split(1));

				sub.subTask("Closing/reopening " + project.getName());
				project.close(sub.split(1));
				project.open(sub.split(1));

				sub.subTask("Removing metadata from " + project.getName());
				deleteMetadataFiles(project, sub.newChild(0));
				project.refreshLocal(IResource.DEPTH_INFINITE, sub.newChild(0));
			} catch (CoreException e) {
				return new Status(IStatus.ERROR, GamaDevToolsActivator.PLUGIN_ID,
						"Error during Really Refresh of project " + project.getName(), e);
			}
		}

		try {
			sub.subTask("Full workspace build");
			workspace.build(IncrementalProjectBuilder.FULL_BUILD, sub.split(1));
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, GamaDevToolsActivator.PLUGIN_ID, "Error during full workspace build", e);
		}

		return Status.OK_STATUS;
	}

	/**
	 * Deletes the {@value #GENERATED_FOLDER} folder from the given project if it exists.
	 *
	 * @param project
	 *            the project whose generated folder is to be deleted
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 *             if the folder cannot be deleted
	 */
	private void deleteGeneratedFolder(final IProject project, final IProgressMonitor monitor) throws CoreException {
		IFolder generated = project.getFolder(GENERATED_FOLDER);
		if (generated.exists()) { generated.delete(true, true, monitor); }
	}

	/**
	 * Deletes {@code .classpath} and {@code .factorypath} from the root of the given project if they exist.
	 *
	 * @param project
	 *            the project whose metadata files are to be removed
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 *             if a file cannot be deleted
	 */
	private void deleteMetadataFiles(final IProject project, final IProgressMonitor monitor) throws CoreException {
		for (String name : new String[] { ".classpath", ".factorypath" }) {
			IResource file = project.getFile(name);
			if (file.exists()) { file.delete(true, monitor); }
		}
	}

}
