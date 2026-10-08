/*******************************************************************************************************
 *
 * SerialisedGridSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
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
import gama.api.kernel.serialization.SerialisedGrid;
import gama.api.kernel.topology.IGrid;
import gama.api.runtime.scope.IScope;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link SerialisedGrid} instances. Persists the species name, the list of serialised agents, and
 * the underlying {@link IGrid} matrix.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class SerialisedGridSerialiser extends FSTIndividualSerialiser<SerialisedGrid> {

	/**
	 * Serialises the species name, the list of agents, and the grid matrix.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the serialised grid to write
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final SerialisedGrid o) throws Exception {
		out.writeStringUTF(o.speciesName());
		out.writeObject(o.agents());
		out.writeObject(o.matrix());
	}

	/**
	 * Deserialises a grid by reading its species name, agent list, and grid matrix.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link SerialisedGrid}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public SerialisedGrid deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		return new SerialisedGrid(in.readStringUTF(), (List<ISerialisedAgent>) in.readObject(),
				(IGrid) in.readObject());
	}

}
