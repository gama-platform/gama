/*******************************************************************************************************
 *
 * AbstractGraphEdgeAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.graph;

import gama.annotations.doc;
import gama.annotations.species;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IGraphAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.core.agent.GamlAgent;

// FIXME: Add all the necessary variables and actions ?
/**
 * A base agent class for edges in a GAMA graph. Each edge agent maintains a reference to its source and target node
 * agents, and updates its geometry during each simulation step to reflect the current positions of those nodes.
 *
 * <p>
 * This class is safe for use in concurrent and parallel simulation setups: the {@link #_step_} method guards against
 * interrupted scopes, dead edge agents, and concurrently-disposed source or target agents before attempting any
 * geometry update.
 * </p>
 *
 * <h3>Variables</h3>
 * <ul>
 * <li>{@code source} ({@code agent}) – the source node agent of this edge.</li>
 * <li>{@code target} ({@code agent}) – the target node agent of this edge.</li>
 * </ul>
 */
@species (
		name = "graph_edge",
		doc = @doc ("A species that represents an edge of a graph made of agents. The source and the target of the edge should be agents"))
@vars ({ @variable (
		name = IKeyword.SOURCE,
		type = IType.AGENT,
		doc = @doc ("The source agent of this edge")),
		@variable (
				name = IKeyword.TARGET,
				type = IType.AGENT,
				doc = @doc ("The target agent of this edge")) })
@doc ("A species that represents an edge of a graph made of agents. The source and the target of the edge should be agents")
public class AbstractGraphEdgeAgent extends GamlAgent implements IGraphAgent {

	/**
	 * Instantiates a new abstract graph edge agent.
	 *
	 * @param s
	 *            the population this edge agent belongs to
	 * @param index
	 *            the index of this agent within its population
	 * @throws GamaRuntimeException
	 *             if an error occurs during agent instantiation
	 */
	public AbstractGraphEdgeAgent(final IPopulation<? extends IAgent> s, final int index) throws GamaRuntimeException {
		super(s, index);
	}

	/**
	 * Performs one simulation step for this edge agent. The geometry of the edge is updated to a line connecting the
	 * current locations of the source and target node agents.
	 *
	 * <p>
	 * The step is skipped entirely when any of the following conditions hold, preventing errors in concurrent or
	 * parallel simulation setups:
	 * </p>
	 * <ul>
	 * <li>The execution scope has been interrupted.</li>
	 * <li>This edge agent is dead (already disposed).</li>
	 * <li>The source or target attribute is {@code null}.</li>
	 * <li>The source or target agent has been concurrently disposed ({@code dead()}).</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the result of the parent {@code _step_} call, or {@code null} if the step was skipped
	 */
	@Override
	public Object _step_(final IScope scope) {
		if (scope.interrupted() || dead()) return null;
		final IAgent s = (IAgent) getAttribute(IKeyword.SOURCE);
		final IAgent t = (IAgent) getAttribute(IKeyword.TARGET);
		if (s == null || s.dead() || t == null || t.dead()) return null;
		setGeometry(GamaShapeFactory.buildLine(s.getLocation(scope), t.getLocation(scope)));
		return super._step_(scope);
	}

}