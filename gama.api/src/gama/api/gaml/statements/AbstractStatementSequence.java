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
 * The Class AbstractStatementSequence.
 */
public class AbstractStatementSequence extends AbstractStatement {

	/** The commands. */
	protected IStatement[] commands;

	/** The is top level. */
	final boolean isTopLevel;

	/**
	 * Instantiates a new abstract statement sequence.
	 *
	 * @param desc
	 *            the desc
	 */
	public AbstractStatementSequence(final IDescription desc) {
		super(desc);
		isTopLevel = desc != null && desc.getMeta().isTopLevel();
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		this.commands = FluentIterable.from(commands).filter(IStatement.class).toArray(IStatement.class);
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	@Override
	public boolean isEmpty() { return commands.length == 0; }

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		enterScope(scope);
		try {
			return super.executeOn(scope);
		} finally {
			leaveScope(scope);
		}
	}

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
	 * Leave scope.
	 *
	 * @param scope
	 *            the scope
	 */
	public void leaveScope(final IScope scope) {
		// Clears any action_halted status in case we are a top-level behavior
		// (reflex, init, state, etc.)
		if (isTopLevel) { scope.getAndClearReturnStatus(); }
		scope.pop(this);
	}

	/**
	 * Enter scope.
	 *
	 * @param scope
	 *            the scope
	 */
	public void enterScope(final IScope scope) {
		scope.push(this);
	}

	/**
	 * Gets the commands.
	 *
	 * @return the commands
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