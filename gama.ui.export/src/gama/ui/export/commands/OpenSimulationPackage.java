package gama.ui.export.commands;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.dev.DEBUG;
import gama.export.SimulationPackageOpening;

/** Opens packages in a separate process; no package code is loaded into the modeling session. */
public final class OpenSimulationPackage extends AbstractHandler {

	public static final String COMMAND = "gama.ui.export.openPackage";
	public static final String PATH_PARAMETER = "gama.ui.export.packagePath";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var shell = HandlerUtil.getActiveShellChecked(event);
		String selected = event.getParameter(PATH_PARAMETER);
		if (selected == null && event.getCommand().getId().equals("gama.ui.export.playSelection")) {
			final var selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection structured) {
				final var file = org.eclipse.core.runtime.Adapters.adapt(structured.getFirstElement(), org.eclipse.core.resources.IFile.class);
				if (file != null && file.getLocation() != null) selected = file.getLocation().toOSString();
			}
		}
		if (selected == null) {
			final var dialog = new FileDialog(shell, SWT.OPEN);
			dialog.setText("Play exported model");
			dialog.setFilterExtensions(new String[] { "*.gamx" });
			selected = dialog.open();
			if (selected == null) return null;
		}
		try {
			final var launch = SimulationPackageOpening.open(shell, Path.of(selected));
			final Display display = shell.getDisplay();
			Thread.ofVirtual().name("gama-package-startup").start(() -> observeStartup(launch, display));
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (final IOException | RuntimeException e) {
			reportFailure(e);
		}
		return null;
	}

	private static void observeStartup(final SimulationPackageOpening.Launch launch, final Display display) {
		try {
			SimulationPackageOpening.awaitStartup(launch);
		} catch (final IOException e) {
			notifyFailure(display, e.getMessage());
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			DEBUG.ERR("Playable model startup monitoring was interrupted", e);
		}
	}

	private static void notifyFailure(final Display display, final String message) {
		DEBUG.ERR(message);
		if (!display.isDisposed()) display.asyncExec(() -> {
			if (!display.isDisposed()) MessageDialog.openError(display.getActiveShell(), "Playable model startup", message);
		});
	}

	private static void reportFailure(final Throwable error) {
		DEBUG.ERR("Unable to open the playable model", error);
		MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to open playable model", error.getMessage());
	}
}
