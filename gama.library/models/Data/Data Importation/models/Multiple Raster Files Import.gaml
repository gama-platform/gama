/**
* Name: Multiple Raster Files Import
* Author: Patrick Taillandier
* Description: Shows how to use multiple raster files simultaneously to initialize different attributes of a
*   single GAMA grid. In this example, a DEM (Digital Elevation Model) raster in ASC format provides elevation
*   values, and a land-cover raster in GeoTIFF format provides land-use categories. Each grid cell reads from
*   both files and its attributes are set accordingly. This technique is common in environmental and ecological
*   simulations where the spatial environment is described by several overlapping thematic layers.
* Tags: load_file, gis, 3d, dem, tif, asc, raster, grid, multiple, land_cover, elevation
*/

model importationraster

global {
	//the two grid files that we are going to use to initialize the grid
	file dem_file <- file("../includes/mnt.asc");
	file land_cover_file <- file("../includes/land-cover.tif");
	
	//we use the dem file to initialize the world environment
	geometry shape <- envelope(dem_file);
	
	//map of colors (key: land_use, value: color)  just uses to visualize the different land_use
	map<int,rgb> colors;
	
		
	init {
		// In GAMA, when a grid is built from multiple files, the 'bands' list only contains the values from the subsequent files.
		// Therefore, the value from the second file (land_cover_file) is located at index 0 of the 'bands' attribute.ask cell {
	
		ask cell {
			land_use <- int(bands[0]);
		}
		
		//we define a color per land_use and use it to define the color of the cell
		list<int> land_uses <- remove_duplicates(cell collect each.land_use);
		colors <- land_uses as_map (each::rnd_color(255));
		ask cell {
			color <- colors[land_use];
		}
	}
}
// We define the cell grid from the two grid files: the first file (dem_file) will be used as a reference for the definition of the grid's number of rows, columns, and location.
// In GAMA, the value of the FIRST file is exclusively stored in the 'grid_value' built-in variable.
// The values from the FOLLOWING files are stored in the 'bands' built-in list attribute (e.g., the second file is in bands[0], the third in bands[1], etc.).
grid cell files: [dem_file,land_cover_file] {
	int land_use;
}

experiment importationraster type: gui {
	output {
		display map type: 3d axes:false antialias:false{
			grid cell elevation: true  triangulation: true refresh: false;	
		}
	}
}
