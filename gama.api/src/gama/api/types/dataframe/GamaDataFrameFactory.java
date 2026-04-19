/*******************************************************************************************************
 *
 * GamaDataFrameFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
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
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.dflib.DataFrame;
import org.dflib.Series;
import org.dflib.csv.Csv;
import org.dflib.csv.CsvLoader;
import org.dflib.excel.Excel;
import org.dflib.jdbc.Jdbc;
import org.dflib.jdbc.connector.JdbcConnector;
import org.dflib.json.Json;
import org.dflib.parquet.Parquet;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IField;
import gama.api.types.matrix.IMatrix;
import gama.api.utils.files.FileUtils;

/**
 * A static factory for creating {@link GamaDataFrame} instances.
 *
 * <p>
 * Provides creation methods from various sources (columns + data, CSV files, Excel files, JSON files) and casting from
 * other GAMA types.
 * </p>
 *
 * @author GAMA Team
 * @see GamaDataFrame
 * @see IDataFrame
 */
public class GamaDataFrameFactory {

	/** Private constructor to prevent instantiation. */
	private GamaDataFrameFactory() {}

	/**
	 * Creates an empty dataframe with the specified column names.
	 *
	 * @param columns
	 *            the column names
	 * @return a new empty dataframe
	 */
	public static IDataFrame create(final String... columns) {
		return new GamaDataFrame(DataFrame.foldByRow(columns).of());
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
	public static IDataFrame create(final IScope scope, final IList<String> columns, final IList<IList> data) {

		if (columns == null || columns.isEmpty()) throw GamaRuntimeException.error("Columns cannot be empty", scope);

		final String[] colArray = columns.toArray(new String[0]);
		final int numCols = colArray.length;

		// Empty data = empty dataframe with correct column schema
		if (data == null || data.isEmpty()) {
			final Series<?>[] emptySeries = new Series<?>[numCols];
			for (int c = 0; c < numCols; c++) { emptySeries[c] = Series.of(); }
			return new GamaDataFrame(DataFrame.byColumn(colArray).of(emptySeries));
		}

		// Validate row width against column count
		final IList<?> firstRow = data.firstValue(scope);
		if (firstRow != null && firstRow.size() != numCols) throw GamaRuntimeException
				.error("Each row must have " + numCols + " values (got " + firstRow.size() + ")", scope);

		final int numRows = data.size();

		// Build one typed Series per column
		final Series<?>[] series = new Series<?>[numCols];
		for (int c = 0; c < numCols; c++) { series[c] = buildTypedSeriesFromRows(data, c, numRows); }
		return new GamaDataFrame(DataFrame.byColumn(colArray).of(series));
	}

	/**
	 * Extracts column {@code col} from a list-of-rows, infers its uniform Java type, and returns a typed DFLib
	 * {@link Series}. Mixed numeric types are promoted to {@code double}. Anything else falls back to
	 * {@code Series<Object>}.
	 */
	@SuppressWarnings ("unchecked")
	private static Series<?> buildTypedSeriesFromRows(final IList<IList> data, final int col, final int numRows) {
		// Collect raw values for this column position
		final Object[] vals = new Object[numRows];
		for (int r = 0; r < numRows; r++) {
			final IList<?> row = data.get(r);
			vals[r] = col < row.size() ? row.get(col) : null;
		}

		// Infer the common Java class across all non-null values
		final Class<?> type = inferColumnType(vals);

		if (type == Integer.class) {
			final int[] arr = new int[numRows];
			for (int r = 0; r < numRows; r++) { arr[r] = vals[r] instanceof Number n ? n.intValue() : 0; }
			return Series.ofInt(arr);
		}
		if (type == Long.class) {
			final long[] arr = new long[numRows];
			for (int r = 0; r < numRows; r++) { arr[r] = vals[r] instanceof Number n ? n.longValue() : 0L; }
			return Series.ofLong(arr);
		}
		if (type == Double.class || type == Float.class) {
			final double[] arr = new double[numRows];
			for (int r = 0; r < numRows; r++) { arr[r] = vals[r] instanceof Number n ? n.doubleValue() : 0.0; }
			return Series.ofDouble(arr);
		}
		if (type == Boolean.class) {
			final boolean[] arr = new boolean[numRows];
			for (int r = 0; r < numRows; r++) { arr[r] = Boolean.TRUE.equals(vals[r]); }
			return Series.ofBool(arr);
		}
		if (type == String.class) {
			final String[] arr = new String[numRows];
			for (int r = 0; r < numRows; r++) { arr[r] = vals[r] != null ? vals[r].toString() : null; }
			return Series.of(arr);
		}
		// Fallback: generic object series
		return Series.of(vals);
	}

	/**
	 * Infers the most specific uniform Java class across all non-null values. Integer + Double → Double (numeric
	 * promotion). Any other mismatch → Object.
	 */
	private static Class<?> inferColumnType(final Object[] vals) {
		Class<?> common = null;
		for (final Object v : vals) {
			if (v == null) { continue; }
			final Class<?> c = v.getClass();
			if (common == null) {
				common = c;
			} else if (common != c) {
				// Promote int ↔ double mixed columns to double
				if (common != Integer.class && common != Double.class && common != Float.class && common != Long.class
						|| c != Integer.class && c != Double.class && c != Float.class && c != Long.class)
					return Object.class;
				common = Double.class;
			}
		}
		return common == null ? Object.class : common;
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
	public static IDataFrame fromCSV(final IScope scope, final String path, final char separator, final boolean header,
			final String charset) {
		final File file = new File(FileUtils.constructAbsoluteFilePath(scope, path, true));
		final Charset cs = charset != null ? Charset.forName(charset) : StandardCharsets.UTF_8;
		CsvLoader loader = Csv.loader().format(CSVFormat.DEFAULT.withDelimiter(separator)).encoding(cs);
		if (!header) { loader = loader.generateHeader(); }
		return new GamaDataFrame(loader.load(file));
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
	public static IDataFrame fromExcel(final IScope scope, final String path) {
		final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, true);
		return new GamaDataFrame(Excel.loader().firstRowAsHeader().loadSheet(new java.io.File(resolvedPath), 0));
	}

	/**
	 * Creates a GamaDataFrame from a Parquet file.
	 *
	 * @param scope
	 *            the execution scope
	 * @param path
	 *            the file path (relative to the model or absolute)
	 * @return a new GamaDataFrame
	 */
	public static IDataFrame fromParquet(final IScope scope, final String path) {
		final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, true);
		return new GamaDataFrame(Parquet.loader().load(new File(resolvedPath)));
	}

	/**
	 * Creates a GamaDataFrame from a JSON file.
	 *
	 * @param scope
	 *            the execution scope
	 * @param path
	 *            the file path (relative to the model or absolute)
	 * @return a new GamaDataFrame
	 */
	public static IDataFrame fromJson(final IScope scope, final String path) {
		final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, true);
		return new GamaDataFrame(Json.loader().load(new File(resolvedPath)));
	}

