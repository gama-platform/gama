/**
* Name: GeoPackage Layers
* Author: Alexis Drogoul
* Description: Shows how to use the layers of a GeoPackage (.gpkg) file in a simulation. Both layers ("building" and
*   "road") are read from the same file; the roads are used to build a graph on which a few agents move between
*   randomly chosen buildings. The 'read()' operator could be used in the same way to retrieve attributes 
*   stored in the file.
* Tags: load_file, geopackage, gpkg, gis, graph, import, spatial
*/

model geopackage_layers

global {
	file building_file <- geopackage_file("../includes/city.gpkg", "building");
	file road_file <- geopackage_file("../includes/city.gpkg", "road");
	geometry shape <- envelope(road_file);
	graph road_network;
	
	init {
		create building from: building_file;
		create road from: road_file;
		road_network <- as_edge_graph(road);
		create people number: 100 {
			location <- any_location_in(one_of(building));
			target <- any_location_in(one_of(building));
		}
	}
}

species building {
	aspect default {
		draw shape color: #lightgray border: #gray;
	}
}

species road {
	aspect default {
		draw shape color: #black;
	}
}

species people skills: [moving] {
	point target;
	
	reflex move {
		do goto (target: target, on: road_network, speed: 5.0);
		if (location = target) {
			target <- any_location_in(one_of(building));
		}
	}
	
	aspect default {
		draw circle(8) color: #blue;
	}
}

experiment Display type: gui {
	output {
		display City type: 2d {
			species building;
			species road;
			species people;
		}
	}
}
