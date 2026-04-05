/*******************************************************************************************************
 *
 * NullGuiHandler.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import gama.api.GAMA;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IParameter;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.font.IFont;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.ui.displays.DisplayDescription;
import gama.api.ui.displays.IDisplayCreator;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.utils.server.ISocketCommand;
import gama.api.utils.tests.CompoundSummary;
import gama.dev.DEBUG;

/**
 * A null object implementation of {@link IGui} for headless operation mode.
 * 
 * <p>This class provides a complete no-op implementation of the GAMA GUI interface,
 * allowing the platform to run in headless mode (without a graphical user interface).
 * This is essential for server deployments, batch processing, testing, and other
 * scenarios where a GUI is not available or desired.</p>
 * 
 * <h2>Main Responsibilities:</h2>
 * <ul>
 *   <li>Provide null/default implementations of all GUI operations</li>
 *   <li>Log messages instead of displaying dialogs</li>
 *   <li>Create minimal display surfaces for headless rendering</li>
 *   <li>Allow headless experiments to run without modification</li>
 * </ul>
 * 
 * <h2>Design Pattern:</h2>
 * <p>This class implements the Null Object pattern, allowing client code to call
 * GUI methods without checking for null or whether a GUI is available. All methods
 * provide safe default behaviors:</p>
 * <ul>
 *   <li>Dialog methods log messages instead of showing dialogs</li>
 *   <li>User input methods return default/null values</li>
 *   <li>View operations silently succeed</li>
 *   <li>Display creation returns minimal functional surfaces</li>
 * </ul>
 * 
 * <h2>Logging:</h2>
 * <p>The class uses a pluggable {@link IHeadlessLogger} to log messages that would
 * normally be shown in dialogs. By default, it logs to DEBUG, but can be customized
 * via {@link #setHeadlessLogger(IHeadlessLogger)}.</p>
 * 
 * <h2>Display Support:</h2>
 * <p>Even in headless mode, GAMA can create display surfaces for:</p>
 * <ul>
 *   <li>Generating images and snapshots</li>
 *   <li>Recording simulations</li>
 *   <li>Batch experiments that produce visual output</li>
 * </ul>
 * <p>The {@link #createDisplaySurfaceFor(gama.api.ui.IOutput.Display, Object)} method
 * creates image-based displays that work without a GUI.</p>
 * 
 * <h2>Initialization:</h2>
 * <p>A static initializer automatically registers this class as the headless GUI
 * implementation in GAMA, making it available for headless mode.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // In headless mode, GAMA automatically uses NullGuiHandler
 * IGui gui = GAMA.getGui();  // Returns NullGuiHandler instance
 * gui.inform("Message");      // Logs instead of showing dialog
 * gui.error("Error");         // Logs instead of showing error dialog
 * }</pre>
 *
 * @author The GAMA Development Team
 * @since GAMA 1.0
 */
public class NullGuiHandler implements IGui {

	/**
	 * The console listener for headless mode.
	 * 
	 * <p>Lazily initialized to log console messages to the headless logger.</p>
	 */
	protected IConsoleListener console = null;

	/**
	 * The logger used for outputting messages in headless mode.
	 * 
	 * <p>By default logs to DEBUG, but can be customized via
	 * {@link #setHeadlessLogger(IHeadlessLogger)}.</p>
	 */
	private IHeadlessLogger logger = DEBUG::LOG;

	/**
	 * The dialog factory for headless mode.
	 * 
	 * <p>Logs messages instead of showing actual dialogs.</p>
	 */
	IDialogFactory dialogFactory = new IDialogFactory() {
		@Override
		public void inform(final IScope scope, final String message) {
			logger.log("Message: " + message);
		}

		@Override
		public void error(final IScope scope, final String error) {
			logger.log("Error: " + error);
		}
	};

	static {
		GAMA.setHeadlessGui(new NullGuiHandler());
	}

	/**
	 * Opens a user input dialog in headless mode.
	 * 
	 * <p>In headless mode, this method returns null since user interaction is not
	 * possible. Models that require user input will fail or use default values
	 * when running headless.</p>
	 *
	 * @param scope the scope in which the dialog would operate
	 * @param title the dialog title (ignored in headless mode)
	 * @param parameters the input parameters to collect (ignored in headless mode)
	 * @param font the font for the dialog (ignored in headless mode)
	 * @param color the color scheme for the dialog (ignored in headless mode)
	 * @param showTitle whether to show the title (ignored in headless mode)
	 * @return null, as no user input can be collected in headless mode
	 */
	@Override
	public Map<String, Object> openUserInputDialog(final IScope scope, final String title,
			final List<IParameter> parameters, final IFont font, final IColor color, final Boolean showTitle) {
		return null;
	}

	/**
	 * Opens a wizard dialog in headless mode.
	 * 
	 * <p>In headless mode, this method returns null since user interaction is not
	 * possible. Wizards require user navigation through multiple pages, which cannot
	 * be done without a GUI.</p>
	 *
	 * @param scope the scope in which the wizard would operate
	 * @param title the wizard title (ignored in headless mode)
	 * @param finish the action to execute on finish (ignored in headless mode)
	 * @param pages the wizard pages (ignored in headless mode)
	 * @return null, as no wizard can be shown in headless mode
	 */
	@Override
	public IMap<String, IMap<String, Object>> openWizard(final IScope scope, final String title,
			final IActionDescription finish, final IList<IMap<String, Object>> pages) {
		return null;
	}

