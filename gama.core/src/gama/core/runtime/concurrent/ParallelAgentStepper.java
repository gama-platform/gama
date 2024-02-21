/*******************************************************************************************************
 *
 * ParallelAgentStepper.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.runtime.concurrent;

import java.util.Spliterator;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;

/**
 * The Class ParallelAgentStepper.
 */
public class ParallelAgentStepper extends ParallelAgentRunner<Boolean> {

	/**
	 * Instantiates a new parallel agent stepper.
	 *
	 * @param scope the scope
	 * @param agents the agents
	 */
	public ParallelAgentStepper(final IScope scope, final Spliterator<IAgent> agents) {
		super(scope, agents);
	}

	@Override
	public Boolean executeOn(final IScope scope) throws GamaRuntimeException {
		final Boolean[] mutableBoolean = { Boolean.TRUE };
		agents.forEachRemaining(each -> {
			if (mutableBoolean[0].booleanValue()) {
				mutableBoolean[0] = Boolean.valueOf(scope.step(each).passed());
			}
		});
		return mutableBoolean[0];
	}

	@Override
	ParallelAgentRunner<Boolean> subTask(final Spliterator<IAgent> sub) {
		return new ParallelAgentStepper(originalScope, sub);
	}

}