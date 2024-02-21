/*******************************************************************************************************
 *
 * RemoteSequence.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.ExecutionResult;
import gama.core.runtime.FlowStatus;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;

/**
 * The Class RemoteSequence.
 */
public class RemoteSequence extends AbstractStatementSequence {

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

	// @Override
	// public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
	// scope.addVarWithValue(IKeyword.MYSELF, myself.get());
	// return super.privateExecuteIn(scope);
	// }

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		scope.addVarWithValue(IKeyword.MYSELF, myself.get());
		Object lastResult = null;
		for (final IStatement command : commands) {
			final ExecutionResult result = scope.execute(command);
			if (!result.passed()) return lastResult;
			FlowStatus fs = scope.getAndClearContinueStatus();
			if (scope.interrupted() || fs == FlowStatus.BREAK) // scope.setFlowStatus(IScope.FlowStatus.BREAK); // we
																// set it again for
				// the outer statement
				return lastResult;
			if (fs == FlowStatus.CONTINUE) { continue; }
			lastResult = result.getValue();
		}
		return lastResult;
	}
}