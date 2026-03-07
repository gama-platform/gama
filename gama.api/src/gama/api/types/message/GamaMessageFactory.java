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
package gama.api.types.message;

import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * A static factory for creating and converting {@link IMessage} instances.
 *
 * <p>
 * This class provides the main entry point for creating messages in GAMA. It delegates to an internal
 * {@link IMessageFactory} implementation that can be configured at runtime, allowing for pluggable message
 * implementations while maintaining a consistent public API.
 * </p>
 *
 * <h2>Configuration</h2>
 * <p>
 * The factory must be configured with an internal implementation using {@link #setBuilder(IMessageFactory)} before any
 * messages can be created. This is typically done during GAMA platform initialization.
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <pre>
 * // Create a message with specific sender and receivers
 * IMessage msg = GamaMessageFactory.create(scope, senderAgent, receiverAgent, "Hello!");
 *
 * // Create a message from an agent with arbitrary content
 * IMessage msg2 = GamaMessageFactory.castToMessage(scope, agent, contentObject);
 * </pre>
 *
 * @author drogoul
 * @since GAMA 1.0
 */
public class GamaMessageFactory {

	/**
	 * The internal factory implementation used to create message instances. This is set once during platform
	 * initialization.
	 */
	private static IMessageFactory InternalFactory;

	/**
	 * Configures the internal factory implementation.
	 *
	 * <p>
	 * This method should be called once during GAMA platform initialization to register the concrete message factory
	 * implementation.
	 * </p>
	 *
	 * @param factory
	 *            the {@link IMessageFactory} implementation to use for creating messages
	 */
	public static void setBuilder(final IMessageFactory factory) { InternalFactory = factory; }

	/**
	 * Converts an arbitrary object to a message.
	 *
	 * <p>
	 * This method creates a message with the specified agent as sender and the provided object as contents. The
	 * receivers are not explicitly set and may default to a value depending on the implementation.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param agent
	 *            the agent that will be the sender of the message
	 * @param contents
	 *            the content/payload of the message
	 * @return a new {@link IMessage} instance
	 */
	public static IMessage castToMessage(final IScope scope, final IAgent agent, final Object contents) {
		return InternalFactory.create(scope, agent, contents);
	}

	/**
	 * Creates a message with full control over sender, receivers, and contents.
	 *
	 * <p>
	 * This is the primary method for creating messages with complete information. All parameters are stored as provided
	 * without validation, allowing for flexible message creation patterns.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param sender
	 *            the agent or entity sending the message
	 * @param receivers
	 *            the intended recipient(s) - can be a single agent, a list of agents, or any other representation
	 * @param contents
	 *            the payload/content of the message - can be any GAMA value or object
	 * @return a new {@link IMessage} instance
	 */
	public static IMessage create(final IScope scope, final Object sender, final Object receivers,
			final Object contents) {
		return InternalFactory.create(scope, sender, receivers, contents);
	}

}
