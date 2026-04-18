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

import java.util.List;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.ITypeProvider;
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
	org.dflib.DataFrame getInnerDataFrame();
	

	@Override
	default IContainerType<?> getGamlType() { return Types.DATAFRAME; }
	
	/**
	 * Tries to find a type common to all columns. If not possible will return NO_TYPE.
	 * @return
	 */
	IType getContentType(final IScope scope);

	@Override
	default Object get(final IScope scope, final String columnName) {
		return getColumnValues(columnName);
	}

	@Override
	default Object getFromIndicesList(final IScope scope, final IList<String> indices) {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, indices.get(0));
	}

	@Override
	default boolean containsKey(final IScope scope, final Object o) {
		if (o instanceof String s) return getColumns().contains(s);
		return false;
	}

	@Override
	default boolean contains(final IScope scope, final Object o) {
		return containsKey(scope, o);
	}

	@Override
	default IList<IList<Object>> listValue(final IScope scope, final IType<?> contentType, final boolean copy) {
		final IList<IList<Object>> result = GamaListFactory.create(Types.LIST);
		for (int i = 0; i < getRows(); i++) { result.add(getRowValues(i)); }
		return result;
	}

	@Override
	default <D, C> IMap<C, D> mapValue(final IScope scope, final IType<C> keyType, final IType<D> contentType,
			final boolean copy) {
		final IMap result = GamaMapFactory.create(Types.STRING, Types.LIST);
		for (final String col : getColumns()) { result.put(col, getColumnValues(col)); }
		return result;
	}

	@Override
	default Iterable<? extends IList<Object>> iterable(final IScope scope) {
		return listValue(scope, Types.NO_TYPE, false);
	}

	@Override
	default int length(final IScope scope) {
		return getRows();
	}

	@Override
	default boolean isEmpty(final IScope scope) {
		return getRows() == 0;
	}

	@Override
	default IList<Object> firstValue(final IScope scope) {
		if (getRows() == 0) return null;
		return getRowValues(0);
	}

	@Override
	default IList<Object> lastValue(final IScope scope) {
		if (getRows() == 0) return null;
		return getRowValues(getRows() - 1);
	}

	@Override
	default IList<Object> anyValue(final IScope scope) {
		if (getRows() == 0) return null;
		final int i = scope.getRandom().between(0, getRows() - 1);
		return getRowValues(i);
	}

	@Override
	default IContainer<String, IList<Object>> reverse(final IScope scope) {
		return copy(scope);
	}
}
