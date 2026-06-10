/**
* Name: Save Dataframe to Parquet
* Author: GAMA Team
* Description: Shows how to save a dataframe to a Parquet file with df_save_parquet. A dataframe is built
*   from a population of bug agents and written out in the columnar Parquet format, which is compact and
*   efficient for large tabular datasets. The file is then reloaded with df_load_parquet to demonstrate a
*   round-trip. Parquet preserves column names and is widely used in data-science pipelines (pandas, Spark, ...).
* Tags: save_file, parquet, export, dataframe, tabular, data
*/

model SaveDataframeToParquet

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

		bool ok <- df_save_parquet(bugs, "../results/bugs.parquet");
		write "Saved bugs.parquet : " + ok;

		// Round-trip: reload the file we just wrote
		dataframe reloaded <- df_load_parquet("../results/bugs.parquet");
		write "Reloaded bugs.parquet : " + reloaded.rows + " rows, columns: " + reloaded.keys;
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
