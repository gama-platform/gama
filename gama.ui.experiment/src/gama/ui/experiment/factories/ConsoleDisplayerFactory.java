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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import gama.api.GAMA;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.ui.ConsoleEvent;
import gama.api.ui.IConsoleListener;
import gama.api.ui.IGamaView;
import gama.api.ui.IGui;
import gama.api.ui.IGamaView.Console;
import gama.api.ui.IStatusMessage;
import gama.api.utils.prefs.GamaPreferences;
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

		private static final String TRUNCATION_MARKER = "(...)\n";
		private final Object bufferLock = new Object();
		private final Deque<BufferedConsoleMessage> consoleBuffer = new ArrayDeque<>();
		private int bufferedCharacters;
		private String bufferTruncationMarker = "";
		private boolean replayingBuffer;
		private boolean openRequested;
		private long lastFailedOpenAttempt;
		private static final long OPEN_RETRY_DELAY = 1000L;

		private record BufferedConsoleMessage(ConsoleEvent event, String text) {}

		@Override
		public void informConsole(final String msg, final ITopLevelAgent root, final IColor color) {
			accept(ConsoleEvent.info(msg, root, color));
		}

		@Override
		public void debugConsole(final int cycle, final String msg, final ITopLevelAgent root, final IColor color) {
			accept(ConsoleEvent.debug(cycle, msg, root, color));
		}

		@Override
		public void accept(final ConsoleEvent event) {
			final String msg = displayMessage(event);
			if (msg == null || msg.isEmpty()) return;
			// Forward the first non-empty line to the status bar so it is visible
			// even before the console view has opened (e.g. during experiment launch).
			if (msg != null && !msg.isBlank()) {
				final String firstLine = msg.lines().filter(l -> !l.isBlank()).findFirst().orElse(null);
				if (firstLine != null) {
					final var status = GAMA.getGui().getStatus();
					if (status != null) { status.informStatus(firstLine, IStatusMessage.SIMULATION_ICON); }
				}
			}
			synchronized (bufferLock) {
				if (replayingBuffer) {
					bufferConsoleMessage(event.withColor(colorFor(event.agent(), event.color())), msg);
					return;
				}
			}
			IGamaView.Console[] console = new IGamaView.Console[1];
			try {
				console[0] = (Console) ViewsHelper.findView(IGui.CONSOLE_VIEW_ID, null, true);
			} catch (final ConcurrentModificationException e) {
				// See Issue #2812. With concurrent views opening, the view might be impossible to find
			}
			synchronized (bufferLock) {
				if (console[0] == null) {
					// Never block the producing (simulation) thread on the UI thread: buffer the message and
					// schedule at most one asynchronous attempt to open the console.
					bufferConsoleMessage(event.withColor(colorFor(event.agent(), event.color())), msg);
					if (!openRequested && System.currentTimeMillis() - lastFailedOpenAttempt >= OPEN_RETRY_DELAY) {
						openRequested = true;
						WorkbenchHelper.asyncRun(this::openConsoleAndReplay);
					}
					return;
				}
				if (replayingBuffer) {
					bufferConsoleMessage(event.withColor(colorFor(event.agent(), event.color())), msg);
					return;
				}
				if (consoleBuffer.isEmpty()) {
					console[0].append(msg, event.agent(), event.color());
					return;
				}
				// The console has been opened by other means while messages were buffered: replay them first
				bufferConsoleMessage(event.withColor(colorFor(event.agent(), event.color())), msg);
				replayingBuffer = true;
			}
			replayBufferedMessages(console[0]);
		}

		private void openConsoleAndReplay() {
			IGamaView.Console console = null;
			try {
				if (PerspectiveHelper.showConsoles()) {
					console = (Console) GAMA.getGui().showView(null, IGui.CONSOLE_VIEW_ID, null,
							IWorkbenchPage.VIEW_ACTIVATE);
				}
			} finally {
				synchronized (bufferLock) {
					openRequested = false;
					lastFailedOpenAttempt = console == null ? System.currentTimeMillis() : 0L;
					if (console == null || replayingBuffer) return;
					replayingBuffer = true;
				}
			}
			replayBufferedMessages(console);
		}

		private static String displayMessage(final ConsoleEvent event) {
			return event.level() == ConsoleEvent.Level.DEBUG && event.cycle() != null
					? "(cycle: " + event.cycle() + ") " + event.message() : event.message();
		}

		private static IColor colorFor(final ITopLevelAgent root, final IColor color) {
			return color == null ? root == null ? GamaColorFactory.get(0) : root.getColor() : color;
		}

		private void bufferConsoleMessage(final ConsoleEvent event, final String text) {
			final int limit = getBufferLimit();
			if (limit == 0) return;
			consoleBuffer.addLast(new BufferedConsoleMessage(event, text));
			bufferedCharacters += text.length();
			if (limit < 0) return;
			if (bufferTruncationMarker.isEmpty() && bufferedCharacters <= limit) return;
			if (bufferTruncationMarker.isEmpty()) { bufferTruncationMarker = truncationMarker(limit); }
			final int retainedLimit = Math.max(0, limit - bufferTruncationMarker.length());
			while (bufferedCharacters > retainedLimit && !consoleBuffer.isEmpty()) {
				final BufferedConsoleMessage first = consoleBuffer.removeFirst();
				final int excess = bufferedCharacters - retainedLimit;
				if (first.text().length() > excess) {
					final String suffix = first.text().substring(excess);
					consoleBuffer.addFirst(new BufferedConsoleMessage(first.event(), suffix));
					bufferedCharacters -= excess;
				} else {
					bufferedCharacters -= first.text().length();
				}
			}
		}

		private static int getBufferLimit() {
			int limit = GamaPreferences.Interface.CORE_CONSOLE_BUFFER.getValue();
			final int displayLimit = GamaPreferences.Interface.CORE_CONSOLE_SIZE.getValue();
			if (displayLimit > -1) { limit = limit == -1 ? displayLimit : Math.min(limit, displayLimit); }
			return limit;
		}

		private static String truncationMarker(final int limit) {
			return limit < TRUNCATION_MARKER.length() ? TRUNCATION_MARKER.substring(0, limit)
					: TRUNCATION_MARKER;
		}

		private void replayBufferedMessages(final IGamaView.Console console) {
			while (true) {
				final List<BufferedConsoleMessage> batch;
				final String truncationMarker;
				synchronized (bufferLock) {
					if (consoleBuffer.isEmpty()) {
						replayingBuffer = false;
						return;
					}
					batch = new ArrayList<>(consoleBuffer);
					consoleBuffer.clear();
					bufferedCharacters = 0;
					truncationMarker = bufferTruncationMarker;
					bufferTruncationMarker = "";
				}
				if (!truncationMarker.isEmpty()) {
					console.append(truncationMarker, null, GamaColorFactory.get(0));
				}
				for (final BufferedConsoleMessage message : batch) {
					console.append(message.text(), message.event().agent(), message.event().color());
				}
			}
		}

		@Override
		public void eraseConsole(final boolean setToNull) {
			final IGamaView console = (IGamaView) ViewsHelper.findView(IGui.CONSOLE_VIEW_ID, null, false);
			if (console != null) { WorkbenchHelper.run(() -> console.reset()); }
			synchronized (bufferLock) {
				consoleBuffer.clear();
				bufferedCharacters = 0;
				bufferTruncationMarker = "";
			}
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
				synchronized (bufferLock) { replayingBuffer = true; }
				// Use VIEW_VISIBLE (not VIEW_ACTIVATE) for both console views so they are shown without being
				// activated. VIEW_ACTIVATE causes an immediate render + focus switch that produces a visible
				// flash before the display views have been placed by ArrangeDisplayViews.
				GAMA.getGui().showView(null, IGui.INTERACTIVE_CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
				final IGamaView.Console console = (Console) GAMA.getGui().showView(null, IGui.CONSOLE_VIEW_ID, null,
						IWorkbenchPage.VIEW_VISIBLE);
				if (console == null) {
					synchronized (bufferLock) { replayingBuffer = false; }
				} else {
					replayBufferedMessages(console);
				}
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
