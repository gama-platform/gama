/*******************************************************************************************************
 *
 * GamaRdsFile.java, in gama.extension.rds, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.rds;

import java.io.IOException;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.gaml.symbols.Facets;
import gama.api.types.file.GamaFile;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.extension.dataframe.GamaDataFrameFactory;
import gama.extension.dataframe.IDataFrame;

/**
 * The root can be a list, map, matrix or dataframe. There is no nominal common interface for their
 * addressable/modifiable intersection; the checked intersection below enforces the GamaFile buffer contract.
 */
@file (
		name = "rds",
		extensions = { "rds" },
		buffer_type = IType.CONTAINER,
		concept = { IConcept.FILE },
		doc = @doc ("Reads and writes RDS serialization version 2, gzip-compressed or uncompressed. "
				+ "Export input in R with saveRDS(object, path, version = 2). Atomic vectors remain lists, including singletons."))
@SuppressWarnings ({ "rawtypes", "unchecked" })
public final class GamaRdsFile<C extends IContainer.Addressable & IContainer.Modifiable> extends GamaFile<C, Object> {

	@doc (
			value = "Opens an RDS version-2 file containing a list, map, matrix or dataframe.",
			examples = @example (value = "file f <- rds_file('data.rds');", isExecutable = false))
	public GamaRdsFile(final IScope scope, final String path) {
		super(scope, path);
	}

	@doc (
			value = "Stores a container in an RDS file object; use save to write it.",
			examples = @example (value = "file f <- rds_file('out.rds', [1, 2]); save f;", isExecutable = false))
	public GamaRdsFile(final IScope scope, final String path, final IContainer contents) {
		super(scope, path, (C) checkedBuffer(scope, contents));
	}

	private static IContainer.Addressable checkedBuffer(final IScope scope, final Object value) {
		final Object mutable = value instanceof IDataFrame frame ? GamaDataFrameFactory.mutable(frame) : value;
		if (!(mutable instanceof IList || mutable instanceof gama.api.types.map.IMap
				|| mutable instanceof gama.api.types.matrix.IMatrix || mutable instanceof IDataFrame)
				|| !(mutable instanceof IContainer.Addressable) || !(mutable instanceof IContainer.Modifiable))
			throw RdsConverter.error(scope, "The root must be a list, map, matrix or dataframe, not NULL or an unsupported value");
		return (IContainer.Addressable & IContainer.Modifiable) mutable;
	}

	@Override
	protected void fillBuffer(final IScope scope) {
		if (getBuffer() != null) return;
		try {
			setBuffer((C) checkedBuffer(scope, RdsConverter.fromR(scope, RdsIO.read(getFile(scope).toPath()))));
		} catch (final IOException e) {
			throw GamaRuntimeException.error("Cannot read RDS file '" + getPath(scope) + "': " + e.getMessage(), scope);
		}
	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) {
		try {
			final var rewrite = facets.getExpr(IKeyword.REWRITE);
			RdsSaver.write(scope, getContents(scope), getFile(scope),
					rewrite == null || Cast.asBool(scope, rewrite.value(scope)), facets.getExpr(IKeyword.ATTRIBUTES));
		} catch (final IOException e) {
			throw GamaRuntimeException.error("Cannot write RDS file '" + getPath(scope) + "': " + e.getMessage(), scope);
		}
	}

	@Override
	public void save(final IScope scope, final Facets facets) {
		// GamaFile.save deletes an existing destination before conversion; RDS writes replace only on success.
		if (!writable) throw RdsConverter.error(scope, "File " + getName(scope) + " is not writable");
		flushBuffer(scope, facets);
		if (isRemote()) sendToURL(scope);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		final Object contents = getContents(scope);
		return contents instanceof IDataFrame frame ? frame.getColumns() : super.getAttributes(scope);
	}
}
