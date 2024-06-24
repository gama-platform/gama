/*******************************************************************************************************
 *
 * GraphSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
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

import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.BufferingController.BufferingStrategies;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.graph.writer.GraphExporters;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
		final var g = Cast.asGraph(scope, item);
		if (g != null) {
			if (exp == null) throw GamaRuntimeException.error("Format is not recognized ('" + saveOptions.type + "')", scope);
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
