/**
* Name: Parquet Airbnb Listings
* Author: GAMA Team
* Description: Demonstrates loading a Parquet file into a dataframe and performing
*   common data exploration tasks on a real-world Airbnb listings dataset (Bali, Indonesia).
*   The dataset contains 29 440 listings with 61 columns covering location, pricing,
*   ratings, occupancy and revenue metrics — all stored as strings in the Parquet file.
*
*   Key operations demonstrated:
*     - df_load_parquet: load a .parquet file
*     - keys / df_column_types: inspect schema
*     - df_filter / df_remove_empty: clean and subset data
*     - df_select_columns: keep only relevant columns
*     - df_column / df_cell: access values
*     - iloc: position-based sampling (first, last, arbitrary rows)
*     - df_add_column: derive a new column
*     - df_save_csv: export results
*     - loop + df_cell: aggregate statistics manually
*
*   Charts displayed:
*     1. Listings by City (bar) — top 10 cities by listing count
*     2. Listings by Room Type (pie) — entire_home / private_room / etc.
*     3. Revenue by City (bar) — total TTM revenue (USD) per city, top 10
*     4. Rating Distribution (bar) — listings bucketed by overall rating
*     5. Occupancy Distribution (bar) — listings bucketed by TTM occupancy %
*     6. Price Tier — Seminyak entire-home (pie) — budget/mid-range/premium/luxury
*
* Dataset: Airbnb Market Data — Asia Pacific (listings.parquet)
*   Source : https://www.kaggle.com/datasets/jasonairroi/airbnb-market-data-asia-pacific
*   Author : jasonairroi
*   License: Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)
*            https://creativecommons.org/licenses/by-nc/4.0/
*   Note   : This model and its associated data file are provided for educational and
*            non-commercial use only, in accordance with the dataset license.
*
* Tags: parquet, dataframe, load_file, airbnb, tabular, iloc, filter, statistics, chart
*/

model ParquetAirbnbListings

