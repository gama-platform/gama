/**
* Name: OBJ File Drawing
* Author: Arnaud Grignard
* Description: Shows how to use an OBJ file to draw a complex 3D geometry as the visual representation of agents.
*   OBJ is a common 3D model format supported by most 3D modeling tools. In this model, the OBJ file provides only
*   the visual shape — the agent's functional geometry is still a simple shape for computation purposes, while the
*   draw statement renders the 3D model. This pattern keeps spatial computations fast while enabling richly detailed
*   3D visualization. Multiple agent instances can share the same OBJ file.
* Tags: load_file, 3d, obj, geometry, visualization, draw, agent
*/


model obj_drawing   

global {
	geometry shape <- square(40);

	init { 
		create objects number: 30;
	}  
} 

species objects skills: [moving]{
	rgb color <- rgb(rnd(255),rnd(255),rnd(255));
	int size <- rnd(10) + 1;
	int rot <- 1000 + rnd(1000);
	reflex m  {
		do wander amplitude: 30.0 speed: 1.0;
	}
	aspect obj {
		draw obj_file("../includes/teapot.obj") color: color size: size rotate: cycle/rot::{0,1,0} ;
	}
}	

experiment Display  type: gui {
	output synchronized: true{
		display ComplexObject type: 3d background:#orange{
			species objects aspect:obj;				
		}
	}
}
