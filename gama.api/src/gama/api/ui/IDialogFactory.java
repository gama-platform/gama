/*******************************************************************************************************
 *
 * IDialogFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import org.eclipse.core.runtime.IPath;

import gama.api.GAMA;
import gama.api.gaml.statements.IStatement;
import gama.api.runtime.scope.IScope;

/**
 * Factory interface for creating and displaying various types of user dialogs in GAMA.
 * 
 * <p>This interface provides methods for showing error messages, warnings, informational
 * messages, and interactive dialogs such as questions and confirmations. It supports
 * both simple message dialogs and complex user input forms.</p>
 * 
 * <h2>Main Responsibilities:</h2>
 * <ul>
 *   <li>Display error, warning, and information messages</li>
 *   <li>Show question and confirmation dialogs</li>
 *   <li>Open user input dialogs and control panels</li>
 *   <li>Show workspace and file selection dialogs</li>
 * </ul>
 * 
 * <h2>Dialog Types:</h2>
 * <ul>
 *   <li><strong>Error:</strong> Critical error messages requiring user acknowledgment</li>
 *   <li><strong>Warning:</strong> Warning messages about potential issues</li>
 *   <li><strong>Information:</strong> General informational messages</li>
 *   <li><strong>Question:</strong> Yes/No questions for user decisions</li>
 *   <li><strong>Confirmation:</strong> Confirmation requests for actions</li>
 *   <li><strong>Modal Question:</strong> Blocking questions that must be answered</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * IDialogFactory factory = gui.getDialogFactory();
 * factory.inform("Simulation completed successfully");
 * if (factory.question("Save Results?", "Do you want to save the results?")) {
 *     // Save results
 * }
 * }</pre>
 * 
 * <h2>Scope Handling:</h2>
 * <p>Most methods have two versions: one that takes an {@link IScope} parameter and one
 * that uses the default runtime scope. When a scope is available, prefer using the version
 * that accepts a scope parameter for better context management.</p>
 *
 * @author The GAMA Development Team
 * @since GAMA 1.0
 */
public interface IDialogFactory {

	/**
	 * Displays an error dialog using the default runtime scope.
	 * 
	 * <p>This is a convenience method that delegates to {@link #error(IScope, String)}
	 * using {@link GAMA#getRuntimeScope()}. When a scope is available, prefer using
	 * the scoped version for better error context.</p>
	 *
	 * @param error the error message to display
	 */
	default void error(final String error) {
		error(GAMA.getRuntimeScope(), error);
	}

	/**
	 * Displays an error dialog in the context of the given scope.
	 * 
	 * <p>The default implementation delegates to {@link #inform(IScope, String)} with
	 * an "Error: " prefix. Implementations may override this to provide specialized
	 * error dialog styling or behavior.</p>
	 *
	 * @param scope the scope in which the error occurred (provides context)
	 * @param error the error message to display
	 */
	default void error(final IScope scope, final String error) {
		inform(scope, "Error: " + error);
	}

	/**
	 * Displays an informational dialog using the default runtime scope.
	 * 
	 * <p>This is a convenience method that delegates to {@link #inform(IScope, String)}
	 * using {@link GAMA#getRuntimeScope()}. When a scope is available, prefer using
	 * the scoped version.</p>
	 *
	 * @param message the informational message to display
	 */
	default void inform(final String message) {
		inform(GAMA.getRuntimeScope(), message);
	}

	/**
	 * Displays an informational dialog in the context of the given scope.
	 * 
	 * <p>This is the main method for displaying informational messages to the user.
	 * The default implementation does nothing (for headless mode). UI implementations
	 * should override this to show actual dialogs.</p>
	 *
	 * @param scope the scope providing context for the message
	 * @param message the informational message to display
	 */
	default void inform(final IScope scope, final String message) {}

	/**
	 * Displays a warning dialog using the default runtime scope.
	 * 
	 * <p>This is a convenience method that delegates to {@link #warning(IScope, String)}
	 * using {@link GAMA#getRuntimeScope()}. When a scope is available, prefer using
	 * the scoped version.</p>
	 *
	 * @param warning the warning message to display
	 */
	default void warning(final String warning) {
		warning(GAMA.getRuntimeScope(), warning);
	}

	/**
	 * Displays a warning dialog in the context of the given scope.
	 * 
	 * <p>The default implementation delegates to {@link #inform(IScope, String)} with
	 * a "Warning: " prefix. Implementations may override this to provide specialized
	 * warning dialog styling or behavior.</p>
	 *
	 * @param scope the scope providing context for the warning
	 * @param warning the warning message to display
	 */
	default void warning(final IScope scope, final String warning) {
		inform(scope, "Warning: " + warning);
	}

