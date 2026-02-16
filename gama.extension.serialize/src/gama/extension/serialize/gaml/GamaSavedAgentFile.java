/*******************************************************************************************************
 *
 * GamaSavedAgentFile.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.gaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import gama.api.constants.IKeyword;
import gama.api.constants.ISerialisationConstants;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GamaFile;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.geometry.IEnvelope;
import gama.annotations.doc;
import gama.annotations.file;
import gama.annotations.support.IConcept;

/**
 * The Class GamaSavedSimulationFile.
 */
@file (
		name = IKeyword.AGENT,
		extensions = { IKeyword.AGENT },
		buffer_type = IType.LIST,
		buffer_content = IType.STRING,
		buffer_index = IType.INT,
		concept = { IConcept.FILE, IConcept.SAVE_FILE },
		doc = @doc ("Represents a saved agent file. The internal contents is a string at index 0 that contains the binary (bytes) representation of the agent"))
// TODO : this type needs to be improved ....
@SuppressWarnings ({ "unchecked" })
public class GamaSavedAgentFile extends GamaFile<IList<String>, String> implements ISerialisationConstants {

	/**
	 * Instantiates a new gama saved simulation file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc ("File containing a saved agent in the java binary serialisation protocol")
	public GamaSavedAgentFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, true);
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(Path.of(getPath(scope)));
			setBuffer(GamaListFactory.create(scope, Types.STRING, new String(bytes, STRING_BYTE_ARRAY_CHARSET)));
		} catch (IOException e) {
			setBuffer(GamaListFactory.create());
			throw GamaRuntimeException.create(e, scope);
		}

	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return null;
	}

}
