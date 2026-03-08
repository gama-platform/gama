/*******************************************************************************************************
 *
 * IGamaView.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import gama.api.gaml.statements.IStatement;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.geometry.IPoint;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.utils.interfaces.ITopLevelAgentChangeListener;
import gama.api.utils.tests.CompoundSummary;

/**
 * An abstract representation of views in the GAMA user interface.
 * 
 * <p>This interface defines the contract for UI views that display outputs or present
 * information to users. A view can display one or several outputs (e.g., multiple monitors
 * in a monitor view) and manages the lifecycle and updating of these outputs.</p>
 * 
 * <h2>Main Responsibilities:</h2>
 * <ul>
 *   <li>Manage output lifecycle (add, remove, update)</li>
 *   <li>Control view visibility and naming</li>
 *   <li>Handle view updates and toolbar state</li>
 *   <li>Respond to simulation changes</li>
 * </ul>
 * 
 * <h2>View Types:</h2>
 * <p>This interface has several specialized sub-interfaces for specific view types:</p>
 * <ul>
 *   <li>{@link IGamaView.Display} - For graphical display views</li>
 *   <li>{@link IGamaView.Console} - For console output views</li>
 *   <li>{@link IGamaView.Parameters} - For parameter editing views</li>
 *   <li>{@link IGamaView.Test} - For test result views</li>
 *   <li>{@link IGamaView.Error} - For error display views</li>
 *   <li>{@link IGamaView.Html} - For HTML/web content views</li>
 *   <li>{@link IGamaView.User} - For user-defined panel views</li>
 * </ul>
 * 
 * <h2>Lifecycle:</h2>
 * <p>Views manage their outputs through the following lifecycle:</p>
 * <ol>
 *   <li>Output is added via {@link #addOutput(IOutput)}</li>
 *   <li>View updates when {@link #update(IOutput)} is called</li>
 *   <li>Output is removed via {@link #removeOutput(IOutput)}</li>
 *   <li>View closes via {@link #close(IScope)}</li>
 * </ol>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * IGamaView view = gui.showView(scope, "gama.ui.view.MonitorView", "Monitors", 0);
 * view.addOutput(monitorOutput);
 * view.update(monitorOutput);
 * }</pre>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 */
public interface IGamaView {

	/**
	 * Marker interface for views that interact with top-level agent changes.
	 * 
	 * <p>Views implementing this interface will be notified when the active top-level
	 * agent (simulation or experiment) changes, allowing them to update their content
	 * accordingly.</p>
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 13 août 2023
	 */
	public interface Interactive extends ITopLevelAgentChangeListener {}

	/**
	 * Updates the view with new data from the specified output.
	 * 
	 * <p>This method is called when an output needs to refresh its display. The view
	 * should update its UI elements to reflect the current state of the output.</p>
	 *
	 * @param output the output that has new data to display
	 */
	void update(IOutput output);

	/**
	 * Adds an output to this view.
	 * 
	 * <p>Multiple outputs can be added to the same view (e.g., multiple monitors
	 * in a monitor view). The view becomes responsible for managing and displaying
	 * this output.</p>
	 *
	 * @param output the output to add
	 */
	void addOutput(IOutput output);

	/**
	 * Removes an output from this view.
	 * 
	 * <p>The view should clean up any UI elements associated with this output and
	 * stop displaying its data.</p>
	 *
	 * @param output the output to remove
	 */
	void removeOutput(IOutput output);

	/**
	 * Gets the primary output associated with this view.
	 * 
	 * <p>If the view displays multiple outputs, this returns the primary or first
	 * output. If the view displays a single output, this returns that output.</p>
	 *
	 * @return the primary output, or null if no output is associated
	 */
	IOutput getOutput();

	/**
	 * Closes this view.
	 * 
	 * <p>The view should clean up resources, remove all outputs, and prepare for
	 * disposal. After closing, the view should not be used again.</p>
	 *
	 * @param scope the scope in which the view is being closed
	 */
	void close(IScope scope);

	/**
	 * Updates the view's title to include simulation-specific information.
	 * 
	 * <p>This method is typically called when a simulation starts or when switching
	 * between multiple simulations, allowing the view to display which simulation
	 * it is showing data for.</p>
	 *
	 * @param agent the simulation agent whose name should be included in the title
	 */
	void changePartNameWithSimulation(ISimulationAgent agent);

	/**
	 * Resets the view to its initial state.
	 * 
	 * <p>This method is typically called when starting a new simulation or when
	 * the user explicitly requests a reset. The view should clear any accumulated
	 * data and return to a clean state.</p>
	 */
	void reset();

	/**
	 * Gets the current name/title of this view part.
	 * 
	 * <p>The part name is displayed in the view's tab or title bar in the UI.</p>
	 *
	 * @return the current part name
	 */
	String getPartName();

	/**
	 * Sets the name/title of this view part.
	 * 
	 * <p>This updates the text displayed in the view's tab or title bar.</p>
	 *
	 * @param name the new name for the view
	 */
	void setName(String name);

