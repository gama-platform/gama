/*******************************************************************************************************
 *
 * SerializationContext.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary.spi;

import gama.api.runtime.scope.IScope;

/**
 * GAMA state available to type serializers during one encoding or decoding operation.
 */
public interface SerializationContext {

	/**
	 * Returns the simulation scope associated with this operation.
	 *
	 * @return the operation's scope, or {@code null} when the operation has no scope
	 */
	IScope scope();

	/**
	 * Returns whether serialization is currently processing the contents of an agent.
	 *
	 * @return {@code true} while inside an agent serialization
	 */
	boolean isSerializingAgent();

	/**
	 * Updates whether serialization is currently processing the contents of an agent.
	 *
	 * @param serializingAgent
	 *            {@code true} while inside an agent serialization
	 */
	void setSerializingAgent(boolean serializingAgent);
}
