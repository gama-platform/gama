/*******************************************************************************************************
 *
 * GamaSavedAgentFile.java, in gama.serialize, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.gaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ISerialisationConstants;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.GamaFile;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
	public IContainerType<?> getGamlType() { return Types.FILE.of(Types.INT, Types.STRING); }

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
	public Envelope3D computeEnvelope(final IScope scope) {
		return null;
	}

}
