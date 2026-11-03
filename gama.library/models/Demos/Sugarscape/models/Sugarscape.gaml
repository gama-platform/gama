/**
* Name: Sugarscape
* Author: Gama Development Team
* Description: An implementation of the classic Sugarscape model originally proposed by Epstein & Axtell (1996)
*   in "Growing Artificial Societies". Agents move on a grid searching for cells with the highest sugar level
*   within their vision radius. Each step, agents consume sugar equal to their metabolism rate. Sugar regrows
*   at a fixed rate each step. Agents die when their sugar reserve reaches zero or when they exhaust their
*   maximum lifespan. The model demonstrates how simple resource-seeking rules lead to complex spatial
*   resource distribution patterns and population dynamics.
*   A slate-to-gold scale shows available sugar. Teal agents have more than eight units of reserve;
*   coral agents have eight or fewer. Optional rings show each agent's vision radius.
* Tags: grid, sugarscape, resource, agent, metabolism, emergence, Epstein, artificial_societies
*/
model sugarscape

  
global {
	rgb backdrop <- rgb(18, 26, 40);
	rgb lettering <- rgb(220, 231, 242);
	rgb strong_color <- rgb(83, 218, 196);
	rgb weak_color <- rgb(255, 139, 112);
	list<rgb> sugar_palette <- [rgb(28, 40, 56), rgb(77, 74, 57), rgb(130, 110, 61),
		rgb(191, 153, 72), rgb(244, 198, 102)];
	bool show_vision <- false;
	// Parameters 
	
	//Growth rate of the sugar
	int sugarGrowthRate <- 1;
	//Minimum age of death
	int minDeathAge <- 60;
	//Maximum age of death
	int maxDeathAge <- 100;
	//Metabolism maximum
	int maxMetabolism <- 3;
	
	//Maximum and Minimum initial sugar
	int maxInitialSugar <- 25 ;
	int minInitialSugar <- 5;
	//Maximum range
	int maxRange <- 6;
	bool replace <- true;
	//Number of agents
	int numberOfAgents <- 400;	
	
	// Environment
	geometry shape <- rectangle(50, 50);
		
	file<int> types  <- file<int>('../images/sugarscape.pgm');
	
	init {
		
		//Create the animal
		create animal number: numberOfAgents;
		
		//Ask to each sugar cell to set its sugar
		ask sugar_cell {
			maxSugar <- (types at {grid_x,grid_y});
			sugar <- maxSugar;
			color <- sugar_palette at sugar;
		}
	}
}

	//Grid species representing the sugar cells
	grid sugar_cell width: 50 height: 50 neighbors: 4 use_individual_shapes: false use_regular_agents: false{ 
		//Maximum sugar
		int maxSugar;
		//Sugar contained in thecell
		int sugar update: sugar + sugarGrowthRate max: maxSugar;
		rgb color update: sugar_palette at sugar;
		map<int,list<sugar_cell>> neighbours;
		
		//Initialization of the neighbours
		init {
			loop i from: 1 to: maxRange {
				neighbours[i] <- self neighbors_at i; 
			}
		}
	}	
	
