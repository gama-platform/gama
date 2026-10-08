/*******************************************************************************************************
 *
 * SerialisedPopulationSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import java.util.List;

import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.kernel.serialization.SerialisedPopulation;
import gama.api.runtime.scope.IScope;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link SerialisedPopulation} instances. Persists the species name and the ordered list of
 * serialised agents belonging to the population.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class SerialisedPopulationSerialiser extends FSTIndividualSerialiser<SerialisedPopulation> {

	/**
	 * Serialises the species name and the list of serialised agents.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the serialised population to write
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final SerialisedPopulation o) throws Exception {
		out.writeStringUTF(o.speciesName());
		out.writeObject(o.agents());
	}

	/**
	 * Deserialises a population by reading its species name and list of agents.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link SerialisedPopulation}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public SerialisedPopulation deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		return new SerialisedPopulation(in.readStringUTF(), (List<ISerialisedAgent>) in.readObject());
	}

}
