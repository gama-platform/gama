/*******************************************************************************************************
 *
 * KeyCombination.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

import java.util.Locale;
import java.util.List;

import gama.api.additions.delegates.IEventLayerDelegate;

/** A toolkit-independent key and exact set of modifiers, used only by explicit combination events. */
public record KeyCombination(String key, int modifiers) {

	public static final int CTRL = 1;
	public static final int SHIFT = 2;
	public static final int ALT = 4;
	public static final int CMD = 8;

	private static final List<String> MODIFIERS =
			List.of("control", "ctrl", "shift", "alt", "command", "cmd", "meta", "windows", "win");

	public KeyCombination {
		key = key.length() == 1 ? String.valueOf(Character.toLowerCase(key.charAt(0))) : key.toLowerCase(Locale.ROOT);
	}

	public static int modifiers(final boolean ctrl, final boolean shift, final boolean alt, final boolean cmd) {
		return (ctrl ? CTRL : 0) | (shift ? SHIFT : 0) | (alt ? ALT : 0) | (cmd ? CMD : 0);
	}

	/** Parse modifier names followed by a character or named key, e.g. ctrl+shift+a or alt+arrow_up. */
	public static KeyCombination parse(final String trigger) {
		final String[] parts = expandCompactModifiers(trigger).split("\\+", -1);
		final boolean plusKey = parts.length >= 3 && parts[parts.length - 1].isEmpty()
				&& parts[parts.length - 2].isEmpty();
		final int keyIndex = parts.length - (plusKey ? 2 : 1);
		if (keyIndex == 0) throw new IllegalArgumentException("A key combination must include a modifier and a key");
		int modifiers = 0;
		for (int i = 0; i < keyIndex; i++) {
			final int modifier = switch (parts[i].toLowerCase(Locale.ROOT)) {
				case "ctrl", "control" -> CTRL;
				case "shift" -> SHIFT;
				case "alt" -> ALT;
				case "cmd", "command", "meta", "win", "windows" -> CMD;
				default -> throw new IllegalArgumentException("Unknown key modifier '" + parts[i] + "'");
			};
			if ((modifiers & modifier) != 0) throw new IllegalArgumentException("Duplicate key modifier '" + parts[i] + "'");
			modifiers |= modifier;
		}
		String key = plusKey ? "+" : parts[keyIndex];
		if ("space".equalsIgnoreCase(key)) key = " ";
		final KeyCombination result = new KeyCombination(key, modifiers);
		if (result.key.length() != 1 || Character.isISOControl(result.key.charAt(0))) {
			if (!IEventLayerDelegate.KEYBOARD_EVENTS.contains(result.key)
					|| switch (result.key) {
						case "ctrl", "shift", "alt", "cmd" -> true;
						default -> false;
					}) {
				throw new IllegalArgumentException("Expected a character or named keyboard key, got '" + key + "'");
			}
		}
		return result;
	}

	/** Constants such as #ctrl + "a" evaluate to compact strings such as "ctrla". */
	public static boolean isCombination(final String trigger) {
		if (trigger.length() <= 1) return false;
		if (trigger.contains("+")) return true;
		final String lower = trigger.toLowerCase(Locale.ROOT);
		return MODIFIERS.stream().anyMatch(modifier -> lower.startsWith(modifier) && lower.length() > modifier.length());
	}

	private static String expandCompactModifiers(final String trigger) {
		final StringBuilder result = new StringBuilder();
		String remaining = trigger;
		while (remaining.length() > 1) {
			final String lower = remaining.toLowerCase(Locale.ROOT);
			String prefix = null;
			for (final String modifier : MODIFIERS) {
				if (lower.startsWith(modifier) && lower.length() > modifier.length()) {
					prefix = modifier;
					break;
				}
			}
			if (prefix == null) break;
			result.append(prefix).append('+');
			remaining = remaining.substring(prefix.length());
			if (remaining.startsWith("+")) {
				remaining = remaining.substring(1);
				break;
			}
		}
		return result.append(remaining).toString();
	}

	/** Map GAMA special-key codes without depending on SWT, AWT or NEWT key codes. */
	public static String specialKey(final int code) {
		return switch (code) {
			case IEventLayerListener.ARROW_UP -> "arrow_up";
			case IEventLayerListener.ARROW_DOWN -> "arrow_down";
			case IEventLayerListener.ARROW_LEFT -> "arrow_left";
			case IEventLayerListener.ARROW_RIGHT -> "arrow_right";
			case IEventLayerListener.KEY_TAB -> "tab";
			case IEventLayerListener.KEY_ESC -> "escape";
			case IEventLayerListener.KEY_PAGE_UP -> "page_up";
			case IEventLayerListener.KEY_PAGE_DOWN -> "page_down";
			case IEventLayerListener.KEY_RETURN -> "enter";
			default -> null;
		};
	}
}
