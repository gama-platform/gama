/**
* Name: Save to ASC
* Author: Patrick Taillandier
* Description: Shows how to save grid data to an ESRI ASCII raster file (.asc) for reuse in later simulations or
*   in external GIS tools. The ESRI ASCII format is a simple text-based raster format that stores the grid dimensions,
*   cell size, and origin coordinates as a header, followed by one numeric value per cell. In GAMA, the 'save'
*   statement can write grid data directly to this format by specifying a grid species or a field variable.
*   The resulting file can be reloaded in subsequent GAMA models or in GIS software such as QGIS or ArcGIS.
* Tags: save_file, asc, grid, raster, export, esri
*/

model SavetoAsc

global {
	init {	
		//save grid "grid_value" attribute into the asc file.
		save cell to:"../results/grid.asc";
	}
}

//Grid that will be saved in the ASC File
grid cell width: 50 height: 50 {
	float grid_value <- self distance_to world.location;
	rgb color <- rgb(255 * (1 - grid_value / 50), 0,0);
}

experiment main type: gui {
	output {
		display map type:2d antialias:false{
			grid cell border: #black;
		}
	}
}
