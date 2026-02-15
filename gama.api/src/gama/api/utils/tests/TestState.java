/*******************************************************************************************************
 *
 * TestState.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import gama.api.data.objects.IColor;
import gama.api.runtime.scope.IScope;
import gama.api.utils.IColored;
import gama.api.utils.color.GamaColorFactory;

/**
 * The Enum TestState.
 */
public enum TestState implements IColored {

	/** The aborted. */
	ABORTED("error"),
	/** The failed. */
	FAILED("failed"),
	/** The warning. */
	WARNING("warning"),
	/** The passed. */
	PASSED("passed"),
	/** The not run. */
	NOT_RUN("not run");

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new test state.
	 *
	 * @param s
	 *            the s
	 */
	TestState(final String s) {
		name = s;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	@Override
	public IColor getColor(final IScope scope) {
		return switch (this) {
			case FAILED -> GamaColorFactory.get("gamared");
			case NOT_RUN -> GamaColorFactory.get("gamablue");
			case WARNING -> GamaColorFactory.get("gamaorange");
			case PASSED -> GamaColorFactory.get("gamagreen");
			default -> GamaColorFactory.get(83, 95, 107); // GamaColors.toGamaColor(IGamaColors.NEUTRAL.color());
		};
	}
}