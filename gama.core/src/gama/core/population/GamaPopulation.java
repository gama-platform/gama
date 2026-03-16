/*******************************************************************************************************
 *
 * GamaPopulation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.population;

import static gama.annotations.constants.IKeyword.EDGE_SPECIES;
import static gama.annotations.constants.IKeyword.LOCATION;
import static gama.annotations.constants.IKeyword.MIRRORS;
import static gama.annotations.constants.IKeyword.SHAPE;
import static gama.annotations.constants.IKeyword.TARGET;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.FlowStatus;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.misc.IContainer;
import gama.api.types.topology.GamaTopologyFactory;
import gama.api.utils.interfaces.IAgentFilter;
import gama.core.topology.continuous.ContinuousTopology;
import gama.core.topology.filter.In;
import gama.core.topology.graph.GamaSpatialGraph;
import gama.core.topology.graph.GraphTopology;
import gama.core.util.graph.AbstractGraphNodeAgent;
import gama.dev.DEBUG;

/**
 * Written by drogoul Modified on 6 sept. 2010
 *
 * @todo Description
 *
 */
public class GamaPopulation<T extends IAgent> extends AbstractPopulation<T> {

	static {
		DEBUG.OFF();
	}

	/** The agents container. */
	private final IList<T> agentsContainer;

	/** The mirror management. */
	private final MirrorPopulationManagement mirrorManagement;

	/**
	 * The Class MirrorPopulationManagement.
	 */
	class MirrorPopulationManagement implements IExecutable {

		/** The list of target agents. */
		final IExpression listOfTargetAgents;

		/**
		 * Instantiates a new mirror population management.
		 *
		 * @param exp
		 *            the exp
		 */
		MirrorPopulationManagement(final IExpression exp) {
			listOfTargetAgents = exp;
		}

		@Override
		public Object executeOn(final IScope scope) throws GamaRuntimeException {
			final IPopulation<T> pop = GamaPopulation.this;
			final Set<IAgent> targets =
					new HashSet<IAgent>(GamaListFactory.castToList(scope, listOfTargetAgents.value(scope)));
			final List<IAgent> toKill = new ArrayList<>();
			for (final IAgent agent : pop) {
				final IAgent target = Cast.asAgent(scope, agent.getAttribute(TARGET));
				if (targets.contains(target)) {
					targets.remove(target);
				} else {
					toKill.add(agent);
				}
			}
			for (final IAgent agent : toKill) { agent.dispose(); }
			final List<Map<String, Object>> attributes = new ArrayList<>();
			for (final IAgent target : targets) {
				final Map<String, Object> att = GamaMapFactory.createUnordered();
				att.put(TARGET, target);
				attributes.add(att);
			}
			return pop.createAgents(scope, targets.size(), attributes, false, true, null);
		}

	}

	/**
	 * Instantiates a new gama population.
	 *
	 * @param host
	 *            the agent hosting this population which is considered as the direct macro-agent.
	 * @param species
	 *            the species
	 */
	public GamaPopulation(final IMacroAgent host, final ISpecies species) {
		super(host, species);
		agentsContainer = GamaListFactory.create(type.getContentType(), 10);
		mirrorManagement =
				species.isMirror() && host != null ? new MirrorPopulationManagement(species.getFacet(MIRRORS)) : null;
	}

	@Override
	protected void manageMirror(final IScope scope) {
		if (mirrorManagement != null) { mirrorManagement.executeOn(scope); }
	}

	/**
	 * Step agents.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	@Override
	protected boolean stepAgents(final IScope scope) {
		return GamaExecutorService.step(scope, this, getSpecies());
	}

	@SuppressWarnings ("unchecked")
	@Override
	public Iterable<T> iterable(final IScope scope) {
		return (Iterable<T>) getAgents(scope);
	}

	/**
	 * Special case for creating agents directly from geometries
	 *
	 * @param scope
	 * @param number
	 * @param initialValues
	 * @param geometries
	 * @return
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public IList<T> createAgents(final IScope scope, final IContainer<?, ? extends IShape> geometries) {
		final int number = geometries.length(scope);
		if (number == 0) return GamaListFactory.getEmptyList();
		final IList<T> list = GamaListFactory.create(getGamlType().getContentType(), number);
		final IAgentConstructor constr = species.getDescription().getAgentConstructor();
		for (final IShape geom : geometries.iterable(scope)) {
			// WARNING Should be redefined somehow
			final T a = (T) constr.createOneAgent(this, currentAgentIndex++);
			// final int ind = currentAgentIndex++;
			// a.setIndex(ind);
			a.setGeometry(geom);
			list.add(a);
		}
		/* agents. */addAll(list);

