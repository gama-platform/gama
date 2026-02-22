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
 * The internal factory interface for creating message instances.
 * 
 * <p>
 * This interface defines the contract that concrete message factory implementations must fulfill. It is used internally
 * by {@link GamaMessageFactory} to delegate the actual creation of message objects. This design allows for pluggable
 * implementations while maintaining a consistent public API.
 * </p>
 * 
 * <p>
 * Implementations of this interface are responsible for:
 * </p>
 * <ul>
 * <li>Creating messages with full sender, receiver, and content information</li>
 * <li>Converting arbitrary objects to messages with a specified sender</li>
 * <li>Initializing message timestamps appropriately</li>
 * <li>Setting initial message state (e.g., unread status)</li>
 * </ul>
 * 
 * <p>
 * This interface should not be used directly by user code. Instead, use the static methods in
 * {@link GamaMessageFactory}.
 * </p>
 * 
 * @see GamaMessageFactory
 * @author drogoul
 * @since GAMA 1.0
 */
public interface IMessageFactory {

	/**
	 * Creates a message from an agent and contents.
	 * 
	 * <p>
	 * This method creates a message with the specified agent as sender and the provided object as contents. The
	 * receivers may be left unspecified or set to a default value.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param agent
	 *            the agent that will be the sender of the message
	 * @param contents
	 *            the payload of the message
	 * @return a new {@link IMessage} instance
	 */
	IMessage create(IScope scope, IAgent agent, Object contents);

	/**
	 * Creates a message with full information.
	 * 
	 * <p>
	 * This method creates a message with complete control over all message attributes: sender, receivers, and contents.
	 * This is the most flexible creation method.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param sender
	 *            the agent or entity sending the message
	 * @param receivers
	 *            the intended recipient(s) of the message
	 * @param contents
	 *            the payload/content of the message
	 * @return a new {@link IMessage} instance
	 */
	IMessage create(final IScope scope, final Object sender, final Object receivers, final Object contents);
}
