/**
* Name: Dataframe File IO
* Author: GAMA Team
* Description: Demonstrates loading and saving dataframes from/to CSV, Excel, and JSON files.
*   Shows how to use different separators and character encodings for CSV files.
*   File paths are relative to the model file location, following GAMA conventions.
* Tags: dataframe, csv, excel, json, load_file, save, import, export, io
*/

model DataframeFileIO

global {

	init {
		// ===== 1. Load CSV (default: comma, header, UTF-8) =====
		write "===== CSV Import =====";
		dataframe csv_data <- df_load_csv("../includes/sample_data.csv");
		write "Loaded CSV: " + (csv_data.rows) + " rows, columns: " + (csv_data.keys);

		// ===== 2. Load CSV with custom separator and encoding =====
		write "";
		write "===== CSV Import (semicolon, ISO-8859-1) =====";
		dataframe csv_semi <- df_load_csv_with("../includes/sample_data_fr.csv", ";", true, "ISO-8859-1");
		write "Loaded: " + (csv_semi.rows) + " rows, columns: " + (csv_semi.keys);

		// ===== 3. Load CSV without header =====
		write "";
		write "===== CSV Import (no header) =====";
		dataframe csv_noheader <- df_load_csv_with("../includes/sample_noheader.csv", ",", false, "UTF-8");
		write "Loaded: " + (csv_noheader.rows) + " rows, auto-generated columns: " + (csv_noheader.keys);

		// ===== 4. Load Excel =====
		write "";
		write "===== Excel Import =====";
		dataframe excel_data <- df_load_excel("../includes/sample_data.xlsx");
		write "Loaded Excel: " + (excel_data.rows) + " rows, columns: " + (excel_data.keys);

		// ===== 5. Load JSON =====
		write "";
		write "===== JSON Import =====";
		dataframe json_data <- df_load_json("../includes/sample_data.json");
		write "Loaded JSON: " + df_rows(json_data) + " rows, columns: " + df_columns(json_data);

		// ===== 6. Save to CSV =====
		write "";
		write "===== CSV Export =====";
		dataframe to_save <- dataframe_with(
			["city", "population", "country"],
			[["Paris", 2161000, "France"], ["Lyon", 516092, "France"], ["Marseille", 870018, "France"]]
		);
		bool csv_ok <- df_save_csv(to_save, "../results/cities.csv");
		write "Saved to CSV: " + csv_ok;

		// ===== 7. Save to CSV with custom separator =====
		bool csv_semi_ok <- df_save_csv_with(to_save, "../results/cities_semicolon.csv", ";", "UTF-8");
		write "Saved to CSV (semicolon): " + csv_semi_ok;

		// ===== 8. Save to Excel =====
		write "";
		write "===== Excel Export =====";
		bool excel_ok <- df_save_excel(to_save, "../results/cities.xlsx", "Cities");
		write "Saved to Excel: " + excel_ok;

		// ===== 9. Save to JSON =====
		write "";
		write "===== JSON Export =====";
		bool json_ok <- df_save_json(to_save, "../results/cities.json");
		write "Saved to JSON: " + json_ok;

		// ===== 10. Round-trip test =====
		write "";
		write "===== Round-trip =====";
		dataframe reloaded <- df_load_csv("../results/cities.csv");
		write "Reloaded: " + (reloaded.rows) + " rows, columns: " + (reloaded.keys);
		write "First city: " + df_cell(reloaded, 0, "city");
	}
}

experiment main type: gui;
