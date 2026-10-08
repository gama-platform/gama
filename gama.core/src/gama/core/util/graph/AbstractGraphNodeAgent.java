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
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
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
 * A base agent class for nodes in a GAMA graph. Agents inheriting from this species are treated as graph nodes.
 *
 * <p>
 * Subclasses should override the {@code related_to} action to define the relationship predicate used when building the
 * graph from agent containers. This class is thread-safe for concurrent and parallel simulation setups: no shared
 * mutable state is used during the evaluation of the {@code related_to} predicate.
 * </p>
 *
 * <h3>Variables</h3>
 * <ul>
 * <li>{@code my_graph} ({@code graph}) – a reference to the graph that contains this agent.</li>
 * </ul>
 *
 * <h3>Actions</h3>
 * <ul>
 * <li>{@code related_to(other: agent): bool} – virtual action that subclasses must override to declare whether this
 * node is related to another agent. The default implementation always returns {@code false}.</li>
 * </ul>
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

	/**
	 * A {@link VertexRelationship} implementation that delegates to the {@code related_to} action defined on
	 * {@link AbstractGraphNodeAgent} instances.
	 *
	 * <p>
	 * Thread safety: each call to {@link #related} creates its own {@link Arguments} instance so that concurrent
	 * invocations from parallel simulation threads do not share mutable argument state. The cached {@link #action}
	 * reference is declared {@code volatile} to guarantee safe publication across threads.
	 * </p>
	 */
	public static class NodeRelation implements VertexRelationship<AbstractGraphNodeAgent> {

		/**
		 * Cached reference to the {@code related_to} action statement. Declared {@code volatile} so that the
		 * first-write by one thread is immediately visible to all other threads without requiring synchronization on
		 * the hot path.
		 */
		volatile IStatement.WithArgs action;

		/**
		 * Returns {@code true} when {@code p1} is related to {@code p2} according to the {@code related_to} action
		 * defined on {@code p1}'s species.
		 *
		 * <p>
		 * A fresh {@link Arguments} object is created on every call so that concurrent invocations from different
		 * threads cannot interfere with each other's argument state.
		 * </p>
		 *
		 * @param scope
		 *            the current execution scope
		 * @param p1
		 *            the source node agent
		 * @param p2
		 *            the candidate neighbour node agent
		 * @return {@code true} if {@code p1} declares itself related to {@code p2}
		 */
		@Override
		public boolean related(final IScope scope, final AbstractGraphNodeAgent p1, final AbstractGraphNodeAgent p2) {
			// A new Arguments instance is created per invocation to avoid shared mutable state
			// between concurrent threads in parallel simulation setups.
			final Arguments localArgs = new Arguments();
			localArgs.put("other", GAML.getExpressionDescriptionFactory().createConstant(p2));
			final IExecutionResult result = scope.execute(getAction(p1), p1, localArgs);
			return Cast.asBool(scope, result.getValue());
		}

		/**
		 * Returns {@code true} when {@code p1} and {@code p2} are the same object reference, i.e. the same agent.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param p1
		 *            the first node agent
		 * @param p2
		 *            the second node agent
		 * @return {@code true} if {@code p1 == p2}
		 */
		@Override
		public boolean equivalent(final IScope scope, final AbstractGraphNodeAgent p1,
				final AbstractGraphNodeAgent p2) {
			return p1 == p2;
		}

		/**
		 * Lazily resolves and caches the {@code related_to} action from the given node agent's species. The field is
		 * {@code volatile} so that the cached value is safely visible to all threads after the first write.
		 *
		 * @param a1
		 *            the node agent whose species defines the {@code related_to} action
		 * @return the resolved {@link IStatement.WithArgs} for the {@code related_to} action
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
