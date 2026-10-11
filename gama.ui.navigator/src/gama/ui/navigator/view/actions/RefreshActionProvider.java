/*******************************************************************************************************
 *
 * RefreshActionProvider.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.ui.navigator.view.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

import gama.ui.navigator.view.contents.UserGroupFolder;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * The Class RefreshActionProvider.
 */
public class RefreshActionProvider extends CommonActionProvider {

	/** The refresh action. */
	private RefreshAction refreshAction;

	/** The clean and update action. */
	private CleanAndUpdateAction cleanAndUpdateAction;

	/** The run all tests action. */
	private RunAllTestsAction runAllTestsAction;

	/** The shell. */
	private Shell shell;

	@Override
	public void init(final ICommonActionExtensionSite aSite) {
		super.init(aSite);
		shell = aSite.getViewSite().getShell();
		makeActions();
	}

	@Override
	public void fillActionBars(final IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		updateActionBars();
	}

	@Override
	public void fillContextMenu(final IMenuManager menu) {
		final IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		refreshAction.selectionChanged(selection);
		runAllTestsAction.selectionChanged(selection);
		if (runAllTestsAction.isEnabled()) { menu.appendToGroup("group.refresh", runAllTestsAction); }
		menu.appendToGroup("group.refresh", refreshAction);
		menu.appendToGroup("group.refresh", cleanAndUpdateAction);
		if (selection.size() == 1) {
			final Object o = selection.getFirstElement();
			if (GroupActions.isUserFolder(o)) {
				menu.appendToGroup("group.refresh", new GroupActions.NewGroup(null));
			} else if (o instanceof UserGroupFolder group && group.isEditable()) {
				menu.appendToGroup("group.refresh", new GroupActions.NewGroup(group));
				menu.appendToGroup("group.refresh", new GroupActions.RenameGroup(group));
				menu.appendToGroup("group.refresh", new GroupActions.DeleteGroup(group));
			}
		}
	}

	/**
	 * Make actions.
	 */
	protected void makeActions() {
		final IShellProvider sp = () -> shell;
		refreshAction = new RefreshAction(sp);
		refreshAction.setImageDescriptor(GamaIcon.named(IGamaIcons.FILE_REFRESH).descriptor());
		refreshAction.setDisabledImageDescriptor(GamaIcon.named(IGamaIcons.FILE_REFRESH).disabledDescriptor());
		refreshAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_REFRESH);
		cleanAndUpdateAction = new CleanAndUpdateAction();
		cleanAndUpdateAction.setImageDescriptor(GamaIcon.named(IGamaIcons.FILE_REFRESH).descriptor());
		runAllTestsAction = new RunAllTestsAction(sp, "Run all tests");
		runAllTestsAction.setImageDescriptor(GamaIcon.named(IGamaIcons.TEST_RUN).descriptor());
		runAllTestsAction.setDisabledImageDescriptor(GamaIcon.named(IGamaIcons.TEST_RUN).disabledDescriptor());

	}

	@Override
	public void updateActionBars() {
		final IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		refreshAction.selectionChanged(selection);
		runAllTestsAction.selectionChanged(selection);
	}

}
