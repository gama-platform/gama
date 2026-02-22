/*******************************************************************************************************
 *
 * IConsoleListener.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.types.color.IColor;

/**
 * The listener interface for receiving console events in GAMA.
 * 
 * <p>This interface defines the contract for components that need to listen to and handle
 * console output from GAMA simulations. It provides methods for displaying debug and 
 * informational messages, managing console visibility, and clearing console content.</p>
 * 
 * <p>Console listeners can be chained together, allowing multiple consumers of console
 * output. The interface provides default implementations for most methods, making it easy
 * to implement only the necessary functionality.</p>
 * 
 * <h2>Main Responsibilities:</h2>
 * <ul>
 *   <li>Display debug messages with cycle information</li>
 *   <li>Display informational messages</li>
 *   <li>Control console view visibility</li>
 *   <li>Clear console content</li>
 *   <li>Manage listener chains</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * IConsoleListener console = gui.getConsole();
 * console.informConsole("Simulation started", agent, null);
 * console.debugConsole(10, "Processing cycle", agent, GamaColorFactory.GREEN);
 * }</pre>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @date 2 nov. 2023
 */
public interface IConsoleListener {

	/**
	 * Adds a console listener to create a chain of listeners.
	 * 
	 * <p>This method allows multiple console listeners to be chained together, enabling
	 * console output to be sent to multiple destinations (e.g., UI console, log file, etc.).</p>
	 * 
	 * <p>The default implementation does nothing. Override to implement listener chaining.</p>
	 *
	 * @param console the console listener to add to the chain
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 2 nov. 2023
	 */
	default void addConsoleListener(final IConsoleListener console) {}

	/**
	 * Removes a console listener from the chain.
	 * 
	 * <p>This method removes a previously added listener from the chain. If the listener
	 * is not in the chain, this method does nothing.</p>
	 * 
	 * <p>The default implementation does nothing. Override to implement listener removal.</p>
	 *
	 * @param console the console listener to remove
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 2 nov. 2023
	 */
	default void removeConsoleListener(final IConsoleListener console) {}

	/**
	 * Displays a debug message in the console with cycle information and color.
	 * 
	 * <p>Debug messages are typically used during model execution to track the simulation
	 * state at specific cycles. The message is prefixed with the cycle number.</p>
	 * 
	 * <p>The default implementation delegates to {@link #informConsole(String, ITopLevelAgent, IColor)}
	 * with a formatted message including the cycle number.</p>
	 *
	 * @param cycle the simulation cycle number
	 * @param s the message to display
	 * @param root the top-level agent (simulation or experiment) associated with this message
	 * @param color the color to use for displaying the message (may be null for default color)
	 */
	default void debugConsole(final int cycle, final String s, final ITopLevelAgent root, final IColor color) {
		informConsole("(cycle: " + String.valueOf(cycle) + ") " + s, root, color);
	}

	/**
	 * Displays a debug message in the console with cycle information.
	 * 
	 * <p>This is a convenience method that calls {@link #debugConsole(int, String, ITopLevelAgent, IColor)}
	 * with a null color (default color will be used).</p>
	 *
	 * @param cycle the simulation cycle number
	 * @param s the message to display
	 * @param root the top-level agent (simulation or experiment) associated with this message
	 */
	default void debugConsole(final int cycle, final String s, final ITopLevelAgent root) {
		debugConsole(cycle, s, root, null);
	}

	/**
	 * Displays an informational message in the console with optional color.
	 * 
	 * <p>This is the main method for sending messages to the console. It should be
	 * implemented by concrete console implementations to actually display the message.</p>
	 * 
	 * <p>Messages can be associated with a specific agent to help track which simulation
	 * or experiment generated the output. Colors can be used to differentiate message types
	 * or importance levels.</p>
	 *
	 * @param s the message to display
	 * @param root the top-level agent (simulation or experiment) associated with this message
	 * @param color the color to use for displaying the message (may be null for default color)
	 */
	void informConsole(String s, ITopLevelAgent root, IColor color);

	/**
	 * Displays an informational message in the console.
	 * 
	 * <p>This is a convenience method that calls {@link #informConsole(String, ITopLevelAgent, IColor)}
	 * with a null color (default color will be used).</p>
	 *
	 * @param s the message to display
	 * @param root the top-level agent (simulation or experiment) associated with this message
	 */
	default void informConsole(final String s, final ITopLevelAgent root) {
		informConsole(s, root, null);
	}

	/**
	 * Shows or hides the console view for the specified agent.
	 * 
	 * <p>This method controls the visibility of console views in the UI. It can be used
	 * to show the console when starting a simulation or hide it when the simulation ends.</p>
	 * 
	 * <p>The default implementation does nothing. Override to implement console view visibility control.</p>
	 *
	 * @param agent the agent whose console view should be toggled
	 * @param show true to show the console view, false to hide it
	 */
	default void toggleConsoleViews(final ITopLevelAgent agent, final boolean show) {}

	/**
	 * Erases all content from the console.
	 * 
	 * <p>This method clears the console output, optionally resetting it to null. It is
	 * typically called when starting a new simulation or when the user explicitly requests
	 * to clear the console.</p>
	 * 
	 * <p>The default implementation does nothing. Override to implement console clearing.</p>
	 *
	 * @param setToNull if true, reset the console to null; if false, just clear the content
	 */
	default void eraseConsole(final boolean setToNull) {}
}
