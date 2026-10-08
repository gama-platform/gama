/*******************************************************************************************************
 *
 * StoppingCriterionMaxIt.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.batch;

import java.util.Map;

/**
 * The Class StoppingCriterionMaxIt.
 */
public record StoppingCriterionMaxIt(int maxIt) implements StoppingCriterion {

	@Override
	@SuppressWarnings ("boxing")
	public boolean stopSearchProcess(final Map<String, Object> parameters) {
		return (Integer) parameters.get("Iteration") > maxIt;
	}

}