	/**
	 * Updates the toolbar state for this view.
	 * 
	 * <p>This method refreshes the enabled/disabled state of toolbar buttons based
	 * on the current state of the view and its outputs.</p>
	 */
	void updateToolbarState();

	/**
	 * Shows or hides the toolbar for this view.
	 * 
	 * <p>This method controls the visibility of the view's toolbar, which typically
	 * contains action buttons and controls.</p>
	 *
	 * @param show true to show the toolbar, false to hide it
	 */
	void showToolbar(boolean show);

	/**
	 * Interface for test result views.
	 * 
	 * <p>Test views display the results of running GAML tests, showing passed/failed
	 * tests and progress information.</p>
	 */
	public interface Test {

		/**
		 * Adds a test result to the view.
		 * 
		 * <p>This method is called when a test completes, providing a summary of
		 * the test execution results.</p>
		 *
		 * @param summary the compound summary containing test results
		 */
		void addTestResult(final CompoundSummary<?, ?> summary);

		/**
		 * Starts a new test sequence.
		 * 
		 * <p>This method is called before running a series of tests, allowing the
		 * view to prepare for displaying results.</p>
		 *
		 * @param all true if running all tests, false if running a subset
		 */
		void startNewTestSequence(boolean all);

		/**
		 * Displays progress for the current test sequence.
		 * 
		 * <p>This method updates a progress indicator showing how many tests have
		 * completed out of the total number of tests.</p>
		 *
		 * @param number the number of tests completed so far
		 * @param total the total number of tests to run
		 */
		void displayProgress(int number, int total);

		/**
		 * Finishes the current test sequence.
		 * 
		 * <p>This method is called after all tests have completed, allowing the view
		 * to finalize the display and show summary information.</p>
		 */
		void finishTestSequence();

	}

	/**
	 * Interface for display views that show graphical simulation outputs.
	 * 
	 * <p>Display views are the most common type of view in GAMA, showing 2D or 3D
	 * graphical representations of the simulation. They manage a display surface
	 * that renders layers, agents, and other graphical elements.</p>
	 * 
	 * <h2>Main Responsibilities:</h2>
	 * <ul>
	 *   <li>Manage the display surface for rendering</li>
	 *   <li>Handle full-screen mode toggling</li>
	 *   <li>Control overlay visibility</li>
	 *   <li>Capture snapshots of the display</li>
	 *   <li>Manage display indexing for multiple displays</li>
	 * </ul>
	 */
	public interface Display extends IGamaView {

		/**
		 * Interface for components contained within a display view.
		 * 
		 * <p>Inner components can retrieve their parent display view for coordination
		 * and context.</p>
		 */
		public interface InnerComponent {
			/**
			 * Gets the display view that contains this component.
			 *
			 * @return the parent display view
			 */
			Display getView();
		}

		/**
		 * Checks if the display contains the specified point.
		 * 
		 * <p>This method is used for hit testing and determining if mouse or touch
		 * events should be processed by this display.</p>
		 *
		 * @param x the x coordinate in screen/view coordinates
		 * @param y the y coordinate in screen/view coordinates
		 * @return true if the point is within the display bounds, false otherwise
		 */
		boolean containsPoint(int x, int y);

		/**
		 * Gets the display surface used for rendering.
		 * 
		 * <p>The display surface is the core rendering component that manages layers,
		 * camera, graphics context, and all rendering operations.</p>
		 *
		 * @return the display surface
		 */
		IDisplaySurface getDisplaySurface();

		/**
		 * Toggles full-screen mode for this display.
		 * 
		 * <p>In full-screen mode, the display occupies the entire screen and hides
		 * other UI elements. Calling this method when in full-screen mode returns
		 * to normal mode, and vice versa.</p>
		 */
		void toggleFullScreen();

		/**
		 * Checks if the display is currently in full-screen mode.
		 *
		 * @return true if in full-screen mode, false otherwise
		 */
		boolean isFullScreen();

		/**
		 * Toggles the visibility of the overlay.
		 * 
		 * <p>The overlay typically displays information such as scale, coordinates,
		 * FPS, and other metadata about the display.</p>
		 */
		void toggleOverlay();

		/**
		 * Shows or hides the overlay.
		 * 
		 * <p>The overlay typically displays information such as scale, coordinates,
		 * FPS, and other metadata about the display.</p>
		 *
		 * @param show true to show the overlay, false to hide it
		 */
		void showOverlay(boolean show);

		/**
		 * Gets the index of this display.
		 * 
		 * <p>When multiple displays are open, each has a unique index used for
		 * identification and ordering purposes.</p>
		 *
		 * @return the display index
		 */
		int getIndex();

		/**
		 * Sets the index of this display.
		 *
		 * @param i the new display index
		 */
		void setIndex(int i);

		/**
		 * Takes a snapshot (screenshot) of the display contents.
		 * 
		 * <p>This method captures the current state of the display as an image,
		 * which can be saved to a file. Custom dimensions can be specified to
		 * resize the snapshot.</p>
		 *
		 * @param customDimensions the desired dimensions for the snapshot, or null
		 *                         to use the current display dimensions
		 */
		void takeSnapshot(IPoint customDimensions);

