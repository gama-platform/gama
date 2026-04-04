/**
* Name: Save to PNG
* Author: Patrick Taillandier
* Description: Shows how to export a GAMA grid as a georeferenced PNG image file. Unlike a plain PNG, the
*   georeferenced version embeds a world file (.pgw) alongside the image so that the pixel coordinates can
*   be mapped back to geographic coordinates. The model classifies grid cells based on whether buildings overlap
*   them, assigns colors accordingly, and saves the result. The output can be reloaded in GAMA or opened in
*   external GIS tools that support world files.
* Tags: save_file, png, grid, raster, export, image, georeferenced
*/

model SavetoGeotiff

global {
	shape_file buildings <- shape_file("../includes/building.shp");
	geometry shape <- envelope(buildings);
	init {	 
		ask cell {
			if not empty(buildings overlapping self) {
				color <- #blue;
			}
		}
		//save grid "grid_value" attribute into the georefrenced png file.
		save cell to:"../results/grid.png";
	}
}

grid cell width: 50 height: 50 ;

experiment main type: gui {
	output {
		display map type:2d antialias:false {
			grid cell border: #black;
		}
	}
}
