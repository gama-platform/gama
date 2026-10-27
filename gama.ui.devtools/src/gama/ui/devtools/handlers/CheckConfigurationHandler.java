/*******************************************************************************************************
 *
 * CheckConfigurationHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.devtools.config.ConfigurationIssue;
import gama.ui.devtools.config.PluginConfigurationChecker;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.Notifier;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Checks the configuration of the selected GAMA plugins (or all of them), creates markers in the Problems view and
 * proposes to repair the fixable issues.
 */
public class CheckConfigurationHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		List<IProject> targets = GamaProjects.fromSelectionOrAll(HandlerUtil.getCurrentSelection(event));
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		WorkspaceJob job = new WorkspaceJob("Checking GAMA plugins configuration") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				List<ConfigurationIssue> issues = PluginConfigurationChecker.check(targets, monitor);
				PluginConfigurationChecker.updateMarkers(targets, true, issues);
				Display.getDefault().asyncExec(() -> report(window, targets, issues));
				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.setUser(true);
		job.schedule();
		return null;
	}

	private static void report(final IWorkbenchWindow window, final List<IProject> targets,
			final List<ConfigurationIssue> issues) {
		if (issues.isEmpty()) {
			Notifier.show("GAMA plugins configuration", targets.size() + " plugin(s) checked: no problem found");
			return;
		}
		try {
			if (window != null) { window.getActivePage().showView(IPageLayout.ID_PROBLEM_VIEW); }
		} catch (PartInitException e) {
			ResourceUtils.log(e);
		}
		List<ConfigurationIssue> fixable = issues.stream().filter(i -> i.kind().fixable).toList();
		if (fixable.isEmpty()) {
			Notifier.show("GAMA plugins configuration", issues.size()
					+ " problem(s) found, none can be repaired automatically. See the Problems view.");
			return;
		}
		Shell shell = window == null ? null : window.getShell();
		StringBuilder sb = new StringBuilder();
		sb.append(issues.size()).append(" problem(s) found in ").append(targets.size()).append(" plugin(s), ")
				.append(fixable.size()).append(" of which can be repaired automatically:\n\n");
		fixable.stream().limit(15).forEach(i -> sb.append("- ").append(i).append("\n"));
		if (fixable.size() > 15) { sb.append("- ...\n"); }
		sb.append("\nRepair them now?");
		if (!MessageDialog.openQuestion(shell, "GAMA plugins configuration", sb.toString())) return;
		WorkspaceJob repair = new WorkspaceJob("Repairing GAMA plugins configuration") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				int n = PluginConfigurationChecker.repair(fixable, monitor);
				List<ConfigurationIssue> remaining = PluginConfigurationChecker.check(targets, monitor);
				PluginConfigurationChecker.updateMarkers(targets, true, remaining);
				Notifier.show("GAMA plugins configuration", n + " problem(s) repaired, " + remaining.size()
						+ " remaining. Run 'Clean and Rebuild GAMA Plugins' to regenerate the GAML additions.");
				return Status.OK_STATUS;
			}
		};
		repair.setRule(ResourcesPlugin.getWorkspace().getRoot());
		repair.setUser(true);
		repair.schedule();
	}

}
