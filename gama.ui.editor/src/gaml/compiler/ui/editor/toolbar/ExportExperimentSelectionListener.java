package gaml.compiler.ui.editor.toolbar;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.StreamSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.IDialogConstants;

import gama.api.GAMA;
import gaml.compiler.validation.GamlModelBuilder;
import gama.api.kernel.species.IModelSpecies;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.utils.prefs.GamaPreferences;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.ui.editor.GamlEditor;
import gaml.compiler.ui.editor.GamlEditorState;
import gama.export.GamaZipBuilder;
import gama.export.ui.ExportModelDialog;

public class ExportExperimentSelectionListener implements Selector {

	/** The editor. */
	GamlEditor editor;

	/** The state. */
	GamlEditorState state;

	/**
	 *
	 */
	public ExportExperimentSelectionListener(final GamlEditor editor, final GamlEditorState state) {
		this.editor = editor;
		this.state = state;
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(final SelectionEvent e) {

		// final IGui gui = GAMA.getRegularGui();
		// We refuse to run if there is no XtextGui available.
		editor.doSave(null);
		if (GamaPreferences.Modeling.EDITOR_SAVE.getValue()) {
			WorkbenchHelper.getPage().saveAllEditors(GamaPreferences.Modeling.EDITOR_SAVE_ASK.getValue());
		}

		Set<String> neededModules = Set.of(
            "gama.extension.bdi",
            "gama.extension.database",
            "gama.extension.fipa",
            "gama.extension.pedestrian",
            "gama.extension.physics",
            "gama.extension.traffic",
            "gama.library",
            "gama.ui.display.opengl",
            "gama.ui.display.opengl4"
        );

		final URI modelURI = editor.getURI();

		final String relativeModelPathStr = modelURI.toPlatformString(true);

		final String workspacePathStr =  GAMA.getWorkspaceManager().getWorkspaceLocation();

		final Path modelPath = Path.of(workspacePathStr,relativeModelPathStr);
		
		final IModelSpecies model = GamlModelBuilder.getInstance().compile(modelURI,null);

		final String[] experimentNames = StreamSupport.stream(model.getExperiments().spliterator(),false)
			.map(experiment -> experiment.getDescription().getName())
			.toArray(String[]::new);

		try {
			final ExportModelDialog dialog = new ExportModelDialog(experimentNames);

			final int result = dialog.open();
        
			if (result == IDialogConstants.OK_ID) {
				
				// Récupération des données via les getters de la classe
				final String outputPathStr = dialog.getOutputPath();
				
				final String targetExperiments = String.join("#",dialog.getSelectedExperiments());

				final String formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HHmmss"));

				final String modelName = Path.of(relativeModelPathStr).getFileName().toString().replace(".gaml","");

				final String outputFilename = modelName  + "-" + targetExperiments + "-" + formattedTimestamp + ".zip";

				final Path outputPath = Path.of(outputPathStr,outputFilename);

				final GamaZipBuilder ziper = new GamaZipBuilder(neededModules,
					workspacePathStr,
					modelPath.toString(),
					targetExperiments);
				try { 
					ziper.zip(outputPath.toString());
				} catch (Exception exception) {
					System.err.println("Exception raised while cloning GAMA :\n" + exception);
				}
        }
				} catch (Throwable t) {
            t.printStackTrace();
        }


	}

}
