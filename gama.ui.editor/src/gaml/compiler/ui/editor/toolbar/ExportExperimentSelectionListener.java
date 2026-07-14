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
import gama.api.types.file.GenericFile;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.GamlProperties;
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

		final URI modelURI = editor.getURI();

		final String relativeModelPathStr = modelURI.toPlatformString(true);

		final String workspacePathStr =  GAMA.getWorkspaceManager().getWorkspaceLocation();

		final Path modelPath = Path.of(workspacePathStr,relativeModelPathStr);
		
		final GenericFile modelFile = new GenericFile(modelPath.toString());

		final GamlProperties metaProperties = new GamlProperties();


		try {
			final IModelSpecies model = GamlModelBuilder.getInstance().compile(modelFile.getFile(null),null,metaProperties);
			
			Set<String> plugins = metaProperties.get(GamlProperties.PLUGINS); 

			final String[] experimentNames = StreamSupport.stream(model.getExperiments().spliterator(),false)
				.map(experiment -> experiment.getDescription().getName())
				.toArray(String[]::new);
				
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

				final GamaZipBuilder ziper = new GamaZipBuilder(
					plugins,
					workspacePathStr,
					modelPath.toString(),
					targetExperiments);
				try { 
					ziper.zip(outputPath.toString());
				} catch (Exception exception) {
					System.err.println("Exception raised while cloning GAMA :\n" + exception);
					exception.printStackTrace();
				}
        }
				} catch (Throwable t) {
            t.printStackTrace();
        }


	}

}
