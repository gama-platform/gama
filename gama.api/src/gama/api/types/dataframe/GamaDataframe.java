/*******************************************************************************************************
 *
 * GamaDataframe.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.dataframe;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.dflib.DataFrame;
import org.dflib.Exp;
import org.dflib.Printers;
import org.dflib.csv.Csv;
import org.dflib.csv.CsvLoader;
import org.dflib.excel.Excel;
import org.dflib.json.Json;
import org.dflib.print.Printer;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.utils.StringUtils;
import gama.api.utils.files.FileUtils;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The primary implementation of {@link IDataframe} for the GAMA platform.
 *
 * <p>
 * {@code GamaDataframe} wraps a DFLib {@link DataFrame} and integrates with GAMA's type system. It provides tabular
 * data operations including loading/saving from multiple formats, filtering, joining, pivoting, and row/column
 * manipulation.
 * </p>
 *
 * <p>
 * <b>Do not instantiate directly.</b> Use {@link GamaDataframeFactory} or the GAML operators instead.
 * </p>
 *
 * @author GAMA Team
 */
public class GamaDataframe implements IDataframe, IContainer<String, IList<Object>> {

	/** The underlying DFLib DataFrame. */
	private final DataFrame inner;

	/**
	 * Constructs a new GamaDataframe wrapping a DFLib DataFrame.
	 *
	 * @param inner
	 *            the DFLib DataFrame to wrap
	 */
	GamaDataframe(final DataFrame inner) {
		this.inner = inner;
	}

	// ========================= IDataframe implementation =========================

	@Override
	public IList<String> getColumns() {
		final String[] cols = inner.getColumnsIndex().toArray();
		return GamaListFactory.createWithoutCasting(Types.STRING, List.of(cols));
	}

	@Override
	public int getRows() {
		return inner.height();
	}

	@Override
	public int getCols() {
		return inner.width();
	}

	@Override
	public IList<Object> getColumnValues(final String columnName) {
		final IList<Object> result = GamaListFactory.create(Types.NO_TYPE, inner.height());
		for (int i = 0; i < inner.height(); i++) { result.add(inner.get(columnName, i)); }
		return result;
	}

	@Override
	public IList<Object> getRowValues(final int rowIndex) {
		final IList<Object> result = GamaListFactory.create(Types.NO_TYPE, inner.width());
		final String[] cols = inner.getColumnsIndex().toArray();
		for (final String col : cols) { result.add(inner.get(col, rowIndex)); }
		return result;
	}

	@Override
	public Object getCellValue(final int rowIndex, final String columnName) {
		return inner.get(columnName, rowIndex);
	}

	@Override
	public DataFrame getInnerDataFrame() {
		return inner;
	}

	// ========================= IContainer =========================

	@Override
	public IDataframe copy(final IScope scope) {
		return new GamaDataframe(inner);
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentType, final boolean copy) {
		return listValue(scope, contentType, copy).matrixValue(scope, contentType, copy);
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentType, final IPoint size,
			final boolean copy) {
		return listValue(scope, contentType, copy).matrixValue(scope, contentType, size, copy);
	}

	// ========================= IValue =========================

	@Override
	public String stringValue(final IScope scope) {
		return serializeToGaml(false);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder();
		sb.append("dataframe([");
		final String[] cols = inner.getColumnsIndex().toArray();
		for (int c = 0; c < cols.length; c++) {
			if (c > 0) { sb.append(','); }
			sb.append(StringUtils.toGaml(cols[c], includingBuiltIn));
		}
		sb.append("],[");
		for (int r = 0; r < inner.height(); r++) {
			if (r > 0) { sb.append(','); }
			sb.append('[');
			for (int c = 0; c < cols.length; c++) {
				if (c > 0) { sb.append(','); }
				sb.append(StringUtils.toGaml(inner.get(cols[c], r), includingBuiltIn));
			}
			sb.append(']');
		}
		sb.append("])");
		return sb.toString();
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return mapValue(null, Types.STRING, Types.LIST, false).serializeToJson(json);
	}

