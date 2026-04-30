/*******************************************************************************************************
 *
 * LaunchingOverlay.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.utils;

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

import gama.api.GAMA;
import gama.api.ui.IConsoleListener;
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
	 * Horizontal margin on each side of the centred console widget, expressed as a fraction of the overlay width
	 * (1/8th ≈ 12.5 % per side).
	 */
	private static final int CONSOLE_H_MARGIN_DIVISOR = 8;

	/**
	 * Inner text padding (pixels) applied to all four sides of the console {@link StyledText} widget via
	 * {@link StyledText#setMargins(int, int, int, int)}.
	 */
	private static final int CONSOLE_TEXT_PADDING = 6;

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
		if (shell != null) {
			WorkbenchHelper.asyncRun(() -> { if (!shell.isDisposed()) { shell.close(); } });
		}
	}

	// ── Private helpers ───────────────────────────────────────────────────────────

	/**
	 * Builds all SWT widgets, installs listeners, and opens the overlay shell. Must be called on the UI thread.
	 */
	private void buildAndOpen() {
		final Color bg = parent.getBackground();
		final Color fg = parent.getForeground();

		// SWT.ON_TOP ensures the overlay stays above every child shell (display views, etc.)
		// that is opened during the launch sequence. The macOS synthetic-ESC issue that
		// originally motivated removing this flag is already neutralised by the
		// asyncRun(hideLaunchingOverlay) call in SwtGui.openAndApplyLayout(), which defers
		// the shell close until after the syncExec that opens the display views returns.
		final Shell overlay = new Shell(parent, SWT.NO_TRIM | SWT.ON_TOP);
		overlay.setBackground(bg);
		overlay.setLayout(null);
		overlayShell = overlay;

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
		// No SWT.BORDER – we use a tinted background instead to frame the widget.
		// No SWT.H_SCROLL – SWT.WRAP already prevents horizontal overflow.
		consoleText = new StyledText(overlay, SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY | SWT.WRAP);
		consoleText.setEditable(false);
		consoleText.setBackground(bg);
		// Dimmed foreground matching the subtitle style
		final boolean dark = ThemeHelper.isDark();
		final var consoleFg = new Color(overlay.getDisplay(), blend(fg.getRed(), dark ? 255 : 0, 20),
				blend(fg.getGreen(), dark ? 255 : 0, 20), blend(fg.getBlue(), dark ? 255 : 0, 20));
		consoleText.setForeground(consoleFg);
		consoleText.addDisposeListener(e -> consoleFg.dispose());
		consoleText.setFont(parent.getFont());
		// Inner padding so text doesn't touch the widget edges.
		consoleText.setMargins(CONSOLE_TEXT_PADDING, CONSOLE_TEXT_PADDING, CONSOLE_TEXT_PADDING, CONSOLE_TEXT_PADDING);

		// Position everything and open
		final Rectangle ca = parent.getClientArea();
		final org.eclipse.swt.graphics.Point origin = parent.toDisplay(ca.x, ca.y);
		overlay.setBounds(origin.x, origin.y, ca.width, ca.height);
		positionChildren(overlay, cancelBar);
		overlay.open();

		// ── Resize tracking ───────────────────────────────────────────────────────
		final ControlListener[] ref = new ControlListener[1];
		ref[0] = new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				if (overlay.isDisposed()) {
					parent.removeControlListener(ref[0]);
				} else {
					final Rectangle ca2 = parent.getClientArea();
					final org.eclipse.swt.graphics.Point o2 = parent.toDisplay(ca2.x, ca2.y);
					overlay.setBounds(o2.x, o2.y, ca2.width, ca2.height);
					positionChildren(overlay, cancelBar);
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
			if (consoleText.getCharCount() > 0) { consoleText.append(System.lineSeparator()); }
			consoleText.append(firstLine);
			// Auto-scroll to the bottom so the latest line is always visible;
			// the user can freely scroll up to read earlier lines.
			consoleText.setTopIndex(consoleText.getLineCount() - 1);
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
			@Override
			public boolean isDisposed() { return overlay.isDisposed(); }

			@Override
			public void updateWith(final IStatusMessage m) {
				// Forward to the real status bar first.
				if (realControl != null && !realControl.isDisposed()) { realControl.updateWith(m); }
				// Skip EXPERIMENT-state messages — they carry no useful text.
				if (m == null || m.type() == IStatusMessage.StatusType.EXPERIMENT) return;
				final String text = m.message();
				appendToConsole(text);
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

		// Tell the canvas its blockTop so the title is vertically aligned with the cancel bar.
		canvas.setData("blockTop", blockTop);
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
}
