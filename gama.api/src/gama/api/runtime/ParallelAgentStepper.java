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
package gama.api.runtime;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

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
		final AtomicBoolean result = new AtomicBoolean(true);
		agents.forEachRemaining(each -> {
			if (result.get()) {
				result.set(scope.step(each).passed());
			}
		});
		return result.get();
	}

	@Override
	ParallelAgentRunner<Boolean> subTask(final Spliterator<IAgent> sub) {
		return new ParallelAgentStepper(originalScope, sub);
	}

}