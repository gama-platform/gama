/*******************************************************************************************************
 *
 * GamaMessage.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.messaging;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.message.IMessage;
import gama.api.types.message.IMessageFactory;
import gama.api.utils.StringUtils;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The Class GamaMessageProxy.
 *
 * @author drogoul
 */

public class GamaMessage implements IMessage {

	/**
	 * The Class Factory.
	 */
	public static class Factory implements IMessageFactory {

		@Override
		public IMessage create(final IScope scope, final IAgent agent, final Object contents) {
			return new GamaMessage(scope, agent, null, contents);
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
		@Override
		public IMessage create(final IScope scope, final Object sender, final Object receivers, final Object contents) {
			return new GamaMessage(scope, sender, receivers, contents);
		}

	}

	/** The unread. */
	private boolean unread;

	/** The sender. */
	private Object sender;

	/** The receivers. */
	private Object receivers;

	/** The contents. */
	protected Object contents;

	/** The emission time stamp. */
	protected int emissionTimeStamp;

	/**
	 * Instantiates a new gama message.
	 *
	 * @param scope
	 *            the scope
	 * @param sender
	 *            the sender
	 * @param receivers
	 *            the receivers
	 * @param content
	 *            the content
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected GamaMessage(final IScope scope, final Object sender, final Object receivers, final Object content)
			throws GamaRuntimeException {
		emissionTimeStamp = scope.getClock().getCycle();
		unread = true;
		setSender(sender);
		setReceivers(receivers);
		setContents(content);
	}

	/**
	 * Gets the sender.
	 *
	 * @return the sender
	 */
	@Override

	public Object getSender() { return sender; }

	/**
	 * Sets the sender.
	 *
	 * @param sender
	 *            the sender
	 */
	@Override

	public void setSender(final Object sender) { this.sender = sender; }

	/**
	 * Gets the receivers.
	 *
	 * @return the receivers
	 */
	@Override

	public Object getReceivers() { return receivers; }

	/**
	 * Sets the receivers.
	 *
	 * @param sender
	 *            the receivers
	 */
	@Override

	public void setReceivers(final Object receivers) { this.receivers = receivers; }

	/**
	 * Gets the contents of the message.
	 *
	 * @return the contents
	 */

	@Override
	public Object getContents(final IScope scope) {
		setUnread(false);
		return contents;
	}

	/**
	 * Sets the contents of the message.
	 *
	 * @param content
	 *            the content
	 */
	@Override

	public void setContents(final Object content) { contents = content; }

	/**
	 * Checks if is unread.
	 *
	 * @return true, if is unread
	 */
	@Override

	public boolean isUnread() { return unread; }

	/**
	 * Sets the unread.
	 *
	 * @param unread
	 *            the new unread
	 */
	@Override

	public void setUnread(final boolean unread) { this.unread = unread; }

	/**
	 * Gets the emission timestamp.
	 *
	 * @return the emission timestamp
	 */
	@Override

	public int getEmissionTimestamp() { return emissionTimeStamp; }

	/**
	 * Gets the reception timestamp.
	 *
	 * @return the reception timestamp
	 */
	@Override

	public int getReceptionTimestamp() { return emissionTimeStamp; }

	/**
	 * Serialize to gaml.
	 *
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return StringUtils.toGaml(contents, includingBuiltIn);
	}

	/**
	 * String value.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "message[sender: " + getSender() + "; content: " + getContents(scope) + "]";
	}

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the gama message
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public GamaMessage copy(final IScope scope) throws GamaRuntimeException {
		return new GamaMessage(scope, getSender(), getReceivers(), getContents(scope));
	}

	/**
	 * Method getType()
	 *
	 * @see gama.api.gaml.types.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.get(IType.MESSAGE); }

	/**
	 * Int value.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	@Override
	public int intValue(final IScope scope) {
		return getEmissionTimestamp();
	}

	/**
	 * Checks for been received.
	 *
	 * @param scope
	 *            the scope
	 */
	@Override
	public void hasBeenReceived(final IScope scope) {}

	/**
	 * Serialize to json.
	 *
	 * @param json
	 *            the json
	 * @return the i json value
	 */
	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), SENDER, sender, CONTENTS, contents, RECEPTION_TIMESTAMP,
				emissionTimeStamp, EMISSION_TIMESTAMP, emissionTimeStamp).add(RECEIVERS, receivers);
	}

	/**
	 * @return
	 */
	public Object rawContents() {
		return contents;
	}

}
