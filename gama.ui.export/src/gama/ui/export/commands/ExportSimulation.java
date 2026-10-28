package gama.ui.export.commands;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.api.GAMA;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.export.ExportModels;
import gama.export.GamaZipBuilder;
import gama.ui.export.dialogs.ExportModelDialog;

public final class ExportSimulation extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try {
			final List<IFile> files;
			if (event.getCommand().getId().equals("gama.ui.export.model")) {
				final var editor = HandlerUtil.getActiveEditorChecked(event);
				if (!(editor.getEditorInput() instanceof IFileEditorInput input)) return null;
				editor.doSave(null);
				if (editor.isDirty()) return null;
				files = List.of(input.getFile());
			} else {
				final var selection = HandlerUtil.getCurrentSelection(event);
				if (!(selection instanceof IStructuredSelection structured)) return null;
				final IResource resource = Adapters.adapt(structured.getFirstElement(), IResource.class);
				if (!(resource instanceof IContainer container)) return null;
				files = ExportModels.modelFiles(container);
			}
			if (GamaPreferences.Modeling.EDITOR_SAVE.getValue()
					&& !window.getActivePage().saveAllEditors(GamaPreferences.Modeling.EDITOR_SAVE_ASK.getValue()))
				return null;
			final ExportModels models = ExportModels.discover(files);
			final boolean multipleModels = models.roots().size() > 1;
			final var experiments = models.experiments();
			final String[] labels = experiments.stream().map(experiment -> experiment.label(multipleModels))
					.toArray(String[]::new);
			final var dialog = new ExportModelDialog(labels);
			if (dialog.open() != IDialogConstants.OK_ID) return null;
			final List<ExportModels.Experiment> selected = experiments.stream()
					.filter(experiment -> Arrays.asList(dialog.getSelectedExperiments())
							.contains(experiment.label(multipleModels))).toList();
			if (selected.isEmpty()) throw new IOException("Select at least one experiment to export");
			final boolean multipleSelectedModels = multipleModels && selected.size() > 1;
			final String modelPath = multipleSelectedModels ? "" : selected.getFirst().modelPath();
			final String selection = selected.stream().map(experiment -> experiment.launcherSelection(multipleSelectedModels))
					.collect(Collectors.joining("#"));
			final Path output = Path.of(dialog.getOutputPath(), dialog.getOutputFileName());
			final var builder = new GamaZipBuilder(models.plugins(), models.projects(), modelPath, selection,
					models.dataFiles(), dialog.getIncludeJdk(), dialog.getOneFile());
			final String startup = selected.stream()
					.filter(experiment -> experiment.label(multipleModels).equals(dialog.getStartupExperiment()))
					.map(experiment -> experiment.launcherSelection(multipleSelectedModels)).findFirst().orElse("");
			builder.setLauncherConfiguration(dialog.getLauncherConfiguration(
					files.size() == 1 ? files.getFirst().getName() : files.getFirst().getProject().getName(), startup));
			new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
				try {
					builder.zip(output.toString(), monitor);
				} catch (final IOException e) {
					throw new InvocationTargetException(e);
				}
			});
			if (Desktop.isDesktopSupported() && GAMA.getGui().getDialogFactory()
					.question("Model export successful", "Do you want to show the target directory?")) {
				Desktop.getDesktop().open(output.toAbsolutePath().getParent().toFile());
			}
		} catch (final InterruptedException | OperationCanceledException e) {
			// Cancellation leaves the destination unchanged.
		} catch (final InvocationTargetException e) {
			if (!(e.getCause() instanceof OperationCanceledException)) {
				reportFailure(e.getCause());
			}
		} catch (final IOException | CoreException | RuntimeException e) {
			reportFailure(e);
		}
		return null;
	}

	private static void reportFailure(final Throwable error) {
		DEBUG.ERR("Unable to export the simulation", error);
		GAMA.getGui().getDialogFactory().error("Unable to export the simulation: " + error.getMessage());
	}
}
