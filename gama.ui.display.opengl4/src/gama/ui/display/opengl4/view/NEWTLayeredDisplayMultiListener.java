/*******************************************************************************************************
 *
 * NEWTLayeredDisplayMultiListener.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.view;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;

import gama.api.runtime.SystemInfo;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.layers.IEventLayerListener;
import gama.api.utils.interfaces.IDisposable;
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

	/** The key listener. */
	final Consumer<Short> keyListenerForWindows;

	/** The key listener for mac and linux. */
	final Consumer<Character> keyListenerForMac;

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
			final boolean controlOk = control != null;
			if (!controlOk) return false;
			return surface != null && !surface.isDisposed();
		};

		keyListenerForMac = keyCode -> {
			switch (keyCode) {
				case 'o':
				case 'O':
					deco.toggleOverlay();
					break;
				case 't':
				case 'T':
					deco.toggleToolbar();
			}
		};

		keyListenerForWindows = code -> {
			switch (code) {
				// "o"
				case 0x4f:
					deco.toggleOverlay();
					break;
				// "t"
				case 0x54:
					deco.toggleToolbar();
					break;
			}
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
		// Bug on Windows : the character returned contains the modifiers despite the documentation saying the contrary
		boolean isPrintable = SystemInfo.isWindows() || SystemInfo.isLinux()
				? KeyEvent.isPrintableKey(e.getKeySymbol(), true) : e.isPrintableKey();
		boolean isCommand = SystemInfo.isMac() ? e.isMetaDown() : e.isControlDown();
		if (isPrintable) {
			if (isCommand) {
				if (SystemInfo.isWindows() || SystemInfo.isLinux()) {
					keyListenerForWindows.accept(e.getKeySymbol());
				} else {
					keyListenerForMac.accept(e.getKeyChar());
				}
			} else {
				delegate.keyPressed(e.getKeyChar());
			}
		} else if (e.getModifiers() == 0
				|| e.isAutoRepeat() && !e.isAltDown() && !e.isControlDown() && !e.isShiftDown() && !e.isMetaDown()) {
			delegate.specialKeyPressed(mapKeyCodeToEventAction(e.getKeyCode()));
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		if (e.isAutoRepeat()) return;
		DEBUG.OUT("Key released in Newt listener: " + e);
		if (!ok.get()) return;
		boolean isPrintable = SystemInfo.isWindows() || SystemInfo.isLinux()
				? KeyEvent.isPrintableKey(e.getKeySymbol(), true) : e.isPrintableKey();
		boolean isCommand = SystemInfo.isMac() ? e.isMetaDown() : e.isControlDown();
		if (isPrintable && !isCommand) {
			delegate.keyReleased(e.getKeyChar());
		} else if (e.getModifiers() == 0
				|| e.isAutoRepeat() && !e.isAltDown() && !e.isControlDown() && !e.isShiftDown() && !e.isMetaDown()) {
			delegate.specialKeyReleased(mapKeyCodeToEventAction(e.getKeyCode()));
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
