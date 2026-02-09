/*******************************************************************************************************
 *
 * IExecutable.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Arguments;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * Class IExecutable.
 *
 * @author drogoul
 * @since 20 août 2013
 *
 */
public interface IExecutable {

	/**
	 * Execute on.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	Object executeOn(final IScope scope) throws GamaRuntimeException;

	/**
	 * Sets the runtime args.
	 *
	 * @param executionScope
	 *            the execution scope
	 * @param args
	 *            the args
	 */
	default void setRuntimeArgs(final IScope executionScope, final Arguments args) {
		// Do nothing
	}

	/**
	 * Sets the myself.
	 *
	 * @param caller
	 *            the new myself
	 */
	default void setMyself(final IAgent caller) {
		// Do nothing
	}

}
