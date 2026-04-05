/*******************************************************************************************************
 *
 * DialogFactory.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.factories;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import gama.api.gaml.statements.IStatement;
import gama.api.runtime.SystemInfo;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IDialogFactory;
import gama.ui.application.workbench.PickWorkspaceDialog;
import gama.ui.shared.dialogs.UserControlDialog;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class DialogFactory.
 */
public class DialogFactory extends AbstractServiceFactory implements IDialogFactory {

	/**
	 * Open user dialog.
	 *
	 * @param scope
	 *            the scope
	 * @param panel
	 *            the panel
	 */
	@Override
	public void openUserDialog(final IScope scope, final IStatement panel) {
		final UserControlDialog dialog = new UserControlDialog(scope, panel);
		dialog.open();
	}

	@Override
	public void closeUserDialog() {
		final UserControlDialog d = UserControlDialog.current;
		if (d != null) { d.close(); }
	}

	/**
	 * Error.
	 *
	 * @param error
	 *            the error
	 */
	@Override
	public void error(final IScope scope, final String error) {
		WorkbenchHelper.run(() -> MessageDialog.openError(null, "Error", error));
	}

	/**
	 * Tell.
	 *
	 * @param error
	 *            the error
	 */
	@Override
	public void inform(final IScope scope, final String message) {
		WorkbenchHelper.run(() -> MessageDialog.openInformation(null, "Information", message));
	}

	/**
	 * Question.
	 *
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @return true, if successful
	 */
	@Override
	public boolean question(final IScope scope, final String title, final String message) {
		return WorkbenchHelper.run(() -> MessageDialog.open(MessageDialog.QUESTION, null, title, message, SWT.SHEET));
	}

	/**
	 * Modal question.
	 *
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @return true, if successful
	 */
	@Override
	public boolean modalQuestion(final IScope scope, final String title, final String message) {
		return WorkbenchHelper.run(() -> MessageDialog.open(MessageDialog.QUESTION, null, title, message,
				!SystemInfo.isMac() ? SWT.NONE : SWT.SHEET));
	}

	/**
	 * Confirm.
	 *
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @return true, if successful
	 */
	@Override
	public boolean confirm(final IScope scope, final String title, final String message) {
		return WorkbenchHelper.run(() -> MessageDialog.openConfirm(null, title, message));
	}

	@Override
	public void warning(final IScope scope, final String warning) {
		WorkbenchHelper.run(() -> MessageDialog.openWarning(null, "Warning", warning));
	}

	/**
	 * Opens the workspace selection dialog.
	 *
	 * @return the new path, if successful and null if cancelled (i.e. no workspace selected)
	 */
	@Override
	public String openWorkspaceSelectionDialog(final boolean performInitialCheck) {
		PickWorkspaceDialog pwd = new PickWorkspaceDialog(performInitialCheck);
		WorkbenchHelper.run(pwd::open);
		return pwd.getResultWorkspace();
	}

	/**
	 * Opens a container selection dialog.
	 *
	 * @param title
	 *            the title of the dialog
	 * @param message
	 *            the message to display in the dialog
	 * @return a path to a container (folder, project...) or null if cancelled
	 */
	@Override
	public IPath openContainerSelectionDialog(final String title, final String message) {
		final ContainerSelectionDialog dialog = new ContainerSelectionDialog(null, null, false, message);
		dialog.setTitle(title);
		dialog.showClosedProjects(false);
		WorkbenchHelper.run(dialog::open);
		return (IPath) dialog.getResult()[0];
	}

	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return this;
	}

}
