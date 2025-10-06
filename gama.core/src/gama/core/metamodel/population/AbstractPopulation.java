/*******************************************************************************************************
 *
 * AbstractPopulation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.population;

import static com.google.common.collect.Iterators.transform;
import static gama.core.common.interfaces.IKeyword.SHAPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.benchmark.StopWatch; // restored
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.descriptions.VariableDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IExecutable;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.IContainerType;
import gama.gaml.variables.IVariable;
import one.util.streamex.StreamEx;

/**
 * AbstractPopulation is the base class for all population types in GAMA.
 *
 * Interface hierarchy (direct + up to 2 levels up):
 *
 * <pre>
 * AbstractPopulation
 *  |-- IPopulation<T>
 *        |-- Comparable<IPopulation<T>>
 *        |-- IList<T>
 *        |     |-- List<T>
 *        |           |-- Collection<T>
 *        |                 |-- Iterable<T>
 *        |     |-- IModifiableContainer
 *        |     |-- IAddressableContainer
 *        |-- IStepable (init, step)
 *        |-- IDisposable (dispose)
 *        |-- IPopulationSet<T>
 *              |-- IContainer<Integer,T>
 *                    |-- IValue
 *              |-- IAgentFilter (accept, filter)
 * </pre>
 *
 * Method groups below follow this hierarchy: 1. IPopulation (direct population-specific API) 2. IStepable 3.
 * IDisposable 4. IPopulationSet ( + IAgentFilter ) 5. IList / List / Collection / Iterable 6. Comparable 7. Object
 * overrides 8. Internal & protected helpers
 */
public abstract class AbstractPopulation<T extends IAgent> implements IPopulation<T> {

	/** The host. */
	protected IMacroAgent host;

	/** The species. */
	protected final ISpecies species;

	/** The topology. */
	protected ITopology topology;

	/** The type. */
	protected final IContainerType<?> type;

	/** The updatable vars. */
	protected final IVariable[] orderedVars, updatableVars;

	/** The ordered var names. */
	protected final LinkedHashSet<String> orderedVarNames = new LinkedHashSet<>();

	/** The current agent index. */
	protected int currentAgentIndex;

	/** The hash code. */
	protected final int hashCode;

	/** The is step overriden. */
	protected final boolean isInitOverriden, isStepOverriden;

	/** The is disposing. */
	protected boolean isDisposing = false;
	/** The notifier. */
	protected final PopulationNotifier notifier = new PopulationNotifier();

