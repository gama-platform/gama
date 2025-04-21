/*******************************************************************************************************
 *
 * Activator.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.SwtGui;

/**
 * The Class Activator.
 */
public class Activator extends AbstractUIPlugin {

	static {
		DEBUG.OFF();
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		GamaIcon.preloadAllIcons();
		GAMA.setRegularGui(new SwtGui());

		try {
			FieldDecorationRegistry.setDefault(new GamlFieldDecorationRegistry());
			replaceImages();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Replace images.
	 */
	void replaceImages() {
		replace(Dialog.DLG_IMG_ERROR, IGamaIcons.MARKER_ERROR);
		replace(Dialog.DLG_IMG_WARNING, IGamaIcons.MARKER_WARNING);
		replace(Dialog.DLG_IMG_INFO, IGamaIcons.MARKER_INFO);
		replace(Dialog.DLG_IMG_MESSAGE_ERROR, IGamaIcons.MARKER_ERROR);
		replace(Dialog.DLG_IMG_MESSAGE_WARNING, IGamaIcons.MARKER_WARNING);
		replace(Dialog.DLG_IMG_MESSAGE_INFO, IGamaIcons.MARKER_INFO);
		replace("org.eclipse.jface.fieldassist.IMG_DEC_FIELD_INFO", IGamaIcons.MARKER_INFO);
		replace("org.eclipse.jface.fieldassist.IMG_DEC_FIELD_ERROR", IGamaIcons.MARKER_ERROR);
		replace("org.eclipse.jface.fieldassist.IMG_DEC_FIELD_ERROR_QUICKFIX", IGamaIcons.MARKER_ERROR);
		replace("org.eclipse.jface.fieldassist.IMG_DEC_FIELD_WARNING", IGamaIcons.MARKER_WARNING);
		replace(FieldDecorationRegistry.DEC_ERROR, IGamaIcons.MARKER_ERROR);
		replace(FieldDecorationRegistry.DEC_WARNING, IGamaIcons.MARKER_WARNING);
		replace(FieldDecorationRegistry.DEC_INFORMATION, IGamaIcons.MARKER_INFO);
		replace(IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH, IGamaIcons.MARKER_ERROR);
		replace(IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH, IGamaIcons.MARKER_WARNING);
		replace(IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH, IGamaIcons.MARKER_INFO);
		replace(IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_ERROR, IGamaIcons.MARKER_ERROR);
		replace(IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_WARNING, IGamaIcons.MARKER_WARNING);
		replace(IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_INFO, IGamaIcons.MARKER_INFO);
		replace(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_ERROR, IGamaIcons.MARKER_ERROR);
		replace(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_WARNING, IGamaIcons.MARKER_WARNING);
		replace(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_INFO, IGamaIcons.MARKER_INFO);
		replace(ISharedImages.IMG_OBJS_ERROR_TSK, IGamaIcons.MARKER_ERROR);
		replace(ISharedImages.IMG_OBJS_WARN_TSK, IGamaIcons.MARKER_WARNING);
		replace(ISharedImages.IMG_OBJS_INFO_TSK, IGamaIcons.MARKER_INFO);
		replace(ISharedImages.IMG_TOOL_UNDO, IGamaIcons.MENU_UNDO);
		replace(ISharedImages.IMG_TOOL_REDO, IGamaIcons.MENU_REDO);
		replace(ISharedImages.IMG_OBJ_FILE, IGamaIcons.FILE_TEXT);
		replace(ISharedImages.IMG_OBJ_FOLDER, IGamaIcons.FOLDER_MODEL);
		replace(ISharedImages.IMG_LCL_LINKTO_HELP, IGamaIcons.MENU_HELP);
		replace(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_CONTENTS, IGamaIcons.MENU_HELP);
		replace(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_SEARCH, IGamaIcons.MENU_HELP);
		replace(Dialog.DLG_IMG_HELP, IGamaIcons.MENU_HELP);
		replace(IDE.SharedImages.IMG_OBJ_PROJECT, IGamaIcons.FOLDER_PROJECT);
		replace(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, IGamaIcons.CLOSED_PROJECT);
		// replace(, IGamaIcons.CLOSED_PROJECT);
		replace(ISharedImages.IMG_TOOL_CUT, IGamaIcons.CUT);
		replace(ISharedImages.IMG_TOOL_COPY, IGamaIcons.COPY);
		replace(ISharedImages.IMG_TOOL_PASTE, IGamaIcons.PASTE);
		replace(ISharedImages.IMG_TOOL_DELETE, IGamaIcons.DELETE);
	}

	/**
	 * Replace.
	 *
	 * @param id
	 *            the id
	 * @param gamlImage
	 *            the gaml image
	 */
	void replace(final String id, final String gamlImage) {
		JFaceResources.getImageRegistry().remove(id);
		JFaceResources.getImageRegistry().put(id, GamaIcon.named(gamlImage).image());
		WorkbenchImages.declareImage(id, GamaIcon.named(gamlImage).descriptor(), true);
	}

}
