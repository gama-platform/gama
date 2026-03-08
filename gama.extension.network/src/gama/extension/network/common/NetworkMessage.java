/*******************************************************************************************************
 *
 * NetworkMessage.java, in gama.extension.network, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
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
import gama.extension.serialize.binary.BinarySerialisation;

/**
 * The Class NetworkMessage.
 */
public class NetworkMessage implements ConnectorMessage {

	/** The Constant UNDEFINED. */
	public static final String UNDEFINED = "undefined";

	/** The from. */
	private final String from;

	/** The to. */
	private final String to;

	/** The content. */
	private final String content;

	/** The is plain message. */
	protected boolean isPlainMessage = false;

	/**
	 * Instantiates a new network message.
	 *
	 * @param to
	 *            the to
	 * @param data
	 *            the data
	 */
	protected NetworkMessage(final String to, final String data) {
		this.content = data;
		this.from = UNDEFINED;
		this.to = to;
		isPlainMessage = true;
	}

	/**
	 * Instantiates a new network message.
	 *
	 * @param to
	 *            the to
	 * @param data
	 *            the data
	 */
	protected NetworkMessage(final String from, final String to, final String data, final boolean isPlain) {
		this.from = from;
		this.to = to;
		this.content = data;
		isPlainMessage = isPlain;
	}

	/**
	 * Instantiates a new network message.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param data
	 *            the data
	 */
	protected NetworkMessage(final String from, final String to, final String data) {
		this.from = from;
		this.to = to;
		this.content = data;
		isPlainMessage = false;
	}

	@Override
	public String getSender() { return from; }

	@Override
	public String getReceiver() { return to; }

	@Override
	public String getPlainContents() { return content; }

	@Override
	public boolean isPlainMessage() { return isPlainMessage; }

	@Override
	public IMessage getContents(final IScope scope) {
		return isPlainMessage ? getPlainContent(scope) : getCompositeContent(scope);
	}

	/**
	 * Gets the plain content.
	 *
	 * @param scope
	 *            the scope
	 * @return the plain content
	 */
	public IMessage getPlainContent(final IScope scope) {
		final IMessage message = GamaMessageFactory.create(scope, from, to, content);
		message.hasBeenReceived(scope);
		return message;
	}

	/**
	 * Gets the composite content.
	 *
	 * @param scope
	 *            the scope
	 * @return the composite content
	 */
	public IMessage getCompositeContent(final IScope scope) {
		final Object messageContent = BinarySerialisation.createFromString(scope, content);
		IMessage message = null;
		if (messageContent instanceof CompositeGamaMessage cgm) {
			message = cgm;
		} else {
			message = GamaMessageFactory.create(scope, from, to, messageContent);
		}
		message.hasBeenReceived(scope);
		return message;
	}

	@Override
	public boolean isCommandMessage() { return false; }
}
