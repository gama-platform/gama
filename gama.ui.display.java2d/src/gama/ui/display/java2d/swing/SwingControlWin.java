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
import java.lang.reflect.InvocationTargetException;

import javax.swing.JApplet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import gama.ui.display.java2d.AWTDisplayView;
import gama.ui.display.java2d.Java2DDisplaySurface;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class SwingControl.
 */
public class SwingControlWin extends SwingControl {

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
				JApplet applet = new JApplet();
				frame = SWT_AWT.new_Frame(SwingControlWin.this);
				frame.setAlwaysOnTop(false);
				surface.setVisibility(() -> visible);
				applet.getContentPane().add(surface);
				frame.add(applet);
				Listener resizeListener = event -> { surface.setMonitor(this.getMonitor()); };
				addListener(SWT.Resize, resizeListener);
				addListener(SWT.Dispose, event -> {
					removeListener(SWT.Resize, resizeListener);
					EventQueue.invokeLater(() -> {
						try {
							applet.getContentPane().remove(surface);
							frame.remove(applet);
							surface.dispose();
							frame.dispose();
						} catch (final Exception e) {}

					});
				}

				);

			});

		}
	}

	@Override
	protected void privateSetDimensions(final int width, final int height) {
		// Assignment necessary for #3313 and #3239
		WorkbenchHelper.asyncRun(() -> {
			if (isDisposed()) return;
			Rectangle r = this.getBounds();
			int w = r.width;
			int h = r.height;
			// Solves a problem where the last view on HiDPI screens on Windows would be outscaled
			if (!this.isDisposed()) { this.requestLayout(); }
			try {
				EventQueue.invokeAndWait(() -> {
					// DEBUG.OUT("Set size sent by SwingControl " + width + " " + height);
					// frame.setBounds(x, y, width, height);
					// frame.setVisible(false);
					surface.setSize(w, h);
					// frame.setVisible(true);
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}

		});

	}

}
