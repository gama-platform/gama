/**
* Name: 3D Shapefile Loading
* Author: Gama Development Team
* Description: Shows how to load a 3D Shapefile in GAMA and create agents whose geometry includes the third
*   dimension (z-coordinates). The key difference from a regular shapefile load is passing 'true' as the second
*   argument of the 'shape_file' operator, which instructs GAMA to read and preserve the z-values from the file.
*   The resulting agents have 3D geometries that can be displayed and manipulated in 3D environments. The example
*   uses a shapefile of urban furniture objects with elevation data.
* Tags: 3d, shapefile, load_file, geometry, elevation, gis
*/
model shapefile_loading

global {
	 
	//file variable that will store the shape file : the "true" argument allows to specify that we want to take into account the 3D dimension of the data
	file shape_file_gis_3d_objects <- shape_file('../includes/Mobilier.shp', true);
	geometry shape <- envelope(shape_file_gis_3d_objects);
	init {
		create gis_3d_object(location:location) from: shape_file_gis_3d_objects;
	}
}

species gis_3d_object {
	aspect base {
		draw shape  color: #gray border: #darkgray width: 4;
	}
}

experiment display_shape type: gui {

	output {
		display city_display type: 3d axes:false background: #black{
			species gis_3d_object aspect: base;
		}

	}
}

