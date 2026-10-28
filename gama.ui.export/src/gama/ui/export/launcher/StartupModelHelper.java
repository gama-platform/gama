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
import gama.export.LauncherConfiguration;
import gama.export.SimulationArtifacts;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.workspace.manager.WorkspacePaths;

public final class StartupModelHelper {

	private static final StartupModelHelper INSTANCE = new StartupModelHelper();
	private static final String CONTEXTUAL_SEPARATOR = "    from model    ";

	private final SimulationArtifacts artifacts = new SimulationArtifacts();
	private final EmbeddedWorkspace workspace = new EmbeddedWorkspace();
	private String modelPath;
	private String experiment;
	private LauncherConfiguration configuration;
	private List<String> selections = List.of();
	private Path projectPath;

	public LauncherConfiguration getConfiguration() { return configuration; }

	public List<String> getSelections() { return selections; }

	public String label(final String selection) {
		final int separator = selection.lastIndexOf('@');
		return separator < 0 ? selection
				: selection.substring(0, separator) + CONTEXTUAL_SEPARATOR + selection.substring(separator + 1);
	}

	private StartupModelHelper() {}

	public static StartupModelHelper getInstance() { return INSTANCE; }

	public boolean initialize() throws IOException, CoreException {
		workspace.registerProjects();
		final String selection = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();
		if (selection == null || selection.isBlank()) throw new IOException("No startup experiment is configured");
		selections = List.of(selection.split("#"));
		configuration = LauncherConfiguration.load(WorkspacePaths.installationDirectory());
		final String startup = configuration.startupExperiment();
		if (!startup.isBlank() && !selections.contains(startup))
			throw new IOException("The configured default experiment is not exported");
		return true;
	}

	private void select(final String selected) {
		final int separator = selected.lastIndexOf('@');
		experiment = separator < 0 ? selected : selected.substring(0, separator);
		modelPath = separator < 0 ? GamaPreferences.Interface.CORE_DEFAULT_MODEL.getValue().getPath(null)
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
		if (GAMA.getExperiment() == compiled.getExperiment(experiment)) {
			projectPath = Path.of(compiled.getProjectPath());
		} else {
			compiled.dispose();
		}
	}

	public boolean areThereAnyArtifactsToSave() throws IOException {
		return projectPath != null && artifacts.hasChanges(projectPath);
	}

	public boolean saveSimulationArtifacts(final Path target, final IProgressMonitor monitor) throws IOException {
		if (projectPath == null) throw new IOException("No simulation project is available");
		return artifacts.save(projectPath, target, monitor);
	}
}