	/**
	 * Creates a GamaDataFrame by loading a whole database table via JDBC.
	 *
	 * @param scope
	 *            the execution scope
	 * @param jdbcUrl
	 *            the JDBC URL (e.g. "jdbc:postgresql://host:5432/db", "jdbc:sqlite:/path/to/db")
	 * @param user
	 *            the database user (may be null)
	 * @param password
	 *            the database password (may be null)
	 * @param tableName
	 *            the table to load
	 * @return a new GamaDataFrame
	 */
	public static IDataFrame fromDatabaseTable(final IScope scope, final String jdbcUrl, final String user,
			final String password, final String tableName) {
		try {
			final JdbcConnector connector = buildJdbcConnector(jdbcUrl, user, password);
			return new GamaDataFrame(connector.tableLoader(tableName).load());
		} catch (final Exception e) {
			throw GamaRuntimeException
					.error("Failed to load table '" + tableName + "' from database: " + e.getMessage(), scope);
		}
	}

	/**
	 * Creates a GamaDataFrame by running a SQL query via JDBC.
	 *
	 * @param scope
	 *            the execution scope
	 * @param jdbcUrl
	 *            the JDBC URL
	 * @param user
	 *            the database user (may be null)
	 * @param password
	 *            the database password (may be null)
	 * @param sqlQuery
	 *            the SQL query (typically a SELECT)
	 * @return a new GamaDataFrame with the query result
	 */
	public static IDataFrame fromDatabaseQuery(final IScope scope, final String jdbcUrl, final String user,
			final String password, final String sqlQuery) {
		try {
			final JdbcConnector connector = buildJdbcConnector(jdbcUrl, user, password);
			return new GamaDataFrame(connector.sqlLoader(sqlQuery).load());
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to run SQL query on database: " + e.getMessage(), scope);
		}
	}

	/**
	 * Builds a DFLib JDBC connector from a URL and optional credentials.
	 */
	public static JdbcConnector buildJdbcConnector(final String jdbcUrl, final String user, final String password) {
		var builder = Jdbc.connector(jdbcUrl);
		if (user != null) { builder = builder.userName(user); }
		if (password != null) { builder = builder.password(password); }
		return builder.build();
	}

	/**
	 * Wraps a DFLib DataFrame into a GamaDataFrame.
	 *
	 * @param dataFrame
	 *            the DFLib DataFrame
	 * @return a new GamaDataFrame wrapping it
	 */
	public static IDataFrame wrap(final DataFrame dataFrame) {
		return new GamaDataFrame(dataFrame);
	}

