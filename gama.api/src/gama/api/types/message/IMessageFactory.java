/*******************************************************************************************************
 *
 * IMessageFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.message;

import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public interface IMessageFactory {

	/**
	 * @param scope
	 * @param agent
	 * @param contents
	 * @return
	 */
	IMessage create(IScope scope, IAgent agent, Object contents);

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param sender
	 *            the sender
	 * @param receivers
	 *            the receivers
	 * @param contents
	 *            the contents
	 * @return the i message
	 */
	IMessage create(final IScope scope, final Object sender, final Object receivers, final Object contents);
}
