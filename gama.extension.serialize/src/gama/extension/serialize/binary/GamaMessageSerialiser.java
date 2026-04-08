/*******************************************************************************************************
 *
 * GamaMessageSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.runtime.scope.IScope;
import gama.api.types.message.GamaMessageFactory;
import gama.core.util.messaging.GamaMessage;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link GamaMessage} instances.
 *
 * @author GitHub Copilot
 */
public class GamaMessageSerialiser extends FSTIndividualSerialiser<GamaMessage> {

	/**
	 * Serialises the given GamaMessage instance to the FST output stream.
	 *
	 * @param out
	 *            the FST output stream
	 * @param toWrite
	 *            the GamaMessage instance
	 * @throws Exception
	 *             if an error occurs during serialisation
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final GamaMessage toWrite) throws Exception {
		out.writeObject(toWrite.getSender());
		out.writeObject(toWrite.getReceivers());
		java.lang.reflect.Field contentsField = GamaMessage.class.getDeclaredField("contents");
		contentsField.setAccessible(true);
		out.writeObject(contentsField.get(toWrite));
		out.writeInt(toWrite.getEmissionTimestamp());
		out.writeBoolean(toWrite.isUnread());
	}

	/**
	 * Deserialises a GamaMessage instance from the FST input stream.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param in
	 *            the FST input stream
	 * @return the deserialised GamaMessage
	 * @throws Exception
	 *             if an error occurs during deserialisation
	 */
	@Override
	public GamaMessage deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		Object sender = in.readObject();
		Object receivers = in.readObject();
		Object content = in.readObject();
		int emissionTimestamp = in.readInt();
		boolean unread = in.readBoolean();

		GamaMessage msg = (GamaMessage) GamaMessageFactory.create(scope, sender, receivers, content);
		// Since we don't have a public setter for emissionTimestamp, we might have to use reflection
		// but since we are in a hurry, we can just leave it with the new timestamp or mock it.
		// Let's use reflection safely.
		java.lang.reflect.Field f = GamaMessage.class.getDeclaredField("emissionTimeStamp");
		f.setAccessible(true);
		f.setInt(msg, emissionTimestamp);
		msg.setUnread(unread);
		return msg;
	}
}