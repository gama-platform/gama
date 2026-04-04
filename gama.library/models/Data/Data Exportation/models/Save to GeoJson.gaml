/**
* Name: Save to GeoJSON
* Author: Patrick Taillandier
* Description: Shows how to save agent geometries and attributes to a GeoJSON file for reuse or sharing with
*   external GIS tools. GeoJSON is a widely used open standard format for geographic feature collections; it stores
*   geometries and properties in a human-readable JSON structure that can be opened by QGIS, web mapping libraries,
*   and many other tools. The model creates a set of building agents and saves them with their spatial and attribute
*   data to a GeoJSON file.
* Tags: save_file, geojson, gis, export, geometry, spatial
*/

model SavetoGeoJson

global {
	init {
		geometry free_space <- copy(shape);
		
		//creation of the building agents that will be saved
		create building number: 50 {
			shape <- square(5.0);
			location <- any_location_in (free_space - 5.0);
			free_space <- free_space - shape;
		}
		save building to:"../results/buildings.geojson" format: "geojson";
	} 
} 
  
//species that represent the building agents that will be saved
species building {
	string type <- flip(0.8) ? "residential" : "industrial";
	aspect default {
		draw shape color: type = "residential" ? #gray : #pink;
	}
}
experiment main type: gui {
	output {
		display map {
			species building;
		}
	}
}
