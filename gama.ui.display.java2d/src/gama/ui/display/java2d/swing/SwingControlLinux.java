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

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import gama.ui.display.java2d.AWTDisplayView;
import gama.ui.display.java2d.Java2DDisplaySurface;
import gama.ui.display.java2d.WorkaroundForIssue2476;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * Linux-specific implementation of {@link SwingControl} that embeds a {@link Java2DDisplaySurface} inside a
 * SWT-AWT bridge frame. The surface is added directly to the AWT {@link java.awt.Frame} (no intermediate
 * Swing panel is needed, as the surface manages its own layout via {@link java.awt.BorderLayout}).
 * The mouse-wheel and mouse-event workaround for Linux rendering artefacts (issue #2476) is installed on
 * the frame via {@link gama.ui.display.java2d.WorkaroundForIssue2476}.
 */
public class SwingControlLinux extends SwingControl {

    /**
     * Instantiates a new SwingControlLinux.
     *
     * @param parent
     *            the SWT parent composite into which the AWT frame will be embedded
     * @param view
     *            the {@link AWTDisplayView} that owns this control, used by the parent class to track
     *            part-visibility events
     * @param component
     *            the {@link Java2DDisplaySurface} (a {@link javax.swing.JPanel} subclass) that will be
     *            added to the embedded Swing hierarchy
     * @param style
     *            the SWT style bits forwarded to the parent {@link SwingControl}
     */
    public SwingControlLinux(final Composite parent, final AWTDisplayView view, final Java2DDisplaySurface component,
	    final int style) {
	super(parent, view, component, style);
    }

    /**
     * Lazily builds the embedded Swing component hierarchy the first time the control needs to be laid out.
     * <p>
     * On the first call (guarded by {@code populated}), this method:
     * <ol>
     * <li>Creates an AWT {@link java.awt.Frame} via {@code SWT_AWT.new_Frame} and registers any key and
     * mouse-motion listeners directly on it.</li>
     * <li>Adds the {@link Java2DDisplaySurface} directly to the frame (no intermediate panel is required,
     * as the surface manages its own {@link java.awt.BorderLayout}) and installs the Linux mouse
     * workaround via
     * {@link gama.ui.display.java2d.WorkaroundForIssue2476#installOn(java.awt.Container, gama.api.ui.displays.IDisplaySurface)}.</li>
     * <li>Registers a {@code SWT.Dispose} listener that removes all listeners and properly disposes of
     * the frame and surface when the SWT control is destroyed (see issue #489).</li>
     * </ol>
     * All Swing-side operations are executed on the AWT Event Dispatch Thread via
     * {@link gama.ui.shared.utils.WorkbenchHelper#asyncRun(Runnable)}.
     */
    @Override
    protected void populate() {
	if (isDisposed()) {
	    return;
	}
	if (!populated) {
	    populated = true;
	    WorkbenchHelper.asyncRun(() -> {
		frame = SWT_AWT.new_Frame(SwingControlLinux.this);
		frame.setAlwaysOnTop(false);
		if (swingKeyListener != null) {
		    frame.addKeyListener(swingKeyListener);
		}
		if (swingMouseListener != null) {
		    frame.addMouseMotionListener(swingMouseListener);
		}
		surface.setVisibility(() -> visible);
		frame.add(surface);
		WorkaroundForIssue2476.installOn(frame, surface);
		frame.validate();
		addListener(SWT.Dispose, e -> EventQueue.invokeLater(() -> {
		    try {
			if (swingKeyListener != null) {
			    frame.removeKeyListener(swingKeyListener);
			}
			if (swingMouseListener != null) {
			    frame.removeMouseMotionListener(swingMouseListener);
			}
			frame.removeAll();
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



    @Override
    protected void privateSetDimensions(final int width, final int height) {
	WorkbenchHelper.asyncRun(() -> {
	    if (isDisposed()) {
		return;
	    }
	    org.eclipse.swt.graphics.Rectangle r = this.getBounds();
	    int w = r.width;
	    int h = r.height;
	    EventQueue.invokeLater(() -> {
		boolean sizeChanged = surface.getWidth() != w || surface.getHeight() != h;
		if (sizeChanged) {
		    surface.setBounds(0, 0, w, h);
		}
	    });
	});
    }
}
