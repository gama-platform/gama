/*******************************************************************************************************
 *
 * OpenExperimentSelectionListener.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.toolbar;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.events.SelectionEvent;

import gama.api.GAMA;
import gama.api.compilation.IModelsManager;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.utils.prefs.GamaPreferences;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.ui.editor.GamlEditor;
import gaml.compiler.ui.editor.GamlEditorState;

/**
 * The class CreateExperimentSelectionListener.
 *
 * @author drogoul
 * @since 27 août 2016
 *
 */
public class OpenExperimentSelectionListener implements Selector {

	/** The editor. */
	GamlEditor editor;

	/** The state. */
	GamlEditorState state;

	/** The modelsManager. */
	final IModelsManager modelsManager;

	/**
	 *
	 */
	public OpenExperimentSelectionListener(final GamlEditor editor, final GamlEditorState state,
			final IModelsManager runner) {
		this.editor = editor;
		this.state = state;
		this.modelsManager = runner;
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
		String name = (String) e.widget.getData("exp");
		final int i = state.abbreviations.indexOf(name);
		if (i == -1) return;
		name = state.experiments.get(i);
		modelsManager.runModel(editor.getDocument(), name);

	}

	/**
	 * Goto editor.
	 *
	 * @param exception
	 *            the exception
	 */
	void gotoEditor(final GamaRuntimeException exception) {
		final EObject o = exception.getEditorContext();
		if (o != null) { WorkbenchHelper.asyncRun(() -> GAMA.getGui().getModelsManager().editModel(o)); }

	}

}
