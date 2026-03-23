/*******************************************************************************************************
 *
 * GamlAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import gama.api.compilation.descriptions.IModelDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.IGraph;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.misc.IContainer;
import gama.core.population.MetaPopulation;

/**
 * The Class GamlAgent. Represents agents that can be manipulated in GAML. They are provided with everything their
 * species defines. Adds the possibility for these agents to become 'macro-agents', i.e. to manage micro-populations
 */
@SuppressWarnings ("unchecked")
public class GamlAgent extends MinimalAgent implements IMacroAgent {

	// hqnghi manipulate micro-models AD put it to null to have lazy
	/** The extern micro populations. */
	// initialization (saves some bytes in each agent)
	protected IMap<String, IPopulation<? extends IAgent>> externMicroPopulations;
	// Added to optimize the traversal of "non-minimal" agents that contain
	/** The micro populations. */
	// micropopulations
	protected final AtomicReference<IPopulation<? extends IAgent>[]> microPopulations = new AtomicReference<>();

	/** The Constant NO_POP. */
	static final IPopulation<? extends IAgent>[] NO_POP = new IPopulation[0];

	// end-hqnghi

	/**
	 * @param s
	 *            the population used to prototype the agent.
	 */
	public GamlAgent(final IPopulation<? extends IAgent> s, final int index) {
		super(s, index);
	}

	/**
	 * @param gridPopulation
	 * @param geometry
	 */
	public GamlAgent(final IPopulation<? extends IAgent> gridPopulation, final int index, final IShape geometry) {
		super(gridPopulation, index, geometry);
	}

	/**
	 * Checks if is population.
	 *
	 * @param populationName
	 *            the population name
	 * @return the boolean
	 */
	private Boolean isPopulation(final String populationName) {
		final IVariable v = getSpecies().getVar(populationName);
		if (v == null) return false;
		return v.isMicroPopulation();
	}

	@Override
	public IPopulation<? extends IAgent>[] getMicroPopulations() {
		IPopulation<? extends IAgent>[] current = microPopulations.get();
		if (current == null) {
			final List<IPopulation<?>> pops = new ArrayList<>();
			forEachAttribute((s, o) -> {
				if (isPopulation(s)) { pops.add((IPopulation<?>) o); }
				return true;
			});
			IPopulation<? extends IAgent>[] computed = pops.toArray(new IPopulation[pops.size()]);
			if (computed.length == 0) {
				computed = NO_POP;
			} else {
				Arrays.sort(computed, (p1, p2) -> p1.isGrid() ? p2.isGrid() ? 0 : 1 : p2.isGrid() ? -1 : 0);
			}
			// If another thread already set it, use that result; otherwise ours wins.
			microPopulations.compareAndSet(null, computed);
			current = microPopulations.get();
		}
		return current;
	}

	@Override
	protected boolean initSubPopulations(final IScope scope) {
		for (final IPopulation<? extends IAgent> pop : getMicroPopulations()) {
			if (!scope.init(pop).passed()) return false;
		}
		return true;
	}

	@Override
	protected boolean stepSubPopulations(final IScope scope) {
		for (final IPopulation<? extends IAgent> pop : getMicroPopulations()) {
			if (!scope.step(pop).passed()) return false;
		}
		return true;
	}

	@Override
	public IList<IAgent> captureMicroAgents(final IScope scope, final ISpecies microSpecies,
			final IList<IAgent> microAgents) throws GamaRuntimeException {
		if (microAgents == null || microAgents.isEmpty() || microSpecies == null
				|| !this.getSpecies().getMicroSpecies().contains(microSpecies))
			return GamaListFactory.getEmptyList();
		IList<IAgent> capturedAgents = GamaListFactory.create(Types.AGENT);
		IList<IAgent> candidates = GamaListFactory.create(Types.AGENT);
		for (final IAgent a : microAgents.iterable(scope)) {
			if (this.canCapture(a, microSpecies)) { candidates.add(a); }
		}
		final IPopulation<? extends IAgent> microSpeciesPopulation = this.getPopulationFor(microSpecies);
		for (final IAgent micro : candidates) {
			final ISerialisedAgent savedMicro = SerialisedAgent.of(micro, true);
			micro.dispose();
			capturedAgents.add(savedMicro.restoreInto(scope, microSpeciesPopulation));
		}
		return capturedAgents;
	}

