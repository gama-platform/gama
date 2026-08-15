/*******************************************************************************************************
 *
 * ExcelSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.save;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.dflib.DataFrame;
import org.dflib.excel.Excel;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.dataframe.IDataFrame;
import gama.api.types.map.IMap;
import gama.api.utils.files.SaveOptions;

/**
 * Saves data to an Excel workbook (.xlsx) through the 'save' statement. Dispatches on the type of the saved object: a
 * single dataframe is written to a one-sheet workbook, while a map of dataframes is written as a multi-sheet workbook
 * (the keys of the map are used as sheet names).
 *
 * <p>
 * Examples:
 * </p>
 *
 * <pre>
 * save my_dataframe to: "../results/output.xlsx" format: "xlsx";
 * save ["Summary"::df1, "Details"::df2] to: "../results/report.xlsx" format: "xlsx";
 * </pre>
 */
public class ExcelSaver extends AbstractSaver {

	/** The default sheet name used when saving a single dataframe. */
	private static final String DEFAULT_SHEET = "Sheet1";

	@SuppressWarnings ("unchecked")
	@Override
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions saveOptions)
			throws IOException {
		final Object value = item.value(scope);
		if (value instanceof IDataFrame df) {
			Excel.saveSheet(df.getInner(), file, DEFAULT_SHEET);
		} else if (value instanceof IMap) {
			final IMap<String, IDataFrame> sheets = (IMap<String, IDataFrame>) value;
			final Map<String, DataFrame> dfBySheet = new LinkedHashMap<>();
			for (final Map.Entry<String, IDataFrame> entry : sheets.entrySet()) {
				dfBySheet.put(String.valueOf(entry.getKey()), entry.getValue().getInner());
			}
			Excel.save(dfBySheet, file);
		} else
			throw GamaRuntimeException.error(
					"Saving to Excel expects a dataframe or a map of dataframes, but got " + item.getGamlType(), scope);
	}

	@Override
	public boolean handlesDataType(final IType request) {
		if (request == null) return false;
		return request.id() == IType.DATAFRAME
				|| request.id() == IType.MAP && request.getContentType().id() == IType.DATAFRAME;
	}

	@Override
	protected Set<String> computeFileTypes() {
		return Set.of("xlsx");
	}

}
