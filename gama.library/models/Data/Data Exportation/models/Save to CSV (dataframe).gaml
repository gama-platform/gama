/**
* Name: Save Dataframe to CSV
* Author: GAMA Team
* Description: Shows how to save a dataframe to a CSV file. A dataframe is built from a population
*   of bug agents, then written to CSV with the default comma separator (df_save_csv) and with a
*   custom separator and character encoding (df_save_csv_with). The file is finally reloaded with
*   df_load_csv to demonstrate a round-trip. Unlike the agent-based 'save' statement (see
*   "Save to CSV"), the dataframe operators give full control over the tabular content and headers.
* Tags: save_file, csv, export, dataframe, tabular, data
*/

model SaveDataframeToCSV

global {
	init {
		// Create the agents whose attributes we will export
		create bug number: 50;
	}

	// Build a dataframe from the agents and save it once the simulation has run a bit
	reflex save_bugs when: cycle = 100 {
		dataframe bugs <- dataframe_with(
			["name", "speed", "size"],
			bug collect ([each.name, each.speed, each.size])
		);

		// Save with default settings (comma separator, UTF-8)
		bool ok <- df_save_csv(bugs, "../results/bugs.csv");
		write "Saved bugs.csv (comma, UTF-8) : " + ok;

		// Save with a custom separator and encoding
		bool ok_semi <- df_save_csv_with(bugs, "../results/bugs_semicolon.csv", ";", "ISO-8859-1");
		write "Saved bugs_semicolon.csv (semicolon, ISO-8859-1) : " + ok_semi;

		// Round-trip: reload the file we just wrote
		dataframe reloaded <- df_load_csv("../results/bugs.csv");
		write "Reloaded bugs.csv : " + reloaded.rows + " rows, columns: " + reloaded.keys;
		write "First bug name : " + df_cell(reloaded, 0, "name");

		do pause();
	}
}

// Species that will be saved
species bug skills: [moving] {
	float size <- 1.0 + rnd(4) min: 1.0 max: 5.0;
	float speed <- 1.0 + rnd(4.0);

	reflex update_size {
		int nb_neigh <- length(bug at_distance 20.0);
		if (nb_neigh > 5) {
			size <- size + 1;
		} else {
			size <- size - 1;
		}
	}

	reflex move {
		do wander();
	}

	aspect default {
		draw circle(size) color: #red;
	}
}

experiment main type: gui {
	output {
		display map {
			species bug;
		}
	}
}
