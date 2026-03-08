/*******************************************************************************************************
 *
 * AbstractQuickDialog.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import gama.dev.DEBUG;

/**
 * The Class AbstractQuickDialog.
 */
abstract class AbstractQuickDialog extends PopupDialog {

	/** The Constant GRAB_FOCUS. */
	protected static final boolean GRAB_FOCUS = true;

	/** The Constant PERSIST_NO_SIZE. */
	protected static final boolean PERSIST_NO_SIZE = false;

	/** The Constant PERSIST_SIZE. */
	protected static final boolean PERSIST_SIZE = true;

	/** The Constant PERSIST_NO_BOUNDS. */
	protected static final boolean PERSIST_NO_BOUNDS = false;

	/** The Constant PERSIST_BOUNDS. */
	protected static final boolean PERSIST_BOUNDS = true;

	/** The Constant SHOW_DIALOG_MENU. */
	protected static final boolean SHOW_DIALOG_MENU = true;

	/** The Constant SHOW_NO_DIALOG_MENU. */
	protected static final boolean SHOW_NO_DIALOG_MENU = false;

	/** The Constant SHOW_NO_PERSIST_ACTIONS. */
	protected static final boolean SHOW_NO_PERSIST_ACTIONS = false;

	/** The Constant SHOW_PERSIST_ACTIONS. */
	protected static final boolean SHOW_PERSIST_ACTIONS = true;

	/**
	 * Instantiates a new abstract quick dialog.
	 *
	 * @param parent
	 *            the parent
	 * @param shellStyle
	 *            the shell style
	 * @param takeFocusOnOpen
	 *            the take focus on open
	 * @param persistSize
	 *            the persist size
	 * @param persistLocation
	 *            the persist location
	 * @param showDialogMenu
	 *            the show dialog menu
	 * @param showPersistActions
	 *            the show persist actions
	 * @param titleText
	 *            the title text
	 * @param infoText
	 *            the info text
	 */
	AbstractQuickDialog(final Shell parent, final int shellStyle, final boolean takeFocusOnOpen,
			final boolean persistSize, final boolean persistLocation, final boolean showDialogMenu,
			final boolean showPersistActions, final String titleText, final String infoText) {
		super(parent, shellStyle, takeFocusOnOpen, persistSize, persistLocation, showDialogMenu, showPersistActions,
				titleText, infoText);
	}

	@Override
	public final int open() {
		int value = super.open();
		beforeRunEventLoop();
		runEventLoop(getShell());
		return value;
	}

	/**
	 * Before run event loop.
	 */
	protected void beforeRunEventLoop() {

	}

	/**
	 * Run event loop.
	 *
	 * @param loopShell
	 *            the loop shell
	 */
	private void runEventLoop(final Shell loopShell) {
		Display display;
		if (getShell() == null) {
			display = Display.getCurrent();
		} else {
			display = loopShell.getDisplay();
		}

		while (loopShell != null && !loopShell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) { display.sleep(); }
			} catch (Throwable e) {
				DEBUG.ERR("UI problems on dispatch", e);
			}
		}
		if (!display.isDisposed()) { display.update(); }
	}

	@Override
	protected boolean canHandleShellCloseEvent() {
		return true;
	}

}