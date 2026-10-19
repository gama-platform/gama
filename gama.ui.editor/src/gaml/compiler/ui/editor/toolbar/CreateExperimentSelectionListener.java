/*******************************************************************************************************
 *
 * CreateExperimentSelectionListener.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.toolbar;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.ui.editor.GamlEditor;
import gaml.compiler.ui.reference.BuiltinReferenceMenu;
import gaml.compiler.ui.reference.ColorReferenceMenu;
import gaml.compiler.ui.reference.OperatorsReferenceMenu;
import gaml.compiler.ui.reference.TemplateReferenceMenu;

/**
 * The class CreateExperimentSelectionListener.
 *
 * @author drogoul
 * @since 27 août 2016
 *
 */

@SuppressWarnings ("deprecation")
public class CreateExperimentSelectionListener implements Selector {

	/** The editor. */
	GamlEditor editor;

	/** The control. */
	Control control;

	/**
	 *
	 */
	public CreateExperimentSelectionListener(final GamlEditor editor, final Control toolbar) {
		this.editor = editor;
		this.control = toolbar;
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(final SelectionEvent e) {

		final GamaMenu menu = new GamaMenu() {

			@Override
			protected void fillMenu() {
				final String[] paths = { "Experiment.Gui.1", "Experiment.Batch.2", "Experiment.Batch.1" };
				for (final String path : paths) {
					final Template t = editor.getTemplateStore().getTemplateData(path).getTemplate();
					action(t.getDescription(), e1 -> editor.applyTemplateAtTheEnd(t));
				}
				action("Add experiment...", e1 -> editor.openExperimentWizard(true)).setEnabled(editor.isEditable());
				action("Add display...", e1 -> editor.openDisplayWizard(true)).setEnabled(editor.canAddDisplay());
				sep();
				action("Add species...", e1 -> editor.openSpeciesWizard(true)).setEnabled(editor.isEditable());
				action("Add grid species...", e1 -> editor.openGridWizard()).setEnabled(editor.isEditable());
				action("Add class...", e1 -> editor.openClassWizard()).setEnabled(editor.isEditable());
				sep();
				action("Add action...", e1 -> editor.openActionWizard(true)).setEnabled(editor.isEditable());
				action("Add reflex...", e1 -> editor.openBehaviorWizard(true)).setEnabled(editor.isEditable());
				action("Extract selection as action...", e1 -> editor.extractAction()).setEnabled(editor.canExtractAction());
				sep();
				final Menu builtInElements = sub("Insert built-in elements...");
				new TemplateReferenceMenu().installSubMenuIn(builtInElements);
				new BuiltinReferenceMenu().installSubMenuIn(builtInElements);
				new OperatorsReferenceMenu().installSubMenuIn(builtInElements);
				new ColorReferenceMenu().installSubMenuIn(builtInElements);
			}

		};
		menu.open(control, e, 32);

	}

}
