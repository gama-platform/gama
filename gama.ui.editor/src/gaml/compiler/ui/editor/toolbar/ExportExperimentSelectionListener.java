package gaml.compiler.ui.editor.toolbar;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.core.resources.IFile;

import gama.api.utils.prefs.GamaPreferences;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.ui.editor.GamlEditor;
import gaml.compiler.ui.editor.GamlEditorState;
import gama.ui.navigator.commands.ExportProjectAsSimulation;

public class ExportExperimentSelectionListener implements Selector {

	/** The editor. */
	GamlEditor editor;

	/** The state. */
	GamlEditorState state;

	private boolean exportIsDone = false;

	/**
	 *
	 */
	public ExportExperimentSelectionListener(final GamlEditor editor, final GamlEditorState state) {
		this.editor = editor;
		this.state = state;
	}

	private synchronized boolean getExportIsDone() {
		return exportIsDone;
	}

	private synchronized void setExportIsDoneToTrue() {
		exportIsDone = true;
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

		List<IFile> modelFiles = new LinkedList<IFile>();
		modelFiles.add(file);

		ExportProjectAsSimulation exporter = new ExportProjectAsSimulation();
		exporter.export(modelFiles);
	}

}
