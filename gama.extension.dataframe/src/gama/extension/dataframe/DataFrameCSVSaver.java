/*******************************************************************************************************
 *
 * DataFrameCSVSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.dataframe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.csv.CSVFormat;
import org.dflib.csv.Csv;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.SaveOptions;
import gama.gaml.statements.save.CSVSaver;

/**
 * The Class DataFrameCSVSaver.
 */
public class DataFrameCSVSaver extends CSVSaver {

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param item
	 *            the item
	 * @param f
	 *            the f
	 * @param header
	 *            the header
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions saveOptions)
			throws GamaRuntimeException, IOException {

		// Dataframes are written directly through DFLib, which preserves column names and types.
		// The delimiter comes from the 'separator' facet, or the CSV_SEPARATOR preference if omitted.
		// The 'encoding' facet is honored here through a charset-aware Writer (DFLib writes UTF-8 to a File otherwise).
		final char del = resolveDelimiter(saveOptions);
		try (Writer w = new FileWriter(file, saveOptions.writeCharset(), false)) {
			Csv.saver().format(CSVFormat.DEFAULT.withDelimiter(del)).save(((IDataFrame) item.value(scope)).getInner(),
					w);
		}
	}

	/**
	 * Gets the data type.
	 *
	 * @return the data type
	 */
	@Override
	public IType getDataType() { return IDataframeConstants.TYPE; }

}
