/*******************************************************************************************************
 *
 * IMapSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import java.io.IOException;

import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link IMap} instances. Persists the key type, content type, ordering flag, entry count, and all
 * key-value pairs. The map is reconstructed via {@link GamaMapFactory#create(IType, IType, boolean)}.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IMapSerialiser extends FSTIndividualSerialiser<IMap> {

	/**
	 * Serialises the map's key type, content type, ordering flag, size, and all key-value pairs. Each key and value is
	 * written as an object via {@link gama.extension.serialize.fst.FSTObjectOutput#writeObject}.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the map to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public void serialise(final IGamaObjectOutput out, final IMap o) throws Exception {
		out.writeObject(o.getGamlType().getKeyType());
		out.writeObject(o.getGamlType().getContentType());
		out.writeBoolean(o.isOrdered());
		out.writeInt(o.size());
		o.forEach((k, v) -> {
			try {
				out.writeObject(k);
				out.writeObject(v);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Deserialises a map by reading its key type, content type, ordering flag, and all entries.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IMap}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	public IMap deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		IType k = (IType) in.readObject();
		IType c = (IType) in.readObject();
		boolean ordered = in.readBoolean();
		IMap<Object, Object> result = GamaMapFactory.create(k, c, ordered);
		int size = in.readInt();
		for (int i = 0; i < size; i++) { result.put(in.readObject(), in.readObject()); }
		return result;
	}

}
