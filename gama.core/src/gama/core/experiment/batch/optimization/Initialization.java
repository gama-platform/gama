/*******************************************************************************************************
 *
 * Initialization.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/

package gama.core.experiment.batch.optimization;

import java.util.List;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IParameter;
import gama.api.runtime.scope.IScope;

/**
 * The Interface Initialization.
 */
public interface Initialization {

	/**
	 * Initialize pop.
	 *
	 * @param scope the scope
	 * @param variables the variables
	 * @param algo the algo
	 * @return the list
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	List<Chromosome> initializePop(IScope scope, List<IParameter.Batch> variables, GeneticAlgorithm algo)
			throws GamaRuntimeException;
}
