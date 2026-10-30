package gama.ui.export.launcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import gama.api.GAMA;
import gama.api.kernel.species.IModelSpecies;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.export.EmbeddedWorkspace;
import gama.export.ExportHelper;
import gama.export.LauncherConfiguration;
import gama.export.SimulationArtifacts;
import gama.export.SimulationLaunchContext;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.workspace.manager.WorkspacePaths;
import gama.ui.shared.menus.ExperimentMenu;
import gama.ui.shared.utils.LaunchPresentation;

public final class StartupModelHelper {

	private static final StartupModelHelper INSTANCE = new StartupModelHelper();

	private final SimulationArtifacts artifacts = new SimulationArtifacts();
	private final EmbeddedWorkspace workspace = new EmbeddedWorkspace();
	private String modelPath;
	private String experiment;
	private LauncherConfiguration configuration;
	private List<String> selections = List.of();
	private boolean startupAttempted;
	private SimulationLaunchContext context;

	public LauncherConfiguration getConfiguration() { return configuration; }

	public List<String> getSelections() { return selections; }

	public Path getContentRoot() { return context.content(); }

	public LaunchPresentation.Content getPresentation() {
		return new LaunchPresentation.Content(
				configuration.title().isBlank() ? "GAMA playable model" : configuration.title(),
				configuration.description(), configuration.image().isBlank() ? null : getContentRoot().resolve(configuration.image()),
				selections.stream().map(selection -> new ExperimentMenu.Entry(selection, label(selection), "gui")).toList());
	}

	public boolean isHomeVisibleAtStartup() { return configuration.showHome() || startupAttempted; }

	public void launchDefaultExperiment() {
		if (startupAttempted || configuration.showHome()) return;
		startupAttempted = true;
		launch(configuration.startupExperiment());
	}

	public String label(final String selection) {
		final int separator = selection.lastIndexOf('@');
		return "Experiment " + (separator < 0 ? selection : selection.substring(0, separator));
	}

	private StartupModelHelper() {}

	public static StartupModelHelper getInstance() { return INSTANCE; }

	public boolean initialize() throws IOException, CoreException {
		final String selection = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();
		if (selection == null || selection.isBlank()) throw new IOException("No startup experiment is configured");
		final var defaultModel = GamaPreferences.Interface.CORE_DEFAULT_MODEL.getValue();
		try {
			return initialize(new SimulationLaunchContext(WorkspacePaths.installationDirectory(),
					Path.of(ExportHelper.toAbsoluteFromEmbeddedWorkspacePath("")),
					LauncherConfiguration.load(WorkspacePaths.installationDirectory()), List.of(selection.split("#")),
					defaultModel == null ? "" : defaultModel.getPath(null)));
		} catch (final IllegalArgumentException e) {
			throw new IOException("Invalid simulation launch configuration: " + e.getMessage(), e);
		}
	}

	public boolean initialize(final SimulationLaunchContext launchContext) throws IOException, CoreException {
		if (context != null) throw new IllegalStateException("The simulation player is already initialized");
		workspace.registerProjects(launchContext.workspace());
		artifacts.initialize(launchContext.workspace());
		context = launchContext;
		selections = context.selections();
		configuration = context.configuration();
		return true;
	}

	private void select(final String selected) {
		final int separator = selected.lastIndexOf('@');
		experiment = separator < 0 ? selected : selected.substring(0, separator);
		modelPath = separator < 0 ? context.defaultModel()
				: selected.substring(separator + 1);
	}

	public void launch(final String selection) {
		if (!selections.contains(selection)) throw new IllegalArgumentException("Unknown experiment: " + selection);
		if (!GAMA.getControllers().isEmpty() || !PerspectiveHelper.isHomePerspective(PerspectiveHelper.currentPerspectiveId)
				|| PerspectiveHelper.currentSimulationPerspective != null)
			throw new IllegalStateException("Return to Home and close the current experiment before choosing another");
		select(selection);
		startSimulation();
	}

	private void startSimulation() {
		// A fresh species lets normal experiment disposal run unchanged when switching.
		final IModelSpecies compiled;
		try {
			compiled = workspace.compile(modelPath);
		} catch (final IOException e) {
			DEBUG.ERR("Unable to compile the startup model", e);
			GAMA.getGui().getDialogFactory().error(e.getMessage());
			return;
		}
		if (compiled.getExperiment(experiment) == null) {
			compiled.dispose();
			GAMA.getGui().getDialogFactory().error("The startup experiment does not exist: " + experiment);
			return;
		}
		GAMA.runGuiExperiment(experiment, compiled);
		if (GAMA.getExperiment() != compiled.getExperiment(experiment)) {
			compiled.dispose();
		}
	}

	public boolean areThereAnyArtifactsToSave() throws IOException {
		return context != null && artifacts.hasChanges(context.workspace());
	}

	public void saveSimulationArtifacts(final Path target, final IProgressMonitor monitor) throws IOException {
		if (context == null) throw new IOException("No simulation workspace is available");
		artifacts.saveZip(context.workspace(), target, monitor);
	}
}
