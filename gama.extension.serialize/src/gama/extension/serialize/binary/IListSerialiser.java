/*******************************************************************************************************
 *
 * IListSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
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
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link IList} instances. Persists the content type, element count, and all elements in order. The
 * list is reconstructed via {@link GamaListFactory#create(IType)}. Objects deserialised by this serialiser are not
 * registered for back-reference tracking.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IListSerialiser extends FSTIndividualSerialiser<IList> {

	/**
	 * Returns {@code false}: lists are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	protected boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises the list's content type, size, and all elements. Each element is written as an object via
	 * {@link gama.extension.serialize.fst.FSTObjectOutput#writeObject}.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the list to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public void serialise(final IGamaObjectOutput out, final IList o) throws Exception {
		out.writeObject(o.getGamlType().getContentType());
		out.writeInt(o.size());
		o.forEach(v -> {
			try {
				out.writeObject(v);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Deserialises a list by reading its content type, size, and all elements.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IList}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	public IList deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		IType c = (IType) in.readObject();
		IList<Object> result = GamaListFactory.create(c);
		int size = in.readInt();
		for (int i = 0; i < size; i++) { result.add(in.readObject()); }
		return result;
	}

}
