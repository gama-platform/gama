/**
* Name: GeoTIFF File Import
* Author: Patrick Taillandier
* Description: Shows how to create a grid of cells from a GeoTIFF raster file. GeoTIFF is the standard format
*   for georeferenced raster data used in remote sensing and GIS. GAMA reads GeoTIFF files via the 'grid_file'
*   operator and automatically infers the grid dimensions, cell size, and coordinate reference system. Each grid
*   cell is initialized with the pixel value from the corresponding raster location. Note: GAMA currently supports
*   only byte-type GeoTIFF files (typically displayed in grayscale); Float32 and Float64 data types are not yet
*   supported. The example loads a land-use grid over Bogotá, Colombia.
* Tags: load_file, tif, geotiff, gis, grid, raster, import, spatial
*/

model geotiffimport

global {
	//definiton of the file to import
	file grid_data <- grid_file("../includes/bogota_grid.tif");

	//computation of the environment size from the geotiff file
	geometry shape <- envelope(grid_data);	
	
	float max_value;
	float min_value;
	init {
		max_value <- cell max_of (each.grid_value);
		min_value <- cell min_of (each.grid_value);
		ask cell {
			int val <- int(255 * ( 1  - (grid_value - min_value) /(max_value - min_value)));
			color <- rgb(val,val,val);
		}
	}
}

//definition of the grid from the geotiff file: the width and height of the grid are directly read from the asc file. The values of the asc file are stored in the grid_value attribute of the cells.
grid cell file: grid_data;

experiment show_example type: gui {
	output {
		display test axes:false type:3d{
			camera 'default' location: {16384.6813,51385.7828,15210.911} target: {15510.9655,18019.9225,0.0};
			grid cell border: #black elevation:grid_value*5 triangulation:true;
		}
	} 
}
