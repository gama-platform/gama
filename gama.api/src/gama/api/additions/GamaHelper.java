/*******************************************************************************************************
 *
 * GamaHelper.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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

/**
 * A general-purpose helper record that implements IGamaHelper and can be used to execute actions or operations within
 * the GAMA framework. This class acts as a wrapper around a delegate helper, providing a named and
 * skill-class-associated execution context. It can be subclassed and used similarly to a Runnable to define custom
 * behaviors for agents and skills.
 *
 * <p>
 * The helper pattern allows for flexible delegation of execution logic while maintaining associations with specific
 * skill classes and named operations.
 * </p>
 *
 * @param <T>
 *            the return type of the helper's run method
 * @author drogoul
 * @since 14 août 2010
 * @see IGamaHelper
 */
@SuppressWarnings ({ "rawtypes" })
public record GamaHelper<T>(String name, Class skillClass, IGamaHelper<T> delegate) implements IGamaHelper<T> {

	/**
	 * The name of this helper, used to identify the specific operation or action being performed.
	 */

	/**
	 * The skill class associated with this helper. Represents the class that provides the context for executing the
	 * helper's operations. This is typically the skill class that defines the action or behavior being executed.
	 */

	/**
	 * The delegate helper that performs the actual execution logic. This helper is invoked by the run method to
	 * delegate the execution of operations. If null, the run method returns null without performing any action.
	 */

	/**
	 * Instantiates a new GamaHelper with a skill class and delegate, but without a specific name. This constructor is
	 * useful when the helper does not need a named identifier.
	 *
	 * @param clazz
	 *            the skill class that provides the context for this helper
	 * @param delegate
	 *            the delegate helper that will perform the actual execution logic
	 */
	public GamaHelper(final Class clazz, final IGamaHelper<T> delegate) {
		this(null, clazz, delegate);
	}

	/**
	 * Gets the skill class associated with this helper. The skill class represents the class that provides the context
	 * for executing the helper's operations.
	 *
	 * @return the skill class associated with this helper
	 */
	@Override
	public Class getSkillClass() { return skillClass; }

	/**
	 * Gets the name of this helper, which identifies the specific operation or action being performed.
	 *
	 * @return the name of this helper, or null if no name was specified
	 */
	@Override
	public String getName() { return name; }

	/**
	 * Executes the helper's operation by delegating to the underlying delegate helper. If no delegate is present
	 * (null), this method returns null without performing any action. Otherwise, it invokes the delegate's run method
	 * with the provided parameters.
	 *
	 * @param scope
	 *            the execution scope providing the context for running the operation
	 * @param agent
	 *            the agent on which the operation is being performed
	 * @param skill
	 *            the skill or variable/action support object providing additional context
	 * @param values
	 *            the input values or arguments for the operation
	 * @return the result of the delegate's execution, or null if no delegate is present
	 * @throws GamaRuntimeException
	 *             if an error occurs during the execution of the delegate's run method
	 */
	@Override
	public T run(final IScope scope, final IAgent agent, final IVarAndActionSupport skill, final Object values)
			throws GamaRuntimeException {
		if (delegate == null) return null;
		return delegate.run(scope, agent, skill, values);
	}

}