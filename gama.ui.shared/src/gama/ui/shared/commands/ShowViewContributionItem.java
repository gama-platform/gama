/*******************************************************************************************************
 *
 * ShowViewContributionItem.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import gama.api.GAMA;
import gama.api.ui.IGui;

/**
 * The Class ShowViewContributionItem.
 */
public class ShowViewContributionItem extends CompoundContributionItem {

	/**
	 * Instantiates a new show view contribution item.
	 */
	public ShowViewContributionItem() {
	}

	/**
	 * Instantiates a new show view contribution item.
	 *
	 * @param id the id
	 */
	public ShowViewContributionItem(final String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		final List<IContributionItem> menuContributionList = new ArrayList<>();
		addView(menuContributionList, IGui.NAVIGATOR_VIEW_ID);
		menuContributionList.add(new Separator());
		addView(menuContributionList, IGui.CONSOLE_VIEW_ID);
		addView(menuContributionList, IGui.INTERACTIVE_CONSOLE_VIEW_ID);
		addView(menuContributionList, IGui.SERVER_CONSOLE_VIEW_ID);
		menuContributionList.add(new Separator());
		addView(menuContributionList, IGui.OUTLINE_VIEW_ID);
		addView(menuContributionList, "gama.ui.navigator.view.PreviousVersions");
		addView(menuContributionList, IGui.MINIMAP_VIEW_ID);
		addView(menuContributionList, "gama.ui.navigator.view.Dependencies");
		menuContributionList.add(new Separator());
		addView(menuContributionList, IGui.VALIDATION_VIEW_ID);
		addView(menuContributionList, IGui.TEST_VIEW_ID);
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null && window.getActivePage() != null) {
			menuContributionList.add(new Separator());
			final CommandContributionItemParameter other = new CommandContributionItemParameter(window,
					"gama.showView.other", "org.eclipse.ui.views.showView", CommandContributionItem.STYLE_PUSH);
			other.label = "Other...";
			menuContributionList.add(new CommandContributionItem(other));
		}
		return menuContributionList.toArray(new IContributionItem[0]);
	}

	private static void addView(final List<IContributionItem> items, final String viewId) {
		final var descriptor = PlatformUI.getWorkbench().getViewRegistry().find(viewId);
		if (descriptor == null) return;
		final Action action = createViewAction(descriptor.getLabel(), viewId);
		action.setImageDescriptor(descriptor.getImageDescriptor());
		items.add(new ActionContributionItem(action));
	}

	private static Action createViewAction(final String label, final String viewId) {
		final Action action = new Action(label) {

			@Override
			public void run() {
				GAMA.getGui().showView(null, viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
			}
		};
		action.setId(viewId);
		return action;
	}

}
