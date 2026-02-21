/*******************************************************************************************************
 *
 * AgentReference.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

import java.util.LinkedList;
import java.util.List;

import gama.api.constants.IKeyword;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;
import gama.api.utils.json.IJsonable;
import one.util.streamex.StreamEx;

/**
 * A unique way to reference agents across simulations within experiments.
 * 
 * <p>AgentReference provides a stable string-based identifier for agents that persists
 * across simulation snapshots, saves, and remote communications. Each reference encodes
 * the full hierarchical path from the experiment through the simulation to the target agent.</p>
 *
 * <h2>Reference Format</h2>
 * <ul>
 *   <li><b>Simulation:</b> {@code "simulation[n]"} - where n is the simulation index</li>
 *   <li><b>Top-level agent:</b> {@code "simulation[n].species_name[m]"} - where m is the agent index</li>
 *   <li><b>Nested agent:</b> {@code "simulation[n].species[m].nested_species[x]"} - hierarchical path</li>
 * </ul>
 *
 * <h2>Assumptions</h2>
 * <ol>
 *   <li>The experiment is unique in the scope</li>
 *   <li>The first species name refers to the simulation</li>
 *   <li>Agent indices are stable within their populations</li>
 * </ol>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Creating References</h3>
 * <pre>{@code
 * // From an agent instance
 * IAgent myAgent = ...;
 * AgentReference ref = AgentReference.of(myAgent);
 * String refString = ref.toString();  // e.g., "simulation[0].people[42]"
 * 
 * // From a reference string
 * AgentReference ref = AgentReference.of("simulation[0].people[42]");
 * 
 * // From arrays
 * AgentReference ref = AgentReference.of(
 *     new String[]{"simulation", "people"},
 *     new Integer[]{0, 42}
 * );
 * }</pre>
 * 
 * <h3>Resolving References</h3>
 * <pre>{@code
 * AgentReference ref = AgentReference.of("simulation[0].people[42]");
 * IAgent agent = ref.getReferencedAgent(scope);
 * if (agent != null && !agent.dead()) {
 *     // Use the agent
 * }
 * }</pre>
 * 
 * <h3>JSON Serialization</h3>
 * <pre>{@code
 * AgentReference ref = AgentReference.of(myAgent);
 * IJson json = IJson.create();
 * ref.toJson(json);
 * String jsonString = json.toString();  // {"reference": "simulation[0].people[42]"}
 * }</pre>
 * 
 * @param species array of species names in the hierarchical path
 * @param index array of agent indices corresponding to each species
 * @param cached_ref the precomputed string representation
 * 
 * @see IAgent
 * @see IPopulation
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.9
 */
public record AgentReference(String[] species, Integer[] index, String cached_ref) implements IJsonable {

	/** The null. */
	public static final AgentReference NULL = new AgentReference(new String[0], new Integer[0], null);

	/**
	 * Creates an agent reference from an agent instance.
	 * 
	 * <p>This method constructs the reference by traversing the agent's host hierarchy
	 * upward to the experiment, collecting species names and indices along the way.</p>
	 * 
	 * @param agt the agent to create a reference for, or null
	 * @return the agent reference, or {@link #NULL} if the agent is null
	 */
	public static AgentReference of(final IAgent agt) {
		if (agt == null) return NULL;
		return of(buildSpeciesArray(agt), buildIndicesArray(agt));
	}

	/**
	 * Parses a reference string to create an AgentReference.
	 * 
	 * <p>The string format should be: {@code "species1[idx1].species2[idx2]..."}
	 * where each segment represents a level in the agent hierarchy.</p>
	 * 
	 * @param ref the reference string to parse
	 * @return the parsed agent reference
	 * 
	 * @throws NumberFormatException if indices cannot be parsed as integers
	 */
	public static AgentReference of(final String ref) {
		String[] tokens = ref.split("[\\[\\]\\.]");
		tokens = StreamEx.of(tokens).filter(s -> !s.isEmpty()).toArray(String.class);
		int size = tokens.length / 2;
		String[] species = new String[size];
		Integer[] index = new Integer[size];
		for (int i = 0; i < size; i++) {
			species[i] = tokens[i * 2];
			index[i] = Integer.decode(tokens[i * 2 + 1]);
		}
		return new AgentReference(species, index, ref);
	}

	/**
	 * Of.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param species
	 *            the species
	 * @param index
	 *            the index
	 * @return the agent reference
	 * @date 1 nov. 2023
	 */
	public static AgentReference of(final String[] species, final Integer[] index) {
		if (species.length == 0) return NULL;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < species.length; i++) {
			sb.append(species[i]).append('[').append(index[i]).append(']').append('.');
		}
		sb.setLength(sb.length() - 1);
		String ref = sb.toString();
		return new AgentReference(species, index, ref);
	}

	@Override
	public String toString() {
		return cached_ref;
	}

	/**
	 * Resolves this reference to retrieve the actual agent instance.
	 * 
	 * <p>This method navigates through the simulation hierarchy using the stored
	 * species names and indices to locate the referenced agent. It starts from
	 * the experiment's simulation population and descends through nested populations.</p>
	 * 
	 * <p><b>Important:</b> The returned agent may be dead. Callers should check
	 * {@link IAgent#dead()} before using the agent.</p>
	 * 
	 * @param scope the current execution scope (must contain a valid experiment)
	 * @return the referenced agent, or null if:
	 *         <ul>
	 *           <li>The scope or experiment is null</li>
	 *           <li>Any population in the path doesn't exist</li>
	 *           <li>Any index in the path is invalid</li>
	 *         </ul>
	 * 
	 * @see IAgent#dead()
	 * @see IPopulation#getOrCreateAgent(IScope, int)
	 */
	public IAgent getReferencedAgent(final IScope scope) {
		if (scope == null) return null;
		IExperimentAgent sim = scope.getExperiment();
		if (sim == null) return null;
		IPopulation<? extends IAgent> pop = sim.getSimulationPopulation();
		IAgent referencedAgt = pop.getOrCreateAgent(scope, index[0]);
		for (int i = 1; i < index.length; i++) {
			pop = referencedAgt.getPopulationFor(species[i]);
			if (pop == null) return null;
			referencedAgt = pop.getOrCreateAgent(scope, index[i]);
		}
		return referencedAgt;
	}

	/**
	 * Builds the species array. simulation > species1 > nested_species > ...
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param a
	 *            the a
	 * @return the string[]
	 * @date 7 août 2023
	 */
	static String[] buildSpeciesArray(final IAgent a) {
		List<String> species = new LinkedList<>();
		species.add(a instanceof ISimulationAgent ? IKeyword.SIMULATION : a.getSpeciesName());
		IAgent host = a.getHost();
		while (host != null && !(host instanceof IExperimentAgent)) {
			species.add(0, host.getSpeciesName());
			host = host.getHost();
		}
		return species.toArray(new String[0]);
	}

	/**
	 * Builds the species array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param a
	 *            the a
	 * @return the int[]
	 * @date 7 août 2023
	 */
	static Integer[] buildIndicesArray(final IAgent a) {
		List<Integer> species = new LinkedList<>();
		species.add(a.getIndex());
		IAgent host = a.getHost();
		while (host != null && !(host instanceof IExperimentAgent)) {
			species.add(0, host.getIndex());
			host = host.getHost();
		}
		return species.toArray(new Integer[0]);
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.object(IJson.Labels.AGENT_REFERENCE_LABEL, toString());
	}

}