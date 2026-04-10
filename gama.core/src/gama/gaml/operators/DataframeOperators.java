/*******************************************************************************************************
 *
 * DataframeOperators.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

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
import gama.api.types.dataframe.GamaDataframe;
import gama.api.types.dataframe.IDataframe;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IField;
import gama.api.types.matrix.IMatrix;
import gama.api.utils.prefs.GamaPreferences;

/**
 * Operators for manipulating dataframes in GAML.
 *
 * <p>
 * Provides operators for creating, loading, saving, querying, filtering, transforming, and combining tabular data
 * (dataframes). Dataframes are two-dimensional data structures with named columns and indexed rows, similar to tables in
 * databases or spreadsheets.
 * </p>
 *
 * @author GAMA Team
 */
public class DataframeOperators {

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
	@test ("df_rows(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]])) = 2")
	@test ("df_columns(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]])) = [\"name\",\"age\"]")
	public static GamaDataframe dataframeWith(final IScope scope, final IList<String> columns,
			final IList<IList<Object>> data) {
		return GamaDataframe.create(scope, columns, data);
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
	public static GamaDataframe loadCsv(final IScope scope, final String path) {
		return GamaDataframe.fromCSV(	scope, 
										path, 
										GamaPreferences.External.CSV_SEPARATOR.value(scope).toString().charAt(0), 
										true, 
										null);
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
			value = "Loads a CSV file into a dataframe with a custom separator, header option, and character encoding. "
					+ "The separator is a string of length 1. The header flag indicates whether the first row contains column names. "
					+ "The charset is a string like 'UTF-8' or 'ISO-8859-1'.",
			usages = { @usage (
					value = "Load a semicolon-separated CSV file in ISO-8859-1 without header",
					examples = { @example (
							value = "dataframe df <- df_load_csv_with(\"../includes/data.csv\", \";\", false, \"ISO-8859-1\");",
							isExecutable = false) }) },
			see = { "df_load_csv", "df_load_excel", "df_load_json" })
	@no_test
	public static GamaDataframe loadCsvWith(final IScope scope, final String path, final String separator,
			final Boolean header, final String charset) {
		if (separator == null || separator.length() != 1)
			throw GamaRuntimeException.error("Separator must be a single character, got: " + separator, scope);
		return GamaDataframe.fromCSV(scope, path, separator.charAt(0), header != null && header, charset);
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
	public static GamaDataframe loadExcel(final IScope scope, final String path) {
		return GamaDataframe.fromExcelFile(scope, path);
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
	public static GamaDataframe loadJson(final IScope scope, final String path) {
		return GamaDataframe.fromJson(scope, path);
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
	public static GamaDataframe loadParquet(final IScope scope, final String path) {
		return GamaDataframe.fromParquet(scope, path);
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
			value = "Loads a whole database table into a dataframe via JDBC. Arguments: the JDBC URL, the user, "
					+ "the password, and the table name. Pass empty strings for user/password if the database does not "
					+ "require credentials. The corresponding JDBC driver must be available on the classpath.",
			usages = { @usage (
					value = "Load a PostgreSQL table",
					examples = { @example (
							value = "dataframe df <- df_load_table(\"jdbc:postgresql://localhost:5432/mydb\", \"user\", \"pwd\", \"people\");",
							isExecutable = false) }) },
			see = { "df_load_sql", "df_save_table" })
	@no_test
	public static GamaDataframe loadTable(final IScope scope, final String jdbcUrl, final String user,
			final String password, final String tableName) {
		return GamaDataframe.fromDatabaseTable(scope, jdbcUrl, emptyToNull(user), emptyToNull(password), tableName);
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
			value = "Runs a SQL query on a database via JDBC and returns the result as a dataframe. "
					+ "Arguments: the JDBC URL, the user, the password, and the SQL query. "
					+ "Pass empty strings for user/password if the database does not require credentials. "
					+ "The corresponding JDBC driver must be available on the classpath.",
			usages = { @usage (
					value = "Run a SQL query on a PostgreSQL database",
					examples = { @example (
							value = "dataframe df <- df_load_sql(\"jdbc:postgresql://localhost:5432/mydb\", \"user\", \"pwd\", \"SELECT name, age FROM people WHERE age > 18\");",
							isExecutable = false) }) },
			see = { "df_load_table", "df_save_table" })
	@no_test
	public static GamaDataframe loadSql(final IScope scope, final String jdbcUrl, final String user,
			final String password, final String sqlQuery) {
		return GamaDataframe.fromDatabaseQuery(scope, jdbcUrl, emptyToNull(user), emptyToNull(password), sqlQuery);
	}

	// ========================= Save operators =========================

	/**
	 * Saves a dataframe to a CSV file with default settings (comma separator).
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
	public static Boolean saveCsv(final IScope scope, final IDataframe df, final String path) {
		return GamaDataframe.saveCSV(scope, (GamaDataframe) df, path, ',', null);
	}

	/**
	 * Saves a dataframe to a CSV file with custom separator and charset.
	 */
	@operator (
			value = "df_save_csv_with",
			can_be_const = false,
			category = { IOperatorCategory.DATAFRAME, IOperatorCategory.FILE },
			concept = { IConcept.DATAFRAME, IConcept.FILE, IConcept.CSV })
	@doc (
			value = "Saves a dataframe to a CSV file with a custom separator and character encoding. "
					+ "The separator is a string of length 1. The charset is a string like 'UTF-8' or 'ISO-8859-1'. "
					+ "The file path is relative to the model file. Returns true on success.",
			usages = { @usage (
					value = "Save a dataframe to a semicolon-separated CSV in ISO-8859-1",
					examples = { @example (
							value = "bool success <- df_save_csv_with(my_df, \"../results/output.csv\", \";\", \"ISO-8859-1\");",
							isExecutable = false) }) },
			see = { "df_save_csv", "df_save_excel", "df_save_json", "df_load_csv_with" })
	@no_test
	public static Boolean saveCsvWith(final IScope scope, final IDataframe df, final String path,
			final String separator, final String charset) {
		if (separator == null || separator.length() != 1)
			throw GamaRuntimeException.error("Separator must be a single character, got: " + separator, scope);
		return GamaDataframe.saveCSV(scope, (GamaDataframe) df, path, separator.charAt(0), charset);
	}

	/**
	 * Saves a dataframe to an Excel file.
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
	public static Boolean saveExcel(final IScope scope, final IDataframe df, final String path,
			final String sheetName) {
		return GamaDataframe.saveExcelSheet(scope, (GamaDataframe) df, path, sheetName);
	}

	/**
	 * Saves a dataframe to a JSON file.
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
	public static Boolean saveJson(final IScope scope, final IDataframe df, final String path) {
		return GamaDataframe.saveJson(scope, (GamaDataframe) df, path);
	}

	/**
	 * Saves a dataframe to a Parquet file.
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
	public static Boolean saveParquet(final IScope scope, final IDataframe df, final String path) {
		return GamaDataframe.saveParquet(scope, (GamaDataframe) df, path);
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
			value = "Saves a dataframe to a database table via JDBC. Arguments: the dataframe, the JDBC URL, the user, "
					+ "the password, and the destination table name. The table must already exist with a compatible schema. "
					+ "Pass empty strings for user/password if the database does not require credentials. "
					+ "The corresponding JDBC driver must be available on the classpath. Returns true on success.",
			usages = { @usage (
					value = "Save a dataframe to a PostgreSQL table",
					examples = { @example (
							value = "bool ok <- df_save_table(my_df, \"jdbc:postgresql://localhost:5432/mydb\", \"user\", \"pwd\", \"people\");",
							isExecutable = false) }) },
			see = { "df_load_table", "df_load_sql" })
	@no_test
	public static Boolean saveTable(final IScope scope, final IDataframe df, final String jdbcUrl, final String user,
			final String password, final String tableName) {
		return GamaDataframe.saveDatabaseTable(scope, (GamaDataframe) df, jdbcUrl, emptyToNull(user),
				emptyToNull(password), tableName);
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
			see = { "df_row", "df_cell", "df_columns" })
	@test ("df_column(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), \"name\") = [\"Alice\",\"Bob\"]")
	public static IList<Object> dfColumn(final IScope scope, final IDataframe df, final String columnName) {
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
			see = { "df_column", "df_cell", "df_rows" })
	@test ("df_row(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 0) = [\"Alice\",30]")
	public static IList<Object> dfRow(final IScope scope, final IDataframe df, final Integer rowIndex) {
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
	public static Object dfCell(final IScope scope, final IDataframe df, final Integer rowIndex,
			final String columnName) {
		if (rowIndex < 0 || rowIndex >= df.getRows())
			throw GamaRuntimeException.error("Row index out of bounds: " + rowIndex, scope);
		return df.getCellValue(rowIndex, columnName);
	}

	/**
	 * Returns the list of column names.
	 */
	@operator (
			value = "df_columns",
			can_be_const = true,
			content_type = IType.STRING,
			type = IType.LIST,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Returns the list of column names of the dataframe.",
			usages = { @usage (
					value = "Get the column names",
					examples = { @example (
							value = "list<string> cols <- df_columns(my_df);",
							isExecutable = false) }) },
			see = { "df_rows", "df_column" })
	@test ("df_columns(dataframe_with([\"name\",\"age\",\"city\"], [[\"Alice\",30,\"Paris\"]])) = [\"name\",\"age\",\"city\"]")
	public static IList<String> dfColumns(final IScope scope, final IDataframe df) {
		return df.getColumns();
	}

	/**
	 * Returns the number of rows.
	 */
	@operator (
			value = "df_rows",
			can_be_const = true,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Returns the number of rows in the dataframe.",
			usages = { @usage (
					value = "Get the row count",
					examples = { @example (
							value = "int n <- df_rows(my_df);",
							isExecutable = false) }) },
			see = { "df_columns", "df_row" })
	@test ("df_rows(dataframe_with([\"name\"], [[\"Alice\"],[\"Bob\"],[\"Charlie\"]])) = 3")
	public static Integer dfRows(final IScope scope, final IDataframe df) {
		return df.getRows();
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
	@test ("df_rows(df_filter(dataframe_with([\"name\",\"city\"], [[\"Alice\",\"Paris\"],[\"Bob\",\"Lyon\"],[\"Eve\",\"Paris\"]]), \"city\", \"Paris\")) = 2")
	public static GamaDataframe dfFilter(final IScope scope, final IDataframe df, final String columnName,
			final Object value) {
		return GamaDataframe.filterRows((GamaDataframe) df, columnName, value);
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
	@test ("df_rows(df_remove_empty(dataframe_with([\"name\",\"email\"], [[\"Alice\",\"a@x\"],[\"Bob\",\"\"],[\"Charlie\",nil]]), \"email\")) = 1")
	public static GamaDataframe dfRemoveEmpty(final IScope scope, final IDataframe df, final String columnName) {
		return GamaDataframe.removeRowsWithEmptyValues((GamaDataframe) df, columnName);
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
			see = { "df_filter", "df_add_column", "df_columns" })
	@test ("df_columns(df_select_columns(dataframe_with([\"name\",\"age\",\"city\"], [[\"Alice\",30,\"Paris\"]]), [\"name\",\"city\"])) = [\"name\",\"city\"]")
	public static GamaDataframe dfSelectColumns(final IScope scope, final IDataframe df,
			final IList<String> columns) {
		return GamaDataframe.selectColumns((GamaDataframe) df, columns);
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
	@test ("df_columns(df_add_column(dataframe_with([\"name\"], [[\"Alice\"]]), \"score\", 0)) = [\"name\",\"score\"]")
	@test ("df_cell(df_add_column(dataframe_with([\"name\"], [[\"Alice\"]]), \"score\", 0), 0, \"score\") = 0")
	public static GamaDataframe dfAddColumn(final IScope scope, final IDataframe df, final String columnName,
			final Object defaultValue) {
		return GamaDataframe.addColumn((GamaDataframe) df, columnName, defaultValue);
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
	@test ("df_rows(df_add_row(dataframe_with([\"name\",\"age\"], [[\"Alice\",30]]), [\"Bob\",25])) = 2")
	@test ("df_cell(df_add_row(dataframe_with([\"name\",\"age\"], [[\"Alice\",30]]), [\"Bob\",25]), 1, \"name\") = \"Bob\"")
	public static GamaDataframe dfAddRow(final IScope scope, final IDataframe df, final IList<Object> values) {
		return GamaDataframe.addRow((GamaDataframe) df, values);
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
	@test ("df_rows(df_merge(dataframe_with([\"sensor\",\"value\"], [[\"temp\",22.5]]), dataframe_with([\"sensor\",\"value\"], [[\"temp\",23.1],[\"humidity\",60.0]]))) = 3")
	public static GamaDataframe dfMerge(final IScope scope, final IDataframe df1, final IDataframe df2) {
		return GamaDataframe.mergeDataframes((GamaDataframe) df1, (GamaDataframe) df2);
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
	@test ("df_rows(df_join(dataframe_with([\"id\",\"name\"], [[1,\"Alice\"],[2,\"Bob\"],[3,\"Charlie\"]]), dataframe_with([\"id\",\"salary\"], [[1,55000],[2,48000]]), \"id\")) = 2")
	public static GamaDataframe dfJoin(final IScope scope, final IDataframe df1, final IDataframe df2,
			final String columnName) {
		return GamaDataframe.joinDataframesOnCommonCol((GamaDataframe) df1, (GamaDataframe) df2, columnName);
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
	@test ("df_rows(df_pivot(dataframe_with([\"product\",\"quarter\",\"revenue\"], [[\"Widget\",\"Q1\",1000],[\"Widget\",\"Q2\",1500],[\"Gadget\",\"Q1\",800],[\"Gadget\",\"Q2\",950]]), \"product\", \"quarter\", \"revenue\")) = 2")
	public static GamaDataframe dfPivot(final IScope scope, final IDataframe df, final String indexColumn,
			final String pivotColumn, final String valueColumn) {
		return GamaDataframe.pivot((GamaDataframe) df, indexColumn, pivotColumn, valueColumn);
	}
	
	@operator (
			value = "df_pretty_print",
			can_be_const = true,
			type = IType.STRING,
			category = {IOperatorCategory.DATAFRAME},
			concept = {IConcept.DATAFRAME}
	)
	@doc (
		value = "Creates a string representing the dataframe in a human readable format. The number of rows and columns is limited to 10 and the number of characters per cell to 50."
	)
	public static String dfPrettyPrint(final IScope scope, final IDataframe df) {
		return GamaDataframe.prettyPrint(df, 10, 10, 50);
	}
	
	@operator (
			value = "df_pretty_print",
			can_be_const = true,
			type = IType.STRING,
			category = {IOperatorCategory.DATAFRAME},
			concept = {IConcept.DATAFRAME}
	)
	@doc (
		value = "Creates a string representing the dataframe in a human readable format. The maximum number of rows, columns and the number of characters per cell to print is defined by the parameters."
	)
	public static String dfPrettyPrint(final IScope scope, final IDataframe df, int maxRows, int maxCols, int maxChars) {
		return GamaDataframe.prettyPrint(df, maxRows, maxCols, maxChars);
	}

	// ========================= Outgoing conversions =========================

	/**
	 * Converts a dataframe into a map (column name -> column values).
	 */
	@operator (
			value = "df_to_map",
			can_be_const = true,
			type = IType.MAP,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME, IConcept.MAP })
	@doc (
			value = "Converts a dataframe into an ordered map whose keys are column names and whose values are "
					+ "lists of column values.",
			usages = { @usage (
					value = "Convert a dataframe to a map",
					examples = { @example (
							value = "map<string,list> m <- df_to_map(my_df);",
							isExecutable = false) }) },
			see = { "df_to_matrix", "df_to_field", "dataframe_with" })
	@test ("df_to_map(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]))[\"name\"] = [\"Alice\",\"Bob\"]")
	public static IMap<String, IList<Object>> dfToMap(final IScope scope, final IDataframe df) {
		return GamaDataframe.toMap(scope, (GamaDataframe) df);
	}

	/**
	 * Converts a dataframe into an object matrix.
	 */
	@operator (
			value = "df_to_matrix",
			can_be_const = true,
			type = IType.MATRIX,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME, IConcept.MATRIX })
	@doc (
			value = "Converts a dataframe into a matrix with the same shape. Column names are dropped. "
					+ "Cell values are kept as-is (object matrix).",
			usages = { @usage (
					value = "Convert a dataframe to a matrix",
					examples = { @example (
							value = "matrix m <- df_to_matrix(my_df);",
							isExecutable = false) }) },
			see = { "df_to_map", "df_to_field" })
	@test ("df_to_matrix(dataframe_with([\"a\",\"b\"], [[1,2],[3,4]])) = matrix([[1,2],[3,4]])")
	public static IMatrix<Object> dfToMatrix(final IScope scope, final IDataframe df) {
		return GamaDataframe.toMatrix(scope, (GamaDataframe) df);
	}

	/**
	 * Converts a dataframe into a GAMA field of float values.
	 */
	@operator (
			value = "df_to_field",
			can_be_const = true,
			type = IType.FIELD,
			category = { IOperatorCategory.DATAFRAME },
			concept = { IConcept.DATAFRAME })
	@doc (
			value = "Converts a dataframe into a field of float values. All cells must be numeric (or parseable as "
					+ "float); null cells become 0.0. Column names are dropped.",
			usages = { @usage (
					value = "Convert a numeric dataframe to a field",
					examples = { @example (
							value = "field f <- df_to_field(my_df);",
							isExecutable = false) }) },
			see = { "df_to_matrix", "df_to_map" })
	@no_test
	public static IField dfToField(final IScope scope, final IDataframe df) {
		return GamaDataframe.toField(scope, (GamaDataframe) df);
	}
}
