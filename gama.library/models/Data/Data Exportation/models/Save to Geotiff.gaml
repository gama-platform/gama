/**
* Name: Save to GeoTIFF
* Author: Patrick Taillandier
* Description: Shows how to export a GAMA grid as a georeferenced GeoTIFF raster file. GeoTIFF is a standard
*   format for raster geographic data that embeds spatial reference information (coordinate system, cell size,
*   and origin) directly in the file. The exported file can be reloaded in subsequent GAMA simulations, or
*   opened and analyzed in external GIS software such as QGIS, ArcGIS, GRASS, or processed with tools like GDAL.
*   The 'save' statement with 'format: "geotiff"' handles the conversion automatically.
* Tags: save_file, tiff, geotiff, grid, raster, export, gis
*/

model SavetoGeotiff

global {
	init {	 
		//save grid "grid_value" attribute into the geotiff file.
		save cell to:"../results/grid.tif" format:"geotiff";
	}
}

//Grid that will be saved in the Geotiff File
grid cell width: 50 height: 50 {
	float grid_value <- self distance_to world.location * 2;
	rgb color <- rgb(255 * (1 - grid_value / 50), 0,0);
}

experiment main type: gui {
	output {
		display map {
			grid cell border: #black;
		}
	}
}
