/**
* Name: Dataframes
* Author: GAMA Team
* Description: Examples of the syntax and various operators used to manipulate the 'dataframe' data type in GAML.
*   A dataframe is an immutable, column-oriented table (like a pandas DataFrame): rows are indexed by position,
*   columns by name. This model demonstrates creating dataframes from scratch and from a CSV file, inspecting
*   their schema, accessing columns/rows/cells, positional indexing with iloc (pandas-style, negative indices
*   allowed), combining dataframes (filter, select, add, merge, join, pivot) and looping over them.
*   Because dataframes are immutable, every operator returns a NEW dataframe and leaves the original unchanged.
*   Read the comments and run the model to get a better understanding of how to use dataframes effectively in GAML.
* Tags: dataframe, container, tabular, iloc, filter, join, pivot, csv, immutable
*/

model Dataframes

species declaring_dataframes {

	/**
	 * Declarations and creation of dataframes
	 */
	// A dataframe is built with 'dataframe_with(column_names, rows)': the first argument is the list of column
	// names, the second is a list of rows (each row a list of values, in the same order as the columns).
	dataframe students <- dataframe_with(
		["name", "age", "city", "grade"],
		[
			["Alice",   22, "Paris",     16.5],
			["Bob",     24, "Lyon",      12.0],
			["Charlie", 21, "Paris",     14.0],
			["Diana",   23, "Marseille", 18.0],
			["Eve",     22, "Paris",     15.5]
		]
	);
	// An empty dataframe can be declared by giving only its column names and an empty list of rows.
	dataframe empty_df <- dataframe_with(["col_a", "col_b"], []);
	// A dataframe can also be loaded directly from a CSV file (comma separator, header, UTF-8).
	// The file path is relative to the model file location.
	dataframe iris <- df_load_csv("includes/iris_small.csv");

	init {
		write "";
		write "== DECLARING DATAFRAMES ==";
		write "";
		// The pseudo-attributes 'keys', 'rows' and 'columns' describe the schema.
		write sample(students.keys);     // column names
		write sample(students.rows);     // number of rows
		write sample(students.columns);  // number of columns
		write sample(empty_df.keys);
		write sample(empty_df.rows);
		// df_pretty_print returns a nicely formatted, human-readable table.
		write df_pretty_print(students);
		write "";
		// A dataframe loaded from CSV infers its columns from the header line.
		write sample(iris.keys);
		write sample(iris.rows);
		write df_pretty_print(iris);
	}
}

species accessing_dataframe_elements {

	dataframe sensors <- dataframe_with(
		["id", "sensor", "location", "value", "unit"],
		[
			[1, "temp",     "room_A", 22.5, "°C"],
			[2, "humidity", "room_A", 61.0, "%"],
			[3, "temp",     "room_B", 19.8, "°C"],
			[4, "co2",      "room_B", 412,  "ppm"],
			[5, "temp",     "room_C", 24.1, "°C"]
		]
	);

	init {
		write "";
		write "== ACCESSING DATAFRAME ELEMENTS ==";
		write "";
		write df_pretty_print(sensors);
		// Column-, row- and cell-based access by name/index.
		write sample(df_column(sensors, "sensor"));   // a whole column as a list
		write sample(df_row(sensors, 0));              // a whole row as a list
		write sample(df_cell(sensors, 1, "location")); // a single cell (row 1, column "location")

		// iloc provides positional (integer) indexing, mirroring pandas' DataFrame.iloc.
		// Negative indices count from the end, exactly like in Python (-1 is the last).
		write sample(iloc(sensors, 0));            // row 0 as a list
		write sample(iloc(sensors, -1));           // last row as a list
		write sample(iloc(sensors, 1, 2));         // single cell: row 1, column 2
		write sample(iloc(sensors, -1, -1));       // last row, last column
		write sample(iloc(sensors, 2, [1, 2, 3])); // row 2, restricted to columns 1,2,3
		write sample(iloc(sensors, [0, 2, 4], 3)); // column 3, restricted to rows 0,2,4

		// iloc with a list of rows (and optionally columns) returns a sub-dataframe.
		write df_pretty_print(iloc(sensors, [0, 2, 4]));            // rows 0,2,4, all columns
		write df_pretty_print(iloc(sensors, [0, 1], [1, 2, 3]));    // rows 0-1, columns 1-3
	}
}

