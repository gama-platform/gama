/***
* Name: Simplification and Buffer
* Author: Alexis Drogoul
* Description: An interactive demonstration of two key geometry processing operations: simplification and
*   buffering. Simplification (Douglas-Peucker algorithm) reduces the number of vertices in a geometry while
*   preserving its overall shape; the 'tolerance' parameter controls how aggressively vertices are removed.
*   Buffering expands or contracts a geometry by a given distance. The model lets users manipulate both
*   parameters live via sliders and immediately see their effect on a reference geometry.
* Tags: geometry, simplification, buffer, spatial_computation, tolerance, interactive, display
***/

model Tolerance

global {
	
	int tolerance <- 0;
	int buffer <- 0;
	file complex <- file("../gis/water_body.shp");
	geometry shape <- envelope(complex);
	
	init {
		create shapes from: complex ;
	}
}


species shapes;

experiment "Simplify this ! " {
	
	parameter "Simplification tolerance" category: "Change the value and observe the visual result" var:tolerance min: 0 max: 800 step: 1 {
		do update_outputs;
	}
	
	parameter "Buffer value" category: "Change the value and observe the visual result" var:buffer min: -100 max: 100 step: 1  {
		do update_outputs;
	}
	
	user_command "Close the simulation"  category:"Change the value and observe the visual result" color: #red {
		do die;
	}
	
	
	
	output {
		layout #split consoles: false tray: false tabs: false controls: false editors: false toolbars: false navigator: false;
		display my_display type:3d axes: false { 
			species shapes {
				draw shape color: #red;
			}
			species shapes {
				draw simplification(shape, tolerance) + buffer color: #blue;
			}

		}
		
	}	
}
