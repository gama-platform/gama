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
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IFont;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IParameter;
import gama.api.runtime.scope.IScope;
import gama.api.ui.displays.DisplayDescription;
import gama.api.ui.displays.IDisplayCreator;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.utils.server.ISocketCommand;
import gama.api.utils.tests.CompoundSummary;
import gama.dev.DEBUG;

/**
 * The listener interface for receiving headless events. The class that is interested in processing a headless event
 * implements this interface, and the object created with that class is registered with a component using the
 * component's <code>addHeadlessListener<code> method. When the headless event occurs, that object's appropriate method
 * is invoked.
 *
 * @see HeadlessEvent
 */
public class NullGuiHandler implements IGui {

	/** The console. */
	protected IConsoleListener console = null;

	/** The logger. */
	private IHeadlessLogger logger = DEBUG::LOG;

	/** The dialog factory. */
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
	 * Open user input dialog.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param parameters
	 *            the parameters
	 * @param font
	 *            the font
	 * @param color
	 *            the color
	 * @param showTitle
	 *            the show title
	 * @return the map
	 */
	@Override
	public Map<String, Object> openUserInputDialog(final IScope scope, final String title,
			final List<IParameter> parameters, final IFont font, final IColor color, final Boolean showTitle) {
		return null;
	}

	/**
	 * Open wizard.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param finish
	 *            the finish
	 * @param pages
	 *            the pages
	 * @return the i map
	 */
	@Override
	public IMap<String, IMap<String, Object>> openWizard(final IScope scope, final String title,
			final IActionDescription finish, final IList<IMap<String, Object>> pages) {
		return null;
	}

	@Override
	public IDialogFactory getDialogFactory() { return dialogFactory; }

	@Override
	public void runtimeError(final IScope scope, final GamaRuntimeException g) {
		logger.log("Runtime error: " + g.getMessage());
	}

	/**
	 * Creates the display surface for.
	 *
	 * @param output
	 *            the output
	 * @param objects
	 *            the objects
	 * @return the i display surface
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
	 * Method getDisplayDescriptionFor()
	 *
	 * @see gama.api.ui.IGui#getDisplayDescriptionFor(java.lang.String)
	 */
	@Override
	public IDisplayCreator getDisplayDescriptionFor(final String name) {
		return new DisplayDescription(null, null, "display", "gama.core");
	}

	/**
	 * The Interface IHeadlessLogger.
	 */
	public interface IHeadlessLogger {

		/**
		 * Log.
		 *
		 * @param message
		 *            the message
		 */
		void log(String message);
	}

	/**
	 * Sets the headless logger.
	 *
	 * @param logger
	 *            the new headless logger
	 */
	public void setHeadlessLogger(final IHeadlessLogger logger) { this.logger = logger; }

	@Override
	public IConsoleListener getConsole() {
		if (console == null) { console = (s, root, color) -> logger.log(s); }
		return console;
	}

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

	@Override
	public void exit() {
		System.exit(0);
	}

	@Override
	public void displayTestsResults(final IScope scope, final CompoundSummary<?, ?> summary) {
		logger.log(summary.toString());
	}

	@Override
	public IPoint getMouseLocationInModel() { return GamaPointFactory.getNullPoint(); }

	@Override
	public IPoint getMouseLocationInDisplay() { return GamaPointFactory.getNullPoint(); }

	@Override
	public Map<String, ISocketCommand> getServerCommands() { return Collections.EMPTY_MAP; }

}
