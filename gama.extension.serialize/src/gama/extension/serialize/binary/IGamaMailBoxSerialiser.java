/*******************************************************************************************************
 *
 * IObjectSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
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
import gama.api.types.message.IMessage;
import gama.core.util.messaging.GamaMailbox;
import gama.dev.DEBUG;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link IObject} instances. Serialises the species name and the full attribute map of the object.
 * On deserialisation, the species class is looked up in the model and a new instance is created. Objects deserialised
 * by this serialiser are not registered for back-reference tracking.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IGamaMailBoxSerialiser extends FSTIndividualSerialiser<GamaMailbox> {

	static
	{
		DEBUG.ON();
	}
	/**
	 * Returns {@code false}: objects are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	protected boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises the species name and the attribute map of the object.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the object to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final GamaMailbox o) throws Exception {
		DEBUG.OUT("serialize GamaMailbox ");
		out.writeInt(o.size());
		
		for(var auto: o)
		{
			DEBUG.OUT("serialize msg " + auto);
			out.writeObject((IMessage) auto);
		}
	}

	/**
	 * Deserialises an object by reading its species name and attributes, then creating a new instance via the model's
	 * class registry. Returns {@code null} if the class cannot be found.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IObject}, or {@code null} if the class is unknown
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public GamaMailbox deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		DEBUG.OUT("deserialize GamaMailbox ");
		final int size = in.readInt();
		DEBUG.OUT("deserialize GamaMailbox size " + size);
		GamaMailbox mailBox = new GamaMailbox(size);
		for (int i = 0; i < size; i++) { 
			DEBUG.OUT("index " + i);
			mailBox.addMessage(scope,(IMessage) in.readObject()); 
		}
		return mailBox;
	}

}
