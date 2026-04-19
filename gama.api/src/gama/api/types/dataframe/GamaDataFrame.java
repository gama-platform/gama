/*******************************************************************************************************
 *
 * GamaDataFrame.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.dataframe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.dflib.DataFrame;
import org.dflib.Exp;
import org.dflib.Printers;
import org.dflib.print.Printer;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.utils.StringUtils;
import gama.api.utils.interfaces.IFieldMatrixProvider;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The primary implementation of {@link IDataFrame} for the GAMA platform.
 *
 * <p>
 * {@code GamaDataFrame} wraps a DFLib {@link DataFrame} and integrates with GAMA's type system. It provides tabular
 * data operations including loading/saving from multiple formats, filtering, joining, pivoting, and row/column
 * manipulation.
 * </p>
 *
 * <p>
 * <b>Do not instantiate directly.</b> Use {@link GamaDataFrameFactory} or the GAML operators instead.
 * </p>
 *
 * @author GAMA Team
 */
public class GamaDataFrame implements IDataFrame, IContainer<String, IList<Object>>, IFieldMatrixProvider {

	/** The underlying DFLib DataFrame. */
	private final DataFrame inner;

	/**
	 * Constructs a new GamaDataFrame wrapping a DFLib DataFrame.
	 *
	 * @param inner
	 *            the DFLib DataFrame to wrap
	 */
	GamaDataFrame(final DataFrame inner) {
		this.inner = inner;
	}

	// ========================= IDataFrame implementation =========================

	@Override
	public IList<String> getColumns() {
		final String[] cols = getInner().getColumnsIndex().toArray();
		return GamaListFactory.createWithoutCasting(Types.STRING, List.of(cols));
	}

	@Override
	public IList<IType> getColumnTypes() {
		final IList<IType> types = GamaListFactory.create(Types.TYPE);
		for (final String col : getInner().getColumnsIndex()) {
			types.add(Types.get(getInner().getColumn(col).getNominalType()));
		}
		return types;
	}

	@Override
	public int getRows() { return getInner().height(); }

	@Override
	public int getCols() { return getInner().width(); }

	@Override
	public IList<Object> getColumnValues(final String columnName) {
		final IList<Object> result = GamaListFactory.create(Types.NO_TYPE, getInner().height());
		for (int i = 0; i < getInner().height(); i++) { result.add(getInner().get(columnName, i)); }
		return result;
	}

	@Override
	public IList<Object> getRowValues(final int rowIndex) {
		final IList<Object> result = GamaListFactory.create(Types.NO_TYPE, getInner().width());
		final String[] cols = getInner().getColumnsIndex().toArray();
		for (final String col : cols) { result.add(getInner().get(col, rowIndex)); }
		return result;
	}

	@Override
	public Object getCellValue(final int rowIndex, final String columnName) {
		return getInner().get(columnName, rowIndex);
	}

	@Override
	public IType getContentType(final IScope scope) {
		var types = getColumnTypes();
		if (types.length(scope) == 0) return Types.NO_TYPE;
		IType common = types.removeFirst();
		for (var t : types) { common = common.findCommonSupertypeWith(t); }
		return common;
	}

	// ========================= IContainer =========================

