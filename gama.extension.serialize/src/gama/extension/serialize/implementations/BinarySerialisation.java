/*******************************************************************************************************
 *
 * BinarySerialisation.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.implementations;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import gama.core.common.interfaces.ISerialisationConstants;
import gama.core.common.util.FileUtils;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.SerialisedAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.ByteArrayZipper;

/**
 * The Class BinarySerialisationReader.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 31 oct. 2023
 */
public class BinarySerialisation implements ISerialisationConstants {

	/** The processor. */
	private static FSTBinaryProcessor PROCESSOR = new FSTBinaryProcessor();

	/**
	 * Creates an object or an agent from a file.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param path
	 *            the path
	 * @return the object
	 * @date 31 oct. 2023
	 */
	public static Object createFromFile(final IScope scope, final String path) {
		try {
			byte[] all = Files.readAllBytes(Path.of(FileUtils.constructAbsoluteFilePath(scope, path, true)));
			return createFromBytes(scope, all);
		} catch (IOException e) {
			throw GamaRuntimeException.create(e, GAMA.getRuntimeScope());
		}
	}

	/**
	 * Checks if is serialisation header.
	 *
	 * @param b
	 *            the b
	 * @return true, if is serialisation header
	 */
	private static boolean isSerialisationHeader(final byte b) {
		return b == GAMA_OBJECT_IDENTIFIER || b == GAMA_AGENT_IDENTIFIER;
	}

	/**
	 * Creates the from string.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param string
	 *            the string
	 * @return the object
	 * @date 31 oct. 2023
	 */
	public static Object createFromString(final IScope scope, final String string) {
		if (string == null || string.isBlank()) return null;
		try {
			byte[] all = string.getBytes(ISerialisationConstants.STRING_BYTE_ARRAY_CHARSET);
			return createFromBytes(scope, all);
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				return createFromFile(scope, string);
			} catch (Throwable ex) {
				throw GamaRuntimeException.create(ex, scope);
			}
		}
	}

	/**
	 * Creates the from bytes.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param all
	 *            the all
	 * @return the object
	 * @date 31 oct. 2023
	 */
	public static Object createFromBytes(final IScope scope, final byte[] bytes) {
		byte type = bytes[0];
		if (!isSerialisationHeader(type)) throw GamaRuntimeException.error("Not a GAMA serialisation record", scope);
		boolean zip = bytes[1] == COMPRESSED;
		byte[] some = removeIdentifiers(bytes);
		if (zip) { some = ByteArrayZipper.unzip(some); }
		return type == GAMA_OBJECT_IDENTIFIER ? PROCESSOR.createObjectFromBytes(scope, some)
				: PROCESSOR.createAgentFromBytes(scope, some);
	}

	/**
	 * Restore from file.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the sim
	 * @date 8 août 2023
	 */
	public static void restoreFromFile(final IAgent agent, final String path) {
		try {
			byte[] all = Files.readAllBytes(Path.of(path));
			if (isSerialisationHeader(all[0])) { restoreFromBytes(agent, all); }
		} catch (IOException e) {
			throw GamaRuntimeException.create(e, agent.getScope());
		}
	}

	/**
	 * Restore from string.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the agent
	 * @param string
	 *            the string
	 * @date 8 août 2023
	 */
	public static void restoreFromString(final IAgent agent, final String string) {
		try {
			byte[] all = string.getBytes(ISerialisationConstants.STRING_BYTE_ARRAY_CHARSET);
			restoreFromBytes(agent, all);
		} catch (Throwable e) {
			e.printStackTrace();
			// The string is maybe a path ?
			try {
				restoreFromFile(agent, string);
			} catch (Throwable ex) {
				throw GamaRuntimeException.create(ex, agent.getScope());
			}
		}
	}

	/**
	 * Restore from bytes.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the sim
	 * @param bytes
	 *            the bytes
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @date 8 août 2023
	 */
	public static void restoreFromBytes(final IAgent sim, final byte[] bytes) throws IOException {
		if (bytes[0] != GAMA_AGENT_IDENTIFIER) throw new IOException("Not an agent serialisation record");
		boolean zip = bytes[1] == COMPRESSED;
		byte[] some = removeIdentifiers(bytes);
		if (zip) { some = ByteArrayZipper.unzip(some); }
		PROCESSOR.restoreAgentFromBytes(sim, some);
	}

	/**
	 * Save to file.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope of the current simulation
	 * @param o
	 *            the object to serialise
	 * @param path
	 *            the path of the file to which to save the serialisation
	 * @param format
	 *            the format of the serialisation ("json" or "binary")
	 * @param zip
	 *            whether to zip the result or not
	 * @param includingHistory
	 *            whether to include the "history" of the agent in the serialisation. Only applicables to simulations
	 * @date 31 oct. 2023
	 */
	public static final void saveToFile(final IScope scope, final Object o, final String path, final String format,
			final boolean zip, final boolean includingHistory) {
		try (FileOutputStream fos = new FileOutputStream(path, true)) {
			if (o instanceof SimulationAgent sim) {
				sim.setAttribute(SerialisedAgent.SERIALISE_HISTORY, includingHistory);
			}
			fos.write(saveToBytes(scope, o, zip));
			if (o instanceof SimulationAgent sim) { sim.setAttribute(SerialisedAgent.SERIALISE_HISTORY, false); }
		} catch (IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	/**
	 * Save to string.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return
	 * @date 8 août 2023
	 */
	public static final String saveToString(final IScope scope, final Object sim, final String format,
			final boolean zip) {
		return new String(saveToBytes(scope, sim, zip), STRING_BYTE_ARRAY_CHARSET);
	}

	/**
	 * Save to bytes.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the sim
	 * @return the string
	 * @date 21 août 2023
	 */
	public static final byte[] saveToBytes(final IScope scope, final Object object, final boolean zip) {
		byte[] toSave = object instanceof IAgent a ? PROCESSOR.saveAgentToBytes(scope, a)
				: PROCESSOR.saveObjectToBytes(scope, object);
		if (zip) { toSave = ByteArrayZipper.zip(toSave); }
		return addIdentifiers(toSave, object instanceof IAgent ? GAMA_AGENT_IDENTIFIER : GAMA_OBJECT_IDENTIFIER,
				zip ? COMPRESSED : UNCOMPRESSED);
	}

	/**
	 * Adds the byte.
	 *
	 * @param array
	 *            the array
	 * @param identifier
	 *            the identifier
	 * @param zip
	 *            the zip
	 * @return the byte[]
	 */
	private static byte[] addIdentifiers(final byte[] array, final byte identifier, final byte zip) {
		if (array == null) return new byte[] { identifier, zip };
		ByteBuffer buffer = ByteBuffer.allocate(array.length + 2);
		buffer.put(identifier);
		buffer.put(zip);
		buffer.put(array);
		return buffer.array();
	}

	/**
	 * Removes the identifiers.
	 *
	 * @param array
	 *            the tableau
	 * @return the byte[]
	 */
	public static byte[] removeIdentifiers(final byte[] array) {
		if (array == null || array.length < 2) return new byte[0];
		byte[] newArray = new byte[array.length - 2];
		System.arraycopy(array, 2, newArray, 0, newArray.length);
		return newArray;
	}

	/**
	 * Save to bytes.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @param zip
	 *            the zip
	 * @return the byte[]
	 * @date 29 déc. 2023
	 */
	public static final byte[] saveToBytes(final Object object, final boolean zip) {
		return saveToBytes(GAMA.getRuntimeScope(), object, zip);
	}

}
