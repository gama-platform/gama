/*******************************************************************************************************
 *
 * LaunchingOverlay.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPart;

import gama.api.GAMA;
import gama.api.ui.IConsoleListener;
import gama.api.ui.IGamaView;
import gama.api.ui.IGui;
import gama.api.ui.IStatusControl;
import gama.api.ui.IStatusDisplayer;
import gama.api.ui.IStatusMessage;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * {@code LaunchingOverlay} is a lightweight full-window SWT {@link Shell} that is displayed over the workbench while a
 * GAMA experiment is being launched. It shows:
 * <ul>
 * <li>A large bold "Launching experiment…" title, centred in the window.</li>
 * <li>A dimmed subtitle with the model name and experiment name.</li>
 * <li>A cancel (stop) button that aborts the launch and returns to the modelling perspective.</li>
 * <li>A scrollable, read-only console area at the bottom that echoes every status message and every
 * {@code write}/{@code print} statement produced during loading. The user can freely scroll through the history.</li>
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <ol>
 * <li>Instantiate with {@link #LaunchingOverlay(Shell, String, String, IConsoleListener, IStatusDisplayer, Runnable)}.
 * </li>
 * <li>Call {@link #show()} to open the overlay shell and register all listeners.</li>
 * <li>Call {@link #hide()} to remove all listeners, restore the real status control, and close the shell.</li>
 * </ol>
 *
 * <p>
 * All SWT calls are guarded so that both {@link #show()} and {@link #hide()} are safe to invoke from any thread.
 * </p>
 */
public class LaunchingOverlay {

	// ── Configuration ───────────────────────────────────────────────────────────

	/**
	 * Approximate height of the bottom console area (in pixels). Sized to show roughly five lines of text plus a small
	 * margin.
	 */
	private static final int CONSOLE_AREA_HEIGHT = 110;

	/**
	 * Bottom margin between the console widget and the bottom edge of the overlay shell (pixels).
	 */
	private static final int CONSOLE_BOTTOM_MARGIN = 28;

	/**
	 * Horizontal margin on each side of the centred console widget, expressed as a fraction of the overlay width (1/8th
	 * ≈ 12.5 % per side).
	 */
	private static final int CONSOLE_H_MARGIN_DIVISOR = 8;

	/**
	 * Inner text padding (pixels) applied to all four sides of the console {@link StyledText} widget via
	 * {@link StyledText#setMargins(int, int, int, int)}.
	 */
	private static final int CONSOLE_TEXT_PADDING = 6;

	/**
	 * Blend percentage (0–100) used to derive the console border colour from the overlay background and foreground. A
	 * value of 30 means the border is 30 % of the way between the background and the foreground, creating a subtle but
	 * clearly visible frame on both light and dark themes.
	 */
	private static final int CONSOLE_BORDER_BLEND_PCT = 30;

	/**
	 * Blend percentage (0–100) used to tint the console background colour away from the overlay background. A value of
	 * 8 means only a slight tint toward white (dark theme) or black (light theme), just enough to visually separate the
	 * terminal area from the surrounding overlay without being distracting.
	 */
	private static final int CONSOLE_BG_BLEND_PCT = 8;

	/**
	 * SWT style of the overlay shell. The overlay is intentionally modeless so the native frame of the parent workbench
	 * shell remains draggable and resizable while launch feedback is displayed. {@link SWT#NO_TRIM} preserves the
	 * current undecorated full-client-area appearance.
	 */
	private static final int OVERLAY_SHELL_STYLE = SWT.NO_TRIM;

	/**
	 * Displays whose native OpenGL canvases were hidden for the current launch overlay and must be restored when the
	 * overlay closes.
	 */
	private static final Set<IGamaView.Display> SUPPRESSED_NATIVE_DISPLAYS = ConcurrentHashMap.newKeySet();

	/**
	 * Indicates whether a launching overlay is currently active. OpenGL views created while this flag is {@code true}
	 * keep their native canvases hidden until the launch completes.
	 */
	private static volatile boolean launchOverlayVisible;