//Species animal representing the animal agents
species animal {
	//Color of the animal
	rgb color  <- #red;
	//Speed of the animal
	float speed  <- 1.0;
	//Metabolism of the animal
	int metabolism  min: 1 <- rnd(maxMetabolism);
	//Perception range of the animal
	int vision  min: 1 <- rnd(maxRange);
	//Maximal age of the animal
	int maxAge  min: minDeathAge max: maxDeathAge <- rnd (maxDeathAge - minDeathAge) + minDeathAge;
	//Size of the animal
	float size  <- 0.5;
	//Sugar of the animal
	int sugar min: 0 <- (rnd (maxInitialSugar - minInitialSugar)) + minInitialSugar update: sugar - metabolism;
	//Age of the animal
	int age max: maxAge <- 0 update: int(age + step);
	//Place of the animal
	sugar_cell place ; 
	
	//Launched at the initialization of the animal agent
	init {
		//Set the place as one of the sugar cell
		place <- one_of(sugar_cell);
		location <- place.location;
	}
	//Move the agent to another place and collect the sugar of the previous place
	reflex basic_move { 
		sugar <- sugar + place.sugar;
		place.sugar <- 0;
		list<sugar_cell> neighbours <- place.neighbours[vision];
		list<sugar_cell> poss_targets <- (neighbours) where (each.sugar > 0);
		//If no sugar is found in the neighbours cells, move randomly
		place <- empty(poss_targets) ? one_of (neighbours) : one_of (poss_targets);
		location <- place.location;
	}
	//Reflex to kill the animal once it reaches its maximal age or it doesn't have sugar anymore
	reflex end_of_life when: (sugar = 0) or (age = maxAge) {
		if replace {
			create animal ;
		}
		do die();
	}
	aspect default {
		rgb tint <- sugar > 8 ? strong_color : weak_color;
		if (show_vision) {
			draw circle(vision) color: rgb(tint, 0.025) border: rgb(tint, 0.15);
		}
		draw circle(0.34) color: tint border: backdrop;
		if (sugar <= 8) {
			draw circle(0.44) color: tint wireframe: true;
		}
	}
}

experiment sugarscape type: gui{
	float minimum_cycle_duration<-0.1;
	parameter 'Growth rate of sugar:' var: sugarGrowthRate category: 'Environment' unit: "/cycle";
	parameter 'Minimum age of death:' var: minDeathAge <- 60 category: 'Agents' unit: "cycles";
	parameter 'Maximum age of death:' var: maxDeathAge <- 100 category: 'Agents' unit: "cycles";
	parameter 'Maximum metabolism:' var: maxMetabolism <- 3 category: 'Agents';
	parameter 'Maximum initial sugar per cell:'  var: maxInitialSugar <- 25 category: 'Environment';
	parameter 'Minimum initial sugar per cell:' var: minInitialSugar <- 5 category: 'Environment';
	parameter 'Maximum range of vision:' var: maxRange <- 6 category: 'Agents' unit: "cells";
	parameter 'Replace dead agents ?' var: replace <- true category: 'Agents';
	parameter 'Number of agents:' var: numberOfAgents <- 400 category: 'Agents';
	parameter "Show vision ranges" var: show_vision category: "Appearance";
	
	output {
		display grille type: 2d background: backdrop {
			grid sugar_cell;
			species animal;
			overlay position: {12#px, 12#px} size: {320#px, 142#px} background: backdrop rounded: true {
				draw "SUGARSCAPE / RESOURCES & RESERVES" at: {12#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw "Teal: reserve > 8 / Coral rings: reserve <= 8" at: {12#px, 47#px}
					color: lettering font: font("Helvetica", 11);
				draw "Population: " + length(animal) + "   Low reserve: " + (animal count (each.sugar <= 8))
					at: {12#px, 72#px} color: lettering font: font("Helvetica", 11);
				loop level from: 0 to: 4 {
					draw rectangle(25#px, 12#px) at: {(26 + level * 29)#px, 97#px}
						color: sugar_palette[level] border: rgb(lettering, 0.3);
				}
				draw "Cell sugar: 0 (slate) to 4 (gold)" at: {12#px, 126#px}
					color: lettering font: font("Helvetica", 11);
			}
		}
		display chart refresh: every(5#cycles) type: 2d background: backdrop {
			chart "Sugar reserve / population share" type: pie background: backdrop color: lettering
				style: ring label_background_color: backdrop label_text_color: lettering {
				data "Reserve > 8" value: (animal as list) count (each.sugar > 8) color: strong_color;
				data "Reserve <= 8" value: (animal as list) count (each.sugar < 9) color: weak_color;
			}
		}
		display chart2 refresh: every(5#cycles) type: 2d background: backdrop {
			chart "Sugar reserve / agent counts" type: histogram background: backdrop color: lettering axes: lettering
				label_background_color: backdrop label_text_color: lettering gap: 0.2 y_label: "Agents" {
				data "Reserve > 8" value: (animal as list) count (each.sugar > 8) color: strong_color;
				data "Reserve <= 8" value: (animal as list) count (each.sugar < 9) color: weak_color;
			}
		}
	}
}