		for (final IAgent a : list) {
			a.schedule(scope);
			// a.scheduleAndExecute(null);
		}
		// AD May 2021: adds the execution of the sequence *before* firing listeners (otherwise, improperly initialized
		// agents were "notified"
		createVariablesFor(scope, list, EMPTY_LIST);
		// if (sequence != null && !sequence.isEmpty()) {
		// for (final IAgent a : list) { if (!scope.execute(sequence, a, null).passed()) { break; } }
		// }
		fireAgentsAdded(scope, list);
		return list;

	}

	/**
	 * Creates the agents.
	 *
	 * @param scope
	 *            the scope
	 * @param number
	 *            the number
	 * @param initialValues
	 *            the initial values
	 * @param isRestored
	 *            the is restored
	 * @param toBeScheduled
	 *            the to be scheduled
	 * @param sequence
	 *            the sequence
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public IList<T> createAgents(final IScope scope, final int number,
			final List<? extends Map<String, Object>> initialValues, final boolean isRestored,
			final boolean toBeScheduled, final IStatement sequence) throws GamaRuntimeException {
		if (number == 0) return GamaListFactory.getEmptyList();
		final IList<T> list = GamaListFactory.create(getGamlType().getContentType(), number);
		final IAgentConstructor constr = species.getDescription().getAgentConstructor();
		for (int i = 0; i < number; i++) {
			final T a = (T) constr.createOneAgent(this, currentAgentIndex++);
			// Try to grab the location earlier
			if (initialValues != null && !initialValues.isEmpty()) {
				final Map<String, Object> init = initialValues.get(i);
				final Object val = init.get(SHAPE);
				final Object loc = init.get(LOCATION);
				if (val != null) {
					if (val instanceof IPoint p) {
						a.setGeometry(GamaShapeFactory.createFrom(p));
					} else {
						a.setGeometry((IShape) val);
					}
					init.remove(SHAPE);
				} else if (loc != null) {
					a.setLocation(scope, (IPoint) loc);
					init.remove(LOCATION);
				}
			}
			list.add(a);
		}

		createVariablesFor(scope, list, initialValues);
		// #3626 ??
		addAll(list);
		if (!isRestored) {
			for (final IAgent a : list) {
				// if agent is restored (on the capture or release); then don't
				// need to run the "init" reflex
				a.schedule(scope);
				// a.scheduleAndExecute(sequence);
			}
			// AD May 2021: adds the execution of the sequence *before* firing listeners (otherwise, improperly
			// initialized
			// agents were "notified"
			if (sequence != null && !sequence.isEmpty()) {
				for (final IAgent a : list) {

					if (!scope.execute(sequence, a, null).passed()
							|| scope.getAndClearBreakStatus() == FlowStatus.BREAK) {
						break;
					}
				}
			}

		}
		fireAgentsAdded(scope, list);
		return list;
	}

	/**
	 * Creates the variables for.
	 *
	 * @param scope
	 *            the scope
	 * @param agents
	 *            the agents
	 * @param initialValues
	 *            the initial values
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("null")
	public void createVariablesFor(final IScope scope, final List<T> agents,
			final List<? extends Map<String, Object>> initialValues) throws GamaRuntimeException {
		if (agents == null || agents.isEmpty()) return;
		final boolean empty = initialValues == null || initialValues.isEmpty();
		Map<String, Object> inits;
		for (int i = 0, n = agents.size(); i < n; i++) {
			final IAgent a = agents.get(i);
			inits = empty ? EMPTY_MAP : initialValues.get(i);
			for (final IVariable var : orderedVars) {
				final Object initGet =
						empty || !allowVarInitToBeOverridenByExternalInit(var) ? null : inits.get(var.getName());
				var.initializeWith(scope, a, initGet);
			}
			// Added to fix #3266 -- saves the values of the "extra" attributes found in the files
			if (!empty) {
				inits.forEach((name, v) -> { if (!orderedVarNames.contains(name)) { a.setAttribute(name, v); } });
			}
		}
	}

	/**
	 * Allow var init to be overriden by external init.
	 *
	 * @param var
	 *            the var
	 * @return true, if successful
	 *
	 */
	protected boolean allowVarInitToBeOverridenByExternalInit(final IVariable var) {
		return true;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public T getAgent(final IScope scope, final IPoint coord) {
		final IAgentFilter filter = In.list(scope, this);
		if (filter == null) return null;
		return topology == null ? null : (T) topology.getAgentClosestTo(scope, coord, filter);
	}

	@Override
	protected void computeTopology(final IScope scope) throws GamaRuntimeException {
		final IExpression expr = species.getFacet(IKeyword.TOPOLOGY);
		final boolean isGraph = species.isGraph();
		if (expr != null) {
			if (!isGraph) {
				topology = GamaTopologyFactory.castToTopology(scope, scope.evaluate(expr, host).getValue(), false);
				return;
			}
			throw GamaRuntimeException.warning(
					"Impossible to assign a topology to " + species.getName() + " as it already defines one.", scope);
		}
		if (isGraph) {
			final IExpression spec = species.getFacet(EDGE_SPECIES);
			final String edgeName = spec == null ? "base_edge" : spec.literalValue();
			final ISpecies edgeSpecies = scope.getModel().getSpecies(edgeName);
			final IType<?> edgeType = scope.getType(edgeName);
			final IType<?> nodeType = getGamlType().getContentType();
			// TODO Specifier directed quelque part dans l'espece
			final GamaSpatialGraph g = new GamaSpatialGraph(GamaListFactory.EMPTY_LIST, false, false, false,
					new AbstractGraphNodeAgent.NodeRelation(), edgeSpecies, scope, nodeType, edgeType);
			this.addListener(g);
			g.postRefreshManagementAction(scope);
			topology = new GraphTopology(scope, this.getHost(), g);
		} else {
			topology = new ContinuousTopology(scope, this.getHost());
		}

	}

	@SuppressWarnings ("unchecked")
	@Override
	public T getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null) return null;
		final int size = indices.size();
		return switch (size) {
			case 0 -> null;
			case 1 -> (T) agentsContainer.getFromIndicesList(scope, indices);
			case 2 -> this.getAgent(scope,
					GamaPointFactory.create(Cast.asFloat(scope, indices.get(0)), Cast.asFloat(scope, indices.get(1))));
			default -> throw GamaRuntimeException.error("Populations cannot be accessed with 3 or more indexes", scope);
		};

	}

	@Override
	public List<T> internalListOfAgents(final boolean makeCopy, final boolean onlyLiving) {
		if (onlyLiving) return Lists.newArrayList(Iterables.filter(agentsContainer, a -> a != null && !a.dead()));
		if (!makeCopy) return agentsContainer;
		return new ArrayList<>(agentsContainer);
	}

}