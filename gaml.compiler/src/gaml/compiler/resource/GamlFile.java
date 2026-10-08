/*******************************************************************************************************
 *
 * GamlFile.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.resource;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GamaFile;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.files.IGamlFileInfo;
import gama.api.utils.geometry.IEnvelope;

/**
 * Written by drogoul Modified on 13 nov. 2011
 *
 * @todo Description
 *
 */
@vars ({ @variable (
		name = "experiments",
		type = IType.LIST,
		of = IType.STRING,
		doc = { @doc ("Returns a list containing the names of the experiments defined in this file. An empty list is returned if it does not define any experiment") }),
		@variable (
				name = "tags",
				type = IType.LIST,
				of = IType.STRING,
				doc = { @doc ("Returns a list containing the names of the tags defined in this file. An empty list is returned if it does not define any tag") }),
		@variable (
				name = "uses",
				type = IType.LIST,
				of = IType.STRING,
				doc = { @doc ("Returns a list containing the names of the files 'used' (read or written) in this model") }),
		@variable (
				name = "valid",
				type = IType.BOOL,
				doc = { @doc ("Returns true if this file is syntactically valid, false otherwise") }),
		@variable (
				name = "imports",
				type = IType.LIST,
				of = IType.STRING,
				doc = { @doc ("Returns a list containing the names of the models imported by this file. An empty list is returned if it does not import any model") }) })
@file (
		name = "gaml",
		extensions = { "gaml", "experiment" },
		buffer_type = IType.LIST,
		buffer_content = IType.SPECIES,
		buffer_index = IType.INT,
		concept = { IConcept.FILE },
		doc = @doc ("Represents GAML model files"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlFile extends GamaFile<IList<IModelSpecies>, IModelSpecies> {

	/**
	 * Instantiates a new gaml file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a gaml file (.gaml)",
			examples = { @example (
					value = "file f <- gaml_file(\"file.gaml\");",
					isExecutable = false) })
	public GamlFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		return GamaListFactory.getEmptyList();
	}

	/**
	 * Gets the experiments.
	 *
	 * @return the experiments
	 */
	@getter ("experiments")
	public IList<String> getExperiments(final IScope scope) {
		IGamlFileInfo info = GAML.getInfo(getFile(scope));
		if (info != null) return GamaListFactory.wrap(Types.STRING, info.getExperiments());
		return GamaListFactory.getEmptyList();
	}

	/**
	 * Gets the tags.
	 *
	 * @param scope
	 *            the scope
	 * @return the tags
	 */
	@getter ("tags")
	public IList<String> getTags(final IScope scope) {
		IGamlFileInfo info = GAML.getInfo(getFile(scope));
		if (info != null) return GamaListFactory.wrap(Types.STRING, info.getTags());
		return GamaListFactory.getEmptyList();
	}

	/**
	 * Gets the uses.
	 *
	 * @param scope
	 *            the scope
	 * @return the uses
	 */
	@getter ("uses")
	public IList<String> getUses(final IScope scope) {
		IGamlFileInfo info = GAML.getInfo(getFile(scope));
		if (info != null) return GamaListFactory.wrap(Types.STRING, info.getUses());
		return GamaListFactory.getEmptyList();
	}

	/**
	 * Gets the imports.
	 *
	 * @param scope
	 *            the scope
	 * @return the imports
	 */
	@getter ("imports")
	public IList<String> getImports(final IScope scope) {
		IGamlFileInfo info = GAML.getInfo(getFile(scope));
		if (info != null) return GamaListFactory.wrap(Types.STRING, info.getImports());
		return GamaListFactory.getEmptyList();
	}

	/**
	 * Checks if is valid.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter ("valid")
	public Boolean isValid(final IScope scope) {
		IGamlFileInfo info = GAML.getInfo(getFile(scope));
		if (info != null) return info.isValid();
		return false; // If the file is not available, return false by default
	}

	/**
	 * @see gama.core.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return null;
	}

}
