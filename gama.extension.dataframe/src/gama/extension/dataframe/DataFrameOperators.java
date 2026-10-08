/*******************************************************************************************************
 *
 * DataFrameOperators.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.dataframe;

import org.dflib.jdbc.connector.JdbcConnector;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.no_fuzz_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.tests;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;

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
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT, IConcept.CONTAINER })
	@doc (
			value = "Creates a dataframe from a list of column names and a list of rows (each row is a list of values).",
			usages = { @usage (
					value = "Create a dataframe with column names and data rows",
					examples = { @example (
							value = "dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]])",
							isExecutable = false) }) })
	@tests ({
			@test ("(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]])).rows = 2"),
			@test ("(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]])).keys = [\"name\",\"age\"]"),
			@test ("string(type_of((dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]))[\"name\"])) = \"list<unknown>\""),
			@test ("string(type_of((dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]))[\"name\"][0])) = \"unknown\""),
			@test ("string(actual_type_of((dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]))[\"name\"])) = \"list<string>\""),
			@test ("dataframe df <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); df != nil"),
			@test ("dataframe df2 <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); df2.rows = 2"),
			@test ("dataframe df3 <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); df3.keys = [\"name\", \"age\"]"),
			// the operator is not destructive
			@test ("dataframe df4 <- dataframe_with([\"name\"], [[\"Alice\"], [\"Bob\"]]); df4.keys = [\"name\"]"),
			@test ("dataframe a <- dataframe_with([\"a\", \"b\"], [[1, 2]]); dataframe b <- dataframe_with([\"a\", \"b\"], [[3, 4], [5, 6]]); (a + b).rows = 3"),
			@test ("dataframe a2 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); (a2 + [7, 8]).rows = 2"),
			@test ("dataframe df5 <- dataframe_with([\"name\", \"age\", \"city\"], [[\"Alice\", 30, \"Paris\"], [\"Bob\", 25, \"Lyon\"], [\"Charlie\", 35, \"Marseille\"]]); df5 != nil"),
			@test ("dataframe df6 <- dataframe_with([\"name\", \"age\", \"city\"], [[\"Alice\", 30, \"Paris\"], [\"Bob\", 25, \"Lyon\"], [\"Charlie\", 35, \"Marseille\"]]); df6.rows = 3"),
			@test ("dataframe df7 <- dataframe_with([\"name\", \"age\", \"city\"], [[\"Alice\", 30, \"Paris\"], [\"Bob\", 25, \"Lyon\"], [\"Charlie\", 35, \"Marseille\"]]); df7.keys = [\"name\", \"age\", \"city\"]"),
			@test ("dataframe df8 <- dataframe_with([\"x\", \"y\", \"z\"], [[1, 2, 3], [4, 5, 6]]); df8.keys = [\"x\", \"y\", \"z\"]"),
			@test ("dataframe df9 <- dataframe_with([\"x\", \"y\", \"z\"], [[1, 2, 3], [4, 5, 6]]); df9.rows = 2"),
			@test ("dataframe df10 <- dataframe_with([\"x\", \"y\", \"z\"], [[1, 2, 3], [4, 5, 6]]); df10.columns = 3"),
			@test ("dataframe df11 <- dataframe_with([\"a\", \"b\"], [[1, 2], [3, 4], [5, 6]]); df11.rows = 3"),
			@test ("dataframe df12 <- dataframe_with([\"x\", \"y\"], [[1, 2]]); list<string> cols <- df12.keys; cols = [\"x\", \"y\"]"),
			@test ("dataframe df13 <- dataframe_with([\"a\"], [[1], [2], [3]]); length(df13) = 3"),
			@test ("dataframe df14 <- dataframe_with([\"a\"], [[1]]); !empty(df14)"),
			// Original unchanged (immutable)
			@test ("dataframe df15 <- dataframe_with([\"name\"], [[\"Alice\"], [\"Bob\"]]); !(df15.keys contains \"score\")"),
			@test ("dataframe df16 <- dataframe_with([\"x\", \"y\"], [[1, 2]]); dataframe df22 <- (df16 + [3, 4]); df22.rows = 2"),
			// Check that there is no side effect on the first dataframe
			@test ("dataframe df17 <- dataframe_with([\"x\", \"y\"], [[1, 2]]); df17.rows = 1"),
			@test ("dataframe df1 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); dataframe df23 <- dataframe_with([\"a\", \"b\"], [[3, 4], [5, 6]]); dataframe merged <- (df1 + df23); merged.rows = 3"),
			@test ("dataframe df18 <- dataframe_with([\"name\", \"score\"], [[\"Alice\", 95], [\"Bob\", 40], [\"Charlie\", 72]]); !(df18.keys contains \"status\")"),
			@test ("dataframe df19 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); dataframe df24 <- dataframe_with([\"a\", \"b\"], [[3, 4], [5, 6]]); dataframe merged2 <- (df19 + df24); merged2.rows = 3"),
			@test ("dataframe df110 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); dataframe with_row <- (df110 + [7, 8]); with_row.rows = 2"),
			// original unchanged (immutability)
			@test ("dataframe df111 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); df111.rows = 1")
	})
	public static IDataFrame dataframeWith(final IScope scope, final IList<String> columns, final IList<IList> data) {
		return GamaDataFrameFactory.create(scope, columns, data);
	}

	// ========================= Database loading operators
	// =========================

	/**
	 * Loads a whole database table into a dataframe via JDBC.
	 */
	@no_fuzz_test ("acts on the outside world (files, network, clipboard, user interface, shell...)")
	@operator (
			value = "load_table",
			can_be_const = false,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY, IOperatorCategory.FILE },
			concept = { IDataframeConstants.CONCEPT, IConcept.DATABASE })
	@doc (
			value = """
					Loads a whole database table into a dataframe via JDBC. Arguments: the JDBC URL, the user, \
					the password, and the table name. Pass empty strings for user/password if the database does not \
					require credentials. The corresponding JDBC driver must be available on the classpath.""",
			usages = { @usage (
					value = "Load a PostgreSQL table",
					examples = { @example (
							value = "dataframe df <- load_table(\"jdbc:postgresql://localhost:5432/mydb\", \"user\", \"pwd\", \"people\");",
							isExecutable = false) }) },
			see = { "load_sql", "save_table" })
	@no_test
	public static IDataFrame loadTable(final IScope scope, final String jdbcUrl, final String user,
			final String password, final String tableName) {
		return GamaDataFrameFactory.fromDatabaseTable(scope, jdbcUrl, emptyToNull(user), emptyToNull(password),
				tableName);
	}

	/**
	 * Loads the result of a SQL query into a dataframe via JDBC.
	 */
	@no_fuzz_test ("acts on the outside world (files, network, clipboard, user interface, shell...)")
	@operator (
			value = "load_sql",
			can_be_const = false,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY, IOperatorCategory.FILE },
			concept = { IDataframeConstants.CONCEPT, IConcept.DATABASE })
	@doc (
			value = """
					Runs a SQL query on a database via JDBC and returns the result as a dataframe. \
					Arguments: the JDBC URL, the user, the password, and the SQL query. \
					Pass empty strings for user/password if the database does not require credentials. \
					The corresponding JDBC driver must be available on the classpath.""",
			usages = { @usage (
					value = "Run a SQL query on a PostgreSQL database",
					examples = { @example (
							value = "dataframe df <- load_sql(\"jdbc:postgresql://localhost:5432/mydb\", \"user\", \"pwd\", \"SELECT name, age FROM people WHERE age > 18\");",
							isExecutable = false) }) },
			see = { "load_table", "save_table" })
	@no_test
	public static IDataFrame loadSql(final IScope scope, final String jdbcUrl, final String user, final String password,
			final String sqlQuery) {
		return GamaDataFrameFactory.fromDatabaseQuery(scope, jdbcUrl, emptyToNull(user), emptyToNull(password),
				sqlQuery);
	}

	// ========================= Save operators =========================

	/**
	 * Saves a dataframe to a database table via JDBC.
	 */
	@no_fuzz_test ("acts on the outside world (files, network, clipboard, user interface, shell...)")
	@operator (
			value = "save_table",
			can_be_const = false,
			category = { IDataframeConstants.CATEGORY, IOperatorCategory.FILE },
			concept = { IDataframeConstants.CONCEPT, IConcept.DATABASE })
	@doc (
			value = """
					Saves a dataframe to a database table via JDBC. Arguments: the dataframe, the JDBC URL, the user, \
					the password, and the destination table name. The table must already exist with a compatible schema. \
					Pass empty strings for user/password if the database does not require credentials. \
					The corresponding JDBC driver must be available on the classpath. Returns true on success.""",
			usages = { @usage (
					value = "Save a dataframe to a PostgreSQL table",
					examples = { @example (
							value = "bool ok <- save_table(my_df, \"jdbc:postgresql://localhost:5432/mydb\", \"user\", \"pwd\", \"people\");",
							isExecutable = false) }) },
			see = { "load_table", "load_sql" })
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

	// ========================= Cell access operator =========================
	//
	// Column and row access is provided by the matrix-style operators 'column_at'
	// and 'row_at'
	// (see the "Matrix-style access" section below).

	/**
	 * Returns a single cell value.
	 */
	@operator (
			value = "cell",
			can_be_const = true,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Returns the value at the specified row index and column name.",
			usages = { @usage (
					value = "Get a specific cell value",
					examples = { @example (
							value = "unknown val <- cell(my_df, 0, \"name\");",
							isExecutable = false) }) },
			see = { "column_at", "row_at" })
	@tests ({
			@test ("cell(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 1, \"name\") = \"Bob\""),
			@test ("dataframe df <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); cell(df, 1, \"name\") = \"Bob\""),
			@test ("dataframe df22 <- dataframe_with([\"name\"], [[\"Alice\"], [\"Bob\"]]); dataframe df22 <- add_column(df22, \"score\", 0); cell(df22, 0, \"score\") = 0"),
			@test ("dataframe df3 <- dataframe_with([\"name\"], [[\"Alice\"], [\"Bob\"]]); dataframe df23 <- add_column(df3, \"score\", 0); cell(df23, 1, \"score\") = 0"),
			@test ("dataframe df2 <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); cell(df2, 0, \"name\") = \"Alice\""),
			@test ("dataframe df4 <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); cell(df4, 1, \"age\") = 25"),
			@test ("dataframe df5 <- dataframe_with([\"name\", \"note\"], [[\"Alice\", \"ok\"], [\"Bob\", \"\"], [\"Charlie\", nil]]); dataframe cleaned <- remove_empty(df5, \"note\"); cell(cleaned, 0, \"name\") = \"Alice\""),
			@test ("dataframe df1 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); dataframe df24 <- dataframe_with([\"a\", \"b\"], [[3, 4], [5, 6]]); dataframe merged <- (df1 + df24); cell(merged, 0, \"a\") = 1"),
			@test ("dataframe df12 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); dataframe df25 <- dataframe_with([\"a\", \"b\"], [[3, 4], [5, 6]]); dataframe merged2 <- (df12 + df25); cell(merged2, 1, \"a\") = 3"),
			@test ("dataframe df13 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); dataframe df26 <- dataframe_with([\"a\", \"b\"], [[3, 4], [5, 6]]); dataframe merged3 <- (df13 + df26); cell(merged3, 2, \"a\") = 5"),
			// Product A, Q1 = 100
			@test ("dataframe df6 <- dataframe_with( [\"product\", \"quarter\", \"revenue\"], [[\"A\", \"Q1\", 100], [\"A\", \"Q2\", 200], [\"B\", \"Q1\", 150], [\"B\", \"Q2\", 250]] ); dataframe pivoted <- pivot(df6, \"product\", \"quarter\", \"revenue\"); cell(pivoted, 0, \"Q1\") = 100"),
			// Product B, Q2 = 250
			@test ("dataframe df7 <- dataframe_with( [\"product\", \"quarter\", \"revenue\"], [[\"A\", \"Q1\", 100], [\"A\", \"Q2\", 200], [\"B\", \"Q1\", 150], [\"B\", \"Q2\", 250]] ); dataframe pivoted2 <- pivot(df7, \"product\", \"quarter\", \"revenue\"); cell(pivoted2, 1, \"Q2\") = 250")
	})
	public static Object dfCell(final IScope scope, final IDataFrame df, final Integer rowIndex,
			final String columnName) {
		if (rowIndex < 0 || rowIndex >= df.getRows())
			throw GamaRuntimeException.error("Row index out of bounds: " + rowIndex, scope);
		return df.getCellValue(rowIndex, columnName);
	}

	// ========================= Matrix-style access (merged operators)
	// =========================
	//
	// These operators reuse the vocabulary of the existing GAML matrix operators
	// (row_at, column_at,
	// rows_list, columns_list) so that a dataframe can be manipulated like a
	// matrix. They are additive
	// overloads registered for the ID operand type and do not affect the
	// matrix versions.

	/**
	 * Returns the row at the given index as a list, mirroring the matrix {@code row_at} operator.
	 */
	@operator (
			value = "row_at",
			can_be_const = true,
			content_type = IType.NONE,
			type = IType.LIST,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Returns the row of the dataframe at the given index as a list of values. "
					+ "Overloads the matrix 'row_at' operator for dataframes.",
			usages = { @usage (
					value = "Get the row at index 1",
					examples = { @example (
							value = "my_df row_at 1",
							isExecutable = false) }) },
			see = { "column_at", "rows_list", "iloc" })
	@tests ({
			@test ("(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]) row_at 1) = [\"Bob\",25]"),
			@test ("dataframe df <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); (df row_at 0) = [\"Alice\", 30]"),
			@test ("dataframe df2 <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); (df2 row_at 1) = [\"Bob\", 25]"),
			@test ("dataframe df3 <- dataframe_with([\"a\", \"b\", \"c\"], [[1, 2, 3], [4, 5, 6], [7, 8, 9]]); list row0 <- (df3 row_at 0); row0 = [1, 2, 3]"),
			@test ("dataframe df4 <- dataframe_with([\"a\", \"b\", \"c\"], [[1, 2, 3], [4, 5, 6], [7, 8, 9]]); list row2 <- (df4 row_at 2); row2 = [7, 8, 9]"),
			@test ("dataframe df5 <- dataframe_with([\"x\", \"y\"], [[1, 2]]); dataframe df22 <- (df5 + [3, 4]); (df22 row_at 1) = [3, 4]"),
			// row_at / column_at mirror the matrix operators
			@test ("dataframe df6 <- dataframe_with( [\"name\", \"age\", \"city\"], [[\"Alice\", 30, \"Paris\"], [\"Bob\", 25, \"Lyon\"], [\"Charlie\", 35, \"Marseille\"]] ); (df6 row_at 0) = [\"Alice\", 30, \"Paris\"]"),
			@test ("dataframe df1 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); dataframe df23 <- dataframe_with([\"a\", \"b\"], [[3, 4], [5, 6]]); dataframe merged <- (df1 + df23); (merged row_at 2) = [5, 6]"),
			@test ("dataframe df12 <- dataframe_with([\"a\", \"b\"], [[1, 2]]); dataframe with_row <- (df12 + [7, 8]); (with_row row_at 1) = [7, 8]")
	})
	public static IList rowAt(final IScope scope, final IDataFrame df, final Integer rowIndex) {
		if (rowIndex < 0 || rowIndex >= df.getRows())
			throw GamaRuntimeException.error("Row index out of bounds: " + rowIndex, scope);
		return df.getRowValues(rowIndex);
	}

	/**
	 * Returns a column by name as a list, mirroring the matrix {@code column_at} operator.
	 */
	@operator (
			value = "column_at",
			can_be_const = true,
			content_type = IType.NONE,
			type = IType.LIST,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Returns the values of the named column of the dataframe as a list. "
					+ "Overloads the matrix 'column_at' operator to accept a column name.",
			usages = { @usage (
					value = "Get the 'name' column",
					examples = { @example (
							value = "my_df column_at \"name\"",
							isExecutable = false) }) },
			see = { "row_at", "columns_list" })
	@tests ({
			@test ("(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]) column_at \"name\") = [\"Alice\",\"Bob\"]"),
			@test ("dataframe df <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); (df column_at \"name\") = [\"Alice\", \"Bob\"]"),
			@test ("dataframe df2 <- dataframe_with([\"name\", \"city\"], [[\"Alice\", \"Paris\"], [\"Bob\", \"Lyon\"], [\"Eve\", \"Paris\"]]); dataframe parisians <- filter(df2, \"city\", \"Paris\"); (parisians column_at \"name\") = [\"Alice\", \"Eve\"]"),
			@test ("dataframe df3 <- dataframe_with([\"id\", \"city\"], [[1, \"Paris\"], [2, \"Lyon\"], [3, \"Paris\"]]); dataframe chained <- select_columns(filter(df3, \"city\", \"Paris\"), [\"id\"]); (chained column_at \"id\") = [1, 3]"),
			@test ("dataframe df4 <- dataframe_with([\"name\", \"score\"], [[\"Alice\", 95], [\"Bob\", 87], [\"Charlie\", 92]]); list col <- (df4 column_at \"name\"); length(col) = 3"),
			@test ("dataframe df5 <- dataframe_with([\"name\", \"score\"], [[\"Alice\", 95], [\"Bob\", 87], [\"Charlie\", 92]]); list col2 <- (df5 column_at \"name\"); col2[0] = \"Alice\""),
			@test ("dataframe df6 <- dataframe_with([\"name\", \"score\"], [[\"Alice\", 95], [\"Bob\", 87], [\"Charlie\", 92]]); list col3 <- (df6 column_at \"name\"); col3[1] = \"Bob\""),
			@test ("dataframe df7 <- dataframe_with([\"name\", \"score\"], [[\"Alice\", 95], [\"Bob\", 87], [\"Charlie\", 92]]); list col4 <- (df7 column_at \"name\"); col4[2] = \"Charlie\""),
			@test ("dataframe df8 <- dataframe_with([\"name\", \"score\"], [[\"Alice\", 95], [\"Bob\", 87], [\"Charlie\", 92]]); list scores <- (df8 column_at \"score\"); scores[0] = 95"),
			@test ("dataframe df9 <- dataframe_with([\"name\", \"score\"], [[\"Alice\", 95], [\"Bob\", 87], [\"Charlie\", 92]]); list scores2 <- (df9 column_at \"score\"); scores2[2] = 92"),
			@test ("dataframe df10 <- dataframe_with([\"name\", \"city\"], [[\"Alice\", \"Paris\"], [\"Bob\", \"Lyon\"], [\"Charlie\", \"Paris\"]]); dataframe filtered <- filter(df10, \"city\", \"Paris\"); list names <- (filtered column_at \"name\"); names contains \"Alice\""),
			@test ("dataframe df11 <- dataframe_with([\"name\", \"city\"], [[\"Alice\", \"Paris\"], [\"Bob\", \"Lyon\"], [\"Charlie\", \"Paris\"]]); dataframe filtered2 <- filter(df11, \"city\", \"Paris\"); list names2 <- (filtered2 column_at \"name\"); names2 contains \"Charlie\""),
			@test ("dataframe df12 <- dataframe_with([\"name\", \"city\"], [[\"Alice\", \"Paris\"], [\"Bob\", \"Lyon\"], [\"Charlie\", \"Paris\"]]); dataframe filtered3 <- filter(df12, \"city\", \"Paris\"); list names3 <- (filtered3 column_at \"name\"); !(names3 contains \"Bob\")"),
			@test ("dataframe df13 <- dataframe_with( [\"name\", \"age\", \"city\"], [[\"Alice\", 30, \"Paris\"], [\"Bob\", 25, \"Lyon\"], [\"Charlie\", 35, \"Marseille\"]] ); (df13 column_at \"age\") = [30, 25, 35]")
	})
	public static IList columnAtName(final IScope scope, final IDataFrame df, final String columnName) {
		if (!df.getColumns().contains(columnName))
			throw GamaRuntimeException.error("Unknown column: " + columnName, scope);
		return df.getColumnValues(columnName);
	}

	/**
	 * Returns a column by integer index as a list, mirroring the matrix {@code column_at} operator.
	 */
	@operator (
			value = "column_at",
			can_be_const = true,
			content_type = IType.NONE,
			type = IType.LIST,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Returns the values of the column at the given integer position as a list. "
					+ "Overloads the matrix 'column_at' operator for dataframes.",
			usages = { @usage (
					value = "Get the first column",
					examples = { @example (
							value = "my_df column_at 0",
							isExecutable = false) }) },
			see = { "row_at", "columns_list" })
	@tests ({
			@test ("(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]) column_at 0) = [\"Alice\",\"Bob\"]"),
			@test ("dataframe df <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); (df column_at 1) = [30, 25]"),
			@test ("dataframe df2 <- dataframe_with( [\"name\", \"age\", \"city\"], [[\"Alice\", 30, \"Paris\"], [\"Bob\", 25, \"Lyon\"], [\"Charlie\", 35, \"Marseille\"]] ); (df2 column_at 0) = [\"Alice\", \"Bob\", \"Charlie\"]")
	})
	public static IList columnAtIndex(final IScope scope, final IDataFrame df, final Integer columnIndex) {
		final IList<String> cols = df.getColumns();
		if (columnIndex < 0 || columnIndex >= cols.size())
			throw GamaRuntimeException.error("Column index out of bounds: " + columnIndex, scope);
		return df.getColumnValues(cols.get(columnIndex));
	}

	/**
	 * Returns all rows as a list of lists, mirroring the matrix {@code rows_list} operator.
	 */
	@operator (
			value = "rows_list",
			can_be_const = true,
			content_type = IType.LIST,
			type = IType.LIST,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Returns the list of the rows of the dataframe, each row being a list of its cell values. "
					+ "Overloads the matrix 'rows_list' operator for dataframes.",
			see = { "columns_list", "row_at" })
	@tests ({
			@test ("rows_list(dataframe_with([\"a\",\"b\"], [[1,2],[3,4]])) = [[1,2],[3,4]]"),
			@test ("dataframe df <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); rows_list(df) = [[\"Alice\", 30], [\"Bob\", 25]]"),
			// rows_list / columns_list
			@test ("dataframe df2 <- dataframe_with( [\"name\", \"age\", \"city\"], [[\"Alice\", 30, \"Paris\"], [\"Bob\", 25, \"Lyon\"], [\"Charlie\", 35, \"Marseille\"]] ); rows_list(df2) = [[\"Alice\",30,\"Paris\"],[\"Bob\",25,\"Lyon\"],[\"Charlie\",35,\"Marseille\"]]")
	})
	public static IList rowsList(final IScope scope, final IDataFrame df) {
		// Explicitly build a list of rows-as-lists (row values), independent of the
		// dataframe's iteration
		// element type, which under Model R is a row-map rather than a row-list.
		final IList result = GamaListFactory.create(Types.LIST);
		for (int i = 0; i < df.getRows(); i++) { result.add(df.getRowValues(i)); }
		return result;
	}

	/**
	 * Returns all columns as a list of lists, mirroring the matrix {@code columns_list} operator.
	 */
	@operator (
			value = "columns_list",
			can_be_const = true,
			content_type = IType.LIST,
			type = IType.LIST,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Returns the list of the columns of the dataframe, each column being a list of its values. "
					+ "Overloads the matrix 'columns_list' operator for dataframes.",
			see = { "rows_list", "column_at" })
	@tests ({
			@test ("columns_list(dataframe_with([\"a\",\"b\"], [[1,2],[3,4]])) = [[1,3],[2,4]]"),
			@test ("dataframe df <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); columns_list(df) = [[\"Alice\", \"Bob\"], [30, 25]]"),
			@test ("dataframe df2 <- dataframe_with( [\"name\", \"age\", \"city\"], [[\"Alice\", 30, \"Paris\"], [\"Bob\", 25, \"Lyon\"], [\"Charlie\", 35, \"Marseille\"]] ); columns_list(df2) = [[\"Alice\",\"Bob\",\"Charlie\"],[30,25,35],[\"Paris\",\"Lyon\",\"Marseille\"]]")
	})
	public static IList columnsList(final IScope scope, final IDataFrame df) {
		final IList result = GamaListFactory.create(Types.LIST);
		for (final String col : df.getColumns()) { result.add(df.getColumnValues(col)); }
		return result;
	}

	// ========================= Filtering operators =========================

	/**
	 * Filters rows where a column matches a value.
	 */
	@operator (
			value = "filter",
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT, IConcept.FILTER })
	@doc (
			value = "Returns a new dataframe containing only the rows where the specified column equals the given value.",
			usages = { @usage (
					value = "Filter rows where 'city' equals 'Paris'",
					examples = { @example (
							value = "dataframe df2 <- filter(my_df, \"city\", \"Paris\");",
							isExecutable = false) }) },
			see = { "remove_empty", "select_columns" })
	@tests ({
			@test ("(filter(dataframe_with([\"name\",\"city\"], [[\"Alice\",\"Paris\"],[\"Bob\",\"Lyon\"],[\"Eve\",\"Paris\"]]), \"city\", \"Paris\")).rows = 2"),
			@test ("dataframe df <- dataframe_with([\"name\", \"city\"], [[\"Alice\", \"Paris\"], [\"Bob\", \"Lyon\"], [\"Eve\", \"Paris\"]]); dataframe parisians <- filter(df, \"city\", \"Paris\"); parisians.rows = 2"),
			@test ("dataframe df2 <- dataframe_with([\"name\", \"city\"], [[\"Alice\", \"Paris\"], [\"Bob\", \"Lyon\"], [\"Eve\", \"Paris\"]]); dataframe nobody <- filter(df2, \"city\", \"Berlin\"); nobody.rows = 0"),
			@test ("dataframe df3 <- dataframe_with([\"name\", \"city\"], [[\"Alice\", \"Paris\"], [\"Bob\", \"Lyon\"], [\"Charlie\", \"Paris\"]]); dataframe filtered <- filter(df3, \"city\", \"Paris\"); filtered.rows = 2"),
			@test ("dataframe df4 <- dataframe_with([\"val\"], [[1], [2], [3]]); dataframe filtered2 <- filter(df4, \"val\", 999); filtered2.rows = 0")
	})
	public static IDataFrame dfFilter(final IScope scope, final IDataFrame df, final String columnName,
			final Object value) {
		return df.filterRows(columnName, value);
	}

	/**
	 * Removes rows where a column has empty or null values.
	 */
	@operator (
			value = "remove_empty",
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT, IConcept.FILTER })
	@doc (
			value = "Returns a new dataframe with rows removed where the specified column has null or empty values.",
			usages = { @usage (
					value = "Remove rows with empty 'name' values",
					examples = { @example (
							value = "dataframe df2 <- remove_empty(my_df, \"name\");",
							isExecutable = false) }) },
			see = { "filter", "select_columns" })
	@tests ({
			@test ("(remove_empty(dataframe_with([\"name\",\"email\"], [[\"Alice\",\"a@x\"],[\"Bob\",\"\"],[\"Charlie\",nil]]), \"email\")).rows = 1"),
			@test ("dataframe df <- dataframe_with([\"name\", \"note\"], [[\"Alice\", \"ok\"], [\"Bob\", \"\"], [\"Charlie\", nil]]); dataframe cleaned <- remove_empty(df, \"note\"); cleaned.rows = 1")
	})
	public static IDataFrame dfRemoveEmpty(final IScope scope, final IDataFrame df, final String columnName) {
		return df.removeRowsWithEmptyValues(columnName);
	}

	/**
	 * Selects a subset of columns.
	 */
	@operator (
			value = "select_columns",
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Returns a new dataframe containing only the specified columns.",
			usages = { @usage (
					value = "Select only 'name' and 'age' columns",
					examples = { @example (
							value = "dataframe df2 <- select_columns(my_df, [\"name\", \"age\"]);",
							isExecutable = false) }) },
			see = { "filter", "add_column" })
	@tests ({
			@test ("(select_columns(dataframe_with([\"name\",\"age\",\"city\"], [[\"Alice\",30,\"Paris\"]]), [\"name\",\"city\"])).keys = [\"name\",\"city\"]"),
			@test ("dataframe df <- dataframe_with([\"id\", \"city\"], [[1, \"Paris\"], [2, \"Lyon\"], [3, \"Paris\"]]); dataframe chained <- select_columns(filter(df, \"city\", \"Paris\"), [\"id\"]); chained.keys = [\"id\"]"),
			@test ("dataframe df2 <- dataframe_with([\"id\", \"city\"], [[1, \"Paris\"], [2, \"Lyon\"], [3, \"Paris\"]]); dataframe chained2 <- select_columns(filter(df2, \"city\", \"Paris\"), [\"id\"]); chained2.rows = 2"),
			@test ("dataframe df3 <- dataframe_with([\"a\", \"b\", \"c\"], [[1, 2, 3], [4, 5, 6]]); dataframe sub <- select_columns(df3, [\"a\", \"c\"]); sub.keys = [\"a\", \"c\"]"),
			@test ("dataframe df4 <- dataframe_with([\"a\", \"b\", \"c\"], [[1, 2, 3], [4, 5, 6]]); dataframe sub2 <- select_columns(df4, [\"a\", \"c\"]); sub2.rows = 2"),
			@test ("dataframe df5 <- dataframe_with([\"a\", \"b\", \"c\"], [[1, 2, 3], [4, 5, 6]]); dataframe sub3 <- select_columns(df5, [\"a\", \"c\"]); cell(sub3, 0, \"a\") = 1"),
			@test ("dataframe df6 <- dataframe_with([\"a\", \"b\", \"c\"], [[1, 2, 3], [4, 5, 6]]); dataframe sub4 <- select_columns(df6, [\"a\", \"c\"]); cell(sub4, 0, \"c\") = 3"),
			@test ("dataframe df7 <- dataframe_with( [\"name\", \"age\", \"city\", \"score\"], [[\"Alice\", 30, \"Paris\", 95], [\"Bob\", 25, \"Lyon\", 87], [\"Charlie\", 35, \"Paris\", 92], [\"Diana\", 28, \"Paris\", 78]] ); dataframe result <- select_columns(filter(df7, \"city\", \"Paris\"), [\"name\", \"score\"]); result.rows = 3"),
			@test ("dataframe df8 <- dataframe_with( [\"name\", \"age\", \"city\", \"score\"], [[\"Alice\", 30, \"Paris\", 95], [\"Bob\", 25, \"Lyon\", 87], [\"Charlie\", 35, \"Paris\", 92], [\"Diana\", 28, \"Paris\", 78]] ); dataframe result2 <- select_columns(filter(df8, \"city\", \"Paris\"), [\"name\", \"score\"]); result2.keys = [\"name\", \"score\"]")
	})
	public static IDataFrame dfSelectColumns(final IScope scope, final IDataFrame df, final IList<String> columns) {
		return df.selectColumns(columns);
	}

	// ========================= Modification operators =========================

	/**
	 * Adds a column with a default value.
	 */
	@operator (
			value = "add_column",
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Returns a new dataframe with an additional column filled with the given default value.",
			usages = { @usage (
					value = "Add a 'score' column with default value 0",
					examples = { @example (
							value = "dataframe df2 <- add_column(my_df, \"score\", 0);",
							isExecutable = false) }) },
			see = { "select_columns" })
	@tests ({
			@test ("(add_column(dataframe_with([\"name\"], [[\"Alice\"]]), \"score\", 0)).keys = [\"name\",\"score\"]"),
			@test ("cell(add_column(dataframe_with([\"name\"], [[\"Alice\"]]), \"score\", 0), 0, \"score\") = 0"),
			@test ("dataframe df <- dataframe_with([\"name\"], [[\"Alice\"], [\"Bob\"]]); dataframe df2 <- add_column(df, \"score\", 0); df2.keys = [\"name\", \"score\"]"),
			@test ("dataframe df3 <- dataframe_with([\"name\"], [[\"Alice\"], [\"Bob\"]]); dataframe df22 <- add_column(df3, \"score\", 0); df22.keys contains \"score\""),
			@test ("dataframe df4 <- dataframe_with([\"name\"], [[\"Alice\"], [\"Bob\"]]); dataframe df23 <- add_column(df4, \"score\", 0); cell(df23, 0, \"score\") = 0"),
			@test ("dataframe df5 <- dataframe_with([\"name\"], [[\"Alice\"], [\"Bob\"]]); dataframe df24 <- add_column(df5, \"score\", 0); cell(df24, 1, \"score\") = 0"),
			@test ("dataframe df6 <- dataframe_with([\"name\", \"score\"], [[\"Alice\", 95], [\"Bob\", 40], [\"Charlie\", 72]]); dataframe with_passed <- add_column(df6, \"status\", \"unknown\"); with_passed.keys contains \"status\"")
	})
	public static IDataFrame dfAddColumn(final IScope scope, final IDataFrame df, final String columnName,
			final Object defaultValue) {
		return df.addColumn(columnName, defaultValue);
	}

	// ========================= Join (merged operator) =========================

	/**
	 * Inner-joins two dataframes on a single common key column.
	 */
	@operator (
			value = "join",
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Inner-joins two dataframes on the given common key column: only the rows whose key matches in "
					+ "both dataframes are kept.",
			usages = { @usage (
					value = "Join two dataframes on the 'id' column",
					examples = { @example (
							value = "join(df_people, df_scores, \"id\")",
							isExecutable = false) }) },
			see = { "pivot" })
	@tests ({
			@test ("(join(dataframe_with([\"id\",\"name\"], [[1,\"Alice\"],[2,\"Bob\"],[3,\"Charlie\"]]), dataframe_with([\"id\",\"salary\"], [[1,55000],[2,48000]]), \"id\")).rows = 2"),
			// Inner join: only matching rows (id 1 and 2)
			@test ("dataframe people <- dataframe_with([\"id\", \"name\"], [[1, \"Alice\"], [2, \"Bob\"], [3, \"Charlie\"]]); dataframe scores <- dataframe_with([\"id\", \"score\"], [[1, 95], [2, 87]]); dataframe joined <- join(people, scores, \"id\"); joined.rows = 2"),
			@test ("dataframe people2 <- dataframe_with([\"id\", \"name\"], [[1, \"Alice\"], [2, \"Bob\"], [3, \"Charlie\"]]); dataframe scores2 <- dataframe_with([\"id\", \"score\"], [[1, 95], [2, 87]]); dataframe joined2 <- join(people2, scores2, \"id\"); joined2.keys contains \"name\""),
			@test ("dataframe people3 <- dataframe_with([\"id\", \"name\"], [[1, \"Alice\"], [2, \"Bob\"], [3, \"Charlie\"]]); dataframe scores3 <- dataframe_with([\"id\", \"score\"], [[1, 95], [2, 87]]); dataframe joined3 <- join(people3, scores3, \"id\"); joined3.keys contains \"score\""),
			// single key, string and list forms
			@test ("dataframe people4   <- dataframe_with([\"id\", \"name\"],   [[1, \"Alice\"], [2, \"Bob\"], [3, \"Charlie\"]]); dataframe salaries <- dataframe_with([\"id\", \"salary\"], [[1, 55000], [2, 48000]]); (join(people4, salaries, \"id\")).rows = 2")
	})
	public static IDataFrame join(final IScope scope, final IDataFrame df1, final IDataFrame df2,
			final String keyColumn) {
		final IList<String> cols = GamaListFactory.create(Types.STRING);
		cols.add(keyColumn);
		return df1.join(scope, df2, cols, "inner");
	}

	/**
	 * Inner-joins two dataframes on several common key columns.
	 */
	@operator (
			value = "join",
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Inner-joins two dataframes on the given list of common key columns.",
			usages = { @usage (
					value = "Join two dataframes on two key columns",
					examples = { @example (
							value = "join(df1, df2, [\"country\", \"year\"])",
							isExecutable = false) }) },
			see = { "join" })
	@tests ({
			@test ("(join(dataframe_with([\"id\",\"name\"], [[1,\"Alice\"],[2,\"Bob\"]]), dataframe_with([\"id\",\"salary\"], [[1,55000],[2,48000]]), [\"id\"])).rows = 2"),
			@test ("dataframe people   <- dataframe_with([\"id\", \"name\"],   [[1, \"Alice\"], [2, \"Bob\"], [3, \"Charlie\"]]); dataframe salaries <- dataframe_with([\"id\", \"salary\"], [[1, 55000], [2, 48000]]); (join(people, salaries, [\"id\"])).rows = 2"),
			// only (FR,2020) and (DE,2020) match on both keys
			@test ("dataframe a <- dataframe_with([\"country\", \"year\", \"gdp\"], [[\"FR\", 2020, 100], [\"FR\", 2021, 110], [\"DE\", 2020, 120]]); dataframe b <- dataframe_with([\"country\", \"year\", \"pop\"], [[\"FR\", 2020, 67], [\"DE\", 2020, 83]]); (join(a, b, [\"country\", \"year\"])).rows = 2")
	})
	public static IDataFrame join(final IScope scope, final IDataFrame df1, final IDataFrame df2,
			final IList<String> keyColumns) {
		return df1.join(scope, df2, keyColumns, "inner");
	}

	/**
	 * Joins two dataframes on several key columns using an explicit join type.
	 */
	@operator (
			value = "join",
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Joins two dataframes on the given key columns using an explicit join type: \"inner\" (default), "
					+ "\"left\", \"right\" or \"full\".",
			usages = { @usage (
					value = "Left-join two dataframes on 'id' (keeps every row of the left dataframe)",
					examples = { @example (
							value = "join(df1, df2, [\"id\"], \"left\")",
							isExecutable = false) }) },
			see = { "join" })
	@tests ({
			@test ("(join(dataframe_with([\"id\",\"name\"], [[1,\"Alice\"],[2,\"Bob\"],[3,\"Charlie\"]]), dataframe_with([\"id\",\"salary\"], [[1,55000],[2,48000]]), [\"id\"], \"left\")).rows = 3"),
			// join types
			@test ("dataframe people   <- dataframe_with([\"id\", \"name\"],   [[1, \"Alice\"], [2, \"Bob\"], [3, \"Charlie\"]]); dataframe salaries <- dataframe_with([\"id\", \"salary\"], [[1, 55000], [2, 48000]]); (join(people, salaries, [\"id\"], \"inner\")).rows = 2"),
			@test ("dataframe people2   <- dataframe_with([\"id\", \"name\"],   [[1, \"Alice\"], [2, \"Bob\"], [3, \"Charlie\"]]); dataframe salaries2 <- dataframe_with([\"id\", \"salary\"], [[1, 55000], [2, 48000]]); (join(people2, salaries2, [\"id\"], \"left\")).rows  = 3"),
			@test ("dataframe people3   <- dataframe_with([\"id\", \"name\"],   [[1, \"Alice\"], [2, \"Bob\"], [3, \"Charlie\"]]); dataframe salaries3 <- dataframe_with([\"id\", \"salary\"], [[1, 55000], [2, 48000]]); (join(people3, salaries3, [\"id\"], \"right\")).rows = 2"),
			@test ("dataframe people4   <- dataframe_with([\"id\", \"name\"],   [[1, \"Alice\"], [2, \"Bob\"], [3, \"Charlie\"]]); dataframe salaries4 <- dataframe_with([\"id\", \"salary\"], [[1, 55000], [2, 48000]]); (join(people4, salaries4, [\"id\"], \"full\")).rows  = 3")
	})
	public static IDataFrame join(final IScope scope, final IDataFrame df1, final IDataFrame df2,
			final IList<String> keyColumns, final String joinType) {
		return df1.join(scope, df2, keyColumns, joinType);
	}

	// ========================= Concatenation via '+' (merged operator)
	// =========================

	/**
	 * Vertically concatenates two dataframes with the '+' operator.
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Vertically concatenates two dataframes (appends the rows of the second to the first). "
					+ "Both dataframes must share the same column structure.",
			see = { "join" })
	@test ("(dataframe_with([\"a\",\"b\"], [[1,2]]) + dataframe_with([\"a\",\"b\"], [[3,4],[5,6]])).rows = 3")
	public static IDataFrame plus(final IScope scope, final IDataFrame df1, final IDataFrame df2) {
		return df1.mergeWith(df2);
	}

	/**
	 * Appends a single row (given as a list of values) to a dataframe with the '+' operator.
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Appends a single row to the dataframe. The list of values must match the number of columns.",
			see = { "join" })
	@test ("(dataframe_with([\"a\",\"b\"], [[1,2]]) + [3,4]).rows = 2")
	public static IDataFrame plus(final IScope scope, final IDataFrame df, final IList<Object> row) {
		return df.addRow(row);
	}

	// ========================= Pivot operator =========================

	/**
	 * Pivots a dataframe.
	 */
	@operator (
			value = "pivot",
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Pivots the dataframe: the index column becomes row labels, the pivot column values become new column names, "
					+ "and the value column provides the cell values. When multiple values exist for a combination, the first is kept.",
			usages = { @usage (
					value = "Pivot a sales dataframe",
					examples = { @example (
							value = "dataframe pivoted <- pivot(sales_df, \"product\", \"quarter\", \"revenue\");",
							isExecutable = false) }) },
			see = { "filter", "select_columns" })
	@tests ({
			@test ("(pivot(dataframe_with([\"product\",\"quarter\",\"revenue\"], [[\"Widget\",\"Q1\",1000],[\"Widget\",\"Q2\",1500],[\"Gadget\",\"Q1\",800],[\"Gadget\",\"Q2\",950]]), \"product\", \"quarter\", \"revenue\")).rows = 2"),
			@test ("dataframe df <- dataframe_with( [\"product\", \"quarter\", \"revenue\"], [[\"A\", \"Q1\", 100], [\"A\", \"Q2\", 200], [\"B\", \"Q1\", 150], [\"B\", \"Q2\", 250]] ); dataframe pivoted <- pivot(df, \"product\", \"quarter\", \"revenue\"); pivoted.rows = 2"),
			@test ("dataframe df2 <- dataframe_with( [\"product\", \"quarter\", \"revenue\"], [[\"A\", \"Q1\", 100], [\"A\", \"Q2\", 200], [\"B\", \"Q1\", 150], [\"B\", \"Q2\", 250]] ); dataframe pivoted2 <- pivot(df2, \"product\", \"quarter\", \"revenue\"); pivoted2.keys contains \"Q1\""),
			@test ("dataframe df3 <- dataframe_with( [\"product\", \"quarter\", \"revenue\"], [[\"A\", \"Q1\", 100], [\"A\", \"Q2\", 200], [\"B\", \"Q1\", 150], [\"B\", \"Q2\", 250]] ); dataframe pivoted3 <- pivot(df3, \"product\", \"quarter\", \"revenue\"); pivoted3.keys contains \"Q2\"")
	})
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
			value = "pretty_print",
			can_be_const = true,
			type = IType.STRING,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
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
			value = "pretty_print",
			can_be_const = true,
			type = IType.STRING,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
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
	// iloc(df, i, [j,...]) -> row i restricted to given cols (cf. df.iloc[i,
	// [j,...]])
	// iloc(df, [i,...], j) -> col j restricted to given rows (cf. df.iloc[[i,...],
	// j])
	// iloc(df, [i,...]) -> sub-dataframe with given rows (cf. df.iloc[[i,...]])
	// iloc(df, [i,...], [j,...]) -> sub-dataframe (cf. df.iloc[[i,...], [j,...]])
	//
	// Negative indices are supported on both axes (Python-style: -1 = last
	// element).

	/**
	 * Pandas-style {@code df.iloc[i]}: returns row i as a list of values.
	 */
	@operator (
			value = "iloc",
			can_be_const = true,
			content_type = IType.NONE,
			type = IType.LIST,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Pandas-style df.iloc[i]: returns the row at integer position i as a list of values. "
					+ "Negative indices are supported (-1 is the last row).",
			usages = { @usage (
					value = "Get the last row",
					examples = { @example (
							value = "list row <- iloc(my_df, -1);",
							isExecutable = false) }) },
			see = { "row_at", "cell" })
	@tests ({
			@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 0) = [\"Alice\",30]"),
			@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), -1) = [\"Bob\",25]"),
			@test ("dataframe df <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); iloc(df, 0) = [\"Alice\", 30]"),
			@test ("dataframe df2 <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); iloc(df2, -1) = [\"Bob\", 25]")
	})
	public static IList<Object> ilocRow(final IScope scope, final IDataFrame df, final Integer rowIndex) {
		return df.ilocRow(scope, rowIndex);
	}

	/**
	 * Pandas-style {@code df.iloc[i, j]}: returns a single cell value at the given integer (row, col) position.
	 */
	@operator (
			value = "iloc",
			can_be_const = true,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Pandas-style df.iloc[i, j]: returns the cell value at integer (row, col) position. "
					+ "Negative indices are supported on both axes.",
			usages = { @usage (
					value = "Get the cell at row 1, column 0",
					examples = { @example (
							value = "unknown v <- iloc(my_df, 1, 0);",
							isExecutable = false) }) },
			see = { "cell", "row_at", "column_at" })
	@tests ({
			@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 1, 0) = \"Bob\""),
			@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), 0, 1) = 30"),
			@test ("iloc(dataframe_with([\"name\",\"age\"], [[\"Alice\",30],[\"Bob\",25]]), -1, -1) = 25"),
			@test ("dataframe df <- dataframe_with([\"name\", \"age\"], [[\"Alice\", 30], [\"Bob\", 25]]); iloc(df, 1, 0) = \"Bob\"")
	})
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
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Pandas-style df.iloc[i, [j, ...]]: returns the values of row i taken from the selected columns, "
					+ "in the order of the input column indices. Negative indices are supported.",
			usages = { @usage (
					value = "Get cells [0] and [2] of row 1",
					examples = { @example (
							value = "list values <- iloc(my_df, 1, [0, 2]);",
							isExecutable = false) }) },
			see = { "row_at", "select_columns" })
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
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Pandas-style df.iloc[[i, ...], j]: returns the values of column j taken from the selected rows, "
					+ "in the order of the input row indices. Negative indices are supported.",
			usages = { @usage (
					value = "Get column 1 values at rows 0 and 2",
					examples = { @example (
							value = "list values <- iloc(my_df, [0, 2], 1);",
							isExecutable = false) }) },
			see = { "column_at", "row_at" })
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
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
	@doc (
			value = "Pandas-style df.iloc[[i, ...]]: returns a new dataframe containing only the rows at the given "
					+ "integer indices (in that order). All columns are kept. Negative indices are supported.",
			usages = { @usage (
					value = "Select rows 0 and 2",
					examples = { @example (
							value = "dataframe sub <- iloc(my_df, [0, 2]);",
							isExecutable = false) }) },
			see = { "row_at", "filter" })
	@test ("(iloc(dataframe_with([\"name\"], [[\"Alice\"],[\"Bob\"],[\"Eve\"]]), [0,2])).rows = 2")
	@test ("cell(iloc(dataframe_with([\"name\"], [[\"Alice\"],[\"Bob\"],[\"Eve\"]]), [0,2]), 1, \"name\") = \"Eve\"")
	public static IDataFrame ilocRows(final IScope scope, final IDataFrame df, final IList<Integer> rowIndices) {
		return df.ilocRows(scope, rowIndices);
	}

	/**
	 * Pandas-style {@code df.iloc[[i, ...], [j, ...]]}: returns a sub-dataframe with the selected rows and columns.
	 */
	@operator (
			value = "iloc",
			can_be_const = true,
			type = IDataframeConstants.ID,
			category = { IDataframeConstants.CATEGORY },
			concept = { IDataframeConstants.CONCEPT })
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
			see = { "select_columns", "filter" })
	@test ("(iloc(dataframe_with([\"a\",\"b\",\"c\"], [[1,2,3],[4,5,6]]), [0], [0,2])).keys = [\"a\",\"c\"]")
	@test ("cell(iloc(dataframe_with([\"a\",\"b\",\"c\"], [[1,2,3],[4,5,6]]), [1], [2]), 0, \"c\") = 6")
	public static IDataFrame iloc(final IScope scope, final IDataFrame df, final IList<Integer> rowIndices,
			final IList<Integer> colIndices) {
		return df.iloc(scope, rowIndices, colIndices);
	}

}
