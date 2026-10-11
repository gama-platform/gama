/**
* Name: Predator Prey Tutorial - Step 02 - Vegetation Dynamics (Grid)
* Author: Gama Development Team
* Description: Second step of the Predator-Prey tutorial. Adds a grid to represent vegetation that prey agents
*   eat as they move. Each grid cell tracks its current food level, which regrows at a constant rate each step.
*   Prey eat food from the cell they occupy and gain energy; they die if their energy reaches zero. This step
*   introduces grid-based environments and energy dynamics in a predator-prey ecosystem model.
* Tags: tutorial, prey, predator, grid, energy, vegetation, ecosystem
*/
model prey_predator

global {
	int nb_preys_init <- 200;
	init {
		create prey number: nb_preys_init;
	}
}

species prey {
	float size <- 1.0;
	rgb color <- #blue;
	vegetation_cell my_cell <- one_of (vegetation_cell);
		
	init {
		location <- my_cell.location;
	}

	aspect base {
		draw circle(size) color: color;
	}
}

grid vegetation_cell width: 50 height: 50 neighbors: 4 {
	float max_food <- 1.0;
	float food_prod <- rnd(0.01);
	float food <- rnd(1.0) max: max_food update: food + food_prod;
	rgb color <- rgb(int(255 * (1 - food)), 255, int(255 * (1 - food))) update: rgb(int(255 * (1 - food)), 255, int(255 * (1 - food)));
}

experiment prey_predator type: gui {
	parameter "Initial number of preys: " var: nb_preys_init min: 1 max: 1000 category: "Prey";
	output {
		display main_display {
			grid vegetation_cell border: #black;
			species prey aspect: base;
		}
	}
}
