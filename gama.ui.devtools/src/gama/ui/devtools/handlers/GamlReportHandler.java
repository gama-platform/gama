/*******************************************************************************************************
 *
 * GamlReportHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.handlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import gama.ui.devtools.GamaDevToolsActivator;
import gama.ui.devtools.jobs.GamlReport;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Produces a Markdown report listing the undocumented elements and untested operators of the selected GAMA plugins
 * (or all of them), and opens it in an editor.
 */
public class GamlReportHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		List<IProject> targets = GamaProjects.fromSelectionOrAll(HandlerUtil.getCurrentSelection(event));
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		Job job = Job.create("Building the GAML documentation and tests report", (IProgressMonitor monitor) -> {
			try {
				String report = GamlReport.build(targets);
				Path file = GamaDevToolsActivator.getInstance().getStateLocation().toPath()
						.resolve("gaml-report.md");
				Files.writeString(file, report, StandardCharsets.UTF_8);
				Display.getDefault().asyncExec(() -> {
					try {
						IDE.openEditorOnFileStore(window.getActivePage(), EFS.getLocalFileSystem().fromLocalFile(file.toFile()));
					} catch (Exception e) {
						ResourceUtils.log(e);
					}
				});
				return Status.OK_STATUS;
			} catch (CoreException | IOException e) {
				return ResourceUtils.error("Cannot build the report", e);
			}
		});
		job.setUser(true);
		job.schedule();
		return null;
	}

}
