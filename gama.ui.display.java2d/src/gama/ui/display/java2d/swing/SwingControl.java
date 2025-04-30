/*******************************************************************************************************
 *
 * SwingControl.java, in gama.ui.display.java2d, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.java2d.swing;

import java.awt.Frame;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import gama.core.runtime.PlatformHelper;
import gama.dev.DEBUG;
import gama.ui.display.java2d.AWTDisplayView;
import gama.ui.display.java2d.Java2DDisplaySurface;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class SwingControl.
 */
public abstract class SwingControl extends Composite {

	static {
		DEBUG.ON();
	}

	/**
	 * Creates the.
	 *
	 * @param parent
	 *            the parent
	 * @param view
	 *            the view
	 * @param surface
	 *            the surface
	 * @param style
	 *            the style
	 * @return the composite
	 */
	public static Composite create(final Composite parent, final AWTDisplayView view,
			final Java2DDisplaySurface surface, final int style) {
		if (PlatformHelper.isLinux()) return new SwingControlLinux(parent, view, surface, style);
		if (PlatformHelper.isWindows()) return new SwingControlWin(parent, view, surface, style);
		if (PlatformHelper.isMac()) return new SwingControlMac(parent, view, surface, style);
		return null;
	}

	/** The multi listener. */
	KeyListener swingKeyListener;

	/** The swing mouse listener. */
	MouseMotionListener swingMouseListener;

	/** The frame. */
	Frame frame;

	/** The surface. */
	Java2DDisplaySurface surface;

	/** The populated. */
	volatile boolean populated = false;

	/** The visible. */
	volatile boolean visible = false;

	/**
	 * Instantiates a new swing control.
	 *
	 * @param parent
	 *            the parent
	 * @param awtDisplayView
	 * @param style
	 *            the style
	 */
	public SwingControl(final Composite parent, final AWTDisplayView view, final Java2DDisplaySurface component,
			final int style) {
		super(parent, style | ((style & SWT.BORDER) == 0 ? SWT.EMBEDDED : 0) | SWT.NO_BACKGROUND);
		setEnabled(false);
		this.surface = component;
		IPartListener2 listener = new IPartListener2() {

			@Override
			public void partHidden(final IWorkbenchPartReference partRef) {
				if (partRef.getPart(false).equals(view)) {
					// DEBUG.OUT("Hidden event received for " + view.getTitle());
					visible = false;
				}
			}

			@Override
			public void partVisible(final IWorkbenchPartReference partRef) {
				// DEBUG.OUT("Visible event received for " + view.getTitle());
				if (partRef.getPart(false).equals(view)) { visible = true; }
			}
		};
		WorkbenchHelper.getPage().addPartListener(listener);
		addListener(SWT.Dispose, event -> {
			WorkbenchHelper.getPage().removePartListener(listener);
			// map.forEach((k, l) -> { removeListener(k, l); });
			this.setData("org.eclipse.swt.awt.SWT_AWT.embeddedFrame", null);
		});
		setLayout(new FillLayout());
	}

	@Override
	public void checkWidget() {}

	@Override
	public boolean isFocusControl() {
		boolean result = false;
		try {
			result = super.isFocusControl();
		} catch (final Exception e) {
			// Nothing. Eliminates annoying exceptions when closing Java2D displays.
			// However, it denotes that some listeners are still active while they should have been disposed a while
			// ago. Therefore contributing to issue #489 (Memory leak in Java2D displays). The only solution is to
			// remove the listeners from Display (as SWT_AWT does not do it correctly)
			try {
				removeListenerFrom(WorkbenchHelper.getDisplay());
				removeListenerFrom(WorkbenchHelper.getWindow().getShell());
			} catch (Exception e1) {
				// nothing
			}

		}
		return result;
	}

	/**
	 * A terrible hack to be able to dispose of the listeners installed by SWT_AWT and wrongly removed (the shell it
	 * installs them on is not the shell it removes them from)
	 */
	private void removeListenerFrom(final Object object) throws Exception {
		Field field;
		if (object instanceof Widget) {
			field = Widget.class.getDeclaredField("eventTable");
		} else {
			field = object.getClass().getDeclaredField("eventTable");
		}
		field.setAccessible(true);
		Object table = field.get(object);
		Class<?> eventTableClass = table.getClass();
		field = eventTableClass.getDeclaredField("listeners");
		field.setAccessible(true);
		Method method = eventTableClass.getDeclaredMethod("remove", int.class);
		method.setAccessible(true);
		Listener[] listeners = (Listener[]) field.get(table);
		listeners = listeners.clone();
		for (int i = 0; i < listeners.length; i++) {
			Listener listener = listeners[i];
			if (listener != null && listener.getClass().getName().contains("SWT_AWT")) {
				method.invoke(table, i);
				DEBUG.OUT("Removed " + listener.getClass().getName());
			}
		}
	}
	// TODO Auto-generated method stub

	/**
	 * Populate.
	 */
	protected abstract void populate();

	/**
	 * Overridden to propagate the size to the embedded Swing component.
	 */
	@Override
	public final void setBounds(final int x, final int y, final int width, final int height) {
		// DEBUG.OUT("-- SwingControl bounds set to " + x + " " + y + " | " + width + " " + height);
		populate();
		// See Issue #3426
		super.setBounds(x, y, width, height);
		this.privateSetDimensions(width, height);
	}

	/** The map. Keep the listeners to remove them after disposal */
	SetMultimap<Integer, Listener> map = HashMultimap.create();

	@Override
	public void addListener(final int eventType, final Listener listener) {
		map.put(eventType, listener);
		super.addListener(eventType, listener);
	}

	/**
	 * Private set dimensions.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	protected void privateSetDimensions(final int width, final int height) {}

	/**
	 * Sets the key listener.
	 *
	 * @param adapter
	 *            the new key listener
	 */
	public void setKeyListener(final KeyListener adapter) { swingKeyListener = adapter; }

	/**
	 * Sets the mouse listener.
	 *
	 * @param adapter
	 *            the new mouse listener
	 */
	public void setMouseListener(final MouseMotionListener adapter) { swingMouseListener = adapter; }

	@Override
	public void dispose() {
		super.dispose();
		// Removes the reference to the different objects (see #489)
		surface = null;
		frame = null;
		swingMouseListener = null;
		swingKeyListener = null;
	}

}
