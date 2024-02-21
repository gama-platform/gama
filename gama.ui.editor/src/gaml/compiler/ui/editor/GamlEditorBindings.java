/*******************************************************************************************************
 *
 * GamlEditorBindings.java, in gama.ui.shared.modeling, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

import static gama.core.common.interfaces.IGui.NAVIGATOR_VIEW_ID;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchPage;

import gama.ui.navigator.view.GamaNavigator;
import gama.ui.navigator.view.GamaNavigatorNewMenu;
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.bindings.GamaKeyBindings.PluggableBinding;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.IGamlEditor;

/**
 * The class GamlEditorBindings.
 *
 * @author drogoul
 * @since 10 nov. 2016
 *
 */
public class GamlEditorBindings {

	/** The Constant MODIFIERS. */
	public static final int MODIFIERS = SWT.SHIFT + SWT.ALT;

	/**
	 * Install.
	 */
	public static void install() {

		GamaKeyBindings.plug(new PluggableBinding(SWT.MOD1, 'g') {

			@Override
			public void run() {
				final IGamlEditor editor = WorkbenchHelper.getActiveEditor();
				if (!(editor instanceof GamlEditor)) return;
				((GamlEditor) editor).doSearch();
			}
		});

		GamaKeyBindings.plug(new PluggableBinding(SWT.ALT + GamaKeyBindings.COMMAND, 'n') {

			GamaNavigator navigator;

			private GamaNavigator getNavigator() {
				if (navigator == null) {
					final IWorkbenchPage page = WorkbenchHelper.getPage();
					if (page != null) { navigator = (GamaNavigator) page.findView(NAVIGATOR_VIEW_ID); }
				}
				return navigator;
			}

			@Override
			public void run() {
				final Point p = WorkbenchHelper.getDisplay().getCursorLocation();
				IStructuredSelection s = getNavigator().getSelection();
				GamaNavigatorNewMenu menu = new GamaNavigatorNewMenu(s);
				menu.open(p);
				// final Control c = WorkbenchHelper.getDisplay().getCursorControl();
			}
		});

		// for (int i = 0; i < 9; i++) {
		// GamaKeyBindings.plug(newBinding(i));
		// }
	}

	// private static PluggableBinding newExperimentBinding(final int index) {
	// return new PluggableBinding('0' + index, MODIFIERS) {
	//
	// @Override
	// public void run() {
	//
	// final IEditorPart editor = WorkbenchHelper.getActiveEditor();
	// if (!(editor instanceof GamlEditor)) { return; }
	// ((GamlEditor) editor).runExperiment(index);
	//
	// }
	// };
	// }

}
