/*******************************************************************************************************
 *
 * SwingControlWin.java, in gama.ui.display.java2d, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.java2d.swing;

import java.awt.EventQueue;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import gama.dev.DEBUG;
import gama.ui.display.java2d.AWTDisplayView;
import gama.ui.display.java2d.Java2DDisplaySurface;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class SwingControl.
 */
public class SwingControlWin extends SwingControl {

	static {
		DEBUG.ON();
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
	public SwingControlWin(final Composite parent, final AWTDisplayView view, final Java2DDisplaySurface component,
			final int style) {
		super(parent, view, component, style);
	}

	@Override
	protected void populate() {
		if (isDisposed()) return;
		if (!populated) {
			populated = true;
			WorkbenchHelper.asyncRun(() -> {
				frame = SWT_AWT.new_Frame(SwingControlWin.this);
				frame.setAlwaysOnTop(false);
				surface.setVisibility(() -> visible);
				JPanel rootPane = new JPanel();
				rootPane.setLayout(new GridBagLayout());
				rootPane.add(surface);
				frame.add(rootPane);
				frame.validate();

				Listener resizeListener = e -> { surface.setMonitor(this.getMonitor()); };
				addListener(SWT.Resize, resizeListener);
				addListener(SWT.Dispose, e -> {
					removeListener(SWT.Resize, resizeListener);
					EventQueue.invokeLater(() -> {
						try {
							rootPane.removeAll();
							frame.removeAll();
							surface.dispose();
							frame.dispose();
							// Removes the reference to the different objects
							// (see #489)
							removeAllReferences();
						} catch (final Exception e1) {
							DEBUG.LOG(e1.getMessage());
						}

					});
				}

				);

			});

		}
	}

	// @Override
	// protected void populate() {
	// if (isDisposed()) { return; }
	// if (!populated) {
	// populated = true;
	// WorkbenchHelper.asyncRun(() -> {
	// JApplet applet = new JApplet();
	// frame = SWT_AWT.new_Frame(SwingControlWin.this);
	// frame.setAlwaysOnTop(false);
	// surface.setVisibility(() -> visible);
	// applet.add(surface);
	// frame.add(applet);
	// // if (swingKeyListener != null) {
	// // applet.addKeyListener(swingKeyListener);
	// // Tested but do not provide any improvement
	// // frame.addKeyListener(swingKeyListener);
	// // surface.addKeyListener(swingKeyListener);
	// // }
	// // if (swingMouseListener != null) { applet.addMouseMotionListener(swingMouseListener); }
	// Listener resizeListener = e -> { surface.setMonitor(this.getMonitor()); };
	//
	// addListener(SWT.Resize, resizeListener);
	// addListener(SWT.Dispose, e -> {
	// removeListener(SWT.Resize, resizeListener);
	// EventQueue.invokeLater(() -> {
	// try {
	// applet.getContentPane().removeAll();
	// frame.removeAll();
	// surface.dispose();
	// frame.dispose();
	// // Removes the reference to the different objects
	// // (see #489)
	// removeAllReferences();
	// } catch (final Exception e1) {
	// DEBUG.LOG(e1.getMessage());
	// }
	//
	// });
	// }
	//
	// );
	//
	// });
	//
	// }
	// }

	@Override
	protected void privateSetDimensions(final int width, final int height) {
		// Assignment necessary for #3313 and #3239
		// DEBUG.OUT("[privateSetDimensions] " + (surface != null ? surface.getName() : "null") + " w=" + width + " h="
		// + height + " thread=" + Thread.currentThread().getName());
		WorkbenchHelper.asyncRun(() -> {
			if (isDisposed()) return;
			Rectangle r = this.getBounds();
			int w = r.width;
			int h = r.height;
			// Solves a problem where the last view on HiDPI screens on Windows
			// would be outscaled
			if (!this.isDisposed() && surface.getWidth() != w && surface.getHeight() != h) {
				// DEBUG.OUT("[privateSetDimensions asyncRun] requestLayout for "
				// + (surface != null ? surface.getName() : "null") + " surface=" + surface.getWidth() + "x"
				// + surface.getHeight() + " swtBounds=" + w + "x" + h);
				this.requestLayout();
			}
			// Use invokeLater rather than invokeAndWait: the latter blocked the SWT
			// display thread until the AWT surface was resized, but with N displays each
			// componentResized → resizeImage → repaint chain queued a full layer-draw on
			// the AWT EDT that had to drain before the next invokeAndWait could run,
			// making relaunch O(N) times slower (see issue #3719).
			// DEBUG.OUT("[privateSetDimensions asyncRun] invokeLater surface.setBounds for "
			// + (surface != null ? surface.getName() : "null") + " " + w + "x" + h);
			EventQueue.invokeLater(() -> { surface.setBounds(0, 0, w, h); });
		});

	}

}
