/*******************************************************************************************************
 *
 * GamaSavedSimulationFile.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform.
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.gaml;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.IConcept;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.types.IType;

/**
 * The Class GamaSavedSimulationFile.
 */
@file (
		name = IKeyword.SIMULATION,
		extensions = { "gsim", IKeyword.SIMULATION },
		buffer_type = IType.LIST,
		buffer_content = IType.STRING,
		buffer_index = IType.INT,
		concept = { IConcept.FILE, IConcept.SAVE_FILE },
		doc = @doc ("Represents a saved simulation file. The internal contents is a string at index 0 that contains the binary (bytes) representation of the simulation"))
// TODO : this type needs to be improved ....
@SuppressWarnings ({ "unchecked" })
public class GamaSavedSimulationFile extends GamaSavedAgentFile {

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
	@doc ("File containing 	a saved simulation in the java binary serialisation protocol")
	public GamaSavedSimulationFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

}