	/**
	 * Gets the dialog factory for headless mode.
	 * 
	 * <p>Returns a dialog factory that logs messages instead of showing dialogs.</p>
	 *
	 * @return the headless dialog factory
	 */
	@Override
	public IDialogFactory getDialogFactory() { return dialogFactory; }

	/**
	 * Handles runtime errors in headless mode.
	 * 
	 * <p>Logs the error message instead of showing an error dialog.</p>
	 *
	 * @param scope the scope in which the error occurred
	 * @param g the runtime exception to handle
	 */
	@Override
	public void runtimeError(final IScope scope, final GamaRuntimeException g) {
		logger.log("Runtime error: " + g.getMessage());
	}

	/**
	 * Creates a display surface for headless rendering.
	 * 
	 * <p>In headless mode, this creates an image-based display surface that can
	 * render to memory without a GUI. This allows simulations to generate images,
	 * take snapshots, and produce visual output even in headless mode.</p>
	 *
	 * @param output the display output to create a surface for
	 * @param uiComponent the UI component (ignored in headless mode)
	 * @return an image-based display surface, or NULL if no creator is available
	 */
	@Override
	public IDisplaySurface createDisplaySurfaceFor(final IOutput.Display output, final Object uiComponent) {
		final IDisplayCreator creator = GamaAdditionRegistry.getDisplay("image");
		if (creator == null) return IDisplaySurface.NULL;
		IDisplaySurface surface = creator.create(output, null);
		surface.outputReloaded();
		return surface;
	}

	/**
	 * Gets a display creator description.
	 * 
	 * <p>Returns a minimal display description for the specified display type.</p>
	 *
	 * @param name the display type name
	 * @return a display description
	 */
	@Override
	public IDisplayCreator getDisplayDescriptionFor(final String name) {
		return new DisplayDescription(null, null, "display", "gama.core");
	}

	/**
	 * Interface for logging in headless mode.
	 * 
	 * <p>This functional interface defines a simple logging mechanism for headless
	 * mode. Implementations can direct log output to files, console, remote logging
	 * systems, or any other destination.</p>
	 * 
	 * <h2>Usage Example:</h2>
	 * <pre>{@code
	 * NullGuiHandler handler = new NullGuiHandler();
	 * handler.setHeadlessLogger(message -> System.err.println(message));
	 * }</pre>
	 */
	public interface IHeadlessLogger {

		/**
		 * Logs a message in headless mode.
		 * 
		 * <p>Implementations should handle the message appropriately for the
		 * deployment environment (e.g., write to file, send to logging service,
		 * print to console).</p>
		 *
		 * @param message the message to log
		 */
		void log(String message);
	}

	/**
	 * Sets the logger for headless mode.
	 * 
	 * <p>This allows customizing where messages are logged in headless mode.
	 * By default, messages are logged using DEBUG.LOG, but this can be changed
	 * to log to files, remote services, or other destinations.</p>
	 *
	 * @param logger the new headless logger to use
	 */
	public void setHeadlessLogger(final IHeadlessLogger logger) { this.logger = logger; }

	/**
	 * Gets the console listener for headless mode.
	 * 
	 * <p>Lazily creates a console listener that logs messages to the headless
	 * logger instead of displaying them in a GUI console.</p>
	 *
	 * @return the headless console listener
	 */
	@Override
	public IConsoleListener getConsole() {
		if (console == null) { console = (s, root, color) -> logger.log(s); }
		return console;
	}

	/**
	 * Runs a task in headless mode.
	 * 
	 * <p>Executes the task either synchronously on the calling thread or
	 * asynchronously on a new thread, depending on the asynchronous parameter.</p>
	 *
	 * @param taskName the name of the task (used for logging/debugging)
	 * @param opener the task to execute
	 * @param asynchronous if true, runs on a new thread; if false, runs on calling thread
	 */
	@Override
	public void run(final String taskName, final Runnable opener, final boolean asynchronous) {
		if (opener != null) {
			if (asynchronous) {
				new Thread(opener).start();
			} else {
				opener.run();
			}
		}
	}

	/**
	 * Exits the application in headless mode.
	 * 
	 * <p>Terminates the Java Virtual Machine with exit code 0.</p>
	 */
	@Override
	public void exit() {
		System.exit(0);
	}

	/**
	 * Displays test results in headless mode.
	 * 
	 * <p>Logs the test summary instead of displaying it in a GUI.</p>
	 *
	 * @param scope the scope in which tests ran
	 * @param summary the test results summary
	 */
	@Override
	public void displayTestsResults(final IScope scope, final CompoundSummary<?, ?> summary) {
		logger.log(summary.toString());
	}

	/**
	 * Gets the mouse location in model coordinates.
	 * 
	 * <p>In headless mode, there is no mouse, so this returns a null point.</p>
	 *
	 * @return a null point (coordinates are meaningless in headless mode)
	 */
	@Override
	public IPoint getMouseLocationInModel() { return GamaPointFactory.getNullPoint(); }

	/**
	 * Gets the mouse location in display coordinates.
	 * 
	 * <p>In headless mode, there is no mouse, so this returns a null point.</p>
	 *
	 * @return a null point (coordinates are meaningless in headless mode)
	 */
	@Override
	public IPoint getMouseLocationInDisplay() { return GamaPointFactory.getNullPoint(); }

	/**
	 * Gets the server commands available in headless mode.
	 * 
	 * <p>In basic headless mode, no server commands are available. Returns an
	 * empty map.</p>
	 *
	 * @return an empty map of server commands
	 */
	@Override
	public Map<String, ISocketCommand> getServerCommands() { return Collections.EMPTY_MAP; }

}
