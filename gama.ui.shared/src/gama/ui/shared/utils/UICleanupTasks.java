/*******************************************************************************************************
 *
 * UICleanupTasks.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.utils;

import static java.util.Map.entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.ResourceUtility;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ActionSetContributionItem;
import org.eclipse.ui.internal.CoolBarToTrimManager;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

import gama.api.GAMA;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.shared.GamlFieldDecorationRegistry;

/**
 * The Class UICleanupTasks. This class contains various static methods to clean up and customize the Eclipse UI.
 */
public class UICleanupTasks {

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

	static {
		DEBUG.ON();
	}

	/**
	 * Run.
	 *
	 * @throws IOException
	 */
	public static void run() {
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

		WorkbenchHelper.runInUI("Configuring GAMA UI", 0, p -> {
			try {
				DEBUG.TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GUI, "Configuring GAMA UI", "completed in", () -> {
					Workbench workbench = (Workbench) WorkbenchHelper.getWorkbench();
					WorkbenchWindow window = (WorkbenchWindow) workbench.getActiveWorkbenchWindow();
					if (window == null) return;
					RemoveUnwantedWizards.run(workbench);
					RemoveUnwantedActionSets.run(window);
					RearrangeMenus.run(window);
					ForceMaximizeRestoration.run(window);
					LockToolbars.run(window);
					ImageReplacer.run();
					FieldDecorationRegistry.setDefault(new GamlFieldDecorationRegistry());
					GamaKeyBindings.install();
				});
			} catch (Throwable e) {
				DEBUG.ERR(e);
			}
		});
	}

	/**
	 * The Class LockToolbars.
	 */
	static class LockToolbars {

		/**
		 * Run.
		 */
		static void run(final WorkbenchWindow window) {
			MTrimmedWindow winModel = window.getService(MTrimmedWindow.class);
			EModelService modelService = window.getService(EModelService.class);

			ICoolBarManager coolBarManager = window.getCoolBarManager2();
			if (coolBarManager != null) {
				// lock is the opposite of the original value before toggle
				final List<MToolBar> children = modelService.findElements(winModel, null, MToolBar.class);
				for (MToolBar el : children) {
					// locks the toolbars
					if (!el.getTags().contains(IPresentationEngine.NO_MOVE)) {
						el.getTags().add(IPresentationEngine.NO_MOVE);
					}
					if (el.getTags().contains(IPresentationEngine.DRAGGABLE)) {
						el.getTags().remove(IPresentationEngine.DRAGGABLE);
					}
					// Force the rendering update
					el.setToBeRendered(false);
					el.setToBeRendered(true);
				}
				coolBarManager.setContextMenuManager(null);
			}

		}
	}

	/**
	 * The Class ForceMaximizeRestoration.
	 */
	static class ForceMaximizeRestoration {

		/**
		 * Run.
		 */
		public static void run(final WorkbenchWindow window) {
			final IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				page.addPartListener(new IPartListener2() {

					@Override
					public void partActivated(final IWorkbenchPartReference partRef) {
						final IViewReference[] refs = page.getViewReferences();
						final IEditorReference[] eds = page.getEditorReferences();
						for (final IViewReference ref : refs) {
							if (!partRef.equals(ref) && page.getPartState(ref) == IWorkbenchPage.STATE_MAXIMIZED) {
								page.toggleZoom(ref);
								break;
							}
						}
						for (final IEditorReference ref : eds) {
							if (!partRef.equals(ref) && page.getPartState(ref) == IWorkbenchPage.STATE_MAXIMIZED) {
								page.toggleZoom(ref);
								break;
							}
						}

					}
				});
			}

		}
	}

	/**
	 * The Class RemoveUnwantedActionSets.
	 */
	static class RemoveUnwantedActionSets extends PerspectiveAdapter /* implements IStartup */ {

		/** The toolbar action sets to remove. */
		String[] TOOLBAR_ACTION_SETS_TO_REMOVE =
				{ "org.eclipse", "gaml.compiler.Gaml", "org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo" };

		/** The menus to remove. */
		String[] MENUS_TO_REMOVE = { "org.eclipse.ui.run", "window", "navigate", "project" };

		/**
		 * Run.
		 */
		public static void run(final WorkbenchWindow window) {
			final RemoveUnwantedActionSets remove = new RemoveUnwantedActionSets();
			final IWorkbenchPage page = window.getActivePage();
			if (page != null) { remove.perspectiveActivated(page, null); }
			window.addPerspectiveListener(remove);

		}

		@Override
		public void perspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
			final WorkbenchWindow w = (WorkbenchWindow) page.getWorkbenchWindow();
			if (w.isClosing()) return;
			WorkbenchHelper.runInUI("Cleaning menus", 0, e -> {
				try {
					if (w.isClosing()) return;
					final CoolBarToTrimManager cm = (CoolBarToTrimManager) w.getCoolBarManager2();
					final IContributionItem[] items = cm.getItems();
					// We remove all contributions to the toolbar that do not
					// relate to gama
					for (final IContributionItem item : items) {
						for (final String s1 : TOOLBAR_ACTION_SETS_TO_REMOVE) {
							if (item.getId().contains(s1)) {
								try {
									if (w.getCoolBarManager2().find(item.getId()) != null) {
										w.getCoolBarManager2().remove(item);
									}
								} catch (final Exception e1) {}
							}
						}
					}
					for (final String s2 : MENUS_TO_REMOVE) {
						w.getMenuBarManager().remove(s2);
						w.getMenuManager().remove(s2);
					}
					w.getMenuManager().update(true);
					w.getMenuBarManager().update(true);
					cm.resetItemOrder();
				} catch (final Exception e1) {}
			});

		}

		/**
		 * Perspective changed.
		 *
		 * @param p
		 *            the p
		 * @param d
		 *            the d
		 * @param c
		 *            the c
		 */
		@Override
		public void perspectiveChanged(final IWorkbenchPage p, final IPerspectiveDescriptor d, final String c) {
			if (IWorkbenchPage.CHANGE_RESET_COMPLETE.equals(c)) { perspectiveActivated(p, d); }

		}

	}

	/**
	 * The Class RemoveUnwantedWizards.
	 */
	static class RemoveUnwantedWizards {

		/** The categories to remove. */
		private static Set<String> CATEGORIES_TO_REMOVE =
				new HashSet<>(Arrays.asList("org.eclipse.pde.PDE", "org.eclipse.emf.codegen.ecore.ui.wizardCategory"));

		/** The ids to remove. */
		private static Set<String> IDS_TO_REMOVE = new HashSet<>(
				Arrays.asList("org.eclipse.ui.wizards.new.project", "org.eclipse.equinox.p2.replication.import",
						"org.eclipse.equinox.p2.replication.importfrominstallation",
						"org.eclipse.team.ui.ProjectSetImportWizard", "org.eclipse.equinox.p2.replication.export",
						"org.eclipse.team.ui.ProjectSetExportWizard"));

		/**
		 * Run.
		 */
		static void run(final IWorkbench workbench) {
			final List<IWizardCategory> cats = new ArrayList<>();
			AbstractExtensionWizardRegistry r = (AbstractExtensionWizardRegistry) workbench.getNewWizardRegistry();
			cats.addAll(Arrays.asList(r.getRootCategory().getCategories()));
			r = (AbstractExtensionWizardRegistry) workbench.getImportWizardRegistry();
			cats.addAll(Arrays.asList(r.getRootCategory().getCategories()));
			r = (AbstractExtensionWizardRegistry) workbench.getExportWizardRegistry();
			cats.addAll(Arrays.asList(r.getRootCategory().getCategories()));
			for (final IWizardDescriptor wizard : getAllWizards(cats.toArray(new IWizardCategory[0]))) {
				final String catId = wizard.getCategory().getId();
				if (CATEGORIES_TO_REMOVE.contains(catId) || IDS_TO_REMOVE.contains(wizard.getId())) {
					final WorkbenchWizardElement element = (WorkbenchWizardElement) wizard;
					r.removeExtension(element.getConfigurationElement().getDeclaringExtension(),
							new Object[] { element });
				}
			}

		}

		/**
		 * Gets the all wizards.
		 *
		 * @param categories
		 *            the categories
		 * @return the all wizards
		 */
		static private IWizardDescriptor[] getAllWizards(final IWizardCategory[] categories) {
			final List<IWizardDescriptor> results = new ArrayList<>();
			for (final IWizardCategory wizardCategory : categories) {

				results.addAll(Arrays.asList(wizardCategory.getWizards()));
				results.addAll(Arrays.asList(getAllWizards(wizardCategory.getCategories())));
			}
			return results.toArray(new IWizardDescriptor[0]);
		}

	}

	/**
	 * The Class ImageReplacer.
	 */
	public static class ImageReplacer {

		/** The registry. */
		static ImageRegistry registry = JFaceResources.getImageRegistry();

		/**
		 * Replace images.
		 */
		static void run() {
			replace(Dialog.DLG_IMG_MESSAGE_ERROR, IGamaIcons.MARKER_ERROR);
			replace(Dialog.DLG_IMG_MESSAGE_WARNING, IGamaIcons.MARKER_WARNING);
			replace(Dialog.DLG_IMG_MESSAGE_INFO, IGamaIcons.MARKER_INFO);
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
		static void replace(final String id, final String gamlImage) {
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
		static void replace(final String id, final String gamlImage, final boolean disabled) {
			registry.remove(id);
			if (!disabled) {
				registry.put(id, GamaIcon.named(gamlImage).image());
				WorkbenchImages.declareImage(id, GamaIcon.named(gamlImage).descriptor(), true);
			} else {
				registry.put(id, GamaIcon.named(gamlImage).disabled());
				WorkbenchImages.declareImage(id, GamaIcon.named(gamlImage).disabledDescriptor(), true);
			}
		}
	}

	/**
	 * The Class RearrangeMenus.
	 */
	public static class RearrangeMenus {

		/** The Constant MENU_ITEMS_TO_REMOVE. */
		public final static Set<String> MENU_ITEMS_TO_REMOVE = new HashSet<>(Arrays.asList("openWorkspace",
				"helpSearch", "org.eclipse.search.OpenFileSearchPage", "textSearchSubMenu", "reopenEditors",
				"converstLineDelimitersTo", "org.eclipse.equinox.p2.ui.sdk.update",
				"org.eclipse.ui.edit.text.toggleBlockSelectionMode", "org.eclipse.ui.edit.text.toMultiSelection",
				"org.eclipse.equinox.p2.ui.sdk.install", "org.eclipse.equinox.p2.ui.sdk.installationDetails",
				"org.eclipse.e4.ui.importer.openDirectory.menu"));

		/** The Constant MENU_IMAGES. */
		public final static Map<String, String> MENU_IMAGES = new HashMap<>() {
			{
				put("print", IGamaIcons.MENU_PRINT);
				put("save", IGamaIcons.MENU_SAVE);
				put("saveAs", IGamaIcons.SAVE_AS);
				put("saveAll", IGamaIcons.MENU_SAVE_ALL);
				put("revert", IGamaIcons.MENU_REVERT);
				put("refresh", IGamaIcons.FILE_REFRESH);
				put("new", IGamaIcons.MENU_NEW);
				put("import", IGamaIcons.MENU_IMPORT);
				put("export", IGamaIcons.MENU_EXPORT);
				put("undo", IGamaIcons.MENU_UNDO);
				put("redo", IGamaIcons.MENU_REDO);
				put("cut", IGamaIcons.CUT);
				put("copy", IGamaIcons.COPY);
				put("paste", IGamaIcons.PASTE);
				put("delete", IGamaIcons.DELETE);
				put("helpContents", IGamaIcons.MENU_HELP);
				put("org.eclipse.search.OpenSearchDialog", IGamaIcons.MENU_SEARCH);
				put("org.eclipse.ui.openLocalFile", IGamaIcons.MENU_OPEN);
				put("converstLineDelimitersTo", IGamaIcons.MENU_DELIMITER);
			}
		};

		/**
		 * Run.
		 */
		public static void run(final WorkbenchWindow window) {
			final IMenuManager menuManager = window.getMenuManager();
			for (final IContributionItem item : menuManager.getItems()) {
				IMenuManager menu = null;
				if (item instanceof MenuManager) {
					menu = (MenuManager) item;
				} else if (item instanceof ActionSetContributionItem
						&& ((ActionSetContributionItem) item).getInnerItem() instanceof MenuManager) {
					menu = (MenuManager) ((ActionSetContributionItem) item).getInnerItem();
				}
				if (menu != null) { processItems(menu); }
			}
			menuManager.updateAll(true);
		}

		/**
		 * Process items.
		 *
		 * @param menu
		 *            the menu
		 */
		private static void processItems(final IMenuManager menu) {
			for (final IContributionItem item : menu.getItems()) {
				final String name = item.getId();
				if (MENU_ITEMS_TO_REMOVE.contains(name)) {
					item.setVisible(false);
					continue;
				}
				changeIcon(menu, item, name);
			}
		}

		/**
		 * Change icon.
		 *
		 * @param menu
		 *            the menu
		 * @param item
		 *            the item
		 * @param id
		 *            the id
		 */
		public static void changeIcon(final IMenuManager menu, final IContributionItem item, final String id) {
			if (item.isGroupMarker() || item.isSeparator() || !item.isVisible()) return;
			String imageName = MENU_IMAGES.get(id);
			if (imageName != null) { changeIcon(menu, item, GamaIcon.named(imageName).descriptor()); }
		}

		/**
		 * Change icon.
		 *
		 * @param menu
		 *            the menu
		 * @param item
		 *            the item
		 * @param image
		 *            the image
		 */
		private static void changeIcon(final IMenuManager menu, final IContributionItem item,
				final ImageDescriptor image) {
			if (item instanceof ActionContributionItem) {
				((ActionContributionItem) item).getAction().setImageDescriptor(image);
			} else if (item instanceof CommandContributionItem) {
				final CommandContributionItemParameter data = ((CommandContributionItem) item).getData();
				data.commandId = ((CommandContributionItem) item).getCommand().getId();
				data.icon = image;
				final CommandContributionItem newItem = new CommandContributionItem(data);
				newItem.setId(item.getId());
				menu.insertAfter(item.getId(), newItem);
				menu.remove(item);
				item.dispose();
			} else if (item instanceof ActionSetContributionItem) {
				changeIcon(menu, ((ActionSetContributionItem) item).getInnerItem(), image);
			} else if (item instanceof MenuManager) { ((MenuManager) item).setImageDescriptor(image); }
		}

	}

}