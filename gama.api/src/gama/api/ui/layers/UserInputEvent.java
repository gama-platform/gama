/*******************************************************************************************************
 *
 * UserInputEvent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;

/** The input being handled by an event layer, stored in its execution scope. */
public record UserInputEvent(String keyPressed, String mouseAction, String mouseButton, IPoint modelLocation,
		IPoint displayLocation) {

	public static final String SCOPE_KEY = UserInputEvent.class.getName();

	public static UserInputEvent keyboard(final String key) {
		return new UserInputEvent(key, "", "", null, null);
	}

	public static UserInputEvent mouse(final String action, final int button, final IPoint modelLocation,
			final int x, final int y) {
		final IPoint location = modelLocation == null ? null
				: GamaPointFactory.create(modelLocation.getX(), modelLocation.getY(), modelLocation.getZ());
		return new UserInputEvent("", action, buttonName(button), location, GamaPointFactory.create(x, y));
	}

	public static UserInputEvent current(final IScope scope) {
		return scope != null && scope.getData(SCOPE_KEY) instanceof UserInputEvent event ? event : null;
	}

	public static String keyPressed(final IScope scope) {
		final UserInputEvent event = current(scope);
		return event == null ? "" : event.keyPressed();
	}

	public static String mouseAction(final IScope scope) {
		final UserInputEvent event = current(scope);
		return event == null ? "" : event.mouseAction();
	}

	public static String mouseButton(final IScope scope) {
		final UserInputEvent event = current(scope);
		return event == null ? "" : event.mouseButton();
	}

	public static String buttonName(final int button) {
		return switch (button) {
			case 1 -> "left";
			case 2 -> "middle";
			case 3 -> "right";
			default -> button > 0 ? "button_" + button : "none";
		};
	}

	/** Use the produced character, or the unmodified key symbol when a shortcut produces a control character. */
	public static String keyName(final char character, final int symbol, final int nativeCode) {
		if (isPrintable(character)) return String.valueOf(character);
		if (symbol > 0 && symbol <= Character.MAX_VALUE && isPrintable((char) symbol))
			return String.valueOf(Character.toLowerCase((char) symbol));
		return "key_" + nativeCode;
	}

	private static boolean isPrintable(final char character) {
		return character != '\uffff' && !Character.isISOControl(character) && !Character.isSurrogate(character);
	}
}
