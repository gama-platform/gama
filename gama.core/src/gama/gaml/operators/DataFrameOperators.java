/*******************************************************************************************************
 *
 * DataFrameOperators.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.dflib.DataFrame;
import org.dflib.csv.Csv;
import org.dflib.excel.Excel;
import org.dflib.jdbc.connector.JdbcConnector;
import org.dflib.json.Json;
import org.dflib.parquet.Parquet;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.dataframe.GamaDataFrame;
import gama.api.types.dataframe.GamaDataFrameFactory;
import gama.api.types.dataframe.IDataFrame;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.files.FileUtils;
import gama.api.utils.prefs.GamaPreferences;

/**
 * Operators for manipulating dataframes in GAML.
 *
 * <p>
 * Provides operators for creating, loading, saving, querying, filtering, transforming, and combining tabular data
 * (dataframes). Dataframes are two-dimensional data structures with named columns and indexed rows, similar to tables
 * in databases or spreadsheets.
 * </p>
 *
 * @author GAMA Team
 */
public class DataFrameOperators {

	// ========================= Creation operators =========================

	/**
	 * Creates a dataframe from a list of column names and a list of row data.
	 *
	 * <p>
	 * Usage in GAML:
	 * </p>
	 *
	 * <pre>
	 * dataframe df &lt;- dataframe_with(["name", "age", "city"], [["Alice", 30, "Paris"], ["Bob", 25, "Lyon"]]);
	 * </pre>
	 */
	@operator (
			value = "dataframe_with",
			can_be_const = false,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME, IConcept.CONTAINER })
	@doc (
			value = "Creates a dataframe from a list of column names and a list of rows (each row is a list of values).",
			usages = { @usage (
					value = "Create a dataframe with column names and data rows",
					examples = { @example (
							value = "dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]])",
							isExecutable = false) }) })
	@test ("(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]])).rows = 2")
	@test ("(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]])).keys = [\"name\",\"age\"]")
	public static IDataFrame dataframeWith(final IScope scope, final IList<String> columns, final IList<IList> data) {
		return GamaDataFrameFactory.create(scope, columns, data);
	}

	// ========================= File loading operators =========================

	/**
	 * Loads a CSV file into a dataframe with default settings (default separator, header, UTF-8).
	 */
	@operator (
			value = "df_load_csv",
			can_be_const = false,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE, IConcept.CSV })
	@doc (
			value = "Loads a CSV file into a dataframe. The file path is relative to the model file. "
					+ "Uses comma as separator, assumes the first row is a header, and reads in UTF-8.",
			usages = { @usage (
					value = "Load a CSV file with default settings (default separator, with header, UTF-8)",
					examples = { @example (
							value = "dataframe df <- df_load_csv(\"../includes/data.csv\");",
							isExecutable = false) }) },
			see = { "df_load_csv_with", "df_load_excel", "df_load_json" })
	@no_test
	public static IDataFrame loadCsv(final IScope scope, final String path) {
		return GamaDataFrameFactory.fromCSV(scope, path,
				GamaPreferences.External.CSV_SEPARATOR.value(scope).toString().charAt(0), true, null);
	}

	/**
	 * Loads a CSV file into a dataframe with custom separator, header option, and charset.
	 */
	@operator (
			value = "df_load_csv_with",
			can_be_const = false,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE, IConcept.CSV })
	@doc (
			value = """
					Loads a CSV file into a dataframe with a custom separator, header option, and character encoding. \
					The separator is a string of length 1. The header flag indicates whether the first row contains column names. \
					The charset is a string like 'UTF-8' or 'ISO-8859-1'.""",
			usages = { @usage (
					value = "Load a semicolon-separated CSV file in ISO-8859-1 without header",
					examples = { @example (
							value = "dataframe df <- df_load_csv_with(\"../includes/data.csv\", \";\", false, \"ISO-8859-1\");",
							isExecutable = false) }) },
			see = { "df_load_csv", "df_load_excel", "df_load_json" })
	@no_test
	public static IDataFrame loadCsvWith(final IScope scope, final String path, final String separator,
			final Boolean header, final String charset) {
		if (separator == null || separator.length() != 1)
			throw GamaRuntimeException.error("Separator must be a single character, got: " + separator, scope);
		return GamaDataFrameFactory.fromCSV(scope, path, separator.charAt(0), header != null && header, charset);
	}

	/**
	 * Loads an Excel file into a dataframe (first sheet).
	 */
	@operator (
			value = "df_load_excel",
			can_be_const = false,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE })
	@doc (
			value = "Loads the first sheet of an Excel file (.xlsx) into a dataframe. "
					+ "The file path is relative to the model file.",
			usages = { @usage (
					value = "Load an Excel file",
					examples = { @example (
							value = "dataframe df <- df_load_excel(\"../includes/data.xlsx\");",
							isExecutable = false) }) },
			see = { "df_load_csv", "df_load_json", "df_save_excel" })
	@no_test
	public static IDataFrame loadExcel(final IScope scope, final String path) {
		return GamaDataFrameFactory.fromExcel(scope, path);
	}

	/**
	 * Loads a JSON file into a dataframe.
	 */
	@operator (
			value = "df_load_json",
			can_be_const = false,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE })
	@doc (
			value = "Loads a JSON file into a dataframe. The file path is relative to the model file. "
					+ "The JSON file must contain an array of objects with consistent keys.",
			usages = { @usage (
					value = "Load a JSON file",
					examples = { @example (
							value = "dataframe df <- df_load_json(\"../includes/data.json\");",
							isExecutable = false) }) },
			see = { "df_load_csv", "df_load_excel", "df_save_json" })
	@no_test
	public static IDataFrame loadJson(final IScope scope, final String path) {
		return GamaDataFrameFactory.fromJson(scope, path);
	}

	/**
	 * Loads a Parquet file into a dataframe.
	 */
	@operator (
			value = "df_load_parquet",
			can_be_const = false,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE })
	@doc (
			value = "Loads a Parquet file (.parquet) into a dataframe. The file path is relative to the model file.",
			usages = { @usage (
					value = "Load a Parquet file",
					examples = { @example (
							value = "dataframe df <- df_load_parquet(\"../includes/data.parquet\");",
							isExecutable = false) }) },
			see = { "df_save_parquet", "df_load_csv", "df_load_json" })
	@no_test
	public static IDataFrame loadParquet(final IScope scope, final String path) {
		return GamaDataFrameFactory.fromParquet(scope, path);
	}

	/**
	 * Loads a whole database table into a dataframe via JDBC.
	 */
	@operator (
			value = "df_load_table",
			can_be_const = false,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.DATABASE })
	@doc (
			value = """
					Loads a whole database table into a dataframe via JDBC. Arguments: the JDBC URL, the user, \
					the password, and the table name. Pass empty strings for user/password if the database does not \
					require credentials. The corresponding JDBC driver must be available on the classpath.""",
			usages = { @usage (
					value = "Load a PostgreSQL table",
					examples = { @example (
							value = "dataframe df <- df_load_table(\"jdbc:postgresql://localhost:5432/mydb\", \"user\", \"pwd\", \"people\");",
							isExecutable = false) }) },
			see = { "df_load_sql", "df_save_table" })
	@no_test
	public static IDataFrame loadTable(final IScope scope, final String jdbcUrl, final String user,
			final String password, final String tableName) {
		return GamaDataFrameFactory.fromDatabaseTable(scope, jdbcUrl, emptyToNull(user), emptyToNull(password),
				tableName);
	}

	/**
	 * Loads the result of a SQL query into a dataframe via JDBC.
	 */
	@operator (
			value = "df_load_sql",
			can_be_const = false,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.DATABASE })
	@doc (
			value = """
					Runs a SQL query on a database via JDBC and returns the result as a dataframe. \
					Arguments: the JDBC URL, the user, the password, and the SQL query. \
					Pass empty strings for user/password if the database does not require credentials. \
					The corresponding JDBC driver must be available on the classpath.""",
			usages = { @usage (
					value = "Run a SQL query on a PostgreSQL database",
					examples = { @example (
							value = "dataframe df <- df_load_sql(\"jdbc:postgresql://localhost:5432/mydb\", \"user\", \"pwd\", \"SELECT name, age FROM people WHERE age > 18\");",
							isExecutable = false) }) },
			see = { "df_load_table", "df_save_table" })
	@no_test
	public static IDataFrame loadSql(final IScope scope, final String jdbcUrl, final String user, final String password,
			final String sqlQuery) {
		return GamaDataFrameFactory.fromDatabaseQuery(scope, jdbcUrl, emptyToNull(user), emptyToNull(password),
				sqlQuery);
	}

	// ========================= Save operators =========================

	/**
	 * Saves a dataframe to a CSV file with default settings (comma separator). TODO WARNING: Should use the save
	 * statement
	 */
	@operator (
			value = "df_save_csv",
			can_be_const = false,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE, IConcept.CSV })
	@doc (
			value = "Saves a dataframe to a CSV file with comma separator. "
					+ "The file path is relative to the model file. Returns true on success.",
			usages = { @usage (
					value = "Save a dataframe to CSV",
					examples = { @example (
							value = "bool success <- df_save_csv(my_df, \"../results/output.csv\");",
							isExecutable = false) }) },
			see = { "df_save_csv_with", "df_save_excel", "df_save_json", "df_load_csv" })
	@no_test
	public static Boolean saveCsv(final IScope scope, final IDataFrame df, final String path) {
		return saveCsvWith(scope, df, path, ",", null);
	}

	/**
	 * Saves a dataframe to a CSV file with custom separator and charset. TODO WARNING: Should use the save statement
	 */
	@operator (
			value = "df_save_csv_with",
			can_be_const = false,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE, IConcept.CSV })
	@doc (
			value = """
					Saves a dataframe to a CSV file with a custom separator and character encoding. \
					The separator is a string of length 1. The charset is a string like 'UTF-8' or 'ISO-8859-1'. \
					The file path is relative to the model file. Returns true on success.""",
			usages = { @usage (
					value = "Save a dataframe to a semicolon-separated CSV in ISO-8859-1",
					examples = { @example (
							value = "bool success <- df_save_csv_with(my_df, \"../results/output.csv\", \";\", \"ISO-8859-1\");",
							isExecutable = false) }) },
			see = { "df_save_csv", "df_save_excel", "df_save_json", "df_load_csv_with" })
	@no_test
	public static Boolean saveCsvWith(final IScope scope, final IDataFrame df, final String path,
			final String separator, final String charset) {
		if (separator == null || separator.length() != 1)
			throw GamaRuntimeException.error("Separator must be a single character, got: " + separator, scope);

		try {
			final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, false);
			Csv.saver().format(CSVFormat.DEFAULT.withDelimiter(separator.charAt(0))).save(df.getInner(), resolvedPath);
			return true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to save CSV file: " + path + " - " + e.getMessage(), scope);
		}

	}

	/**
	 * Saves a dataframe to an Excel file. TODO WARNING: Should use the save statement
	 */
	@operator (
			value = "df_save_excel",
			can_be_const = false,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE })
	@doc (
			value = "Saves a dataframe to an Excel file (.xlsx) with the given sheet name. "
					+ "The file path is relative to the model file. Returns true on success.",
			usages = { @usage (
					value = "Save a dataframe to an Excel file",
					examples = { @example (
							value = "bool success <- df_save_excel(my_df, \"../results/output.xlsx\", \"Sheet1\");",
							isExecutable = false) }) },
			see = { "df_save_csv", "df_save_json", "df_load_excel" })
	@no_test
	public static Boolean saveExcel(final IScope scope, final IDataFrame df, final String path,
			final String sheetName) {
		try {
			final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, false);
			Excel.saver().createMissingDirs().saveSheet(df.getInner(), new File(resolvedPath), sheetName);
			return true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to save Excel file: " + path + " - " + e.getMessage(), scope);
		}
	}

	/**
	 * Saves multiple dataframes to a single Excel workbook, one per sheet. TODO WARNING: Should use the save statement
	 */
	@operator (
			value = "df_save_excel_sheets",
			can_be_const = false,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE })
	@doc (
			value = """
					Saves multiple dataframes to a single Excel workbook (.xlsx). The argument is a map whose keys \
					are sheet names and values are dataframes. All sheets are written in one pass; existing sheets \
					in the file that are not in the map are left untouched. Returns true on success.""",
			usages = { @usage (
					value = "Save two dataframes as two sheets in one workbook",
					examples = { @example (
							value = "bool ok <- df_save_excel_sheets([\"Summary\"::df1, \"Details\"::df2], \"../results/report.xlsx\");",
							isExecutable = false) }) },
			see = { "df_save_excel", "df_load_excel" })
	@no_test
	public static Boolean saveExcelSheets(final IScope scope, final IMap<String, IDataFrame> sheets,
			final String path) {
		try {
			final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, false);
			final Map<String, DataFrame> dfBySheet = new LinkedHashMap<>();
			for (final Map.Entry<String, IDataFrame> entry : sheets.entrySet()) {
				dfBySheet.put(entry.getKey(), entry.getValue().getInner());
			}
			Excel.saver().createMissingDirs().save(dfBySheet, new File(resolvedPath));
			return true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to save multi-sheet Excel file: " + path + " - " + e.getMessage(),
					scope);
		}
	}

	/**
	 * Saves a dataframe to a JSON file. TODO WARNING: Should use the save statement and use the JSON infrastructure...
	 */
	@operator (
			value = "df_save_json",
			can_be_const = false,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE })
	@doc (
			value = "Saves a dataframe to a JSON file. The file path is relative to the model file. Returns true on success.",
			usages = { @usage (
					value = "Save a dataframe to JSON",
					examples = { @example (
							value = "bool success <- df_save_json(my_df, \"../results/output.json\");",
							isExecutable = false) }) },
			see = { "df_save_csv", "df_save_excel", "df_load_json" })
	@no_test
	public static Boolean saveJson(final IScope scope, final IDataFrame df, final String path) {
		try {
			final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, false);
			Json.saver().save(df.getInner(), resolvedPath);
			return true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to save JSON file: " + path + " - " + e.getMessage(), scope);
		}
	}

	/**
	 * Saves a dataframe to a Parquet file. TODO WARNING: Should use the save statement
	 */
	@operator (
			value = "df_save_parquet",
			can_be_const = false,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE })
	@doc (
			value = "Saves a dataframe to a Parquet file (.parquet). The file path is relative to the model file. "
					+ "Missing parent directories are created automatically. Returns true on success.",
			usages = { @usage (
					value = "Save a dataframe to Parquet",
					examples = { @example (
							value = "bool success <- df_save_parquet(my_df, \"../results/output.parquet\");",
							isExecutable = false) }) },
			see = { "df_load_parquet", "df_save_csv", "df_save_json" })
	@no_test
	public static Boolean saveParquet(final IScope scope, final IDataFrame df, final String path) {
		try {
			final String resolvedPath = FileUtils.constructAbsoluteFilePath(scope, path, false);
			Parquet.saver().createMissingDirs().save(df.getInner(), new File(resolvedPath));
			return true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to save Parquet file: " + path + " - " + e.getMessage(), scope);
		}
	}

	/**
	 * Saves a dataframe to a database table via JDBC.
	 */
	@operator (
			value = "df_save_table",
			can_be_const = false,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.DATABASE })
	@doc (
			value = """
					Saves a dataframe to a database table via JDBC. Arguments: the dataframe, the JDBC URL, the user, \
					the password, and the destination table name. The table must already exist with a compatible schema. \
					Pass empty strings for user/password if the database does not require credentials. \
					The corresponding JDBC driver must be available on the classpath. Returns true on success.""",
			usages = { @usage (
					value = "Save a dataframe to a PostgreSQL table",
					examples = { @example (
							value = "bool ok <- df_save_table(my_df, \"jdbc:postgresql://localhost:5432/mydb\", \"user\", \"pwd\", \"people\");",
							isExecutable = false) }) },
			see = { "df_load_table", "df_load_sql" })
	@no_test
	public static Boolean saveTable(final IScope scope, final IDataFrame df, final String jdbcUrl, final String user,
			final String password, final String tableName) {
		try {
			final JdbcConnector connector = GamaDataFrameFactory.buildJdbcConnector(jdbcUrl, user, password);
			connector.tableSaver(tableName).save(df.getInner());
			return true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Failed to save dataframe to table '" + tableName + "': " + e.getMessage(),
					scope);
		}
	}

	/**
	 * Converts an empty string to null, used to make user/password arguments optional in GAML.
	 */
	private static String emptyToNull(final String s) {
		return s == null || s.isEmpty() ? null : s;
	}

	// ========================= Column/Row access operators =========================

	/**
	 * Returns the values of a column as a list.
	 */
	@operator (
			value = "df_column",
			can_be_const = true,
			content_type = IType.NONE,
			type = IType.LIST,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Returns all values of the given column as a list.",
			usages = { @usage (
					value = "Get all values from the 'name' column",
					examples = { @example (
							value = "list names <- df_column(my_df, \"name\");",
							isExecutable = false) }) },
			see = { "df_add_column" })
	@test ("df_column(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), \"name\") = [\"Alice\",\"Bob\"]")
	public static IList<Object> dfColumn(final IScope scope, final IDataFrame df, final String columnName) {
		return df.getColumnValues(columnName);
	}

	/**
	 * Returns the values of a row as a list.
	 */
	@operator (
			value = "df_row",
			can_be_const = true,
			content_type = IType.NONE,
			type = IType.LIST,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Returns all values of the row at the given index as a list.",
			usages = { @usage (
					value = "Get all values from the first row",
					examples = { @example (
							value = "list row_data <- df_row(my_df, 0);",
							isExecutable = false) }) },
			see = { "df_column", "df_cell" })
	@test ("df_row(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 0) = [\"Alice\",30]")
	public static IList<Object> dfRow(final IScope scope, final IDataFrame df, final Integer rowIndex) {
		if (rowIndex < 0 || rowIndex >= df.getRows())
			throw GamaRuntimeException.error("Row index out of bounds: " + rowIndex, scope);
		return df.getRowValues(rowIndex);
	}

	/**
	 * Returns a single cell value.
	 */
	@operator (
			value = "df_cell",
			can_be_const = true,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Returns the value at the specified row index and column name.",
			usages = { @usage (
					value = "Get a specific cell value",
					examples = { @example (
							value = "unknown val <- df_cell(my_df, 0, \"name\");",
							isExecutable = false) }) },
			see = { "df_column", "df_row" })
	@test ("df_cell(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 1, \"name\") = \"Bob\"")
	public static Object dfCell(final IScope scope, final IDataFrame df, final Integer rowIndex,
			final String columnName) {
		if (rowIndex < 0 || rowIndex >= df.getRows())
			throw GamaRuntimeException.error("Row index out of bounds: " + rowIndex, scope);
		return df.getCellValue(rowIndex, columnName);
	}

	// ========================= Filtering operators =========================

	/**
	 * Filters rows where a column matches a value.
	 */
	@operator (
			value = "df_filter",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME, IConcept.FILTER })
	@doc (
			value = "Returns a new dataframe containing only the rows where the specified column equals the given value.",
			usages = { @usage (
					value = "Filter rows where 'city' equals 'Paris'",
					examples = { @example (
							value = "dataframe df2 <- df_filter(my_df, \"city\", \"Paris\");",
							isExecutable = false) }) },
			see = { "df_remove_empty", "df_select_columns" })
	@test ("(df_filter(dataframe_with([\"name\",\"city\"], [[\"Alice\",\"Paris\"],[\"Bob\",\"Lyon\"],[\"Eve\",\"Paris\"]]), \"city\", \"Paris\")).rows = 2")
	public static IDataFrame dfFilter(final IScope scope, final IDataFrame df, final String columnName,
			final Object value) {
		return df.filterRows(columnName, value);
	}

	/**
	 * Removes rows where a column has empty or null values.
	 */
	@operator (
			value = "df_remove_empty",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME, IConcept.FILTER })
	@doc (
			value = "Returns a new dataframe with rows removed where the specified column has null or empty values.",
			usages = { @usage (
					value = "Remove rows with empty 'name' values",
					examples = { @example (
							value = "dataframe df2 <- df_remove_empty(my_df, \"name\");",
							isExecutable = false) }) },
			see = { "df_filter", "df_select_columns" })
	@test ("(df_remove_empty(dataframe_with([\"name\",\"email\"], [[\"Alice\",\"a@x\"],[\"Bob\",\"\"],[\"Charlie\",nil]]), \"email\")).rows = 1")
	public static IDataFrame dfRemoveEmpty(final IScope scope, final IDataFrame df, final String columnName) {
		return df.removeRowsWithEmptyValues(columnName);
	}

	/**
	 * Selects a subset of columns.
	 */
	@operator (
			value = "df_select_columns",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Returns a new dataframe containing only the specified columns.",
			usages = { @usage (
					value = "Select only 'name' and 'age' columns",
					examples = { @example (
							value = "dataframe df2 <- df_select_columns(my_df, [\"name\", \"age\"]);",
							isExecutable = false) }) },
			see = { "df_filter", "df_add_column" })
	@test ("(df_select_columns(dataframe_with([\"name\",\"age\",\"city\"], [[\"Alice\",30,\"Paris\"]]), [\"name\",\"city\"])).keys = [\"name\",\"city\"]")
	public static IDataFrame dfSelectColumns(final IScope scope, final IDataFrame df, final IList<String> columns) {
		return df.selectColumns(columns);
	}

	// ========================= Modification operators =========================

	/**
	 * Adds a column with a default value.
	 */
	@operator (
			value = "df_add_column",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Returns a new dataframe with an additional column filled with the given default value.",
			usages = { @usage (
					value = "Add a 'score' column with default value 0",
					examples = { @example (
							value = "dataframe df2 <- df_add_column(my_df, \"score\", 0);",
							isExecutable = false) }) },
			see = { "df_add_row", "df_select_columns" })
	@test ("(df_add_column(dataframe_with([\"name\"], [[\"Alice\"]]), \"score\", 0)).keys = [\"name\",\"score\"]")
	@test ("df_cell(df_add_column(dataframe_with([\"name\"], [[\"Alice\"]]), \"score\", 0), 0, \"score\") = 0")
	public static IDataFrame dfAddColumn(final IScope scope, final IDataFrame df, final String columnName,
			final Object defaultValue) {
		return df.addColumn(columnName, defaultValue);
	}

	/**
	 * Adds a row to a dataframe.
	 */
	@operator (
			value = "df_add_row",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Returns a new dataframe with an additional row. The values list must match the number of columns.",
			usages = { @usage (
					value = "Add a new row",
					examples = { @example (
							value = "dataframe df2 <- df_add_row(my_df, [\"Charlie\", 35, \"Marseille\"]);",
							isExecutable = false) }) },
			see = { "df_add_column", "df_merge" })
	@test ("(df_add_row(dataframe_with([\"name\",\"age\"], [[\"Alice\",30]]), [\"Bob\",25])).rows = 2")
	@test ("df_cell(df_add_row(dataframe_with([\"name\",\"age\"], [[\"Alice\",30]]), [\"Bob\",25]), 1, \"name\") = \"Bob\"")
	public static IDataFrame dfAddRow(final IScope scope, final IDataFrame df, final IList<Object> values) {
		return df.addRow(values);
	}

	// ========================= Combining operators =========================

	/**
	 * Vertically concatenates two dataframes.
	 */
	@operator (
			value = "df_merge",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Vertically concatenates two dataframes (appends rows of the second to the first). "
					+ "Both dataframes must have the same column structure.",
			usages = { @usage (
					value = "Merge two dataframes vertically",
					examples = { @example (
							value = "dataframe merged <- df_merge(df1, df2);",
							isExecutable = false) }) },
			see = { "df_join", "df_add_row" })
	@test ("(df_merge(dataframe_with([\"sensor\",\"value\"], [[\"temp\",22.5]]), dataframe_with([\"sensor\",\"value\"], [[\"temp\",23.1],[\"humidity\",60.0]]))).rows = 3")
	public static IDataFrame dfMerge(final IScope scope, final IDataFrame df1, final IDataFrame df2) {
		return df1.mergeWith(df2);
	}

	/**
	 * Inner joins two dataframes on a common column.
	 */
	@operator (
			value = "df_join",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Performs an inner join of two dataframes on a common column. "
					+ "Only rows with matching values in both dataframes are retained.",
			usages = { @usage (
					value = "Join two dataframes on the 'id' column",
					examples = { @example (
							value = "dataframe joined <- df_join(df_people, df_scores, \"id\");",
							isExecutable = false) }) },
			see = { "df_merge" })
	@test ("(df_join(dataframe_with([\"id\",\"name\"], [[1,\"Alice\"],[2,\"Bob\"],[3,\"Charlie\"]]), dataframe_with([\"id\",\"salary\"], [[1,55000],[2,48000]]), \"id\")).rows = 2")
	public static IDataFrame dfJoin(final IScope scope, final IDataFrame df1, final IDataFrame df2,
			final String columnName) {
		return df1.joinOnCommonCol(df2, columnName);
	}

	// ========================= Pivot operator =========================

	/**
	 * Pivots a dataframe.
	 */
	@operator (
			value = "df_pivot",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Pivots the dataframe: the index column becomes row labels, the pivot column values become new column names, "
					+ "and the value column provides the cell values. When multiple values exist for a combination, the first is kept.",
			usages = { @usage (
					value = "Pivot a sales dataframe",
					examples = { @example (
							value = "dataframe pivoted <- df_pivot(sales_df, \"product\", \"quarter\", \"revenue\");",
							isExecutable = false) }) },
			see = { "df_filter", "df_select_columns" })
	@test ("(df_pivot(dataframe_with([\"product\",\"quarter\",\"revenue\"], [[\"Widget\",\"Q1\",1000],[\"Widget\",\"Q2\",1500],[\"Gadget\",\"Q1\",800],[\"Gadget\",\"Q2\",950]]), \"product\", \"quarter\", \"revenue\")).rows = 2")
	public static IDataFrame dfPivot(final IScope scope, final IDataFrame df, final String indexColumn,
			final String pivotColumn, final String valueColumn) {
		return df.pivot(indexColumn, pivotColumn, valueColumn);
	}

	/**
	 * Df pretty print.
	 *
	 * @param scope
	 *            the scope
	 * @param df
	 *            the df
	 * @return the string
	 */
	@operator (
			value = "df_pretty_print",
			can_be_const = true,
			type = IType.STRING,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Creates a string representing the dataframe in a human readable format. The number of rows and columns is limited to 10 and the number of characters per cell to 50.")
	public static String dfPrettyPrint(final IScope scope, final IDataFrame df) {
		return GamaDataFrame.prettyPrint(df, 10, 10, 50);
	}

	/**
	 * Df pretty print.
	 *
	 * @param scope
	 *            the scope
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
	@operator (
			value = "df_pretty_print",
			can_be_const = true,
			type = IType.STRING,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Creates a string representing the dataframe in a human readable format. The maximum number of rows, columns and the number of characters per cell to print is defined by the parameters.")
	public static String dfPrettyPrint(final IScope scope, final IDataFrame df, final int maxRows, final int maxCols,
			final int maxChars) {
		return GamaDataFrame.prettyPrint(df, maxRows, maxCols, maxChars);
	}

	// ========================= iloc (integer location) =========================
	//
	// Pandas-style purely integer-position-based indexing. See:
	// https://pandas.pydata.org/pandas-docs/stable/reference/api/pandas.DataFrame.iloc.html
	//
	// Supported forms:
	// iloc(df, i) -> row i as a list of values (cf. df.iloc[i])
	// iloc(df, i, j) -> scalar cell value (cf. df.iloc[i, j])
	// iloc(df, i, [j,...]) -> row i restricted to given cols (cf. df.iloc[i, [j,...]])
	// iloc(df, [i,...], j) -> col j restricted to given rows (cf. df.iloc[[i,...], j])
	// iloc(df, [i,...]) -> sub-dataframe with given rows (cf. df.iloc[[i,...]])
	// iloc(df, [i,...], [j,...]) -> sub-dataframe (cf. df.iloc[[i,...], [j,...]])
	//
	// Negative indices are supported on both axes (Python-style: -1 = last element).

	/**
	 * Pandas-style {@code df.iloc[i]}: returns row i as a list of values.
	 */
	@operator (
			value = "iloc",
			can_be_const = true,
			content_type = IType.NONE,
			type = IType.LIST,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Pandas-style df.iloc[i]: returns the row at integer position i as a list of values. "
					+ "Negative indices are supported (-1 is the last row).",
			usages = { @usage (
					value = "Get the last row",
					examples = { @example (
							value = "list row <- iloc(my_df, -1);",
							isExecutable = false) }) },
			see = { "df_row", "df_cell" })
	@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 0) = [\"Alice\",30]")
	@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), -1) = [\"Bob\",25]")
	public static IList<Object> ilocRow(final IScope scope, final IDataFrame df, final Integer rowIndex) {
		return df.ilocRow(scope, rowIndex);
	}

	/**
	 * Pandas-style {@code df.iloc[i, j]}: returns a single cell value at the given integer (row, col) position.
	 */
	@operator (
			value = "iloc",
			can_be_const = true,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Pandas-style df.iloc[i, j]: returns the cell value at integer (row, col) position. "
					+ "Negative indices are supported on both axes.",
			usages = { @usage (
					value = "Get the cell at row 1, column 0",
					examples = { @example (
							value = "unknown v <- iloc(my_df, 1, 0);",
							isExecutable = false) }) },
			see = { "df_cell", "df_row", "df_column" })
	@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 1, 0) = \"Bob\"")
	@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 0, 1) = 30")
	@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), -1, -1) = 25")
	public static Object iloc(final IScope scope, final IDataFrame df, final Integer rowIndex, final Integer colIndex) {
		return df.iloc(scope, rowIndex, colIndex);
	}

	/**
	 * Pandas-style {@code df.iloc[i, [j, ...]]}: returns row i restricted to the selected columns, as a list.
	 */
	@operator (
			value = "iloc",
			can_be_const = true,
			content_type = IType.NONE,
			type = IType.LIST,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Pandas-style df.iloc[i, [j, ...]]: returns the values of row i taken from the selected columns, "
					+ "in the order of the input column indices. Negative indices are supported.",
			usages = { @usage (
					value = "Get cells [0] and [2] of row 1",
					examples = { @example (
							value = "list values <- iloc(my_df, 1, [0, 2]);",
							isExecutable = false) }) },
			see = { "df_row", "df_select_columns" })
	@test ("iloc(dataframe_with([\"a\",\"b\",\"c\"], [[1,2,3],[4,5,6]]), 1, [0,2]) = [4,6]")
	public static IList<Object> ilocRowCols(final IScope scope, final IDataFrame df, final Integer rowIndex,
			final IList<Integer> colIndices) {
		return df.iloc(scope, rowIndex, colIndices);
	}

	/**
	 * Pandas-style {@code df.iloc[[i, ...], j]}: returns column j restricted to the selected rows, as a list.
	 */
	@operator (
			value = "iloc",
			can_be_const = true,
			content_type = IType.NONE,
			type = IType.LIST,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Pandas-style df.iloc[[i, ...], j]: returns the values of column j taken from the selected rows, "
					+ "in the order of the input row indices. Negative indices are supported.",
			usages = { @usage (
					value = "Get column 1 values at rows 0 and 2",
					examples = { @example (
							value = "list values <- iloc(my_df, [0, 2], 1);",
							isExecutable = false) }) },
			see = { "df_column", "df_row" })
	@test ("iloc(dataframe_with([\"a\",\"b\",\"c\"], [[1,2,3],[4,5,6],[7,8,9]]), [0,2], 1) = [2,8]")
	public static IList<Object> ilocRowsCol(final IScope scope, final IDataFrame df, final IList<Integer> rowIndices,
			final Integer colIndex) {
		return df.iloc(scope, rowIndices, colIndex);
	}

	/**
	 * Pandas-style {@code df.iloc[[i, ...]]}: returns a sub-dataframe with the selected rows (all columns kept).
	 */
	@operator (
			value = "iloc",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Pandas-style df.iloc[[i, ...]]: returns a new dataframe containing only the rows at the given "
					+ "integer indices (in that order). All columns are kept. Negative indices are supported.",
			usages = { @usage (
					value = "Select rows 0 and 2",
					examples = { @example (
							value = "dataframe sub <- iloc(my_df, [0, 2]);",
							isExecutable = false) }) },
			see = { "df_row", "df_filter" })
	@test ("(iloc(dataframe_with([\"name\"], [[\"Alice\"],[\"Bob\"],[\"Eve\"]]), [0,2])).rows = 2")
	@test ("df_cell(iloc(dataframe_with([\"name\"], [[\"Alice\"],[\"Bob\"],[\"Eve\"]]), [0,2]), 1, \"name\") = \"Eve\"")
	public static IDataFrame ilocRows(final IScope scope, final IDataFrame df, final IList<Integer> rowIndices) {
		return df.ilocRows(scope, rowIndices);
	}

	/**
	 * Pandas-style {@code df.iloc[[i, ...], [j, ...]]}: returns a sub-dataframe with the selected rows and columns.
	 */
	@operator (
			value = "iloc",
			can_be_const = true,
			type = IType.DATAFRAME,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = """
					Pandas-style df.iloc[[i, ...], [j, ...]]: returns a new dataframe containing only the rows and \
					columns at the given integer indices, in the order of the input indices. Negative indices are \
					supported on both axes.""",
			usages = { @usage (
					value = "Select rows 0 and 2, columns 0 and 1",
					examples = { @example (
							value = "dataframe sub <- iloc(my_df, [0, 2], [0, 1]);",
							isExecutable = false) }) },
			see = { "df_select_columns", "df_filter" })
	@test ("(iloc(dataframe_with([\"a\",\"b\",\"c\"], [[1,2,3],[4,5,6]]), [0], [0,2])).keys = [\"a\",\"c\"]")
	@test ("df_cell(iloc(dataframe_with([\"a\",\"b\",\"c\"], [[1,2,3],[4,5,6]]), [1], [2]), 0, \"c\") = 6")
	public static IDataFrame iloc(final IScope scope, final IDataFrame df, final IList<Integer> rowIndices,
			final IList<Integer> colIndices) {
		return df.iloc(scope, rowIndices, colIndices);
	}

}
