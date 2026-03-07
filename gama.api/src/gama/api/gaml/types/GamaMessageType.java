/*******************************************************************************************************
 *
 * GamaMessageType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.message.GamaMessageFactory;
import gama.api.types.message.IMessage;

/**
 * Type representing messages in GAML - structured communications exchanged between agents.
 * <p>
 * Messages are the fundamental unit of communication in multi-agent systems within GAMA. They encapsulate information
 * being transmitted between agents, including sender, receivers, content, and communication protocol metadata. Messages
 * support various communication paradigms including FIPA-ACL (agent communication language) protocols.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Structured agent-to-agent communication</li>
 * <li>Sender and receiver identification</li>
 * <li>Arbitrary content types</li>
 * <li>Protocol-based communication (FIPA-ACL)</li>
 * <li>Conversation management</li>
 * <li>Message queuing and retrieval</li>
 * <li>Network communication support</li>
 * </ul>
 *
 * <h2>Message Attributes:</h2>
 * <ul>
 * <li><b>sender</b> - the agent that sent the message</li>
 * <li><b>receivers</b> - list of intended recipient agents</li>
 * <li><b>contents</b> - the actual content/payload of the message</li>
 * <li><b>performative</b> - FIPA performative (inform, request, propose, etc.)</li>
 * <li><b>protocol</b> - communication protocol identifier</li>
 * <li><b>conversation_id</b> - identifier for conversation tracking</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <pre>
 * {@code
 * // Create and send a message (messaging skill required)
 * species communicating_agent skills: [messaging] {
 *
 *     reflex send_info {
 *         do start_conversation to: [agent1, agent2]
 *            protocol: 'fipa-request'
 *            performative: 'inform'
 *            contents: ['Hello', 42];
 *     }
 *
 *     reflex receive_messages {
 *         // Check mailbox
 *         if (length(mailbox) > 0) {
 *             message msg <- mailbox[0];
 *             write "Received from " + msg.sender + ": " + msg.contents;
 *
 *             // Reply
 *             do reply message: msg contents: "Acknowledged";
 *         }
 *     }
 * }
 *
 * // Create message directly
 * message msg <- message("Simple text content");
 *
 * // Access message properties
 * agent sender <- my_message.sender;
 * list receivers <- my_message.receivers;
 * unknown content <- my_message.contents;
 * string perf <- my_message.performative;
 *
 * // Filter messages by sender
 * list<message> msgs_from_agent1 <- mailbox where (each.sender = agent1);
 * }
 * </pre>
 *
 * <h2>Communication Protocols:</h2>
 * <p>
 * GAMA supports FIPA-ACL protocols for structured agent communication:
 * <ul>
 * <li>fipa-request - request/response interactions</li>
 * <li>fipa-query - information queries</li>
 * <li>fipa-propose - proposal negotiations</li>
 * <li>fipa-contract-net - contract negotiation</li>
 * <li>And more custom protocols...</li>
 * </ul>
 * </p>
 *
 * @author GAMA Development Team
 * @see GamaType
 * @see IMessage
 * @see gama.api.types.message.GamaMessageFactory
 * @since GAMA 1.0
 */
@type (
		name = GamaMessageType.MESSAGE_STR,
		id = IType.MESSAGE,
		wraps = { IMessage.class },
		kind = ISymbolKind.REGULAR,
		doc = @doc ("Represents the messages exchanged between agents"))
public class GamaMessageType extends GamaType<IMessage> {

	/** The constant for the message type name. */
	public static final String MESSAGE_STR = "message";

	/**
	 * Constructs a new message type.
	 *
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaMessageType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Returns the default value for message type.
	 * <p>
	 * The default message is null, as there is no meaningful default message.
	 * </p>
	 *
	 * @return null
	 */
	@Override
	public IMessage getDefault() { return null; }

	/**
	 * Indicates whether this type accepts null instances.
	 * <p>
	 * Message type accepts null values.
	 * </p>
	 *
	 * @return true, null messages are accepted
	 */
	@Override
	protected boolean acceptNullInstances() {
		return true;
	}

	/**
	 * Casts an object to a message.
	 * <p>
	 * This method supports casting from:
	 * <ul>
	 * <li>IMessage - returns the message itself</li>
	 * <li>Other types - creates a new message with the current agent as sender and the object as contents</li>
	 * </ul>
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a message
	 * @param param
	 *            optional parameter (not used for message casting)
	 * @param copy
	 *            whether to create a copy (not applicable for messages)
	 * @return the message instance
	 * @throws GamaRuntimeException
	 *             if the casting operation encounters an error
	 */
	@Override
	@doc ("Returns a message built from the argument. If the argument is already a message returns it, otherwise returns a message with the current agent as the sender and the argument as the contents ")
	public IMessage cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof IMessage m) return (IMessage) (copy ? m.copy(scope) : m);
		return GamaMessageFactory.castToMessage(scope, scope.getAgent(), obj);
	}

	/**
	 * Indicates whether messages can be cast to constant values.
	 * <p>
	 * Messages cannot be constant as they represent dynamic communication events.
	 * </p>
	 *
	 * @return false, messages are not constant
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}
}
