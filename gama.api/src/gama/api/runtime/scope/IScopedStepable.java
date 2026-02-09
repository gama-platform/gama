/*******************************************************************************************************
 *
 * IScopedStepable.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

import gama.api.runtime.IStepable;

/**
 * Represent GAMA stepables that generate their own scope
 *
 * @author A. Drogoul
 *
 */

public interface IScopedStepable extends IScoped, IStepable {

	/**
	 * Step.
	 *
	 * @return true, if successful
	 */
	default boolean step() {
		return getScope().step(this).passed();
	}

	/**
	 * Inits the.
	 *
	 * @return true, if successful
	 */
	default boolean init() {
		return getScope().init(this).passed();
	}

}