	/**
	 * Instantiates a new abstract population.
	 *
	 * @param host
	 *            the host
	 * @param species
	 *            the species
	 */
	protected AbstractPopulation(final IMacroAgent host, final ISpecies species) {
		this.host = host;
		this.species = species;
		final TypeDescription ecd = species.getDescription();
		this.type = gama.gaml.types.Types.LIST.of(ecd.getModelDescription().getTypeNamed(species.getName()));
		orderedVars = orderAttributes(ecd, v -> true, VariableDescription.INIT_DEPENDENCIES_FACETS);
		for (IVariable v : orderedVars) { orderedVarNames.add(v.getName()); }
		updatableVars =
				orderAttributes(ecd, VariableDescription::isUpdatable, VariableDescription.UPDATE_DEPENDENCIES_FACETS);
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

	// ----------------------------------------------------------------------------------
	// 1. IPopulation (direct API)
	// ----------------------------------------------------------------------------------

	@Override
	public ISpecies getSpecies() { return species; }

	@Override
	public IMacroAgent getHost() { return host; }

	@Override
	public void setHost(final IMacroAgent agt) { host = agt; }

	@Override
	public ITopology getTopology() { return topology; }

	@Override
	public String getName() { return species.getName(); }

	@Override
	public boolean isGrid() { return false; }

	@Override
	public boolean hasVar(final String n) {
		return species.getVar(n) != null;
	}

	@Override
	public boolean hasAspect(final String aspect) {
		return species.hasAspect(aspect);
	}

	@Override
	public IExecutable getAspect(final String aspect) {
		return species.getAspect(aspect);
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

	@Override
	public boolean isInitOverriden() { return isInitOverriden; }

	@Override
	public boolean isStepOverriden() { return isStepOverriden; }

	@Override
	public boolean isDisposing() { return isDisposing; }

	@Override
	public void createVariablesFor(final IScope scope, final T agent) throws GamaRuntimeException {
		for (final IVariable var : orderedVars) { var.initializeWith(scope, agent, null); }
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

	@Override
	public void initializeFor(final IScope scope) throws GamaRuntimeException {
		computeTopology(scope);
		if (topology != null) { topology.initialize(scope, this); }
	}

	@Override
	public void killMembers() throws GamaRuntimeException {
		final T[] ag = toArray();
		for (final IAgent a : ag)
			if (a != null) { a.dispose(); }
	}

	@Override
	public T getAgent(final Integer index) {
		return internalListOfAgents(false, true).get(index);
	}

	@Override
	public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
		return GamaListFactory.create(scope, getGamlType().getContentType(), internalListOfAgents(false, true));
	}

	@Override
	public T getAgent(final IScope scope, final GamaPoint coord) {
		return null;
		/* Optional: override in spatial populations */ }

	@Override
	public void addListener(final Listener listener) {
		notifier.addListener(listener);
	}

	@Override
	public void removeListener(final Listener listener) {
		notifier.removeListener(listener);
	}

	@Override
	public void removeValue(final IScope scope, final Object value) {
		if (value instanceof IAgent && remove(value) && topology != null) { topology.removeAgent((IAgent) value); }
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
	public void addValue(final IScope scope, final T value) {
		add(value);
	}

	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final T value) {
		add(value);
	}

	@Override
	public void addValues(final IScope scope, final IContainer values) {
		for (final T o : (java.lang.Iterable<T>) values.iterable(scope)) { addValue(scope, o); }
	}

	@Override
	public boolean contains(final IScope scope, final Object o) {
		return o instanceof IAgent ia && ia.getPopulation() == this;
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	@Override
	public IContainerType<?> getGamlType() { return type; }

	@Override
	@SuppressWarnings ("unchecked")
	public T getOrCreateAgent(final IScope scope, final Integer index) {
		T agent = getAgent(index);
		return agent == null ? (T) createAgentAtIndex(scope, index, Collections.EMPTY_MAP, false, true) : agent;
	}

	@Override
	public <X extends IAgent> void fireAgentsAdded(final IScope scope, final IList<X> agents) {
		notifier.notifyAgentsAdded(scope, this, agents);
	}

	@Override
	public Collection<? extends IPopulation<? extends IAgent>> getPopulations(final IScope scope) {
		return Collections.singleton(this);
	}

	@Override
	public boolean hasAgentList() {
		return true;
	}

	@Override
	public T createAgentAtIndex(final IScope scope, final int index, final Map<String, Object> initialValues,
			final boolean isRestored, final boolean toBeScheduled) throws GamaRuntimeException {
		final List<Map<String, Object>> mapInitialValues = new ArrayList<>();
		mapInitialValues.add(initialValues);
		final int tempIndexAgt = currentAgentIndex;
		currentAgentIndex = index;
		final IList<T> listAgt = createAgents(scope, 1, mapInitialValues, isRestored, toBeScheduled, null);
		currentAgentIndex = tempIndexAgt;
		return listAgt.firstValue(scope);
	}

	@Override
	public abstract IList<T> createAgents(IScope scope, int number, List<? extends Map<String, Object>> initialValues,
			boolean isRestored, boolean toBeScheduled, RemoteSequence sequence) throws GamaRuntimeException;

	@Override
	public abstract IList<T> createAgents(final IScope scope, final IContainer<?, ? extends IShape> geometries)
			throws GamaRuntimeException;

	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
		final IAgent sourceAgent = source == null ? null : source.getAgent();
		results.remove(sourceAgent);
		final com.google.common.base.Predicate<IShape> toRemove = each -> {
			final IAgent a = each.getAgent();
			return a == null || a.dead()
					|| a.getPopulation() != this
							&& (a.getPopulation().getGamlType().getContentType() != this.getGamlType().getContentType()
									|| !this.contains(a));
		};
		results.removeIf(toRemove);
	}

	@Override
	public boolean accept(final IScope scope, final IShape source, final IShape a) {
		final IAgent agent = a.getAgent();
		if (agent == null || agent.getPopulation() != this || agent.dead()) return false;
		final IAgent as = source.getAgent();
		return agent != as;
	}

	// ----------------------------------------------------------------------------------
	// 2. IStepable
	// ----------------------------------------------------------------------------------
	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		manageMirror(scope);
		return true;
	}

	@Override
	public boolean step(final IScope scope) throws GamaRuntimeException {
		final IExpression frequencyExp = species.getFrequency();
		if (frequencyExp != null) {
			final int frequency = Cast.asInt(scope, frequencyExp.value(scope));
			final int step = scope.getClock().getCycle();
			if (frequency == 0 || step % frequency != 0) return true;
		}
		manageMirror(scope);
		getSpecies().getArchitecture().preStep(scope, this);
		return stepAgents(scope);
	}

	// ----------------------------------------------------------------------------------
	// 3. IDisposable
	// ----------------------------------------------------------------------------------
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

	// ----------------------------------------------------------------------------------
	// 4. IPopulationSet + IAgentFilter additions already integrated above
	// (getPopulations, accept, filter, stream(IScope) below)
	// ----------------------------------------------------------------------------------
	@Override
	@SuppressWarnings ("unchecked")
	public StreamEx<T> stream(final IScope scope) {
		return (StreamEx<T>) stream();
	}

	// ----------------------------------------------------------------------------------
	// 5. IList / List / Collection / Iterable
	// ----------------------------------------------------------------------------------
	@Override
	public int size() {
		return Iterables.size(internalListOfAgents(false, true));
	}

	@Override
	public boolean isEmpty() { return size() == 0; }

	@Override
	public boolean contains(final Object o) {
		return internalListOfAgents(false, false).contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return internalListOfAgents(false, true).iterator();
	}

	@Override
	public <A> A[] toArray(final A[] a) {
		return (A[]) internalListOfAgents(false, true).toArray();
	}

	@Override
	public T[] toArray() {
		return (T[]) internalListOfAgents(false, true).toArray(new IAgent[0]);
	}

	@Override
	public boolean add(final T e) {
		return internalListOfAgents(false, false).add(e);
	}

	@Override
	public boolean remove(final Object o) {
		return internalListOfAgents(false, false).remove(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (Object o : c)
			if (!contains(o)) return false;
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends T> c) {
		boolean changed = false;
		for (T e : c) { changed |= add(e); }
		return changed;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends T> c) {
		return addAll(c);
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean changed = false;
		for (Object o : c) { changed |= remove(o); }
		return changed;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return internalListOfAgents(false, false).retainAll(c);
	}

	@Override
	public void clear() { /* populations are cleared through killMembers/dispose */ }

	@Override
	public T get(final int index) {
		return internalListOfAgents(false, true).get(index);
	}

	@Override
	public T set(final int index, final T element) {
		return internalListOfAgents(false, false).set(index, element);
	}

	@Override
	public void add(final int index, final T element) {
		internalListOfAgents(false, false).add(index, element);
	}

	@Override
	public T remove(final int index) {
		return internalListOfAgents(false, false).remove(index);
	}

	@Override
	public int indexOf(final Object o) {
		return internalListOfAgents(false, false).indexOf(o);
	}

	@Override
	public int lastIndexOf(final Object o) {
		return indexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return internalListOfAgents(false, true).listIterator();
	}

	@Override
	public ListIterator<T> listIterator(final int index) {
		return internalListOfAgents(false, true).listIterator(index);
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex) {
		return internalListOfAgents(false, true).subList(fromIndex, toIndex);
	}

	// ----------------------------------------------------------------------------------
	// 6. Comparable
	// ----------------------------------------------------------------------------------
	@Override
	public int compareTo(final IPopulation<T> o) {
		return getName().compareTo(o.getName());
	}

	// ----------------------------------------------------------------------------------
	// 7. Object overrides
	// ----------------------------------------------------------------------------------
	@Override
	public final int hashCode() {
		return hashCode;
	}

	@Override
	public final boolean equals(final Object o) {
		return o == this;
	}

	@Override
	public String toString() {
		return "Population of " + species.getName();
	}

	// ----------------------------------------------------------------------------------
	// 8. Internal & protected helpers (cleaned)
	// ----------------------------------------------------------------------------------
	/**
	 * Internal list of agents.
	 *
	 * @param makeCopy
	 *            the make copy
	 * @param onlyLiving
	 *            the only living
	 * @return the list
	 */

	public abstract List<T> internalListOfAgents(boolean makeCopy, boolean onlyLiving);

	/**
	 * Manage mirror.
	 *
	 * @param scope
	 *            the scope
	 */
	protected void manageMirror(final IScope scope) { /* optional override */ }

	/**
	 * Step agents.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	protected boolean stepAgents(final IScope scope) {
		return false;
	}

	/**
	 * Compute topology.
	 *
	 * @param scope
	 *            the scope
	 */
	protected abstract void computeTopology(IScope scope);

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

	@SuppressWarnings ("unchecked")
	@Override
	public Stream<T> stream() {
		return StreamEx.of(internalListOfAgents(false, true));
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
	// Attribute ordering utility
	public IVariable[] orderAttributes(final TypeDescription ecd, final Predicate<VariableDescription> keep,
			final Set<String> facetsToConsider) {
		final DirectedAcyclicGraph<String, Object> graph = new VariableOrderingGraph();
		ecd.visitAllAttributes(d -> {
			VariableDescription var = (VariableDescription) d;
			if (keep.apply(var)) {
				String name = var.getName();
				graph.addVertex(name);
				for (final VariableDescription dep : var.getDependencies(facetsToConsider, false, true)) {
					if (keep.apply(dep)) { tryAdd(graph, dep.getName(), name); }
				}
				if (var.isSyntheticSpeciesContainer()) { tryAdd(graph, SHAPE, name); }
			}
			return true;
		});
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
		return Iterators.toArray(transform(graph.iterator(), AbstractPopulation.this::getVar), IVariable.class);
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
		} catch (IllegalArgumentException e) { /* ignore cycles */ }
	}
}