	/**
	 * Casts an arbitrary object to a GamaDataFrame.
	 *
	 * <p>
	 * Conversion strategies:
	 * </p>
	 * <ul>
	 * <li><b>GamaDataFrame:</b> returns it (or a copy)</li>
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
	 * @return a GamaDataFrame, or null if conversion is not possible
	 */
	@SuppressWarnings ("unchecked")
	public static IDataFrame castToDataframe(final IScope scope, final Object obj, final boolean copy) {
		return switch (obj) {
			case null -> null;
			case IDataFrame idf -> copy ? idf.copy(scope) : idf;
			case IMap<?, ?> map -> fromMap(scope, (IMap<String, IList<Object>>) map);
			case IList<?> list -> fromList(scope, (IList<IList<Object>>) list);
			case IField field -> fromField(scope, field);
			case IMatrix<?> matrix -> fromMatrix(scope, matrix);
			default -> null;
		};
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
	private static IDataFrame fromMap(final IScope scope, final IMap<String, IList<Object>> map) {
		final List<String> colNames = new ArrayList<>(map.keySet());
		if (colNames.isEmpty()) return create();
		final int rowCount = map.get(colNames.get(0)).size();
		final Object[] flat = new Object[rowCount * colNames.size()];
		int idx = 0;
		for (int r = 0; r < rowCount; r++) {
			for (final String col : colNames) {
				final IList<Object> colData = map.get(col);
				flat[idx++] = r < colData.size() ? colData.get(r) : null;
			}
		}
		return new GamaDataFrame(DataFrame.foldByRow(colNames.toArray(new String[0])).of(flat));
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
	private static IDataFrame fromList(final IScope scope, final IList<IList<Object>> list) {
		if (list.isEmpty()) return create();
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
		return new GamaDataFrame(DataFrame.foldByRow(colNames).of(flat));
	}

	/**
	 * Creates a dataframe from a GAMA matrix. Each matrix column becomes a dataframe column named "col0", "col1", ...
	 *
	 * @param scope
	 *            the execution scope
	 * @param matrix
	 *            the source matrix
	 * @return a new dataframe
	 */
	public static IDataFrame fromMatrix(final IScope scope, final IMatrix<?> matrix) {
		if (matrix == null) throw GamaRuntimeException.error("Cannot build a dataframe from a nil matrix", scope);
		final int cols = matrix.getCols(scope);
		final int rows = matrix.getRows(scope);
		if (cols == 0 || rows == 0)
			throw GamaRuntimeException.error("Cannot build a dataframe from an empty matrix", scope);

		// Determine the content type of the matrix to build typed DFLib Series per column.
		final IType<?> contentType = matrix.computeRuntimeType(scope).getContentType();
		final int typeId = contentType == null ? IType.NONE : contentType.id();

		final String[] colNames = new String[cols];
		for (int c = 0; c < cols; c++) { colNames[c] = "col" + c; }

		final Series<?>[] series = new Series<?>[cols];
		for (int c = 0; c < cols; c++) { series[c] = buildTypedSeries(scope, matrix, c, rows, typeId); }
		return new GamaDataFrame(DataFrame.byColumn(colNames).of(series));
	}

	/**
	 * Builds a typed DFLib {@link Series} for column {@code col} of the given matrix. The series type mirrors the
	 * matrix content type so that {@link #getColumnTypes()} returns accurate results.
	 */
	private static Series<?> buildTypedSeries(final IScope scope, final IMatrix<?> matrix, final int col,
			final int rows, final int typeId) {
		switch (typeId) {
			case IType.INT: {
				final int[] arr = new int[rows];
				for (int r = 0; r < rows; r++) {
					final Object v = matrix.get(scope, col, r);
					arr[r] = v instanceof Number n ? n.intValue() : 0;
				}
				return Series.ofInt(arr);
			}
			case IType.FLOAT: {
				final double[] arr = new double[rows];
				for (int r = 0; r < rows; r++) {
					final Object v = matrix.get(scope, col, r);
					arr[r] = v instanceof Number n ? n.doubleValue() : 0.0;
				}
				return Series.ofDouble(arr);
			}
			case IType.BOOL: {
				final boolean[] arr = new boolean[rows];
				for (int r = 0; r < rows; r++) {
					final Object v = matrix.get(scope, col, r);
					arr[r] = Boolean.TRUE.equals(v);
				}
				return Series.ofBool(arr);
			}
			case IType.STRING: {
				final String[] arr = new String[rows];
				for (int r = 0; r < rows; r++) {
					final Object v = matrix.get(scope, col, r);
					arr[r] = v == null ? null : v.toString();
				}
				return Series.of(arr);
			}
			default: {
				final Object[] arr = new Object[rows];
				for (int r = 0; r < rows; r++) { arr[r] = matrix.get(scope, col, r); }
				return Series.of(arr);
			}
		}
	}

	/**
	 * Creates a dataframe from a GAMA field. Each field column becomes a dataframe column named "col0", "col1", ...
	 *
	 * @param scope
	 *            the execution scope
	 * @param field
	 *            the source field
	 * @return a new dataframe
	 */
	public static IDataFrame fromField(final IScope scope, final IField field) {
		if (field == null) throw GamaRuntimeException.error("Cannot build a dataframe from a nil field", scope);
		return fromMatrix(scope, field);
	}
}
