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

import static java.util.Map.entry;

import java.util.Map;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.ResourceUtility;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
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

	/**
	 * The Class GamaResourceUtilities.
	 */
	public static final class GamaResourceUtilities extends ResourceUtility {

		/**
		 * The dynamic substitions. Not used for the moment. Kept for reference in order to implement substitutions with
		 * GamaIcons if needed. The current implementation lists only a few of them. The complete list is :
		 *
		 * -- for org.eclipse.ui.ide :
		 *
		 * ./full/obj16/quickfix_info_obj.svg ./full/obj16/quickfix_warning_obj.svg ./full/obj16/lprio_tsk.svg
		 * ./full/obj16/includeMode_filter.svg ./full/obj16/fileType_filter.svg ./full/obj16/error_tsk.svg
		 * ./full/obj16/welcome_item.svg ./full/obj16/keyspref_obj.svg ./full/obj16/workset.svg
		 * ./full/obj16/exportpref_obj.svg ./full/obj16/header_complete.svg ./full/obj16/header_priority.svg
		 * ./full/obj16/inheritable_filter.svg ./full/obj16/warning.svg ./full/obj16/warn_tsk.svg
		 * ./full/obj16/folderType_filter.svg ./full/obj16/incomplete_tsk.svg ./full/obj16/fileFolderType_filter.svg
		 * ./full/obj16/welcome_banner.svg ./full/obj16/importpref_obj.svg ./full/obj16/hprio_tsk.svg
		 * ./full/obj16/bkmrk_tsk.svg ./full/obj16/quickfix_error_obj.svg ./full/obj16/goto_input.svg
		 * ./full/obj16/taskmrk_tsk.svg ./full/obj16/prj_obj.svg ./full/obj16/welcome_editor.svg
		 * ./full/obj16/cprj_obj.svg ./full/obj16/info_tsk.svg ./full/obj16/folder.svg ./full/obj16/complete_tsk.svg
		 * ./full/obj16/excludeMode_filter.svg ./full/markers/contassist_ovr.svg ./full/markers/help_small.svg
		 * ./full/wizban/importzip_wiz.svg ./full/wizban/newprj_wiz.svg ./full/wizban/exportdir_wiz.svg
		 * ./full/wizban/newgroup_wiz.svg ./full/wizban/saveas_wiz.svg ./full/wizban/importdir_wiz.svg
		 * ./full/wizban/importproj_wiz.svg ./full/wizban/new_wiz.svg ./full/wizban/exportzip_wiz.svg
		 * ./full/wizban/newfile_wiz.svg ./full/wizban/quick_fix.svg ./full/wizban/workset_wiz.svg
		 * ./full/wizban/newfolder_wiz.svg ./full/etool16/importzip_wiz.svg ./full/etool16/newprj_wiz.svg
		 * ./full/etool16/exportdir_wiz.svg ./full/etool16/newgroup_wiz.svg ./full/etool16/importdir_wiz.svg
		 * ./full/etool16/exportzip_wiz.svg ./full/etool16/newfile_wiz.svg ./full/etool16/prev_nav.svg
		 * ./full/etool16/newfolder_wiz.svg ./full/etool16/problem_category.svg ./full/etool16/export_wiz.svg
		 * ./full/etool16/import_wiz.svg ./full/etool16/next_nav.svg ./full/etool16/build_exec.svg
		 * ./full/etool16/search_src.svg ./full/ovr16/symlink_ovr.svg ./full/ovr16/filterapplied_ovr.svg
		 * ./full/ovr16/link_ovr.svg ./full/ovr16/linkwarn_ovr.svg ./full/ovr16/virt_ovr.svg
		 * ./full/eview16/problems_view_info.svg ./full/eview16/bkmrk_nav.svg ./full/eview16/problems_view_error.svg
		 * ./full/eview16/tasks_tsk.svg ./full/eview16/problems_view.svg ./full/eview16/filenav_nav.svg
		 * ./full/eview16/problems_view_warning.svg ./full/eview16/pview.svg ./full/elcl16/selected_mode.svg
		 * ./full/elcl16/cpyqual_menu.svg ./full/elcl16/workingsets.svg ./full/elcl16/addtsk_tsk.svg
		 * ./full/elcl16/addtoworkset.svg ./full/elcl16/configs.svg ./full/elcl16/showtsk_tsk.svg
		 * ./full/elcl16/collapseall.svg ./full/elcl16/flatLayout.svg ./full/elcl16/showerr_tsk.svg
		 * ./full/elcl16/usearch_obj.svg ./full/elcl16/refresh_nav.svg ./full/elcl16/gotoobj_tsk.svg
		 * ./full/elcl16/filter_ps.svg ./full/elcl16/showcomplete_tsk.svg ./full/elcl16/smartmode_co.svg
		 * ./full/elcl16/synced.svg ./full/elcl16/hierarchicalLayout.svg ./full/elcl16/showchild_mode.svg
		 * ./full/elcl16/step_current.svg ./full/elcl16/step_done.svg ./full/elcl16/removefromworkset.svg
		 * ./full/elcl16/showwarn_tsk.svg
		 *
		 * -- for org.eclipse.ui.console:
		 *
		 * ./full/cview16/console_view.svg ./full/eview16/console_view.svg ./full/elcl16/lock_co.svg
		 * ./full/elcl16/wordwrap.svg ./full/elcl16/pin.svg ./full/elcl16/rem_co.svg ./full/elcl16/new_con.svg
		 * ./full/elcl16/clear_co.svg
		 *
		 *
		 * -- for org.eclipse.ui.views.log:
		 *
		 * -- for org.eclipse.ui.workbench.texteditor:
		 *
		 * -- for org.eclipse.search:
		 *
		 * -- for org.eclipse.ui.browser:
		 *
		 * -- for org.eclipse.e4.ui.workbench.renderers.swt:
		 *
		 * -- for org.eclipse.ui.views:
		 *
		 * -- for org.eclipse.ui:
		 */
		final static Map<String, String> DYNAMIC_SUBSTITIONS = Map.ofEntries(entry(
				"platform:/plugin/org.eclipse.ui.workbench.texteditor/$nl$/icons/full/etool16/block_selection_mode.svg",
				""), entry("platform:/plugin/org.eclipse.search/icons/full/eview16/searchres.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.ide/icons/full/eview16/bkmrk_nav.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.console/icons/full/cview16/console_view.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.views.log/icons/eview16/error_log.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.browser/icons/obj16/internal_browser.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.workbench.texteditor/icons/full/eview16/minimap.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.views/icons/full/eview16/outline_co.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.ide/icons/full/eview16/problems_view.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.ide/icons/full/eview16/pview.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.views/icons/full/eview16/prop_ps.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.ide/icons/full/eview16/tasks_tsk.svg", ""),
				entry("platform:/plugin/org.eclipse.ui/icons/full/eview16/defaultview_misc.svg", ""),
				entry("platform:/plugin/org.eclipse.ui.ide/$nl$/icons/full/etool16/importdir_wiz.svg", ""),
				entry("platform:/plugin/org.eclipse.e4.ui.workbench.renderers.swt/icons/full/elcl16/view_menu.svg", ""),
				entry("platform:/plugin/org.eclipse.e4.ui.workbench.renderers.swt/icons/full/ovr16/pinned_ovr.svg",
						""));

		/** The light prefix uri. */
		static final URI LIGHT_SEGMENT = URI.createPlatformPluginURI("gama.ui.shared/icons_svg/light/", true);

		/** The dark prefix uri. */
		static final URI DARK_SEGMENT = URI.createPlatformPluginURI("gama.ui.shared/icons_svg/dark/", true);

		static {
			// DEBUG.OUT(LIGHT_SEGMENT + " <-> " + DARK_SEGMENT);
		}

		@Override
		public ImageDescriptor imageDescriptorFromURI(final URI path) {
			// DEBUG.OUT("Requesting image at " + path);
			if (isPrefix(LIGHT_SEGMENT, path)) {
				String pathToIcon = path.deresolve(LIGHT_SEGMENT).toString().replace(".svg", "");
				boolean isDisabled = pathToIcon.endsWith("_disabled");
				if (isDisabled) { pathToIcon = pathToIcon.substring(0, pathToIcon.length() - "_disabled".length()); }
				// DEBUG.OUT("Resolved icon id: " + pathToIcon);
				GamaIcon icon = GamaIcon.named(pathToIcon);
				if (icon == null) {
					if (ThemeHelper.isDark())
						return super.imageDescriptorFromURI(path.replacePrefix(LIGHT_SEGMENT, DARK_SEGMENT));
					return super.imageDescriptorFromURI(path);
				}
				// DEBUG.OUT("Found Gama icon for " + pathToIcon + ", isDisabled=" + isDisabled);
				if (isDisabled) return icon.disabledDescriptor();
				return icon.descriptor();
			}
			return super.imageDescriptorFromURI(path);

		}
	}

	/**
	 * Checks if is prefix.
	 *
	 * @param prefix
	 *            the prefix
	 * @param uri
	 *            the uri
	 * @return true, if is prefix
	 */
	public static boolean isPrefix(final URI prefix, final URI uri) {
		// Ensure prefix is treated as a directory if it doesn't have a trailing separator
		URI folderPrefix = prefix.hasTrailingPathSeparator() ? prefix : prefix.appendSegment("");
		URI relative = uri.deresolve(folderPrefix);
		return relative.isRelative() && !relative.toString().startsWith("..");
	}

	/** The Constant RES_UTIL. */
	public static final IResourceUtilities RES_UTIL = new GamaResourceUtilities();

	@Override
	public void start(final BundleContext c) throws Exception {
		super.start(c);
		GamaIcon.preloadAllIcons();
		final MApplication a = PlatformUI.getWorkbench().getService(MApplication.class);
		EclipseContext context = (EclipseContext) a.getContext();
		if (context != null) {

			context.runAndTrack(new RunAndTrack() {

				@Override
				public boolean changed(final IEclipseContext eContext) {
					IResourceUtilities utils = eContext.get(IResourceUtilities.class);
					// If there are no utilites present, of if it is already
					// substituted, do nothing
					if (utils == null || utils == RES_UTIL) return true;
					// Otherwise, inject the resource utilities that will
					// forward all calls to the original, but change
					// the URI to dark ones when necessary
					eContext.set(IResourceUtilities.class, RES_UTIL);
					return true;
				}
			});
		}
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
		replace(ISharedImages.IMG_TOOL_UNDO_DISABLED, IGamaIcons.MENU_UNDO, true);
		replace(ISharedImages.IMG_TOOL_REDO, IGamaIcons.MENU_REDO);
		replace(ISharedImages.IMG_TOOL_REDO_DISABLED, IGamaIcons.MENU_REDO, true);
		replace(ISharedImages.IMG_TOOL_BACK, "command.lastedit");
		replace(ISharedImages.IMG_TOOL_BACK_DISABLED, IGamaIcons.MENU_BACK, true);
		replace(ISharedImages.IMG_TOOL_FORWARD, "command.nextedit");
		replace(ISharedImages.IMG_TOOL_FORWARD_DISABLED, "command.nextedit", true);
		replace(ISharedImages.IMG_OBJ_FILE, IGamaIcons.FILE_TEXT);
		replace(ISharedImages.IMG_OBJ_FOLDER, IGamaIcons.FOLDER_MODEL);
		replace(ISharedImages.IMG_LCL_LINKTO_HELP, IGamaIcons.MENU_HELP);
		replace(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_CONTENTS, IGamaIcons.MENU_HELP);
		replace(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_SEARCH, IGamaIcons.MENU_HELP);
		replace(Dialog.DLG_IMG_HELP, IGamaIcons.MENU_HELP);
		replace(IDE.SharedImages.IMG_OBJ_PROJECT, IGamaIcons.FOLDER_PROJECT);
		replace(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, IGamaIcons.CLOSED_PROJECT);
		replace(ISharedImages.IMG_TOOL_CUT, IGamaIcons.CUT);
		replace(ISharedImages.IMG_TOOL_COPY, IGamaIcons.COPY);
		replace(ISharedImages.IMG_TOOL_PASTE, IGamaIcons.PASTE);
		replace(ISharedImages.IMG_TOOL_DELETE, IGamaIcons.DELETE);
		replace(ISharedImages.IMG_DEC_FIELD_ERROR, IGamaIcons.OVERLAY_ERROR);
		replace(ISharedImages.IMG_DEC_FIELD_WARNING, IGamaIcons.OVERLAY_WARNING);
		replace(ISharedImages.IMG_TOOL_COPY_DISABLED, IGamaIcons.COPY, true);
		replace(ISharedImages.IMG_TOOL_CUT_DISABLED, IGamaIcons.CUT, true);
		replace(ISharedImages.IMG_TOOL_PASTE_DISABLED, IGamaIcons.PASTE, true);
		replace(ISharedImages.IMG_TOOL_DELETE_DISABLED, IGamaIcons.DELETE, true);

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
		replace(id, gamlImage, false);
	}

	/**
	 * Replace.
	 *
	 * @param id
	 *            the id
	 * @param gamlImage
	 *            the gaml image
	 * @param disabled
	 *            the disabled
	 */
	void replace(final String id, final String gamlImage, final boolean disabled) {
		JFaceResources.getImageRegistry().remove(id);
		if (!disabled) {
			JFaceResources.getImageRegistry().put(id, GamaIcon.named(gamlImage).image());
			WorkbenchImages.declareImage(id, GamaIcon.named(gamlImage).descriptor(), true);
		}
		JFaceResources.getImageRegistry().put(id, GamaIcon.named(gamlImage).disabled());
		WorkbenchImages.declareImage(id, GamaIcon.named(gamlImage).disabledDescriptor(), true);
	}

}
