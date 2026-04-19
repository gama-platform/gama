/*******************************************************************************************************
 *
 * IDataFrame.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.dataframe;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.misc.IContainer;

/**
 * The interface for dataframe values in the GAMA platform.
 *
 * <p>
 * A dataframe is a tabular data structure with named columns. It is addressed by column name (String key) and contains
 * Object values. Dataframes support operations like filtering rows, selecting columns, joining, pivoting, and
 * import/export from CSV, Excel, and JSON.
 * </p>
 *
 * @author GAMA Team
 */
@vars ({ @variable (
		name = IDataFrame.COLUMNS,
		type = IType.LIST,
		of = IType.STRING,
		doc = { @doc ("Returns the list of column names of this dataframe") }),
		@variable (
				name = IDataFrame.ROWS,
				type = IType.INT,
				doc = { @doc ("Returns the number of rows of this dataframe") }),
		@variable (
				name = IDataFrame.COLS,
				type = IType.INT,
				doc = { @doc ("Returns the number of columns of this dataframe") }) })
public interface IDataFrame extends IContainer.Addressable<String, IList<Object>, String, Object> {

	/** Pseudo-variable name for the list of column names. */
	String COLUMNS = "columns";

	/** Pseudo-variable name for the number of rows. */
	String ROWS = "rows";

	/** Pseudo-variable name for the number of columns. */
	String COLS = "cols";

	/**
	 * Returns the list of column names.
	 *
	 * @return the column names as a list of strings
	 */
	@getter (COLUMNS)
	IList<String> getColumns();

	/**
	 * Returns the number of rows.
	 *
	 * @return the row count
	 */
	@getter (ROWS)
	int getRows();

	/**
	 * Returns the number of columns.
	 *
	 * @return the column count
	 */
	@getter (COLS)
	int getCols();

	/**
	 * Returns all values in a given column as a list.
	 *
	 * @param columnName
	 *            the column name
	 * @return the list of values
	 */
	IList<Object> getColumnValues(String columnName);

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i data frame
	 */
	@Override
	IDataFrame copy(final IScope scope);

	/**
	 * Returns the list of types of the columns
	 *
	 * @return
	 */
	IList<IType> getColumnTypes();

	/**
	 * Returns all values in a given row as a list.
	 *
	 * @param rowIndex
	 *            the row index
	 * @return the list of values
	 */
	IList<Object> getRowValues(int rowIndex);

	/**
	 * Returns a cell value at a given row and column.
	 *
	 * @param rowIndex
	 *            the row index
	 * @param columnName
	 *            the column name
	 * @return the cell value
	 */
	Object getCellValue(int rowIndex, String columnName);

	/**
	 * Returns the underlying DFLib DataFrame.
	 *
	 * @return the DFLib DataFrame
	 */
	org.dflib.DataFrame getInner();

	/**
	 * Gets the gaml type.
	 *
	 * @return the gaml type
	 */
	@Override
	default IContainerType<?> getGamlType() { return Types.DATAFRAME; }

	/**
	 * Tries to find a type common to all columns. If not possible will return NO_TYPE.
	 *
	 * @return
	 */
	IType getContentType(final IScope scope);

	/**
	 * Gets the.
	 *
	 * @param scope
	 *            the scope
	 * @param columnName
	 *            the column name
	 * @return the object
	 */
	@Override
	default Object get(final IScope scope, final String columnName) {
		return getColumnValues(columnName);
	}

