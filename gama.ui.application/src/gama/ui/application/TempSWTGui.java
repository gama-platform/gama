/*******************************************************************************************************
 *
 * TempSWTGui.java, in gama.ui.application, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.application;

import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import gama.api.compilation.descriptions.IActionDescription;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPoint;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IConsoleListener;
import gama.api.ui.IDialogFactory;
import gama.api.ui.IGui;
import gama.api.utils.geometry.GamaPointFactory;
import gama.api.utils.map.GamaMapFactory;
import gama.api.utils.server.ISocketCommand;
import gama.dev.DEBUG;
import gama.ui.application.workbench.PickWorkspaceDialog;

/**
 * A partial implementation of IGui using SWT dialogs and the console for logging. Serves during initialization, before
 * the real GUI is ready.
 */
public class TempSWTGui implements IGui {

	@Override
	public void exit() {
		Display.getDefault().syncExec(() -> {
			if (PlatformUI.isWorkbenchRunning()) {
				PlatformUI.getWorkbench().close();
				IGui.super.exit();
			}
		});

	}

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

			/**
			 * Opens the workspace selection dialog.
			 *
			 * @return the new path, if successful and null if cancelled (i.e. no workspace selected)
			 */
			@Override
			public String openWorkspaceSelectionDialog(final boolean performInitialCheck) {
				PickWorkspaceDialog pwd = new PickWorkspaceDialog(performInitialCheck);
				Display.getDefault().syncExec(pwd::open);
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
				Display.getDefault().syncExec(dialog::open);
				return (IPath) dialog.getResult()[0];
			}
		};
	}

	@Override
	public IMap<String, IMap<String, Object>> openWizard(final IScope scope, final String title,
			final IActionDescription finish, final IList<IMap<String, Object>> pages) {
		return GamaMapFactory.EMPTY;
	}

	@Override
	public IPoint getMouseLocationInModel() { return GamaPointFactory.getNullPoint(); }

	@Override
	public IPoint getMouseLocationInDisplay() { return GamaPointFactory.getNullPoint(); }

	@Override
	public Map<String, ISocketCommand> getServerCommands() { return GamaMapFactory.EMPTY; }

}
