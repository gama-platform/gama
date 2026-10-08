/*******************************************************************************************************
 *
 * IExecutable.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Arguments;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * Represents executable elements in GAMA simulations, such as statements, actions, or expressions that can be executed
 * within a scope.
 * 
 * <p>
 * IExecutable is a fundamental interface in GAMA's execution model. It defines the contract for objects that can be
 * executed by the simulation engine, including GAML statements, primitive actions, behaviors, and various simulation
 * constructs.
 * </p>
 * 
 * <p>
 * Key responsibilities:
 * </p>
 * <ul>
 * <li>Execute logic within a specific simulation scope</li>
 * <li>Accept runtime arguments for parameterized execution</li>
 * <li>Maintain context about the calling agent ("myself")</li>
 * <li>Return execution results that can be used by other statements</li>
 * </ul>
 * 
 * <p>
 * Implementations include:
 * </p>
 * <ul>
 * <li>GAML statements (if, loop, create, ask, etc.)</li>
 * <li>GAML actions (user-defined and primitive)</li>
 * <li>Species behaviors (reflexes, init blocks)</li>
 * <li>Expressions that produce side effects</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * IExecutable action = ...; // Some GAML action or statement
 * IScope scope = ...; // Current execution scope
 * 
 * // Set up execution context
 * action.setMyself(callingAgent);
 * action.setRuntimeArgs(scope, arguments);
 * 
 * // Execute and get result
 * Object result = action.executeOn(scope);
 * </pre>
 * 
 * @see IScope
 * @see ParallelAgentExecuter
 * @see GamaExecutorService
 * 
 * @author drogoul
 * @since 20 août 2013
 */
public interface IExecutable {

	/**
	 * Executes this executable object within the provided scope.
	 * 
	 * <p>
	 * This is the main execution method that performs the logic defined by this executable. The scope provides access
	 * to the current agent, variables, random generators, and other execution context. The returned value depends on
	 * the type of executable:
	 * </p>
	 * <ul>
	 * <li>Statements typically return null or a status indicator</li>
	 * <li>Actions return their computed result</li>
	 * <li>Expressions return their evaluated value</li>
	 * </ul>
	 * 
	 * @param scope
	 *            the execution scope providing context for execution, including the current agent, variables, and
	 *            runtime services
	 * @return the result of execution, which may be null for statements without return values
	 * @throws GamaRuntimeException
	 *             if an error occurs during execution that should be reported to the user
	 */
	Object executeOn(final IScope scope) throws GamaRuntimeException;

	/**
	 * Sets the runtime arguments for this executable before execution.
	 * 
	 * <p>
	 * This method is called to provide argument values when executing actions or parameterized statements. The default
	 * implementation does nothing, as many executables don't accept runtime arguments. Implementations that do accept
	 * arguments should override this method to store them for use during {@link #executeOn(IScope)}.
	 * </p>
	 * 
	 * @param executionScope
	 *            the scope in which arguments should be evaluated
	 * @param args
	 *            the arguments to pass to this executable (may be null if no arguments)
	 */
	default void setRuntimeArgs(final IScope executionScope, final Arguments args) {
		// Do nothing
	}

	/**
	 * Sets the calling agent ("myself") for this execution.
	 * 
	 * <p>
	 * In GAML, "myself" refers to the agent that called the current action or statement. This method allows the
	 * execution framework to establish that context before execution. The default implementation does nothing, as not
	 * all executables need to track the caller. Implementations that use "myself" should override this method.
	 * </p>
	 * 
	 * @param caller
	 *            the agent calling this executable, which becomes the "myself" pseudo-variable
	 */
	default void setMyself(final IAgent caller) {
		// Do nothing
	}

}
