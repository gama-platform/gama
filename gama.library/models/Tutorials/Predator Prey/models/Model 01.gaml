/**
* Name: Predator Prey Tutorial - Step 01 - Basic Model with Prey Agents
* Author: Gama Development Team
* Description: First step of the Predator-Prey tutorial. Creates a basic model with only prey agents moving
*   randomly. Introduces the fundamental concepts of species declaration, agent creation, and the 'moving'
*   skill for random movement. This is the minimal starting point before adding vegetation dynamics,
*   predators, and complex behaviors in subsequent steps.
* Tags: tutorial, prey, predator, species, basic, moving
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
		
	aspect base {
		draw circle(size) color: color;
	}
} 

experiment prey_predator type: gui {
	parameter "Initial number of preys: " var: nb_preys_init min: 1 max: 1000 category: "Prey";
	output {
		display main_display {
			species prey aspect: base;
		}
	}
}
