/**
* Name: Dataframe iloc and Indexing
* Author: GAMA Team
* Description: Demonstrates integer-based indexing (iloc) and column-type inspection on dataframes.
*   iloc mirrors pandas' DataFrame.iloc interface:
*     iloc(df, i)              -> row i as a list of values
*     iloc(df, i, j)           -> single cell value
*     iloc(df, i, [j,...])     -> row i restricted to given columns
*     iloc(df, [i,...], j)     -> column j restricted to given rows
*     iloc(df, [i,...])        -> sub-dataframe with given rows (all columns)
*     iloc(df, [i,...], [j,...]) -> sub-dataframe
*   Negative indices work like Python: -1 is the last row/column.
*   All operations are non-destructive (return a new dataframe).
* Tags: dataframe, iloc, indexing, column types, tabular
*/

model DataframeIloc

global {

	init {
		// Base dataframe used throughout this model
		dataframe sensors <- dataframe_with(
			["id", "sensor", "location", "value", "unit"],
			[
				[1, "temp",     "room_A", 22.5, "°C"],
				[2, "humidity", "room_A", 61.0, "%"],
				[3, "temp",     "room_B", 19.8, "°C"],
				[4, "co2",      "room_B", 412,  "ppm"],
				[5, "temp",     "room_C", 24.1, "°C"],
				[6, "humidity", "room_C", 55.5, "%"],
				[7, "co2",      "room_A", 398,  "ppm"]
			]
		);

		write df_pretty_print(sensors);

		// ===== 1. Column metadata =====
		write "";
		write "===== Column metadata =====";
		write "Columns      : " + (sensors.keys);
		write "Row count    : " + (sensors.rows);

		// ===== 2. iloc(df, i) — single row as a list =====
		write "";
		write "===== iloc(df, i) — single row =====";
		list first_row <- iloc(sensors, 0);
		write "Row 0  : " + first_row;

		list last_row <- iloc(sensors, -1);    // negative index: last row
		write "Row -1 : " + last_row;

		// ===== 3. iloc(df, i, j) — single cell =====
		write "";
		write "===== iloc(df, i, j) — single cell =====";
		unknown v00 <- iloc(sensors, 0, 0);    // first row, first col  -> 1 (id)
		write "iloc(0, 0) = " + v00;

		unknown v12 <- iloc(sensors, 1, 2);    // row 1, col 2 -> "room_A" (location)
		write "iloc(1, 2) = " + v12;

		unknown vNeg <- iloc(sensors, -1, -1); // last row, last col -> "ppm"
		write "iloc(-1,-1) = " + vNeg;

		// ===== 4. iloc(df, i, [j,...]) — row i, selected columns =====
		write "";
		write "===== iloc(df, i, [j,...]) — row restricted to columns =====";
		list row2_cols <- iloc(sensors, 2, [1, 2, 3]);  // sensor, location, value of row 2
		write "Row 2, cols [1,2,3] : " + row2_cols;

		list row_neg_cols <- iloc(sensors, -2, [0, 4]);  // id and unit of second-to-last row
		write "Row -2, cols [0,4]  : " + row_neg_cols;

		// ===== 5. iloc(df, [i,...], j) — column j, selected rows =====
		write "";
		write "===== iloc(df, [i,...], j) — column restricted to rows =====";
		list values_col <- iloc(sensors, [0, 2, 4, 6], 3);  // 'value' column at even rows
		write "Col 3 at rows [0,2,4,6] : " + values_col;

		list sensors_col <- iloc(sensors, range(0, 2), 1); // 'sensor' col for first 3 rows
		write "Col 1 at rows 0..2 : " + sensors_col;

		// ===== 6. iloc(df, [i,...]) — sub-dataframe, selected rows =====
		write "";
		write "===== iloc(df, [i,...]) — sub-dataframe (row selection) =====";
		dataframe temp_rows <- iloc(sensors, [0, 2, 4]);  // rows 0, 2, 4 (temp sensors)
		write "Temp sensor rows:";
		write df_pretty_print(temp_rows);

		dataframe last_three <- iloc(sensors, [-3, -2, -1]);  // last 3 rows via negative indices
		write "Last 3 rows:";
		write df_pretty_print(last_three);

		// ===== 7. iloc(df, [i,...], [j,...]) — sub-dataframe, rows and columns =====
		write "";
		write "===== iloc(df, [i,...], [j,...]) — sub-dataframe (row+col) =====";
		dataframe sub <- iloc(sensors, [0, 1, 2], [1, 2, 3]);  // rows 0-2, cols sensor/location/value
		write "Rows 0..2, cols 1..3:";
		write df_pretty_print(sub);

		dataframe inverted <- iloc(sensors, [6, 5, 4, 3], [0, 3, 4]);  // reversed rows + 3 cols
		write "Rows [6,5,4,3] (reversed), cols [0,3,4]:";
		write df_pretty_print(inverted);

		// ===== 8. Using iloc for row iteration (compare with df_row) =====
		write "";
		write "===== Row iteration with iloc =====";
		write "All sensor readings (iloc loop):";
		loop i from: 0 to: (sensors.rows) - 1 {
			list row <- iloc(sensors, i);
			write "  [" + i + "] id=" + row[0] + "  " + row[1] + " @ " + row[2] + " -> " + row[3] + " " + row[4];
		}

		// ===== 9. Combining iloc with other operators =====
		write "";
		write "===== Combining iloc with df_filter / df_select_columns =====";
		// Filter temperature sensors, then iloc to get first 2 rows and cols 1-3
		dataframe temp_sensors <- df_filter(sensors, "sensor", "temp");
		write "Temp sensors full:";
		write df_pretty_print(temp_sensors);

		dataframe temp_preview <- iloc(temp_sensors, [0, 1], [1, 2, 3]);
		write "First 2 temp sensors, cols sensor/location/value:";
		write df_pretty_print(temp_preview);

		// ===== 10. Negative-index tricks =====
		write "";
		write "===== Negative index tricks =====";
		// Last column of all rows via mixed iloc
		int last_col <- length(df_columns(sensors)) - 1;
		list units <- iloc(sensors, range(0, df_rows(sensors) - 1), last_col);
		write "All units (last column): " + units;

		// First and last row values for column 3 (value)
		unknown first_val <- iloc(sensors, 0, 3);
		unknown last_val  <- iloc(sensors, -1, 3);
		write "First value reading: " + first_val + ", last: " + last_val;
	}
}

experiment main type: gui;
