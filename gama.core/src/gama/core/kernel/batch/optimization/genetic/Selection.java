/*******************************************************************************************************
 *
 * Selection.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/

package gama.core.kernel.batch.optimization.genetic;

import java.util.List;

import gama.core.runtime.IScope;

/**
 * The Interface Selection.
 */
public interface Selection {

	/**
	 * Select.
	 *
	 * @param scope the scope
	 * @param population the population
	 * @param populationDim the population dim
	 * @param maximize the maximize
	 * @return the list
	 */
	public List<Chromosome> select(IScope scope, final List<Chromosome> population, final int populationDim,
		boolean maximize);
}