	/**
	 * Gets the from indices list.
	 *
	 * @param scope
	 *            the scope
	 * @param indices
	 *            the indices
	 * @return the from indices list
	 */
	@Override
	default Object getFromIndicesList(final IScope scope, final IList<String> indices) {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, indices.get(0));
	}

	/**
	 * Contains key.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@Override
	default boolean containsKey(final IScope scope, final Object o) {
		if (o instanceof String s) return getColumns().contains(s);
		return false;
	}

	/**
	 * Contains.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@Override
	default boolean contains(final IScope scope, final Object o) {
		return containsKey(scope, o);
	}

	/**
	 * List value.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param copy
	 *            the copy
	 * @return the i list
	 */
	@Override
	default IList<IList<Object>> listValue(final IScope scope, final IType<?> contentType, final boolean copy) {
		final IList<IList<Object>> result = GamaListFactory.create(Types.LIST);
		for (int i = 0; i < getRows(); i++) { result.add(getRowValues(i)); }
		return result;
	}

	/**
	 * Map value.
	 *
	 * @param <D>
	 *            the generic type
	 * @param <C>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param keyType
	 *            the key type
	 * @param contentType
	 *            the content type
	 * @param copy
	 *            the copy
	 * @return the i map
	 */
	@Override
	default <D, C> IMap<C, D> mapValue(final IScope scope, final IType<C> keyType, final IType<D> contentType,
			final boolean copy) {
		final IMap result = GamaMapFactory.create(Types.STRING, Types.LIST);
		for (final String col : getColumns()) { result.put(col, getColumnValues(col)); }
		return result;
	}

	/**
	 * Iterable.
	 *
	 * @param scope
	 *            the scope
	 * @return the iterable<? extends I list< object>>
	 */
	@Override
	default Iterable<? extends IList<Object>> iterable(final IScope scope) {
		return listValue(scope, Types.NO_TYPE, false);
	}

	/**
	 * Length.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	@Override
	default int length(final IScope scope) {
		return getRows();
	}

	/**
	 * Checks if is empty.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is empty
	 */
	@Override
	default boolean isEmpty(final IScope scope) {
		return getRows() == 0;
	}

	/**
	 * First value.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 */
	@Override
	default IList<Object> firstValue(final IScope scope) {
		if (getRows() == 0) return null;
		return getRowValues(0);
	}

	/**
	 * Last value.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 */
	@Override
	default IList<Object> lastValue(final IScope scope) {
		if (getRows() == 0) return null;
		return getRowValues(getRows() - 1);
	}

	/**
	 * Any value.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 */
	@Override
	default IList<Object> anyValue(final IScope scope) {
		if (getRows() == 0) return null;
		final int i = scope.getRandom().between(0, getRows() - 1);
		return getRowValues(i);
	}

	/**
	 * Reverse.
	 *
	 * @param scope
	 *            the scope
	 * @return the i container
	 */
	@Override
	default IContainer<String, IList<Object>> reverse(final IScope scope) {
		return copy(scope);
	}

	/**
	 * @param indexColumn
	 * @param pivotColumn
	 * @param valueColumn
	 * @return
	 */
	IDataFrame pivot(String indexColumn, String pivotColumn, String valueColumn);

	/**
	 * @param scope
	 * @param rowIndices
	 * @param colIndices
	 * @return
	 */
	IDataFrame iloc(IScope scope, IList<Integer> rowIndices, IList<Integer> colIndices);

	/**
	 * @param scope
	 * @param rowIndices
	 * @return
	 */
	IDataFrame ilocRows(IScope scope, IList<Integer> rowIndices);

	/**
	 * @param columnName
	 * @param defaultValue
	 * @return
	 */
	IDataFrame addColumn(String columnName, Object defaultValue);

	/**
	 * @param df2
	 * @return
	 */
	IDataFrame mergeWith(IDataFrame df2);

	/**
	 * @param scope
	 * @param rowIndices
	 * @param colIndex
	 * @return
	 */
	IList<Object> iloc(IScope scope, IList<Integer> rowIndices, int colIndex);

	/**
	 * @param df2
	 * @param columnName
	 * @return
	 */
	IDataFrame joinOnCommonCol(IDataFrame df2, String columnName);

	/**
	 * @param columnName
	 * @return
	 */
	IDataFrame removeRowsWithEmptyValues(String columnName);

	/**
	 * @param scope
	 * @param rowIndex
	 * @param colIndices
	 * @return
	 */
	IList<Object> iloc(IScope scope, int rowIndex, IList<Integer> colIndices);

	/**
	 * @param scope
	 * @param rowIndex
	 * @return
	 */
	IList<Object> ilocRow(IScope scope, int rowIndex);

	/**
	 * @param scope
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 */
	Object iloc(IScope scope, int rowIndex, int colIndex);

	/**
	 * @param values
	 * @return
	 */
	IDataFrame addRow(IList<Object> values);

	/**
	 * @param columns2
	 * @return
	 */
	IDataFrame selectColumns(IList<String> columns2);

	/**
	 * @param columnName
	 * @param value
	 * @return
	 */
	IDataFrame filterRows(String columnName, Object value);
}