global {

	// -----------------------------------------------------------------------
	// Load the Parquet file at startup (all 61 columns as strings)
	// -----------------------------------------------------------------------
	dataframe listings <- df_load_parquet("../includes/listings.parquet");

	// -----------------------------------------------------------------------
	// Aggregation containers — populated in init, read by chart displays
	// -----------------------------------------------------------------------
	map<string, int>   listings_by_city      <- map<string,int>();
	map<string, float> revenue_by_city       <- map<string, float>();
	map<string, int>   listings_by_room_type <- map<string, int>();
	map<string, int>   rating_buckets        <- map([
		"< 3.0"::0, "3.0 – 3.5"::0, "3.5 – 4.0"::0,
		"4.0 – 4.5"::0, "4.5 – 4.75"::0, "4.75 – 5.0"::0
	]);
	map<string, int>   occ_buckets           <- map([
		"0 – 10 %"::0, "10 – 20 %"::0, "20 – 30 %"::0,
		"30 – 50 %"::0, "50 – 70 %"::0, "> 70 %"::0
	]);
	map<string, int>   price_tier_seminyak   <- map([
		"budget (< $100)"::0, "mid-range ($100–250)"::0,
		"premium ($250–500)"::0, "luxury (> $500)"::0
	]);

	init {

		// ===== 1. Schema inspection =====
		write "===== Schema =====";
		write "Rows    : " + (listings.rows);
		write "Columns : " + (listings.keys);
		write "";
		write "First 5 rows (cols 0-7):";
		write df_pretty_print(iloc(listings, range(0, 4), range(0, 7)), 5, 8, 20);

		// ===== 2. Quick positional look with iloc =====
		write "";
		write "===== iloc — positional access =====";
		list first <- iloc(listings, 0);
		write "First listing : id=" + first[0] + "  type=" + first[1] + "  city=" + first[60];
		list last  <- iloc(listings, -1);
		write "Last listing  : id=" + last[0]  + "  type=" + last[1]  + "  city=" + last[60];
		write "City at row 10: " + iloc(listings, 10, 60);

		dataframe sample5 <- iloc(listings, range(0, 4), [0, 1, 60]);
		write df_pretty_print(sample5, 5, 3, 25);

		// ===== 3. Single-pass aggregation over all listings =====
		// We iterate once and fill all maps simultaneously for efficiency.
		write "";
		write "===== Aggregating (single pass over all " + (listings.rows) + " listings) =====";

		int total <- listings.rows;
		loop i from: 0 to: total - 1 {

			// — city count and revenue
			string city    <- string(df_cell(listings, i, "city"));
			string rev_str <- string(df_cell(listings, i, "ttm_revenue"));
			if city != nil and city != "" {
				listings_by_city[city] <- (listings_by_city contains_key city
					? listings_by_city[city] : 0) + 1;
				if rev_str != nil and rev_str != "" {
					float rev <- float(rev_str);
					revenue_by_city[city] <- (revenue_by_city contains_key city
						? revenue_by_city[city] : 0.0) + rev;
				}
			}

			// — room type
			string rtype <- string(df_cell(listings, i, "room_type"));
			if rtype != nil and rtype != "" {
				listings_by_room_type[rtype] <- (listings_by_room_type contains_key rtype
					? listings_by_room_type[rtype] : 0) + 1;
			}

			// — rating bucket
			string rat_str <- string(df_cell(listings, i, "rating_overall"));
			if rat_str != nil and rat_str != "" {
				float rat <- float(rat_str);
				string bucket <- 
					rat < 3.0  ? "< 3.0"      :
					(rat < 3.5  ? "3.0 – 3.5"  :
					(rat < 4.0  ? "3.5 – 4.0"  :
					(rat < 4.5  ? "4.0 – 4.5"  :
					(rat < 4.75 ? "4.5 – 4.75" : "4.75 – 5.0"))));
				rating_buckets[bucket] <- rating_buckets[bucket] + 1;
			}

			// — occupancy bucket
			string occ_str <- string(df_cell(listings, i, "ttm_occupancy"));
			if occ_str != nil and occ_str != "" {
				float occ <- float(occ_str) * 100.0;
				string ob <-
					occ < 10.0 ? "0 – 10 %"   :
					(occ < 20.0 ? "10 – 20 %"  :
					(occ < 30.0 ? "20 – 30 %"  :
					(occ < 50.0 ? "30 – 50 %"  :
					(occ < 70.0 ? "50 – 70 %"  : "> 70 %"))));
				occ_buckets[ob] <- occ_buckets[ob] + 1;
			}

			// — price tier for Seminyak entire-home
			string city2  <- string(df_cell(listings, i, "city"));
			string rtype2 <- string(df_cell(listings, i, "room_type"));
			if city2 = "Seminyak" and rtype2 = "entire_home" {
				string rate_str <- string(df_cell(listings, i, "ttm_avg_rate"));
				if rate_str != nil and rate_str != "" {
					float rate <- float(rate_str);
					string tier <-
						rate < 100.0 ? "budget (< $100)"       :
						(rate < 250.0 ? "mid-range ($100–250)"  :
						(rate < 500.0 ? "premium ($250–500)"    : "luxury (> $500)"));
					price_tier_seminyak[tier] <- price_tier_seminyak[tier] + 1;
				}
			}
		}

		// ===== 4. Summary output =====
		write "";
		write "===== Listings by city (top 10) =====";
		list<string> top_cities <- listings_by_city.keys
			sort_by (-listings_by_city[each]);
		loop i from: 0 to: min(9, length(top_cities) - 1) {
			string c <- top_cities[i];
			write "  " + c + " : " + listings_by_city[c];
		}

		write "";
		write "===== Listings by room type =====";
		loop rt over: listings_by_room_type.keys sort_by (-listings_by_room_type[each]) {
			write "  " + rt + " : " + listings_by_room_type[rt];
		}

		write "";
		write "===== Revenue by city (top 10, TTM USD) =====";
		list<string> top_rev_cities <- revenue_by_city.keys
			sort_by (-revenue_by_city[each]);
		loop i from: 0 to: min(9, length(top_rev_cities) - 1) {
			string c <- top_rev_cities[i];
			write "  " + c + " : $" + round(revenue_by_city[c]);
		}

		write "";
		write "===== Rating distribution =====";
		loop b over: ["< 3.0","3.0 – 3.5","3.5 – 4.0","4.0 – 4.5","4.5 – 4.75","4.75 – 5.0"] {
			write "  " + b + " : " + rating_buckets[b];
		}

		write "";
		write "===== Occupancy distribution =====";
		loop b over: ["0 – 10 %","10 – 20 %","20 – 30 %","30 – 50 %","50 – 70 %","> 70 %"] {
			write "  " + b + " : " + occ_buckets[b];
		}

		write "";
		write "===== Price tier — Seminyak entire-home =====";
		loop t over: price_tier_seminyak.keys {
			write "  " + t + " : " + price_tier_seminyak[t];
		}

		// ===== 5. Detailed look at Seminyak entire-home =====
		write "";
		write "===== Seminyak entire-home — detailed stats =====";
		dataframe seminyak_entire <- df_remove_empty(
			df_select_columns(
				df_filter(df_filter(listings, "city", "Seminyak"), "room_type", "entire_home"),
				["listing_id","listing_type","guests","bedrooms",
				 "num_reviews","rating_overall","ttm_revenue","ttm_occupancy","ttm_avg_rate"]
			),
			"rating_overall"
		);

		write "Rows : " + (seminyak_entire.rows);
		write df_pretty_print(iloc(seminyak_entire, range(0, 4)), 5, 9, 16);

		// Top-rated (>= 4.9)
		dataframe top_rated <- dataframe_with((seminyak_entire.keys), []);
		loop i from: 0 to: (seminyak_entire.rows) - 1 {
			string rv <- string(df_cell(seminyak_entire, i, "rating_overall"));
			if rv != nil and rv != "" and float(rv) >= 4.9 {
				top_rated <- df_add_row(top_rated, df_row(seminyak_entire, i));
			}
		}
		write "";
		write "Top-rated (>= 4.9) : " + (top_rated.rows) + " listings";
		if (top_rated.rows) > 0 {
			write df_pretty_print(iloc(top_rated, range(0, min(4, (top_rated.rows) - 1))), 5, 9, 16);
		}

		// ===== 6. Save =====
		write "";
		write "===== Saving =====";
		bool ok <- df_save_csv(seminyak_entire, "../results/seminyak_entire_home.csv");
		write "Saved seminyak_entire_home.csv : " + ok;
		write "Done.";
	}
}

