/*******************************************************************************************************
 *
 * SwingControlLinux.java, in gama.ui.display.java2d, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.java2d.swing;

import java.awt.EventQueue;

import javax.swing.JApplet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import gama.ui.display.java2d.AWTDisplayView;
import gama.ui.display.java2d.Java2DDisplaySurface;
import gama.ui.display.java2d.WorkaroundForIssue2476;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class SwingControl.
 */
public class SwingControlLinux extends SwingControl {

    /**
     * Instantiates a new swing control.
     *
     * @param parent
     *            the parent
     * @param awtDisplayView
     * @param style
     *            the style
     */
    public SwingControlLinux(final Composite parent, final AWTDisplayView view, final Java2DDisplaySurface component,
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
	    WorkbenchHelper.asyncRun(() -> {
		JApplet applet = new JApplet();
		frame = SWT_AWT.new_Frame(SwingControlLinux.this);
		frame.setAlwaysOnTop(false);
		if (swingKeyListener != null) {
		    frame.addKeyListener(swingKeyListener);
		}
		if (swingMouseListener != null) {
		    applet.addMouseMotionListener(swingMouseListener);
		}
		surface.setVisibility(() -> visible);
		applet.getContentPane().add(surface);
		WorkaroundForIssue2476.installOn(applet, surface);
		frame.add(applet);
		addListener(SWT.Dispose, e -> EventQueue.invokeLater(() -> {
		    try {
			if (swingKeyListener != null) {
			    frame.removeKeyListener(swingKeyListener);
			}
			if (swingMouseListener != null) {
			    applet.removeMouseMotionListener(swingMouseListener);
			}
			applet.getContentPane().remove(surface);
			frame.remove(applet);
			surface.dispose();
			frame.dispose();
			// Removes the reference to the different objects
			// (see #489)
			removeAllReferences();
		    } catch (final Exception e1) {
		    }

		}));
	    });

	}
    }

}
