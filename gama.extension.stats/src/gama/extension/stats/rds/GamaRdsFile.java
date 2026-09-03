/*******************************************************************************************************
 *
 * GamaRdsFile.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats.rds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.renjin.sexp.SEXP;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GamaFile;
import gama.api.utils.geometry.IEnvelope;
import se.alipsa.rdatautils.RDataUtil;

/**
 * A file type that reads and writes R RDS serialization files (.rds).
 */
@file (
		name = "rds",
		extensions = { "rds" },
		buffer_type = IType.CONTAINER,
		concept = { IConcept.FILE, IConcept.STATISTIC },
		doc = @doc ("A file that reads and writes R RDS data serialization files (.rds)"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaRdsFile extends GamaFile<Object, Object> {

	/**
	 * Creates an rds_file object from a file path.
	 */
	@doc (
			value = "Reads an RDS file into a GAMA container (dataframe, matrix, map, or list depending on the RDS contents)",
			examples = { @example (
					value = "file f <- rds_file(\"data.rds\");",
					isExecutable = false) })
	public GamaRdsFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/**
	 * Stores a container object in memory to be saved later as an RDS file.
	 */
	@doc (
			value = "Stores a container object in an rds_file object in memory (it does not save it immediately). It can then be saved using the 'save' statement.",
			examples = { @example (
					value = "file f <- rds_file(\"out.rds\", my_dataframe);",
					isExecutable = false) })
	public GamaRdsFile(final IScope scope, final String pathName, final Object container) {
		super(scope, pathName, container);
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		File file = getFile(scope);
		if (!file.exists()) {
			throw GamaRuntimeException.error("RDS file does not exist: " + file.getAbsolutePath(), scope);
		}
		try (InputStream is = new FileInputStream(file)) {
			SEXP sexp = RDataUtil.read(is);
			Object converted = RdsGamaConverter.toGamaObject(scope, sexp);
			setBuffer(converted);
		} catch (IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		Object buffer = getBuffer();
		if (buffer == null) return;
		File file = getFile(scope);
		try (OutputStream os = new FileOutputStream(file)) {
			SEXP sexp = RdsGamaConverter.toSexp(scope, buffer);
			RDataUtil.write(sexp, os);
		} catch (IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return null;
	}
}
