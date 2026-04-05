/**
* Name: Uniform Diffusion (Grid)
* Author: Benoit Gaudou
* Description: Demonstrates uniform (isotropic) diffusion on a grid using the 'diffuse' statement. A cell
*   at the center emits pheromone each step; it spreads outward through the grid using the default uniform
*   3×3 diffusion matrix (value 1/nb_neighbors for each neighbor). The pheromone concentration is rendered
*   as a 3D elevation map (each cell's height = pheromone level). This is the simplest diffusion model and
*   the starting point before exploring anisotropic or masked diffusion.
* Tags: diffusion, matrix, math, grid, pheromone, elevation, 3d
*/

model uniform_diffusion

global {
	int size <- 64; // better to have a pow of 2 for the size of the grid
  	geometry shape <- envelope(square(size) * 10);
  	cells_eight_nb selected_cells_8;
  	cells_four_nb selected_cells_4;

	// Initialize the emiter cell as the cell at the center of the word
	init {
		selected_cells_8 <- location as cells_eight_nb;
		selected_cells_4 <- location as cells_four_nb;
	}
	reflex new_Value {
		ask selected_cells_4 {
			phero <- 1.0;
		}
		ask selected_cells_8 {
			phero <- 1.0;
		}
	}
	reflex diff {
		// Declare a diffusion on the grid "cells", with a uniform matrix of diffusion. The value of the diffusion
		// will be store in the new variable "phero" of the cell.
		diffuse var: phero on: cells_eight_nb ;
		diffuse var: phero on: cells_four_nb ;
	}
}


grid cells_eight_nb height: size width: size neighbors: 8 {
	// "phero" is the variable storing the value of the diffusion
	float phero  <- 0.0;
	// the color of the cell is linked to the value of "phero".
	rgb color <- hsb(min([0.6,phero]),1.0,1.0) update: hsb(min([0.6,phero]),1.0,1.0);
	// Update the "grid_value", which will be used for the elevation of the cell
	float grid_value update: phero * 100;
} 

grid cells_four_nb height: size width: size neighbors: 4 {
	// "phero" is the variable storing the value of the diffusion
	float phero  <- 0.0;
	// the color of the cell is linked to the value of "phero".
	rgb color <- hsb(min([0.6,phero]),1.0,1.0) update: hsb(min([0.6,phero]),1.0,1.0);
	// Update the "grid_value", which will be used for the elevation of the cell
	float grid_value update: phero * 100;
} 


experiment diffusion type: gui {
	output synchronized: true {
		layout #split;
		display uniform_diffusion_in_8_neighbors_grid type: 3d antialias:false {
			camera 'default' location: {-185.5731,1098.5143,337.845} target: {320.0,320.0,0.0};
			// Display the grid with elevation
			grid cells_eight_nb elevation: true triangulation: true;
		}
		display uniform_diffusion_in_4_neighbors_grid type: 3d antialias:false{
			camera 'default' location: {-185.5731,1098.5143,337.845} target: {320.0,320.0,0.0};
			// Display the grid with elevation
			grid cells_four_nb elevation: true triangulation: true;
		}
	}
}
