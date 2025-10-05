/*******************************************************************************************************
 *
 * GamaPopulation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.population;

import static gama.core.common.interfaces.IKeyword.EDGE_SPECIES;
import static gama.core.common.interfaces.IKeyword.LOCATION;
import static gama.core.common.interfaces.IKeyword.MIRRORS;
import static gama.core.common.interfaces.IKeyword.SHAPE;
import static gama.core.common.interfaces.IKeyword.TARGET;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.continuous.ContinuousTopology;
import gama.core.metamodel.topology.filter.IAgentFilter;
import gama.core.metamodel.topology.filter.In;
import gama.core.metamodel.topology.graph.GamaSpatialGraph;
import gama.core.metamodel.topology.graph.GraphTopology;
import gama.core.runtime.FlowStatus;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.graph.AbstractGraphNodeAgent;
import gama.dev.DEBUG;
import gama.gaml.compilation.IAgentConstructor;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IExecutable;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.GamaTopologyType;
import gama.gaml.types.IType;
import gama.gaml.variables.IVariable;

/**
 * Written by drogoul Modified on 6 sept. 2010
 *
 * @todo Description
 *
 */
public class GamaPopulation<T extends IAgent> extends AbstractPopulation<T> implements IPopulation<T> {

	static {
		DEBUG.OFF();
	}

	/** The agents container. */
	private final IList<T> agentsContainer;

	/** The mirror management. */
	private final MirrorPopulationManagement mirrorManagement;

