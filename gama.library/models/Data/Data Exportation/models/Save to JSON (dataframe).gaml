/**
* Name: Save to JSON (dataframe)
* Author: GAMA Team
* Description: Shows how to save a dataframe to a JSON file with the 'save' statement. A dataframe is
*   built from a population of bug agents and written out as a JSON array of objects (one object per
*   row, keys taken from the column names). The file is then reloaded with df_load_json to demonstrate
*   a round-trip. JSON is convenient for interoperating with web services and scripting languages.
* Tags: save_file, json, export, dataframe, tabular, data
*/

model SaveToJSONDataframe

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

		// Save the dataframe to JSON with the 'save' statement
		save bugs to: "../results/bugs.json" format: "json";
		write "Saved bugs.json";

		// Round-trip: reload the file we just wrote
		dataframe reloaded <- df_load_json("../results/bugs.json");
		write "Reloaded bugs.json : " + reloaded.rows + " rows, columns: " + reloaded.keys;
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
