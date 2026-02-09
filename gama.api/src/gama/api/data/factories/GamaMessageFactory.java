/*******************************************************************************************************
 *
 * GamaMessageFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import gama.api.data.objects.IMessage;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public class GamaMessageFactory implements IFactory<IMessage> {

	/** The Internal factory. */
	private static IMessageFactory InternalFactory;

	/**
	 * Sets the internal builder.
	 *
	 * @param factory
	 *            the new internal builder
	 */
	public static void setBuilder(final IMessageFactory factory) { InternalFactory = factory; }

	/**
	 * @param scope
	 * @param agent
	 * @param obj
	 * @return
	 */
	public static IMessage create(final IScope scope, final IAgent agent, final Object contents) {
		return InternalFactory.create(scope, agent, contents);
	}

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
	public static IMessage create(final IScope scope, final Object sender, final Object receivers,
			final Object contents) {
		return InternalFactory.create(scope, sender, receivers, contents);
	}

}
