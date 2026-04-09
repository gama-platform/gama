/*******************************************************************************************************
 *
 * GamaDataframeFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.dataframe;

import java.util.ArrayList;
import java.util.List;

import org.dflib.DataFrame;

import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;

/**
 * A static factory for creating {@link GamaDataframe} instances.
 *
 * <p>
 * Provides creation methods from various sources (columns + data, CSV files, Excel files, JSON files) and casting from
 * other GAMA types.
 * </p>
 *
 * @author GAMA Team
 * @see GamaDataframe
 * @see IDataframe
 */
public class GamaDataframeFactory {

	/** Private constructor to prevent instantiation. */
	private GamaDataframeFactory() {}

	/**
	 * Creates an empty dataframe with the specified column names.
	 *
	 * @param columns
	 *            the column names
	 * @return a new empty dataframe
	 */
	public static GamaDataframe create(final String... columns) {
		return new GamaDataframe(DataFrame.foldByRow(columns).of());
	}

	/**
	 * Creates a dataframe from column names and row data.
	 *
	 * @param scope
	 *            the execution scope
	 * @param columns
	 *            the column names
	 * @param data
	 *            the row data (list of lists)
	 * @return a new dataframe
	 */
	public static GamaDataframe create(final IScope scope, final IList<String> columns,
			final IList<IList<Object>> data) {
		return GamaDataframe.create(scope, columns, data);
	}

	/**
	 * Creates a dataframe from a CSV file.
	 *
	 * @param file
	 *            the CSV file
	 * @param separator
	 *            the column separator
	 * @param header
	 *            whether the first row is a header
	 * @return a new dataframe
	 */
	public static GamaDataframe fromCSV(final IScope scope, final String path, final char separator,
			final boolean header, final String charset) {
		return GamaDataframe.fromCSV(scope, path, separator, header, charset);
	}

	/**
	 * Creates a dataframe from an Excel file (first sheet).
	 *
	 * @param scope
	 *            the execution scope
	 * @param path
	 *            the path to the Excel file
	 * @return a new dataframe
	 */
	public static GamaDataframe fromExcel(final IScope scope, final String path) {
		return GamaDataframe.fromExcelFile(scope, path);
	}

	/**
	 * Creates a dataframe from a JSON file.
	 *
	 * @param scope
	 *            the execution scope
	 * @param path
	 *            the path to the JSON file
	 * @return a new dataframe
	 */
	public static GamaDataframe fromJson(final IScope scope, final String path) {
		return GamaDataframe.fromJson(scope, path);
	}

	/**
	 * Wraps a DFLib DataFrame into a GamaDataframe.
	 *
	 * @param dataFrame
	 *            the DFLib DataFrame
	 * @return a new GamaDataframe wrapping it
	 */
	public static GamaDataframe wrap(final DataFrame dataFrame) {
		return new GamaDataframe(dataFrame);
	}

	/**
	 * Casts an arbitrary object to a GamaDataframe.
	 *
	 * <p>
	 * Conversion strategies:
	 * </p>
	 * <ul>
	 * <li><b>GamaDataframe:</b> returns it (or a copy)</li>
	 * <li><b>IMap&lt;String, IList&gt;:</b> treats keys as column names, values as column data</li>
	 * <li><b>IList&lt;IList&gt;:</b> treats first row as headers, remaining as data</li>
	 * </ul>
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to cast
	 * @param copy
	 *            whether to create a copy
	 * @return a GamaDataframe, or null if conversion is not possible
	 */
	@SuppressWarnings ("unchecked")
	public static GamaDataframe castToDataframe(final IScope scope, final Object obj, final boolean copy) {
		if (obj == null) return null;
		if (obj instanceof GamaDataframe gdf) return copy ? (GamaDataframe) gdf.copy(scope) : gdf;
		if (obj instanceof IDataframe idf) return copy ? (GamaDataframe) idf.copy(scope) : (GamaDataframe) idf;
		if (obj instanceof IMap<?, ?> map) return fromMap(scope, (IMap<String, IList<Object>>) map);
		if (obj instanceof IList<?> list) return fromList(scope, (IList<IList<Object>>) list);
		return null;
	}

	/**
	 * Creates a dataframe from a map where keys are column names and values are column data lists.
	 *
	 * @param scope
	 *            the execution scope
	 * @param map
	 *            the map to convert
	 * @return a new dataframe
	 */
	@SuppressWarnings ("unchecked")
	private static GamaDataframe fromMap(final IScope scope, final IMap<String, IList<Object>> map) {
		final List<String> colNames = new ArrayList<>(map.keySet());
		if (colNames.isEmpty()) return create(new String[0]);
		final int rowCount = map.get(colNames.get(0)).size();
		final Object[] flat = new Object[rowCount * colNames.size()];
		int idx = 0;
		for (int r = 0; r < rowCount; r++) {
			for (final String col : colNames) {
				final IList<Object> colData = map.get(col);
				flat[idx++] = r < colData.size() ? colData.get(r) : null;
			}
		}
		return new GamaDataframe(DataFrame.foldByRow(colNames.toArray(new String[0])).of(flat));
	}

	/**
	 * Creates a dataframe from a list of lists (first sub-list = column names, rest = row data).
	 *
	 * @param scope
	 *            the execution scope
	 * @param list
	 *            the list of lists
	 * @return a new dataframe
	 */
	@SuppressWarnings ("unchecked")
	private static GamaDataframe fromList(final IScope scope, final IList<IList<Object>> list) {
		if (list.isEmpty()) return create(new String[0]);
		// First row is headers
		final IList<Object> headers = list.get(0);
		final String[] colNames = new String[headers.size()];
		for (int i = 0; i < headers.size(); i++) { colNames[i] = String.valueOf(headers.get(i)); }
		// Remaining rows are data
		final Object[] flat = new Object[(list.size() - 1) * colNames.length];
		int idx = 0;
		for (int r = 1; r < list.size(); r++) {
			final IList<Object> row = list.get(r);
			for (int c = 0; c < colNames.length; c++) { flat[idx++] = c < row.size() ? row.get(c) : null; }
		}
		return new GamaDataframe(DataFrame.foldByRow(colNames).of(flat));
	}
}
