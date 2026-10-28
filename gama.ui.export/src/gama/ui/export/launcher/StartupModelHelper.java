package gama.ui.export.launcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;

import gama.api.GAMA;
import gama.api.kernel.species.IModelSpecies;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.export.EmbeddedWorkspace;
import gama.export.SimulationArtifacts;
import gama.ui.export.dialogs.PickExperimentDialog;

public final class StartupModelHelper {

	private static final StartupModelHelper INSTANCE = new StartupModelHelper();
	private static final String CONTEXTUAL_SEPARATOR = "    from model    ";

	private final SimulationArtifacts artifacts = new SimulationArtifacts();
	private final EmbeddedWorkspace workspace = new EmbeddedWorkspace();
	private String modelPath;
	private String experiment;
	private IModelSpecies model;

	private StartupModelHelper() {}

	public static StartupModelHelper getInstance() { return INSTANCE; }

	public boolean initialize() throws IOException, CoreException {
		workspace.registerProjects();
		final String selection = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();
		if (selection == null || selection.isBlank()) throw new IOException("No startup experiment is configured");
		if (selection.contains("#") || selection.contains("@")) return pickExperiment();
		experiment = selection;
		modelPath = GamaPreferences.Interface.CORE_DEFAULT_MODEL.getValue().getPath(null);
		return true;
	}

	public boolean pickExperiment() {
		final String[] selections = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue().split("#");
		final String[] labels = Arrays.stream(selections).map(selection -> {
			final int separator = selection.lastIndexOf('@');
			return separator < 0 ? selection
					: selection.substring(0, separator) + CONTEXTUAL_SEPARATOR + selection.substring(separator + 1);
		}).toArray(String[]::new);
		final var dialog = new PickExperimentDialog(labels);
		if (dialog.open() != IDialogConstants.OK_ID) return false;
		final int index = Arrays.asList(labels).indexOf(dialog.getSelectedExperiment());
		final String selected = selections[index];
		final int separator = selected.lastIndexOf('@');
		experiment = separator < 0 ? selected : selected.substring(0, separator);
		modelPath = separator < 0 ? GamaPreferences.Interface.CORE_DEFAULT_MODEL.getValue().getPath(null)
				: selected.substring(separator + 1);
		return true;
	}

	public void startSimulation() {
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
			model = compiled;
		} else {
			compiled.dispose();
		}
	}

	public boolean areThereAnyArtifactsToSave() throws IOException {
		return model != null && artifacts.hasChanges(Path.of(model.getProjectPath()));
	}

	public boolean saveSimulationArtifacts(final Path target, final IProgressMonitor monitor) throws IOException {
		if (model == null) throw new IOException("No simulation project is available");
		return artifacts.save(Path.of(model.getProjectPath()), target, monitor);
	}
}
