/*******************************************************************************************************
 *
 * ParallelAgentExecuter.java, in gama.core, is part of the source code of the
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
 * The Class ParallelAgentExecuter.
 */
public class ParallelAgentExecuter extends ParallelAgentRunner<Object> {

	/** The executable. */
	final IExecutable executable;

	/**
	 * Instantiates a new parallel agent executer.
	 *
	 * @param scope the scope
	 * @param executable the executable
	 * @param agents the agents
	 */
	public ParallelAgentExecuter(final IScope scope, final IExecutable executable, final Spliterator<IAgent> agents) {
		super(scope, agents);
		this.executable = executable;
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		final AtomicBoolean result = new AtomicBoolean(true);
		agents.forEachRemaining(each -> {
			if (result.get()) {
				result.set(scope.execute(executable, each, null).passed());
			}
		});
		return result.get();
	}

	@Override
	ParallelAgentExecuter subTask(final Spliterator<IAgent> sub) {
		return new ParallelAgentExecuter(originalScope, executable, sub);
	}

}