	// ── Construction-time dependencies (never null after construction) ───────────

	/** The parent workbench shell over which the overlay is placed. */
	private final Shell parent;

	/**
	 * Display name of the model being launched. May be empty but never {@code null}.
	 */
	private final String modelName;

	/**
	 * Display name of the experiment being launched. May be empty but never {@code null}.
	 */
	private final String expName;

	/**
	 * The composite console listener of the running GUI instance. Used to register and later unregister the overlay's
	 * own {@link #overlayConsoleListener}.
	 */
	private final IConsoleListener consoleSource;

	/**
	 * The status displayer of the running GUI instance. Used to intercept status messages and to restore the real
	 * status control on {@link #hide()}.
	 */
	private final IStatusDisplayer statusDisplayer;

	/**
	 * Runnable that is executed when the user clicks the cancel button. Typically closes all experiments and returns to
	 * the modelling perspective. Must be thread-safe (executed on a dedicated thread).
	 */
	private final Runnable cancelAction;

	// ── Runtime state ────────────────────────────────────────────────────────────

	/**
	 * The overlay shell. {@code null} before {@link #show()} and after the shell has been disposed by {@link #hide()}.
	 */
	private volatile Shell overlayShell;

	/**
	 * The canvas that paints the background, title and subtitle. Fills the whole overlay shell so it acts as both a
	 * background and a text renderer.
	 */
	private Canvas canvas;

	/**
	 * The scrollable, read-only console widget shown at the bottom of the overlay. Receives lines from both
	 * {@link #overlayConsoleListener} and the status interceptor.
	 */
	private StyledText consoleText;

	/**
	 * The console listener registered on {@link #consoleSource} while the overlay is visible. Removed by
	 * {@link #hide()}.
	 */
	private volatile IConsoleListener overlayConsoleListener;

	/**
	 * The real {@link IStatusControl} that was active before the overlay was shown. Saved on {@link #show()} and
	 * restored on {@link #hide()}.
	 */
	private volatile IStatusControl savedStatusControl;

	/**
	 * The last line of text that was actually appended to the console widget. Tracked on the UI thread inside
	 * {@link #appendToConsole(String)} to deduplicate identical consecutive messages that may arrive through multiple
	 * channels (the console listener and the status interceptor can both deliver the same text).
	 */
	private String lastConsoleLine;

	// ── Constructor ──────────────────────────────────────────────────────────────

	/**
	 * Instantiates a new launching overlay. The overlay is not yet visible; call {@link #show()} to open it.
	 *
	 * @param parent
	 *            the parent workbench shell – must not be {@code null} and must not be disposed
	 * @param modelName
	 *            display name of the model being launched; may be empty but not {@code null}
	 * @param expName
	 *            display name of the experiment being launched; may be empty but not {@code null}
	 * @param consoleSource
	 *            the composite console listener used to register the overlay's own listener
	 * @param statusDisplayer
	 *            the status displayer used to intercept status messages; may be {@code null} if unavailable
	 * @param cancelAction
	 *            runnable invoked (on a fresh thread) when the user clicks the cancel button
	 */
	public LaunchingOverlay(final Shell parent, final String modelName, final String expName,
			final IConsoleListener consoleSource, final IStatusDisplayer statusDisplayer, final Runnable cancelAction) {
		this.parent = parent;
		this.modelName = modelName;
		this.expName = expName;
		this.consoleSource = consoleSource;
		this.statusDisplayer = statusDisplayer;
		this.cancelAction = cancelAction;
	}

	/**
	 * Returns whether a launching overlay is currently active.
	 *
	 * @return {@code true} if a launching overlay is currently visible or being prepared, {@code false} otherwise
	 */
	public static boolean isLaunchOverlayVisible() { return launchOverlayVisible; }

