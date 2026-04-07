/*******************************************************************************************************
 *
 * IScopeSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.runtime.scope.IScope;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * FST serialiser for {@link IScope} instances.
 * Only the scope's name is persisted. On deserialisation, a named copy of the current
 * simulation scope is returned via {@link IScope#copy(String)}.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IScopeSerialiser extends FSTIndividualSerialiser<IScope> {

	/**
	 * Constructs a new {@code IScopeSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser
	 */
	IScopeSerialiser(final BinarySerialiser serialiser) {
		super(serialiser);
	}

	/**
	 * Serialises the scope's name.
	 *
	 * @param out
	 *            the FST output stream
	 * @param toWrite
	 *            the scope to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final FSTObjectOutput out, final IScope toWrite) throws Exception {
		out.writeStringUTF(toWrite.getName());
	}

	/**
	 * Deserialises a scope by creating a named copy of the current simulation scope.
	 *
	 * @param scope
	 *            the current GAMA simulation scope to copy
	 * @param in
	 *            the FST input stream
	 * @return a copy of {@code scope} with the stored name
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public IScope deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
		String name = in.readStringUTF();
		return scope.copy(name);
	}

}
