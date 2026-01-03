/*******************************************************************************************************
 *
 * NullGuiHandler.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime;

import java.util.List;
import java.util.Map;

import gama.core.common.interfaces.IConsoleListener;
import gama.core.common.interfaces.IDialogFactory;
import gama.core.common.interfaces.IDisplayCreator;
import gama.core.common.interfaces.IDisplayCreator.DisplayDescription;
import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.parameters.IParameter;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.outputs.display.NullDisplaySurface;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaFont;
import gama.core.util.IColor;
import gama.core.util.list.IList;
import gama.core.util.map.GamaMapFactory;
import gama.core.util.map.IMap;
import gama.dev.DEBUG;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.statements.test.CompoundSummary;

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

	@Override
	public Map<String, Object> openUserInputDialog(final IScope scope, final String title,
			final List<IParameter> parameters, final GamaFont font, final IColor color, final Boolean showTitle) {
		final Map<String, Object> initialValues = GamaMapFactory.create();
		parameters.forEach(p -> { initialValues.put(p.getName(), p.getInitialValue(scope)); });
		return initialValues;
	}

	@Override
	public IMap<String, IMap<String, Object>> openWizard(final IScope scope, final String title,
			final ActionDescription finish, final IList<IMap<String, Object>> pages) {
		final IMap<String, IMap<String, Object>> initialValues = GamaMapFactory.create();
		for (IMap l : pages) {
			final IMap<String, Object> initialValuesPage = GamaMapFactory.create();
			String t = (String) l.get(IKeyword.TITLE);

			initialValues.put(t, initialValuesPage);
			@SuppressWarnings ("unchecked") IList<IParameter> ps = (IList<IParameter>) l.get(IKeyword.PARAMETERS);
			if (ps != null) { ps.forEach(p -> { initialValuesPage.put(p.getName(), p.getInitialValue(scope)); }); }

		}

		return initialValues;
	}

	@Override
	public IDialogFactory getDialogFactory() { return dialogFactory; }

	@Override
	public void runtimeError(final IScope scope, final GamaRuntimeException g) {
		logger.log("Runtime error: " + g.getMessage());
	}

	@Override
	public IDisplaySurface createDisplaySurfaceFor(final LayeredDisplayOutput output, final Object... objects) {
		final IDisplayCreator creator = DISPLAYS.get("image");
		if (creator == null) return new NullDisplaySurface();
		IDisplaySurface surface = creator.create(output);
		surface.outputReloaded();
		return surface;
	}

	/**
	 * Method getDisplayDescriptionFor()
	 *
	 * @see gama.core.common.interfaces.IGui#getDisplayDescriptionFor(java.lang.String)
	 */
	@Override
	public DisplayDescription getDisplayDescriptionFor(final String name) {
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

}
