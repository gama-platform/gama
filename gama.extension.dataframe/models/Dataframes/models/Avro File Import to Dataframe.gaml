/**
* Name: Avro File Import to Dataframe
* Author: GAMA Team
* Description: Shows how to load an Avro file into a dataframe with dataframe_file and explore its contents.
*   Avro is a compact, schema-based binary format widely used for data exchange and streaming (Kafka,
*   Hadoop, ...). The schema (column names and types) is embedded in the file, so the resulting dataframe
*   keeps named, typed columns — just like the CSV, JSON, Excel and Parquet importers.
* Tags: avro, dataframe, load_file, import, data, tabular
*/

model AvroFileImportToDataframe

global {
	// Load an Avro file into a dataframe.
	// The file path is relative to the model file location.
	dataframe employees <- dataframe(dataframe_file("../includes/employees.avro"));

	init {
		write "===== Dataframe loaded from Avro =====";
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
