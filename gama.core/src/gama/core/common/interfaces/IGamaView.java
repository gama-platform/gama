/*******************************************************************************************************
 *
 * IGamaView.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.outputs.IOutput;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.gaml.architecture.user.UserPanelStatement;
import gama.gaml.statements.test.CompoundSummary;

/**
 * An abstract representation of the 'views', in a UI sense, that are used to display outputs or present information to
 * the user. A view can display one or several outputs (for instance, several monitors)
 *
 * @author drogoul
 */
public interface IGamaView {

	/**
	 * The Interface Interactive.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 13 août 2023
	 */
	public interface Interactive extends ITopLevelAgentChangeListener {}

	/**
	 * Update.
	 *
	 * @param output
	 *            the output
	 */
	void update(IOutput output);

	/**
	 * Adds the output.
	 *
	 * @param output
	 *            the output
	 */
	void addOutput(IOutput output);

	/**
	 * Removes the output.
	 *
	 * @param putput
	 *            the putput
	 */
	void removeOutput(IOutput putput);

	/**
	 * Gets the output.
	 *
	 * @return the output
	 */
	IOutput getOutput();

	/**
	 * Close.
	 *
	 * @param scope
	 *            the scope
	 */
	void close(IScope scope);

	/**
	 * Change part name with simulation.
	 *
	 * @param agent
	 *            the agent
	 */
	void changePartNameWithSimulation(SimulationAgent agent);

	/**
	 * Reset.
	 */
	void reset();

	/**
	 * Gets the part name.
	 *
	 * @return the part name
	 */
	String getPartName();

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	void setName(String name);

	/**
	 * Update toolbar state.
	 */
	void updateToolbarState();

	/**
	 * Show toolbar.
	 */
	void showToolbar(boolean show);

	/**
	 * The Interface Test.
	 */
	public interface Test {

		/**
		 * Adds the test result.
		 *
		 * @param summary
		 *            the summary
		 */
		void addTestResult(final CompoundSummary<?, ?> summary);

		/**
		 * Start new test sequence.
		 *
		 * @param all
		 *            the all
		 */
		void startNewTestSequence(boolean all);

		/**
		 * Display progress.
		 *
		 * @param number
		 *            the number
		 * @param total
		 *            the total
		 */
		void displayProgress(int number, int total);

		/**
		 * Finish test sequence.
		 */
		void finishTestSequence();

	}

	/**
	 * The Interface Display.
	 */
	public interface Display extends IGamaView {

		/**
		 * The Interface InnerComponent.
		 */
		public interface InnerComponent {
			/**
			 * Gets the view.
			 *
			 * @return the view
			 */
			Display getView();
		}

		/**
		 * Contains point.
		 *
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @return true, if successful
		 */
		boolean containsPoint(int x, int y);

		/**
		 * Gets the display surface.
		 *
		 * @return the display surface
		 */
		IDisplaySurface getDisplaySurface();

		/**
		 * Toggle full screen.
		 */
		void toggleFullScreen();

		/**
		 * Checks if is full screen.
		 *
		 * @return true, if is full screen
		 */
		boolean isFullScreen();

		/**
		 * Toggle side controls.
		 */
		// void toggleSideControls();

		/**
		 * Toggle overlay.
		 */
		void toggleOverlay();

		/**
		 * Show overlay.
		 */
		void showOverlay(boolean show);

		/**
		 * Gets the output.
		 *
		 * @return the output
		 */
		@Override
		LayeredDisplayOutput getOutput();

		/**
		 * Gets the index.
		 *
		 * @return the index
		 */
		int getIndex();

		/**
		 * Sets the index.
		 *
		 * @param i
		 *            the new index
		 */
		void setIndex(int i);

		/**
		 * Take snapshot. Returns a capture of the display contents in the form of a BufferedImage
		 *
		 * @return
		 */
		void takeSnapshot(GamaPoint customDimensions);

		/**
		 * Hide canvas.
		 */
		default void hideCanvas() {}

		/**
		 * Show canvas.
		 */
		default void showCanvas() {}

		/**
		 * Focus canvas.
		 */
		default void focusCanvas() {}

		/**
		 * Checks if this view belongs to a HIDPI Monitor (i.e. if a zoom is applied).
		 *
		 * @return the boolean
		 */
		boolean isHiDPI();

		/**
		 * Checks if is 2d.
		 *
		 * @return true, if is 2d
		 */
		boolean is2D();

		/**
		 * Checks if is esc redefined.
		 *
		 * @return true, if is esc redefined
		 */
		boolean isEscRedefined();
	}

	/**
	 * The Interface Error.
	 */
	public interface Error {

		/**
		 * Display errors.
		 *
		 * @param reset
		 *            the reset
		 */
		void displayErrors(boolean reset);

	}

	/**
	 * The Interface Html.
	 */
	public interface Html {

		/**
		 * Sets the url.
		 *
		 * @param url
		 *            the new url
		 */
		void setUrl(String url);
	}

	/**
	 * The Interface Parameters.
	 */
	public interface Parameters extends ITopLevelAgentChangeListener {

		/**
		 * Update item values.
		 */
		void updateItemValues(boolean synchronously);
	}

	/**
	 * The Interface Console.
	 */
	public interface Console {

		/**
		 * Append.
		 *
		 * @param msg
		 *            the msg
		 * @param root
		 *            the root
		 * @param color
		 *            the color
		 */
		void append(String msg, ITopLevelAgent root, GamaColor color);

	}

	/**
	 * The Interface User.
	 */
	public interface User {
		/**
		 * Inits the for.
		 *
		 * @param scope
		 *            the scope
		 * @param panel
		 *            the panel
		 */
		void initFor(final IScope scope, final UserPanelStatement panel);
	}

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	boolean isVisible();

}
