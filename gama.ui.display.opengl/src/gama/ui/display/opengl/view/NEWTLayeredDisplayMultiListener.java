/*******************************************************************************************************
 *
 * NEWTLayeredDisplayMultiListener.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.view;

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

import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.runtime.SystemInfo;
import gama.api.ui.IStatusDisplayer;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.layers.IEventLayerListener;
import gama.api.ui.layers.KeyCombination;
import gama.api.utils.interfaces.IDisposable;
import gama.dev.DEBUG;
import gama.ui.experiment.views.displays.LayeredDisplayDecorator;
import gama.ui.experiment.views.displays.LayeredDisplayMultiListener;
import gama.ui.shared.utils.WorkbenchHelper;

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

	final Runnable resetCameraInteraction;

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
		resetCameraInteraction = () -> {
			if (surface instanceof SWTOpenGLDisplaySurface glSurface) {
				glSurface.renderer.getCameraHelper().cancelInteraction();
			}
		};

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
		delegate.userKeyPressed(NEWTUserInput.key(e));
		delegate.keyCombinationPressed(combinationKey(e), e.isControlDown(), e.isShiftDown(),
				e.isAltDown(), e.isMetaDown());
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
			delegate.specialKeyPressed(specialKeyCode(e.getKeyCode()));
		}
	}

	static String combinationKey(final KeyEvent e) {
		final String special = KeyCombination.specialKey(specialKeyCode(e.getKeyCode()));
		if (special != null) return special;
		if (!KeyEvent.isPrintableKey(e.getKeySymbol(), true)) return null;
		return String.valueOf(Character.isLetterOrDigit((char) e.getKeySymbol()) || Character.isISOControl(e.getKeyChar())
				? (char) e.getKeySymbol() : e.getKeyChar());
	}

	private static int specialKeyCode(final int code) {
		return switch (code) {
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
		};
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

	private int[] windowCoordinates(final MouseEvent e) {
		return control.convertToWindowUnits(new int[] { e.getX(), e.getY() });
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		if (!ok.get()) return;
		final int[] p = windowCoordinates(e);
		delegate.userMouseEvent(IEventLayerDelegate.MOUSE_ENTERED, p[0], p[1], NEWTUserInput.button(e));
		delegate.mouseEnter(p[0], p[1], hasModifiers(e), e.getButton());
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if (!ok.get()) return;
		final int[] p = windowCoordinates(e);
		delegate.userMouseEvent(IEventLayerDelegate.MOUSE_EXITED, p[0], p[1], NEWTUserInput.button(e));
		delegate.mouseExit(p[0], p[1], hasModifiers(e), e.getButton());
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if (!ok.get()) return;
		final int button = NEWTUserInput.button(e);
		final int[] p = windowCoordinates(e);
		delegate.userMouseEvent(button == 0 ? IEventLayerDelegate.MOUSE_MOVED : IEventLayerDelegate.MOUSE_DRAGGED,
				p[0], p[1], button);
		delegate.mouseMove(p[0], p[1], hasModifiers(e));
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (!ok.get()) return;
		WorkbenchHelper.getService(IStatusDisplayer.class).hideStatusPopups();
		final int[] p = windowCoordinates(e);
		delegate.userMouseEvent(IEventLayerDelegate.MOUSE_DOWN, p[0], p[1], e.getButton());
		// DEBUG.OUT("Mouse pressed with button " + e.getButton() + " modifiers " + e.getModifiersString(null));
		if (e.getButton() == 3 || e.isControlDown()) {
			delegate.userMouseEvent(IEventLayerDelegate.MOUSE_MENU, p[0], p[1], e.getButton());
			delegate.menuDetected(p[0], p[1]);
		} else {
			delegate.mouseDown(p[0], p[1], e.getButton(), hasModifiers(e));
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (!ok.get()) return;
		final int[] p = windowCoordinates(e);
		delegate.userMouseEvent(IEventLayerDelegate.MOUSE_UP, p[0], p[1], e.getButton());
		delegate.mouseUp(p[0], p[1], e.getButton(), hasModifiers(e));
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (!ok.get()) return;
		final int[] p = windowCoordinates(e);
		delegate.userMouseEvent(IEventLayerDelegate.MOUSE_DRAGGED, p[0], p[1], NEWTUserInput.button(e));
		delegate.dragDetected(p[0], p[1]);
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
		resetCameraInteraction.run();
		delegate.focusLost();

	}

	@Override
	public void windowRepaint(final WindowUpdateEvent e) {}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (!ok.get()) return;
		final int[] p = windowCoordinates(e);
		delegate.userMouseEvent(IEventLayerDelegate.MOUSE_CLICKED, p[0], p[1], e.getButton());
		delegate.mouseUp(p[0], p[1], e.getButton(), hasModifiers(e));
	}

	@Override
	public void mouseWheelMoved(final MouseEvent e) {
		if (!ok.get()) return;
		final int[] p = windowCoordinates(e);
		delegate.mouseMove(p[0], p[1], hasModifiers(e));
	}

}
