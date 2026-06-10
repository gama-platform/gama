/**
* Name: Excel File Import to Dataframe
* Author: GAMA Team
* Description: Demonstrates loading an Excel workbook (.xlsx) into a dataframe with df_load_excel,
*   then exploring a real-world dataset of 149 116 coffee-shop transactions: schema inspection,
*   column and cell access, positional sampling, filtering and on-the-fly aggregations rendered
*   as charts. Unlike a matrix import, a dataframe preserves column names and types.
*
*   Operations demonstrated:
*     - df_load_excel       : load the first sheet of an .xlsx file
*     - keys / rows         : schema inspection
*     - df_column / df_cell : access columns and cells
*     - df_filter           : subset by store location or product category
*     - df_select_columns   : keep only relevant columns
*     - iloc                : positional sampling
*     - df_pretty_print     : compact display
*
*   To save a dataframe back to Excel, see "Save to Excel" in Data Exportation.
*
* Dataset: Coffee Shop Sales
*   Source : https://www.kaggle.com/datasets/ahmedabbas757/coffee-sales
*   Author : ahmedabbas757
*   License: GNU Lesser General Public License v3.0 (LGPL-3.0)
*            https://www.gnu.org/licenses/lgpl-3.0.html
*
* Tags: excel, dataframe, load_file, import, aggregation, chart, coffee, tabular
*/

model ExcelFileImportToDataframe

global {

	// -----------------------------------------------------------------------
	// Load the Excel file at startup (first sheet: "Transactions")
	// -----------------------------------------------------------------------
	dataframe transactions <- df_load_excel("../includes/Coffee Shop Sales.xlsx");

	// Aggregated summaries — built in init, used by charts
	dataframe by_store    <- dataframe_with(["store", "revenue", "transactions"], []);
	dataframe by_category <- dataframe_with(["category", "revenue", "avg_price", "transactions"], []);

	init {

		// ===== 1. Schema inspection =====
		write "===== Schema =====";
		write "Rows    : " + (transactions.rows);
		write "Columns : " + (transactions.keys);
		write "";
		write "First 5 rows (all columns):";
		write df_pretty_print(iloc(transactions, range(0, 4)), 5, 11, 18);

		// ===== 2. Column access =====
		write "";
		write "===== Spot checks =====";
		write "First transaction_id  : " + iloc(transactions, 0,  0);
		write "Last  transaction_date: " + iloc(transactions, -1, 1);
		write "Cell (row 2, 'store_location'): " + df_cell(transactions, 2, "store_location");

		// ===== 3. Revenue aggregation by store =====
		write "";
		write "===== Revenue by store =====";

		list<string> stores <- ["Lower Manhattan", "Hell's Kitchen", "Astoria"];

		loop store over: stores {
			dataframe sub <- df_filter(transactions, "store_location", store);
			float rev   <- 0.0;
			int   count <- sub.rows;
			loop i from: 0 to: count - 1 {
				float qty   <- float(df_cell(sub, i, "transaction_qty"));
				float price <- float(df_cell(sub, i, "unit_price"));
				rev <- rev + qty * price;
			}
			rev <- round(rev * 100) / 100.0;
			write "  " + store + " : $" + rev + "  (" + count + " transactions)";
			by_store <- df_add_row(by_store, [store, rev, count]);
		}

		write "";
		write "By-store summary:";
		write df_pretty_print(by_store, 5, 3, 20);

		// ===== 4. Revenue aggregation by product category =====
		write "";
		write "===== Revenue by product category =====";

		list<string> categories <- [
			"Coffee", "Tea", "Bakery", "Drinking Chocolate",
			"Flavours", "Coffee beans", "Loose Tea", "Branded", "Packaged Chocolate"
		];

		loop cat over: categories {
			dataframe sub   <- df_filter(transactions, "product_category", cat);
			float rev       <- 0.0;
			float price_sum <- 0.0;
			int   count     <- sub.rows;
			loop i from: 0 to: count - 1 {
				float qty   <- float(df_cell(sub, i, "transaction_qty"));
				float price <- float(df_cell(sub, i, "unit_price"));
				rev       <- rev       + qty * price;
				price_sum <- price_sum + price;
			}
			rev           <- round(rev * 100)           / 100.0;
			float avg_p   <- count > 0 ? round(price_sum / count * 100) / 100.0 : 0.0;
			write "  " + cat + " : $" + rev + "  avg $" + avg_p + "  (" + count + " transactions)";
			by_category <- df_add_row(by_category, [cat, rev, avg_p, count]);
		}

		write "";
		write "By-category summary:";
		write df_pretty_print(by_category, 10, 4, 22);

		// ===== 5. Most sold product type overall =====
		write "";
		write "===== Top 5 product types by transaction count =====";

		list<string> all_types  <- df_column(transactions, "product_type");
		map<string,int> type_counts <- map<string,int>();
		loop t over: all_types {
			if t != nil { type_counts[t] <- (type_counts contains_key t ? type_counts[t] : 0) + 1; }
		}
		// Sort descending and display top 5
		list<string> sorted_types <- type_counts.keys sort_by (-type_counts[each]);
		loop i from: 0 to: min(4, length(sorted_types) - 1) {
			string t <- sorted_types[i];
			write "  " + (i + 1) + ". " + t + " : " + type_counts[t] + " transactions";
		}

		// ===== 6. Filter: high-value transactions (unit_price >= 5) =====
		write "";
		write "===== High-value transactions (unit_price >= 5.0) =====";
		dataframe high_value <- dataframe_with((transactions.keys), []);
		int total <- transactions.rows;
		loop i from: 0 to: total - 1 {
			if float(df_cell(transactions, i, "unit_price")) >= 5.0 {
				high_value <- df_add_row(high_value, df_row(transactions, i));
			}
		}
		write "High-value transactions : " + high_value.rows;
		write df_pretty_print(
			df_select_columns(iloc(high_value, range(0, min(4, (high_value.rows)-1))),
				["transaction_id","store_location","product_category","product_detail","unit_price"]),
			5, 5, 22
		);

		write "";
		write "Done.";
	}
}

// -----------------------------------------------------------------------
// Charts — rendered in a GUI experiment
// -----------------------------------------------------------------------
experiment "Explore Coffee Sales" type: gui {

	output {

		display "Revenue by Store" type: 2d {
			chart "Revenue by Store Location" type: pie {
				loop i from: 0 to: (by_store.rows) - 1 {
					data string(df_cell(by_store, i, "store"))
						value: float(df_cell(by_store, i, "revenue"));
				}
			}
		}

		display "Revenue by Category" type: 2d {
			chart "Revenue by Product Category" type: histogram {
				loop i from: 0 to: (by_category.rows) - 1 {
					data string(df_cell(by_category, i, "category"))
						value: float(df_cell(by_category, i, "revenue"));
				}
			}
		}

		display "Avg Price by Category" type: 2d {
			chart "Average Unit Price by Product Category" type: histogram {
				loop i from: 0 to: (by_category.rows) - 1 {
					data string(df_cell(by_category, i, "category"))
						value: float(df_cell(by_category, i, "avg_price"));
				}
			}
		}
	}
}
