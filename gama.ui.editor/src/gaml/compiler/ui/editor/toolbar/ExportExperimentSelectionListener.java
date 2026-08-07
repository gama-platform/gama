package gaml.compiler.ui.editor.toolbar;

import java.nio.file.Path;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.StreamSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

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
import gaml.compiler.resource.GamlFileInfo;

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

		final IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();

		final String relativeModelPathStr = file.getFullPath().toOSString();

		final GamlProperties metaProperties = new GamlProperties();


		try {
			final IModelSpecies model = GamlModelBuilder.getInstance().compile(file.getLocation().toFile(),null,metaProperties);
			GamlFileInfo fileInfo = new GamlFileInfo(file);
			final Path modelFileParent = Path.of(file.getLocation().toOSString()).getParent();
			final Set<String> dataFiles = new HashSet<>();
			for (final String use : fileInfo.getUses()) {
				dataFiles.add(modelFileParent.resolve(use).normalize().toString());
			}

			Set<String> plugins = metaProperties.get(GamlProperties.PLUGINS);

			final String[] experimentNames = StreamSupport.stream(model.getExperiments().spliterator(),false)
				.map(experiment -> experiment.getDescription().getName())
				.toArray(String[]::new);
				
			final ExportModelDialog dialog = new ExportModelDialog(experimentNames);

			final int result = dialog.open();
        
			if (result != IDialogConstants.OK_ID)
				return;
				
			// Récupération des données via les getters de la classe
			
			final String targetExperiments = String.join("#",dialog.getSelectedExperiments());

			// final String formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HHmmss"));
			// final String modelName = Path.of(relativeModelPathStr).getFileName().toString().replace(".gaml","");
			// final String outputFileName = modelName  + "-" + targetExperiments + "-" + formattedTimestamp + ".zip";

			final Path outputPath = Path.of(dialog.getOutputPath(),dialog.getOutputFileName());

			final GamaZipBuilder ziper = new GamaZipBuilder(
				plugins,
				file.getProject().getLocation().toOSString(),
				relativeModelPathStr,
				targetExperiments,
				dataFiles);
			try { 
				ziper.zip(outputPath.toString());
				System.out.println("Model exported successfully");
			} catch (Exception exception) {
				System.err.println("Exception raised while cloning GAMA :\n" + exception);
				exception.printStackTrace();
			}

		} catch (Throwable t) {
            t.printStackTrace();
        }


	}

}
