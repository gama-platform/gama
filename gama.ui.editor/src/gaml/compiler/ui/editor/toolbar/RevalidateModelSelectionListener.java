/*******************************************************************************************************
 *
 * RevalidateModelSelectionListener.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.toolbar;

import org.eclipse.swt.events.SelectionEvent;

import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.ui.editor.GamlEditor;

/**
 * The class CreateExperimentSelectionListener.
 *
 * @author drogoul
 * @since 27 août 2016
 *
 */
public class RevalidateModelSelectionListener implements Selector {

	/** The editor. */
	GamlEditor editor;

	/**
	 *
	 */
	public RevalidateModelSelectionListener(final GamlEditor editor) {
		this.editor = editor;
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(final SelectionEvent e) {
		revalidate();
	}

	/** Recompile the text editor's resource when its error status is clicked. */
	public void revalidate() {
		editor.validateNow();
	}

}
