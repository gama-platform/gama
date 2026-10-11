/**
* Name: Blend Color in a Cuve (Multiple Signals)
* Author: Julien Mazars
* Description: Demonstrates multi-signal diffusion on a grid where three differently colored pheromones
*   are emitted simultaneously from three corners of a bounded 'cuve' (vat). As they spread and overlap,
*   their RGB color values blend. The 'cycle_length' facet accelerates computation by running multiple
*   diffusion iterations per simulation step. The 'avoid_mask' facet preserves the total pheromone sum.
* Tags: diffusion, matrix, math, grid, color, blend, multiple_signals, cycle_length, avoid_mask, pheromone
*/

model cycle_length

global {
	int size <- 64; // better to have a pow of 2 for the size of the grid
	int cycle_length <- 5;
  	geometry shape <- envelope(square(size));
  	list<cells> top_left_cells;
  	list<cells> top_right_cells;
  	list<cells> bot_left_cells;
  	// Declare the uniform matrix
  	matrix<float> mat_diff <- matrix([
									[1/9,1/9,1/9],
									[1/9,1/9,1/9],
									[1/9,1/9,1/9]]);

	// Initialize the emiter cells
	init {
		top_left_cells <- list<cells>(cells where (each.grid_x < location.x
			and each.grid_x > cycle_length
			and each.grid_y < location.y
			and each.grid_y > cycle_length
		));
		top_right_cells <- list<cells>(cells where (each.grid_x < size-cycle_length
			and each.grid_x > location.x
			and each.grid_y < location.y
			and each.grid_y > cycle_length
		));
		bot_left_cells <- list<cells>(cells where (each.grid_x < location.x
			and each.grid_x > cycle_length
			and each.grid_y < size-cycle_length
			and each.grid_y > location.y
		));
	}
	reflex init_value when:cycle=0 {
		ask(top_left_cells){
			phero1 <- 1.0;
		}
		ask(top_right_cells){
			phero2 <- 1.0;
		}
		ask(bot_left_cells){
			phero3 <- 1.0;
		}	
	}

	reflex diff {
		// Declare a diffusion on the grid "cells" for each one of the pheromons. 
		// In order to not loosing phero value, we apply a hand made mask (with the operator "where") and we turn the "avoid_mask" facet to true.
		list cells_where_diffuse <- cells where (each.grid_x < size-cycle_length and each.grid_x > cycle_length and each.grid_y < size-cycle_length and each.grid_y > cycle_length);
		diffuse var: phero1 on: cells_where_diffuse matrix: mat_diff avoid_mask: true method:dot_product cycle_length:cycle_length;
		diffuse var: phero2 on: cells_where_diffuse matrix: mat_diff avoid_mask: true method:dot_product cycle_length:cycle_length;
		diffuse var: phero3 on: cells_where_diffuse matrix: mat_diff avoid_mask: true method:dot_product cycle_length:cycle_length;
	}
}


grid cells height: size width: size {
	// "phero1", "phero2", "phero3" are the variables storing the value of the diffusion
	float phero1  <- 0.0;
	float phero2  <- 0.0;
	float phero3  <- 0.0;
	// The color of the cell is construct using the 3 pheromons.
	rgb color <- rgb(phero1*256,phero2*256,phero3*256) update: rgb(phero1*256,phero2*256,phero3*256);
} 


experiment diffusion type: gui {
	output {
		display a type: 3d antialias:false{
			camera 'default' location: {54.9788,112.1365,32.8371} target: {32.0,32.0,0.0};
			// Display the grid with elevation
			grid cells elevation: (phero1+phero2+phero3)*10 triangulation: true;
		}
	}
}
