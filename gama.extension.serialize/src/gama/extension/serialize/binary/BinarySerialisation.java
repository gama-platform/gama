/*******************************************************************************************************
 *
 * BinarySerialisation.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import static gama.core.common.util.FileUtils.constructAbsoluteFilePath;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.nustaq.serialization.FSTConfiguration;

import gama.core.common.interfaces.ISerialisationConstants;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.SerialisedAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;

/**
 * The Class BinarySerialisationReader.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 31 oct. 2023
 */
public class BinarySerialisation implements ISerialisationConstants {

	private static FSTConfiguration FST = FSTConfiguration.createDefaultConfiguration();

	/** The processor. */
	private static BinarySerialiser PROCESSOR = new BinarySerialiser();

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
			return createFromBytes(scope, readAllBytes(Path.of(constructAbsoluteFilePath(scope, path, true))));
		} catch (IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
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
			return createFromBytes(scope, string.getBytes(STRING_BYTE_ARRAY_CHARSET));
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
		return PROCESSOR.createObjectFromBytes(scope, bytes);
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
			restoreFromBytes(agent, Files.readAllBytes(Path.of(path)));
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
			restoreFromBytes(agent, string.getBytes(STRING_BYTE_ARRAY_CHARSET));
		} catch (Throwable e) {
			try {
				restoreFromFile(agent, string);
			} catch (Throwable ex) {
				GAMA.reportAndThrowIfNeeded(agent.getScope(), GamaRuntimeException.create(ex, agent.getScope()), true);
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
		PROCESSOR.restoreAgentFromBytes(sim, bytes);
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
	public static final void saveToFile(final IScope scope, final Object o, final String path,
			final boolean includingHistory) {
		try (OutputStream os = Files.newOutputStream(new File(path).toPath(), CREATE, WRITE, TRUNCATE_EXISTING)) {
			if (o instanceof SimulationAgent sim) {
				sim.setAttribute(SerialisedAgent.SERIALISE_HISTORY, includingHistory);
			}
			os.write(saveToBytes(scope, o));
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
	public static final String saveToString(final IScope scope, final Object sim) {
		return new String(saveToBytes(scope, sim), STRING_BYTE_ARRAY_CHARSET);
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
	public static final byte[] saveToBytes(final IScope scope, final Object object) {
		return PROCESSOR.saveObjectToBytes(scope, object);
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
	public static final byte[] saveToBytes(final Object object) {
		return saveToBytes(GAMA.getRuntimeScope(), object);
	}

}
