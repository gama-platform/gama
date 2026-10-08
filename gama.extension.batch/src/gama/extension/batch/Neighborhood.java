/*******************************************************************************************************
 *
 * Neighborhood.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.batch;

import java.util.List;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IParameter;
import gama.api.runtime.scope.IScope;
import gama.core.experiment.*;
import gama.core.experiment.parameters.ParametersSet;

/**
 * The Class Neighborhood.
 */
public abstract class Neighborhood {

	/** The variables. */
	protected final List<IParameter.Batch> variables;

	/**
	 * Instantiates a new neighborhood.
	 *
	 * @param variables the variables
	 */
	public Neighborhood(final List<IParameter.Batch> variables) {
		this.variables = variables;
	}

	/**
	 * Neighbor.
	 *
	 * @param scope the scope
	 * @param solution the solution
	 * @return the list
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	public abstract List<ParametersSet> neighbor(IScope scope, final ParametersSet solution)
		throws GamaRuntimeException;

}
