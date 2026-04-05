/*******************************************************************************************************
 *
 * ISerialisedAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.serialization;

import java.util.Map;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IScope;
import gama.api.utils.json.IJsonable;

/**
 * The Interface ISerialisedAgent.
 * 
 * <p>
 * Represents a serialized snapshot of an agent's state in GAMA. This interface provides a lightweight, portable
 * representation of an agent that can be stored, transmitted, or used to restore an agent's state at a later time.
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * ISerialisedAgent is used for:
 * </p>
 * <ul>
 * <li><b>Simulation Snapshots:</b> Save and restore simulation states</li>
 * <li><b>Persistence:</b> Store agent states to disk (JSON, XML, etc.)</li>
 * <li><b>Distribution:</b> Transfer agent states across processes or machines</li>
 * <li><b>Debugging:</b> Inspect and analyze agent states offline</li>
 * <li><b>Replication:</b> Create exact copies of agents with all their attributes</li>
 * </ul>
 * 
 * <h3>Core Components</h3>
 * <ul>
 * <li><b>Index:</b> Unique identifier for the agent within its population</li>
 * <li><b>Attributes:</b> Map of attribute names to their serialized values</li>
 * <li><b>Inner Populations:</b> Serialized micro-populations for macro-agents</li>
 * </ul>
 * 
 * <h3>Serialization Process</h3>
 * <ol>
 * <li>Agent attributes are extracted and filtered (excluding transient/computed values)</li>
 * <li>Values are converted to serializable forms</li>
 * <li>For macro-agents, inner populations are recursively serialized</li>
 * <li>Result can be converted to JSON or other formats</li>
 * </ol>
 * 
 * <h3>Restoration Process</h3>
 * <ol>
 * <li>Create or locate target agent in population</li>
 * <li>Restore attribute values from serialized data</li>
 * <li>For macro-agents, restore inner populations recursively</li>
 * <li>Update references and relationships</li>
 * </ol>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <h4>1. Saving Simulation State</h4>
 * 
 * <pre>
 * <code>
 * global {
 *     reflex save_state when: every(100 #cycle) {
 *         save simulation to: "saves/state_" + cycle + ".json" format: json;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Restoring Simulation State</h4>
 * 
 * <pre>
 * <code>
 * experiment myExp {
 *     action restore_from_file {
 *         do restore_simulation from: "saves/state_1000.json";
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Java Usage</h3>
 * 
 * <pre>
 * <code>
 * // Serialize an agent
 * ISerialisedAgent serialized = SerialisedAgent.of(agent, true);
 * 
 * // Access serialized data
 * int index = serialized.getIndex();
 * Object nameValue = serialized.getAttributeValue("name");
 * Map&lt;String, Object&gt; allAttributes = serialized.attributes();
 * 
 * // Restore into population
 * IAgent restoredAgent = serialized.restoreInto(scope, targetPopulation);
 * 
 * // Or restore onto existing agent
 * serialized.restoreAs(scope, existingAgent);
 * 
 * // Export to JSON
 * IJsonValue jsonValue = serialized.serializeToJson(json);
 * </code>
 * </pre>
 * 
 * <h3>Implementation Notes</h3>
 * <ul>
 * <li>Not all attributes are serialized (e.g., transient, computed, or circular references)</li>
 * <li>Grid agents have special handling for grid-specific attributes</li>
 * <li>Simulation agents include additional state (seed, RNG, clock cycle)</li>
 * <li>Macro-agents recursively serialize their micro-populations</li>
 * <li>Serialization preserves agent index for proper restoration</li>
 * </ul>
 * 
 * @see SerialisedAgent
 * @see ISerialisedPopulation
 * @see IAgent
 * @see IJsonable
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.9
 * @date 8 août 2023
 */
public interface ISerialisedAgent extends IJsonable {

	/**
	 * Gets the index.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the index
	 * @date 8 août 2023
	 */
	int getIndex();

	/**
	 * Gets the attribute.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param var
	 *            the var
	 * @return the attribute
	 * @date 8 août 2023
	 */
	Object getAttributeValue(String var);

	/**
	 * Sets the attribute value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param var
	 *            the var
	 * @param val
	 *            the val
	 * @date 8 août 2023
	 */
	void setAttributeValue(String var, Object val);

	/**
	 * Gets the variables.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the variables
	 * @date 29 oct. 2023
	 */
	Map<String, Object> attributes();

	/**
	 * Gets the inner populations.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the inner populations
	 * @date 29 oct. 2023
	 */
	Map<String, ISerialisedPopulation> innerPopulations();

	/**
	 * Restore to.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param microPop
	 *            the micro pop
	 * @date 29 oct. 2023
	 */
	IAgent restoreInto(IScope scope, IPopulation<? extends IAgent> microPop);

	/**
	 * Restore as.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @date 31 oct. 2023
	 */
	void restoreAs(IScope scope, IAgent agent);

}