	@Override
	public IType<?> computeRuntimeType(final IScope scope) {
		return Types.DATAFRAME;
	}

	@Override
	public int intValue(final IScope scope) {
		return getRows();
	}

	@Override
	public double floatValue(final IScope scope) {
		return getRows();
	}



	/**
	 * Creates a GamaDataframe from a CSV file.
	 *
	 * @param scope
	 *            the execution scope
	 * @param path
	 *            the file path (relative to the model or absolute)
	 * @param separator
	 *            the column separator character
	 * @param header
	 *            whether the first row is a header
	 * @param charset
	 *            the character encoding (e.g. "UTF-8", "ISO-8859-1"). If null, defaults to UTF-8.
	 * @return a new GamaDataframe
	 */
	public static GamaDataframe fromCSV(final IScope scope, final String path, final char separator,
			final boolean header, final String charset) {
		final File file = new File(FileUtils.constructAbsoluteFilePath(scope, path, true));
		final Charset cs = charset != null ? Charset.forName(charset) : StandardCharsets.UTF_8;
		CsvLoader loader = Csv.loader()
				.format(CSVFormat.DEFAULT.withDelimiter(separator))
				.encoding(cs);
		if (!header) { loader = loader.generateHeader(); }
		return new GamaDataframe(loader.load(file));
	}

	/**
	 * Creates a GamaDataframe from an Excel file (first sheet).
	 *
	 * @param scope
	 *            the execution scope
	 * @param path
	 *            the file path (relative to the model or absolute)
	 * @return a new GamaDataframe
	 */
	public static GamaDataframe fromExcelFile(final IScope scope, final String path) {
		final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, true);
		return new GamaDataframe(Excel.loader().load(resolvedPath).values().iterator().next());
	}

	/**
	 * Creates a GamaDataframe from a JSON file.
	 *
	 * @param scope
	 *            the execution scope
	 * @param path
	 *            the file path (relative to the model or absolute)
	 * @return a new GamaDataframe
	 */
	public static GamaDataframe fromJson(final IScope scope, final String path) {
		final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, true);
		return new GamaDataframe(Json.loader().load(new File(resolvedPath)));
	}

	/**
	 * Creates a GamaDataframe from column names and row data.
	 *
	 * @param scope
	 *            the execution scope
	 * @param columns
	 *            the column names
	 * @param data
	 *            the row data (list of lists)
	 * @return a new GamaDataframe
	 */
	public static GamaDataframe create(final IScope scope, final IList<String> columns,
			final IList<IList<Object>> data) {
		if (columns == null || columns.length(scope) == 0) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("Columns cannot be empty", scope), false);
		}
		if (data == null || data.length(scope) == 0) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("Data cannot be empty", scope), false);
		}
		if (data.firstValue(scope) != null && columns.length(scope) != data.firstValue(scope).length(scope)) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.error("Columns and data must have the same length", scope), false);
		}
		final String[] colArray = columns.toArray(new String[0]);
		final Object[] flat = new Object[data.size() * columns.size()];
		int i = 0;
		for (final IList<?> row : data) {
			for (final Object val : row) { flat[i++] = val; }
		}
		return new GamaDataframe(DataFrame.foldByRow(colArray).of(flat));
	}

	// ========================= Data manipulation operations =========================

	/**
	 * Vertically concatenates two dataframes.
	 *
	 * @param df1
	 *            the first dataframe
	 * @param df2
	 *            the second dataframe
	 * @return a new merged dataframe
	 */
	public static GamaDataframe mergeDataframes(final GamaDataframe df1, final GamaDataframe df2) {
		return new GamaDataframe(df1.inner.vConcat(df2.inner));
	}

	/**
	 * Inner joins two dataframes on a common column.
	 *
	 * @param df1
	 *            the first dataframe
	 * @param df2
	 *            the second dataframe
	 * @param colName
	 *            the column to join on
	 * @return a new joined dataframe
	 */
	public static GamaDataframe joinDataframesOnCommonCol(final GamaDataframe df1, final GamaDataframe df2,
			final String colName) {
		return new GamaDataframe(df1.inner.innerJoin(df2.inner).on(colName).select());
	}

	/**
	 * Removes rows where the specified column has empty or null values.
	 *
	 * @param df
	 *            the dataframe
	 * @param columnToCheck
	 *            the column to check
	 * @return a new filtered dataframe
	 */
	public static GamaDataframe removeRowsWithEmptyValues(final GamaDataframe df, final String columnToCheck) {
		return new GamaDataframe(df.inner.rows(r -> {
			final Object val = r.get(columnToCheck);
			return val != null && !val.toString().isBlank();
		}).select());
	}

	/**
	 * Filters rows where a column matches a specific value.
	 *
	 * @param df
	 *            the dataframe
	 * @param columnToCheck
	 *            the column to filter on
	 * @param valueToMatch
	 *            the value to match
	 * @return a new filtered dataframe
	 */
	public static GamaDataframe filterRows(final GamaDataframe df, final String columnToCheck,
			final Object valueToMatch) {
		return new GamaDataframe(
				df.inner.rows(r -> valueToMatch.equals(r.get(columnToCheck))).select());
	}
	
