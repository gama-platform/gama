/**
* Name: GeoPackage File Import
* Author: Alexis Drogoul
* Description: Shows how to read a GeoPackage (.gpkg) file in GAMA. GeoPackage is an open, SQLite-based format that
*   can store several vector layers in a single file. The 'geopackage_file' operator reads the first layer of the 
*   file by default; a layer name can be passed as a second argument to select another one. The example loads the 
*   two layers ("building" and "road") of a small city extract and creates one agent per feature.
* Tags: load_file, geopackage, gpkg, gis, geometry, import, spatial
*/

model geopackage_loading

global {
	file building_file <- geopackage_file("../includes/city.gpkg", "building");
	file road_file <- geopackage_file("../includes/city.gpkg", "road");
	geometry shape <- envelope(building_file) + envelope(road_file);
	
	init {
		create building from: building_file;
		create road from: road_file;
		write "" + length(building) + " buildings and " + length(road) + " roads loaded.";
	}
}

species building {
	aspect default {
		draw shape color: #gray border: #black;
	}
}

species road {
	aspect default {
		draw shape color: #red;
	}
}

experiment Display type: gui {
	output {
		display City type: 2d {
			species building;
			species road;
		}
	}
}
