/*******************************************************************************************************
 *
 * SerialisedPopulation.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IScope;
import one.util.streamex.StreamEx;

/**
 * The Class SerialisedPopulation.
 * 
 * <p>
 * Standard serialization implementation for regular (non-grid) agent populations in GAMA. This class provides complete
 * save and restore functionality for populations, handling agent creation, updates, and removals during restoration.
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * SerialisedPopulation enables:
 * </p>
 * <ul>
 * <li>Capturing the complete state of a population at a point in time</li>
 * <li>Saving and loading population states from files</li>
 * <li>Transferring populations between simulations</li>
 * <li>Implementing simulation checkpoints and rollback functionality</li>
 * </ul>
 * 
 * <h3>Structure</h3>
 * <p>
 * SerialisedPopulation is a Java record with two components:
 * </p>
 * <ul>
 * <li><b>speciesName:</b> The name of the species this population belongs to</li>
 * <li><b>agents:</b> List of all serialized agents in the population</li>
 * </ul>
 * 
 * <h3>Usage in GAML Context</h3>
 * 
 * <h4>Saving Population State</h4>
 * 
 * <pre>
 * <code>
 * species person {
 *     int age;
 *     string name;
 *     float energy;
 * }
 * 
 * global {
 *     reflex checkpoint when: every(100 #cycle) {
 *         // All person agents are serialized
 *         save simulation to: "checkpoint_" + cycle + ".json" format: json;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>Multi-Level Population</h4>
 * 
 * <pre>
 * <code>
 * species organization {
 *     species employee {
 *         int salary;
 *         string department;
 *     }
 * }
 * 
 * // When serializing organizations, employee populations are also serialized
 * </code>
 * </pre>
 * 
 * <h3>Java Usage</h3>
 * 
 * <pre>
 * <code>
 * // Serialize a population
 * IPopulation&lt;IAgent&gt; population = ...;
 * SerialisedPopulation serialized = new SerialisedPopulation(population);
 * 
 * // Access serialized data
 * String speciesName = serialized.speciesName();
 * List&lt;ISerialisedAgent&gt; agents = serialized.agents();
 * 
 * // Restore to target population
 * IPopulation&lt;IAgent&gt; targetPop = ...;
 * serialized.restoreAs(scope, targetPop);
 * </code>
 * </pre>
 * 
 * <h3>Restoration Process</h3>
 * <p>
 * The restoreAs() method intelligently handles population differences:
 * </p>
 * <ol>
 * <li><b>Match Agents:</b> Agents are matched by index between serialized and target populations</li>
 * <li><b>Update Existing:</b> Agents found in both are updated with serialized values</li>
 * <li><b>Create Missing:</b> Agents in serialized but not in target are created</li>
 * <li><b>Remove Extra:</b> Agents in target but not in serialized are killed</li>
 * </ol>
 * 
 * <h3>Example Scenarios</h3>
 * 
 * <h4>Scenario 1: Exact Match</h4>
 * 
 * <pre>
 * Serialized: [Agent#0, Agent#1, Agent#2]
 * Target:     [Agent#0, Agent#1, Agent#2]
 * Result:     All three updated with serialized values
 * </pre>
 * 
 * <h4>Scenario 2: Missing Agents</h4>
 * 
 * <pre>
 * Serialized: [Agent#0, Agent#1, Agent#2, Agent#3]
 * Target:     [Agent#0, Agent#1]
 * Result:     Agent#0, #1 updated; Agent#2, #3 created
 * </pre>
 * 
 * <h4>Scenario 3: Extra Agents</h4>
 * 
 * <pre>
 * Serialized: [Agent#0, Agent#1]
 * Target:     [Agent#0, Agent#1, Agent#2, Agent#3]
 * Result:     Agent#0, #1 updated; Agent#2, #3 killed
 * </pre>
 * 
 * <h4>Scenario 4: Different Agents</h4>
 * 
 * <pre>
 * Serialized: [Agent#0, Agent#2, Agent#4]
 * Target:     [Agent#1, Agent#3, Agent#5]
 * Result:     Agent#1, #3, #5 killed; Agent#0, #2, #4 created
 * </pre>
 * 
 * <h3>Nested Populations</h3>
 * <p>
 * For macro-agents with micro-populations:
 * </p>
 * 
 * <pre>
 * <code>
 * // Each agent's inner populations are serialized recursively
 * SerialisedPopulation orgPop = new SerialisedPopulation(organizationPopulation);
 * // This includes:
 * // - All organization agents
 * // - Each organization's employee population
 * // - Each employee's attributes
 * </code>
 * </pre>
 * 
 * <h3>Implementation Details</h3>
 * <ul>
 * <li>Uses StreamEx for efficient agent matching and processing</li>
 * <li>Agents are matched by index (Integer key)</li>
 * <li>Death status is cleared after killing extra agents</li>
 * <li>Restoration is atomic - either all succeed or none</li>
 * <li>Preserves agent indices from serialized data</li>
 * </ul>
 * 
 * <h3>Best Practices</h3>
 * <ul>
 * <li>Ensure species compatibility between serialized and target populations</li>
 * <li>Be aware that restoration can kill existing agents</li>
 * <li>Test restoration with various population size differences</li>
 * <li>Consider the impact of agent death on other parts of the simulation</li>
 * <li>Use appropriate error handling when restoring from external files</li>
 * </ul>
 * 
 * @param speciesName
 *            The name of the species
 * @param agents
 *            List of serialized agents in this population
 * @see ISerialisedPopulation
 * @see SerialisedAgent
 * @see IPopulation
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.9
 * @date 31 juil. 2023
 */
public record SerialisedPopulation(String speciesName, List<ISerialisedAgent> agents) implements ISerialisedPopulation {

	/**
	 * Instantiates a new population proxy. This is where the serialised agents are created
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param pop
	 *            the pop
	 * @date 31 juil. 2023
	 */
	public SerialisedPopulation(final IPopulation<? extends IAgent> pop) {
		this(pop.getSpecies().getName(), new ArrayList<>());
		for (IAgent a : pop) { agents.add(SerialisedAgent.of(a, true)); }
	}

	/**
	 * Restore as.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param pop
	 *            the pop
	 * @date 31 oct. 2023
	 */
	public void restoreAs(final IScope scope, final IPopulation<? extends IAgent> pop) {

		Map<Integer, IAgent> agents = StreamEx.of(pop).toMap(IAgent::getIndex, each -> each);
		Map<Integer, ISerialisedAgent> images = StreamEx.of(agents()).toMap(ISerialisedAgent::getIndex, each -> each);
		for (Map.Entry<Integer, ISerialisedAgent> entry : images.entrySet()) {
			int index = entry.getKey();
			// We gather the corresponding agent and remove it from this temp map
			IAgent agent = agents.remove(index);
			// If the agent is not found we create a new one
			if (agent == null) { agent = pop.getOrCreateAgent(scope, index); }
			entry.getValue().restoreAs(scope, agent);
		}
		// The remaining agents in the map are killed
		agents.forEach((i, a) -> { a.primDie(scope); });
		scope.getAndClearDeathStatus();

	}

}
