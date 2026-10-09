/*******************************************************************************************************
 *
 * SerializationPreferences.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize;

import gama.api.gaml.types.IType;
import gama.api.kernel.serialization.AbstractBinarySerializer;
import gama.api.kernel.serialization.BinarySerialisation;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.Pref;
import gama.extension.serialize.fory.ForyBinarySerialiser;
import gama.extension.serialize.fst.FSTBinarySerialiser;

/**
 * Preference (and system property override) selecting the library used for binary serialisation. A single format is
 * used for both reading and writing: changing the backend does not allow reading data written with the other one.
 */
public class SerializationPreferences {

	/** Name of the FST backend. */
	public static final String FST = "FST";

	/** Name of the Fory backend. */
	public static final String FORY = "Fory";

	/** System property that, when set to FST or Fory, overrides the preference (useful in headless mode). */
	public static final String SYSTEM_PROPERTY = "gama.binary.serializer";

	/** The preference. */
	public static final Pref<String> BINARY_BACKEND = GamaPreferences
			.create("pref_binary_serializer", "Library used for binary serialisation (agents, simulation saving)", FST,
					IType.STRING, true)
			.among(FST, FORY).in(GamaPreferences.External.NAME, "Serialisation")
			.onChange(SerializationPreferences::apply);

	/**
	 * Returns the selected backend name, the system property taking precedence over the preference.
	 */
	public static String selected() {
		final String p = System.getProperty(SYSTEM_PROPERTY);
		if (FST.equalsIgnoreCase(p)) return FST;
		if (FORY.equalsIgnoreCase(p)) return FORY;
		return BINARY_BACKEND.getValue();
	}

	/** Installs the backend currently selected. */
	public static void apply() {
		apply(selected());
	}

	/**
	 * Installs the given backend.
	 */
	public static void apply(final String name) {
		final Class<? extends AbstractBinarySerializer> c =
				FORY.equalsIgnoreCase(name) ? ForyBinarySerialiser.class : FSTBinarySerialiser.class;
		BinarySerialisation.setBinarySerializerClass(c);
	}

}
