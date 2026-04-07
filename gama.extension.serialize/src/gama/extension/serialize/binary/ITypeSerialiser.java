/*******************************************************************************************************
 *
 * ITypeSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * FST serialiser for {@link IType} instances.
 * Serialises the GAML type name. For compound types (e.g. {@code map<string, int>}),
 * also serialises the key and content types recursively.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class ITypeSerialiser extends FSTIndividualSerialiser<IType> {

	/**
	 * Constructs a new {@code ITypeSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser
	 */
	ITypeSerialiser(final BinarySerialiser serialiser) {
		super(serialiser);
	}

	/**
	 * Serialises the GAML type name. For compound types, also writes the key and content types.
	 *
	 * @param out
	 *            the FST output stream
	 * @param toWrite
	 *            the type to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final FSTObjectOutput out, final IType toWrite) throws Exception {
		out.writeStringUTF(toWrite.getGamlType().getName());
		if (toWrite.isCompoundType()) {
			out.writeObject(toWrite.getKeyType());
			out.writeObject(toWrite.getContentType());
		}
	}

	/**
	 * Deserialises a type by looking it up in the scope by name. For compound types,
	 * also reads the key and content types and combines them via {@link GamaType#from}.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IType}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ("rawtypes")
	@Override
	public IType deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
		String name = in.readStringUTF();
		IType type = scope.getType(name);
		if (type.isCompoundType()) {
			IType key = (IType) in.readObject();
			IType content = (IType) in.readObject();
			return GamaType.from(type, key, content);
		}
		return type;
	}

}
