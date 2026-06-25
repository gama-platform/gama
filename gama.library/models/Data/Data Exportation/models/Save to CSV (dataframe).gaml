/**
* Name: Save to CSV (dataframe)
* Author: GAMA Team
* Description: Shows how to save a dataframe to a CSV file with the 'save' statement. A dataframe is
*   built from a population of bug agents, then written to CSV (the format is taken from the file
*   extension, or can be forced with the 'format:' facet). The file is finally reloaded with
*   df_load_csv to demonstrate a round-trip. Saving a dataframe uses the same 'save ... to: ... '
*   statement as agents and other containers — see "Save to CSV (agents)" for the agent-based variant.
* Tags: save_file, csv, export, dataframe, tabular, data
*/

model SaveToCSVDataframe

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

		// Save the dataframe to CSV with the 'save' statement.
		// The format is inferred from the ".csv" extension; the 'format:' facet makes it explicit.
		save bugs to: "../results/bugs.csv" format: "csv";
		write "Saved bugs.csv";

		// The 'separator:' facet sets the column delimiter (here ';'). When omitted, the
		// 'CSV separator' preference is used. This facet works for any CSV save (agents, lists, dataframes).
		save bugs to: "../results/bugs_semicolon.csv" format: "csv" separator: ";";
		write "Saved bugs_semicolon.csv (semicolon-separated)";

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
