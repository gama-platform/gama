/*******************************************************************************************************
 *
 * ISerialisationConstants.java, in gama.serialize, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Interface defining constants used for serialization and deserialization of GAMA agents and simulations.
 * <p>
 * This interface provides constants for:
 * </p>
 * <ul>
 * <li>File format identifiers (JSON, binary)</li>
 * <li>File type identifiers (agent files, simulation files)</li>
 * <li>Compression flags</li>
 * <li>Object type identifiers used in binary serialization</li>
 * <li>Character encoding settings for byte array conversions</li>
 * </ul>
 * <p>
 * These constants are used by the serialization framework to save and restore agent states
 * and simulation snapshots, enabling features like simulation persistence and agent migration.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 21 août 2023
 */
public interface ISerialisationConstants {

	/** The serialisation string. */
	String SERIALISATION_STRING = "serialisation_string";

	/** The Constant CLASS_PREFIX. */
	String CLASS_PREFIX = "";

	/** The json format. */
	String JSON_FORMAT = "json";

	/** The binary format. */
	String BINARY_FORMAT = "binary";

	/** The agent format. */
	String AGENT_FILE = IKeyword.AGENT;

	/** The simulation formation. */
	String SIMULATION_FILE = IKeyword.SIMULATION;

	/** The file formats. */
	Set<String> FILE_FORMATS = Set.of(JSON_FORMAT, BINARY_FORMAT);

	/** The file types. */
	Set<String> FILE_TYPES = Set.of(AGENT_FILE, SIMULATION_FILE);

	/** The Constant NULL. */
	byte[] NULL = {};

	/** The Constant COMPRESSED. */
	byte COMPRESSED = 1;

	/** The Constant UNCOMPRESSED. */
	byte UNCOMPRESSED = 0;

	/** The Constant GAMA_IDENTIFIER. */
	byte GAMA_AGENT_IDENTIFIER = 42;

	/** The gama object identifier. */
	byte GAMA_OBJECT_IDENTIFIER = 43;

	/** The Constant STRING_BYTE_ARRAY_CHARSET. The Charset to use to save byte arrays in strings and reversely */
	Charset STRING_BYTE_ARRAY_CHARSET = StandardCharsets.ISO_8859_1;

}
