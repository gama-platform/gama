/*******************************************************************************************************
 *
 * LocalMessage.java, in gama.extension.network, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.network.common;

import gama.api.runtime.scope.IScope;
import gama.api.types.message.GamaMessageFactory;
import gama.api.types.message.IMessage;

/**
 * The Class LocalMessage.
 */
public class LocalMessage implements ConnectorMessage {

	/** The internal message. */
	private final Object internalMessage;

	/** The receiver. */
	private final String receiver;

	/** The sender. */
	private final String sender;

	/**
	 * Instantiates a new local message.
	 *
	 * @param sender
	 *            the sender
	 * @param receiver
	 *            the receiver
	 * @param ct
	 *            the ct
	 */
	public LocalMessage(final String sender, final String receiver, final Object ct) {
		this.sender = sender;
		this.receiver = receiver;
		this.internalMessage = ct;
	}

	@Override
	public String getSender() { return sender; }

	@Override
	public String getReceiver() { return receiver; }

	@Override
	public String getPlainContents() { return this.internalMessage.toString(); }

	@Override
	public boolean isPlainMessage() { return false; }

	@Override
	public IMessage getContents(final IScope scope) {
		IMessage message = null;
		if (internalMessage instanceof IMessage im) {
			message = im;
		} else {
			message = GamaMessageFactory.create(scope, sender, receiver, internalMessage);
		}
		message.hasBeenReceived(scope);
		return message;
	}

	@Override
	public boolean isCommandMessage() {

		return false;
	}

}
