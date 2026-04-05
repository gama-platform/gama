/*******************************************************************************************************
 *
 * FSTBasicObjectSerializer.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fst;

/**
 * Created with IntelliJ IDEA. User: ruedi Date: 11.11.12 Time: 12:09 To change this template use File | Settings | File
 * Templates.
 */
public abstract class FSTBasicObjectSerializer implements FSTObjectSerializer {

	/**
	 * Instantiates a new FST basic object serializer.
	 */
	protected FSTBasicObjectSerializer() {}

	@Override
	public boolean willHandleClass(final Class cl) {
		return true;
	}

	@Override
	public void readObject(final FSTObjectInput in, final Object toRead, final FSTClazzInfo clzInfo,
			final FSTClazzInfo.FSTFieldInfo referencedBy) throws Exception {}

	/**
	 * @return true if FST can skip a search for same instances in the serialized ObjectGraph. This speeds up reading
	 *         and writing and makes sense for short immutable such as Integer, Short, Character, Date, .. . For those
	 *         classes it is more expensive (CPU, size) to do a lookup than to just write the Object twice in case.
	 */
	@Override
	public boolean alwaysCopy() {
		return false;
	}

	@Override
	public Object instantiate(final Class objectClass, final FSTObjectInput in, final FSTClazzInfo serializationInfo,
			final FSTClazzInfo.FSTFieldInfo referencee, final int streamPosition) throws Exception {
		return null;
	}

	/**
	 * Write tuple end.
	 *
	 * @return true, if successful
	 */
	public boolean writeTupleEnd() {
		return true;
	}

}
