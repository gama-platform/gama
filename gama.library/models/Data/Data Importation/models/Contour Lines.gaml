/**
* Name: Contour Lines Import
* Author: Patrick Taillandier
* Description: Imports a shapefile of topographic contour lines and uses them to reconstruct a 3D terrain surface.
*   The model builds triangles from consecutive contour line pairs and assigns each triangle an elevation value
*   derived from the 'elevation' attribute stored in the shapefile column. The resulting 3D surface approximates
*   the underlying terrain and can be visualized in a 3D GAMA display. This technique is useful when a DEM raster
*   is not available but vector contour data is, as is common with many topographic datasets.
* Tags: load_file, gis, shapefile, contour, elevation, 3d, terrain, triangulation
*/


model contour_lines_import

global {
	//the contour lines shapefile
	file shape_file_cl <- file('../includes/contourLines.shp') ;
	
	//define the size of the world from the countour line shapefile
	geometry shape <- envelope(shape_file_cl);

        //tolerance for the triangulation; if an error appears during the triangulation, a workaround consists in increasing this tolerance (0.01 for instance).
	float tolerance <- 0.0;
	
	init {
		//create the contour line agents from the shapefile, and init the elevation for each agent
		create contour_line from: shape_file_cl with: (elevation: float(read("ELEVATION")));
		
		//triangulate the contour lines
		list<geometry> triangles  <- triangulate (list(contour_line), tolerance);
		
		//for each triangle geometry, create a triangle_ag agent and compute the elevation of each of its points (and modified their z value)
		loop tr over: triangles {
			create triangle_ag {
				shape <- tr;
				loop i from: 0 to: length(shape.points) - 1{ 
					float val <- (contour_line closest_to (shape.points at i)).elevation;
					shape <- shape set_z (i,val);
				}
			}
		}	
	}
}

species contour_line {
	float elevation;
	aspect default {
		draw shape color: #red depth: 30 at: {location.x,location.y, elevation}; 
	}
}
species triangle_ag {
	aspect default {
		draw shape color: #grey ; 
	}
}


experiment contour_lines_import type: gui {
	output {
		display map type: 3d {
			camera 'default' location: {4341.8834,20215.2668,9585.3894} target: {5500.0,5500.0,0.0};
			species triangle_ag refresh: false;
			species contour_line refresh: false;
		}
	}
}
