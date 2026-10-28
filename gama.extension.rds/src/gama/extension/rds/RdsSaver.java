/*******************************************************************************************************
 *
 * RdsSaver.java, in gama.extension.rds, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.rds;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.renjin.sexp.SEXP;

import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.IGamaFile;
import gama.api.types.misc.IContainer;
import gama.api.utils.files.SaveOptions;
import gama.gaml.statements.save.AbstractSaver;

/** Save delegate shared by explicit-format and extension-inferred saves. */
public final class RdsSaver extends AbstractSaver {

	@Override
	public boolean handlesAttributes() { return true; }

	@Override
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions options)
			throws IOException {
		write(scope, item.value(scope), file, options.rewrite(), options.attributesToSave());
	}

	static void write(final IScope scope, final Object value, final File file, final boolean rewrite,
			final Object attributes) throws IOException {
		if (!rewrite && file.exists())
			throw RdsConverter.error(scope, "Appending is not supported; use rewrite: true or a new destination");
		final Object contents = value instanceof IGamaFile<?, ?> source ? source.getContents(scope) : value;
		if (!(contents instanceof IContainer<?, ?> container))
			throw RdsConverter.error(scope, "Expected a list, map, matrix, dataframe, or agent collection");
		final SEXP serialized;
		if (RdsAgents.isAgentCollection(contents)) serialized = RdsAgents.snapshot(scope, container, attributes);
		else {
			if (attributes != null) throw RdsConverter.error(scope, "attributes is only supported for agent snapshots");
			serialized = RdsConverter.toR(scope, contents);
		}
		RdsIO.write(serialized, file.toPath(), rewrite);
	}

	@Override
	protected Set<String> computeFileTypes() { return Set.of("rds"); }
}
