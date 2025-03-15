/*******************************************************************************************************
 *
 * GamaPopulation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.population;

import static com.google.common.collect.Iterators.transform;
import static gama.core.common.interfaces.IKeyword.EDGE_SPECIES;
import static gama.core.common.interfaces.IKeyword.EXPERIMENT;
import static gama.core.common.interfaces.IKeyword.LOCATION;
import static gama.core.common.interfaces.IKeyword.MIRRORS;
import static gama.core.common.interfaces.IKeyword.SHAPE;
import static gama.core.common.interfaces.IKeyword.TARGET;
import static gama.gaml.descriptions.VariableDescription.INIT_DEPENDENCIES_FACETS;
import static gama.gaml.descriptions.VariableDescription.UPDATE_DEPENDENCIES_FACETS;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.continuous.ContinuousTopology;
import gama.core.metamodel.topology.filter.IAgentFilter;
import gama.core.metamodel.topology.filter.In;
import gama.core.metamodel.topology.graph.GamaSpatialGraph;
import gama.core.metamodel.topology.graph.GraphTopology;
import gama.core.metamodel.topology.grid.GridPopulation;
import gama.core.runtime.FlowStatus;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.benchmark.StopWatch;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaList;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.graph.AbstractGraphNodeAgent;
import gama.dev.DEBUG;
import gama.gaml.compilation.IAgentConstructor;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.descriptions.VariableDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IExecutable;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.GamaTopologyType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.gaml.variables.IVariable;

/**
 * Written by drogoul Modified on 6 sept. 2010
 *
 * @todo Description
 *
 */
public class GamaPopulation<T extends IAgent> extends GamaList<T> implements IPopulation<T> {

	static {
		DEBUG.ON();
	}

	/**
	 * The agent hosting this population which is considered as the direct macro-agent.
	 */
	protected IMacroAgent host;

	/**
	 * The object describing how the agents of this population are spatially organized
	 */
	protected ITopology topology;

	/** The species. */
	protected final ISpecies species;

	/** The updatable vars. */
	protected final IVariable[] orderedVars, updatableVars;

	/** The current agent index. */
	protected int currentAgentIndex;

	/** The hash code. */
	private final int hashCode;

	/** The is step overriden. */
	private final boolean isInitOverriden, isStepOverriden;

	/** The mirror management. */
	private final MirrorPopulationManagement mirrorManagement;

	/**
	 * Listeners, created in a lazy way
	 */
	private final PopulationNotifier notifier = new PopulationNotifier();

	/** The ordered var names. */
	public final LinkedHashSet<String> orderedVarNames = new LinkedHashSet<>();

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
	 * Try add.
	 *
	 * @param graph
	 *            the graph
	 * @param v
	 *            the v
	 * @param existing
	 *            the existing
	 */
	private static void tryAdd(final DirectedAcyclicGraph<String, Object> graph, final String v,
			final String existing) {
		graph.addVertex(v);
		try {
			graph.addEdge(v, existing);
		} catch (IllegalArgumentException e) {
			// AD Revision in Aug 2021 for Issue #3068: edge is not added if it creates a cycle
		}
	}

