/*******************************************************************************************************
 *
 * GamlFile.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.resource;

import java.io.File;

import org.eclipse.emf.common.util.URI;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.common.geometry.Envelope3D;
import gama.core.kernel.model.IModel;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.GamaFile;
import gama.core.util.file.GamlFileInfo;
import gama.gaml.compilation.GAML;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
public class GamlFile extends GamaFile<IList<IModel>, IModel> {

	/** The mymodel. */
	private IModel model;

	/** The alias name. */
	private final String aliasName;

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
		aliasName = "";

	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		return GamaListFactory.EMPTY_LIST;
	}

	/**
	 * Gets the experiments.
	 *
	 * @return the experiments
	 */
	@getter ("experiments")
	public IList<String> getExperiments(final IScope scope) {
		File file = getFile(scope);
		// TODO AD Verify the use of a 'file' URI.
		GamlFileInfo info = GAML.getInfo(URI.createFileURI(getFile(scope).getAbsolutePath()), file.lastModified());
		if (info != null) return GamaListFactory.wrap(Types.STRING, info.getExperiments());
		return GamaListFactory.EMPTY_LIST;
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
		File file = getFile(scope);
		GamlFileInfo info = GAML.getInfo(URI.createFileURI(getFile(scope).getAbsolutePath()), file.lastModified());
		if (info != null) return GamaListFactory.wrap(Types.STRING, info.getTags());
		return GamaListFactory.EMPTY_LIST;
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
		File file = getFile(scope);
		GamlFileInfo info = GAML.getInfo(URI.createFileURI(getFile(scope).getAbsolutePath()), file.lastModified());
		if (info != null) return GamaListFactory.wrap(Types.STRING, info.getUses());
		return GamaListFactory.EMPTY_LIST;
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
		File file = getFile(scope);
		GamlFileInfo info = GAML.getInfo(URI.createFileURI(getFile(scope).getAbsolutePath()), file.lastModified());
		if (info != null) return GamaListFactory.wrap(Types.STRING, info.getImports());
		return GamaListFactory.EMPTY_LIST;
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
		File file = getFile(scope);
		GamlFileInfo info = GAML.getInfo(URI.createFileURI(getFile(scope).getAbsolutePath()), file.lastModified());
		if (info != null) return info.isValid();
		return false; // If the file is not available, return false by default
	}

	/**
	 * @see gama.core.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		return null;
	}

}
