/*******************************************************************************************************
 *
 * AbstractStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.Symbol;
import gama.api.runtime.scope.IScope;

/**
 * Abstract base implementation for all GAML statements.
 * 
 * <p>
 * This class provides the core functionality for statement execution, including scope management, error handling, and
 * symbol tracking. All concrete statement types extend this class to inherit basic statement behavior.
 * </p>
 * 
 * <h2>Execution Model</h2>
 * <p>
 * Statement execution follows this pattern:
 * </p>
 * <ol>
 * <li>Set the current symbol in the scope for debugging/error tracking</li>
 * <li>Execute the statement's private logic via {@link #privateExecuteIn(IScope)}</li>
 * <li>Catch and augment any runtime exceptions with context information</li>
 * <li>Clear the current symbol from the scope</li>
 * </ol>
 * 
 * <h2>Error Handling</h2>
 * <p>
 * When exceptions occur during execution, they are automatically enhanced with:
 * </p>
 * <ul>
 * <li>The statement that caused the error</li>
 * <li>The source code location</li>
 * <li>The current execution context</li>
 * </ul>
 * 
 * <h2>Subclassing</h2>
 * <p>
 * To create a new statement type, extend this class and implement {@link #privateExecuteIn(IScope)} with the
 * statement's specific logic.
 * </p>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see IStatement
 * @see Symbol
 */

public abstract class AbstractStatement extends Symbol implements IStatement {

	/**
	 * Constructs a new statement from its description.
	 * 
	 * <p>
	 * The statement's display name is constructed from its keyword and name facet, providing useful information for
	 * debugging and error messages.
	 * </p>
	 *
	 * @param desc
	 *            the statement description
	 */
	public AbstractStatement(final IDescription desc) {
		super(desc);
		if (desc != null) {
			final String k = getKeyword();
			final String n = desc.getName();
			setName(k == null ? "" : k + " " + (n == null ? "" : n));
		}
	}

	/**
	 * Executes this statement in the given scope with full error handling.
	 * 
	 * <p>
	 * This method manages the execution context by:
	 * </p>
	 * <ul>
	 * <li>Registering this statement as the currently executing symbol</li>
	 * <li>Delegating to {@link #privateExecuteIn(IScope)} for actual execution</li>
	 * <li>Catching exceptions and adding contextual information</li>
	 * <li>Cleaning up the execution context</li>
	 * </ul>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the result of statement execution, or null
	 * @throws GamaRuntimeException
	 *             if execution fails
	 */
	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		try {
			scope.setCurrentSymbol(this);
			return privateExecuteIn(scope);
		} catch (final GamaRuntimeException e) {
			e.addContext(this);
			GAMA.reportAndThrowIfNeeded(scope, e, true);
			return null;
		} finally {
			scope.setCurrentSymbol(null);
		}
	}

	/**
	 * Performs the actual execution logic of this statement.
	 * 
	 * <p>
	 * This method must be implemented by subclasses to define the specific behavior of each statement type. It is
	 * called by {@link #executeOn(IScope)} within a protected execution context.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope providing access to agents, variables, and simulation state
	 * @return the result value of the statement execution, or null if no value is produced
	 * @throws GamaRuntimeException
	 *             if execution fails
	 */
	protected abstract Object privateExecuteIn(IScope scope) throws GamaRuntimeException;

	/**
	 * Sets the child statements for compound statements.
	 * 
	 * <p>
	 * Most simple statements have no children. This method can be overridden by compound statements (sequences, loops,
	 * etc.) that contain other statements.
	 * </p>
	 *
	 * @param commands
	 *            the child statements
	 */
	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {}

	@Override
	public String toString() {
		return description.serializeToGaml(true);
	}

	@Override
	public IStatementDescription getDescription() { return (IStatementDescription) super.getDescription(); }

}
