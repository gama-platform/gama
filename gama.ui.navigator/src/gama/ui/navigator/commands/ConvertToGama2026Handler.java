/*******************************************************************************************************
 *
 * ConvertToGama2026Handler.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import gaml.compiler.transition.GamlFileProcessor;

/**
 * Runs the GAMA 2025 -> 2026 syntax conversion scripts on the selected projects, folders or GAML files.
 */
public class ConvertToGama2026Handler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IStructuredSelection sel = HandlerUtil.getCurrentStructuredSelection(event);
		final Set<IResource> resources = new LinkedHashSet<>();
		for (final Object o : sel.toList()) {
			final IResource r = o instanceof IResource res ? res : org.eclipse.core.runtime.Adapters.adapt(o, IResource.class);
			if (r != null && r.getLocation() != null) { resources.add(r); }
		}
		if (resources.isEmpty()) return null;
		final boolean ok = MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Convert to GAMA 2026 syntax",
				"All .gaml and .experiment files in the " + resources.size()
						+ " selected item(s) will be rewritten in place to the GAMA 2026 syntax.\n\n"
						+ "This cannot be undone: please make a backup (or use version control) first. Continue?");
		if (!ok) return null;
		// Unsaved edits would be lost or conflict with the rewritten files
		if (!PlatformUI.getWorkbench().saveAllEditors(true)) return null;

		final Job job = new Job("Converting models to GAMA 2026 syntax") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final GamlFileProcessor processor = new GamlFileProcessor();
				int changed = 0;
				try {
					for (final IResource r : resources) {
						final Path p = r.getLocation().toFile().toPath();
						if (r.getType() == IResource.FILE) {
							if (processor.getExtensions().stream().anyMatch(p.getFileName().toString()::endsWith)
									&& processor.processFile(p, false)) {
								changed++;
							}
						} else {
							changed += processor.processDirectory(p, false);
						}
						r.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}
				} catch (final IOException | org.eclipse.core.runtime.CoreException e) {
					return Status.error("Conversion failed: " + e.getMessage(), e);
				}
				final int n = changed;
				Display.getDefault().asyncExec(() -> {
					reloadOpenEditors(resources);
					MessageDialog.openInformation(Display.getDefault().getActiveShell(),
						"Convert to GAMA 2026 syntax", n + " file(s) updated.");
				});
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	/**
	 * Reloads from disk the editors showing a file located under one of the converted resources.
	 */
	private static void reloadOpenEditors(final Set<IResource> resources) {
		for (final IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (final IWorkbenchPage page : w.getPages()) {
				for (final IEditorReference ref : page.getEditorReferences()) {
					final IEditorPart part = ref.getEditor(false);
					if (!(part instanceof final ITextEditor editor)) { continue; }
					if (!(editor.getEditorInput() instanceof final IFileEditorInput input)) { continue; }
					final IFile file = input.getFile();
					if (resources.stream().anyMatch(r -> r.getFullPath().isPrefixOf(file.getFullPath()))) {
						editor.doRevertToSaved();
					}
				}
			}
		}
	}
}
