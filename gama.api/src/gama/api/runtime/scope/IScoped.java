/*******************************************************************************************************
 *
 * IScoped.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

/**
 * Marker interface for objects that have an associated scope.
 * 
 * <p>
 * IScoped indicates that an object maintains a reference to a scope and can provide access to it. This is commonly
 * implemented by agents, statements, expressions, and other GAMA entities that need a scope for execution.
 * </p>
 * 
 * <p>
 * Having access to a scope allows objects to:
 * </p>
 * <ul>
 * <li>Access the current agent and agent hierarchy</li>
 * <li>Read and modify variables</li>
 * <li>Execute statements and evaluate expressions</li>
 * <li>Access runtime services (random, clock, GUI, etc.)</li>
 * <li>Manage execution flow and error handling</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Implementing IScoped in a Custom Class</h3>
 * <pre>{@code
 * public class CustomProcessor implements IScoped {
 *     private final IScope scope;
 *     
 *     public CustomProcessor(IScope scope) {
 *         this.scope = scope;
 *     }
 *     
 *     @Override
 *     public IScope getScope() {
 *         return scope;
 *     }
 *     
 *     public void process() {
 *         // Use scope to access runtime services
 *         IAgent agent = scope.getAgent();
 *         double random = scope.getRandom().next();
 *         // ... processing logic
 *     }
 * }
 * }</pre>
 * 
 * <h3>Using IScoped Objects</h3>
 * <pre>{@code
 * public void executeScoped(IScoped scoped) {
 *     IScope scope = scoped.getScope();
 *     
 *     // Access scope services
 *     IAgent agent = scope.getAgent();
 *     ISimulationAgent simulation = scope.getSimulation();
 *     
 *     // Perform operations in the object's scope context
 *     Object result = scope.getVarValue("myVariable");
 * }
 * }</pre>
 * 
 * <h3>Common Implementations</h3>
 * <p>
 * Many GAMA core classes implement IScoped:
 * </p>
 * <ul>
 * <li>{@link gama.api.kernel.agent.IAgent} - Agents maintain their own scope</li>
 * <li>{@link gama.api.gaml.expressions.IExpression} - Expressions evaluate within a scope</li>
 * <li>{@link gama.api.gaml.statements.IStatement} - Statements execute within a scope</li>
 * <li>{@link IScopedStepable} - Stepable objects with their own scope</li>
 * </ul>
 * 
 * @see IScope
 * @see IScopedStepable
 */
public interface IScoped {

	/**
	 * Returns the scope associated with this object.
	 * 
	 * <p>
	 * The returned scope provides the execution context for this object, including access to variables, agents, and
	 * runtime services. The scope may be:
	 * </p>
	 * <ul>
	 * <li>An agent's own scope (for IAgent implementations)</li>
	 * <li>A parent scope passed during construction</li>
	 * <li>A dynamically created scope for temporary operations</li>
	 * </ul>
	 * 
	 * @return the scope associated with this object, should not be null in normal operation
	 */
	IScope getScope();

}