	/**
	 * Hides the native canvas of the given display when a launching overlay is active.
	 *
	 * @param display
	 *            the display whose native canvas should remain hidden during launch
	 * @return {@code true} if the display is currently suppressed for launch, {@code false} otherwise
	 */
	public static boolean suppressNativeDisplayIfLaunching(final IGamaView.Display display) {
		if (!launchOverlayVisible || !isNativeOpenGLDisplay(display)) return false;
		if (WorkbenchHelper.isDisplayThread()) {
			suppressNativeDisplay(display);
		} else {
			WorkbenchHelper.run(() -> suppressNativeDisplay(display));
		}
		return true;
	}

	// ── Public API ───────────────────────────────────────────────────────────────

	/**
	 * Opens the overlay shell, builds all SWT widgets, and registers both the console listener and the status
	 * interceptor. Safe to call from any thread: all SWT work is performed via {@link WorkbenchHelper#run(Runnable)}.
	 * <p>
	 * Does nothing if the parent shell is already disposed or if the overlay is already visible.
	 * </p>
	 */
	public void show() {
		if (parent == null || parent.isDisposed()) return;
		WorkbenchHelper.run(this::buildAndOpen);
	}

	/**
	 * Removes the console listener, restores the real status control, and closes the overlay shell. Safe to call from
	 * any thread: the SWT close is scheduled via {@link WorkbenchHelper#asyncRun(Runnable)}.
	 * <p>
	 * Idempotent – calling {@code hide()} more than once has no effect.
	 * </p>
	 */
	public void hide() {
		launchOverlayVisible = false;
		final List<IGamaView.Display> suppressedDisplays = drainSuppressedNativeDisplays();

		// Restore the real status control first (thread-safe).
		final IStatusControl saved = savedStatusControl;
		savedStatusControl = null;
		if (saved != null && statusDisplayer != null) { statusDisplayer.setStatusTarget(saved); }

		// Remove the console listener (thread-safe).
		final IConsoleListener listener = overlayConsoleListener;
		overlayConsoleListener = null;
		if (listener != null && consoleSource != null) { consoleSource.removeConsoleListener(listener); }

		// Close the shell on the UI thread.
		final Shell shell = overlayShell;
		overlayShell = null;
		if (shell != null || !suppressedDisplays.isEmpty()) {
			WorkbenchHelper.asyncRun(() -> {
				if (shell != null && !shell.isDisposed()) { shell.close(); }
				restoreNativeDisplays(suppressedDisplays);
			});
		}
	}

	// ── Private helpers ───────────────────────────────────────────────────────────

