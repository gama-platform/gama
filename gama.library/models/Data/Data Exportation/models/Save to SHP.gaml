/**
* Name: Save to Shapefile
* Author: Patrick Taillandier
* Description: Demonstrates how to export agent geometries and attributes to an ESRI Shapefile (.shp) for reuse in
*   subsequent simulations or in external GIS tools such as QGIS and ArcGIS. Shapefiles are one of the most widely
*   used vector GIS formats and support point, line, and polygon geometries together with a dBASE attribute table.
*   The model creates a set of building agents with random positions and shapes, then saves them to a shapefile.
*   The exported file can be reloaded in a subsequent GAMA run using the 'shape_file' operator.
* Tags: save_file, shapefile, shp, gis, export, geometry, vector
*/

model Savetoshapefile

global {
	init {
		geometry free_space <- copy(shape);
		
		//creation of the building agents that will be saved
		create building number: 50 {
			shape <- square(5.0);
			location <- any_location_in (free_space - 5.0);
			free_space <- free_space - shape;
		}
		//save building geometry into the shapefile: add the attribute TYPE which value is set by the type variable of the building agent and the attribute ID 
		save building to:"../results/buildings.shp" format:"shp" attributes: ["ID":: int(self), "TYPE"::type]; 
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
