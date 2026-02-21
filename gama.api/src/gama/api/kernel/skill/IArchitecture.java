/*******************************************************************************************************
 *
 * IArchitecture.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.skill;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * The Interface IArchitecture.
 * 
 * <p>
 * Defines the control architecture that governs how an agent executes its behaviors during a simulation step. An
 * architecture determines the order and conditions under which an agent's reflexes and other behaviors are executed,
 * allowing for different behavioral paradigms (reactive, cognitive, BDI, FSM, etc.).
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * Architectures provide:
 * </p>
 * <ul>
 * <li><b>Behavior Control:</b> Define how and when agent behaviors execute</li>
 * <li><b>Execution Order:</b> Control the sequence of reflex execution</li>
 * <li><b>Lifecycle Management:</b> Initialize and abort agent execution</li>
 * <li><b>Validation:</b> Verify behaviors are compatible with the architecture</li>
 * <li><b>Pre-processing:</b> Setup before population-level execution</li>
 * </ul>
 * 
 * <h3>Built-in Architectures</h3>
 * <ul>
 * <li><b>reflex (default):</b> Execute all reflexes in declaration order</li>
 * <li><b>fsm:</b> Finite State Machine with states and transitions</li>
 * <li><b>weighted_tasks:</b> Execute tasks based on weights/priorities</li>
 * <li><b>sorted_tasks:</b> Execute tasks in sorted order</li>
 * <li><b>user_only:</b> No automatic execution, user controls</li>
 * <li><b>user_first:</b> User command first, then reflexes</li>
 * <li><b>user_last:</b> Reflexes first, then user command</li>
 * </ul>
 * 
 * <h3>Extension Architectures</h3>
 * <ul>
 * <li><b>bdi:</b> Belief-Desire-Intention for cognitive agents (extension)</li>
 * <li><b>moving:</b> Path-following and navigation behaviors</li>
 * </ul>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <h4>1. Default Reflex Architecture</h4>
 * 
 * <pre>
 * <code>
 * species animal {
 *     // Default architecture: reflexes execute in order
 *     reflex eat { }
 *     reflex move { }
 *     reflex reproduce { }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Finite State Machine</h4>
 * 
 * <pre>
 * <code>
 * species robot control: fsm {
 *     state searching initial: true {
 *         transition to: moving when: target != nil;
 *     }
 *     
 *     state moving {
 *         do goto target: target;
 *         transition to: working when: location = target;
 *     }
 *     
 *     state working {
 *         do work;
 *         transition to: searching when: task_complete;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Weighted Tasks</h4>
 * 
 * <pre>
 * <code>
 * species worker control: weighted_tasks {
 *     task eat priority: hunger_level {
 *         // Higher hunger = higher priority
 *     }
 *     
 *     task work priority: 5 {
 *         // Fixed priority
 *     }
 *     
 *     task rest priority: 100 - energy {
 *         // Lower energy = higher priority
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>4. BDI Architecture (Extension)</h4>
 * 
 * <pre>
 * <code>
 * species cognitive_agent control: bdi {
 *     perception {
 *         // Update beliefs based on environment
 *     }
 *     
 *     rule when: has_belief("food_nearby") {
 *         desire: "eat_food";
 *     }
 *     
 *     plan eat_food intention: "eat_food" {
 *         // Execute plan to achieve intention
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>5. User-Controlled</h4>
 * 
 * <pre>
 * <code>
 * species player control: user_only {
 *     // No automatic behaviors
 *     // Agent only acts when user issues commands
 *     
 *     user_command "Move North" {
 *         location <- location + {0, 1};
 *     }
 *     
 *     user_command "Attack" {
 *         do attack;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Java Usage - Implementing Custom Architecture</h3>
 * 
 * <pre>
 * <code>
 * {@literal @}skill(name = "custom_architecture")
 * public class CustomArchitecture extends AbstractArchitecture {
 *     
 *     {@literal @}Override
 *     public Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
 *         IAgent agent = scope.getAgent();
 *         
 *         // Custom execution logic
 *         // 1. Pre-processing
 *         // 2. Execute behaviors based on custom rules
 *         // 3. Post-processing
 *         
 *         return null;
 *     }
 *     
 *     {@literal @}Override
 *     public boolean init(IScope scope) {
 *         // Initialize architecture for agent
 *         return true;
 *     }
 *     
 *     {@literal @}Override
 *     public boolean abort(IScope scope) {
 *         // Clean up when agent dies
 *         return true;
 *     }
 *     
 *     {@literal @}Override
 *     public void verifyBehaviors(ISpecies context) {
 *         // Validate that behaviors are compatible
 *         // Throw GamaRuntimeException if invalid
 *     }
 *     
 *     {@literal @}Override
 *     public void preStep(IScope scope, IPopulation&lt;?&gt; population) {
 *         // Population-level pre-processing before stepping agents
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Architecture Lifecycle</h3>
 * <ol>
 * <li><b>Compilation:</b> verifyBehaviors() validates behaviors against architecture</li>
 * <li><b>Agent Creation:</b> Architecture instance created for each agent</li>
 * <li><b>Initialization:</b> init() called when agent is created</li>
 * <li><b>Pre-Step:</b> preStep() called before population step (once per cycle)</li>
 * <li><b>Execution:</b> privateExecuteIn() called for each agent each cycle</li>
 * <li><b>Termination:</b> abort() called when agent dies</li>
 * </ol>
 * 
 * <h3>Design Patterns</h3>
 * <ul>
 * <li><b>Strategy Pattern:</b> Different architectures = different behavior execution strategies</li>
 * <li><b>Template Method:</b> AbstractArchitecture provides template, subclasses customize</li>
 * <li><b>Command Pattern:</b> Behaviors encapsulated as executable commands</li>
 * </ul>
 * 
 * <h3>Implementation Notes</h3>
 * <ul>
 * <li>Implements both ISkill (capabilities) and IStatement (executable)</li>
 * <li>Each agent has its own architecture instance</li>
 * <li>Architecture determines when and how reflexes/tasks/states execute</li>
 * <li>Order can be customized (default returns 0)</li>
 * <li>preStep allows population-wide setup before individual agent execution</li>
 * </ul>
 * 
 * @see ISkill
 * @see AbstractArchitecture
 * @see IAgent
 * @see IStatement
 * @author drogoul
 * @since GAMA 1.0
 */
public interface IArchitecture extends ISkill, IStatement {

	/**
	 * Inits the.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	boolean init(IScope scope) throws GamaRuntimeException;

	/**
	 * Abort.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	boolean abort(IScope scope) throws GamaRuntimeException;

	/**
	 * Verify behaviors.
	 *
	 * @param context
	 *            the context
	 */
	void verifyBehaviors(ISpecies context);

	/**
	 * Pre step.
	 *
	 * @param scope
	 *            the scope
	 * @param gamaPopulation
	 *            the gama population
	 */
	void preStep(final IScope scope, IPopulation<? extends IAgent> gamaPopulation);

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	@Override
	default int getOrder() { return 0; }

	/**
	 * Sets the order.
	 *
	 * @param o
	 *            the new order
	 */
	@Override
	default void setOrder(final int o) {}
}