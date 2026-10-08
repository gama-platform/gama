/*******************************************************************************************************
 *
 * IClassSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.kernel.object.IClass;
import gama.api.runtime.scope.IScope;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link IClass} instances. Only the class name is persisted. On deserialisation, the class is
 * looked up in the current simulation model by name.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IClassSerialiser extends FSTIndividualSerialiser<IClass> {

	/**
	 * Serialises the class name.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the class to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final IClass o) throws Exception {
		out.writeStringUTF(o.getName());
	}

	/**
	 * Deserialises a class by looking it up in the current simulation model by name.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the FST input stream
	 * @return the resolved {@link IClass}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public IClass deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		String name = in.readStringUTF();
		return scope.getModel().getClass(name);
	}

}
