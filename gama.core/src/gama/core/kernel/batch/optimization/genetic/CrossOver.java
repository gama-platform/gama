/*******************************************************************************************************
 *
 * CrossOver.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/

package gama.core.kernel.batch.optimization.genetic;

import java.util.Set;

import gama.core.runtime.IScope;

/**
 * The Interface CrossOver.
 */
public interface CrossOver {

	/**
	 * Cross over.
	 *
	 * @param scope the scope
	 * @param parent1 the parent 1
	 * @param parent2 the parent 2
	 * @return the sets the
	 */
	Set<Chromosome> crossOver(IScope scope, final Chromosome parent1, final Chromosome parent2);
}
