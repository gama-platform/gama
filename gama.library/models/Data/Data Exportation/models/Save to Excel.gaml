/**
* Name: SavetoExcel
* Based on the internal empty template. 
* Author: Baptiste Lesquoy
* Tags: 
*/


model SavetoExcel


global {
	
	init { 
		//Create all the agents
		create bug number: 50;
	}
	
	//Save the agents bug when the cycle is equals to 100
	reflex save_bug_attribute when: cycle = 100{
		string bugs_path <- "../results/bugs.xlsx";
		string stats_path <- "../results/bugs_and_stats.xlsx";
		dataframe bugs <- dataframe_with(["name", "speed", "size"], bug collect ([each.name, each.speed, each.size]));
		// A single dataframe is saved as a one-sheet workbook with the 'save' statement
		save bugs to: bugs_path format: "xlsx";
		write "Saving all bugs in one xlsx file (" + bugs_path + ")";
		write "Now processing some stats";

		dataframe stats <- dataframe_with(["id", "average location", "average size"], bug collect ([each.index ,mean(each.past_locations), mean(each.past_sizes)]));

		// Inserting global averages at the end
		stats <- df_add_row(stats, ['Global', mean(list<point>(df_column(stats, "average location"))), mean(df_column(stats, "average size"))]);

		// A map of dataframes is saved as a multi-sheet workbook (keys become sheet names)
		save map("Bugs"::bugs, "Stats"::stats) to: stats_path format: "xlsx";
		write "Saving all bugs and their stats in one xlsx file (" + stats_path + ")";
		do pause();
	}
}

//Species that will be saved
species bug skills:[moving]{
	
	float size <- 1.0 + rnd(4) min: 1.0 max: 5.0;
	float speed <- 1.0 + rnd(4.0);
	list<point> past_locations <- [location];
	list<int> past_sizes <- [size];
	
	reflex update_size {
		int nb_neigh <- length(bug at_distance 20.0);
		if (nb_neigh > 5) {
			size <- size + 1;
		} else {
			size <- size - 1;
		}
		past_sizes <+ size;
	} 	
	reflex move {
		do wander();
		past_locations <+ location;
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