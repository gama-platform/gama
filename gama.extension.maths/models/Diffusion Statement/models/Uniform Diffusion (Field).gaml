/**
* Name: Uniform Diffusion (Field)
* Author: Benoit Gaudou
* Description: Demonstrates uniform (isotropic) diffusion on a GAMA field (continuous raster) using the
*   'diffuse' statement. Identical in logic to 'Uniform Diffusion (Grid)' but uses the field type instead
*   of a grid species, offering better performance for large spatial extents. A central cell emits pheromone
*   each step; it spreads through the field using the default uniform diffusion matrix. The field values
*   are rendered as a colored gradient.
* Tags: diffusion, matrix, math, field, pheromone, elevation, gradient, performance
*/

model uniform_diffusion

global {
	int size <- 128; // better to have a pow of 2 for the size of the grid
  	field cells <- field(size, size, 0.0);

	// Initialize the emiter cell as the cell at the center of the word
	reflex new_Value {
		cells[any_point_in(circle(25))] <- (100);
	}
	reflex diff {
		// Declare a diffusion on the grid "cells", with a uniform matrix of diffusion. 
		diffuse "trial" on: cells ;
	}
}

experiment diffusion type: gui autorun:true {
	output  synchronized: true {
		display uniform_diffusion_in_8_neighbors_grid type: 3d camera:#from_up_front axes: false {
			mesh cells color: #green triangulation: true scale: 1 smooth: true ;
		}
	}
}