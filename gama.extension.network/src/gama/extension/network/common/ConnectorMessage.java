/*******************************************************************************************************
 *
 * ConnectorMessage.java, in gama.extension.network, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.network.common;

import gama.api.data.objects.IMessage;
import gama.api.runtime.scope.IScope;

/**
 * The Interface ConnectorMessage.
 */
public interface ConnectorMessage {

	/**
	 * Gets the sender.
	 *
	 * @return the sender
	 */
	String getSender();

	/**
	 * Gets the receiver.
	 *
	 * @return the receiver
	 */
	String getReceiver();

	/**
	 * Gets the plain contents.
	 *
	 * @return the plain contents
	 */
	String getPlainContents();

	/**
	 * Checks if is plain message.
	 *
	 * @return true, if is plain message
	 */
	boolean isPlainMessage();

	/**
	 * Checks if is command message.
	 *
	 * @return true, if is command message
	 */
	boolean isCommandMessage();

	/**
	 * Gets the contents.
	 *
	 * @param scope
	 *            the scope
	 * @return the contents
	 */
	IMessage getContents(IScope scope);
}
