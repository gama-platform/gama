/*******************************************************************************************************
 *
 * SerialisedAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonObject;
import gama.api.utils.json.IJsonValue;

/**
 * The Class SerialisedAgent.
 *
 * <p>
 * The main implementation of ISerialisedAgent, providing complete serialization and deserialization capabilities for
 * GAMA agents. This record class encapsulates an agent's state in a portable, immutable representation that can be
 * stored, transmitted, or used to restore agent state.
 * </p>
 *
 * <h3>Structure</h3>
 * <p>
 * SerialisedAgent is a Java record with four components:
 * </p>
 * <ul>
 * <li><b>index:</b> The agent's unique index within its population</li>
 * <li><b>species:</b> The name of the agent's species</li>
 * <li><b>attributes:</b> Map of serializable attribute names and values</li>
 * <li><b>innerPopulations:</b> Nested populations for macro-agents</li>
 * </ul>
 *
 * <h3>Attribute Filtering</h3>
 * <p>
 * Not all agent attributes are serialized. The following are excluded:
 * </p>
 * <ul>
 * <li><b>NON_SERIALISABLE:</b> members, agents, location, host, peers, experiment, world_agent, time, duration,
 * index</li>
 * <li><b>GRID_NON_SERIALISABLE:</b> grid_x, grid_y, neighbors (for grid agents)</li>
 * <li><b>Populations:</b> When serializePopulations=true, population attributes are stored separately in
 * innerPopulations</li>
 * </ul>
 *
 * <h3>Special Handling</h3>
 *
 * <h4>Simulation Agents</h4>
 * <p>
 * Simulation agents have additional state serialized:
 * </p>
 * <ul>
 * <li>Random number generator seed and algorithm</li>
 * <li>Current cycle number</li>
 * <li>Resource usage statistics</li>
 * <li>Optionally: simulation history (controlled by SERIALISE_HISTORY attribute)</li>
 * </ul>
 *
 * <h4>Grid Agents</h4>
 * <p>
 * Grid agents exclude grid-specific attributes (grid_x, grid_y, neighbors) as these are implicit in the grid structure.
 * </p>
 *
 * <h4>Macro Agents</h4>
 * <p>
 * Macro-agents with micro-populations have those populations serialized recursively into innerPopulations map.
 * </p>
 *
 * <h3>Usage Examples</h3>
 *
 * <h4>1. Basic Serialization</h4>
 *
 * <pre>
 * <code>
 * // Serialize an agent
 * IAgent agent = ...;
 * SerialisedAgent serialized = SerialisedAgent.of(agent, false);
 *
 * // Access components
 * int index = serialized.index();
 * String species = serialized.species();
 * Map&lt;String, Object&gt; attrs = serialized.attributes();
 * </code>
 * </pre>
 *
 * <h4>2. Serialization with Populations</h4>
 *
 * <pre>
 * <code>
 * // Serialize macro-agent with its populations
 * IMacroAgent macroAgent = ...;
 * SerialisedAgent serialized = SerialisedAgent.of(macroAgent, true);
 *
 * // Access inner populations
 * Map&lt;String, ISerialisedPopulation&gt; innerPops = serialized.innerPopulations();
 * </code>
 * </pre>
 *
 * <h4>3. Restoration into Population</h4>
 *
 * <pre>
 * <code>
 * // Restore agent as new member of population
 * IPopulation&lt;IAgent&gt; targetPop = ...;
 * IAgent restored = serialized.restoreInto(scope, targetPop);
 * </code>
 * </pre>
 *
 * <h4>4. Restoration onto Existing Agent</h4>
 *
 * <pre>
 * <code>
 * // Update existing agent with serialized state
 * IAgent existingAgent = population.getAgent(index);
 * serialized.restoreAs(scope, existingAgent);
 * </code>
 * </pre>
 *
 * <h4>5. JSON Serialization</h4>
 *
 * <pre>
 * <code>
 * // Convert to JSON
 * IJson json = ...;
 * IJsonValue jsonValue = serialized.serializeToJson(json);
 *
 * // JSON structure:
 * // {
 * //   "species": "person",
 * //   "index": 42,
 * //   "attributes": { "name": "John", "age": 30 },
 * //   "populations": { "children": [...] }
 * // }
 * </code>
 * </pre>
 *
 * <h4>6. Creating from Manual Data</h4>
 *
 * <pre>
 * <code>
 * // Create serialized agent from components
 * Map&lt;String, Object&gt; attrs = new HashMap&lt;&gt;();
 * attrs.put("name", "Alice");
 * attrs.put("age", 25);
 *
 * SerialisedAgent serialized = SerialisedAgent.of(5, "person", attrs);
 * </code>
 * </pre>
 *
 * <h3>Restoration Process</h3>
 * <ol>
 * <li><b>Create/Locate Agent:</b> Either create new agent or find existing by index</li>
 * <li><b>Restore Attributes:</b> Set all attribute values directly (bypass updates)</li>
 * <li><b>Restore Populations:</b> For macro-agents, recursively restore inner populations</li>
 * <li><b>Update Simulation State:</b> For simulations, restore RNG, cycle, and usage</li>
 * </ol>
 *
 * <h3>Best Practices</h3>
 * <ul>
 * <li>Use serializePopulations=true only when needed (increases size/complexity)</li>
 * <li>Be aware that transient/computed values are not preserved</li>
 * <li>Test serialization round-trips for complex agent types</li>
 * <li>Consider implementing custom serialization for complex custom types</li>
 * <li>Use SERIALISE_HISTORY flag carefully as history can be large</li>
 * </ul>
 *
 * @param index
 *            The agent's unique index within its population
 * @param species
 *            The name of the agent's species
 * @param attributes
 *            Map of serializable attribute names to values
 * @param innerPopulations
 *            Map of population names to serialized populations (for macro-agents)
 * @see ISerialisedAgent
 * @see ISerialisedPopulation
 * @see IAgent
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.9
 * @date 31 juil. 2023
 */
