package gama.ui.export.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.dev.DEBUG;
import gama.export.ProjectArchiveExporter;
import gama.ui.export.dialogs.ProjectExportDialog;

public final class ExportProject extends AbstractHandler {

	@Override public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var selection = HandlerUtil.getCurrentSelection(event);
		if (!(selection instanceof IStructuredSelection structured) || structured.size() != 1)
			throw new ExecutionException("Select one project to export");
		final IResource resource = Adapters.adapt(structured.getFirstElement(), IResource.class);
		if (resource == null) throw new ExecutionException("Select a GAMA project to export");
		export(HandlerUtil.getActiveWorkbenchWindowChecked(event), resource.getProject());
		return null;
	}

	public static boolean export(final IWorkbenchWindow window, final IProject project) {
		if (!window.getActivePage().saveAllEditors(true)) return false;
		try {
			final var plan = new AtomicReference<ProjectArchiveExporter.Plan>();
			new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
				try { plan.set(ProjectArchiveExporter.plan(project, monitor)); }
				catch (final CoreException | IOException e) { throw new InvocationTargetException(e); }
			});
			final var dialog = new ProjectExportDialog(window.getShell(), plan.get());
			if (dialog.open() != IDialogConstants.OK_ID) return false;
			new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
				try {
					ProjectArchiveExporter.export(plan.get(), dialog.resolveLinks(), dialog.includePlugins(), dialog.output(), monitor);
				} catch (final IOException | CoreException e) { throw new InvocationTargetException(e); }
			});
			return true;
		} catch (final InterruptedException | OperationCanceledException e) {
			return false;
		} catch (final InvocationTargetException e) {
			if (e.getCause() instanceof OperationCanceledException || e.getCause() instanceof java.io.InterruptedIOException) return false;
			DEBUG.ERR("Unable to export GAMA project", e.getCause());
			MessageDialog.openError(window.getShell(), "Unable to export project", e.getCause().getMessage());
			return false;
		} catch (final RuntimeException e) {
			DEBUG.ERR("Unable to export GAMA project", e);
			MessageDialog.openError(window.getShell(), "Unable to export project", e.getMessage());
			return false;
		}
	}
}
