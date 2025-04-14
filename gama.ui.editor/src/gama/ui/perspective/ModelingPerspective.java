/*******************************************************************************************************
 *
 * ModelingPerspective.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The Class ModelingPerspective.
 */
public class ModelingPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		layout.setEditorAreaVisible(true);
		// final String editor = layout.getEditorArea();
		// layout.addShowViewShortcut(IGui.INTERACTIVE_CONSOLE_VIEW_ID);
		// layout.addShowViewShortcut(IGui.ERROR_VIEW_ID);
		// layout.addShowViewShortcut(IGui.MINIMAP_VIEW_ID);

		// final IFolderLayout navigAndOthers = layout.createFolder("navigAndOthers", IPageLayout.LEFT, 0.3f, editor);
		// navigAndOthers.addView(IGui.NAVIGATOR_VIEW_ID);
		// navigAndOthers.
		// navigAndOthers.addPlaceholder(IGui.ERROR_VIEW_ID);
		// navigAndOthers.addPlaceholder(IGui.TEST_VIEW_ID);

		// final IFolderLayout consoleFolder =
		// layout.createFolder("consoles", IPageLayout.BOTTOM, 0.70f, "navigAndOthers");

		// consoleFolder.addPlaceholder(IGui.INTERACTIVE_CONSOLE_VIEW_ID);
		// consoleFolder.addPlaceholder(IGui.TEST_VIEW_ID);

		// final IFolderLayout outlineFolder = layout.createFolder("outline", IPageLayout.RIGHT, 0.2f, editor);

		// outlineFolder.addPlaceholder(IGui.OUTLINE_VIEW_ID);

	}
}
