/*******************************************************************************************************
 *
 * RemoteSequence.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.runtime.scope.FlowStatus;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.Invocation;

/**
 * The Class RemoteSequence.
 */
public class RemoteSequence extends AbstractStatementSequence implements IStatement.Remote {

	/**
	 * Instantiates a new remote sequence.
	 *
	 * @param desc
	 *            the desc
	 */
	public RemoteSequence(final IDescription desc) {
		super(desc);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		// The calling agent is handed by the scope, as this sequence is shared by all the simulations and threads
		final Invocation invocation = scope.takeInvocation(this);
		scope.addVarWithValue(IKeyword.MYSELF, invocation == null ? null : invocation.myself());
		Object lastResult = null;
		for (final IStatement command : commands) {
			final IExecutionResult result = scope.execute(command);
			if (!result.passed()) return lastResult;
			FlowStatus fs = scope.getAndClearContinueStatus();
			if (scope.interrupted() || fs == FlowStatus.BREAK) return lastResult;
			if (fs == FlowStatus.CONTINUE) { continue; }
			lastResult = result.getValue();
		}
		return lastResult;
	}
}