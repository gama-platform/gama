/**
* Name: Segregation (Agents)
* Author: Gama Development Team
* Description: An agent-based implementation of Thomas Schelling's classic residential segregation model.
*   Agents of two groups are placed randomly on a grid. At each step, any agent whose percentage of same-group
*   neighbors falls below the 'similarity_wanted' threshold becomes unhappy and moves to a random free cell.
*   Despite starting from a random mixed distribution, even a low preference for neighbors of the same group
*   (e.g., 30%) leads to highly segregated patterns. This version uses individual agent objects to represent
*   residents — compare with the cellular automata and GIS variants.
*   Fill colors identify groups; white outlines identify unhappy residents.
* Tags: grid, segregation, Schelling, emergence, social, agent_based, residential
*/

model segregation

//import the Common Schelling Segregation model
import "../includes/Common Schelling Segregation.gaml"

global {
	//List of all the free places
	list<space> free_places ;
	//List of all the places
	list<space> all_places ;
	//Shape of the world
	geometry shape <- square(dimensions);
	
	//Action to initialize the people agents
	action initialize_people() { 
		create people number: number_of_people; 
		all_people <- people as list ;  
	} 
	//Action to initialize the places
	action initialize_places() { 
		all_places <- shuffle (space);
		free_places <- all_places;  
	} 
}
//Grid to discretize space, each cell representing a free space for the people agents
grid space width: dimensions height: dimensions neighbors: 8 use_regular_agents: false frequency: 0{
	rgb color  <- #black;
}

//Species representing the people agents
species people parent: base  {
	//Color of the people agent
	rgb color <- colors at (rnd (number_of_groups - 1));
	//List of all the neighbours of the agent
	list<people> my_neighbours -> people at_distance neighbours_distance ;
	//Cell representing the place of the agent
	space my_place;
	init {
		//The agent will be located on one of the free places
		my_place <- one_of(free_places);
		location <- my_place.location; 
		//As one agent is in the place, the place is removed from the free places
		free_places >> my_place;
	} 
	//Reflex to migrate the people agent when it is not happy 
	reflex migrate when: !is_happy {
		//Add the place to the free places as it will move to another place
		free_places << my_place;
		//Change the place of the agent
		my_place <- one_of(free_places);
		location <- my_place.location; 
		//Remove the new place from the free places
		free_places >> my_place;
	}
	
	aspect default{ 
		draw circle(0.42) color: color border: (show_unhappy and !is_happy) ? lettering : backdrop;
	}
}



experiment schelling type: gui parent:base_exp{	
	output {
		display Segregation type: 2d background: backdrop {
			species people;
			overlay coordinates: #screen position: {12, 12} size: {290, 92} background: backdrop rounded: true {
				draw "SCHELLING / INDIVIDUAL RESIDENTS" at: {12#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw "Fill: group / White outline: unhappy" at: {12#px, 47#px} color: lettering font: font("Helvetica", 11);
				draw "Residents: " + number_of_people + "   Happy: "
					+ string(with_precision(100.0 * sum_happy_people / max(1, number_of_people), 1)) + "%"
					at: {12#px, 72#px} color: happy_color font: font("Helvetica", 11);
			}
		}	
		display Charts type: 3d background: backdrop {
			chart "Resident satisfaction" coordinates: #screen kind: pie background: backdrop color: lettering
				label_background_color: backdrop label_text_color: lettering style: ring position: {0,0} size: {#display_width,0.5} {
				data "Unhappy" value: number_of_people - sum_happy_people color: unhappy_color;
				data "Happy" value: sum_happy_people color: happy_color;
			}
			chart "Happiness and neighborhood similarity" coordinates: #screen kind: series background: backdrop color: lettering axes: lettering
				position: {0,0.5} size: {#display_width,0.5} y_range: {0,100} x_label: "Cycle" y_label: "Percent" {
				data "Happy (%)" color: happy_color value: 100.0 * sum_happy_people / max(1, number_of_people) style: line;
				data "Similar neighbors (%)" color: similarity_color value: 100.0 * sum_similar_neighbours / max(1, sum_total_neighbours) style: step;
			}
		}
	}
}