	/**
	 * Builds all SWT widgets, installs listeners, and opens the overlay shell. Must be called on the UI thread.
	 */
	private void buildAndOpen() {
		final Color bg = parent.getBackground();
		final Color fg = parent.getForeground();

		// Keep the overlay as an owned, undecorated child shell instead of a modal one so
		// it still covers the workbench client area while leaving the native window frame
		// available for move/resize interactions.
		final Shell overlay = new Shell(parent, OVERLAY_SHELL_STYLE);
		overlay.setBackground(bg);
		overlay.setLayout(null);
		overlayShell = overlay;
		launchOverlayVisible = true;
		suppressExistingNativeDisplays();

		// ── Cancel (stop) button ────────────────────────────────────────────────
		final ToolBar cancelBar = new ToolBar(overlay, SWT.FLAT | SWT.NO_FOCUS);
		cancelBar.setBackground(bg);
		final ToolItem cancelItem = new ToolItem(cancelBar, SWT.FLAT | SWT.PUSH);
		final GamaIcon stopIcon = GamaIcon.named(IGamaIcons.EXPERIMENT_STOP);
		cancelItem.setImage(stopIcon.image());
		cancelItem.setDisabledImage(stopIcon.disabled());
		cancelItem.setToolTipText("Cancel launch and return to the modelling perspective");
		cancelItem.addSelectionListener(
				org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter(e -> handleCancel()));
		cancelBar.pack();

		// ── Canvas for background + title + subtitle ─────────────────────────────
		canvas = new Canvas(overlay, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		canvas.addPaintListener(e -> paintCanvas(e, bg, fg));

		// ── Scrollable console area at the bottom ─────────────────────────────────
		// No SWT.H_SCROLL – SWT.WRAP already prevents horizontal overflow.
		// A tinted background and a drawn border (see paintCanvas) frame the widget.
		consoleText = new StyledText(overlay, SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY | SWT.WRAP);
		consoleText.setEditable(false);
		// Tinted background: slightly lighter (dark theme) or slightly darker (light theme) than the overlay
		// background, so the console area is visually distinct on every platform.
		final boolean dark = ThemeHelper.isDark();
		final var consoleBg = createConsoleBgColor(overlay.getDisplay(), bg, dark);
		consoleText.setBackground(consoleBg);
		consoleText.addDisposeListener(ev -> consoleBg.dispose());
		// Dimmed foreground matching the subtitle style
		final var consoleFg = new Color(overlay.getDisplay(), blend(fg.getRed(), dark ? 255 : 0, 20),
				blend(fg.getGreen(), dark ? 255 : 0, 20), blend(fg.getBlue(), dark ? 255 : 0, 20));
		consoleText.setForeground(consoleFg);
		consoleText.addDisposeListener(ev -> consoleFg.dispose());
		consoleText.setFont(parent.getFont());
		// Inner padding so text doesn't touch the widget edges.
		consoleText.setMargins(CONSOLE_TEXT_PADDING, CONSOLE_TEXT_PADDING, CONSOLE_TEXT_PADDING, CONSOLE_TEXT_PADDING);

		// Position everything and open
		final Rectangle ca = parent.getClientArea();
		final org.eclipse.swt.graphics.Point origin = parent.toDisplay(ca.x, ca.y);
		overlay.setBounds(origin.x, origin.y, ca.width, ca.height);
		positionChildren(overlay, cancelBar);
		overlay.open();
		overlay.forceActive();

		// ── Resize tracking ───────────────────────────────────────────────────────
		final ControlListener[] ref = new ControlListener[1];
		ref[0] = new ControlAdapter() {

			private void syncOverlayBounds() {
				final Rectangle ca2 = parent.getClientArea();
				final org.eclipse.swt.graphics.Point o2 = parent.toDisplay(ca2.x, ca2.y);
				overlay.setBounds(o2.x, o2.y, ca2.width, ca2.height);
				positionChildren(overlay, cancelBar);
			}

			@Override
			public void controlMoved(final ControlEvent e) {
				if (overlay.isDisposed()) {
					parent.removeControlListener(ref[0]);
				} else {
					syncOverlayBounds();
				}
			}

			@Override
			public void controlResized(final ControlEvent e) {
				if (overlay.isDisposed()) {
					parent.removeControlListener(ref[0]);
				} else {
					syncOverlayBounds();
				}
			}
		};
		parent.addControlListener(ref[0]);
		overlay.addDisposeListener(e -> parent.removeControlListener(ref[0]));

		// ── Register console listener ─────────────────────────────────────────────
		final IConsoleListener listener = (msg, root, color) -> appendToConsole(msg);
		overlayConsoleListener = listener;
		if (consoleSource != null) { consoleSource.addConsoleListener(listener); }

		// ── Register status interceptor ───────────────────────────────────────────
		installStatusInterceptor(overlay);
	}

	/**
	 * Handles the cancel button click: hides the overlay and runs the cancel action on a dedicated thread.
	 */
	private void handleCancel() {
		hide();
		new Thread(() -> GAMA.closeAllExperiments(true, false), "Cancel launch").start();
	}

	/**
	 * Appends a non-blank line of text to the scrollable console widget and auto-scrolls to the bottom. Safe to call
	 * from any thread.
	 *
	 * @param msg
	 *            the raw message to append; blank or {@code null} messages are silently ignored
	 */
	private void appendToConsole(final String msg) {
		if (msg == null || msg.isBlank()) return;
		final String firstLine = msg.lines().map(String::strip).filter(l -> !l.isBlank()).findFirst().orElse(null);
		if (firstLine == null) return;
		WorkbenchHelper.asyncRun(() -> {
			if (consoleText == null || consoleText.isDisposed()) return;
			// Deduplicate: skip if this line is identical to the one just appended.
			// Both the console listener and the status interceptor can deliver the same text,
			// so we guard here on the UI thread (single-threaded) rather than in each caller.
			if (firstLine.equals(lastConsoleLine)) return;
			lastConsoleLine = firstLine;
			if (consoleText.getCharCount() > 0) { consoleText.append(System.lineSeparator()); }
			consoleText.append(firstLine);
			// Auto-scroll to the bottom: compute how many lines fit in the visible area and
			// set topIndex so that the most-recent line appears at the bottom (terminal behaviour).
			final int lineCount = consoleText.getLineCount();
			final int lineHeight = consoleText.getLineHeight();
			final int clientHeight = consoleText.getClientArea().height;
			final int visibleLines = lineHeight > 0 && clientHeight > 0 ? clientHeight / lineHeight : 1;
			consoleText.setTopIndex(Math.max(0, lineCount - visibleLines));
		});
	}

	/**
	 * Installs a forwarding {@link IStatusControl} that feeds status messages into the console area and simultaneously
	 * forwards them to the real status bar. The real control is saved in {@link #savedStatusControl} and restored by
	 * {@link #hide()}.
	 *
	 * @param overlay
	 *            the overlay shell, used only to check disposal in {@link IStatusControl#isDisposed()}
	 */
	private void installStatusInterceptor(final Shell overlay) {
		if (statusDisplayer == null) return;
		final IStatusControl realControl = statusDisplayer.getStatusTarget();
		savedStatusControl = realControl;
		statusDisplayer.setStatusTarget(new IStatusControl() {

			String previous;

			@Override
			public boolean isDisposed() { return overlay.isDisposed(); }

			@Override
			public void updateWith(final IStatusMessage m) {
				// Forward to the real status bar first.
				if (realControl != null && !realControl.isDisposed()) { realControl.updateWith(m); }
				// Skip EXPERIMENT-state messages — they carry no useful text.
				if (m == null || m.type() == IStatusMessage.StatusType.EXPERIMENT) return;
				final String text = m.message();
				if (text != null && !text.equals(previous)) {
					appendToConsole(text);
					previous = text;
				}
			}
		});
	}

	/**
	 * Lays out the three children (canvas, cancel bar, console text) inside the overlay shell. The canvas fills the
	 * entire shell; the cancel bar is centred horizontally just below the title/subtitle block; the console text widget
	 * occupies a fixed-height strip along the bottom edge.
	 *
	 * @param overlay
	 *            the overlay shell whose bounds drive the layout
	 * @param cancelBar
	 *            the already-packed cancel tool bar
	 */
	private void positionChildren(final Shell overlay, final ToolBar cancelBar) {
		final var sz = overlay.getSize();
		final var bs = cancelBar.getSize();

		// Estimated block heights (pixel constants matching the canvas paint listener).
		final int titleH = 28;
		final int subH = 16;
		final int textToBtn = 12;
		final int blockH = titleH + 4 + subH + textToBtn + bs.y;
		final int blockTop = (sz.y - blockH) / 2;
		final int btnY = blockTop + titleH + 4 + subH + textToBtn;

		// Canvas fills the whole shell (draws background + title + subtitle).
		canvas.setBounds(0, 0, sz.x, sz.y);

		// Cancel bar centred horizontally, just below the text block.
		cancelBar.setBounds((sz.x - bs.x) / 2, btnY, bs.x, bs.y);
		cancelBar.moveAbove(canvas);

		// Scrollable console at the bottom, centred horizontally with side margins.
		final int hMargin = sz.x / CONSOLE_H_MARGIN_DIVISOR;
		final int consoleY = sz.y - CONSOLE_AREA_HEIGHT - CONSOLE_BOTTOM_MARGIN;
		consoleText.setBounds(hMargin, consoleY, sz.x - 2 * hMargin, CONSOLE_AREA_HEIGHT);
		consoleText.moveAbove(canvas);

		// Tell the canvas its blockTop so the title is vertically aligned with the cancel bar,
		// and the console bounds so it can draw a visible border around the terminal area.
		canvas.setData("blockTop", blockTop);
		canvas.setData("consoleBounds", new Rectangle(hMargin, consoleY, sz.x - 2 * hMargin, CONSOLE_AREA_HEIGHT));
		canvas.redraw();
	}

	/**
	 * Paint listener callback for the background/title/subtitle canvas.
	 *
	 * @param e
	 *            the paint event
	 * @param bg
	 *            background colour of the parent shell
	 * @param fg
	 *            foreground colour of the parent shell
	 */
	private void paintCanvas(final org.eclipse.swt.events.PaintEvent e, final Color bg, final Color fg) {
		final var b = canvas.getBounds();
		e.gc.setBackground(bg);
		e.gc.fillRectangle(0, 0, b.width, b.height);

		// ── Console area: tinted fill + border ────────────────────────────────
		final Object consoleBoundsData = canvas.getData("consoleBounds");
		if (consoleBoundsData instanceof Rectangle cb) {
			// Fill the tinted background region so it shows even behind the StyledText (avoids gaps on resize)
			final boolean darkBg = ThemeHelper.isDark();
			final var consoleBgColor = createConsoleBgColor(e.display, bg, darkBg);
			e.gc.setBackground(consoleBgColor);
			e.gc.fillRectangle(cb.x, cb.y, cb.width, cb.height);
			consoleBgColor.dispose();
			// Draw a 1-px border with CONSOLE_BORDER_BLEND_PCT % blend toward the foreground
			final var borderColor = new Color(e.display, blend(bg.getRed(), fg.getRed(), CONSOLE_BORDER_BLEND_PCT),
					blend(bg.getGreen(), fg.getGreen(), CONSOLE_BORDER_BLEND_PCT),
					blend(bg.getBlue(), fg.getBlue(), CONSOLE_BORDER_BLEND_PCT));
			e.gc.setForeground(borderColor);
			e.gc.drawRectangle(cb.x - 1, cb.y - 1, cb.width + 1, cb.height + 1);
			borderColor.dispose();
			// Restore background for subsequent text drawing
			e.gc.setBackground(bg);
		}

		// ── Title (20pt bold) ─────────────────────────────────────────────────
		final FontData fd = parent.getFont().getFontData()[0];
		final var td = new org.eclipse.swt.graphics.FontData(fd.getName(), 20, SWT.BOLD);
		final var bigFont = new org.eclipse.swt.graphics.Font(e.display, td);
		e.gc.setFont(bigFont);
		e.gc.setForeground(fg);
		final String title = "Launching experiment\u2026";
		final var te = e.gc.textExtent(title);
		final Object stored = canvas.getData("blockTop");
		final int titleY = stored instanceof Integer bt ? bt : (b.height - te.y) / 2;
		e.gc.drawText(title, (b.width - te.x) / 2, titleY, true);
		bigFont.dispose();

		// ── Subtitle (dimmed) ─────────────────────────────────────────────────
		if (!modelName.isEmpty() || !expName.isEmpty()) {
			e.gc.setFont(parent.getFont());
			final String sub = modelName + (expName.isEmpty() ? "" : "  \u2192  " + expName);
			final var se = e.gc.textExtent(sub);
			final boolean dark = ThemeHelper.isDark();
			final var dim = new Color(e.display, blend(bg.getRed(), dark ? 255 : 0, 45),
					blend(bg.getGreen(), dark ? 255 : 0, 45), blend(bg.getBlue(), dark ? 255 : 0, 45));
			e.gc.setForeground(dim);
			e.gc.drawText(sub, (b.width - se.x) / 2, titleY + te.y + 4, true);
			dim.dispose();
		}
	}

	/**
	 * Blends colour channel {@code a} toward {@code b} by {@code pct} percent.
	 *
	 * @param a
	 *            the source channel value (0–255)
	 * @param b
	 *            the target channel value (0–255)
	 * @param pct
	 *            blend percentage (0 = all {@code a}, 100 = all {@code b})
	 * @return the blended channel value
	 */
	private static int blend(final int a, final int b, final int pct) {
		return a + (b - a) * pct / 100;
	}

	/**
	 * Creates a tinted version of the given background colour for use as the console area background. The tint blends
	 * {@code CONSOLE_BG_BLEND_PCT} percent toward white on dark themes and toward black on light themes, so the console
	 * area is subtly distinct from the surrounding overlay on every platform.
	 *
	 * @param display
	 *            the SWT display to allocate the colour on
	 * @param bg
	 *            the overlay background colour
	 * @param dark
	 *            {@code true} if the current theme is dark, {@code false} if light
	 * @return a new {@link Color} that the caller is responsible for disposing
	 */
	private static Color createConsoleBgColor(final org.eclipse.swt.widgets.Display display, final Color bg,
			final boolean dark) {
		final int target = dark ? 255 : 0;
		return new Color(display, blend(bg.getRed(), target, CONSOLE_BG_BLEND_PCT),
				blend(bg.getGreen(), target, CONSOLE_BG_BLEND_PCT),
				blend(bg.getBlue(), target, CONSOLE_BG_BLEND_PCT));
	}

	/**
	 * Hides all currently open native OpenGL displays while the launch overlay is active.
	 */
	private static void suppressExistingNativeDisplays() {
		for (final IGamaView.Display display : ViewsHelper.getDisplayViews(null)) {
			if (display.isVisible()) { suppressNativeDisplay(display); }
		}
	}

	/**
	 * Hides the native canvas of a single display and remembers it for later restoration.
	 *
	 * @param display
	 *            the display to suppress
	 */
	private static void suppressNativeDisplay(final IGamaView.Display display) {
		if (!launchOverlayVisible || !isNativeOpenGLDisplay(display) || display.getDisplaySurface() == null || display.getDisplaySurface().isDisposed()) return;
		if (SUPPRESSED_NATIVE_DISPLAYS.add(display)) { display.hideCanvas(); }
	}

	/**
	 * Drains and returns the set of native displays currently suppressed by the launch overlay.
	 *
	 * @return the displays that must be restored after the overlay closes
	 */
	private static List<IGamaView.Display> drainSuppressedNativeDisplays() {
		final List<IGamaView.Display> displays = new ArrayList<>(SUPPRESSED_NATIVE_DISPLAYS);
		SUPPRESSED_NATIVE_DISPLAYS.clear();
		return displays;
	}

	/**
	 * Restores the native canvases of displays that were hidden while the launch overlay was active.
	 *
	 * @param displays
	 *            the displays to restore
	 */
	private static void restoreNativeDisplays(final List<IGamaView.Display> displays) {
		for (final IGamaView.Display display : displays) {
			if (display == null || display.getDisplaySurface() == null || display.getDisplaySurface().isDisposed()) {
				continue;
			}
			display.showCanvas();
		}
	}

	/**
	 * Returns whether the specified display is backed by a native OpenGL canvas that can paint above SWT controls.
	 *
	 * @param display
	 *            the display to examine
	 * @return {@code true} if the display is a native OpenGL display, {@code false} otherwise
	 */
	private static boolean isNativeOpenGLDisplay(final IGamaView.Display display) {
		if (display == null || display.is2D() || !(display instanceof IWorkbenchPart part) || part.getSite() == null) return false;
		final String id = part.getSite().getId();
		return IGui.GL_LAYER_VIEW_ID.equals(id) || IGui.GL_LAYER_VIEW_ID2.equals(id);
	}
}