//	public static GamaDataframe filterRows(final IScope scope, final GamaDataframe df, final String columnToCheck,
//			final IExpression expressionToMatch) {
//		return new GamaDataframe(df.inner.rows(r -> expressionToMatch.))
//				stream(scope, c).filter(by(scope, eachName, filter)).toCollection(listLike(c));
//	}

	/**
	 * Selects a subset of columns.
	 *
	 * @param df
	 *            the dataframe
	 * @param columns
	 *            the columns to select
	 * @return a new dataframe with only the selected columns
	 */
	public static GamaDataframe selectColumns(final GamaDataframe df, final IList<String> columns) {
		return new GamaDataframe(df.inner.cols(columns.toArray(new String[0])).select());
	}

	/**
	 * Adds a column with a default value.
	 *
	 * @param df
	 *            the dataframe
	 * @param columnName
	 *            the new column name
	 * @param defaultValue
	 *            the default value for all rows
	 * @return a new dataframe with the added column
	 */
	public static GamaDataframe addColumn(final GamaDataframe df, final String columnName,
			final Object defaultValue) {
		return new GamaDataframe(df.inner.cols(columnName).merge(Exp.$val(defaultValue)));
	}

	/**
	 * Adds a row to the dataframe.
	 *
	 * @param df
	 *            the dataframe
	 * @param values
	 *            the row values (must match column count)
	 * @return a new dataframe with the added row
	 */
	public static GamaDataframe addRow(final GamaDataframe df, final IList<Object> values) {
		final String[] colNames = df.inner.getColumnsIndex().toArray();
		final DataFrame newRow = DataFrame.foldByRow(colNames).of(values.toArray());
		return new GamaDataframe(df.inner.vConcat(newRow));
	}

	// ========================= Save operations (scope-aware) =========================

	/**
	 * Saves the dataframe to a CSV file.
	 *
	 * @param scope
	 *            the execution scope
	 * @param df
	 *            the dataframe
	 * @param path
	 *            the output file path (relative to the model or absolute)
	 * @param separator
	 *            the column separator character
	 * @param charset
	 *            the character encoding (e.g. "UTF-8"). If null, defaults to UTF-8.
	 * @return true if saved successfully
	 */
	public static boolean saveCSV(final IScope scope, final GamaDataframe df, final String path,
			final char separator, final String charset) {
		try {
			final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, false);
			Csv.saver()
					.format(CSVFormat.DEFAULT.withDelimiter(separator))
					.save(df.inner, resolvedPath);
			return true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to save CSV file: " + path + " - " + e.getMessage(), scope);
		}
	}

	/**
	 * Saves the dataframe to an Excel file.
	 *
	 * @param scope
	 *            the execution scope
	 * @param df
	 *            the dataframe
	 * @param path
	 *            the output file path (relative to the model or absolute)
	 * @param sheetName
	 *            the sheet name
	 * @return true if saved successfully
	 */
	public static boolean saveExcelSheet(final IScope scope, final GamaDataframe df, final String path,
			final String sheetName) {
		try {
			final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, false);
			Excel.saver().saveSheet(df.inner, new File(resolvedPath), sheetName);
			return true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to save Excel file: " + path + " - " + e.getMessage(), scope);
		}
	}

	/**
	 * Saves the dataframe to a JSON file.
	 *
	 * @param scope
	 *            the execution scope
	 * @param df
	 *            the dataframe
	 * @param path
	 *            the output file path (relative to the model or absolute)
	 * @return true if saved successfully
	 */
	public static boolean saveJson(final IScope scope, final GamaDataframe df, final String path) {
		try {
			final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, false);
			Json.saver().save(df.inner, resolvedPath);
			return true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to save JSON file: " + path + " - " + e.getMessage(), scope);
		}
	}

	// ========================= Pivot operation =========================

	/**
	 * Pivots the dataframe using the first value as the aggregation function.
	 *
	 * @param df
	 *            the dataframe
	 * @param indexColumn
	 *            the column to use as row index
	 * @param pivotColumn
	 *            the column whose values become new column names
	 * @param valueColumn
	 *            the column containing the values
	 * @return a new pivoted dataframe
	 */
	public static GamaDataframe pivot(final GamaDataframe df, final String indexColumn, final String pivotColumn,
			final String valueColumn) {
		return pivot(df, indexColumn, pivotColumn, valueColumn, vals -> vals.isEmpty() ? null : vals.get(0));
	}

	/**
	 * Pivots the dataframe with a custom aggregation function.
	 *
	 * @param df
	 *            the dataframe
	 * @param indexColumn
	 *            the column to use as row index
	 * @param pivotColumn
	 *            the column whose values become new column names
	 * @param valueColumn
	 *            the column containing the values
	 * @param aggregationFunction
	 *            the function to aggregate values
	 * @return a new pivoted dataframe
	 */
	public static GamaDataframe pivot(final GamaDataframe df, final String indexColumn, final String pivotColumn,
			final String valueColumn, final Function<List<Object>, Object> aggregationFunction) {
		final DataFrame dfInner = df.inner;

		final List<Object> pivotValues = new ArrayList<>();
		final List<Object> indexValues = new ArrayList<>();
		for (int i = 0; i < dfInner.height(); i++) {
			final Object pv = dfInner.get(pivotColumn, i);
			final Object iv = dfInner.get(indexColumn, i);
			if (!pivotValues.contains(pv)) { pivotValues.add(pv); }
			if (!indexValues.contains(iv)) { indexValues.add(iv); }
		}

		final Map<Object, Map<Object, List<Object>>> grouped = new LinkedHashMap<>();
		for (final Object iv : indexValues) {
			final Map<Object, List<Object>> row = new LinkedHashMap<>();
			for (final Object pv : pivotValues) { row.put(pv, new ArrayList<>()); }
			grouped.put(iv, row);
		}
		for (int i = 0; i < dfInner.height(); i++) {
			final Object iv = dfInner.get(indexColumn, i);
			final Object pv = dfInner.get(pivotColumn, i);
			final Object val = dfInner.get(valueColumn, i);
			grouped.get(iv).get(pv).add(val);
		}

		final List<String> newCols = new ArrayList<>();
		newCols.add(indexColumn);
		for (final Object pv : pivotValues) { newCols.add(pv.toString()); }

		final Object[] flat = new Object[indexValues.size() * newCols.size()];
		int idx = 0;
		for (final Map.Entry<Object, Map<Object, List<Object>>> entry : grouped.entrySet()) {
			flat[idx++] = entry.getKey();
			for (final Object pv : pivotValues) { flat[idx++] = aggregationFunction.apply(entry.getValue().get(pv)); }
		}

		return new GamaDataframe(DataFrame.foldByRow(newCols.toArray(new String[0])).of(flat));
	}
	
	public static String prettyPrint(final IDataframe df, int maxRows, int maxCols, int maxChars) {
		Printer printer = Printers.tabular(maxRows, maxCols, maxChars); 
		return printer.print(df.getInnerDataFrame());	
	}

	@Override
	public String toString() {
		return inner.toString();
	}
}
