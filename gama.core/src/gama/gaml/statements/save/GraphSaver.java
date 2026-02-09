/*******************************************************************************************************
 *
 * GraphSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.save;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.jgrapht.nio.GraphExporter;

import gama.api.data.factories.GamaGraphFactory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.SaveOptions;
import gama.core.util.graph.GraphExporters;

/**
 * The Class GraphSaver.
 */
public class GraphSaver extends AbstractSaver {

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param item
	 *            the item
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	@SuppressWarnings ("unchecked")
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions saveOptions) {
		GraphExporter<?, ?> exp = GraphExporters.getGraphWriter(saveOptions.type);
		final var g = GamaGraphFactory.createFrom(scope, item, null, false);
		if (g != null) {
			if (exp == null)
				throw GamaRuntimeException.error("Format is not recognized ('" + saveOptions.type + "')", scope);
			exp.exportGraph(g, file.getAbsoluteFile());
		}
	}

	/**
	 * Compute file types.
	 *
	 * @return the string[]
	 */
	@Override
	public Set<String> getFileTypes() { return GraphExporters.getAvailableWriters(); }

	@Override
	public IType getDataType() { return Types.GRAPH; }

	@Override
	protected Set<String> computeFileTypes() {
		// Let them be retrieved dynamically and not cached
		return Collections.EMPTY_SET;
	}
}
