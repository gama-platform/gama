package gama.ui.experiment.views.displays;

import java.awt.Canvas;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.InputEvent;

import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.layers.KeyCombination;

/** Standalone modifier dispatch checks for SWT, AWT and both NEWT display adapters. */
public final class KeyCombinationDispatchTest {

	private static void check(final Object expected, final Object actual) {
		if (!Objects.equals(expected, actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
	}

	public static void main(final String[] args) throws Exception {
		check("a", SWTLayeredDisplayMultiListener.combinationKey('a', '\u0001'));
		check("+", SWTLayeredDisplayMultiListener.combinationKey('=', '+'));
		check("arrow_up", SWTLayeredDisplayMultiListener.combinationKey(SWT.ARROW_UP, '\0'));
		check("enter", SWTLayeredDisplayMultiListener.combinationKey(SWT.CR, '\r'));
		check(null, SWTLayeredDisplayMultiListener.combinationKey(SWT.CTRL, '\0'));
		check(null, SWTLayeredDisplayMultiListener.combinationKey(SWT.F1, '\0'));

		final Canvas canvas = new Canvas();
		final var awtA = new java.awt.event.KeyEvent(canvas, java.awt.event.KeyEvent.KEY_PRESSED, 1,
				java.awt.event.InputEvent.CTRL_DOWN_MASK, java.awt.event.KeyEvent.VK_A, '\u0001');
		check("A", SWTLayeredDisplayMultiListener.awtCombinationKey(awtA));
		final var awtArrow = new java.awt.event.KeyEvent(canvas, java.awt.event.KeyEvent.KEY_PRESSED, 2,
				java.awt.event.InputEvent.ALT_DOWN_MASK, java.awt.event.KeyEvent.VK_UP,
				java.awt.event.KeyEvent.CHAR_UNDEFINED);
		check("arrow_up", SWTLayeredDisplayMultiListener.awtCombinationKey(awtArrow));
		final var awtBracket = new java.awt.event.KeyEvent(canvas, java.awt.event.KeyEvent.KEY_PRESSED, 3,
				java.awt.event.InputEvent.CTRL_DOWN_MASK, java.awt.event.KeyEvent.VK_OPEN_BRACKET, '\u001b');
		check("[", SWTLayeredDisplayMultiListener.awtCombinationKey(awtBracket));

		for (final String backend : List.of("opengl", "opengl4")) {
			final Class<?> listener = Class.forName("gama.ui.display." + backend + ".view.NEWTLayeredDisplayMultiListener");
			final Method key = listener.getDeclaredMethod("combinationKey", KeyEvent.class);
			key.setAccessible(true);
			check("A", key.invoke(null, newt(KeyEvent.VK_A, 'A', '\u0001', InputEvent.CTRL_MASK)));
			check("arrow_up", key.invoke(null, newt(KeyEvent.VK_UP, KeyEvent.VK_UP, '\0', InputEvent.ALT_MASK)));
			check("enter", key.invoke(null, newt(KeyEvent.VK_ENTER, KeyEvent.VK_ENTER, '\r', InputEvent.META_MASK)));
			check("+", key.invoke(null, newt(KeyEvent.VK_EQUALS, '=', '+', InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)));
			check(null, key.invoke(null, newt(KeyEvent.VK_CONTROL, KeyEvent.VK_CONTROL, '\0', InputEvent.CTRL_MASK)));
		}

		final List<KeyCombination> received = new ArrayList<>();
		final IDisplaySurface surface = new IDisplaySurface() {
			@Override
			public void dispatchKeyCombination(final KeyCombination combination) { received.add(combination); }
		};
		final var delegate = new LayeredDisplayMultiListener(surface, null);
		delegate.keyCombinationPressed("A", true, true, false, false);
		delegate.keyCombinationPressed("A", true, true, false, false);
		delegate.keyCombinationPressed(null, true, false, false, false);
		delegate.keyCombinationPressed("a", false, false, false, false);
		delegate.keyCombinationPressed("b", false, false, true, false);
		check(List.of(KeyCombination.parse("ctrl+shift+a"), KeyCombination.parse("ctrl+shift+a"),
				KeyCombination.parse("alt+b")), received);
		// A fresh native snapshot after focus loss or modifier release cannot inherit earlier modifiers.
		delegate.keyCombinationPressed("enter", false, false, false, true);
		check(KeyCombination.parse("cmd+enter"), received.getLast());
	}

	private static KeyEvent newt(final short code, final int symbol, final char character, final int modifiers) {
		return KeyEvent.create(KeyEvent.EVENT_KEY_PRESSED, KeyCombinationDispatchTest.class, 1, modifiers,
				code, (short) symbol, character);
	}
}
