/*******************************************************************************************************
 *
 * Mutation.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/

package gama.core.kernel.batch.optimization.genetic;

import java.util.List;

import gama.core.kernel.experiment.IParameter;
import gama.core.runtime.IScope;

/**
 * The Interface Mutation.
 */
public interface Mutation {

	/**
	 * Mutate.
	 *
	 * @param scope the scope
	 * @param chromosome the chromosome
	 * @param variables the variables
	 * @return the chromosome
	 */
	public Chromosome mutate(IScope scope, Chromosome chromosome, List<IParameter.Batch> variables);

}
