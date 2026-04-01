/*******************************************************************************************************
 *
 * ConsoleDisplayerFactory.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.factories;

import static gama.ui.shared.utils.ViewsHelper.hideView;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import gama.api.GAMA;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.ui.IConsoleListener;
import gama.api.ui.IGamaView;
import gama.api.ui.IGui;
import gama.api.ui.IGamaView.Console;
import gama.api.ui.IStatusMessage;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.shared.utils.ViewsHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * A factory for creating ConsoleDisplayer objects.
 */
public class ConsoleDisplayerFactory extends AbstractServiceFactory {

	/** The displayer. */
	IConsoleListener displayer = new ConsoleDisplayer();

	/**
	 * The Class ConsoleDisplayer.
	 */
	static class ConsoleDisplayer implements IConsoleListener {

		/** The console buffers. */
		Map<IColor, StringBuilder> consoleBuffers = new HashMap<>();

		@Override
		public void informConsole(final String msg, final ITopLevelAgent root, final IColor color) {
			// Forward the first non-empty line to the status bar so it is visible
			// even before the console view has opened (e.g. during experiment launch).
			if (msg != null && !msg.isBlank()) {
				final String firstLine = msg.lines().filter(l -> !l.isBlank()).findFirst().orElse(null);
				if (firstLine != null) {
					final var status = GAMA.getGui().getStatus();
					if (status != null) { status.informStatus(firstLine, IStatusMessage.SIMULATION_ICON); }
				}
			}
			IGamaView.Console[] console = new IGamaView.Console[1];
			try {
				console[0] = (Console) ViewsHelper.findView(IGui.CONSOLE_VIEW_ID, null, true);
			} catch (final ConcurrentModificationException e) {
				// See Issue #2812. With concurrent views opening, the view might be impossible to find
			}
			if (console[0] == null) {
				WorkbenchHelper.run(() -> {
					if (!PerspectiveHelper.showConsoles()) return;
					console[0] = (Console) GAMA.getGui().showView(null, IGui.CONSOLE_VIEW_ID, null,
							IWorkbenchPage.VIEW_ACTIVATE);
				});
			}
			if (console[0] != null) {
				console[0].append(msg, root, color);
			} else { // DO WE KEEP THIS ? NOT HAVING BUFFERS MEANS THAT IF A CONSOLE IS OPENED AFTERWARDS, NOTHING WILL
				// APPEAR ON IT
				IColor c = color == null ? root == null ? GamaColorFactory.get(0) : root.getColor() : color;
				StringBuilder sb = consoleBuffers.get(c);
				if (sb == null) {
					sb = new StringBuilder(2000);
					consoleBuffers.put(c, sb);
				}
				sb.append(msg);
			}

		}

		@Override
		public void eraseConsole(final boolean setToNull) {
			final IGamaView console = (IGamaView) ViewsHelper.findView(IGui.CONSOLE_VIEW_ID, null, false);
			if (console != null) { WorkbenchHelper.run(() -> console.reset()); }
			consoleBuffers.clear();
		}

		/**
		 * Toogle console views.
		 *
		 * @param agent
		 *            the agent
		 */
		@Override
		public void toggleConsoleViews(final ITopLevelAgent agent, final boolean show) {
			if (!show) {
				hideView(IGui.CONSOLE_VIEW_ID);
				hideView(IGui.INTERACTIVE_CONSOLE_VIEW_ID);
			} else {
				// Use VIEW_VISIBLE (not VIEW_ACTIVATE) for both console views so they are shown without being
				// activated. VIEW_ACTIVATE causes an immediate render + focus switch that produces a visible
				// flash before the display views have been placed by ArrangeDisplayViews.
				GAMA.getGui().showView(null, IGui.INTERACTIVE_CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
				final IGamaView.Console console = (Console) GAMA.getGui().showView(null, IGui.CONSOLE_VIEW_ID, null,
						IWorkbenchPage.VIEW_VISIBLE);
				consoleBuffers.forEach((c, sb) -> {
					if (sb.length() > 0 && console != null) {
						console.append(sb.toString(), agent, c);
						sb.setLength(0);
					}
				});
			}
		}

	}

	@SuppressWarnings ("rawtypes")
	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return displayer;
	}

}