	/** The Constant isLiving. */
	public final static IPopulation.IsLiving isLiving = new IPopulation.IsLiving();

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
			final Set<IAgent> targets = new HashSet<IAgent>(Cast.asList(scope, listOfTargetAgents.value(scope)));
			final List<IAgent> toKill = new ArrayList<>();
			for (final IAgent agent : pop.iterable(scope)) {
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

	/**
	 * Step.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 17 sept. 2023
	 */
	@Override
	public boolean step(final IScope scope) throws GamaRuntimeException {
		final IExpression frequencyExp = species.getFrequency();
		if (frequencyExp != null) {
			final int frequency = Cast.asInt(scope, frequencyExp.value(scope));
			final int step = scope.getClock().getCycle();
			if (frequency == 0 || step % frequency != 0) return true;
		}
		if (mirrorManagement != null) { mirrorManagement.executeOn(scope); }
		getSpecies().getArchitecture().preStep(scope, this);
		return stepAgents(scope);

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

	/**
	 * Take copy into account and always creates a list (necessary for #2254)
	 */
	@Override
	public IList<T> listValue(final IScope scope, final IType contentsType, final boolean copy) {
		if (copy) return GamaListFactory.create(scope, contentsType, this);
		return this;
	}

	/**
	 * Explicity copy (necessary for #2254)
	 */
	@Override
	public IList<T> copy(final IScope scope) {
		return listValue(scope, getGamlType().getContentType(), true);
	}

	@Override
	public T getAgent(final Integer index) {
		return Iterables.find(this, each -> each.getIndex() == index, null);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public Iterable<T> iterable(final IScope scope) {
		return (Iterable<T>) getAgents(scope);
	}

	/**
	 * Dispose.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 17 sept. 2023
	 */
	@Override
	public void dispose() {
		isDisposing = true;
		killMembers();
		final IScope scope = getHost() == null ? GAMA.getRuntimeScope() : getHost().getScope();
		firePopulationCleared(scope);
		if (topology != null) {
			topology.dispose();
			topology = null;
		}
		clear();
	}

	@SuppressWarnings ("unchecked")
	@Override
	public T[] toArray() {
		return (T[]) agentsContainer.toArray(new IAgent[0]);
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
	@Override
	public IList<T> createAgents(final IScope scope, final IContainer<?, ? extends IShape> geometries) {
		final int number = geometries.length(scope);
		if (number == 0) return GamaListFactory.EMPTY_LIST;
		final IList<T> list = GamaListFactory.create(getGamlType().getContentType(), number);
		final IAgentConstructor<T> constr = species.getDescription().getAgentConstructor();
		for (final IShape geom : geometries.iterable(scope)) {
			// WARNING Should be redefined somehow
			final T a = constr.createOneAgent(this, currentAgentIndex++);
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

	@SuppressWarnings ("unchecked")
	@Override
	public IList<T> createAgents(final IScope scope, final int number,
			final List<? extends Map<String, Object>> initialValues, final boolean isRestored,
			final boolean toBeScheduled, final RemoteSequence sequence) throws GamaRuntimeException {
		if (number == 0) return GamaListFactory.EMPTY_LIST;
		final IList<T> list = GamaListFactory.create(getGamlType().getContentType(), number);
		final IAgentConstructor<T> constr = species.getDescription().getAgentConstructor();
		for (int i = 0; i < number; i++) {
			final T a = constr.createOneAgent(this, currentAgentIndex++);
			// Try to grab the location earlier
			if (initialValues != null && !initialValues.isEmpty()) {
				final Map<String, Object> init = initialValues.get(i);
				final Object val = init.get(SHAPE);
				final Object loc = init.get(LOCATION);
				if (val != null) {
					if (val instanceof GamaPoint p) {
						a.setGeometry(GamaShapeFactory.createFrom(p));
					} else {
						a.setGeometry((IShape) val);
					}
					init.remove(SHAPE);
				} else if (loc != null) {
					a.setLocation(scope, (GamaPoint) loc);
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
	public T getAgent(final IScope scope, final GamaPoint coord) {
		final IAgentFilter filter = In.list(scope, this);
		if (filter == null) return null;
		return topology == null ? null : (T) topology.getAgentClosestTo(scope, coord, filter);
	}

	/**
	 * Initializes the appropriate topology.
	 *
	 * @param scope
	 * @return
	 * @throws GamaRuntimeException
	 */
	@Override
	protected void computeTopology(final IScope scope) throws GamaRuntimeException {
		final IExpression expr = species.getFacet(IKeyword.TOPOLOGY);
		final boolean isGraph = species.isGraph();
		if (expr != null) {
			if (!isGraph) {
				topology = GamaTopologyType.staticCast(scope, scope.evaluate(expr, host).getValue(), false);
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

	@Override
	public String toString() {
		return "Population of " + species.getName();
	}

	@Override
	public void addValue(final IScope scope, final T value) {
		fireAgentAdded(scope, value);
		add(value);
	}

	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final T value) {
		fireAgentAdded(scope, value);
		agentsContainer.addValueAtIndex(scope, index, value);
	}

	@Override
	public void addValues(final IScope scope, final IContainer values) {
		for (final T o : (java.lang.Iterable<T>) values.iterable(scope)) { addValue(scope, o); }
	}

	@Override
	public void removeValue(final IScope scope, final Object value) {
		if (value instanceof IAgent && agentsContainer.remove(value)) {
			if (topology != null) { topology.removeAgent((IAgent) value); }
			fireAgentRemoved(scope, (IAgent) value);
		}
	}

	@Override
	public void removeValues(final IScope scope, final IContainer values) {
		for (final Object o : values.iterable(scope)) { removeValue(scope, o); }
	}

	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		removeValue(scope, value);
	}

	@Override
	public boolean remove(final Object a) {
		removeValue(null, a);
		return true;
	}

	@Override
	public boolean contains(final IScope scope, final Object o) {
		if (!(o instanceof IAgent)) return false;
		return ((IAgent) o).getPopulation() == this;
	}

	// Filter methods

	@Override
	public T getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null) return null;
		final int size = indices.size();
		return switch (size) {
			case 0 -> null;
			case 1 -> (T) agentsContainer.getFromIndicesList(scope, indices);
			case 2 -> this.getAgent(scope,
					new GamaPoint(Cast.asFloat(scope, indices.get(0)), Cast.asFloat(scope, indices.get(1))));
			default -> throw GamaRuntimeException.error("Populations cannot be accessed with 3 or more indexes", scope);
		};

	}

	/**
	 * @param actionScope
	 * @param iterable
	 * @return
	 */
	public static <T extends IAgent> Iterable<T> allLivingAgents(final Iterable<T> iterable) {
		return Iterables.filter(iterable, isLiving);
	}

	@Override
	public boolean isEmpty() { return agentsContainer.isEmpty(); }

	@Override
	public boolean contains(final Object o) {
		return agentsContainer.contains(o);
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return agentsContainer.toArray(a);
	}

	@Override
	public void clear() {
		agentsContainer.clear();
	}

	@Override
	public List<T> internalListOfAgents(final boolean makeCopy, final boolean onlyLiving) {
		if (onlyLiving) return Lists.newArrayList(allLivingAgents(agentsContainer));
		if (!makeCopy) return agentsContainer;
		return new ArrayList<>(agentsContainer);
	}

}