		/**
		 * Hides the rendering canvas.
		 * 
		 * <p>The default implementation does nothing. Override to implement canvas
		 * hiding behavior.</p>
		 */
		default void hideCanvas() {}

		/**
		 * Shows the rendering canvas.
		 * 
		 * <p>The default implementation does nothing. Override to implement canvas
		 * showing behavior.</p>
		 */
		default void showCanvas() {}

		/**
		 * Gives focus to the rendering canvas.
		 * 
		 * <p>This ensures the canvas receives keyboard and mouse events. The default
		 * implementation does nothing. Override to implement canvas focus behavior.</p>
		 */
		default void focusCanvas() {}

		/**
		 * Checks if this display view is on a HiDPI (high-resolution) monitor.
		 * 
		 * <p>HiDPI monitors may require special handling for proper scaling and
		 * rendering. This method helps determine if such adjustments are needed.</p>
		 *
		 * @return true if on a HiDPI monitor, false otherwise
		 */
		boolean isHiDPI();

		/**
		 * Checks if this is a 2D display (as opposed to 3D/OpenGL).
		 *
		 * @return true if this is a 2D display, false if it is a 3D display
		 */
		boolean is2D();

		/**
		 * Checks if the ESC key has been redefined for this display.
		 * 
		 * <p>By default, ESC exits full-screen mode. This method indicates if the
		 * ESC key has been assigned a different behavior.</p>
		 *
		 * @return true if ESC is redefined, false if it has default behavior
		 */
		boolean isEscRedefined();
	}

	/**
	 * Interface for error display views.
	 * 
	 * <p>Error views display runtime errors, warnings, and other diagnostic messages
	 * that occur during model compilation or execution.</p>
	 */
	public interface Error {

		/**
		 * Displays errors in the view.
		 * 
		 * <p>This method updates the error view with current error information. If
		 * reset is true, existing errors are cleared before displaying new ones.</p>
		 *
		 * @param reset true to clear existing errors first, false to append new errors
		 */
		void displayErrors(boolean reset);

	}

	/**
	 * Interface for HTML content views.
	 * 
	 * <p>HTML views display web content, documentation, or HTML-formatted information
	 * within the GAMA interface.</p>
	 */
	public interface Html {

		/**
		 * Sets the URL to display in the HTML view.
		 * 
		 * <p>The view will load and render the content from the specified URL,
		 * which can be a web URL or a local file URL.</p>
		 *
		 * @param url the URL to display
		 */
		void setUrl(String url);
	}

	/**
	 * Interface for parameter editor views.
	 * 
	 * <p>Parameter views allow users to view and edit experiment and simulation
	 * parameters. They respond to changes in the active simulation and can update
	 * their displayed values.</p>
	 */
	public interface Parameters extends ITopLevelAgentChangeListener {

		/**
		 * Updates the item values displayed in the parameter view.
		 * 
		 * <p>This method refreshes the parameter display with current values from
		 * the model. It can optionally retrieve fresh values from variables and can
		 * run synchronously or asynchronously.</p>
		 * 
		 * @param synchronously if true, update runs on the calling thread; if false,
		 *                      update may run asynchronously
		 * @param retrieveValues if true, retrieves updated values from variables;
		 *                       if false, uses cached values
		 */
		void updateItemValues(boolean synchronously, boolean retrieveValues);
	}

	/**
	 * Interface for console views.
	 * 
	 * <p>Console views display text output from the simulation, including debug
	 * messages, print statements, and other logging information.</p>
	 */
	public interface Console {

		/**
		 * Appends a message to the console.
		 * 
		 * <p>Messages can be color-coded and associated with specific simulations
		 * to help distinguish output from different sources.</p>
		 *
		 * @param msg the message to append
		 * @param root the top-level agent that generated the message
		 * @param color the color to use for the message, or null for default color
		 */
		void append(String msg, ITopLevelAgent root, IColor color);

	}

	/**
	 * Interface for user-defined panel views.
	 * 
	 * <p>User panel views display custom UI panels defined in GAML models,
	 * allowing models to create specialized user interfaces for interaction.</p>
	 */
	public interface User {
		/**
		 * Initializes the user panel for the given scope and GAML panel statement.
		 * 
		 * <p>This method sets up the user interface elements defined in the GAML
		 * panel and prepares them for user interaction.</p>
		 *
		 * @param scope the scope in which the panel operates
		 * @param panel the GAML statement defining the panel structure and behavior
		 */
		void initFor(final IScope scope, final IStatement panel);
	}

	/**
	 * Checks if the view is currently visible in the UI.
	 * 
	 * <p>A view may be hidden when the user closes its tab or when it is minimized.
	 * This method allows checking the visibility state.</p>
	 *
	 * @return true if the view is visible, false if it is hidden or closed
	 */
	boolean isVisible();

}
