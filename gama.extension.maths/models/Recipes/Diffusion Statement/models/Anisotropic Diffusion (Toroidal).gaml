/**
* Name: Anisotropic Diffusion (Toroidal)
* Author: Benoit Gaudou
* Description: Demonstrates anisotropic diffusion on a toroidal (wrap-around) grid. The same directional
*   diffusion kernel as the simple anisotropic model is applied, but the world edges wrap so that pheromone
*   diffusing off one side re-enters from the opposite side. Useful for modelling periodic environments
*   such as atmospheric or oceanic circulation patterns.
* Tags: diffusion, matrix, math, grid, anisotropic, toroidal, torus, pheromone, elevation
*/

model anisotropic_diffusion_torus

global torus: true {
	int size <- 64; // better to have a pow of 2 for the size of the grid
  	geometry shape <- envelope(square(size) * 10);
  	cells selected_cells;
  	matrix<float> mat_diff <- matrix([
									[4/9,2/9,0/9],
									[2/9,1/9,0.0],
									[0/9,0.0,0.0]]);
	init {
		selected_cells <- location as cells;
	}
	reflex new_Value {
		ask selected_cells{
			phero <- 1.0;
		}  
	}

	reflex diff {
		diffuse var: phero on: cells matrix: mat_diff method:dot_product;	
	}
}


grid cells height: size width: size  {
	float phero  <- 0.0;
	rgb color <- hsb(phero,1.0,1.0) update: hsb(phero,1.0,1.0);
	float grid_value update: phero * 100;
} 


experiment diffusion type: gui {
	output {
		display a type: 3d antialias:false{
			camera 'default' location: {277.7744,923.8543,660.6005} target: {320.0,320.0,0.0};
			grid cells elevation: true triangulation: true;
		}
	}
}
