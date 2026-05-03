/*******************************************************************************************************
 *
 * SwingControlMac.java, in gama.ui.display.java2d, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.java2d.swing;

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import gama.ui.display.java2d.AWTDisplayView;
import gama.ui.display.java2d.Java2DDisplaySurface;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class SwingControl.
 */
public class SwingControlMac extends SwingControl {

	static {
		// DEBUG.ON();
	}

	/**
	 * Instantiates a new swing control.
	 *
	 * @param parent
	 *            the parent
	 * @param awtDisplayView
	 * @param style
	 *            the style
	 */
	public SwingControlMac(final Composite parent, final AWTDisplayView view, final Java2DDisplaySurface component,
			final int style) {
		super(parent, view, component, style);
	}

	/** The Constant frameQueue. */
	// Static queue and timer for staggering frame creation. See issue https://github.com/gama-platform/gama/issues/934
	private static final Queue<Runnable> FRAME_QUEUE = new LinkedList<>();

	/** The frame creation in progress. */
	private static boolean FRAME_CREATION_IN_PROGRESS = false;

	/** The Constant FRAME_CREATION_DELAY_MS. */
	private static final long FRAME_CREATION_DELAY_MS = 50;

	/** The Constant FRAME_TIMER. */
	private static final Timer FRAME_TIMER = new Timer("SwingControlMacFrameTimer", true);

	/**
	 * Enqueue frame creation.
	 *
	 * @param task
	 *            the task
	 */
	private static synchronized void enqueueFrameCreation(final Runnable task) {
		FRAME_QUEUE.add(task);
		if (!FRAME_CREATION_IN_PROGRESS) { processNextFrame(); }
	}

	/**
	 * Process next frame.
	 */
	private static synchronized void processNextFrame() {
		Runnable task = FRAME_QUEUE.poll();
		if (task != null) {
			FRAME_CREATION_IN_PROGRESS = true;
			WorkbenchHelper.asyncRun(() -> {
				task.run();
				FRAME_TIMER.schedule(new TimerTask() {
					@Override
					public void run() {
						processNextFrame();
					}
				}, FRAME_CREATION_DELAY_MS);
			});
		} else {
			FRAME_CREATION_IN_PROGRESS = false;
		}
	}

	@Override
	protected void populate() {
		// DEBUG.OUT("[SwingControlMac] populate() called for display: " + (surface != null ? surface.getName() :
		// "null")
		// + " on thread: " + Thread.currentThread().getName());
		if (isDisposed()) // DEBUG.OUT("[SwingControlMac] populate() aborted: disposed");
			return;
		if (!populated) {
			populated = true;
			MouseListener ml = new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					if (surface.isFocusOwner() && !surface.contains(e.getPoint())) {
						frame.setVisible(false);
						frame.setVisible(true);
						WorkbenchHelper.asyncRun(() -> getShell().forceActive());
					}
				}
			};
			// Enqueue the frame creation logic
			enqueueFrameCreation(() -> {
				// DEBUG.OUT("[SwingControlMac] asyncRun: Creating AWT Frame for display: "
				// + (surface != null ? surface.getName() : "null") + " on thread: "
				// + Thread.currentThread().getName());
				// long frameStart = System.currentTimeMillis();
				frame = SWT_AWT.new_Frame(SwingControlMac.this);
				// long frameEnd = System.currentTimeMillis();
				// DEBUG.OUT("[SwingControlMac] Frame created for display: "
				// + (surface != null ? surface.getName() : "null") + " in " + (frameEnd - frameStart) + " ms");
				frame.setAlwaysOnTop(false);
				surface.setVisibility(() -> visible);
				if (swingKeyListener != null) { frame.addKeyListener(swingKeyListener); }
				if (swingMouseListener != null) { frame.addMouseMotionListener(swingMouseListener); }
				// long addSurfaceStart = System.currentTimeMillis();
				frame.add(surface);
				// long addSurfaceEnd = System.currentTimeMillis();
				// DEBUG.OUT("[SwingControlMac] Surface added to frame for display: "
				// + (surface != null ? surface.getName() : "null") + " in " + (addSurfaceEnd - addSurfaceStart)
				// + " ms");
				frame.addMouseListener(ml);
				surface.addMouseListener(ml);
				// DEBUG.OUT("[SwingControlMac] asyncRun: Finished setup for display: "
				// + (surface != null ? surface.getName() : "null") + " on thread: "
				// + Thread.currentThread().getName());
			});
			addListener(SWT.Dispose, e -> EventQueue.invokeLater(() -> {
				try {
					frame.removeMouseListener(ml);
					if (swingKeyListener != null) { frame.removeKeyListener(swingKeyListener); }
					if (swingMouseListener != null) { frame.removeMouseMotionListener(swingMouseListener); }
					surface.removeMouseListener(ml);
					frame.remove(surface);
					surface.dispose();
					frame.dispose();
					// Removes the reference to the different objects
					// (see #489)
					removeAllReferences();
				} catch (final Exception e1) {}
			}));
		}
		// DEBUG.OUT("[SwingControlMac] populate() END for display: " + (surface != null ? surface.getName() : "null")
		// + " on thread: " + Thread.currentThread().getName());
	}
}
