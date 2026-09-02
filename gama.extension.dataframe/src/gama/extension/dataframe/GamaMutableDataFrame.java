/*******************************************************************************************************
 *
 * GamaMutableDataFrame.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.dataframe;

import java.util.HashSet;
import java.util.Set;

import org.dflib.DataFrame;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * A <b>mutable</b> view over an (immutable) {@link GamaDataFrame}, used as the buffer of a {@code dataframe_file}.
 *
 * <p>
 * {@link GamaDataFrame} is immutable: every operation returns a new dataframe. To let a file's contents be modified in
 * place (as GAML's {@code add}/{@code put}/{@code remove} statements expect), this class keeps a <em>swappable</em>
 * reference to the current dataframe: reads are delegated to it, and every write rebuilds it and reassigns the
 * reference. It <em>is</em> an {@link IDataFrame}, so it is directly usable as a dataframe and {@link #copy(IScope)}
 * hands back an immutable {@link GamaDataFrame} snapshot.
 * </p>
 *
 * <p>
 * Mutations are row-oriented: a "value" is a row, given either as a list of values (matching the column count) or as a
 * map (column name -&gt; value).
 * </p>
 *
 * @author GAMA Team
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class GamaMutableDataFrame implements IDataFrame, IContainer.Modifiable<String, Object, String, Object> {

	/** The current (immutable) dataframe backing this mutable view. */
	private IDataFrame current;

	/**
	 * Instantiates a new mutable dataframe. Package-private: use {@link GamaDataFrameFactory#mutable(IDataFrame)}.
	 *
	 * @param df
	 *            the dataframe to wrap
	 */
	GamaMutableDataFrame(final IDataFrame df) {
		current = df;
	}

	/**
	 * Returns the current underlying (immutable) dataframe.
	 *
	 * @return the current dataframe
	 */
	public IDataFrame getDataFrame() { return current; }

	// ========================= Read delegation =========================

	@Override
	public IList<String> getColumns() { return current.getColumns(); }

	@Override
	public int getRows() { return current.getRows(); }

	@Override
	public int getCols() { return current.getCols(); }

	@Override
	public IList<Object> getColumnValues(final String columnName) { return current.getColumnValues(columnName); }

	@Override
	public IList<IType> getColumnTypes() { return current.getColumnTypes(); }

	@Override
	public IList<Object> getRowValues(final int rowIndex) { return current.getRowValues(rowIndex); }

	@Override
	public Object getCellValue(final int rowIndex, final String columnName) {
		return current.getCellValue(rowIndex, columnName);
	}

	@Override
	public IType getContentType() { return current.getContentType(); }

	@Override
	public DataFrame getInner() { return current.getInner(); }

	@Override
	public IDataFrame copy(final IScope scope) {
		// Hand back an immutable snapshot so casting the file buffer to a plain dataframe yields a detached value.
		return current.copy(scope);
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentType, final boolean copy) {
		return current.matrixValue(scope, contentType, copy);
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentType, final gama.api.types.geometry.IPoint size,
			final boolean copy) {
		return current.matrixValue(scope, contentType, size, copy);
	}

	@Override
	public String stringValue(final IScope scope) { return current.stringValue(scope); }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) { return current.serializeToGaml(includingBuiltIn); }

	@Override
	public IJsonValue serializeToJson(final IJson json) { return current.serializeToJson(json); }

	@Override
	public IType<?> computeRuntimeType(final IScope scope) { return current.computeRuntimeType(scope); }

	@Override
	public IDataFrame pivot(final String indexColumn, final String pivotColumn, final String valueColumn) {
		return current.pivot(indexColumn, pivotColumn, valueColumn);
	}

	@Override
	public IDataFrame addColumn(final String columnName, final Object defaultValue) {
		return current.addColumn(columnName, defaultValue);
	}

	@Override
	public IDataFrame mergeWith(final IDataFrame df2) { return current.mergeWith(df2); }

	@Override
	public IDataFrame join(final IScope scope, final IDataFrame other, final IList<String> keyColumns,
			final String joinType) {
		return current.join(scope, other, keyColumns, joinType);
	}

	@Override
	public IDataFrame removeRowsWithEmptyValues(final String columnName) {
		return current.removeRowsWithEmptyValues(columnName);
	}

	@Override
	public IDataFrame addRow(final IList<Object> values) { return current.addRow(values); }

	@Override
	public IDataFrame selectColumns(final IList<String> columns2) { return current.selectColumns(columns2); }

	@Override
	public IDataFrame filterRows(final String columnName, final Object value) {
		return current.filterRows(columnName, value);
	}

	@Override
	public IList<Object> ilocRow(final IScope scope, final int rowIndex) { return current.ilocRow(scope, rowIndex); }

	@Override
	public Object iloc(final IScope scope, final int rowIndex, final int colIndex) {
		return current.iloc(scope, rowIndex, colIndex);
	}

	@Override
	public IList<Object> iloc(final IScope scope, final int rowIndex, final IList<Integer> colIndices) {
		return current.iloc(scope, rowIndex, colIndices);
	}

	@Override
	public IList<Object> iloc(final IScope scope, final IList<Integer> rowIndices, final int colIndex) {
		return current.iloc(scope, rowIndices, colIndex);
	}

	@Override
	public IDataFrame ilocRows(final IScope scope, final IList<Integer> rowIndices) {
		return current.ilocRows(scope, rowIndices);
	}

	@Override
	public IDataFrame iloc(final IScope scope, final IList<Integer> rowIndices, final IList<Integer> colIndices) {
		return current.iloc(scope, rowIndices, colIndices);
	}

	// ========================= Write operations (rebuild the underlying dataframe) =========================

	@Override
	public void addValue(final IScope scope, final Object value) {
		current = current.addRow(asRow(scope, value));
	}

	@Override
	public void addValues(final IScope scope, final Object index, final IContainer<?, ?> values) {
		if (values instanceof IDataFrame df) {
			current = current.mergeWith(df);
			return;
		}
		for (final Object row : values.iterable(scope)) { current = current.addRow(asRow(scope, row)); }
	}

	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final Object value) {
		final IList<IList> rows = rowsCopy();
		rows.add(normalizeIndex(scope, index, rows.size() + 1), asRow(scope, value));
		setRows(scope, rows);
	}

	@Override
	public void setValueAtIndex(final IScope scope, final Object index, final Object value) {
		final IList<IList> rows = rowsCopy();
		rows.set(normalizeIndex(scope, index, rows.size()), asRow(scope, value));
		setRows(scope, rows);
	}

	@Override
	public void setAllValues(final IScope scope, final Object value) {
		final IList<Object> row = asRow(scope, value);
		final IList<IList> rows = GamaListFactory.create(Types.LIST);
		for (int i = 0; i < current.getRows(); i++) { rows.add(row); }
		setRows(scope, rows);
	}

	@Override
	public void removeValue(final IScope scope, final Object value) {
		final IList<Object> target = asRow(scope, value);
		for (int i = 0; i < current.getRows(); i++) {
			if (target.equals(current.getRowValues(i))) {
				keepAllExcept(scope, Set.of(i));
				return;
			}
		}
	}

	@Override
	public void removeIndex(final IScope scope, final Object index) {
		keepAllExcept(scope, Set.of(normalizeIndex(scope, index, current.getRows())));
	}

	@Override
	public void removeIndexes(final IScope scope, final IContainer<?, ?> index) {
		final Set<Integer> drop = new HashSet<>();
		for (final Object o : index.iterable(scope)) { drop.add(normalizeIndex(scope, o, current.getRows())); }
		keepAllExcept(scope, drop);
	}

	@Override
	public void removeValues(final IScope scope, final IContainer<?, ?> values) {
		final Set<Integer> drop = new HashSet<>();
		for (final Object v : values.iterable(scope)) {
			final IList<Object> target = asRow(scope, v);
			for (int i = 0; i < current.getRows(); i++) {
				if (target.equals(current.getRowValues(i))) { drop.add(i); }
			}
		}
		keepAllExcept(scope, drop);
	}

	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		final IList<Object> target = asRow(scope, value);
		final Set<Integer> drop = new HashSet<>();
		for (int i = 0; i < current.getRows(); i++) {
			if (target.equals(current.getRowValues(i))) { drop.add(i); }
		}
		keepAllExcept(scope, drop);
	}

	// ========================= Helpers =========================

	/**
	 * Normalizes a row given either as a list of values (used as-is) or a map (column name -&gt; value, reordered to
	 * match the current columns).
	 */
	private IList<Object> asRow(final IScope scope, final Object value) {
		if (value instanceof IMap<?, ?> m) {
			final IList<Object> row = GamaListFactory.create(Types.NO_TYPE);
			for (final String col : current.getColumns()) { row.add(m.get(col)); }
			return row;
		}
		if (value instanceof IList<?> l) return (IList<Object>) l;
		throw GamaRuntimeException.error(
				"Only a list of values or a map (column::value) can be added as a dataframe row, got: " + value, scope);
	}

	/** Converts a possibly-negative, possibly-non-integer index to a bounds-checked int. */
	private int normalizeIndex(final IScope scope, final Object index, final int size) {
		int i = Cast.asInt(scope, index);
		if (i < 0) { i += size; }
		if (i < 0 || i >= size)
			throw GamaRuntimeException.error("Row index out of bounds: " + index + " (size " + size + ")", scope);
		return i;
	}

	/** Snapshots the current rows as a mutable list of row-value lists. */
	private IList<IList> rowsCopy() {
		final IList<IList> rows = GamaListFactory.create(Types.LIST);
		for (int i = 0; i < current.getRows(); i++) { rows.add(current.getRowValues(i)); }
		return rows;
	}

	/** Rebuilds {@link #current} from the current columns and the given rows. */
	private void setRows(final IScope scope, final IList<IList> rows) {
		current = GamaDataFrameFactory.create(scope, current.getColumns(), rows);
	}

	/** Rebuilds {@link #current} keeping every row whose index is not in {@code drop}. */
	private void keepAllExcept(final IScope scope, final Set<Integer> drop) {
		if (drop.isEmpty()) return;
		final IList<Integer> keep = GamaListFactory.create(Types.INT);
		for (int i = 0; i < current.getRows(); i++) { if (!drop.contains(i)) { keep.add(i); } }
		current = keep.isEmpty() ? GamaDataFrameFactory.create(current.getColumns()) : current.ilocRows(scope, keep);
	}

	@Override
	public String toString() {
		return current.toString();
	}
}