	/**
	 * Order attributes.
	 *
	 * @param ecd
	 *            the ecd
	 * @param keep
	 *            the keep
	 * @param facetsToConsider
	 *            the facets to consider
	 * @return the i variable[]
	 */
	public static IVariable[] orderAttributes(final IPopulation pop, final TypeDescription ecd,
			final Predicate<VariableDescription> keep, final Set<String> facetsToConsider) {
		// AD Revised in Aug 2019 for Issue #2869: keep constraints between superspecies and subspecies
		// AD Revised in Aug 2021 for Issue #3068: do not introduce cycles (which might exist in update blocks) in order
		// to obtain a correct topological order
		final DirectedAcyclicGraph<String, Object> graph = new VariableOrderingGraph();

		// if (this instanceof SimulationPopulation) { DEBUG.OUT("Species to order: " +
		// DEBUG.TO_STRING(Iterables.toArray(Iterables.transform(subs, v->v.getName()), String.class))); }
		ecd.visitAllAttributes(d -> {
			VariableDescription var = (VariableDescription) d;
			if (keep.apply(var)) {
				String name = var.getName();
				graph.addVertex(name);
				for (final VariableDescription dep : var.getDependencies(facetsToConsider, false, true)) {
					if (keep.apply(dep)) { tryAdd(graph, dep.getName(), name); }
				}
				// Adding a constraint between the shape of the macrospecies and the populations of microspecies
				if (var.isSyntheticSpeciesContainer()) {
					// SpeciesDescription sd = var.getGamlType().getSpecies();
					// if (sd != null && graph.containsVertex(sd.getParentName())) {
					// tryAdd(graph, sd.getParentName(), name);
					// }
					tryAdd(graph, SHAPE, name);
				}
			}
			return true;
		});

		// AD Revised in Dec 2022 for Issue #3526: the order in which the sub-species is declared in the variables is
		// kept if possible, so that 'agents' returns the agents in the same order
		final List<VariableDescription> subs = new ArrayList<>();
		ecd.visitAllAttributes(d -> {
			VariableDescription var = (VariableDescription) d;
			if (var.isSyntheticSpeciesContainer()) { subs.add(var); }
			return true;
		});
		for (int i = 0; i < subs.size() - 1; i++) {
			VariableDescription vs = subs.get(i);
			if (keep.apply(vs)) {
				VariableDescription vt = subs.get(i + 1);
				if (keep.apply(vt)) {
					String source = vs.getName();
					String target = vt.getName();
					if (!graph.containsEdge(target, source)) { tryAdd(graph, source, target); }
				}
			}
		}
		// End revision
		// if (this instanceof SimulationPopulation) {
		// DEBUG.OUT("After ordering: " + DEBUG.TO_STRING(Iterators.toArray(
		// Iterators.filter(graph.iterator(),
		// s -> ((VariableDescription) getVar(s).getDescription()).isSyntheticSpeciesContainer()),
		// String.class)));
		// }
		return Iterators.toArray(transform(graph.iterator(), s -> pop.getVar(s)), IVariable.class);

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
		super(0, host == null ? Types.get(EXPERIMENT)
				: host.getModel().getDescription().getTypeNamed(species.getName()));
		this.host = host;
		this.species = species;
		final TypeDescription ecd = species.getDescription();
		orderedVars = orderAttributes(this, ecd, Predicates.alwaysTrue(), INIT_DEPENDENCIES_FACETS);
		for (IVariable v : orderedVars) { orderedVarNames.add(v.getName()); }
		updatableVars = orderAttributes(this, ecd, VariableDescription::isUpdatable, UPDATE_DEPENDENCIES_FACETS);
		if (species.isMirror() && host != null) {
			mirrorManagement = new MirrorPopulationManagement(species.getFacet(MIRRORS));
		} else {
			mirrorManagement = null;
		}

		/*
		 * PATRICK TAILLANDIER: the problem of having the host here is that depending on the simulation the hashcode
		 * will be different... and this hashcode is very important for the manipulation of GamaMap thus, having two
		 * different hashcodes depending on the simulation ensures the replication of simulation. So I remove the
		 * host for the moment.
		 */
		/*
		 * AD: Reverting this as different populations in different hosts should not have the same hash code ! See
		 * discussion in https://github.com/gama-platform/gama/issues/3339
		 */
		hashCode = Objects.hash(getSpecies(), getHost());
		final boolean[] result = { false, false };
		species.getDescription().visitChildren(d -> {
			if (d instanceof ActionDescription && !d.isBuiltIn()) {
				final String name = d.getName();
				if (ISpecies.initActionName.equals(name)) {
					result[0] = true;
				} else if (ISpecies.stepActionName.equals(name)) { result[1] = true; }
			}
			return true;
		});
		isInitOverriden = result[0];
		isStepOverriden = result[1];

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

	/**
	 * Step agents.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
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
	public void updateVariables(final IScope scope, final IAgent a) {
		for (final IVariable v : updatableVars) {
			try (StopWatch w = GAMA.benchmark(scope, v)) {
				scope.setCurrentSymbol(v);
				scope.setAgentVarValue(a, v.getName(), v.getUpdatedValue(scope));
			}
		}
		scope.setCurrentSymbol(null);
	}

	/**
	 * Inits the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @date 17 sept. 2023
	 */
	@Override
	public boolean init(final IScope scope) {
		// See #2933
		if (mirrorManagement != null) { mirrorManagement.executeOn(scope); }
		return true;
		// // Do whatever the population has to do at the first step ?
	}

	@Override
	public void createVariablesFor(final IScope scope, final T agent) throws GamaRuntimeException {
		for (final IVariable var : orderedVars) { var.initializeWith(scope, agent, null); }
	}

	@Override
	public T getAgent(final Integer index) {
		return Iterables.find(this, each -> each.getIndex() == index, null);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public T getOrCreateAgent(final IScope scope, final Integer index) {
		T agent = getAgent(index);
		return agent == null ? (T) createAgentAt(scope, index, Collections.EMPTY_MAP, false, true) : agent;
	}

	/**
	 * Compare to.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return the int
	 * @date 17 sept. 2023
	 */
	@Override
	public int compareTo(final IPopulation<T> o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public ITopology getTopology() { return topology; }

	@Override
	public String getName() { return species.getName(); }

	@Override
	public boolean isGrid() { return species.isGrid(); }

	@Override
	public ISpecies getSpecies() { return species; }

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
		killMembers();
		clear();
		final IScope scope = getHost() == null ? GAMA.getRuntimeScope() : getHost().getScope();
		firePopulationCleared(scope);
		if (topology != null) {
			topology.dispose();
			topology = null;
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	public T[] toArray() {
		return (T[]) super.toArray(new IAgent[0]);
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

	@Override
	public T createAgentAt(final IScope scope, final int index, final Map<String, Object> initialValues,
			final boolean isRestored, final boolean toBeScheduled) throws GamaRuntimeException {

		final List<Map<String, Object>> mapInitialValues = new ArrayList<>();
		mapInitialValues.add(initialValues);

		// TODO : think to another solution... it is ugly
		final int tempIndexAgt = currentAgentIndex;

		currentAgentIndex = index;
		final IList<T> listAgt = createAgents(scope, 1, mapInitialValues, isRestored, toBeScheduled, null);
		currentAgentIndex = tempIndexAgt;

		return listAgt.firstValue(scope);
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

	@Override
	public void initializeFor(final IScope scope) throws GamaRuntimeException {
		dispose();
		computeTopology(scope);
		if (topology != null) { topology.initialize(scope, this); }
	}

	@Override
	public boolean hasVar(final String n) {
		return species.getVar(n) != null;
	}

	@Override
	public boolean hasAspect(final String default1) {
		return species.hasAspect(default1);
	}

	@Override
	public IExecutable getAspect(final String default1) {
		return species.getAspect(default1);
	}

	@Override
	public Collection<String> getAspectNames() { return species.getAspectNames(); }

	@Override
	public IVariable getVar(final String s) {
		return species.getVar(s);
	}

	@Override
	public boolean hasUpdatableVariables() {
		return updatableVars.length > 0;
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
	protected void computeTopology(final IScope scope) throws GamaRuntimeException {
		final IExpression expr = species.getFacet(IKeyword.TOPOLOGY);
		final boolean fixed = species.isGraph() || species.isGrid();
		if (expr != null) {
			if (!fixed) {
				topology = GamaTopologyType.staticCast(scope, scope.evaluate(expr, host).getValue(), false);
				return;
			}
			throw GamaRuntimeException.warning(
					"Impossible to assign a topology to " + species.getName() + " as it already defines one.", scope);
		}
		if (species.isGrid()) {
			topology = GridPopulation.buildGridTopology(scope, species, getHost());
		} else if (species.isGraph()) {
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
	public IMacroAgent getHost() { return host; }

	@Override
	public void setHost(final IMacroAgent agt) { host = agt; }

	@Override
	public final boolean equals(final Object o) {
		return o == this;
	}

	@Override
	public final int hashCode() {
		return hashCode;
	}

	@Override
	public void killMembers() throws GamaRuntimeException {
		final T[] ag = toArray();
		for (final IAgent a : ag) { if (a != null) { a.dispose(); } }
		this.clear();
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
		super.addValueAtIndex(scope, index, value);
	}

	@Override
	public void addValues(final IScope scope, final IContainer values) {
		for (final T o : (java.lang.Iterable<T>) values.iterable(scope)) { addValue(scope, o); }
	}

	@Override
	public void removeValue(final IScope scope, final Object value) {
		if (value instanceof IAgent && super.remove(value)) {
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

	@Override
	public void addListener(final IPopulation.Listener listener) {
		notifier.addListener(listener);
	}

	@Override
	public void removeListener(final IPopulation.Listener listener) {
		notifier.removeListener(listener);
	}

	/**
	 * Fire agent added.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 */
	protected void fireAgentAdded(final IScope scope, final IAgent agent) {
		notifier.notifyAgentAdded(scope, this, agent);
	}

	/**
	 * Fire agents added.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 */
	@Override
	public <T extends IAgent> void fireAgentsAdded(final IScope scope, final IList<T> agents) {
		notifier.notifyAgentsAdded(scope, this, agents);
	}

	/**
	 * Fire agent removed.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 */
	protected void fireAgentRemoved(final IScope scope, final IAgent agent) {
		notifier.notifyAgentRemoved(scope, this, agent);
	}

	/**
	 * Fire population cleared.
	 *
	 * @param scope
	 *            the scope
	 */
	protected void firePopulationCleared(final IScope scope) {
		notifier.notifyPopulationCleared(scope, this);
	}

	// Filter methods

	/**
	 * Method getAgents()
	 *
	 * @see gama.core.metamodel.topology.filter.IAgentFilter#getAgents()
	 */
	@Override
	public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
		return GamaListFactory.create(scope, getGamlType().getContentType(), allLivingAgents(this));
	}

	/**
	 * Checks for agent list.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if successful
	 * @date 17 sept. 2023
	 */
	@Override
	public boolean hasAgentList() {
		return true;
	}

	/**
	 * Method accept()
	 *
	 * @see gama.core.metamodel.topology.filter.IAgentFilter#accept(gama.core.runtime.IScope,
	 *      gama.core.metamodel.shape.IShape, gama.core.metamodel.shape.IShape)
	 */
	@Override
	public boolean accept(final IScope scope, final IShape source, final IShape a) {
		final IAgent agent = a.getAgent();
		if (agent == null || agent.getPopulation() != this || agent.dead()) return false;
		final IAgent as = source.getAgent();
		if (agent == as) return false;
		// }
		return true;
	}

	/**
	 * Method filter()
	 *
	 * @see gama.core.metamodel.topology.filter.IAgentFilter#filter(gama.core.runtime.IScope,
	 *      gama.core.metamodel.shape.IShape, java.util.Collection)
	 */
	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
		final IAgent sourceAgent = source == null ? null : source.getAgent();
		results.remove(sourceAgent);
		final Predicate<IShape> toRemove = each -> {
			final IAgent a = each.getAgent();
			return a == null || a.dead()
					|| a.getPopulation() != this
							&& (a.getPopulation().getGamlType().getContentType() != this.getGamlType().getContentType()
									|| !this.contains(a));
		};
		results.removeIf(toRemove);
	}

	/**
	 * Gets the populations.
	 *
	 * @param scope
	 *            the scope
	 * @return the populations
	 */
	@Override
	public Collection<? extends IPopulation<? extends IAgent>> getPopulations(final IScope scope) {
		return Collections.singleton(this);
	}

	@Override
	public T getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null) return null;
		final int size = indices.size();
		return switch (size) {
			case 0 -> null;
			case 1 -> super.getFromIndicesList(scope, indices);
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

	/**
	 * Method isInitOverriden()
	 *
	 * @see gama.gaml.species.ISpecies#isInitOverriden()
	 */
	@Override
	public boolean isInitOverriden() { return isInitOverriden; }

	/**
	 * Method isStepOverriden()
	 *
	 * @see gama.gaml.species.ISpecies#isStepOverriden()
	 */
	@Override
	public boolean isStepOverriden() { return isStepOverriden; }

}