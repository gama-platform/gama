package gama.ui.export.commands;

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
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.api.GAMA;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.export.ExportModels;
import gama.export.GamaZipBuilder;
import gama.export.SimulationPackageExporter;
import gama.ui.export.dialogs.ExportModelDialog;
import gama.ui.navigator.view.contents.WrappedExperimentContent;

public final class ExportSimulation extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try {
			final String command = event.getCommand().getId();
			final List<IFile> files;
			if (command.equals("gama.ui.export.model")) {
				final var editor = HandlerUtil.getActiveEditorChecked(event);
				if (!(editor.getEditorInput() instanceof IFileEditorInput input)) return null;
				editor.doSave(null);
				if (editor.isDirty()) return null;
				files = List.of(input.getFile());
			} else {
				final var selection = HandlerUtil.getCurrentSelection(event);
				if (!(selection instanceof IStructuredSelection structured) || structured.size() != 1)
					throw new ExecutionException("Select one model, experiment, folder or project to export");
				final IResource resource = selectedResource(structured.getFirstElement());
				if (resource instanceof IFile file) {
					files = List.of(file);
				} else if (resource instanceof IContainer container) {
					files = ExportModels.modelFiles(container);
				} else {
					throw new ExecutionException("The selection does not contain a model to export");
				}
			}
			if (GamaPreferences.Modeling.EDITOR_SAVE.getValue()
					&& !window.getActivePage().saveAllEditors(GamaPreferences.Modeling.EDITOR_SAVE_ASK.getValue()))
				return null;
			final ExportModels models = ExportModels.discover(files);
			final boolean multipleModels = models.roots().size() > 1;
			final var experiments = models.experiments();
			final String[] labels = experiments.stream().map(experiment -> experiment.label(multipleModels))
					.toArray(String[]::new);
			final var dialog = new ExportModelDialog(labels, models.defaultTitle(), models.defaultDescription());
			final var dependencies = new java.util.concurrent.atomic.AtomicReference<List<SimulationPackageExporter.Dependency>>();
			final var dependencyFailure = new java.util.concurrent.atomic.AtomicReference<IOException>();
			new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
				monitor.beginTask("Discovering simulation plugin dependencies", org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
				try {
					dependencies.set(SimulationPackageExporter.dependencies(models, monitor));
				} catch (final IOException e) {
					dependencyFailure.set(e);
				} finally {
					monitor.done();
				}
			});
			if (dependencyFailure.get() == null) {
				dialog.setDependencies(dependencies.get());
			} else {
				DEBUG.ERR("Unable to resolve exported model dependencies", dependencyFailure.get());
				dialog.setDependencyError(dependencyFailure.get().getMessage());
			}
			if (dialog.open() != IDialogConstants.OK_ID) return null;
			final List<ExportModels.Experiment> selected = experiments.stream()
					.filter(experiment -> Arrays.asList(dialog.getSelectedExperiments())
							.contains(experiment.label(multipleModels))).toList();
			if (selected.isEmpty()) throw new IOException("Select at least one experiment to export");
			final Path output = Path.of(dialog.getOutputPath(), dialog.getOutputFileName());
			if (dialog.getFormat() == ExportModelDialog.Format.PACKAGE) {
				final String startup = selected.stream()
						.filter(experiment -> experiment.label(multipleModels).equals(dialog.getStartupExperiment()))
						.map(experiment -> experiment.name() + "@" + experiment.modelPath().replaceFirst("^/", ""))
						.findFirst().orElse("");
				final var configuration = dialog.getLauncherConfiguration(models.defaultTitle(), startup);
				new ProgressMonitorDialog(window.getShell()).run(true, true, monitor -> {
					try {
						SimulationPackageExporter.export(models, selected, configuration, dialog.getPackageId(),
								dialog.getPackageVersion(), dialog.getEmbeddedPlugins(), output, monitor);
					} catch (final IOException | CoreException e) {
						throw new InvocationTargetException(e);
					}
				});
			} else {
				final boolean multipleSelectedModels = multipleModels && selected.size() > 1;
				final String modelPath = multipleSelectedModels ? "" : selected.getFirst().modelPath();
				final String selection = selected.stream().map(experiment -> experiment.launcherSelection(multipleSelectedModels))
						.collect(Collectors.joining("#"));
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
			}
			final boolean show = GAMA.getGui().getDialogFactory().question("Model export successful",
					"The model was exported to:\n" + output.toAbsolutePath() + "\n\nDo you want to show the target directory?");
			if (show) { Program.launch(output.toAbsolutePath().getParent().toString()); }
		} catch (final InterruptedException | OperationCanceledException e) {
			// Cancellation leaves the destination unchanged.
		} catch (final InvocationTargetException e) {
			if (!(e.getCause() instanceof OperationCanceledException)) {
				reportFailure(e.getCause());
			}
		} catch (final IOException | CoreException | RuntimeException | LinkageError e) {
			reportFailure(e);
		}
		return null;
	}

	private static IResource selectedResource(final Object selection) {
		if (selection instanceof WrappedExperimentContent experiment) return experiment.getFile().getResource();
		return Adapters.adapt(selection, IResource.class);
	}

	private static void reportFailure(final Throwable error) {
		DEBUG.ERR("Unable to export the simulation", error);
		GAMA.getGui().getDialogFactory().error("Unable to export the simulation: " + (error.getMessage() == null ? error.toString() : error.getMessage()));
	}
}
