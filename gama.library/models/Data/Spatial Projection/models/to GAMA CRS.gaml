/**
* Name: To GAMA CRS
* Author: Patrick Taillandier
* Description: Shows how to use the 'to_GAMA_CRS' operator to convert geometries from an external coordinate
*   reference system (CRS) into GAMA's internal CRS. External data (such as GPS coordinates in WGS84, or
*   projected coordinates in a national grid) must be reprojected into the simulation's CRS before they can
*   be used spatially. The model loads a building shapefile that defines the simulation CRS, then reads point
*   data from a CSV file expressed in a different CRS and reprojects them so they align correctly with the
*   loaded buildings.
* Tags: gis, shapefile, spatial_computation, spatial_transformation, projection, crs, epsg, reprojection
*/
model To_GAMA_CRS

global {
	
	file building_file <- shape_file("../gis/init.shp");
	file data_csv_file <- csv_file("../gis/data.csv", ",", float);
	
	geometry shape <- envelope(building_file); //set the GAMA coordinate reference system using the one of the building_file (Lambert zone II).
	
	init {
		create building from: building_file;
		matrix<float> data <- matrix<float>(data_csv_file);
		loop i from: 0 to: data.rows - 1 {
			point poi_location_WGS84 <- {data[0,i],data[1,i]};
			point poi_location_GAMA <- point(to_GAMA_CRS(poi_location_WGS84, "EPSG:4326"));
			write "\nPOI location - WGS84: " + poi_location_WGS84 +"\nGAMA CRS: "+ poi_location_GAMA; 
			create poi(location:poi_location_GAMA);
		}
	}
}

species poi {
	aspect default {
		draw circle(5) color: #red border: #black;
	}
}
species building {
	aspect default {
		draw shape color: #gray border: #black;
	}
}

experiment ProjectionManagement type: gui {
	output {
		display map {
			species building;
			species poi;
		}
	}
}
