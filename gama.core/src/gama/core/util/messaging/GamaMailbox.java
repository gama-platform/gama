/*******************************************************************************************************
 *
 * GamaMailbox.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2.0.0).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama2 for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.messaging;

import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaList;
import gama.api.types.message.IMessage;

/**
 * A specialized GamaList that holds messages
 *
 * @author drogoul
 *
 */
public class GamaMailbox<T extends IMessage> extends GamaList<T> {

	/**
	 * Instantiates a new gama mailbox.
	 */
	public GamaMailbox() {
		this(100);
	}

	/**
	 * Instantiates a new gama mailbox.
	 *
	 * @param capacity
	 *            the capacity
	 */
	public GamaMailbox(final int capacity) {
		super(capacity, Types.get(IType.MESSAGE));
	}

	/**
	 * Adds the message.
	 *
	 * @param scope
	 *            the scope
	 * @param message
	 *            the message
	 */
	public void addMessage(final IScope scope, final T message) {
		// message.hasBeenReceived(scope);
		addValue(scope, message);
	}

}
