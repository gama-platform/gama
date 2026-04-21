/**
* Name: Dataframe Join and Pivot
* Author: GAMA Team
* Description: Demonstrates advanced dataframe operations: merging (vertical concatenation),
*   joining on a common column (inner join), and pivoting tables. These operations
*   are essential for combining data from multiple sources or reshaping data for analysis.
* Tags: dataframe, join, merge, pivot, data, tabular
*/

model DataframeJoinPivot

global {

	init {
		// ===== 1. Merge (vertical concatenation) =====
		write "===== Merge =====";
		dataframe batch1 <- dataframe_with(
			["sensor", "value"],
			[["temp", 22.5], ["humidity", 60.0]]
		);
		dataframe batch2 <- dataframe_with(
			["sensor", "value"],
			[["temp", 23.1], ["humidity", 58.5], ["pressure", 1013.2]]
		);
		dataframe all_readings <- df_merge(batch1, batch2);
		write "Merged: " + (all_readings.rows) + " rows";
		loop i from: 0 to: (all_readings.rows) - 1 {
			write "  " + df_cell(all_readings, i, "sensor") + " = " + df_cell(all_readings, i, "value");
		}

		// ===== 2. Join (inner join on common column) =====
		write "";
		write "===== Join =====";
		dataframe people <- dataframe_with(
			["id", "name", "department"],
			[[1, "Alice", "Engineering"], [2, "Bob", "Marketing"], [3, "Charlie", "Engineering"], [4, "Diana", "Sales"]]
		);
		dataframe salaries <- dataframe_with(
			["id", "salary"],
			[[1, 55000], [2, 48000], [4, 52000]]
		);
		// Inner join: only employees with matching salary records
		dataframe employee_data <- df_join(people, salaries, "id");
		write "Joined: " + (employee_data.rows) + " rows (Charlie excluded: no salary record)";
		write "Columns: " + (employee_data.keys);
		loop i from: 0 to: (employee_data.rows) - 1 {
			write "  " + df_cell(employee_data, i, "name") + " earns " + df_cell(employee_data, i, "salary");
		}

		// ===== 3. Pivot =====
		write "";
		write "===== Pivot =====";
		dataframe sales <- dataframe_with(
			["product", "quarter", "revenue"],
			[
				["Widget", "Q1", 1000],
				["Widget", "Q2", 1500],
				["Widget", "Q3", 1200],
				["Gadget", "Q1", 800],
				["Gadget", "Q2", 950],
				["Gadget", "Q3", 1100]
			]
		);
		write "Original sales data: " + sales.rows + " rows";

		// Pivot: rows = products, columns = quarters, values = revenue
		dataframe pivot_table <- df_pivot(sales, "product", "quarter", "revenue");
		write "Pivoted: " + (pivot_table.rows) + " rows x " + (pivot_table.columns);
		loop i from: 0 to: (pivot_table.rows) - 1 {
			string line <- "  " + df_cell(pivot_table, i, "product");
			loop col over: pivot_table.keys {
				if (col != "product") {
					line <- line + "  |  " + col + "=" + df_cell(pivot_table, i, col);
				}
			}
			write line;
		}
	}
}

experiment main type: gui;
