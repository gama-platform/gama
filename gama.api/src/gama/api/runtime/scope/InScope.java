/*******************************************************************************************************
 *
 * InScope.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

/**
 * Functional interface for operations that execute within a scope context.
 * 
 * <p>
 * InScope provides a functional programming pattern for encapsulating operations that require a scope to execute. This
 * is particularly useful for passing scope-dependent operations as parameters or for deferred execution.
 * </p>
 * 
 * <p>
 * The interface is generic, allowing operations to return values of any type. For operations that don't need to return
 * a value, the {@link Void} inner class provides a convenient abstraction.
 * </p>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Usage with Return Value</h3>
 * <pre>{@code
 * InScope<Double> calculation = scope -> {
 *     IAgent agent = scope.getAgent();
 *     double x = (Double) agent.getAttribute("x");
 *     double y = (Double) agent.getAttribute("y");
 *     return Math.sqrt(x * x + y * y);
 * };
 * 
 * // Execute the operation
 * double result = calculation.run(scope);
 * }</pre>
 * 
 * <h3>Using InScope.Void for Side-Effect Operations</h3>
 * <pre>{@code
 * InScope.Void initialization = new InScope.Void() {
 *     @Override
 *     public void process(IScope scope) {
 *         scope.setVarValue("initialized", true);
 *         scope.setVarValue("counter", 0);
 *         scope.getGui().inform("Initialization complete");
 *     }
 * };
 * 
 * // Execute the operation
 * initialization.run(scope);
 * }</pre>
 * 
 * <h3>Passing as Parameter</h3>
 * <pre>{@code
 * public void executeInScope(IScope scope, InScope<Object> operation) {
 *     try {
 *         Object result = operation.run(scope);
 *         // Process result...
 *     } catch (Exception e) {
 *         scope.getGui().error("Operation failed: " + e.getMessage());
 *     }
 * }
 * 
 * // Use it
 * executeInScope(scope, s -> s.getAgent().getName());
 * }</pre>
 * 
 * <h3>With Lambda Expressions</h3>
 * <pre>{@code
 * // Simple lambda
 * InScope<String> getAgentName = scope -> scope.getAgent().getName();
 * 
 * // More complex lambda
 * InScope<List<IAgent>> getNearbyAgents = scope -> {
 *     IAgent agent = scope.getAgent();
 *     ITopology topology = scope.getTopology();
 *     return topology.getNeighborsOf(agent, 10.0, scope);
 * };
 * }</pre>
 * 
 * @param <T>
 *            the return type of the operation
 * 
 * @see IScope
 */
@FunctionalInterface
public interface InScope<T> {

	/**
	 * Abstract base class for void operations that don't return a value.
	 * 
	 * <p>
	 * This class implements InScope&lt;Object&gt; and provides a more intuitive API for operations that perform
	 * side-effects but don't need to return a meaningful value. The {@link #run(IScope)} method always returns null.
	 * </p>
	 * 
	 * <h3>Usage Example</h3>
	 * <pre>{@code
	 * InScope.Void updateAgent = new InScope.Void() {
	 *     @Override
	 *     public void process(IScope scope) {
	 *         IAgent agent = scope.getAgent();
	 *         agent.setAttribute("lastUpdate", scope.getClock().getCycle());
	 *         agent.setAttribute("health", 100);
	 *     }
	 * };
	 * 
	 * updateAgent.run(scope);
	 * }</pre>
	 * 
	 * <p>
	 * Alternatively, you can use lambda expressions with InScope.Void:
	 * </p>
	 * 
	 * <pre>{@code
	 * InScope.Void logMessage = scope -> {
	 *     scope.getGui().inform("Processing agent: " + scope.getAgent().getName());
	 *     return null;
	 * };
	 * }</pre>
	 */
	public abstract static class Void implements InScope<Object> {

		/**
		 * Executes the operation within the given scope and returns null.
		 * 
		 * <p>
		 * This method calls {@link #process(IScope)} to perform the actual operation and always returns null.
		 * </p>
		 * 
		 * @param scope
		 *            the scope in which to execute the operation
		 * @return always returns null
		 */
		@Override
		public Object run(final IScope scope) {
			process(scope);
			return null;
		}

		/**
		 * Performs the operation within the given scope.
		 * 
		 * <p>
		 * Implement this method to define the operation to be performed. This method is called by {@link #run(IScope)}
		 * and should perform side-effects such as modifying variables, updating agents, or triggering actions.
		 * </p>
		 * 
		 * @param scope
		 *            the scope in which to execute the operation
		 */
		public abstract void process(IScope scope);
	}

	/**
	 * Executes the operation within the given scope.
	 * 
	 * <p>
	 * This is the single abstract method of the functional interface. Implementations should perform their operation
	 * using the provided scope and return the result.
	 * </p>
	 * 
	 * <p>
	 * The scope provides access to:
	 * </p>
	 * <ul>
	 * <li>Current agent and agent stack</li>
	 * <li>Variables (local, global, agent attributes)</li>
	 * <li>Execution services (random generator, clock, GUI)</li>
	 * <li>Simulation and experiment agents</li>
	 * <li>Topology and spatial operations</li>
	 * </ul>
	 * 
	 * @param scope
	 *            the scope in which to execute the operation
	 * @return the result of the operation, or null if no result is needed
	 */
	T run(IScope scope);
}