/**
* Name: Road Traffic Tutorial - Step 01 - Loading GIS Data
* Author: Gama Development Team
* Description: First step of the Road Traffic tutorial. Loads buildings, roads, and bounds from shapefiles
*   and creates corresponding agents. The environment geometry is set to the bounds shapefile envelope. Buildings
*   and roads are displayed with different colors. This step introduces GIS data loading with the 'shape_file'
*   operator, the 'create from' statement for populating agents from shapefiles, and the basic 2D display setup.
* Tags: gis, shapefile, tutorial, building, road, load_file, transport
*/

model tutorial_gis_city_traffic

global {
	file shape_file_buildings <- file("../includes/building.shp");
	file shape_file_roads <- file("../includes/road.shp");
	file shape_file_bounds <- file("../includes/bounds.shp");
	geometry shape <- envelope(shape_file_bounds);
	float step <- 10 #mn;
	
	init {
		create building(type:string(read ("NATURE"))) from: shape_file_buildings {
			if type="Industrial" {
				color <- #blue ;
			}
		}
		create road from: shape_file_roads ;
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

experiment road_traffic type: gui {
	parameter "Shapefile for the buildings:" var: shape_file_buildings category: "GIS" ;
	parameter "Shapefile for the roads:" var: shape_file_roads category: "GIS" ;
	parameter "Shapefile for the bounds:" var: shape_file_bounds category: "GIS" ;
		
	output {
		display city_display type:3d {
			species building aspect: base ;
			species road aspect: base ;
		}
	}
}