	/**
	 * Displays a yes/no question dialog using the default runtime scope.
	 * 
	 * <p>This is a convenience method that delegates to {@link #question(IScope, String, String)}
	 * using {@link GAMA#getRuntimeScope()}. When a scope is available, prefer using
	 * the scoped version.</p>
	 *
	 * @param title the dialog title
	 * @param message the question to ask the user
	 * @return true if the user answered yes, false otherwise
	 */
	default boolean question(final String title, final String message) {
		return question(GAMA.getRuntimeScope(), title, message);
	}

	/**
	 * Displays a yes/no question dialog in the context of the given scope.
	 * 
	 * <p>The default implementation returns true, allowing headless mode to proceed
	 * with the default affirmative action. UI implementations should override this
	 * to show actual question dialogs.</p>
	 *
	 * @param scope the scope providing context for the question
	 * @param title the dialog title
	 * @param message the question to ask the user
	 * @return true if the user answered yes, false if the user answered no
	 */
	default boolean question(final IScope scope, final String title, final String message) {
		return true;
	}

	/**
	 * Displays a modal yes/no question dialog using the default runtime scope.
	 * 
	 * <p>Modal dialogs block execution until the user responds. This is a convenience
	 * method that delegates to {@link #modalQuestion(IScope, String, String)} using
	 * {@link GAMA#getRuntimeScope()}. When a scope is available, prefer using the
	 * scoped version.</p>
	 *
	 * @param title the dialog title
	 * @param message the question to ask the user
	 * @return true if the user answered yes, false otherwise
	 */
	default boolean modalQuestion(final String title, final String message) {
		return modalQuestion(GAMA.getRuntimeScope(), title, message);
	}

	/**
	 * Displays a modal yes/no question dialog in the context of the given scope.
	 * 
	 * <p>Modal dialogs block execution until the user responds. The default implementation
	 * returns true, allowing headless mode to proceed. UI implementations should override
	 * this to show actual modal question dialogs.</p>
	 *
	 * @param scope the scope providing context for the question
	 * @param title the dialog title
	 * @param message the question to ask the user
	 * @return true if the user answered yes, false if the user answered no
	 */
	default boolean modalQuestion(final IScope scope, final String title, final String message) {
		return true;
	}

	/**
	 * Displays a confirmation dialog using the default runtime scope.
	 * 
	 * <p>This is a convenience method that delegates to {@link #confirm(IScope, String, String)}
	 * using {@link GAMA#getRuntimeScope()}. When a scope is available, prefer using
	 * the scoped version.</p>
	 *
	 * @param title the dialog title
	 * @param message the confirmation message
	 * @return true if the user confirmed, false otherwise
	 */
	default boolean confirm(final String title, final String message) {
		return confirm(GAMA.getRuntimeScope(), title, message);
	}

	/**
	 * Displays a confirmation dialog in the context of the given scope.
	 * 
	 * <p>Confirmation dialogs typically ask the user to confirm an action before proceeding.
	 * The default implementation returns true, allowing headless mode to proceed. UI
	 * implementations should override this to show actual confirmation dialogs.</p>
	 *
	 * @param scope the scope providing context for the confirmation
	 * @param title the dialog title
	 * @param message the confirmation message
	 * @return true if the user confirmed, false if the user cancelled
	 */
	default boolean confirm(final IScope scope, final String title, final String message) {
		return true;
	}

	/**
	 * Opens a user dialog panel defined in the model.
	 * 
	 * <p>This method displays a custom dialog panel as defined by a GAML statement,
	 * typically from a user_panel or similar construct. The dialog allows interaction
	 * with model parameters or custom UI elements.</p>
	 * 
	 * <p>The default implementation does nothing (for headless mode).</p>
	 *
	 * @param scope the scope in which to execute the panel
	 * @param panel the GAML statement defining the user panel
	 */
	default void openUserDialog(final IScope scope, final IStatement panel) {}

	/**
	 * Closes the currently open user dialog, if any.
	 * 
	 * <p>This method disconnects event handlers and closes any user dialog that was
	 * previously opened with {@link #openUserDialog(IScope, IStatement)}.</p>
	 * 
	 * <p>The default implementation does nothing (for headless mode).</p>
	 */
	default void closeUserDialog() {}

	/**
	 * Opens the workspace selection dialog.
	 * 
	 * <p>This dialog allows the user to select or create a workspace directory where
	 * GAMA will store projects and settings. It is typically shown on first launch
	 * or when switching workspaces.</p>
	 *
	 * @param performInitialCheck if true, performs validation checks on the workspace
	 * @return the path to the selected workspace directory, or an empty string if cancelled
	 */
	default String openWorkspaceSelectionDialog(final boolean performInitialCheck) {
		return "";
	}

	/**
	 * Opens a container selection dialog.
	 * 
	 * <p>This dialog allows the user to select a container such as a folder or project.
	 * It is typically used for file save operations or when the user needs to specify
	 * a location within the workspace.</p>
	 *
	 * @param title the dialog title
	 * @param message a descriptive message to show in the dialog
	 * @return an IPath representing the selected container, or null if cancelled
	 */
	default IPath openContainerSelectionDialog(final String title, final String message) {
		return null;
	}

}