// -----------------------------------------------------------------------
// Experiment with 6 chart displays
// -----------------------------------------------------------------------
experiment "Explore Listings" type: gui {

	output {

		// Chart 1 — Listings by city (top 10, bar)
//		display "Listings by City" type: 2d {
//			chart "Listings by City (top 10)" type: histogram
//					x_label: "City" y_label: "Number of listings" {
//				loop c over: (listings_by_city.keys
//						sort_by (-listings_by_city[each] at {0, 9}))  {
//					data c value: listings_by_city[c] color: #steelblue;
//				}
//			}
//		}

		// Chart 2 — Listings by room type (pie)
		display "Room Type Distribution" type: 2d {
			chart "Listings by Room Type" type: pie {
				loop rt over: listings_by_room_type.keys
						sort_by (-listings_by_room_type[each]) {
					data rt value: listings_by_room_type[rt];
				}
			}
		}

		// Chart 3 — Revenue by city (top 10, bar)
//		display "Revenue by City" type: 2d {
//			chart "Total TTM Revenue by City — top 10 (USD)" type: histogram
//					x_label: "City" y_label: "Total TTM revenue (USD)" {
//				loop c over: (revenue_by_city.keys
//						sort_by (-revenue_by_city[each])) at {0, 9} {
//					data c value: revenue_by_city[c] color: #indianred;
//				}
//			}
//		}

		// Chart 4 — Rating distribution (bar)
		display "Rating Distribution" type: 2d {
			chart "Overall Rating Distribution" type: histogram
					x_label: "Rating range" y_label: "Number of listings" {
				loop b over: ["< 3.0","3.0 – 3.5","3.5 – 4.0",
				              "4.0 – 4.5","4.5 – 4.75","4.75 – 5.0"] {
					data b value: rating_buckets[b] color: #mediumseagreen;
				}
			}
		}

		// Chart 5 — Occupancy distribution (bar)
		display "Occupancy Distribution" type: 2d {
			chart "TTM Occupancy Rate Distribution" type: histogram
					x_label: "Occupancy range" y_label: "Number of listings" {
				loop b over: ["0 – 10 %","10 – 20 %","20 – 30 %",
				              "30 – 50 %","50 – 70 %","> 70 %"] {
					data b value: occ_buckets[b] color: #mediumpurple;
				}
			}
		}

		// Chart 6 — Price tier for Seminyak entire-home (pie)
		display "Price Tiers — Seminyak" type: 2d {
			chart "Price Tier — Seminyak Entire-Home" type: pie {
				loop t over: ["budget (< $100)","mid-range ($100–250)",
				              "premium ($250–500)","luxury (> $500)"] {
					data t value: price_tier_seminyak[t];
				}
			}
		}
	}
}
