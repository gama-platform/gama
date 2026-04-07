/*******************************************************************************************************
 *
 * AgentReferenceSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.kernel.agent.AgentReference;
import gama.api.runtime.scope.IScope;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * FST serialiser for {@link AgentReference} instances.
 * Serialises the species path (a {@code String[]} array) and the index path (an {@code Integer[]} array)
 * that together identify the referenced agent within the simulation hierarchy.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class AgentReferenceSerialiser extends FSTIndividualSerialiser<AgentReference> {

	/**
	 * Constructs a new {@code AgentReferenceSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser
	 */
	AgentReferenceSerialiser(final BinarySerialiser serialiser) {
		super(serialiser);
	}

	/**
	 * Serialises the species path array and the index path array of the agent reference.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the agent reference to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final FSTObjectOutput out, final AgentReference o) throws Exception {
		out.writeObject(o.species());
		out.writeObject(o.index());
	}

	/**
	 * Deserialises an agent reference by reading its species path and index path arrays.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link AgentReference}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public AgentReference deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
		return AgentReference.of((String[]) in.readObject(), (Integer[]) in.readObject());
	}

}
