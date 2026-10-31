/*******************************************************************************************************
 *
 * CleanAndUpdateAction.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.view.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;

import gama.ui.shared.interfaces.IRefreshHandler;
import gama.ui.shared.utils.PreferencesHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * Cleans and updates the whole workspace: the metadata of all the files are deleted and rebuilt, the library of models
 * is rebuilt so that it is in sync with GAMA and its plugins, every project is reclassified, and all the projects are
 * rebuilt. Unlike {@link RefreshAction}, which only synchronises the selection with the file system and rebuilds its
 * metadata, this action always applies to the whole workspace.
 */
public class CleanAndUpdateAction extends Action {

	/** The id of this action. */
	public static final String ID = "gama.ui.navigator.CleanAndUpdateAction";

	/**
	 * Instantiates a new clean and update action.
	 */
	public CleanAndUpdateAction() {
		super("Clean and update workspace...");
		setId(ID);
		setToolTipText("Repairs library links using the running installation and rebuilds metadata and projects");
	}

	@Override
	public void run() {
		if (PreferencesHelper.CONFIRM_CLEAN_AND_UPDATE.getValue()) {
			final MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(
					WorkbenchHelper.getShell(), "Clean and update the workspace",
					"The metadata of all the files will be deleted and rebuilt, the library of models will be rebuilt "
							+ "to be in sync with GAMA and its plugins, and all the projects will be reclassified and "
							+ "rebuilt. Library projects will be linked to the running installation's versions, without "
							+ "creating user copies. Genuine user projects and original library files will not be deleted. "
							+ "A report will describe the result.\n\n"
							+ "This can take some time. Do you want to proceed?",
					"Do not show again", false, null, null);
			if (dialog.getReturnCode() != IDialogConstants.OK_ID) return;
			if (dialog.getToggleState()) { PreferencesHelper.CONFIRM_CLEAN_AND_UPDATE.set(false).save(); }
		}
		if (!org.eclipse.ui.PlatformUI.getWorkbench().saveAllEditors(true)) return;
		final IRefreshHandler refresh = WorkbenchHelper.getService(IRefreshHandler.class);
		if (refresh != null) { refresh.completeRefresh(null); }
	}

}
