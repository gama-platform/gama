/*******************************************************************************************************
 *
 * OpenActionProvider.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import gama.ui.navigator.view.contents.WrappedFile;
import gama.ui.shared.resources.GamaIcon;

/**
 * Provides the open and open with menus for IResources.
 *
 * @since 3.2
 *
 */
public class OpenActionProvider extends CommonActionProvider {

	/** The open file action. */
	private OpenFileAction openFileAction;

	/** The view site. */
	private ICommonViewerWorkbenchSite viewSite = null;

	/** The contribute. */
	private boolean contribute = false;

	@Override
	public void init(final ICommonActionExtensionSite aConfig) {
		if (aConfig.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			viewSite = (ICommonViewerWorkbenchSite) aConfig.getViewSite();
			openFileAction = new OpenFileAction(viewSite.getPage());
			openFileAction.setImageDescriptor(GamaIcon.named("navigator/navigator.open2").descriptor());
			contribute = true;
		}
	}

	@Override
	public void fillContextMenu(final IMenuManager aMenu) {
		if (!contribute || getContext().getSelection().isEmpty()) return;

		final IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		openFileAction.selectionChanged(selection);
		if (openFileAction.isEnabled()) { aMenu.insertAfter(ICommonMenuConstants.GROUP_OPEN, openFileAction); }
		// addOpenWithMenu(aMenu);
	}

	@Override
	public void fillActionBars(final IActionBars theActionBars) {
		if (!contribute) return;
		final IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		if (selection.size() == 1 && selection.getFirstElement() instanceof WrappedFile) {
			openFileAction.selectionChanged(selection);
			theActionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openFileAction);
		}

	}

	// /**
	// * Adds the open with menu.
	// *
	// * @param aMenu
	// * the a menu
	// */
	// private void addOpenWithMenu(final IMenuManager aMenu) {
	// final IStructuredSelection ss = (IStructuredSelection) getContext().getSelection();
	// if (ss == null || ss.size() != 1) return;
	// final Object o = ss.getFirstElement();
	// // first try IResource
	// IAdaptable openable = CloseResourceAction.getAdapter(o, IResource.class);
	// // otherwise try ResourceMapping
	// if (openable == null) {
	// openable = CloseResourceAction.getAdapter(o, ResourceMapping.class);
	// } else if (((IResource) openable).getType() != IResource.FILE) { openable = null; }
	// if (openable != null) {
	// // Create a menu flyout.
	// final IMenuManager submenu =
	// new MenuManager(WorkbenchNavigatorMessages.OpenActionProvider_OpenWithMenu_label,
	// ICommonMenuConstants.GROUP_OPEN_WITH);
	// submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
	// submenu.add(new OpenWithMenu(viewSite.getPage(), openable));
	// submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_ADDITIONS));
	//
	// // Add the submenu.
	// if (submenu.getItems().length > 2 && submenu.isEnabled()) {
	// aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
	// }
	// }
	// }

}
