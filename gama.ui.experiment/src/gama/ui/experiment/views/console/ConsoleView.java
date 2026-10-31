/*******************************************************************************************************
 *
 * ConsoleView.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.views.console;

import static gama.ui.shared.resources.IGamaIcons.ACTION_CLEAR;
import static gama.ui.shared.resources.IGamaIcons.DISPLAY_TOOLBAR_CSVEXPORT;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.internal.console.IOConsoleViewer;

import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.IGamaView;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.access.ConsoleContextMenuHelper;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.GamaViewPart;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;
import gama.ui.shared.views.toolbar.TextSearchItem;

/**
 * The Class ConsoleView.
 */
public class ConsoleView extends GamaViewPart implements IToolbarDecoratedView.Sizable, IToolbarDecoratedView.Pausable,
		IToolbarDecoratedView.LogExportable, IGamaView.Console {

	static {
		// DEBUG.OFF();
	}

	/** The msg console. */
	private final IOConsole msgConsole;
	private final IPreferenceAfterChangeListener<Integer> sizeListener = this::setCharacterLimit;

	public ConsoleView() {
		this("GAMA Console");
	}

	protected ConsoleView(final String name) {
		msgConsole = new IOConsole(name, null);
	}

	/** The viewer. */
	IOConsoleViewer viewer;

	/** The notification shown when paused output is waiting. */
	private ToolItem newContentsStatus;

	/** The paused. */
	boolean paused = false;

	/** The pause buffer. */
	private final Deque<BufferedMessage> pauseBuffer = new ArrayDeque<>();

	/** Number of buffered characters, excluding the truncation marker. */
	private int pauseBufferLength;

	/** Whether older paused output was discarded to respect the buffer limit. */
	private boolean pauseBufferTruncated;

	private static final String TRUNCATION_MARKER = "(...)\n";
	private String pauseTruncationMarker = "";

	/** The writers. */
	private final HashMap<Color, BufferedWriter> writers = new HashMap<>();

	private record BufferedMessage(String text, GamaUIColor color) {}

	/**
	 * Sets the character limit.
	 *
	 * @param limit
	 *            the new character limit
	 */
	public void setCharacterLimit(final int limit) {
		if (limit == -1) {
			msgConsole.setWaterMarks(-1, -1);
		} else {
			msgConsole.setWaterMarks(limit, limit * 2);
		}
	}

	@Override
	public void ownCreatePartControl(final Composite parent) {
		setCharacterLimit(GamaPreferences.Interface.CORE_CONSOLE_SIZE.getValue());
		GamaPreferences.Interface.CORE_CONSOLE_SIZE.addChangeListener(sizeListener);
		viewer = new IOConsoleViewer(parent, msgConsole);
		viewer.setWordWrap(GamaPreferences.Interface.CORE_CONSOLE_WRAP.getValue());
		ConsoleContextMenuHelper.install(viewer.getTextWidget(), this::reset, false);
		TextSearchItem.bind(this, viewer);
	}

	/**
	 * Gets the writer for.
	 *
	 * @param root
	 *            the root
	 * @param color
	 *            the color
	 * @return the writer for
	 */
	private BufferedWriter getWriterFor(final ITopLevelAgent root, final GamaUIColor color) {
		final Color c = getColorFor(root, color);
		BufferedWriter writer = writers.get(c);
		if (writer == null) {
			final IOConsoleOutputStream stream = msgConsole.newOutputStream();
			stream.setColor(c);
			stream.setActivateOnWrite(false);
			writer = new BufferedWriter(new OutputStreamWriter(stream));
			writers.put(c, writer);
		}
		return writer;
	}

	/**
	 * Gets the color for.
	 *
	 * @param root
	 *            the root
	 * @param requested
	 *            the requested
	 * @return the color for
	 */
	private Color getColorFor(final ITopLevelAgent root, final GamaUIColor requested) {
		final GamaUIColor result =
				requested == null ? root == null ? IGamaColors.BLACK : GamaColors.get(root.getColor()) : requested;
		return ThemeHelper.isDark() ? result.lighter(0.6f) : result.color();
	}

	/** The indicated. */
	private boolean indicated = false;

	/**
	 * Append the text to the console.
	 *
	 * @param text
	 *            to display in the console
	 */

	@Override
	public void append(final String text, final ITopLevelAgent agent, final IColor color) {
		append(text, agent, GamaColors.get(color));
	}

	/**
	 * Append.
	 *
	 * @param text
	 *            the text
	 * @param root
	 *            the root
	 * @param color
	 *            the color
	 */
	public void append(final String text, final ITopLevelAgent root, final GamaUIColor color) {
		if (text == null || text.isEmpty()) return;
		boolean indicate = false;
		synchronized (this) {
			if (!paused) {
				writeToConsole(text, root, color);
			} else {
				bufferPausedMessage(text, color == null && root != null ? GamaColors.get(root.getColor()) : color);
				if (!indicated) {
					indicated = true;
					indicate = true;
				}
			}
		}
		if (indicate) {
			WorkbenchHelper.asyncRun(() -> {
				synchronized (ConsoleView.this) {
					if (!paused || !indicated || toolbar == null || toolbar.isDisposed()) return;
					newContentsStatus = toolbar.button(null, "New contents available",
							GamaIcon.named("navigator/status.info").image(), null, 24, SWT.RIGHT);
					toolbar.requestLayout();
				}
			});
		}
	}

	private void writeToConsole(final String text, final ITopLevelAgent root, final GamaUIColor color) {
		final BufferedWriter writer = getWriterFor(root, color);
		try {
			writer.append(text);
			writer.flush();
		} catch (final IOException e) {
			DEBUG.ERR("Unable to write to the execution console", e);
		}
	}

	private void writeToConsole(final String text, final GamaUIColor color) {
		writeToConsole(text, null, color);
	}

	private void bufferPausedMessage(final String text, final GamaUIColor color) {
		final int limit = getPauseBufferLimit();
		if (limit == 0) return;
		pauseBuffer.addLast(new BufferedMessage(text, color));
		pauseBufferLength += text.length();
		if (limit < 0) return;
		if (!pauseBufferTruncated && pauseBufferLength <= limit) return;
		if (!pauseBufferTruncated) {
			pauseBufferTruncated = true;
			pauseTruncationMarker = truncationMarker(limit);
		}
		final int markerLength = pauseTruncationMarker.length();
		final int retainedLimit = Math.max(0, limit - markerLength);
		while (pauseBufferLength > retainedLimit && !pauseBuffer.isEmpty()) {
			final BufferedMessage first = pauseBuffer.removeFirst();
			final int excess = pauseBufferLength - retainedLimit;
			if (first.text().length() > excess) {
				final String suffix = first.text().substring(excess);
				pauseBuffer.addFirst(new BufferedMessage(suffix, first.color()));
				pauseBufferLength -= excess;
			} else {
				pauseBufferLength -= first.text().length();
			}
		}
	}

	private static int getPauseBufferLimit() {
		int limit = GamaPreferences.Interface.CORE_CONSOLE_BUFFER.getValue();
		final int displayLimit = GamaPreferences.Interface.CORE_CONSOLE_SIZE.getValue();
		if (displayLimit > -1) { limit = limit == -1 ? displayLimit : Math.min(limit, displayLimit); }
		return limit;
	}

	private static String truncationMarker(final int limit) {
		return limit < TRUNCATION_MARKER.length() ? TRUNCATION_MARKER.substring(0, limit) : TRUNCATION_MARKER;
	}

	@Override
	public void widgetDisposed(final DisposeEvent e) {
		GamaPreferences.Interface.CORE_CONSOLE_SIZE.removeChangeListener(sizeListener);
		reset();
		super.widgetDisposed(e);
	}

	@Override
	public void close(final IScope scope) {
		reset();
		super.close(scope);
	}

	@Override
	public synchronized void reset() {
		for (final BufferedWriter writer : writers.values()) {
			try {
				writer.close();
			} catch (final IOException e) {
				DEBUG.ERR("Unable to close an execution console output stream", e);
			}
		}
		writers.clear();
		msgConsole.clearConsole();
		clearPauseBuffer();
	}

	private void clearPauseBuffer() {
		pauseBuffer.clear();
		pauseBufferLength = 0;
		pauseBufferTruncated = false;
		pauseTruncationMarker = "";
	}

	@Override
	public Control getSizableFontControl() {
		if (viewer == null) return null;
		return viewer.getTextWidget();
	}

	@Override
	public void setFocus() {
		Control c = getSizableFontControl();
		if (c != null && !c.isDisposed() && !c.isFocusControl()) {
			c.setFocus(); // Necessary ?
		}

	}

	@Override
	public void pauseChanged() {
		final boolean wasPaused;
		synchronized (this) {
			wasPaused = paused;
			paused = !paused;
			if (paused) {
				clearPauseBuffer();
			} else {
				if (pauseBufferTruncated) { writeToConsole(pauseTruncationMarker, (GamaUIColor) null); }
				for (final BufferedMessage message : pauseBuffer) {
					writeToConsole(message.text(), message.color());
				}
				clearPauseBuffer();
			}
			if (wasPaused) { indicated = false; }
		}
		if (wasPaused) {
			WorkbenchHelper.asyncRun(() -> {
				if (newContentsStatus != null && !newContentsStatus.isDisposed()) {
					newContentsStatus.getControl().dispose();
					newContentsStatus.dispose();
					newContentsStatus = null;
					if (toolbar != null && !toolbar.isDisposed()) { toolbar.requestLayout(); }
				}
			});
		}
	}

	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		super.createToolItems(tb);
		GamaToolbarSimple tbs = toolbar.getToolbar(SWT.RIGHT);
		TextSearchItem.install(tbs, () -> TextSearchItem.open(this));
		tbs.button("editor/local.menu", "More...", "More options", e -> {

			final GamaMenu menu = new GamaMenu() {

				@Override
				protected void fillMenu() {
					GamaCommand.build(ACTION_CLEAR, "Clear", "Clear the console", e -> ConsoleView.this.reset())
							.toItem(mainMenu);
					GamaMenu.separate(mainMenu);
					GamaCommand.build(DISPLAY_TOOLBAR_CSVEXPORT, "Export to log file", "Export to log file",
							e -> saveAsLog()).toItem(mainMenu);
					GamaMenu.separate(mainMenu);
					check("Keep contents", GamaPreferences.Interface.CORE_CONSOLE_KEEP);
					check("Wrap long lines", GamaPreferences.Interface.CORE_CONSOLE_WRAP, e -> {
						viewer.setWordWrap(GamaPreferences.Interface.CORE_CONSOLE_WRAP.getValue());
					});
					fillAdditionalMenuItems(mainMenu);
				}

			};
			menu.open(tbs, e, tbs.getSize().y, 0);
		});

	}

	protected void fillAdditionalMenuItems(final Menu menu) {}

	@Override
	protected ViewUpdateUIJob createUpdateJob() {
		return null;
	}

	/**
	 * As ConsoleView is automatically opened by moving to the simulation perspective, the automatic closing can cause
	 * problems. So the view is stated as accepting an "experiment-less" mode. See Issue #1361 Method
	 * shouldBeClosedWhenNoExperiments()
	 *
	 * @see gama.ui.shared.views.GamaViewPart#shouldBeClosedWhenNoExperiments()
	 */
	@Override
	protected boolean shouldBeClosedWhenNoExperiments() {
		return false;
	}

	@Override
	protected boolean needsOutput() {
		return false;
	}

	@Override
	public String getContents() { return viewer.getDocument().get(); }

}