	@Override
	public IAgent captureMicroAgent(final IScope scope, final ISpecies microSpecies, final IAgent microAgent)
			throws GamaRuntimeException {
		if (this.canCapture(microAgent, microSpecies)) {
			final IPopulation<? extends IAgent> microSpeciesPopulation = this.getMicroPopulation(microSpecies);
			final ISerialisedAgent savedMicro = SerialisedAgent.of(microAgent, true);
			microAgent.dispose();
			return savedMicro.restoreInto(scope, microSpeciesPopulation);
		}

		return null;
	}

	@Override
	public IList<IAgent> releaseMicroAgents(final IScope scope, final IList<IAgent> microAgents)
			throws GamaRuntimeException {
		IPopulation<? extends IAgent> originalSpeciesPopulation;
		final IList<IAgent> releasedAgents = GamaListFactory.create(Types.AGENT);

		for (final IAgent micro : microAgents.iterable(scope)) {
			final ISerialisedAgent savedMicro = SerialisedAgent.of(micro, true);
			originalSpeciesPopulation = micro.getPopulationFor(micro.getSpecies().getParentSpecies());
			micro.dispose();
			releasedAgents.add(savedMicro.restoreInto(scope, originalSpeciesPopulation));
		}
		return releasedAgents;
	}

	/**
	 * Migrates some micro-agents from one micro-species to another micro-species of this agent's species.
	 *
	 * @param microAgent
	 * @param newMicroSpecies
	 * @return
	 */
	@Override
	public IList<IAgent> migrateMicroAgents(final IScope scope, final IList<IAgent> microAgents,
			final ISpecies newMicroSpecies) {
		final List<IAgent> immigrantCandidates = GamaListFactory.create(Types.AGENT);

		for (final IAgent m : microAgents.iterable(scope)) {
			if (m.getSpecies().isPeer(newMicroSpecies)) { immigrantCandidates.add(m); }
		}

		final IList<IAgent> immigrants = GamaListFactory.create(Types.AGENT);
		if (!immigrantCandidates.isEmpty()) {
			final IPopulation<? extends IAgent> microSpeciesPopulation = this.getPopulationFor(newMicroSpecies);
			for (final IAgent micro : immigrantCandidates) {
				final ISerialisedAgent savedMicro = SerialisedAgent.of(micro, true);
				micro.dispose();
				immigrants.add(savedMicro.restoreInto(scope, microSpeciesPopulation));
			}
		}

		return immigrants;
	}

	/**
	 * Migrates some micro-agents from one micro-species to another micro-species of this agent's species.
	 *
	 * @param microAgent
	 * @param newMicroSpecies
	 * @return
	 */
	@Override
	public IList<IAgent> migrateMicroAgents(final IScope scope, final ISpecies oldMicroSpecies,
			final ISpecies newMicroSpecies) {
		final IPopulation<? extends IAgent> oldMicroPop = this.getPopulationFor(oldMicroSpecies);

		final IPopulation<? extends IAgent> newMicroPop = this.getPopulationFor(newMicroSpecies);
		final IList<IAgent> immigrants = GamaListFactory.create(Types.AGENT);
		// final Iterator<IAgent> it = oldMicroPop.iterator();
		while (!oldMicroPop.isEmpty()) {
			// while (it.hasNext()) {
			final IAgent m = oldMicroPop.get(0);
			final ISerialisedAgent savedMicro = SerialisedAgent.of(m, true);
			m.dispose();
			immigrants.add(savedMicro.restoreInto(scope, newMicroPop));

		}

		return immigrants;
	}

	@Override
	public void initializeMicroPopulation(final IScope scope, final String name) {
		final ISpecies microSpec = getModel().getSpecies(name);
		final IPopulation<? extends IAgent> microPop =
				scope.getPopulationFactory().createPopulation(scope, this, microSpec);
		registerMicropopulation(scope, microSpec, microPop);
	}

