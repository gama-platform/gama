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
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.dataframe.GamaDataframe;
import gama.api.types.dataframe.IDataframe;
import gama.api.types.list.IList;

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
	public static GamaDataframe dataframeWith(final IScope scope, final IList<String> columns,
			final IList<IList<Object>> data) {
		return GamaDataframe.create(scope, columns, data);
	}

	// ========================= File loading operators =========================

	/**
	 * Loads a CSV file into a dataframe with default settings (comma separator, header, UTF-8).
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
					value = "Load a CSV file with default settings (comma separator, with header, UTF-8)",
					examples = { @example (
							value = "dataframe df <- df_load_csv(\"../includes/data.csv\");",
							isExecutable = false) }) },
			see = { "df_load_csv_with", "df_load_excel", "df_load_json" })
	@no_test
	public static GamaDataframe loadCsv(final IScope scope, final String path) {
		return GamaDataframe.fromCSV(scope, path, ',', true, null);
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
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
	@no_test
	public static GamaDataframe dfPivot(final IScope scope, final IDataframe df, final String indexColumn,
			final String pivotColumn, final String valueColumn) {
		return GamaDataframe.pivot((GamaDataframe) df, indexColumn, pivotColumn, valueColumn);
	}
}
