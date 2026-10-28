/*******************************************************************************************************
 *
 * AbstractStatementSequenceWithArgs.java, in gama.api, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Arguments;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.Invocation;

/**
 * Abstract base class for statement sequences that accept arguments.
 * 
 * <p>
 * This class extends {@link AbstractStatementSequence} to add argument handling capabilities. It is used for actions
 * and other callable sequences that can receive parameters when invoked.
 * </p>
 * 
 * <h2>Argument Management</h2>
 * <p>
 * The runtime arguments of a call are not stored in the statement (shared by all the simulations of a model and by
 * all the threads executing it) but handed to it by the scope performing the call (see
 * {@link gama.api.runtime.scope.Invocation}). They are pushed onto the scope stack before executing the sequence.
 * </p>
 * 
 * <h2>Example Usage</h2>
 * <p>
 * In GAML, this is used for action definitions:
 * </p>
 * <pre>
 * {@code
 * action eat (prey target, int amount) {
 *     // target and amount are arguments
 *     energy <- energy + amount;
 *     ask target {
 *         do die;
 *     }
 * }
 * }
 * </pre>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.4
 * @see AbstractStatementSequence
 * @see IStatement.WithArgs
 * @see Arguments
 */
public class AbstractStatementSequenceWithArgs extends AbstractStatementSequence implements IStatement.WithArgs {

	/**
	 * Constructs a new statement sequence with argument support.
	 *
	 * @param desc
	 *            the statement description
	 */
	public AbstractStatementSequenceWithArgs(final IDescription desc) {
		super(desc);
	}

	/**
	 * Sets the formal argument declarations.
	 * 
	 * <p>
	 * This default implementation does nothing. Subclasses like {@link ActionStatement} override this to store formal
	 * arguments.
	 * </p>
	 *
	 * @param args
	 *            the formal arguments
	 */
	@Override
	public void setFormalArgs(final Arguments args) {}

	/**
	 * Executes the sequence with arguments pushed onto the scope stack.
	 * 
	 * <p>
	 * Before executing child statements, this method makes the runtime arguments available in the scope so they can be
	 * accessed as local variables.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the result of execution
	 * @throws GamaRuntimeException
	 *             if execution fails
	 */
	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final Invocation invocation = scope.takeInvocation(this);
		final Arguments args = actualArgs(invocation == null ? null : invocation.args());
		scope.stackArguments(args, invocation == null ? null : invocation.caller());
		return super.privateExecuteIn(scope);
	}

	/**
	 * Returns the arguments to push onto the scope for one call, given the ones passed by the caller. The runtime
	 * arguments are handed by the scope for each call (see {@link IScope#takeInvocation(gama.api.runtime.IExecutable)})
	 * and never stored in the statement, which can be executed concurrently by several simulations or threads.
	 *
	 * @param args
	 *            the arguments passed by the caller (may be null)
	 * @return the arguments to push (may be null). Must not modify the arguments passed, which may be shared.
	 */
	protected Arguments actualArgs(final Arguments args) {
		return args;
	}

}
