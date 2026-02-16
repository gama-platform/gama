/*******************************************************************************************************
 *
 * AbstractGraphNodeAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.graph;

import gama.annotations.action;
import gama.annotations.arg;
import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.species;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.IConcept;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IGraphAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.api.types.graph.IGraphEventProvider;
import gama.api.types.graph.VertexRelationship;
import gama.core.agent.GamlAgent;
import gama.dev.DEBUG;

/**
 * The Class AbstractGraphNodeAgent.
 */
// FIXME: Add all the necessary variables (degree, neighbors, edges)
@species (
		name = "graph_node",
		concept = { IConcept.GRAPH, IConcept.NODE },
		doc = @doc ("A base species to use as a parent for species representing agents that are nodes of a graph"))
@vars ({ @variable (
		name = IKeyword.MYGRAPH,
		type = IType.GRAPH,
		doc = @doc ("A reference to the graph containing the agent")) })
@doc ("A base species to use as a parent for species representing agents that are nodes of a graph")
public class AbstractGraphNodeAgent extends GamlAgent implements IGraphAgent {

	/** The Constant args. */
	final static Arguments args = new Arguments();

	/**
	 * The Class NodeRelation.
	 */
	public static class NodeRelation implements VertexRelationship<AbstractGraphNodeAgent> {

		/** The action. */
		IStatement.WithArgs action;

		@Override
		public boolean related(final IScope scope, final AbstractGraphNodeAgent p1, final AbstractGraphNodeAgent p2) {
			args.put("other", GAML.getExpressionDescriptionFactory().createConstant(p2));
			final IExecutionResult result = scope.execute(getAction(p1), p1, args);
			return Cast.asBool(scope, result.getValue());
		}

		@Override
		public boolean equivalent(final IScope scope, final AbstractGraphNodeAgent p1,
				final AbstractGraphNodeAgent p2) {
			return p1 == p2;
		}

		/**
		 * Gets the action.
		 *
		 * @param a1
		 *            the a 1
		 * @return the action
		 */
		IStatement.WithArgs getAction(final AbstractGraphNodeAgent a1) {
			if (action == null) { action = a1.getAction(); }
			return action;
		}

	}

	/**
	 * Instantiates a new abstract graph node agent.
	 *
	 * @param s
	 *            the s
	 * @param index
	 *            the index
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public AbstractGraphNodeAgent(final IPopulation<? extends IAgent> s, final int index) throws GamaRuntimeException {
		super(s, index);
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	IStatement.WithArgs getAction() { return getSpecies().getAction("related_to"); }

	/**
	 * Related to.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@action (
			doc = @doc ("This operator should never be called"),
			name = "related_to",
			virtual = true,
			args = { @arg (
					doc = @doc ("The other agent"),
					name = "other",
					optional = false,
					type = IType.AGENT) })
	public Boolean relatedTo(final IScope scope) {
		DEBUG.LOG("Should never be called !");
		return false;
	}

	/**
	 * Gets the graph.
	 *
	 * @return the graph
	 */
	@SuppressWarnings ("rawtypes")
	@getter (IKeyword.MYGRAPH)
	public IGraphEventProvider getGraph() { return (IGraphEventProvider) getTopology().getPlaces(); }
}
