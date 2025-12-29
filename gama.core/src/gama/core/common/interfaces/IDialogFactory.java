/*******************************************************************************************************
 *
 * IDialogFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import org.eclipse.core.runtime.IPath;

import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.gaml.architecture.user.UserPanelStatement;

/**
 *
 */
public interface IDialogFactory {

	/**
	 * Error. Calls the error dialog associated to the current GUI with the default scope. Better to use the complete
	 * form when a scope is available
	 *
	 * @param error
	 *            the error
	 */
	default void error(final String error) {
		error(GAMA.getRuntimeScope(), error);
	}

	/**
	 * Error.
	 *
	 * @param error
	 *            the error
	 */
	default void error(final IScope scope, final String error) {
		inform(scope, "Error: " + error);
	}

	/**
	 * Tell. Calls the information dialog associated to the current GUI with the default scope. Better to use the
	 * complete form when a scope is available
	 *
	 * @param message
	 *            the message
	 */
	default void inform(final String message) {
		inform(GAMA.getRuntimeScope(), message);
	}

	/**
	 * Tell. Calls the information dialog associated to the current GUI
	 *
	 * @param message
	 *            the message
	 */
	default void inform(final IScope scope, final String message) {}

	/**
	 * Warning. Calls the warning dialog associated to the current GUI with the default scope. Better to use the
	 * complete form when a scope is available
	 *
	 * @param warning
	 *            the warning
	 */
	default void warning(final String warning) {
		warning(GAMA.getRuntimeScope(), warning);
	}

	/**
	 * Warning. Calls the warning dialog associated to the current GUI
	 *
	 * @param warning
	 *            the warning
	 */
	default void warning(final IScope scope, final String warning) {
		inform(scope, "Warning: " + warning);
	}

	/**
	 * Question. Calls the question dialog associated to the current GUI with the default scope. Better to use the
	 * complete form when a scope is available
	 *
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @return true, if successful
	 */
	default boolean question(final String title, final String message) {
		return question(GAMA.getRuntimeScope(), title, message);
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
	default boolean question(final IScope scope, final String title, final String message) {
		return true;
	}

	/**
	 * Modal question. Calls the modal question dialog associated to the current GUI with the default scope. Better to
	 * use the complete form when a scope is available
	 *
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @return true, if successful
	 */
	default boolean modalQuestion(final String title, final String message) {
		return modalQuestion(GAMA.getRuntimeScope(), title, message);
	}

	/**
	 * Modal question. Calls the modal question dialog associated to the current GUI
	 *
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @return true, if successful
	 */
	default boolean modalQuestion(final IScope scope, final String title, final String message) {
		return true;
	}

	/**
	 * Confirm. Calls the confirmation dialog associated to the current GUI with the default scope. Better to use the
	 * complete form when a scope is available
	 *
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @return true, if successful
	 */
	default boolean confirm(final String title, final String message) {
		return confirm(GAMA.getRuntimeScope(), title, message);
	}

	/**
	 * Confirmation.
	 *
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @return true, if successful
	 */
	default boolean confirm(final IScope scope, final String title, final String message) {
		return true;
	}

	/**
	 * Opens the user dialog associated to the given panel.
	 *
	 * @param scope
	 * @param panel
	 */
	default void openUserDialog(final IScope scope, final UserPanelStatement panel) {}

	/**
	 * Disconnects and closes the user dialog if any.
	 */
	default void closeUserDialog() {}

	/**
	 * Opens the workspace selection dialog.
	 *
	 * @return true, if successful and false if cancelled (i.e. no workspace selected)
	 */
	default String openWorkspaceSelectionDialog(final boolean performInitialCheck) {
		return "";
	}

	/**
	 * Opens a container selection dialog.
	 *
	 * @param title
	 *            the title
	 * @return a container (folder, project...) or null if cancelled
	 */
	default IPath openContainerSelectionDialog(final String title, final String message) {
		return null;
	}

}
