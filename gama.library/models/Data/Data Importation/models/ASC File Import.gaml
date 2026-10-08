/**
* Name: ASC File Import
* Author: Patrick Taillandier
* Description: Shows how to initialize a GAMA grid using an ESRI ASCII raster file (.asc). The grid_file operator
*   loads the ASC file and automatically infers the grid dimensions, cell size, and geographic extent. Each cell
*   of the grid is initialized with the numeric value from the corresponding raster pixel, which can represent
*   elevation, land use categories, habitat suitability, or any other continuous or categorical spatial data.
*   The world geometry is set to the envelope of the loaded file, ensuring correct spatial alignment.
* Tags: grid, load_file, asc, raster, gis, initialization, spatial
*/

model ascimport

global {
	//definiton of the file to import
	grid_file grid_data <- file('../includes/hab10.asc') ;
	
	//computation of the environment size from the geotiff file
	geometry shape <- envelope(grid_data);	
	
	
	init {
		write actual_type_of(grid_data);
		write actual_type_of(grid_data.contents);
	}
}



//definition of the grid from the asc file: the width and height of the grid are directly read from the asc file. The values of the asc file are stored in the grid_value attribute of the cells.
grid cell file: grid_data{
	init {
		color<- grid_value = 0.0 ? #black  : (grid_value = 1.0  ? #green :   #yellow);
	}
}

experiment gridloading type: gui {
	output {
		display "As DEM" type: 3d axes:false{
			grid cell border: #gray elevation: self.grid_value * 300 ;
		}
		
		display "As 2D grid"  type: 2d {
			grid cell border: #black;
		}
	} 
}


