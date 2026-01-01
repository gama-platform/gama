/*******************************************************************************************************
 *
 * OkForAPI.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.annotations.precompiler;

/**
 * Indicates that the annotated element is ok to be used in the API exposed to plugins.
 */
public @interface OkForAPI {

	/**
	 * The Enum Location.
	 */
	static enum Location {
		/** The UTILS. */
		UTILS,
		/** The METHOD. */
		INTERFACES,
		/** The BOTH. */
		CONSTANTS
	}

	/**
	 * Value.
	 *
	 * @return the location
	 */
	Location value() default Location.INTERFACES;
}
