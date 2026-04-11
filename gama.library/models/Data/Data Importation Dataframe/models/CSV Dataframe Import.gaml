/**
* Name: CSV Dataframe Import
* Author: GAMA Team
* Description: Shows how to load a CSV file into a dataframe and explore its contents.
*   Unlike matrix-based CSV import, dataframes preserve column names and provide
*   direct column-based access. This is useful when working with tabular data
*   that has named columns (like survey results, census data, or sensor readings).
* Tags: csv, dataframe, load_file, data, import, tabular
*/

model CSVDataframeImport

global {
	// Load a CSV file into a dataframe (comma-separated, with header, UTF-8)
	// The file path is relative to the model file location
	dataframe iris_data <- df_load_csv("../includes/iris.csv");

	init {
		write "===== Dataframe loaded from CSV =====";
		write "Columns: " + df_columns(iris_data);
		write "Number of rows: " + df_rows(iris_data);
		write "";

		// Access a specific column
		list sepal_lengths <- df_column(iris_data, "sepallength");
		write "First 5 sepal lengths: " + copy_between(sepal_lengths, 0, 5);

		// Access a specific row
		list first_row <- df_row(iris_data, 0);
		write "First row: " + first_row;

		// Access a specific cell
		write "Species of row 0: " + df_cell(iris_data, 0, "type");
		write "Species of row 50: " + df_cell(iris_data, 50, "type");

		// Use pseudo-variables
		write "";
		write "===== Pseudo-variables =====";
		write "iris_data.columns = " + iris_data.columns;
		write "iris_data.rows = " + iris_data.rows;
		write "iris_data.cols = " + iris_data.cols;
	}
}

experiment main type: gui;
