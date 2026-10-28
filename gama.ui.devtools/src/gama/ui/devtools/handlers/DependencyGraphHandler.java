/*******************************************************************************************************
 *
 * DependencyGraphHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.handlers;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.devtools.GamaDevToolsActivator;
import gama.ui.devtools.dependencies.DependencyGraphBuilder;
import gama.ui.devtools.dependencies.GraphToHtml;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Computes the static dependency graph of the libraries (jars or bundles) found in a directory using {@code jdeps},
 * and opens it as an interactive HTML page in the external browser.
 */
public class DependencyGraphHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		Path jdeps = Path.of(System.getProperty("java.home"), "bin", "jdeps");
		if (!Files.exists(jdeps) && !Files.exists(Path.of(jdeps + ".exe"))) {
			MessageDialog.openError(shell, "GAMA", "'jdeps' cannot be found in " + jdeps.getParent()
					+ ". Run Eclipse with a full JDK (not a JRE) to use this command.");
			return null;
		}
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setText("Libraries to analyze");
		dialog.setMessage("Choose a directory containing jars or bundles (e.g. the GAMA repository or a 'libs' folder)");
		IProject proc = GamaProjects.processor();
		if (proc != null && proc.getLocation() != null) {
			dialog.setFilterPath(proc.getLocation().removeLastSegments(1).toOSString());
		}
		String dir = dialog.open();
		if (dir == null) return null;
		Job job = Job.create("Computing the dependency graph of " + dir, (IProgressMonitor monitor) -> {
			SubMonitor sub = SubMonitor.convert(monitor, 5);
			try {
				DependencyGraphBuilder builder = new DependencyGraphBuilder();
				sub.subTask("Resolving libraries");
				builder.resolveLibrariesInDirectory(dir);
				sub.worked(1);
				sub.subTask("Listing services");
				builder.listServices();
				sub.worked(1);
				sub.subTask("Running jdeps");
				builder.generate();
				sub.worked(1);
				builder.checkServicesDependenciesRelations();
				sub.worked(1);
				Path out = GamaDevToolsActivator.getInstance().getStateLocation().toPath()
						.resolve("dependencies.html");
				GraphToHtml.generateHtml(builder, out.toString());
				builder.exportGraph(out.resolveSibling("dependencies.dot").toString());
				sub.worked(1);
				Display.getDefault().asyncExec(() -> {
					try {
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(out.toUri().toURL());
					} catch (Exception e) {
						ResourceUtils.log(e);
					}
				});
				return Status.OK_STATUS;
			} catch (RuntimeException e) {
				return ResourceUtils.error("Cannot compute the dependency graph", e);
			}
		});
		job.setUser(true);
		job.schedule();
		return null;
	}

}
