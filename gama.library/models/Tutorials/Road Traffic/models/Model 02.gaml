/**
* Name: Road Traffic Tutorial - Step 02 - People Agents
* Author: Gama Development Team
* Description: Second step of the Road Traffic tutorial. Adds 'people' agents placed inside buildings at
*   initialization. Each person is assigned a home building and a work building chosen at random. People
*   are displayed with colored circles. This step introduces agent placement inside loaded GIS features
*   and demonstrates how to assign attributes from the spatial context (building locations).
* Tags: tutorial, gis, people, agent, building, placement, transport
*/

model tutorial_gis_city_traffic

global {
	file shape_file_buildings <- file("../includes/building.shp");
	file shape_file_roads <- file("../includes/road.shp");
	file shape_file_bounds <- file("../includes/bounds.shp");
	geometry shape <- envelope(shape_file_bounds);
	float step <- 10 #mn;
	int nb_people <- 100;
	
	init {
		create building(type:string(read ("NATURE"))) from: shape_file_buildings {
			if type="Industrial" {
				color <- #blue ;
			}
		}
		create road from: shape_file_roads ;
		
		list<building> residential_buildings <- building where (each.type="Residential");
		create people number: nb_people {
			location <- any_location_in (one_of (residential_buildings));
		}
	}
}

species building {
	string type; 
	rgb color <- #gray  ;
	
	aspect base {
		draw shape color: color ;
	}
}

species road  {
	rgb color <- #black ;
	aspect base {
		draw shape color: color ;
	}
}

species people {
	rgb color <- #yellow ;
	
	aspect base {
		draw circle(10) color: color border: #black;
	}
}

experiment road_traffic type: gui {
	parameter "Shapefile for the buildings:" var: shape_file_buildings category: "GIS" ;
	parameter "Shapefile for the roads:" var: shape_file_roads category: "GIS" ;
	parameter "Shapefile for the bounds:" var: shape_file_bounds category: "GIS" ;
	parameter "Number of people agents" var: nb_people category: "People" ;
	
	output {
		display city_display type:3d {
			species building aspect: base ;
			species road aspect: base ;
			species people aspect: base ;
		}
	}
}