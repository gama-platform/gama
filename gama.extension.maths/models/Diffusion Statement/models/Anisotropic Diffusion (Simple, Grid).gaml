/**
* Name: Anisotropic Diffusion (Simple, Grid)
* Author: Benoit Gaudou
* Description: Demonstrates directional (anisotropic) diffusion on a grid. Instead of the default uniform
*   matrix, a custom asymmetric diffusion kernel is supplied, causing pheromone to spread preferentially
*   in one direction. A central cell emits pheromone each step; the anisotropic matrix shapes the resulting
*   gradient into an elongated or directional plume. Compare with 'Uniform Diffusion (Grid)' to see the
*   effect of the custom kernel.
* Tags: diffusion, matrix, math, grid, anisotropic, pheromone, elevation, 3d
*/

model anisotropic_diffusion

global {
	int size <- 64; // better to have a pow of 2 for the size of the grid
  	geometry shape <- envelope(square(size) * 10);
  	cells selected_cells;
  	
  	// Declare the anisotropic matrix (diffuse to the left-upper direction)
	matrix<float> mat_diff <- matrix([
									[5/9,1.5/9,0/9],
									[1.5/9,1/9,0.0],
									[0/9,0.0,0.2/9]]);
	
	reflex diff { 
		diffuse var: phero on: cells matrix:mat_diff;
	}

	// Initialize the emiter cell as the cell at the center of the word
	init {
		selected_cells <- location as cells;
	}
	reflex new_Value {
		ask selected_cells {
			phero <- rnd(2.0);
		}
	}
}


grid cells height: size width: size neighbors: 8 {
	// "phero" is the variable storing the value of the diffusion
	float phero  <- 0.0;
	// the color of the cell is linked to the value of "phero".
	rgb color <- hsb(phero,1.0,1.0) update: hsb(phero,1.0,1.0);
	// Update the "grid_value", which will be used for the elevation of the cell
	float grid_value update: phero * 100;
} 


experiment diffusion type: gui {
	output {
		display a type: 3d antialias:false{
			camera 'default' location: {311.2555,820.9735,742.8089} target: {320.0,320.0,0.0};
			// Display the grid with elevation
			grid cells elevation: true triangulation: true;
		}
	}
}
