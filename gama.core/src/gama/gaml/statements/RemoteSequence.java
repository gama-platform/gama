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

import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.FlowStatus;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;

/**
 * The Class RemoteSequence.
 */
public class RemoteSequence extends AbstractStatementSequence implements IStatement.Remote {

	/** The myself. */
	// AD: adding ThreadLocal for multi-threaded simulations
	final ThreadLocal<IAgent> myself = new ThreadLocal<>();

	/**
	 * Instantiates a new remote sequence.
	 *
	 * @param desc
	 *            the desc
	 */
	public RemoteSequence(final IDescription desc) {
		super(desc);
	}

	/**
	 * Gets the myself.
	 *
	 * @return the myself
	 */
	public IAgent getMyself() { return myself.get(); }

	@Override
	public void setMyself(final IAgent agent) {
		myself.set(agent);
	}

	@Override
	public void leaveScope(final IScope scope) {
		myself.set(null);
		super.leaveScope(scope);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		scope.addVarWithValue(IKeyword.MYSELF, myself.get());
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