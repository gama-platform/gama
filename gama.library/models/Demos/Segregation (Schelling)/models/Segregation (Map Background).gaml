/**
* Name: Segregation (Map Background)
* Author: Gama Development Team
* Description: A variant of Thomas Schelling's residential segregation model that uses a raster map image to
*   define navigable space. Grid cells whose color matches the navigable areas of the image are used as
*   possible residential locations. Agents of two groups are placed on these cells, and unhappy agents move
*   to free cells on the map. The map image provides a realistic geographic context for the segregation
*   dynamics, blending the cellular automata approach with image-based environment initialization.
*   Opaque group-colored markers remain readable over the map; white outlines mark unhappy residents.
*   Residential colors are compared as opaque RGB values, independently of the image pixel alpha.
* Tags: grid, image, segregation, Schelling, emergence, social, raster, map_background
*/
model segregation

//Import the model Common Schelling Segregation
import "../includes/Common Schelling Segregation.gaml"    

global {
	//List of all the free places
	list<space> free_places ; 
	//List of all the places
	list<space> all_places;
	//Shape of the environment
	geometry shape <- square(dimensions);
	//Percentage of similarity wanted by an agent
	float percent_similar_wanted <- 0.6;
	//Distance of perception of the neighbours
	int neighbours_distance <- 4; 
	//Number of groups of people
	int number_of_groups <- 3;
	list google_buildings  <- [rgb("#EBE6DC"), rgb("#D1D0CD"), rgb("#F2EFE9"), rgb("#EEEBE1"), rgb("#F9EFE8")] ;
	//List of all the available places
	list<space> available_places ;
	//Image file to load
	image_file bitmap_file_name <- image_file("../images/hanoi.png");
	matrix<int> map_colors;
 
 	//Action to initialize the people agents
	action initialize_people() {
		create people number: number_of_people ;  
		all_people <- people as list ;  
	}
	//Action to initialize the places using the color in the image
	action initialize_places() { 
		map_colors <- (bitmap_file_name) as_matrix {dimensions,dimensions} ;
		ask space as list {
			// Resized RGB image buffers can contain pixels with an unset alpha channel.
			color <- rgb(map_colors at {grid_x,grid_y}) with_alpha 255;
		}
		all_places <- shuffle (space where (each.color in google_buildings)) ;
		free_places <- copy(all_places);
	}  
}
//Grid to discretize the space
grid space width: dimensions height: dimensions neighbors: 8 use_individual_shapes: false use_regular_agents: false frequency: 0 ; 
 
//Species people representing the people agent
species people parent: base  {
	rgb color <- colors at (rnd (number_of_groups - 1));
	//List of all the neighbours
	list<people> my_neighbours -> (self neighbors_at neighbours_distance) of_species people;
	
	//Launched at the initialization of the agent
	init {
		//Set the place of the agent as one of the free place
		location <- (one_of(free_places)).location; 
		remove location as space from: free_places;
	} 
	//Reflex to migrate the agent when it's not happy
	reflex migrate when: !is_happy { 
		add location as space to: free_places;
		location <- any(free_places).location;
		remove location as space from: free_places;
	}
	aspect geom {
		draw circle(0.46) color: color border: (show_unhappy and !is_happy) ? lettering : backdrop;
	}
	aspect default {
		draw circle(0.46) color: color border: (show_unhappy and !is_happy) ? lettering : backdrop;
	}
}


experiment schelling type: gui parent:base_exp{
	
		
	 parameter "Name of image file to load:" var:bitmap_file_name category: "Environment";
	
	output {
		display Segregation type: 2d background: backdrop {
			picture bitmap_file_name.path ;
			graphics map_tint {
				draw shape color: rgb(backdrop, 0.35);
			}
			species people aspect: geom;
			overlay coordinates: #screen position: {12, 12} size: {290, 92} background: backdrop rounded: true {
				draw "SCHELLING / MAP NEIGHBORHOODS" at: {12#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
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