	/**
	 * Register micropopulation.
	 *
	 * @param scope
	 *            the scope
	 * @param microSpec
	 *            the micro spec
	 * @param microPop
	 *            the micro pop
	 */
	protected void registerMicropopulation(final IScope scope, final ISpecies microSpec,
			final IPopulation<? extends IAgent> microPop) {
		setAttribute(microSpec.getName(), microPop);
		// Invalidate the cached micro-populations array so it will be recomputed on next access
		microPopulations.set(null);
		microPop.initializeFor(scope);
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public void dispose() {
		if (dead) return;
		final IPopulation[] microPops = getMicroPopulations();
		for (final IPopulation pop : microPops) { pop.dispose(); }

		final Object graph = getAttribute("attached_graph");
		if (graph instanceof IGraph g) { g.disposeVertex(this); }
		super.dispose();
	}

	@Override
	public IPopulation<? extends IAgent> getMicroPopulation(final String microSpeciesName) {
		final Object o = getAttribute(microSpeciesName);
		if (o instanceof IPopulation) return (IPopulation<? extends IAgent>) o;
		return null;
	}

	@Override
	public IPopulation<? extends IAgent> getMicroPopulation(final ISpecies microSpecies) {
		final Object o = getAttribute(microSpecies.getName());
		return o instanceof IPopulation ? (IPopulation<IAgent>) o : null;
	}

	@Override
	public boolean hasMembers() {
		if (dead()) return false;
		for (final IPopulation pop : getMicroPopulations()) { if (pop.size() > 0) return true; }
		return false;
	}

	@Override
	public IContainer<?, IAgent> getMembers(final IScope scope) {
		if (dead()) return GamaListFactory.getEmptyList();
		return new MetaPopulation(getMicroPopulations());
	}

	@Override
	public void setMembers(final IList<IAgent> newMembers) {
		// Directly changing "members" not supported
	}

	/*
	 * Returns the number of agents for which this agent is the direct host
	 */
	@Override
	public int getMembersSize(final IScope scope) {
		return getMembers(scope).length(scope);
	}

	@Override
	public void setAgents(final IList<IAgent> agents) {
		// "agents" is read-only attribute
	}

	@Override
	public IList<IAgent> getAgents(final IScope scope) {
		if (!hasMembers()) return GamaListFactory.getEmptyList();

		final IContainer<?, IAgent> members = getMembers(scope);
		final IList<IAgent> agents = GamaListFactory.create(Types.AGENT);
		agents.addAll(members.listValue(scope, Types.NO_TYPE, false));
		for (final IAgent m : members.iterable(scope)) {
			if (m instanceof IMacroAgent) { agents.addAll(((IMacroAgent) m).getAgents(scope)); }
		}

		return agents;
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final ISpecies species) {
		// hqnghi adjust to get population for species which come from main as
		// well micro models
		final IModelDescription micro = species.getDescription().getModelDescription();
		final IModelDescription main = this.getModel().getDescription();
		IPopulation<? extends IAgent> microPopulation = null;
		if (main.getMicroModel(micro.getAlias()) == null) {
			microPopulation = this.getMicroPopulation(species);
			if (microPopulation == null && getHost() != null) { microPopulation = getHost().getPopulationFor(species); }
		} else {
			microPopulation = getSimulation().getExternMicroPopulationFor(micro.getAlias() + "." + species.getName());
		}
		// end-hqnghi
		return microPopulation;
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final String speciesName) {
		final IPopulation<? extends IAgent> microPopulation = this.getMicroPopulation(speciesName);
		if (microPopulation == null && getHost() != null) return getHost().getPopulationFor(speciesName);
		return microPopulation;
	}

	/**
	 * Verifies if this agent can capture other agent as newSpecies.
	 *
	 * @return true if the following conditions are correct: 1. newSpecies is one micro-species of this agent's species;
	 *         2. newSpecies is a sub-species of this agent's species or other species is a sub-species of this agent's
	 *         species; 3. the "other" agent is not macro-agent of this agent; 4. the "other" agent is not a micro-agent
	 *         of this agent.
	 */
	@Override
	public boolean canCapture(final IAgent other, final ISpecies newSpecies) {
		if (other == null || other.dead() || newSpecies == null || !this.getSpecies().containMicroSpecies(newSpecies))
			return false;
		if (getMacroAgents().contains(other)) return false;
		return !this.equals(other.getHost());
	}

	@Override
	public void addExternMicroPopulation(final String expName, final IPopulation<? extends IAgent> pop) {
		if (externMicroPopulations == null) {
			externMicroPopulations = GamaMapFactory.create(Types.STRING, Types.LIST.of(Types.AGENT));
		}
		externMicroPopulations.put(expName, pop);
	}

	@Override
	public IPopulation<? extends IAgent> getExternMicroPopulationFor(final String expName) {
		if (externMicroPopulations != null) return externMicroPopulations.get(expName);
		return null;
	}

}
