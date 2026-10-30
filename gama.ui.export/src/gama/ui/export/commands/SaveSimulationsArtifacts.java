package gama.ui.export.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import gama.api.GAMA;
import gama.dev.DEBUG;
import gama.ui.export.launcher.StartupModelHelper;

/** Saves generated workspace files when the player closes. */
public class SaveSimulationsArtifacts extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		final var helper = StartupModelHelper.getInstance();
		try {
			if (!helper.areThereAnyArtifactsToSave()) return Boolean.TRUE;
			final var shell = Display.getDefault().getActiveShell();
			final var dialog = new FileDialog(shell, SWT.SAVE);
			dialog.setText("Save experiment data as ZIP");
			dialog.setFilterExtensions(new String[] { "*.zip" });
			dialog.setFilterNames(new String[] { "ZIP archive (*.zip)" });
			dialog.setFilterPath(System.getProperty("user.home"));
			final String name = helper.getPresentation().title().replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").strip();
			dialog.setFileName((name.isBlank() ? "Experiment" : name) + "-data-" + LocalDate.now() + ".zip");
			dialog.setOverwrite(true);
			final String selected = dialog.open();
			if (selected == null) return Boolean.FALSE;
			final Path target = Path.of(selected);
			if (!target.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip"))
				throw new IOException("Choose a filename with the .zip extension.");
			new ProgressMonitorDialog(shell).run(true, true, monitor -> {
				try {
					helper.saveSimulationArtifacts(target, monitor);
				} catch (final IOException e) {
					throw new InvocationTargetException(e);
				}
			});
			return Boolean.TRUE;
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			return Boolean.FALSE;
		} catch (final OperationCanceledException e) {
			return Boolean.FALSE;
		} catch (final InvocationTargetException e) {
			if (!(e.getCause() instanceof OperationCanceledException)) reportFailure(e.getCause());
			return Boolean.FALSE;
		} catch (final IOException | RuntimeException e) {
			reportFailure(e);
			return Boolean.FALSE;
		}
	}

	private static void reportFailure(final Throwable error) {
		DEBUG.ERR("Unable to save experiment data", error);
		GAMA.getGui().getDialogFactory().error("Unable to save experiment data: " + error.getMessage());
	}
}
