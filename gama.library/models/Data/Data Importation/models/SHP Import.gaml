/**
* Name: Shapefile Import
* Author: Patrick Taillandier
* Description: The simplest and most common way to load geographic data in GAMA: importing an ESRI Shapefile
*   and creating agents from its features. The 'shape_file' operator loads the file and infers the environment
*   geometry from its extent. The 'create' statement with the 'from' facet then creates one agent per feature in
*   the shapefile, automatically assigning the geometry and all shapefile attribute columns to matching agent
*   attributes. The environment boundary is set to the bounding box of the shapefile's features.
* Tags: load_file, shapefile, shp, gis, import, geometry, spatial, agent
*/
model simpleShapefileLoading



global {
	file shape_file_buildings <- shape_file("../includes/buildings_simple.shp");
	
	//definition of the geometry of the world agent (environment) as the envelope of the shapefile
	geometry shape <- envelope(shape_file_buildings);
	
	init {
		//creation of the building agents from the shapefile: the height and type attributes of the building agents are initialized according to the HEIGHT and NATURE attributes of the shapefile
		create building from: shape_file_buildings with:(height:float(get("HEIGHT")), type:string(get("NATURE")));
	}
}

species building {
	float height;
	string type;
	rgb color <- type = "Industrial" ? #pink : #gray;
	
	aspect default {
		draw shape depth: height color: color;
	}
	
}

experiment GIS_agentification type: gui {
	output {
		display city_display type: 3d axes:false{
			species building;
		}
	}
}