	@Override
	public IDataFrame copy(final IScope scope) {
		return new GamaDataFrame(getInner());
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentType, final boolean copy) {
		return matrixValue(scope, contentType);
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentType, final IPoint size,
			final boolean copy) {
		return matrixValue(scope, contentType);
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
		final String[] cols = getInner().getColumnsIndex().toArray();
		for (int c = 0; c < cols.length; c++) {
			if (c > 0) { sb.append(','); }
			sb.append(StringUtils.toGaml(cols[c], includingBuiltIn));
		}
		sb.append("],[");
		for (int r = 0; r < getInner().height(); r++) {
			if (r > 0) { sb.append(','); }
			sb.append('[');
			for (int c = 0; c < cols.length; c++) {
				if (c > 0) { sb.append(','); }
				sb.append(StringUtils.toGaml(getInner().get(cols[c], r), includingBuiltIn));
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

	// ========================= Outgoing conversions =========================

	/**
	 * Converts the dataframe into a GAMA object matrix. The matrix has the same shape as the dataframe (one cell per
	 * row/column pair). Column names are lost during the conversion.
	 *
	 * @param scope
	 *            the execution scope
	 * @param df
	 *            the dataframe to convert
	 * @return a new {@link IMatrix} of {@code Object}
	 */
	public IMatrix matrixValue(final IScope scope, final IType contentType) {
		final int cols = getInner().width();
		final int rows = getInner().height();
		final IMatrix matrix = GamaMatrixFactory.create(cols, rows, contentType);
		final String[] colNames = getInner().getColumnsIndex().toArray();
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				matrix.set(scope, c, r, contentType.cast(scope, getInner().get(colNames[c], r), null, false));
			}
		}
		return matrix;
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
	@Override
	public IDataFrame mergeWith(final IDataFrame df2) {
		return new GamaDataFrame(getInner().vConcat(df2.getInner()));
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
	@Override
	public IDataFrame joinOnCommonCol(final IDataFrame df2, final String colName) {
		return new GamaDataFrame(getInner().innerJoin(df2.getInner()).on(colName).select());
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
	@Override
	public IDataFrame removeRowsWithEmptyValues(final String columnToCheck) {
		return new GamaDataFrame(getInner().rows(r -> {
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
	@Override
	public IDataFrame filterRows(final String columnToCheck, final Object valueToMatch) {
		return new GamaDataFrame(getInner().rows(r -> valueToMatch.equals(r.get(columnToCheck))).select());
	}

	/**
	 * Selects a subset of columns.
	 *
	 * @param df
	 *            the dataframe
	 * @param columns
	 *            the columns to select
	 * @return a new dataframe with only the selected columns
	 */
	@Override
	public IDataFrame selectColumns(final IList<String> columns) {
		return new GamaDataFrame(getInner().cols(columns.toArray(new String[0])).select());
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
	@Override
	public IDataFrame addColumn(final String columnName, final Object defaultValue) {
		return new GamaDataFrame(getInner().cols(columnName).merge(Exp.$val(defaultValue)));
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
	@Override
	public IDataFrame addRow(final IList<Object> values) {
		final String[] colNames = getInner().getColumnsIndex().toArray();
		final DataFrame newRow = DataFrame.foldByRow(colNames).of(values.toArray());
		return new GamaDataFrame(getInner().vConcat(newRow));
	}

	// ========================= Integer-based location (iloc) =========================
	//
	// These methods mimic pandas' DataFrame.iloc semantics
	// (https://pandas.pydata.org/pandas-docs/stable/reference/api/pandas.DataFrame.iloc.html):
	//
	// - A single integer selects a single row (negative indices count from the end, like Python).
	// - A list of integers selects multiple rows / columns and preserves the 2D dataframe shape.
	// - A (row, col) pair with two integers returns a scalar cell.
	// - Mixed (int, list) or (list, int) pairs return a 1D list of values, not a dataframe.
	// - A pair of lists returns a sub-dataframe.
	// - Out-of-bounds indices raise an IndexError-like GamaRuntimeException.

	/**
	 * Normalizes a possibly-negative pandas-style index against the axis size and checks bounds.
	 */
	private static int normalizeIloc(final IScope scope, final int idx, final int size, final String axis) {
		final int n = idx < 0 ? idx + size : idx;
		if (n < 0 || n >= size) throw GamaRuntimeException
				.error("iloc " + axis + " index out of bounds: " + idx + " (size " + size + ")", scope);
		return n;
	}

	/**
	 * Normalizes a list of pandas-style indices to a zero-based int[] with bounds checking.
	 */
	private static int[] normalizeIlocList(final IScope scope, final IList<Integer> indices, final int size,
			final String axis) {
		if (indices == null) throw GamaRuntimeException.error("iloc " + axis + " indices cannot be nil", scope);
		final int[] out = new int[indices.size()];
		for (int i = 0; i < out.length; i++) { out[i] = normalizeIloc(scope, indices.get(i), size, axis); }
		return out;
	}

	/**
	 * Pandas-style {@code df.iloc[row]}: returns the given row as a list of values. Negative indices are supported.
	 */
	@Override
	public IList<Object> ilocRow(final IScope scope, final int rowIndex) {
		final int r = normalizeIloc(scope, rowIndex, getInner().height(), "row");
		return getRowValues(r);
	}

	/**
	 * Pandas-style {@code df.iloc[row, col]}: returns a single cell value. Negative indices are supported on both axes.
	 */
	@Override
	public Object iloc(final IScope scope, final int rowIndex, final int colIndex) {
		final int r = normalizeIloc(scope, rowIndex, getInner().height(), "row");
		final int c = normalizeIloc(scope, colIndex, getInner().width(), "col");
		return getInner().get(getInner().getColumnsIndex().get(c), r);
	}

	/**
	 * Pandas-style {@code df.iloc[row, [cols]]}: returns the given row restricted to the selected columns, as a list of
	 * values (order matches the input column indices).
	 */
	@Override
	public IList<Object> iloc(final IScope scope, final int rowIndex, final IList<Integer> colIndices) {
		final int r = normalizeIloc(scope, rowIndex, getInner().height(), "row");
		final int[] cIdx = normalizeIlocList(scope, colIndices, getInner().width(), "col");
		final IList<Object> out = GamaListFactory.create(Types.NO_TYPE, cIdx.length);
		final org.dflib.Index idx = getInner().getColumnsIndex();
		for (final int c : cIdx) { out.add(getInner().get(idx.get(c), r)); }
		return out;
	}

	/**
	 * Pandas-style {@code df.iloc[[rows], col]}: returns the given column restricted to the selected rows, as a list of
	 * values (order matches the input row indices).
	 */
	@Override
	public IList<Object> iloc(final IScope scope, final IList<Integer> rowIndices, final int colIndex) {
		final int c = normalizeIloc(scope, colIndex, getInner().width(), "col");
		final int[] rIdx = normalizeIlocList(scope, rowIndices, getInner().height(), "row");
		final String colName = getInner().getColumnsIndex().get(c);
		final IList<Object> out = GamaListFactory.create(Types.NO_TYPE, rIdx.length);
		for (final int r : rIdx) { out.add(getInner().get(colName, r)); }
		return out;
	}

	/**
	 * Pandas-style {@code df.iloc[[rows]]}: returns a sub-dataframe containing the selected rows (all columns kept, in
	 * their original order). Row order matches the input indices (allowing reordering).
	 */
	@Override
	public IDataFrame ilocRows(final IScope scope, final IList<Integer> rowIndices) {
		final int[] idx = normalizeIlocList(scope, rowIndices, getInner().height(), "row");
		return new GamaDataFrame(getInner().rows(idx).select());
	}

	/**
	 * Pandas-style {@code df.iloc[[rows], [cols]]}: returns a sub-dataframe with the selected rows and columns, in the
	 * order of the input indices.
	 */
	@Override
	public IDataFrame iloc(final IScope scope, final IList<Integer> rowIndices, final IList<Integer> colIndices) {
		final int[] rIdx = normalizeIlocList(scope, rowIndices, getInner().height(), "row");
		final int[] cIdx = normalizeIlocList(scope, colIndices, getInner().width(), "col");
		return new GamaDataFrame(getInner().rows(rIdx).cols(cIdx).select());
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
	@Override
	public IDataFrame pivot(final String indexColumn, final String pivotColumn, final String valueColumn) {
		return pivot(indexColumn, pivotColumn, valueColumn, vals -> vals.isEmpty() ? null : vals.get(0));
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
	public IDataFrame pivot(final String indexColumn, final String pivotColumn, final String valueColumn,
			final Function<List<Object>, Object> aggregationFunction) {
		final DataFrame dfInner = getInner();

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

		return new GamaDataFrame(DataFrame.foldByRow(newCols.toArray(new String[0])).of(flat));
	}

	/**
	 * Pretty print.
	 *
	 * @param df
	 *            the df
	 * @param maxRows
	 *            the max rows
	 * @param maxCols
	 *            the max cols
	 * @param maxChars
	 *            the max chars
	 * @return the string
	 */
	public static String prettyPrint(final IDataFrame df, final int maxRows, final int maxCols, final int maxChars) {
		Printer printer = Printers.tabular(maxRows, maxCols, maxChars);
		return printer.print(df.getInner());
	}

	@Override
	public String toString() {
		return getInner().toString();
	}

	/**
	 * Gets the underlying DFLib DataFrame.
	 *
	 * @return the underlying DFLib DataFrame
	 */
	@Override
	public DataFrame getInner() { return inner; }

	/**
	 * IFieldMatrixProvider implementation.
	 */

	@Override
	public int getRows(final IScope scope) {
		return getInner().height();
	}

	@Override
	public int getCols(final IScope scope) {
		return getInner().width();
	}

	@Override
	public double[] getBand(final IScope scope, final int index) throws GamaRuntimeException {
		final DataFrame df = getInner();
		final int rows = df.height();
		final int cols = df.width();
		final String[] colNames = df.getColumnsIndex().toArray();
		final double[] band = new double[rows * cols];
		int i = 0;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) { band[i++] = Cast.asFloat(scope, df.get(colNames[c], r)); }
		}
		return band;
	}
}
