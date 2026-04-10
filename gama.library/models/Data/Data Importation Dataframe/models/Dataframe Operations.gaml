/**
* Name: Dataframe Operations
* Author: GAMA Team
* Description: Demonstrates common dataframe operations: creating from scratch, filtering rows,
*   selecting columns, adding columns and rows, and chaining operations together.
*   Dataframes are immutable — each operation returns a new dataframe, leaving the original unchanged.
* Tags: dataframe, filter, select, create, column, row, tabular
*/

model DataframeOperations

global {

	init {
		// ===== 1. Create a dataframe from scratch =====
		write "===== Creation =====";
		dataframe students <- dataframe_with(
			["name", "age", "city", "grade"],
			[
				["Alice", 22, "Paris", 16.5],
				["Bob", 24, "Lyon", 12.0],
				["Charlie", 21, "Paris", 14.0],
				["Diana", 23, "Marseille", 18.0],
				["Eve", 22, "Paris", 15.5]
			]
		);
		write "Students dataframe: " + df_rows(students) + " rows, " + df_columns(students);

		// ===== 2. Filter rows =====
		write "";
		write "===== Filtering =====";
		dataframe paris_students <- df_filter(students, "city", "Paris");
		write "Students in Paris: " + df_rows(paris_students);
		write "Names: " + df_column(paris_students, "name");

		// ===== 3. Select columns =====
		write "";
		write "===== Column selection =====";
		dataframe names_grades <- df_select_columns(students, ["name", "grade"]);
		write "Columns: " + df_columns(names_grades);
		write "Grades: " + df_column(names_grades, "grade");

		// ===== 4. Add a column =====
		write "";
		write "===== Add column =====";
		dataframe with_status <- df_add_column(students, "status", "enrolled");
		write "New columns: " + df_columns(with_status);
		write "Status of row 0: " + df_cell(with_status, 0, "status");
		// Original is unchanged
		write "Original columns (unchanged): " + df_columns(students);

		// ===== 5. Add a row =====
		write "";
		write "===== Add row =====";
		dataframe with_new_student <- df_add_row(students, ["Frank", 25, "Toulouse", 13.5]);
		write "Rows after add: " + df_rows(with_new_student);
		write "Last row: " + df_row(with_new_student, df_rows(with_new_student) - 1);

		// ===== 6. Remove rows with empty values =====
		write "";
		write "===== Remove empty =====";
		dataframe with_gaps <- dataframe_with(
			["name", "email"],
			[["Alice", "alice@example.com"], ["Bob", ""], ["Charlie", nil], ["Diana", "diana@example.com"]]
		);
		write "Before cleanup: " + df_rows(with_gaps) + " rows";
		dataframe cleaned <- df_remove_empty(with_gaps, "email");
		write "After cleanup: " + df_rows(cleaned) + " rows";
		write "Remaining names: " + df_column(cleaned, "name");

		// ===== 7. Chain operations =====
		write "";
		write "===== Chained operations =====";
		// Get names and grades of Paris students only
		dataframe result <- df_select_columns(
			df_filter(students, "city", "Paris"),
			["name", "grade"]
		);
		write "Paris students grades:";
		loop i from: 0 to: df_rows(result) - 1 {
			write "  " + df_cell(result, i, "name") + " -> " + df_cell(result, i, "grade");
		}
		
		write df_pretty_print(result);
	}
}

experiment main type: gui;
