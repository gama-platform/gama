/*******************************************************************************************************
 *
 * ISpeciesSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.kernel.serialization.IGamaObjectInput;
import gama.api.kernel.serialization.IGamaObjectOutput;
import gama.api.kernel.serialization.IGamaObjectSerializer;
import gama.api.kernel.serialization.AbstractBinarySerializer.TransientSerializationContext;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * FST binarySerialiser for {@link ISpecies} instances. Only the species name is persisted. On deserialisation, the
 * species is looked up in the current simulation model by name.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class ISpeciesSerialiser implements IGamaObjectSerializer<ISpecies> {

	/**
	 * Serialises the species name.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the species to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final ISpecies o, final TransientSerializationContext context)
			throws Exception {
		out.writeStringUTF(o.getName());
	}

	/**
	 * Deserialises a species by looking it up in the current simulation model by name.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the FST input stream
	 * @return the resolved {@link ISpecies}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public ISpecies deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		String name = in.readStringUTF();
		return scope.getModel().getSpecies(name);
	}

}
