/*******************************************************************************************************
 *
 * GenerateTestStubsHandler.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import gama.ui.devtools.quickfix.TestStubGenerator;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.Notifier;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Generates GAML test stubs for all the operators reported as "not tested" in the selected GAMA plugins (or all of
 * them).
 */
public class GenerateTestStubsHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		List<IProject> targets = GamaProjects.fromSelectionOrAll(HandlerUtil.getCurrentSelection(event));
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		WorkspaceJob job = new WorkspaceJob("Generating GAML test stubs for untested operators") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				List<IFile> files = TestStubGenerator.generate(targets, null, monitor);
				if (files.isEmpty()) {
					Notifier.show("GAML test stubs", "No new untested operator found. If you expected some, run "
							+ "'GAMA > Clean all...' first: the warnings are produced by the processor.");
				} else {
					Notifier.show("GAML test stubs", "Test stubs written in " + files.size() + " plugin(s) ("
							+ TestStubGenerator.PATH + ")");
					Display.getDefault().asyncExec(() -> {
						try {
							IDE.openEditor(window.getActivePage(), files.get(0));
						} catch (Exception e) {
							ResourceUtils.log(e);
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

}
