/*******************************************************************************************************
 *
 * AbstractArchitecture.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.skill;

import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * The Class AbstractArchitecture.
 * 
 * <p>
 * Abstract base class for implementing custom agent control architectures in GAMA. This class extends Skill and
 * implements IArchitecture, providing a template for creating architectures that govern how agents execute their
 * behaviors during simulation steps.
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * AbstractArchitecture provides:
 * </p>
 * <ul>
 * <li><b>Template Implementation:</b> Base functionality for architecture implementations</li>
 * <li><b>Simplified Development:</b> Handles boilerplate code for custom architectures</li>
 * <li><b>Consistent Interface:</b> Ensures architectures follow GAMA's architecture contract</li>
 * <li><b>Default Behaviors:</b> No-op implementations for optional methods</li>
 * </ul>
 * 
 * <h3>Key Methods to Override</h3>
 * <ul>
 * <li><b>privateExecuteIn(scope):</b> Main execution logic (REQUIRED)</li>
 * <li><b>init(scope):</b> Initialize architecture for agent (optional)</li>
 * <li><b>abort(scope):</b> Clean up when agent dies (optional)</li>
 * <li><b>verifyBehaviors(context):</b> Validate behaviors at compile time (optional)</li>
 * <li><b>preStep(scope, population):</b> Population-level setup before stepping (optional)</li>
 * </ul>
 * 
 * <h3>Usage - Implementing Custom Architecture</h3>
 * 
 * <h4>1. Simple Custom Architecture</h4>
 * 
 * <pre>
 * <code>
 * {@literal @}skill(name = "priority_based")
 * {@literal @}doc("Execute behaviors in priority order")
 * public class PriorityArchitecture extends AbstractArchitecture {
 *     
 *     {@literal @}Override
 *     public Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
 *         IAgent agent = scope.getAgent();
 *         ISpecies species = agent.getSpecies();
 *         
 *         // Get all reflexes
 *         List&lt;IStatement&gt; behaviors = species.getBehaviors();
 *         
 *         // Sort by priority (custom logic)
 *         behaviors.sort(this::comparePriority);
 *         
 *         // Execute in priority order
 *         for (IStatement behavior : behaviors) {
 *             if (shouldExecute(scope, behavior)) {
 *                 behavior.executeOn(scope);
 *             }
 *         }
 *         
 *         return null;
 *     }
 *     
 *     private int comparePriority(IStatement a, IStatement b) {
 *         // Custom priority comparison logic
 *         return getPriority(a) - getPriority(b);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Architecture with State</h4>
 * 
 * <pre>
 * <code>
 * {@literal @}skill(name = "learning")
 * {@literal @}doc("Agent learns from experience")
 * public class LearningArchitecture extends AbstractArchitecture {
 *     
 *     private Map&lt;IAgent, LearningState&gt; agentStates = new HashMap&lt;&gt;();
 *     
 *     {@literal @}Override
 *     public boolean init(IScope scope) {
 *         // Initialize learning state for agent
 *         IAgent agent = scope.getAgent();
 *         agentStates.put(agent, new LearningState());
 *         return true;
 *     }
 *     
 *     {@literal @}Override
 *     public Object privateExecuteIn(IScope scope) {
 *         IAgent agent = scope.getAgent();
 *         LearningState state = agentStates.get(agent);
 *         
 *         // Select behavior based on learned experience
 *         IStatement behavior = selectBehavior(scope, state);
 *         Object result = behavior.executeOn(scope);
 *         
 *         // Update learning based on result
 *         state.learn(behavior, result);
 *         
 *         return result;
 *     }
 *     
 *     {@literal @}Override
 *     public boolean abort(IScope scope) {
 *         // Clean up when agent dies
 *         agentStates.remove(scope.getAgent());
 *         return true;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Architecture with Validation</h4>
 * 
 * <pre>
 * <code>
 * {@literal @}skill(name = "strict_fsm")
 * public class StrictFSMArchitecture extends AbstractArchitecture {
 *     
 *     {@literal @}Override
 *     public void verifyBehaviors(ISpecies context) {
 *         // Validate at compile time
 *         List&lt;IStatement&gt; behaviors = context.getBehaviors();
 *         
 *         for (IStatement stmt : behaviors) {
 *             if (!(stmt instanceof StateStatement)) {
 *                 throw GamaRuntimeException.error(
 *                     "strict_fsm architecture only accepts 'state' statements", 
 *                     context.getScope());
 *             }
 *         }
 *         
 *         // Verify there's an initial state
 *         if (!hasInitialState(behaviors)) {
 *             throw GamaRuntimeException.error(
 *                 "No initial state defined", 
 *                 context.getScope());
 *         }
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>4. Using Custom Architecture in GAML</h4>
 * 
 * <pre>
 * <code>
 * species learner control: learning {
 *     // Uses LearningArchitecture
 *     
 *     reflex explore {
 *         // Learning architecture decides when to execute
 *     }
 *     
 *     reflex exploit {
 *         // Based on learned experience
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Provided Default Implementations</h3>
 * <ul>
 * <li><b>serializeToGaml():</b> Returns architecture name</li>
 * <li><b>getKeyword():</b> Returns architecture name</li>
 * <li><b>getTrace():</b> Returns empty string (no trace)</li>
 * <li><b>getFacet():</b> Returns null (no facets)</li>
 * <li><b>getFacetValue():</b> Returns default value</li>
 * <li><b>hasFacet():</b> Returns false</li>
 * <li><b>verifyBehaviors():</b> No-op (no validation)</li>
 * <li><b>dispose():</b> No-op (no cleanup)</li>
 * </ul>
 * 
 * <h3>Architecture Execution Flow</h3>
 * <ol>
 * <li><b>Agent Creation:</b> Architecture instance created per agent</li>
 * <li><b>Initialization:</b> init() called</li>
 * <li><b>Each Cycle:</b>
 * <ul>
 * <li>preStep() called once per population</li>
 * <li>privateExecuteIn() called for each agent</li>
 * </ul>
 * </li>
 * <li><b>Agent Death:</b> abort() called</li>
 * <li><b>Disposal:</b> dispose() called</li>
 * </ol>
 * 
 * <h3>Best Practices</h3>
 * <ul>
 * <li>Always override privateExecuteIn() - it's the core execution method</li>
 * <li>Use init() for per-agent initialization</li>
 * <li>Use preStep() for population-wide setup (executed once per cycle)</li>
 * <li>Use verifyBehaviors() to catch errors at compile time</li>
 * <li>Clean up resources in abort() and dispose()</li>
 * <li>Consider thread safety if maintaining shared state</li>
 * </ul>
 * 
 * <h3>Comparison with Skill</h3>
 * <table border="1">
 * <tr>
 * <th>Aspect</th>
 * <th>Skill</th>
 * <th>Architecture</th>
 * </tr>
 * <tr>
 * <td>Purpose</td>
 * <td>Add attributes and actions</td>
 * <td>Control behavior execution</td>
 * </tr>
 * <tr>
 * <td>Multiple?</td>
 * <td>Yes (species can have many)</td>
 * <td>No (only one per species)</td>
 * </tr>
 * <tr>
 * <td>Execution</td>
 * <td>Actions called explicitly</td>
 * <td>Automatically controls stepping</td>
 * </tr>
 * </table>
 * 
 * @see IArchitecture
 * @see Skill
 * @see IStatement
 * @see IAgent
 * @author drogoul
 * @since GAMA 1.0
 */
public abstract class AbstractArchitecture extends Skill implements IArchitecture {

	/**
	 * Instantiates a new abstract architecture.
	 */
	public AbstractArchitecture() {}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	@Override
	public String getKeyword() { return getName(); }

	@Override
	public String getTrace(final IScope scope) {
		return "";
	}

	@Override
	public IExpression getFacet(final String... key) {
		return null;
	}

	/**
	 * Gets the facet value.
	 *
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @return the facet value
	 */

	/**
	 * Gets the facet value.
	 *
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the facet value
	 */

	/**
	 * Gets the facet value.
	 *
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the facet value
	 */

	/**
	 * Gets the facet value.
	 *
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the facet value
	 */
	@Override
	public <T> T getFacetValue(final IScope scope, final String key, final T defaultValue) {
		return null;
	}

	@Override
	public boolean hasFacet(final String key) {
		return false;
	}

	@Override
	public void verifyBehaviors(final ISpecies context) {}

	@Override
	public void dispose() {}

}