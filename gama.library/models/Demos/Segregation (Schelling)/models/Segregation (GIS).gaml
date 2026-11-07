/**
* Name: Segregation (GIS)
* Author: Gama Development Team
* Description: A GIS-based variant of Thomas Schelling's residential segregation model. Unlike the grid and
*   agent-only variants, this version uses a real shapefile to define the spatial units (buildings or land
*   parcels) where residents live. Agents of two groups are assigned to these spatial units, and unhappy
*   agents (whose same-group neighbor percentage is below the threshold) move to a free unit selected from
*   the GIS data. This gives the segregation dynamics a realistic geographic footprint.
*   Building fills show mean resident colors; individual markers show groups and white outlines mark
*   unhappy residents. Empty buildings use a neutral slate fill.
* Tags: gis, shapefile, segregation, Schelling, emergence, social, agent_based, residential
*/
model segregation

//Import the model Common Schelling Segregation
import "../includes/Common Schelling Segregation.gaml" 
global {
	//List of all the free places
	list<space> free_places  ;  
	//List of all the places
	list<space> all_places ;
	//Neighbours distance for the perception of an agent
	int neighbours_distance <- 50 min: 1 max: 1000;
	//Shapefile to load
	file shape_file_name <- file("../gis/nha2.shp");
	//Shape of the environment
	geometry shape <- envelope(shape_file_name);
	//Square meters per people in m2
	int square_meters_per_people <- 200;
	
	//Action to initialize people agents
	action initialize_people() { 
		//Create all the places with a surface given within the shapefile
		create space (surface : float(read("AREA"))) from: shape_file_name;
		all_places  <- shuffle(space);
		//Compute the number of people to create considering the density of people
		number_of_people <- int( density_of_people * sum (all_places collect (each.capacity))); 
		create people number: number_of_people;  
	    all_people <- people as list ; 
	    //Move all the people to a new place
		ask people  {  
			do move_to_new_place();       
		}   
	}      
	//Action to initialize the places
	action initialize_places() {}   
	
} 

//Species people representing the people
species people parent: base { 
	//Size of the people agent
	float size const: true <- 2.0;
	//Color of the people agent  
	rgb color const: true <- colors at (rnd (number_of_groups - 1)); 
	int red const: true <- (color as list) at 0; 
	int green const: true <- (color as list) at 1;  
	int blue const: true <- (color as list) at 2;  
	//Building in which the agent lives
	space current_building <- nil;
	//List of all the neighbour people agents
	list<people> my_neighbours -> people at_distance neighbours_distance; 
	
	//Action to move to a new place
	action move_to_new_place() {  
		current_building <- (shuffle(all_places) first_with (((each).capacity) > 0));
		ask current_building {
			do accept (one_people: myself);   
		}
	}
	//Reflex to migrate to another place if the agent isn't happy
	reflex migrate when: !is_happy {
		if current_building != nil {
			ask current_building { 
				do remove_one (one_people: myself);
			}
		} 
		do move_to_new_place();
	}

	aspect simple {
		draw circle(5) color: color border: (show_unhappy and !is_happy) ? lettering : backdrop;
	}
}

//Species space representing a space for a people agent to live in
species space {	
	//List of all the people agents living within
	list<people> insiders;
	rgb color <- rgb(255, 255, 255); 
	//Surface of the place
	float surface;
	//Capacity of the place
	int capacity  <- 1 + int(surface / square_meters_per_people);
	
	//Action to accept a people agent  
	action accept (people one_people) {
		add one_people to: insiders;
		location of one_people <- any_location_in(shape);
		capacity <- capacity - 1;
	}
	//Action to remove a people agent
	action remove_one (people one_people){
		remove one_people from: insiders;
		capacity <- capacity + 1;
	}
	aspect simple {
		color <- empty(insiders) ? rgb(43, 56, 73) : rgb ([mean (insiders collect each.red), mean (insiders collect each.green), mean (insiders collect each.blue)]);
		draw square(40) color: color border: rgb(68, 87, 109);
	}
	aspect gis {
		color <- empty(insiders) ? rgb(43, 56, 73) : rgb( [mean (insiders collect each.red), mean (insiders collect each.green), mean (insiders collect each.blue)]);
		draw shape color: blend(color, backdrop, 0.45) border: rgb(68, 87, 109);
	} 
	aspect highlighted {
		draw shape + 10 color: rgb(lettering, 0.3) border: lettering;
	}
}


experiment schelling type: gui parent:base_exp{	
	
	parameter "Shapefile to load:" var:shape_file_name category: "GIS specific";
	parameter "Occupancy of people:" var:square_meters_per_people category: "GIS specific" unit: "m²";
	
	output {
		display Town_display type: 2d background: backdrop {
			species space aspect: gis;
			species people  aspect: simple;
			overlay coordinates: #screen position: {12, 12} size: {310, 116} background: backdrop rounded: true {
				draw "SCHELLING / RESIDENTIAL GEOGRAPHY" at: {12#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw "Dots: groups / White outline: unhappy" at: {12#px, 47#px} color: lettering font: font("Helvetica", 11);
				draw "Buildings: blended resident colors" at: {12#px, 72#px} color: lettering font: font("Helvetica", 11);
				draw "Residents: " + number_of_people + "   Happy: "
					+ string(with_precision(100.0 * sum_happy_people / max(1, number_of_people), 1)) + "%"
					at: {12#px, 97#px} color: happy_color font: font("Helvetica", 11);
			}
		}
		display Charts type: 3d background: backdrop {
			chart "Resident satisfaction" coordinates: #screen type: histogram background: backdrop color: lettering axes: lettering
				gap: 0.2 position: {0,0} size: {#display_width,0.5} y_label: "Residents" {
				data "Unhappy" value: number_of_people - sum_happy_people color: unhappy_color;
				data "Happy" value: sum_happy_people color: happy_color;
			}
			chart "Happiness and neighborhood similarity" coordinates: #screen type: series background: backdrop color: lettering axes: lettering
				position: {0,0.5} size: {#display_width,0.5} y_range: {0,100} x_label: "Cycle" y_label: "Percent" {
				data "Happy (%)" color: happy_color value: 100.0 * sum_happy_people / max(1, number_of_people) style: line;
				data "Similar neighbors (%)" color: similarity_color value: 100.0 * sum_similar_neighbours / max(1, sum_total_neighbours) style: step;
			}
		}
	}
}
