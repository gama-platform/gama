/**
* Name: GIS Visualization
* Author: Patrick Taillandier
* Description: Shows how to visualize GIS vector data (shapefiles) directly in a GAMA display without having
*   to create agent species for each layer. The 'draw' statement can render a shape_file or geometry directly
*   from the global init block or a display layer, which is useful for background layers that don't need agent
*   behavior. Textures can be applied to the building polygons for photorealistic appearance. This approach
*   reduces model complexity for purely decorative spatial layers.
* Tags: 3d, shapefile, texture, gis, visualization, display, vector, background
*/

model GIS_visualization

global {
	shape_file shape_file_buildings <- shape_file("../includes/building.shp");
	geometry shape <- envelope(shape_file_buildings);
	string texture <- "../images/building_texture/texture1.jpg";
	string roof_texture <- "../images/building_texture/roof_top.jpg";	
}

experiment GIS_visualization type: gui {
	float minimum_cycle_duration <- 1#s;

	
	output {
		layout #split parameters: false navigator: false editors: false consoles: false;
		// display of buildings in 3D with texture and with reading their HEIGHT attribute from the shapefile
		display gis_displays_graphics type: 3d  {
			camera 'default' location: {809.5553,1386.2038,705.1688} target: {301.1933,449.9174,0.0};
			graphics "Buildings as shapes" refresh: false {
				loop bd over: shape_file_buildings {
					draw bd depth: rnd(50) + 50 texture:[roof_texture,texture] border:false;
				}
			}
		}
		
		//display of the building as an image
		display gis_displays_image type: 3d {
			picture "Buildings as images" gis: shape_file_buildings.path color: rgb("gray") refresh: false;
		}
	}
}
