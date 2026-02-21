/*******************************************************************************************************
 *
 * AbstractStatementSequence.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import com.google.common.collect.FluentIterable;

import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.ISymbol;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;

/**
 * Abstract base class for statements that execute a sequence of child statements.
 * 
 * <p>
 * A statement sequence represents a block of code containing multiple statements that are executed in order. This
 * class manages the execution flow through child statements and provides scope management for entering and leaving
 * code blocks.
 * </p>
 * 
 * <h2>Execution Model</h2>
 * <p>
 * The sequence executes as follows:
 * </p>
 * <ol>
 * <li>Enter a new scope (push onto scope stack)</li>
 * <li>Execute each child statement in order</li>
 * <li>Stop early if any statement requests a break (return, die, etc.)</li>
 * <li>Leave the scope (pop from scope stack)</li>
 * <li>Clear any action-halted status if this is a top-level behavior</li>
 * </ol>
 * 
 * <h2>Top-Level Behaviors</h2>
 * <p>
 * Some sequences are "top-level" behaviors (reflexes, init, state content, etc.). These sequences clear the return
 * status when exiting to prevent return statements from affecting outer scopes.
 * </p>
 * 
 * <h2>Example Usage</h2>
 * <p>
 * In GAML, sequences appear wherever curly braces are used:
 * </p>
 * <pre>
 * {@code
 * reflex move {
 *     // This entire block is an AbstractStatementSequence
 *     point target <- any_location_in(world);
 *     do goto target: target;
 *     write "Moving to " + target;
 * }
 * }
 * </pre>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see AbstractStatement
 * @see IStatement
 */
public class AbstractStatementSequence extends AbstractStatement {

	/** Array of child statements to execute in sequence. */
	protected IStatement[] commands;

	/** Indicates if this sequence is a top-level behavior (reflex, init, etc.). */
	final boolean isTopLevel;

	/**
	 * Constructs a new statement sequence.
	 *
	 * @param desc
	 *            the statement description
	 */
	public AbstractStatementSequence(final IDescription desc) {
		super(desc);
		isTopLevel = desc != null && desc.getMeta().isTopLevel();
	}

	/**
	 * Sets the child statements to execute in this sequence.
	 * 
	 * <p>
	 * Only actual {@link IStatement} children are retained; other symbols are filtered out.
	 * </p>
	 *
	 * @param commands
	 *            the symbols to filter and use as children
	 */
	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		this.commands = FluentIterable.from(commands).filter(IStatement.class).toArray(IStatement.class);
	}

	/**
	 * Checks if this sequence contains no executable statements.
	 *
	 * @return true if there are no child statements, false otherwise
	 */
	@Override
	public boolean isEmpty() { return commands.length == 0; }

	/**
	 * Executes this sequence with scope management.
	 * 
	 * <p>
	 * This override ensures the scope stack is properly managed even if exceptions occur during execution.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the result of the last executed statement
	 * @throws GamaRuntimeException
	 *             if execution fails
	 */
	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		enterScope(scope);
		try {
			return super.executeOn(scope);
		} finally {
			leaveScope(scope);
		}
	}

	/**
	 * Executes all child statements in sequence.
	 * 
	 * <p>
	 * Statements are executed one by one until either all are complete or one requests early termination (via return,
	 * die, break, etc.). The return value is the result of the last successfully executed statement.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the result of the last executed statement, or null
	 * @throws GamaRuntimeException
	 *             if any statement fails
	 */
	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		Object lastResult = null;
		for (final IStatement command : commands) {
			final IExecutionResult result = scope.execute(command);
			if (!result.passed()) return lastResult;
			lastResult = result.getValue();
		}
		return lastResult;
	}

	/**
	 * Exits this sequence's scope after execution.
	 * 
	 * <p>
	 * For top-level behaviors (reflexes, init, etc.), this method also clears any action-halted status to prevent
	 * return statements from affecting the outer scope.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 */
	public void leaveScope(final IScope scope) {
		// Clears any action_halted status in case we are a top-level behavior
		// (reflex, init, state, etc.)
		if (isTopLevel) { scope.getAndClearReturnStatus(); }
		scope.pop(this);
	}

	/**
	 * Enters this sequence's scope before execution.
	 * 
	 * <p>
	 * Pushes this statement onto the scope stack, creating a new variable scope for the sequence's execution.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 */
	public void enterScope(final IScope scope) {
		scope.push(this);
	}

	/**
	 * Gets the array of child statements in this sequence.
	 *
	 * @return the child statements
	 */
	public IStatement[] getCommands() { return commands; }

	@Override
	public void dispose() {
		if (commands != null) {
			for (IStatement statement : commands) { statement.dispose(); }
			commands = null;
		}
		super.dispose();
	}

}