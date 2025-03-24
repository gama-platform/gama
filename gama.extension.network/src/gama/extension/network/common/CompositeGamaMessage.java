/*******************************************************************************************************
 *
 * CompositeGamaMessage.java, in gama.extension.network, is part of the source code of the GAMA modeling and simulation
 * platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.network.common;

import gama.core.messaging.GamaMessage;
import gama.core.runtime.IScope;
import gama.extension.serialize.binary.BinarySerialisation;

/**
 * The Class CompositeGamaMessage.
 */
public class CompositeGamaMessage extends GamaMessage {

	/** The deserialize content. */
	protected Object deserializeContent;

	/**
	 * Instantiates a new composite gama message.
	 *
	 * @param scope
	 *            the scope
	 * @param message
	 *            the message
	 */
	public CompositeGamaMessage(final IScope scope, final GamaMessage message) {
		super(scope, message.getSender(), message.getReceivers(), message.getContents(scope));
		this.contents = BinarySerialisation.saveToString(scope, message.getContents(scope));
		// this.contents = StreamConverter.convertNetworkObjectToStream(scope, message.getContents(scope));
		this.emissionTimeStamp = message.getEmissionTimestamp();
		this.setUnread(true);
		deserializeContent = null;
	}

	/**
	 * Instantiates a new composite gama message.
	 *
	 * @param scope
	 *            the scope
	 * @param sender
	 *            the sender
	 * @param receivers
	 *            the receivers
	 * @param content
	 *            the content
	 * @param deserializeContent
	 *            the deserialize content
	 * @param timeStamp
	 *            the time stamp
	 */
	private CompositeGamaMessage(final IScope scope, final Object sender, final Object receivers, final Object content,
			final Object deserializeContent, final int timeStamp) {
		super(scope, sender, receivers, content);
		this.emissionTimeStamp = timeStamp;
		this.setUnread(true);
		this.deserializeContent = deserializeContent;
	}

	@Override
	public Object getContents(final IScope scope) {
		this.setUnread(false);
		if (deserializeContent == null) {
			deserializeContent = BinarySerialisation.createFromString(scope, (String) contents);
			// deserializeContent = StreamConverter.convertNetworkStreamToObject(scope, (String) contents);//

		}
		return deserializeContent;
	}
}
