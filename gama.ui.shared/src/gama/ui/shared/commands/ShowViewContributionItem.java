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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

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
import org.eclipse.ui.views.IViewDescriptor;
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
		menuContributionList.add(createViewItem("Console", IGui.CONSOLE_VIEW_ID,
				"icons_svg/light/views/tabs/view.console.svg"));
		menuContributionList.add(createViewItem("Interactive Console", IGui.INTERACTIVE_CONSOLE_VIEW_ID,
				"icons_svg/light/views/tabs/view.interactive.svg"));
		menuContributionList.add(createViewItem("Server Console", IGui.SERVER_CONSOLE_VIEW_ID,
				"icons_svg/light/views/tabs/view.server.svg"));
		if (PerspectiveHelper.isSimulationPerspective()) return menuContributionList.toArray(new IContributionItem[0]);
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null && window.getActivePage() != null) {
			menuContributionList.add(new Separator());
			final Set<String> consoles =
					Set.of(IGui.CONSOLE_VIEW_ID, IGui.INTERACTIVE_CONSOLE_VIEW_ID, IGui.SERVER_CONSOLE_VIEW_ID);
			Arrays.stream(window.getActivePage().getShowViewShortcuts()).distinct()
					.filter(id -> !consoles.contains(id))
					.map(id -> window.getWorkbench().getViewRegistry().find(id)).filter(Objects::nonNull)
					.sorted(Comparator.comparing(IViewDescriptor::getLabel, String.CASE_INSENSITIVE_ORDER))
					.forEach(descriptor -> {
						final Action action = createViewAction(descriptor.getLabel(), descriptor.getId());
						action.setImageDescriptor(descriptor.getImageDescriptor());
						menuContributionList.add(new ActionContributionItem(action));
					});
			final CommandContributionItemParameter other = new CommandContributionItemParameter(window,
					"gama.showView.other", "org.eclipse.ui.views.showView", CommandContributionItem.STYLE_PUSH);
			other.label = "Other...";
			menuContributionList.add(new CommandContributionItem(other));
		}
		return menuContributionList.toArray(new IContributionItem[0]);
	}

	private static IContributionItem createViewItem(final String label, final String viewId, final String iconPath) {
		final Action action = createViewAction(label, viewId);
		action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("gama.ui.shared", iconPath));
		return new ActionContributionItem(action);
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
