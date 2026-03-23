/*******************************************************************************************************
 *
 * IGamaHelper.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;

import gama.api.compilation.IVarAndActionSupport;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.interfaces.INamed;

/**
 * A functional interface for executing operations or actions within the GAMA framework. This interface defines a
 * general-purpose helper mechanism similar to Runnable but adapted for GAMA's execution context.
 *
 * <p>
 * As a functional interface, IGamaHelper can be implemented using lambda expressions or method references, making it
 * ideal for defining action execution logic, skill behaviors, and other agent operations.
 * </p>
 *
 * <h2>Purpose</h2>
 *
 * <p>
 * This interface is commonly used for:
 * </p>
 * <ul>
 * <li><strong>Action Execution:</strong> Implementing agent actions and behaviors</li>
 * <li><strong>Skill Operations:</strong> Defining skill-specific operations</li>
 * <li><strong>Variable Setters:</strong> Setting agent or species variables</li>
 * <li><strong>Custom Operations:</strong> Executing any context-dependent operation within GAMA</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <pre>{@code
 * // Simple helper that prints a message
 * IGamaHelper<Object> printHelper = new IGamaHelper<>() {
 * 	public Object run(IScope scope, IAgent agent, IVarAndActionSupport skill, Object values) {
 * 		System.out.println("Action executed for " + agent.getName());
 * 		return null;
 * 	}
 *
 * 	public String getName() { return "print_action"; }
 * };
 *
 * // Lambda-based helper
 * IGamaHelper<Double> calculator = (scope, agent, skill, values) -> { return Math.random() * 100.0; };
 * }</pre>
 *
 * @param <T>
 *            the type of value returned by this helper's execution
 *
 * @author drogoul
 * @since 14 août 2010
 * @version 2025-03
 *
 * @see gama.api.compilation.IVarAndActionSupport
 * @see gama.api.kernel.agent.IAgent
 * @see gama.api.runtime.scope.IScope
 */
@SuppressWarnings ({ "rawtypes" })
@FunctionalInterface
public interface IGamaHelper<T> extends INamed {

	/**
	 * An empty array of objects used as a default when no values are provided to the run method. This constant avoids
	 * creating new empty arrays repeatedly, improving performance.
	 */
	Object[] EMPTY_VALUES = {};

	/**
	 * Gets the skill class associated with this helper. The skill class represents the Java class that provides the
	 * context for executing this helper's operations. This is typically used to identify which skill or class defines
	 * the action or behavior.
	 *
	 * <p>
	 * The default implementation returns null, indicating no specific skill class association.
	 * </p>
	 *
	 * @return the skill class associated with this helper, or null if not associated with a specific skill
	 */
	default Class getSkillClass() { return null; }

	/**
	 * Executes the helper's operation without explicit values, using an empty values array by default. This convenience
	 * method delegates to the full run method with EMPTY_VALUES as the values parameter.
	 *
	 * <p>
	 * Use this method when the operation does not require input values or arguments.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope providing the context for running the operation
	 * @param agent
	 *            the agent on which the operation is being performed
	 * @param skill
	 *            the skill or variable/action support object providing additional context
	 * @return the result of the operation, or null if no value is produced
	 * @throws GamaRuntimeException
	 *             if an error occurs during execution
	 */
	default T run(final IScope scope, final IAgent agent, final IVarAndActionSupport skill)
			throws GamaRuntimeException {
		return run(scope, agent, skill, EMPTY_VALUES);
	}

	/**
	 * Executes the helper's operation with the provided scope, agent, skill context, and input values. This is the
	 * primary execution method that performs the actual operation defined by this helper.
	 *
	 * <p>
	 * Implementations should use the scope to access runtime context, the agent to perform operations on the target
	 * agent, the skill to access skill-specific capabilities, and the values parameter to receive input arguments for
	 * the operation.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope providing the context for running the operation
	 * @param agent
	 *            the agent on which the operation is being performed
	 * @param skill
	 *            the skill or variable/action support object providing additional context
	 * @param values
	 *            the input values or arguments for the operation. Can be a single value, an array, or any other object
	 *            depending on the operation's requirements
	 * @return the result of the operation, which can be any object of type T or null
	 * @throws GamaRuntimeException
	 *             if an error occurs during the execution of the operation
	 */
	T run(final IScope scope, final IAgent agent, final IVarAndActionSupport skill, final Object values)
			throws GamaRuntimeException;

}