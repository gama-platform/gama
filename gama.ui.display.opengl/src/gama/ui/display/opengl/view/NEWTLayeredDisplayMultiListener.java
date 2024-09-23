/*******************************************************************************************************
 *
 * NEWTLayeredDisplayMultiListener.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.view;

import java.util.function.Supplier;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IDisposable;
import gama.core.outputs.layers.IEventLayerListener;
import gama.core.runtime.PlatformHelper;
import gama.dev.DEBUG;
import gama.ui.experiment.views.displays.LayeredDisplayDecorator;
import gama.ui.experiment.views.displays.LayeredDisplayMultiListener;

/**
 * A listener for NEWT events
 *
 */
public class NEWTLayeredDisplayMultiListener implements MouseListener, KeyListener, WindowListener, IDisposable {

	static {
		DEBUG.OFF();
	}

	/** The delegate. */
	final LayeredDisplayMultiListener delegate;

	/** The control. */
	final Window control;

	/** The ok. */
	final Supplier<Boolean> ok;

	/**
	 * Instantiates a new NEWT layered display multi listener.
	 *
	 * @param deco
	 *            the deco
	 * @param surface
	 *            the surface
	 * @param window
	 *            the window
	 */
	public NEWTLayeredDisplayMultiListener(final LayeredDisplayDecorator deco, final IDisplaySurface surface,
			final Window window) {

		delegate = new LayeredDisplayMultiListener(surface, deco);
		control = window;

		ok = () -> {
			final boolean viewOk = deco.view != null && !deco.view.disposed;
			if (!viewOk) return false;
			final boolean controlOk = control != null /* && !control.isDisposed() */;
			if (!controlOk) return false;

			// if (!Objects.equals(WorkbenchHelper.getActivePart(), deco.view)) {
			// WorkbenchHelper.getPage().activate(deco.view);
			// }
			return surface != null && !surface.isDisposed();
		};

		control.addKeyListener(this);
		control.addMouseListener(this);
		control.addWindowListener(this);
	}

	/**
	 * Dispose.
	 */
	@Override
	public void dispose() {
		control.removeKeyListener(this);
		control.removeMouseListener(this);
		control.removeWindowListener(this);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		DEBUG.OUT("Key pressed in Newt listener: " + e);
		if (!ok.get()) return;
		if (e.isPrintableKey()) {
			delegate.keyPressed(e.getKeyChar(), PlatformHelper.isMac() ? e.isMetaDown() : e.isControlDown());
		} else if (e.getModifiers() == 0
				|| e.isAutoRepeat() && !e.isAltDown() && !e.isControlDown() && !e.isShiftDown() && !e.isMetaDown()) {
			delegate.specialKeyPressed(switch (e.getKeyCode()) {
				case KeyEvent.VK_UP -> IEventLayerListener.ARROW_UP;
				case KeyEvent.VK_DOWN -> IEventLayerListener.ARROW_DOWN;
				case KeyEvent.VK_LEFT -> IEventLayerListener.ARROW_LEFT;
				case KeyEvent.VK_RIGHT -> IEventLayerListener.ARROW_RIGHT;
				case KeyEvent.VK_PAGE_UP -> IEventLayerListener.KEY_PAGE_UP;
				case KeyEvent.VK_PAGE_DOWN -> IEventLayerListener.KEY_PAGE_DOWN;
				case KeyEvent.VK_ESCAPE -> IEventLayerListener.KEY_ESC;
				case KeyEvent.VK_ENTER -> IEventLayerListener.KEY_RETURN;
				case KeyEvent.VK_TAB -> IEventLayerListener.KEY_TAB;
				case KeyEvent.VK_SHIFT -> IEventLayerListener.KEY_SHIFT;
				case KeyEvent.VK_ALT -> IEventLayerListener.KEY_ALT;
				case KeyEvent.VK_CONTROL -> IEventLayerListener.KEY_CTRL;
				case KeyEvent.VK_META -> IEventLayerListener.KEY_CMD;
				default -> 0;
			});
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		if (e.isAutoRepeat()) return;
		DEBUG.OUT("Key released in Newt listener: " + e);
		if (!ok.get()) return;
		if (e.isPrintableKey()) {
			delegate.keyReleased(e.getKeyChar(),
					PlatformHelper.isMac() ? e.isMetaDown() : e.isControlDown() /* ?? GamaKeyBindings.ctrl(e) */);
		} else if (e.getModifiers() == 0
				|| e.isAutoRepeat() && !e.isAltDown() && !e.isControlDown() && !e.isShiftDown() && !e.isMetaDown()) {
			delegate.specialKeyReleased(switch (e.getKeyCode()) {
				case KeyEvent.VK_UP -> IEventLayerListener.ARROW_UP;
				case KeyEvent.VK_DOWN -> IEventLayerListener.ARROW_DOWN;
				case KeyEvent.VK_LEFT -> IEventLayerListener.ARROW_LEFT;
				case KeyEvent.VK_RIGHT -> IEventLayerListener.ARROW_RIGHT;
				case KeyEvent.VK_PAGE_UP -> IEventLayerListener.KEY_PAGE_UP;
				case KeyEvent.VK_PAGE_DOWN -> IEventLayerListener.KEY_PAGE_DOWN;
				case KeyEvent.VK_ESCAPE -> IEventLayerListener.KEY_ESC;
				case KeyEvent.VK_ENTER -> IEventLayerListener.KEY_RETURN;
				case KeyEvent.VK_TAB -> IEventLayerListener.KEY_TAB;
				default -> 0;
			});
		}
	}

	/**
	 * Checks for modifiers.
	 *
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	private boolean hasModifiers(final MouseEvent e) {
		return e.isAltDown() || e.isAltGraphDown() || e.isControlDown() || e.isMetaDown() || e.isShiftDown();
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		if (!ok.get()) return;
		delegate.mouseEnter(e.getX(), e.getY(), hasModifiers(e), e.getButton());
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if (!ok.get()) return;
		delegate.mouseExit(e.getX(), e.getY(), hasModifiers(e), e.getButton());
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if (!ok.get()) return;
		delegate.mouseMove(e.getX(), e.getY(), hasModifiers(e));
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (!ok.get()) return;
		// DEBUG.OUT("Mouse pressed with button " + e.getButton() + " modifiers " + e.getModifiersString(null));
		if (e.getButton() == 3 || e.isControlDown()) {
			delegate.menuDetected(e.getX(), e.getY());
		} else {
			delegate.mouseDown(e.getX(), e.getY(), e.getButton(), hasModifiers(e));
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (!ok.get()) return;
		delegate.mouseUp(e.getX(), e.getY(), e.getButton(), hasModifiers(e));
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (!ok.get()) return;
		delegate.dragDetected(e.getX(), e.getY());
	}

	@Override
	public void windowResized(final WindowEvent e) {}

	@Override
	public void windowMoved(final WindowEvent e) {}

	@Override
	public void windowDestroyNotify(final WindowEvent e) {}

	@Override
	public void windowDestroyed(final WindowEvent e) {}

	@Override
	public void windowGainedFocus(final WindowEvent e) {
		if (!ok.get()) return;
		delegate.focusGained();
	}

	@Override
	public void windowLostFocus(final WindowEvent e) {
		if (!ok.get()) return;
		delegate.focusLost();

	}

	@Override
	public void windowRepaint(final WindowUpdateEvent e) {}

	@Override
	public void mouseClicked(final MouseEvent e) {
		this.mouseReleased(e);
	}

	@Override
	public void mouseWheelMoved(final MouseEvent e) {
		this.mouseMoved(e);
	}

}
