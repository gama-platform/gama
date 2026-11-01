/*******************************************************************************************************
 *
 * NEWTUserInput.java, in gama.ui.display.opengl, is part of the source code of the GAMA platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.view;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;

import gama.api.ui.layers.UserInputEvent;

/** Normalizes NEWT input for both OpenGL display implementations. */
public final class NEWTUserInput {

	private NEWTUserInput() {}

	public static String key(final KeyEvent e) {
		final int code = e.getKeyCode();
		if (code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F24) return "f" + (code - KeyEvent.VK_F1 + 1);
		final String named = switch (code) {
			case KeyEvent.VK_UP -> "arrow_up";
			case KeyEvent.VK_DOWN -> "arrow_down";
			case KeyEvent.VK_LEFT -> "arrow_left";
			case KeyEvent.VK_RIGHT -> "arrow_right";
			case KeyEvent.VK_PAGE_UP -> "page_up";
			case KeyEvent.VK_PAGE_DOWN -> "page_down";
			case KeyEvent.VK_ESCAPE -> "escape";
			case KeyEvent.VK_TAB -> "tab";
			case KeyEvent.VK_ENTER -> "enter";
			case KeyEvent.VK_SHIFT -> "shift";
			case KeyEvent.VK_CONTROL -> "ctrl";
			case KeyEvent.VK_ALT, KeyEvent.VK_ALT_GRAPH -> "alt";
			case KeyEvent.VK_META, KeyEvent.VK_WINDOWS -> "cmd";
			case KeyEvent.VK_BACK_SPACE -> "backspace";
			case KeyEvent.VK_DELETE -> "delete";
			case KeyEvent.VK_INSERT -> "insert";
			case KeyEvent.VK_HOME -> "home";
			case KeyEvent.VK_END -> "end";
			case KeyEvent.VK_CAPS_LOCK -> "caps_lock";
			case KeyEvent.VK_NUM_LOCK -> "num_lock";
			case KeyEvent.VK_SCROLL_LOCK -> "scroll_lock";
			case KeyEvent.VK_PAUSE -> "pause";
			case KeyEvent.VK_PRINTSCREEN -> "print_screen";
			case KeyEvent.VK_CONTEXT_MENU -> "menu";
			default -> null;
		};
		if (named != null) return named;
		final int symbol = code >= KeyEvent.VK_NUMPAD0 && code <= KeyEvent.VK_NUMPAD9
				? '0' + code - KeyEvent.VK_NUMPAD0 : switch (code) {
					case KeyEvent.VK_MULTIPLY -> '*';
					case KeyEvent.VK_ADD -> '+';
					case KeyEvent.VK_SUBTRACT -> '-';
					case KeyEvent.VK_DECIMAL -> '.';
					case KeyEvent.VK_DIVIDE -> '/';
					default -> e.getKeySymbol();
				};
		return UserInputEvent.keyName(e.getKeyChar(), symbol, code);
	}

	public static int button(final MouseEvent e) {
		if (e.getButton() > 0) return e.getButton();
		final short[] buttons = e.getButtonsDown();
		return buttons.length == 0 ? 0 : buttons[0];
	}
}
