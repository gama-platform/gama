/*******************************************************************************************************
 *
 * SWTLayeredDisplayMultiListener.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.views.displays;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.runtime.SystemInfo;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.layers.IEventLayerListener;
import gama.api.ui.layers.UserInputEvent;
import gama.api.utils.interfaces.IDisposable;
import gama.dev.DEBUG;
import gama.ui.shared.bindings.GamaKeyBindings;

/**
 * The listener interface for receiving mouse and key events. When an event occurs, that object's appropriate method is
 * invoked.
 *
 */
public class SWTLayeredDisplayMultiListener implements MenuDetectListener, MouseListener, MouseMoveListener,
		MouseTrackListener, MouseWheelListener, KeyListener, DragDetectListener, FocusListener, IDisposable {

	static {
		DEBUG.ON();
	}

	/** The delegate. */
	final LayeredDisplayMultiListener delegate;

	/** The key listener for mac and linux. */
	final Consumer<Character> keyListener;

	/** The control. */
	Control control;

	/** The ok. */
	final Supplier<Boolean> ok;

	/** The last event. */
	KeyEvent lastEvent;

	private volatile boolean awtMotionInput;
	private volatile boolean awtButtonInput;
	private volatile java.awt.Point awtDragOrigin;
	private volatile boolean awtDragged;
	private final Map<Integer, Point> mousePresses = new ConcurrentHashMap<>();
	private final Set<Integer> draggedButtons = ConcurrentHashMap.newKeySet();

	/**
	 * Instantiates a new SWT layered display multi listener.
	 *
	 * @param deco
	 *            the deco
	 * @param surface
	 *            the surface
	 */
	public SWTLayeredDisplayMultiListener(final LayeredDisplayDecorator deco, final IDisplaySurface surface) {
		// int zoom = ViewsHelper.getMonitorOf(deco.view).getZoom();
		// isJava2DOnWindows = surface != null && !surface.getData().is3D() && PlatformHelper.isWindows()
		// && zoomLevelsWithIssues.contains(zoom);
		// UIZoomLevel = zoom / 100d;

		delegate = new LayeredDisplayMultiListener(surface, deco);
		control = deco.view.getInteractionControl();
		keyListener = keyCode -> {
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
		ok = () -> {
			final boolean viewOk = deco.view != null && !deco.view.disposed;
			if (!viewOk) return false;
			final boolean controlOk = control != null && !control.isDisposed();
			if (!controlOk) return false;
			// Removed to prevent views from stealing the focus w/o control
			// if (!control.isFocusControl()) { control.forceFocus(); }
			// if (!Objects.equals(WorkbenchHelper.getActivePart(), deco.view)) {
			// WorkbenchHelper.getPage().activate(deco.view);
			// }
			return surface != null && !surface.isDisposed();
		};

		control.addKeyListener(this);
		control.addMouseListener(this);
		control.addMenuDetectListener(this);
		control.addDragDetectListener(this);
		control.addMouseTrackListener(this);
		control.addMouseMoveListener(this);
		control.addFocusListener(this);

	}

	/**
	 * Dispose.
	 */
	@Override
	public void dispose() {
		if (control == null || control.isDisposed()) return;
		control.removeKeyListener(this);
		control.removeMouseListener(this);
		control.removeMenuDetectListener(this);
		control.removeDragDetectListener(this);
		control.removeMouseTrackListener(this);
		control.removeMouseMoveListener(this);
		control.removeFocusListener(this);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (!ok.get() || String.valueOf(e).equals(String.valueOf(lastEvent))) return;
		lastEvent = e;
		DEBUG.OUT("Key pressed " + e);
		delegate.userKeyPressed(userKey(e.keyCode, e.character));
		delegate.keyCombinationPressed(combinationKey(e.keyCode, e.character), (e.stateMask & SWT.CTRL) != 0,
				(e.stateMask & SWT.SHIFT) != 0, (e.stateMask & SWT.ALT) != 0,
				(e.stateMask & SWT.COMMAND) != 0);
		switch (e.keyCode) {
			case SWT.ARROW_DOWN:
				delegate.specialKeyPressed(IEventLayerListener.ARROW_DOWN);
				return;
			case SWT.ARROW_UP:
				delegate.specialKeyPressed(IEventLayerListener.ARROW_UP);
				return;
			case SWT.ARROW_LEFT:
				delegate.specialKeyPressed(IEventLayerListener.ARROW_LEFT);
				return;
			case SWT.ARROW_RIGHT:
				delegate.specialKeyPressed(IEventLayerListener.ARROW_RIGHT);
				return;
			case SWT.ESC:
				delegate.specialKeyPressed(IEventLayerListener.KEY_ESC);
				return;
			case SWT.TAB:
				delegate.specialKeyPressed(IEventLayerListener.KEY_TAB);
				return;
			case SWT.PAGE_DOWN:
				delegate.specialKeyPressed(IEventLayerListener.KEY_PAGE_DOWN);
				return;
			case SWT.PAGE_UP:
				delegate.specialKeyPressed(IEventLayerListener.KEY_PAGE_UP);
				return;
			case SWT.CR, SWT.KEYPAD_CR:
				delegate.specialKeyPressed(IEventLayerListener.KEY_RETURN);
				return;
			case SWT.COMMAND:
				delegate.specialKeyPressed(IEventLayerListener.KEY_CMD);
				return;
			case SWT.SHIFT:
				delegate.specialKeyPressed(IEventLayerListener.KEY_SHIFT);
				return;
			case SWT.ALT:
				delegate.specialKeyPressed(IEventLayerListener.KEY_ALT);
				return;
			case SWT.CTRL:
				delegate.specialKeyPressed(IEventLayerListener.KEY_CTRL);
		}
		if (GamaKeyBindings.ctrl(e)) {
			if (SystemInfo.isMac()) { keyListener.accept((char) e.keyCode); }
		} else {
			delegate.keyPressed((char) e.keyCode);
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		if (!ok.get() || String.valueOf(e).equals(String.valueOf(lastEvent))) return;
		lastEvent = e;
		DEBUG.OUT("Key released by the SWT listener" + e);
		switch (e.keyCode) {
			case SWT.ARROW_DOWN:
				delegate.specialKeyReleased(IEventLayerListener.ARROW_DOWN);
				return;
			case SWT.ARROW_UP:
				delegate.specialKeyReleased(IEventLayerListener.ARROW_UP);
				return;
			case SWT.ARROW_LEFT:
				delegate.specialKeyReleased(IEventLayerListener.ARROW_LEFT);
				return;
			case SWT.ARROW_RIGHT:
				delegate.specialKeyReleased(IEventLayerListener.ARROW_RIGHT);
				return;
			case SWT.ESC:
				delegate.specialKeyReleased(IEventLayerListener.KEY_ESC);
				return;
			case SWT.TAB:
				delegate.specialKeyReleased(IEventLayerListener.KEY_TAB);
				return;
			case SWT.PAGE_DOWN:
				delegate.specialKeyReleased(IEventLayerListener.KEY_PAGE_DOWN);
				return;
			case SWT.PAGE_UP:
				delegate.specialKeyReleased(IEventLayerListener.KEY_PAGE_UP);
				return;
			case SWT.CR, SWT.KEYPAD_CR:
				delegate.specialKeyReleased(IEventLayerListener.KEY_RETURN);
				return;
			case SWT.COMMAND:
				delegate.specialKeyReleased(IEventLayerListener.KEY_CMD);
				return;
			case SWT.SHIFT:
				delegate.specialKeyReleased(IEventLayerListener.KEY_SHIFT);
				return;
			case SWT.ALT:
				delegate.specialKeyReleased(IEventLayerListener.KEY_ALT);
				return;
			case SWT.CTRL:
				delegate.specialKeyReleased(IEventLayerListener.KEY_CTRL);
				return;

		}
		if (GamaKeyBindings.ctrl(e)) {
			keyListener.accept((char) e.keyCode);
		} else {
			delegate.keyReleased((char) e.keyCode);
		}
	}

	/**
	 * Filter.
	 *
	 * @param e
	 *            the e
	 */
	private void filter(final MouseEvent e) {
		// if (isJava2DOnWindows) {
		// e.x = (int) (e.x / UIZoomLevel);
		// e.y = (int) (e.y / UIZoomLevel);
		// }
	}

	@Override
	public void mouseScrolled(final MouseEvent e) {
		filter(e);
		// if (!ok.get()) {}
	}

	@Override
	public void mouseEnter(final MouseEvent e) {
		if (!ok.get()) return;
		filter(e);
		observeSwtMouse(IEventLayerDelegate.MOUSE_ENTERED, e.x, e.y, swtButton(e));
		delegate.mouseEnter(e.x, e.y, (e.stateMask & SWT.MODIFIER_MASK) != 0, e.button);
	}

	@Override
	public void mouseExit(final MouseEvent e) {
		if (!ok.get()) return;
		filter(e);
		observeSwtMouse(IEventLayerDelegate.MOUSE_EXITED, e.x, e.y, swtButton(e));
		mousePresses.clear();
		draggedButtons.clear();
		delegate.mouseExit(e.x, e.y, (e.stateMask & SWT.MODIFIER_MASK) != 0, e.button);
	}

	@Override
	public void mouseHover(final MouseEvent e) {
		if (!ok.get()) return;
		filter(e);
		delegate.mouseHover(e.x, e.y, e.button);
	}

	@Override
	public void mouseMove(final MouseEvent e) {
		if (!ok.get()) return;
		filter(e);
		// DEBUG.OUT("Mouse move " + e);
		final int button = swtButton(e);
		trackDrags(e.x, e.y);
		observeSwtMouse(button == 0 ? IEventLayerDelegate.MOUSE_MOVED : IEventLayerDelegate.MOUSE_DRAGGED,
				e.x, e.y, button);
		delegate.mouseMove(e.x, e.y, (e.stateMask & SWT.MODIFIER_MASK) != 0);
	}

	@Override
	public void mouseDoubleClick(final MouseEvent e) {
		// if (!ok.get()) {}
		// filter(e);
	}

	@Override
	public void mouseDown(final MouseEvent e) {
		if (!ok.get()) return;
		filter(e);
		DEBUG.OUT("Mouse down " + e);
		if (!awtButtonInput && e.button > 0) {
			mousePresses.put(e.button, new Point(e.x, e.y));
			draggedButtons.remove(e.button);
		}
		observeSwtButton(IEventLayerDelegate.MOUSE_DOWN, e.x, e.y, e.button);
		delegate.mouseDown(e.x, e.y, e.button, (e.stateMask & SWT.MODIFIER_MASK) != 0);
	}

	@Override
	public void mouseUp(final MouseEvent e) {
		if (!ok.get()) return;
		filter(e);
		DEBUG.OUT("Mouse up " + e);
		final Point pressed = mousePresses.remove(e.button);
		final boolean dragged = draggedButtons.remove(e.button);
		final boolean clicked = pressed != null && !dragged && !awtDragged;
		awtDragOrigin = null;
		awtDragged = false;
		observeSwtButton(IEventLayerDelegate.MOUSE_UP, e.x, e.y, e.button);
		delegate.mouseUp(e.x, e.y, e.button, (e.stateMask & SWT.MODIFIER_MASK) != 0);
		if (clicked) { observeSwtButton(IEventLayerDelegate.MOUSE_CLICKED, e.x, e.y, e.button); }
	}

	@Override
	public void menuDetected(final MenuDetectEvent e) {
		if (!ok.get()) return;
		// Verify if the same "filter" is not needed here too.
		DEBUG.LOG("Menu detected ");
		final Point p = control.toControl(e.x, e.y);
		final int button = e.detail == SWT.MENU_KEYBOARD ? 0
				: mousePresses.containsKey(3) ? 3 : mousePresses.containsKey(1) ? 1 : 0;
		if (!awtButtonInput || e.detail == SWT.MENU_KEYBOARD) {
			delegate.userMouseEvent(IEventLayerDelegate.MOUSE_MENU, p.x, p.y, button);
		}
		delegate.menuDetected(p.x, p.y);
	}

	@Override
	public void dragDetected(final DragDetectEvent e) {
		if (!ok.get()) return;
		filter(e);
		draggedButtons.addAll(mousePresses.keySet());
		delegate.dragDetected(e.x, e.y);
	}

	@Override
	public void focusGained(final FocusEvent e) {
		delegate.focusGained();
	}

	@Override
	public void focusLost(final FocusEvent e) {
		mousePresses.clear();
		draggedButtons.clear();
		delegate.focusLost();
	}

	/**
	 * Observe generic mouse motion (and, if {@code buttons} is true, button and menu events) from the embedded AWT
	 * component instead of its SWT parent. SWT events still drive the existing display controls. On macOS, button
	 * presses over an embedded AWT frame are only delivered to SWT, so buttons must stay on SWT there.
	 */
	public void useAwtMouseInput(final boolean buttons) {
		awtMotionInput = true;
		awtButtonInput = buttons;
	}

	private void observeSwtMouse(final String action, final int x, final int y, final int button) {
		if (!awtMotionInput) { delegate.userMouseEvent(action, x, y, button); }
	}

	private void observeSwtButton(final String action, final int x, final int y, final int button) {
		if (!awtButtonInput) { delegate.userMouseEvent(action, x, y, button); }
	}

	/** Remembers AWT drags when buttons come from SWT, so that a drag is not reported as a click. */
	private void trackAwtDrag(final int x, final int y) {
		if (awtButtonInput) return;
		final java.awt.Point origin = awtDragOrigin;
		if (origin == null) {
			awtDragOrigin = new java.awt.Point(x, y);
		} else if (Math.abs(x - origin.x) > 4 || Math.abs(y - origin.y) > 4) { awtDragged = true; }
	}

	private void trackDrags(final int x, final int y) {
		for (final Map.Entry<Integer, Point> press : mousePresses.entrySet()) {
			final Point origin = press.getValue();
			if (Math.abs(x - origin.x) > 4 || Math.abs(y - origin.y) > 4) { draggedButtons.add(press.getKey()); }
		}
	}

	private static int swtButton(final MouseEvent e) {
		if (e.button > 0) return e.button;
		if ((e.stateMask & SWT.BUTTON1) != 0) return 1;
		if ((e.stateMask & SWT.BUTTON2) != 0) return 2;
		if ((e.stateMask & SWT.BUTTON3) != 0) return 3;
		if ((e.stateMask & SWT.BUTTON4) != 0) return 4;
		if ((e.stateMask & SWT.BUTTON5) != 0) return 5;
		return 0;
	}

	/**
	 * Gets the key adapter for AWT. See Issue #3426
	 *
	 * @return the key adapter for AWT
	 */
	public java.awt.event.KeyListener getKeyAdapterForAWT() {
		return new java.awt.event.KeyListener() {

			long previous;

			@Override
			public void keyTyped(final java.awt.event.KeyEvent e) {

			}

			@Override
			public void keyPressed(final java.awt.event.KeyEvent e) {
				// Necessary to filter by the time to avoid repetitions
				if (e.getWhen() == previous) return;
				previous = e.getWhen();
				DEBUG.LOG("Key received by the AWT listener. Code " + e.getKeyCode() + " Action ? " + e.isActionKey());
				delegate.userKeyPressed(awtUserKey(e));
				delegate.keyCombinationPressed(awtCombinationKey(e), e.isControlDown(), e.isShiftDown(),
						e.isAltDown(), e.isMetaDown() || e.getKeyCode() == java.awt.event.KeyEvent.VK_WINDOWS);
				if (e.isControlDown()) {
					keyListener.accept(e.getKeyChar());
				} else if (!e.isActionKey()) {
					delegate.keyPressed(e.getKeyChar());
				} else if (e.getModifiersEx() == 0) {
					delegate.specialKeyPressed(switch (e.getKeyCode()) {
						case java.awt.event.KeyEvent.VK_UP, java.awt.event.KeyEvent.VK_KP_UP -> IEventLayerListener.ARROW_UP;
						case java.awt.event.KeyEvent.VK_DOWN, java.awt.event.KeyEvent.VK_KP_DOWN -> IEventLayerListener.ARROW_DOWN;
						case java.awt.event.KeyEvent.VK_LEFT, java.awt.event.KeyEvent.VK_KP_LEFT -> IEventLayerListener.ARROW_LEFT;
						case java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.KeyEvent.VK_KP_RIGHT -> IEventLayerListener.ARROW_RIGHT;
						case java.awt.event.KeyEvent.VK_PAGE_UP -> IEventLayerListener.KEY_PAGE_UP;
						case java.awt.event.KeyEvent.VK_PAGE_DOWN -> IEventLayerListener.KEY_PAGE_DOWN;
						case java.awt.event.KeyEvent.VK_ESCAPE -> IEventLayerListener.KEY_ESC;
						case java.awt.event.KeyEvent.VK_ENTER -> IEventLayerListener.KEY_RETURN;
						case java.awt.event.KeyEvent.VK_TAB -> IEventLayerListener.KEY_TAB;
						case java.awt.event.KeyEvent.VK_SHIFT -> IEventLayerListener.KEY_SHIFT;
						case java.awt.event.KeyEvent.VK_ALT -> IEventLayerListener.KEY_ALT;
						case java.awt.event.KeyEvent.VK_CONTROL -> IEventLayerListener.KEY_CTRL;
						case java.awt.event.KeyEvent.VK_META -> IEventLayerListener.KEY_CMD;
						default -> 0;
					});
				}
			}

			@Override
			public void keyReleased(final java.awt.event.KeyEvent e) {
				DEBUG.LOG("Key released by the AWT listener. Code " + e.getKeyCode() + " Action ? " + e.isActionKey());
				if (!e.isActionKey()) {
					delegate.keyReleased(e.getKeyChar());
				} else if (e.getModifiersEx() == 0) {
					delegate.specialKeyReleased(switch (e.getKeyCode()) {
						case java.awt.event.KeyEvent.VK_UP, java.awt.event.KeyEvent.VK_KP_UP -> IEventLayerListener.ARROW_UP;
						case java.awt.event.KeyEvent.VK_DOWN, java.awt.event.KeyEvent.VK_KP_DOWN -> IEventLayerListener.ARROW_DOWN;
						case java.awt.event.KeyEvent.VK_LEFT, java.awt.event.KeyEvent.VK_KP_LEFT -> IEventLayerListener.ARROW_LEFT;
						case java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.KeyEvent.VK_KP_RIGHT -> IEventLayerListener.ARROW_RIGHT;
						case java.awt.event.KeyEvent.VK_PAGE_UP -> IEventLayerListener.KEY_PAGE_UP;
						case java.awt.event.KeyEvent.VK_PAGE_DOWN -> IEventLayerListener.KEY_PAGE_DOWN;
						case java.awt.event.KeyEvent.VK_ESCAPE -> IEventLayerListener.KEY_ESC;
						case java.awt.event.KeyEvent.VK_ENTER -> IEventLayerListener.KEY_RETURN;
						case java.awt.event.KeyEvent.VK_TAB -> IEventLayerListener.KEY_TAB;
						default -> 0;
					});
				}
			}
		};
	}

	static String userKey(final int code, final char character) {
		if (code >= SWT.F1 && code <= SWT.F20) return "f" + (code - SWT.F1 + 1);
		final String named = switch (code) {
			case SWT.ARROW_UP -> "arrow_up";
			case SWT.ARROW_DOWN -> "arrow_down";
			case SWT.ARROW_LEFT -> "arrow_left";
			case SWT.ARROW_RIGHT -> "arrow_right";
			case SWT.PAGE_UP -> "page_up";
			case SWT.PAGE_DOWN -> "page_down";
			case SWT.ESC -> "escape";
			case SWT.TAB -> "tab";
			case SWT.CR, SWT.KEYPAD_CR -> "enter";
			case SWT.SHIFT -> "shift";
			case SWT.CTRL -> "ctrl";
			case SWT.ALT -> "alt";
			case SWT.COMMAND -> "cmd";
			case SWT.BS -> "backspace";
			case SWT.DEL -> "delete";
			case SWT.INSERT -> "insert";
			case SWT.HOME -> "home";
			case SWT.END -> "end";
			case SWT.CAPS_LOCK -> "caps_lock";
			case SWT.NUM_LOCK -> "num_lock";
			case SWT.SCROLL_LOCK -> "scroll_lock";
			case SWT.PAUSE -> "pause";
			case SWT.PRINT_SCREEN -> "print_screen";
			default -> null;
		};
		if (named != null) return named;
		final int symbol = code >= SWT.KEYPAD_0 && code <= SWT.KEYPAD_9 ? '0' + code - SWT.KEYPAD_0
				: switch (code) {
					case SWT.KEYPAD_MULTIPLY -> '*';
					case SWT.KEYPAD_ADD -> '+';
					case SWT.KEYPAD_SUBTRACT -> '-';
					case SWT.KEYPAD_DECIMAL -> '.';
					case SWT.KEYPAD_DIVIDE -> '/';
					case SWT.KEYPAD_EQUAL -> '=';
					default -> code;
				};
		return UserInputEvent.keyName(character, symbol, code);
	}

	static String awtUserKey(final java.awt.event.KeyEvent e) {
		final int code = e.getKeyCode();
		if (code >= java.awt.event.KeyEvent.VK_F1 && code <= java.awt.event.KeyEvent.VK_F12)
			return "f" + (code - java.awt.event.KeyEvent.VK_F1 + 1);
		if (code >= java.awt.event.KeyEvent.VK_F13 && code <= java.awt.event.KeyEvent.VK_F24)
			return "f" + (code - java.awt.event.KeyEvent.VK_F13 + 13);
		final String named = switch (code) {
			case java.awt.event.KeyEvent.VK_UP, java.awt.event.KeyEvent.VK_KP_UP -> "arrow_up";
			case java.awt.event.KeyEvent.VK_DOWN, java.awt.event.KeyEvent.VK_KP_DOWN -> "arrow_down";
			case java.awt.event.KeyEvent.VK_LEFT, java.awt.event.KeyEvent.VK_KP_LEFT -> "arrow_left";
			case java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.KeyEvent.VK_KP_RIGHT -> "arrow_right";
			case java.awt.event.KeyEvent.VK_PAGE_UP -> "page_up";
			case java.awt.event.KeyEvent.VK_PAGE_DOWN -> "page_down";
			case java.awt.event.KeyEvent.VK_ESCAPE -> "escape";
			case java.awt.event.KeyEvent.VK_TAB -> "tab";
			case java.awt.event.KeyEvent.VK_ENTER -> "enter";
			case java.awt.event.KeyEvent.VK_SHIFT -> "shift";
			case java.awt.event.KeyEvent.VK_CONTROL -> "ctrl";
			case java.awt.event.KeyEvent.VK_ALT, java.awt.event.KeyEvent.VK_ALT_GRAPH -> "alt";
			case java.awt.event.KeyEvent.VK_META, java.awt.event.KeyEvent.VK_WINDOWS -> "cmd";
			case java.awt.event.KeyEvent.VK_BACK_SPACE -> "backspace";
			case java.awt.event.KeyEvent.VK_DELETE -> "delete";
			case java.awt.event.KeyEvent.VK_INSERT -> "insert";
			case java.awt.event.KeyEvent.VK_HOME -> "home";
			case java.awt.event.KeyEvent.VK_END -> "end";
			case java.awt.event.KeyEvent.VK_CAPS_LOCK -> "caps_lock";
			case java.awt.event.KeyEvent.VK_NUM_LOCK -> "num_lock";
			case java.awt.event.KeyEvent.VK_SCROLL_LOCK -> "scroll_lock";
			case java.awt.event.KeyEvent.VK_PAUSE -> "pause";
			case java.awt.event.KeyEvent.VK_PRINTSCREEN -> "print_screen";
			case java.awt.event.KeyEvent.VK_CONTEXT_MENU -> "menu";
			default -> null;
		};
		if (named != null) return named;
		final int symbol = code >= java.awt.event.KeyEvent.VK_NUMPAD0 && code <= java.awt.event.KeyEvent.VK_NUMPAD9
				? '0' + code - java.awt.event.KeyEvent.VK_NUMPAD0 : switch (code) {
					case java.awt.event.KeyEvent.VK_MULTIPLY -> '*';
					case java.awt.event.KeyEvent.VK_ADD -> '+';
					case java.awt.event.KeyEvent.VK_SUBTRACT -> '-';
					case java.awt.event.KeyEvent.VK_DECIMAL -> '.';
					case java.awt.event.KeyEvent.VK_DIVIDE -> '/';
					case java.awt.event.KeyEvent.VK_SEPARATOR -> ',';
					default -> {
						final String key = awtCombinationKey(e);
						yield key != null && key.length() == 1 ? key.charAt(0) : 0;
					}
				};
		return UserInputEvent.keyName(e.getKeyChar(), symbol, code);
	}

	static String combinationKey(final int code, final char character) {
		return switch (code) {
			case SWT.ARROW_UP -> "arrow_up";
			case SWT.ARROW_DOWN -> "arrow_down";
			case SWT.ARROW_LEFT -> "arrow_left";
			case SWT.ARROW_RIGHT -> "arrow_right";
			case SWT.PAGE_UP -> "page_up";
			case SWT.PAGE_DOWN -> "page_down";
			case SWT.ESC -> "escape";
			case SWT.TAB -> "tab";
			case SWT.CR, SWT.KEYPAD_CR -> "enter";
			case SWT.CTRL, SWT.SHIFT, SWT.ALT, SWT.COMMAND -> null;
			default -> {
				if (code <= 0 || code > Character.MAX_VALUE || Character.isISOControl((char) code)) yield null;
				yield String.valueOf(Character.isLetterOrDigit((char) code) || Character.isISOControl(character)
						? (char) code : character);
			}
		};
	}

	static String awtCombinationKey(final java.awt.event.KeyEvent e) {
		return switch (e.getKeyCode()) {
			case java.awt.event.KeyEvent.VK_UP, java.awt.event.KeyEvent.VK_KP_UP -> "arrow_up";
			case java.awt.event.KeyEvent.VK_DOWN, java.awt.event.KeyEvent.VK_KP_DOWN -> "arrow_down";
			case java.awt.event.KeyEvent.VK_LEFT, java.awt.event.KeyEvent.VK_KP_LEFT -> "arrow_left";
			case java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.KeyEvent.VK_KP_RIGHT -> "arrow_right";
			case java.awt.event.KeyEvent.VK_PAGE_UP -> "page_up";
			case java.awt.event.KeyEvent.VK_PAGE_DOWN -> "page_down";
			case java.awt.event.KeyEvent.VK_ESCAPE -> "escape";
			case java.awt.event.KeyEvent.VK_TAB -> "tab";
			case java.awt.event.KeyEvent.VK_ENTER -> "enter";
			case java.awt.event.KeyEvent.VK_SHIFT, java.awt.event.KeyEvent.VK_CONTROL,
					java.awt.event.KeyEvent.VK_ALT, java.awt.event.KeyEvent.VK_ALT_GRAPH,
					java.awt.event.KeyEvent.VK_META, java.awt.event.KeyEvent.VK_WINDOWS -> null;
			default -> {
				final int code = e.getKeyCode();
				if (code >= java.awt.event.KeyEvent.VK_A && code <= java.awt.event.KeyEvent.VK_Z
						|| code >= java.awt.event.KeyEvent.VK_0 && code <= java.awt.event.KeyEvent.VK_9) {
					yield String.valueOf((char) code);
				}
				final char character = e.getKeyChar();
				if (character != java.awt.event.KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(character)) {
					yield String.valueOf(character);
				}
				yield switch (code) {
					case java.awt.event.KeyEvent.VK_OPEN_BRACKET -> "[";
					case java.awt.event.KeyEvent.VK_CLOSE_BRACKET -> "]";
					case java.awt.event.KeyEvent.VK_BACK_SLASH -> "\\";
					case java.awt.event.KeyEvent.VK_MINUS -> "-";
					case java.awt.event.KeyEvent.VK_EQUALS -> "=";
					case java.awt.event.KeyEvent.VK_SEMICOLON -> ";";
					case java.awt.event.KeyEvent.VK_QUOTE -> "'";
					case java.awt.event.KeyEvent.VK_COMMA -> ",";
					case java.awt.event.KeyEvent.VK_PERIOD -> ".";
					case java.awt.event.KeyEvent.VK_SLASH -> "/";
					case java.awt.event.KeyEvent.VK_BACK_QUOTE -> "`";
					case java.awt.event.KeyEvent.VK_SPACE -> " ";
					default -> null;
				};
			}
		};
	}

	/**
	 * Gets the mouse adapter for AWT.
	 *
	 * @return the mouse adapter for AWT
	 */
	public java.awt.event.MouseMotionListener getMouseAdapterForAWT() {
		return new java.awt.event.MouseMotionListener() {

			@Override
			public void mouseDragged(final java.awt.event.MouseEvent e) {
				trackAwtDrag(e.getX(), e.getY());
				if (awtMotionInput && ok.get()) {
					delegate.userMouseEvent(IEventLayerDelegate.MOUSE_DRAGGED, e.getX(), e.getY(), awtButton(e));
				}
				delegate.dragDetected(e.getX(), e.getY());
			}

			@Override
			public void mouseMoved(final java.awt.event.MouseEvent e) {
				final int button = awtButton(e);
				if (button != 0) {
					trackAwtDrag(e.getX(), e.getY());
				} else {
					awtDragOrigin = null;
					awtDragged = false;
				}
				if (awtMotionInput && ok.get()) {
					delegate.userMouseEvent(button == 0 ? IEventLayerDelegate.MOUSE_MOVED : IEventLayerDelegate.MOUSE_DRAGGED,
							e.getX(), e.getY(), button);
				}
				delegate.mouseMove(e.getX(), e.getY(), e.getModifiers() != 0);
			}
		};
	}

	/** Gets the AWT listener reporting button, popup and enter/exit events from the embedded component. */
	public java.awt.event.MouseListener getUserMouseAdapterForAWT() {
		return new java.awt.event.MouseAdapter() {

			private void observe(final String action, final java.awt.event.MouseEvent e) {
				if (awtButtonInput && ok.get()) {
					delegate.userMouseEvent(action, e.getX(), e.getY(), awtButton(e));
				}
			}

			@Override
			public void mousePressed(final java.awt.event.MouseEvent e) {
				observe(IEventLayerDelegate.MOUSE_DOWN, e);
				if (e.isPopupTrigger()) { observe(IEventLayerDelegate.MOUSE_MENU, e); }
			}

			@Override
			public void mouseReleased(final java.awt.event.MouseEvent e) {
				observe(IEventLayerDelegate.MOUSE_UP, e);
				if (e.isPopupTrigger()) { observe(IEventLayerDelegate.MOUSE_MENU, e); }
			}

			@Override
			public void mouseClicked(final java.awt.event.MouseEvent e) {
				observe(IEventLayerDelegate.MOUSE_CLICKED, e);
			}

			@Override
			public void mouseEntered(final java.awt.event.MouseEvent e) {
				if (awtMotionInput && ok.get()) {
					delegate.userMouseEvent(IEventLayerDelegate.MOUSE_ENTERED, e.getX(), e.getY(), awtButton(e));
				}
			}

			@Override
			public void mouseExited(final java.awt.event.MouseEvent e) {
				if (awtMotionInput && ok.get()) {
					delegate.userMouseEvent(IEventLayerDelegate.MOUSE_EXITED, e.getX(), e.getY(), awtButton(e));
				}
			}
		};
	}

	private static int awtButton(final java.awt.event.MouseEvent e) {
		if (e.getButton() > 0) return e.getButton();
		final int modifiers = e.getModifiersEx();
		for (int button = 1; button <= 20; button++) {
			if ((modifiers & java.awt.event.InputEvent.getMaskForButton(button)) != 0) return button;
		}
		return 0;
	}

}
