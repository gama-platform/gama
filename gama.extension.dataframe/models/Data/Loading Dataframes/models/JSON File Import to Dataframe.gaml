/**
* Name: JSON File Import to Dataframe
* Author: GAMA Team
* Description: Shows how to load a JSON file into a dataframe with dataframe_file and explore its
*   contents. The JSON file must contain an array of objects with consistent keys — each object
*   becomes a row and each key becomes a column. This is the tabular counterpart to the
*   "JSON File Import" model, which reads hierarchical JSON into a map.
* Tags: json, dataframe, load_file, import, data, tabular
*/

model JSONFileImportToDataframe

global {
	// Load a JSON array of objects into a dataframe.
	// The file path is relative to the model file location.
	dataframe employees <- dataframe(dataframe_file("../includes/employees.json"));

	init {
		write "===== Dataframe loaded from JSON =====";
		write "Columns        : " + employees.keys;
		write "Number of rows : " + employees.rows;
		write "";
		write pretty_print(employees);

		// Access a specific column
		write "";
		write "===== Column access =====";
		write "All names    : " + (employees column_at "name");
		write "All salaries : " + (employees column_at "salary");

		// Access a specific row and cell
		write "";
		write "===== Row and cell access =====";
		write "First row          : " + (employees row_at 0);
		write "Department of row 3: " + cell(employees, 3, "department");

		// Filter and select on the imported data
		write "";
		write "===== Filtering =====";
		dataframe engineers <- select_columns(
			filter(employees, "department", "Engineering"),
			["name", "city", "salary"]
		);
		write "Engineers (" + engineers.rows + " rows):";
		write pretty_print(engineers);
	}
}

experiment main type: gui;
