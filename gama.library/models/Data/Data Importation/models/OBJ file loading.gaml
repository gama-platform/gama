/**
* Name: OBJ File Loading
* Author: Arnaud Grignard
* Description: Shows how to load a 3D object file (OBJ, SVG, or 3DS) and assign it as the actual geometry of
*   agents. Unlike the drawing approach, here the loaded geometry becomes the agent's spatial shape, which is
*   used for both display and spatial computations. This is suitable when the 3D object faithfully represents
*   the spatial footprint of the simulated entity. The model loads a complex 3D object and places it as an agent
*   in a large environment.
* Tags: load_file, 3d, obj, svg, geometry, spatial, agent, shape
*/

model obj_loading   

global {
	
	geometry shape <- square(10000);

	init { 
		create objects{
			location <- world.location;
		}
	}  
} 

species objects {
	
	geometry shape <- obj_file("../includes/teapot.obj") as geometry;


			
}	

experiment Display  type: gui {
	output {
		display complex  background:#gray type: 3d{
		  species objects;				
		}
	}
}
