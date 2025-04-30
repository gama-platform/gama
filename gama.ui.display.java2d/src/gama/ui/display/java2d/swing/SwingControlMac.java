/*******************************************************************************************************
 *
 * SwingControlMac.java, in gama.ui.display.java2d, is part of the source code of the GAMA modeling and simulation platform
 * .
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

    @Override
    protected void populate() {
	if (isDisposed()) {
	    return;
	}
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
	    WorkbenchHelper.asyncRun(() -> {
		frame = SWT_AWT.new_Frame(SwingControlMac.this);
		frame.setAlwaysOnTop(false);
		if (swingKeyListener != null) {
		    frame.addKeyListener(swingKeyListener);
		}
		if (swingMouseListener != null) {
		    frame.addMouseMotionListener(swingMouseListener);
		}
		frame.add(surface);

		frame.addMouseListener(ml);
		surface.addMouseListener(ml);

	    });
	    addListener(SWT.Dispose, _ -> EventQueue.invokeLater(() -> {
		try {
		    frame.removeMouseListener(ml);
		    if (swingKeyListener != null) {
			frame.removeKeyListener(swingKeyListener);
		    }
		    if (swingMouseListener != null) {
			frame.removeMouseMotionListener(swingMouseListener);
		    }
		    surface.removeMouseListener(ml);
		    frame.remove(surface);
		    surface.dispose();
		    frame.dispose();
		    // Removes the reference to the different objects
		    // (see #489)
		    removeAllReferences();
		} catch (final Exception e) {
		}

	    }));
	}
    }

}
