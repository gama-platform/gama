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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import gama.api.GAMA;
import gama.api.ui.IGui;
import gama.ui.application.workbench.PerspectiveHelper;

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
		if (PerspectiveHelper.isSimulationPerspective()) {
			menuContributionList.add(createViewItem("Console", IGui.CONSOLE_VIEW_ID,
					"icons_svg/light/views/tabs/view.console.svg"));
			menuContributionList.add(createViewItem("Interactive Console", IGui.INTERACTIVE_CONSOLE_VIEW_ID,
					"icons_svg/light/views/tabs/view.interactive.svg"));
			return menuContributionList.toArray(new IContributionItem[0]);
		}
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IContributionItem item = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		menuContributionList.add(item); // add the list of views in the menu
		return menuContributionList.toArray(new IContributionItem[menuContributionList.size()]);
	}

	private static IContributionItem createViewItem(final String label, final String viewId, final String iconPath) {
		final Action action = new Action(label) {

			@Override
			public void run() {
				GAMA.getGui().showView(null, viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
			}
		};
		action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("gama.ui.shared", iconPath));
		return new ActionContributionItem(action);
	}

}
