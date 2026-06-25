/**
* Name: Save to Avro
* Author: GAMA Team
* Description: Shows how to save a dataframe to an Avro file with the 'save' statement. A dataframe is
*   built from a population of bug agents and written out in the Avro format, a compact, schema-based
*   binary format widely used for data exchange and streaming (Kafka, Hadoop, ...). The file is then
*   reloaded with df_load_avro to demonstrate a round-trip. Avro embeds the schema in the file, so
*   column names and types are preserved.
* Tags: save_file, avro, export, dataframe, tabular, data
*/

model SaveToAvro

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

		// Save the dataframe to Avro with the 'save' statement
		save bugs to: "../results/bugs.avro" format: "avro";
		write "Saved bugs.avro";

		// Round-trip: reload the file we just wrote
		dataframe reloaded <- df_load_avro("../results/bugs.avro");
		write "Reloaded bugs.avro : " + reloaded.rows + " rows, columns: " + reloaded.keys;
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
