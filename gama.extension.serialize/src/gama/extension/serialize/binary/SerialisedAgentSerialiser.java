/*******************************************************************************************************
 *
 * SerialisedAgentSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import java.util.Map;

import gama.api.kernel.serialization.ISerialisedPopulation;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.runtime.scope.IScope;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link SerialisedAgent} instances. Persists the agent's integer index, species name, attribute
 * map, and inner population map.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class SerialisedAgentSerialiser extends FSTIndividualSerialiser<SerialisedAgent> {

	/**
	 * Serialises the agent's index, species name, attribute map, and inner population map.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the serialised agent to write
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final SerialisedAgent o) throws Exception {
		out.writeInt(o.index());
		out.writeStringUTF(o.species());
		out.writeObject(o.attributes());
		out.writeObject(o.innerPopulations());
	}

	/**
	 * Deserialises a serialised agent by reading its index, species name, attribute map, and inner population map.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link SerialisedAgent}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public SerialisedAgent deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		return new SerialisedAgent(in.readInt(), in.readStringUTF(), (Map<String, Object>) in.readObject(),
				(Map<String, ISerialisedPopulation>) in.readObject());
	}

}