species combining_dataframes {

	dataframe students <- dataframe_with(
		["name", "age", "city", "grade"],
		[
			["Alice",   22, "Paris",     16.5],
			["Bob",     24, "Lyon",      12.0],
			["Charlie", 21, "Paris",     14.0],
			["Diana",   23, "Marseille", 18.0],
			["Eve",     22, "Paris",     15.5]
		]
	);

	init {
		write "";
		write "== COMBINING AND TRANSFORMING DATAFRAMES ==";
		write "";
		// df_filter keeps the rows whose column equals a given value.
		write sample(df_filter(students, "city", "Paris").rows);
		write df_pretty_print(df_filter(students, "city", "Paris"));
		// df_select_columns keeps only the given columns.
		write sample(df_select_columns(students, ["name", "grade"]).keys);
		// Operators can be chained: names and grades of Paris students only.
		write df_pretty_print(df_select_columns(df_filter(students, "city", "Paris"), ["name", "grade"]));

		// df_remove_empty drops the rows with an empty/nil value in a given column.
		dataframe with_gaps <- dataframe_with(
			["name", "email"],
			[["Alice", "alice@x.org"], ["Bob", ""], ["Charlie", nil], ["Diana", "diana@x.org"]]
		);
		write sample(with_gaps.rows);
		write sample(df_remove_empty(with_gaps, "email").rows);

		// df_merge concatenates two dataframes vertically (they must share the same columns).
		dataframe batch1 <- dataframe_with(["sensor", "value"], [["temp", 22.5], ["humidity", 60.0]]);
		dataframe batch2 <- dataframe_with(["sensor", "value"], [["temp", 23.1], ["pressure", 1013.2]]);
		write df_pretty_print(df_merge(batch1, batch2));

		// df_join performs an inner join on a common column.
		dataframe people   <- dataframe_with(["id", "name"],   [[1, "Alice"], [2, "Bob"], [3, "Charlie"]]);
		dataframe salaries <- dataframe_with(["id", "salary"], [[1, 55000],   [2, 48000]]);
		write df_pretty_print(df_join(people, salaries, "id")); // Charlie dropped: no salary record

		// df_pivot reshapes long data into a wide table (rows / columns / values).
		dataframe sales <- dataframe_with(
			["product", "quarter", "revenue"],
			[["Widget", "Q1", 1000], ["Widget", "Q2", 1500], ["Gadget", "Q1", 800], ["Gadget", "Q2", 950]]
		);
		write df_pretty_print(df_pivot(sales, "product", "quarter", "revenue"));
	}
}

species modifying_dataframes {

	init {
		write "";
		write "== \"MODIFYING\" DATAFRAMES (they are immutable) ==";
		write "";
		// Dataframes are IMMUTABLE: there is no in-place 'put'/'add' as on lists or matrices.
		// Operators that look like mutations actually return a NEW dataframe, leaving the original intact.
		dataframe students <- dataframe_with(
			["name", "age"],
			[["Alice", 22], ["Bob", 24]]
		);
		write sample(students.keys);
		write sample(students.rows);

		// df_add_column returns a copy with one extra column (same value for every row).
		dataframe with_status <- df_add_column(students, "status", "enrolled");
		write sample(with_status.keys);
		// df_add_row returns a copy with one extra row.
		dataframe with_frank <- df_add_row(students, ["Frank", 25]);
		write sample(with_frank.rows);

		// The original is unchanged by either operation.
		write sample(students.keys);
		write sample(students.rows);
	}
}

species looping_on_dataframes {

	dataframe sales <- dataframe_with(
		["product", "revenue"],
		[["Widget", 1000], ["Gadget", 800], ["Gizmo", 1200]]
	);

	init {
		write "";
		write "== LOOPING ON DATAFRAMES ==";
		write "";
		// Imperative iteration: loop over the row indices and read cells by column name.
		loop i from: 0 to: sales.rows - 1 {
			write "Row #" + i + ": " + df_cell(sales, i, "product") + " -> " + df_cell(sales, i, "revenue");
		}

		// iloc(df, i) gives a whole row as a list, convenient for unpacking.
		loop i from: 0 to: sales.rows - 1 {
			list row <- iloc(sales, i);
			write "  " + row[0] + " = " + row[1];
		}

		// Functional style: pull a column out as a list and use the usual list operators.
		write sample(sum(list<int>(df_column(sales, "revenue"))));
		write sample(max(list<int>(df_column(sales, "revenue"))));
	}
}

experiment Dataframes type: gui {
	user_command "Declaring dataframes"            {create declaring_dataframes;}
	user_command "Accessing dataframe elements"    {create accessing_dataframe_elements;}
	user_command "Combining dataframes"            {create combining_dataframes;}
	user_command "Modifying dataframes"            {create modifying_dataframes;}
	user_command "Looping on dataframes"           {create looping_on_dataframes;}
}
