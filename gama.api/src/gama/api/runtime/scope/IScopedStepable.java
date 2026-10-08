/*******************************************************************************************************
 *
 * IScopedStepable.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

import gama.api.runtime.IStepable;

/**
 * Interface for GAMA stepable objects that generate and manage their own scope.
 * 
 * <p>
 * IScopedStepable combines {@link IScoped} and {@link IStepable}, representing objects that can be stepped (executed
 * over time) and maintain their own scope for execution. This is particularly useful for agents and other entities
 * that need to execute independently with their own execution context.
 * </p>
 * 
 * <p>
 * This interface provides default implementations of {@link #step()} and {@link #init()} that delegate to the object's
 * scope, simplifying the implementation of stepable objects that manage their own scope.
 * </p>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li>Automatic scope management for stepping and initialization</li>
 * <li>Default implementations that use the object's own scope</li>
 * <li>Integration with GAMA's execution and scheduling framework</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Implementing IScopedStepable</h3>
 * <pre>{@code
 * public class CustomAgent implements IScopedStepable {
 *     private final IScope scope;
 *     private boolean initialized = false;
 *     
 *     public CustomAgent(IScope parentScope) {
 *         // Create agent's own scope
 *         this.scope = parentScope.copy("CustomAgent");
 *     }
 *     
 *     @Override
 *     public IScope getScope() {
 *         return scope;
 *     }
 *     
 *     @Override
 *     public boolean step(IScope scope) {
 *         // Called each simulation step
 *         if (!initialized) {
 *             return false;
 *         }
 *         
 *         // Perform agent's step behavior
 *         updateState();
 *         performActions();
 *         
 *         return true; // Indicates successful step
 *     }
 *     
 *     @Override
 *     public boolean init(IScope scope) {
 *         // Called during initialization
 *         initializeVariables();
 *         setupBehavior();
 *         initialized = true;
 *         return true; // Indicates successful initialization
 *     }
 *     
 *     private void updateState() {
 *         // Update agent state using scope services
 *         double time = scope.getClock().getTime();
 *         // ...
 *     }
 *     
 *     private void performActions() {
 *         // Perform actions using scope
 *         // ...
 *     }
 *     
 *     private void initializeVariables() {
 *         scope.setVarValue("energy", 100.0);
 *         scope.setVarValue("position", new GamaPoint(0, 0));
 *     }
 *     
 *     private void setupBehavior() {
 *         // Setup initial behavior
 *         // ...
 *     }
 * }
 * }</pre>
 * 
 * <h3>Using Default Methods</h3>
 * <pre>{@code
 * public class SimpleStepable implements IScopedStepable {
 *     private final IScope scope;
 *     
 *     public SimpleStepable(IScope scope) {
 *         this.scope = scope;
 *     }
 *     
 *     @Override
 *     public IScope getScope() {
 *         return scope;
 *     }
 *     
 *     @Override
 *     public boolean step(IScope scope) {
 *         // Automatically called by default step() method
 *         System.out.println("Step executed");
 *         return true;
 *     }
 *     
 *     @Override
 *     public boolean init(IScope scope) {
 *         // Automatically called by default init() method
 *         System.out.println("Initialized");
 *         return true;
 *     }
 * }
 * 
 * // Usage
 * IScopedStepable stepable = new SimpleStepable(scope);
 * stepable.init();  // Uses default method, delegates to scope.init(this)
 * stepable.step();  // Uses default method, delegates to scope.step(this)
 * }</pre>
 * 
 * <h3>Integration with Scheduler</h3>
 * <pre>{@code
 * // Adding to scheduler
 * IScopedStepable agent = new CustomAgent(scope);
 * 
 * // Initialize
 * if (agent.init()) {
 *     // Add to simulation scheduler
 *     simulation.getScheduler().schedule(agent);
 * }
 * 
 * // The scheduler will call step() each cycle
 * // agent.step() is called automatically by scheduler
 * }</pre>
 * 
 * <h2>Method Behavior</h2>
 * 
 * <p>
 * The default implementations:
 * </p>
 * <ul>
 * <li>{@link #step()} - Calls {@code getScope().step(this).passed()}</li>
 * <li>{@link #init()} - Calls {@code getScope().init(this).passed()}</li>
 * </ul>
 * 
 * <p>
 * These methods can be overridden if custom behavior is needed, but the default implementations are suitable for most
 * use cases where the scope properly handles stepping and initialization.
 * </p>
 * 
 * @see IScoped
 * @see IStepable
 * @see IScope#step(IStepable)
 * @see IScope#init(IStepable)
 * 
 * @author A. Drogoul
 */
public interface IScopedStepable extends IScoped, IStepable {

	/**
	 * Executes one step of this stepable object.
	 * 
	 * <p>
	 * This default implementation delegates to the object's scope by calling {@code getScope().step(this).passed()}.
	 * The scope manages the execution context, including pushing/popping the agent context, handling interruptions, and
	 * benchmarking.
	 * </p>
	 * 
	 * <p>
	 * Override this method if you need custom stepping behavior that bypasses the scope's standard step management.
	 * </p>
	 * 
	 * @return true if the step executed successfully, false if it failed or was interrupted
	 */
	default boolean step() {
		return getScope().step(this).passed();
	}

	/**
	 * Initializes this stepable object.
	 * 
	 * <p>
	 * This default implementation delegates to the object's scope by calling {@code getScope().init(this).passed()}.
	 * The scope manages the initialization context, including handling interruptions and errors.
	 * </p>
	 * 
	 * <p>
	 * Override this method if you need custom initialization behavior that bypasses the scope's standard initialization
	 * management.
	 * </p>
	 * 
	 * @return true if initialization was successful, false if it failed or was interrupted
	 */
	default boolean init() {
		return getScope().init(this).passed();
	}

}