public record SerialisedAgent(int index, String species, Map<String, Object> attributes,
		Map<String, ISerialisedPopulation> innerPopulations) implements ISerialisedAgent {

	/** All the attributes that are not interesting to serialise for regular agents */
	public static final Set<String> NON_SERIALISABLE = Set.of(IKeyword.MEMBERS, IKeyword.AGENTS, IKeyword.LOCATION,
			IKeyword.HOST, IKeyword.PEERS, IKeyword.EXPERIMENT, IKeyword.WORLD_AGENT_NAME, ISimulationAgent.TIME,
			ITopLevelAgent.Platform.MACHINE_TIME, ISimulationAgent.DURATION, ISimulationAgent.AVERAGE_DURATION,
			ISimulationAgent.TOTAL_DURATION, IKeyword.INDEX);

	/** All the attributes that are not interesting to serialise for grid agents */
	public static final Set<String> GRID_NON_SERIALISABLE =
			Set.of(IKeyword.GRID_X, IKeyword.GRID_Y, IKeyword.NEIGHBORS);

	/** The Constant KEY. */
	public static final String HISTORY_KEY = "**history**";

	/** The node key. */
	public static final String SERIALISE_HISTORY = "**serialise_history**";

	/**
	 * Creates a new SerialisedAgent object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the agent
	 * @return the serialised agent
	 * @date 8 août 2023
	 */
	public static SerialisedAgent of(final IAgent target, final boolean serializePopulations) {
		int index = target.getIndex();
		String species = target.getSpeciesName();
		boolean isGrid = target.getPopulation().isGrid();
		Map<String, Object> attributes =
				filterAttributes(target, isGrid, target.getAttributes(true), serializePopulations);
		Map<String, ISerialisedPopulation> populations =
				filterPopulations(target, isGrid, target.getAttributes(true), serializePopulations);
		SerialisedAgent result = new SerialisedAgent(index, species, attributes, populations);
		if (target instanceof ISimulationAgent sa && !shouldSerializeHistory(sa)) {
			result.attributes().remove(HISTORY_KEY);
		}
		return result;
	}

	/**
	 * Of.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @param species
	 *            the species
	 * @param attributes
	 *            the attributes
	 * @return the serialised agent
	 * @date 7 nov. 2023
	 */
	public static SerialisedAgent of(final int index, final String species, final Map<String, Object> attributes) {
		return new SerialisedAgent(index, species, attributes, Collections.EMPTY_MAP);
	}

	/**
	 * Filter populations.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param target
	 *            the target
	 * @param b
	 *            the b
	 * @param m
	 *            the m
	 * @return the map
	 * @date 29 oct. 2023
	 */
	private static Map<String, ISerialisedPopulation> filterPopulations(final IAgent target, final boolean b,
			final Map<String, Object> m, final boolean serializePopulations) {
		if (!serializePopulations) return Collections.EMPTY_MAP;
		Map<String, ISerialisedPopulation> map = new HashMap<>();
		for (Map.Entry<String, Object> entry : m.entrySet()) {
			Object v = entry.getValue();
			if (v instanceof IPopulation<?> p) {
				map.put(entry.getKey(),
						p.isGrid() ? new SerialisedGrid((IPopulation.Grid) p) : new SerialisedPopulation(p));
			}
		}
		return map;
	}

	/**
	 * Gets the index.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the index
	 * @date 6 août 2023
	 */
	@Override
	public int getIndex() { return index; }

	/**
	 * Filter map.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param m
	 *            the m
	 * @return the object
	 * @date 31 juil. 2023
	 */
	@SuppressWarnings ("unchecked")
	public static Map<String, Object> filterAttributes(final IAgent agent, final boolean isGrid,
			final Map<String, Object> m, final boolean serializePopulations) {
		Map<String, Object> map = new HashMap<>();
		for (Map.Entry<String, Object> entry : m.entrySet()) {
			String k = entry.getKey(); //
			Object v = entry.getValue();
			if (NON_SERIALISABLE.contains(k) || isGrid && GRID_NON_SERIALISABLE.contains(k)
					|| serializePopulations && v instanceof IPopulation) {
				continue;
			}
			map.put(k, v);
		}
		boolean isSim = agent instanceof ISimulationAgent;
		if (isSim) {
			ISimulationAgent sim = (ISimulationAgent) agent;
			map.put(IKeyword.SEED, sim.getSeed());
			map.put(IKeyword.RNG, sim.getRng());
			map.put(ISimulationAgent.USAGE, sim.getUsage());
			map.put(IKeyword.CYCLE, sim.getClock().getCycle());
		}
		if (!isGrid) { map.put(IKeyword.SHAPE, agent.getGeometry()); }
		map.put(IKeyword.NAME, agent.getName());
		return map;
	}

	@Override
	public Object getAttributeValue(final String var) {
		return attributes.get(var);
	}

	@Override
	public void setAttributeValue(final String var, final Object val) {
		attributes.put(var, val);
	}

	/**
	 * @param scope
	 *            Restores the saved agent as a member of the target population.
	 *
	 * @param targetPopulation
	 *            The population that the saved agent will be restored to.
	 * @return
	 * @throws GamaRuntimeException
	 */
	@Override
	public IAgent restoreInto(final IScope scope, final IPopulation<? extends IAgent> targetPopulation)
			throws GamaRuntimeException {
		final List<Map<String, Object>> agentAttrs = new ArrayList<>();
		agentAttrs.add(attributes);
		final List<? extends IAgent> restoredAgents = targetPopulation.createAgents(scope, 1, agentAttrs, true, true);
		IAgent result = restoredAgents.get(0);
		restoreAs(scope, result);
		return result;
	}

	/**
	 * Restore as.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @return the i agent
	 * @date 31 oct. 2023
	 */
	@Override
	public void restoreAs(final IScope scope, final IAgent agent) {
		// Update attributes and micropopulations
		attributes().forEach((name, v) -> { agent.setDirectVarValue(scope, name, v); });
		if (agent instanceof IMacroAgent host && innerPopulations != null) {
			innerPopulations.forEach((name, v) -> {
				IPopulation<? extends IAgent> pop = host.getMicroPopulation(name);
				if (pop != null) {
					if (v instanceof SerialisedGrid sg) {
						sg.restoreAs(scope, pop);
					} else if (v instanceof SerialisedPopulation sp) { sp.restoreAs(scope, pop); }
				}
			});
			// Update simulation-specific variables
			if (agent instanceof ISimulationAgent sim) {
				final Map<String, Object> attr = attributes();
				Double seedValue = (Double) attr.remove(IKeyword.SEED);
				String rngValue = (String) attr.remove(IKeyword.RNG);
				Integer usageValue = (Integer) attr.remove(ISimulationAgent.USAGE);
				sim.generateRandomGenerator(seedValue, rngValue);
				sim.setUsage(usageValue);
				// Update Clock
				final Integer cycle = (Integer) sim.getAttribute(IKeyword.CYCLE);
				sim.getClock().setCycleNoCheck(cycle);

			}
		}
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		IJsonObject.Agent obj = (IJsonObject.Agent) json.agent(species, index).add("attributes", attributes);
		if (innerPopulations != null && !innerPopulations.isEmpty()) { obj.add("populations", innerPopulations); }
		return obj;
	}

	/**
	 * Recreate in.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the i agent
	 * @date 30 oct. 2023
	 */
	public IAgent recreateIn(final IScope scope) {
		IPopulation<?> p = scope.getSimulation().getPopulationFor(species);
		if (p == null)
			throw GamaRuntimeException.error("No population named" + species + " exist in this simulation", scope);
		IAgent a = p.getOrCreateAgent(scope, index);
		restoreAs(scope, a);
		return a;
	}

	/**
	 * Should serialize history.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the sim
	 * @return true, if successful
	 * @date 22 oct. 2023
	 */
	static boolean shouldSerializeHistory(final ISimulationAgent sim) {
		return sim.hasAttribute(SERIALISE_HISTORY) && (Boolean) sim.getAttribute(SERIALISE_HISTORY);
	}

}
