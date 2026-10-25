package gama.ui.display.opengl.renderer.helpers;

import java.lang.reflect.Field;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;

import gama.api.runtime.SystemInfo;

/** Standalone checks for platform-specific camera modifiers shared by OpenGL and OpenGL4. */
public final class CameraModifierTest {

	private static void check(final boolean expected, final boolean actual) {
		if (expected != actual) throw new AssertionError("Expected " + expected + ", got " + actual);
	}

	public static void main(final String[] args) throws Exception {
		final Field mac = SystemInfo.class.getDeclaredField("isMac");
		mac.setAccessible(true);
		final boolean original = mac.getBoolean(null);
		try {
			for (final boolean isMac : new boolean[] { true, false }) {
				mac.setBoolean(null, isMac);
				for (final int modifiers : new int[] { 0, InputEvent.CTRL_MASK, InputEvent.META_MASK,
						InputEvent.CTRL_MASK | InputEvent.META_MASK, InputEvent.SHIFT_MASK }) {
					final boolean expected = (modifiers & (isMac ? InputEvent.META_MASK : InputEvent.CTRL_MASK)) != 0;
					check(expected, CameraHelper.isControlDown(KeyEvent.create(KeyEvent.EVENT_KEY_PRESSED,
							CameraModifierTest.class, 1, modifiers, KeyEvent.VK_A, KeyEvent.VK_A, 'a')));
					check(expected, CameraHelper.isControlDown(new MouseEvent(MouseEvent.EVENT_MOUSE_DRAGGED,
							CameraModifierTest.class, 1, modifiers, 10, 20, (short) 1, (short) 1, new float[3], 1)));
				}
				check(isMac, CameraHelper.isCameraModifierKey(KeyEvent.VK_META));
				check(!isMac, CameraHelper.isCameraModifierKey(KeyEvent.VK_CONTROL));
				check(false, CameraHelper.isCameraModifierKey(KeyEvent.VK_SHIFT));
				check(false, CameraHelper.isCameraModifierKey(KeyEvent.VK_ALT));
				final int active = isMac ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
				final short inactiveKey = isMac ? KeyEvent.VK_CONTROL : KeyEvent.VK_META;
				final KeyEvent release = KeyEvent.create(KeyEvent.EVENT_KEY_RELEASED, CameraModifierTest.class, 2,
						active, inactiveKey, inactiveKey, '\0');
				check(true, !CameraHelper.isCameraModifierKey(release.getKeySymbol()) && CameraHelper.isControlDown(release));
			}
		} finally {
			mac.setBoolean(null, original);
		}
	}
}
