/*******************************************************************************************************
 *
 * TestState.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.test;

import gama.core.common.interfaces.IColored;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.core.util.GamaColorFactory;

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
	public GamaColor getColor(final IScope scope) {
		return switch (this) {
			case FAILED -> GamaColorFactory.get("gamared");
			case NOT_RUN -> GamaColorFactory.get("gamablue");
			case WARNING -> GamaColorFactory.get("gamaorange");
			case PASSED -> GamaColorFactory.get("gamagreen");
			default -> GamaColorFactory.get(83, 95, 107); // GamaColors.toGamaColor(IGamaColors.NEUTRAL.color());
		};
	}
}