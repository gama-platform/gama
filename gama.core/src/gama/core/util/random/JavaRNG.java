/*******************************************************************************************************
 *
 * JavaRNG.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.random;

import gama.core.common.util.RandomUtils;

/**
 * The {@link MersenneTwisterRNG} should be used in preference to this class because it is statistically more random and
 * performs slightly better.
 */
public class JavaRNG extends GamaRNG {

	/**
	 * Seed the RNG using the provided seed generation strategy.
	 *
	 * @param seedGenerator
	 *            The seed generation strategy that will provide the seed value for this RNG.
	 */
	public JavaRNG(final RandomUtils seedGenerator) {
		super(seedGenerator.generateSeed(8));
	}

}
