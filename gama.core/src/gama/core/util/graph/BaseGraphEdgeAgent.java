/*******************************************************************************************************
 *
 * BaseGraphEdgeAgent.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util.graph;

import gama.annotations.doc;
import gama.annotations.species;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;

/**
 * The Class BaseGraphEdgeAgent.
 */
@species (
		name = "base_edge",
		doc = @doc ("A built-in species for agents representing the edges of a graph, from which one can inherit"))
@doc ("A built-in species for agents representing the edges of a graph, from which one can inherit")
public class BaseGraphEdgeAgent extends AbstractGraphEdgeAgent {

	/**
	 * Instantiates a new base graph edge agent.
	 *
	 * @param s the s
	 * @param index the index
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	public BaseGraphEdgeAgent(final IPopulation<? extends IAgent> s, final int index) throws GamaRuntimeException {
		super(s, index);
	}

}