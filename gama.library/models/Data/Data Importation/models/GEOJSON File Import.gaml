/**
* Name: GeoJSON File Import
* Author: Alexis Drogoul
* Description: Shows how to import a GeoJSON file in GAMA and use it to create agents whose geometries and
*   attributes come from the file. GeoJSON is a lightweight, human-readable format widely used for web mapping
*   and geographic data exchange. GAMA supports it natively via the 'geojson_file' operator. The 'create'
*   statement with 'from' and 'with' facets maps GeoJSON feature properties to agent attributes using the
*   'read()' operator. The example loads a world countries dataset and creates one agent per country.
* Tags: load_file, geojson, gis, json, geometry, import, spatial
*/

model geojson_loading   

global {
	file geo_file <- geojson_file("../includes/countries.geojson");
	geometry shape <- envelope(geo_file);
	init {
		create countries(name:read("name")) from: geo_file;
	}
} 

species countries {
	rgb color <- rnd_color(255);
	rgb text_color <- (color.brighter);
	
	init {
		shape <- (simplification(shape,0.01));
	}
	aspect default {
		draw shape color: color depth: 10;
		draw name font: font("Helvetica", 12 + #zoom, #bold) color: #black at: location + {0,0,12} perspective:false;
	}
}

experiment Display  type: gui {
	output {
		display Countries type: 3d{	
			species countries;			
		}
	}
}
