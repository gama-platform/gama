package gama.ui.export.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.dev.DEBUG;
import gama.export.ProjectArchive;
import gama.export.ProjectPlugins;
import gama.ui.export.ProjectArchiveImport;
import gama.ui.export.dialogs.ProjectImportDialog;
import gama.ui.navigator.view.contents.UserGroupFolder;
import gama.ui.navigator.view.contents.UserGroups;

public final class ImportProject extends AbstractHandler {

	public static final String COMMAND = "gama.ui.export.importProject";
	public static final String PATH_PARAMETER = "gama.ui.export.projectPath";
	public static final String GROUP_PARAMETER = "gama.ui.export.projectGroup";

	@Override public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		String selected = event.getParameter(PATH_PARAMETER);
		if (selected == null) {
			final var selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof IStructuredSelection structured && structured.size() == 1) {
				final var file = org.eclipse.core.runtime.Adapters.adapt(structured.getFirstElement(), org.eclipse.core.resources.IFile.class);
				if (file != null && file.getLocation() != null
						&& ("gamp".equalsIgnoreCase(file.getFileExtension()) || "zip".equalsIgnoreCase(file.getFileExtension())))
					selected = file.getLocation().toOSString();
			}
		}
		if (selected == null) {
			final var dialog = new FileDialog(window.getShell(), SWT.OPEN);
			dialog.setText("Import GAMA project (.gamp or .zip)");
			dialog.setFilterExtensions(new String[] { "*.gamp;*.zip", "*.*" });
			dialog.setFilterNames(new String[] { "GAMA projects and ZIP archives", "All files" });
			selected = dialog.open();
			if (selected == null) return null;
		}
		String group = event.getParameter(GROUP_PARAMETER);
		final var selection = HandlerUtil.getCurrentSelection(event);
		if (group == null && selection instanceof IStructuredSelection structured
				&& structured.getFirstElement() instanceof UserGroupFolder folder)
			group = folder.getRelativePath().toPortableString();
		return open(Path.of(selected), group == null ? "" : group);
	}

	public static boolean open(final Path archive, final String group) {
		final var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final var imported = new java.util.concurrent.atomic.AtomicBoolean();
		try {
			final var preview = ProjectArchive.inspect(archive);
			final var dialog = new ProjectImportDialog(window.getShell(), preview, group);
			if (dialog.open() != IDialogConstants.OK_ID) return false;
			if (!window.getActivePage().saveAllEditors(true)) return false;
			final var restart = new java.util.concurrent.atomic.AtomicBoolean();
			final var pluginFailure = new AtomicReference<IOException>();
			final var backups = new AtomicReference<java.util.List<Path>>(java.util.List.of());
			new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
				try (var extracted = ProjectArchive.extract(archive, monitor::isCanceled)) {
					// Recheck after extraction, not only against the preview's possibly stale metadata.
					if (!extracted.info().equals(preview))
						throw new IOException("The archive changed while it was being imported. Open it again.");
					final var missing = ProjectPlugins.missing(extracted.info());
					try (var plugins = ProjectPlugins.prepare(extracted, dialog.installPlugins()
							? missing.stream().filter(p -> !p.artifact().isEmpty()).toList() : java.util.List.of())) {
						ResourcesOperation.run(() -> backups.set(ProjectArchiveImport.run(extracted, dialog.choices(), dialog.group(), monitor)), monitor);
						imported.set(true);
						try { restart.set(plugins.publish()); }
						catch (final IOException e) {
							DEBUG.ERR("Projects imported, but plugin installation failed", e);
							pluginFailure.set(e);
						}
					}
				} catch (final IOException | CoreException e) { throw new InvocationTargetException(e); }
			});
			UserGroups.refreshNavigator();
			if (pluginFailure.get() != null) MessageDialog.openWarning(window.getShell(), "Projects imported; plugins not installed",
					"The projects were imported successfully, but their plugins could not be installed:\n"
							+ pluginFailure.get().getMessage() + "\nInstall the required plugins before running the models.");
			if (!backups.get().isEmpty()) MessageDialog.openInformation(window.getShell(), "Project imported",
					"Original project backups were retained at:\n" + String.join("\n", backups.get().stream().map(Path::toString).toList()));
			if (restart.get() && MessageDialog.openQuestion(window.getShell(), "Restart GAMA",
					"The included plugins were installed in GAMA's Dropins folder.\nRestart GAMA now to activate them?"))
				PlatformUI.getWorkbench().restart();
			return true;
		} catch (final InterruptedException | OperationCanceledException e) {
			return false;
		} catch (final InvocationTargetException e) {
			if (!imported.get() && (e.getCause() instanceof OperationCanceledException
					|| e.getCause() instanceof java.io.InterruptedIOException)) return false;
			report(e.getCause(), imported.get());
			return imported.get();
		} catch (final IOException | RuntimeException e) {
			report(e, imported.get());
			return imported.get();
		}
	}

	private static void report(final Throwable error, final boolean imported) {
		final String title = imported ? "Projects imported; finalisation incomplete" : "Unable to import project";
		DEBUG.ERR(title, error);
		UserGroups.refreshNavigator();
		MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title,
				(imported ? "The projects are imported, but plugin provisioning or temporary-file cleanup failed.\n"
						+ "Review installed plugins and restart GAMA if needed.\n\n" : "") + error.getMessage());
	}

	private static final class ResourcesOperation {
		@FunctionalInterface interface Operation { void run() throws IOException, CoreException; }
		static void run(final Operation operation, final org.eclipse.core.runtime.IProgressMonitor monitor)
				throws CoreException, IOException {
			final var failure = new AtomicReference<IOException>();
			org.eclipse.core.resources.ResourcesPlugin.getWorkspace().run(m -> {
				try { operation.run(); } catch (final IOException e) { failure.set(e); }
			}, org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot(),
					org.eclipse.core.resources.IWorkspace.AVOID_UPDATE, monitor);
			if (failure.get() != null) throw failure.get();
		}
	}
}
