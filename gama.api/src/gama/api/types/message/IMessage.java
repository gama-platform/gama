/*******************************************************************************************************
 *
 * IMessage.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.message;

import gama.annotations.doc;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.constants.IKeyword;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IValue;

/**
 * Represents a message that can be exchanged between agents in a GAMA simulation.
 * 
 * <p>
 * Messages are the primary mechanism for agent communication in multi-agent systems. Each message encapsulates
 * information about who sent it, who should receive it, what content it carries, and when it was sent and received.
 * Messages also track their read/unread status to support message processing workflows.
 * </p>
 * 
 * <h2>Message Structure</h2>
 * <p>
 * A message consists of the following components:
 * </p>
 * <ul>
 * <li><strong>Sender:</strong> The agent or entity that created and sent the message</li>
 * <li><strong>Receivers:</strong> The intended recipient(s) of the message (can be a single agent or multiple)</li>
 * <li><strong>Contents:</strong> The payload or data carried by the message (can be any GAMA value)</li>
 * <li><strong>Emission timestamp:</strong> The simulation cycle when the message was sent</li>
 * <li><strong>Reception timestamp:</strong> The simulation cycle when the message was received</li>
 * <li><strong>Unread flag:</strong> Whether the message has been read by the receiver</li>
 * </ul>
 * 
 * <h2>Available Variables</h2>
 * <ul>
 * <li><strong>sender</strong> (any type) - The agent or entity that sent this message</li>
 * <li><strong>contents</strong> (any type) - The payload of the message as a list of arbitrary objects</li>
 * <li><strong>unread</strong> (bool, default: true) - Whether this message is unread</li>
 * <li><strong>emission_timestamp</strong> (int) - The cycle at which this message was emitted</li>
 * <li><strong>reception_timestamp</strong> (int) - The cycle at which this message was received</li>
 * </ul>
 * 
 * <h2>Message Lifecycle</h2>
 * <ol>
 * <li>A message is created with {@link GamaMessageFactory#create(IScope, Object, Object, Object)}</li>
 * <li>The emission timestamp is set to the current simulation cycle</li>
 * <li>The message is sent to receiver(s) and delivered (implementation-specific)</li>
 * <li>Upon delivery, {@link #hasBeenReceived(IScope)} is called to set the reception timestamp</li>
 * <li>The receiver can check {@link #isUnread()} and process the message</li>
 * <li>The receiver marks the message as read using {@link #setUnread(boolean)}</li>
 * </ol>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>
 * // Sending a message
 * IMessage msg = GamaMessageFactory.create(scope, this, otherAgent, "Hello!");
 * // ... send via communication protocol ...
 * 
 * // Receiving and processing messages
 * for (IMessage msg : mailbox) {
 *     if (msg.isUnread()) {
 *         Object sender = msg.getSender();
 *         Object content = msg.getContents();
 *         // ... process message ...
 *         msg.setUnread(false);
 *     }
 * }
 * 
 * // Checking timestamps
 * int delay = msg.getReceptionTimestamp() - msg.getEmissionTimestamp();
 * </pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
@vars ({ @variable (
		name = IMessage.SENDER,
		type = IType.NONE,
		doc = { @doc ("Returns the sender that has sent this message") }),
		@variable (
				name = IMessage.CONTENTS,
				type = IType.NONE,
				doc = { @doc ("Returns the contents of this message, as a list of arbitrary objects") }),
		@variable (
				name = IMessage.UNREAD,
				type = IType.BOOL,
				init = IKeyword.TRUE,
				doc = { @doc ("Returns whether this message is unread or not") }),
		@variable (
				name = IMessage.RECEPTION_TIMESTAMP,
				type = IType.INT,
				doc = { @doc ("Returns the reception time stamp of this message (I.e. at what cycle it has been received)") }),
		@variable (
				name = IMessage.EMISSION_TIMESTAMP,
				type = IType.INT,
				doc = { @doc ("Returns the emission time stamp of this message (I.e. at what cycle it has been emitted)") }) })

public interface IMessage extends IValue {

	/**
	 * Constant key for the message contents attribute.
	 */
	String CONTENTS = "contents";

	/**
	 * Constant key for the unread status attribute.
	 */
	String UNREAD = "unread";

	/**
	 * Constant key for the emission timestamp attribute.
	 */
	String EMISSION_TIMESTAMP = "emission_timestamp";

	/**
	 * Constant key for the reception timestamp attribute.
	 */
	String RECEPTION_TIMESTAMP = "recention_timestamp";

	/**
	 * Constant key for the sender attribute.
	 */
	String SENDER = "sender";

	/**
	 * Constant key for the receivers attribute.
	 */
	String RECEIVERS = "receivers";

	/**
	 * Gets the sender of this message.
	 * 
	 * <p>
	 * The sender is typically an agent, but can be any object depending on the communication protocol.
	 * </p>
	 *
	 * @return the sender object (usually an {@link gama.api.kernel.agent.IAgent})
	 */
	Object getSender();

	/**
	 * Sets the sender of this message.
	 * 
	 * <p>
	 * This method allows changing the sender after message creation, which may be useful in some communication
	 * protocols or message forwarding scenarios.
	 * </p>
	 *
	 * @param sender
	 *            the new sender object
	 */
	void setSender(Object sender);

	/**
	 * Gets the receiver(s) of this message.
	 * 
	 * <p>
	 * The receivers can be a single agent, a list of agents, or any other representation depending on the
	 * implementation and communication protocol.
	 * </p>
	 *
	 * @return the receiver(s) object
	 */
	Object getReceivers();

	/**
	 * Sets the receiver(s) of this message.
	 * 
	 * <p>
	 * This method allows changing the receivers after message creation.
	 * </p>
	 *
	 * @param receivers
	 *            the new receiver(s) object
	 */
	void setReceivers(Object receivers);

	/**
	 * Gets the contents/payload of this message.
	 * 
	 * <p>
	 * The contents can be any GAMA value or object - primitives, lists, maps, agents, geometries, etc. It is up to the
	 * receiver to interpret the contents appropriately.
	 * </p>
	 *
	 * @return the message contents
	 */
	default Object getContents() {
		return null;
	}

	/**
	 * Sets the contents/payload of this message.
	 * 
	 * <p>
	 * This method allows changing the message contents after creation.
	 * </p>
	 *
	 * @param content
	 *            the new message contents
	 */
	void setContents(Object content);

	/**
	 * Checks if this message is unread.
	 * 
	 * <p>
	 * Messages are typically created with unread status set to true. Receivers should set this to false after
	 * processing the message.
	 * </p>
	 *
	 * @return true if the message has not been read, false otherwise
	 */
	boolean isUnread();

	/**
	 * Sets the unread status of this message.
	 * 
	 * <p>
	 * Receivers typically call this method with false after processing a message to mark it as read.
	 * </p>
	 *
	 * @param unread
	 *            true to mark as unread, false to mark as read
	 */
	void setUnread(boolean unread);

	/**
	 * Gets the emission timestamp of this message.
	 * 
	 * <p>
	 * The emission timestamp is the simulation cycle number when the message was created and sent.
	 * </p>
	 *
	 * @return the cycle number when this message was emitted
	 */
	int getEmissionTimestamp();

	/**
	 * Gets the reception timestamp of this message.
	 * 
	 * <p>
	 * The reception timestamp is the simulation cycle number when the message was received/delivered. It is set by
	 * calling {@link #hasBeenReceived(IScope)}.
	 * </p>
	 *
	 * @return the cycle number when this message was received
	 */
	int getReceptionTimestamp();

	/**
	 * Marks this message as having been received.
	 * 
	 * <p>
	 * This method should be called by the message delivery system when the message is delivered to the receiver. It
	 * sets the reception timestamp to the current simulation cycle.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope (provides access to simulation time)
	 */
	void hasBeenReceived(IScope scope);

}