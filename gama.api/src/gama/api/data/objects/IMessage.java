/*******************************************************************************************************
 *
 * IMessage.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.objects;

import gama.annotations.doc;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.constants.IKeyword;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 *
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

	/** The Constant CONTENTS. */
	String CONTENTS = "contents";

	/** The Constant UNREAD. */
	String UNREAD = "unread";

	/** The Constant EMISSION_TIMESTAMP. */
	String EMISSION_TIMESTAMP = "emission_timestamp";

	/** The Constant RECEPTION_TIMESTAMP. */
	String RECEPTION_TIMESTAMP = "recention_timestamp";

	/** The Constant SENDER. */
	String SENDER = "sender";

	/** The Constant RECEIVERS. */
	String RECEIVERS = "receivers";

	/**
	 * Gets the sender.
	 *
	 * @return the sender
	 */
	Object getSender();

	/**
	 * Sets the sender.
	 *
	 * @param sender
	 *            the sender
	 */
	void setSender(Object sender);

	/**
	 * Gets the receivers.
	 *
	 * @return the receivers
	 */
	Object getReceivers();

	/**
	 * Sets the receivers.
	 *
	 * @param sender
	 *            the receivers
	 */
	void setReceivers(Object receivers);

	/**
	 * Sets the contents of the message.
	 *
	 * @param content
	 *            the content
	 */
	void setContents(Object content);

	/**
	 * Checks if is unread.
	 *
	 * @return true, if is unread
	 */
	boolean isUnread();

	/**
	 * Sets the unread.
	 *
	 * @param unread
	 *            the new unread
	 */
	void setUnread(boolean unread);

	/**
	 * Gets the emission timestamp.
	 *
	 * @return the emission timestamp
	 */
	int getEmissionTimestamp();

	/**
	 * Gets the reception timestamp.
	 *
	 * @return the reception timestamp
	 */
	int getReceptionTimestamp();

	/**
	 * Checks for been received.
	 *
	 * @param scope
	 *            the scope
	 */
	void hasBeenReceived(IScope scope);

}