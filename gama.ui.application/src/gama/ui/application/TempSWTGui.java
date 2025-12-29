/*******************************************************************************************************
 *
 * TempSWTGui.java, in gama.ui.application, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.application;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import gama.core.common.interfaces.IConsoleListener;
import gama.core.common.interfaces.IDialogFactory;
import gama.core.common.interfaces.IGui;
import gama.dev.DEBUG;

/**
 * A partial implementation of IGui using SWT dialogs and the console for logging.
 */
public class TempSWTGui implements IGui {

	@Override
	public IConsoleListener getConsole() { return (s, root, color) -> DEBUG.LOG("Console: " + s); }

	@Override
	public IDialogFactory getDialogFactory() {
		return new IDialogFactory() {

			@Override
			public void error(final String error) {
				Display.getDefault().syncExec(() -> MessageDialog.openError(null, "Error", error));
			}

			@Override
			public void inform(final String message) {
				Display.getDefault().syncExec(() -> MessageDialog.openInformation(null, "Information", message));
			}

			@Override
			public void warning(final String message) {
				Display.getDefault().syncExec(() -> MessageDialog.openWarning(null, "Warning", message));
			}

			@Override
			public boolean question(final String title, final String message) {
				return Display.getDefault().syncCall(() -> MessageDialog.openQuestion(null, title, message));
			}

			@Override
			public boolean confirm(final String title, final String message) {
				return Display.getDefault().syncCall(() -> MessageDialog.openConfirm(null, title, message));
			}
		};
	}

}
