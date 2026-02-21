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
 * The class maintains runtime arguments in a thread-local variable to support concurrent execution of the same action
 * by multiple agents. Arguments are automatically pushed onto the scope stack before executing the sequence.
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

	/** Thread-local storage for runtime arguments to support concurrent execution. */
	protected final ThreadLocal<Arguments> actualArgs = new ThreadLocal<>();

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
	 * Sets the runtime argument values for execution.
	 * 
	 * <p>
	 * The arguments are copied to ensure thread safety when the same action is executed concurrently by multiple
	 * agents.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param args
	 *            the runtime argument values
	 */
	@Override
	public void setRuntimeArgs(final IScope scope, final Arguments args) {
		// TODO Verify that this copy of the arguments is required or not.
		actualArgs.set(new Arguments(args));
	}

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
		scope.stackArguments(actualArgs.get());
		return super.privateExecuteIn(scope);
	}

	@Override
	public void dispose() {
		Arguments args = actualArgs.get();
		if (args != null) { args.dispose(); }
		actualArgs.set(null);
		super.dispose();
	}

}
