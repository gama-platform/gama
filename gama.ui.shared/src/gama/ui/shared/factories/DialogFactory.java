/*******************************************************************************************************
 *
 * DialogFactory.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.factories;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import gama.core.common.interfaces.IDialogFactory;
import gama.core.runtime.IScope;
import gama.core.runtime.PlatformHelper;
import gama.gaml.architecture.user.UserPanelStatement;
import gama.ui.shared.dialogs.UserControlDialog;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class DialogFactory.
 */
public class DialogFactory extends AbstractServiceFactory implements IDialogFactory {

	@Override
	public void openUserDialog(final IScope scope, final UserPanelStatement panel) {
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
				!PlatformHelper.isMac() ? SWT.NONE : SWT.SHEET));
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

	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return this;
	}

}
