/**
* Name: Life
* Author: Gama Development Team
* Description: A GAMA implementation of Conway's Game of Life, the most famous cellular automaton.
* This model is a simplification of "Life Using Grid Agents.gaml". It does not allow to edit the death/birth configurations 
* Uses bounded integer sampling for cell initialization; seeded patterns differ from legacy rnd.
*/
model life

global torus: torus_environment {
	int environment_width <- 200 min: 10 max: 1000;
	int environment_height <- 220 min: 10 max: 1000;
	bool torus_environment <- true;
	int density <- 15 min: 1 max: 99;
	
	rgb livingcolor <- #white;
	rgb dyingcolor <- #red;
	rgb emergingcolor <- #orange;
	rgb deadcolor <- #black;
	
	geometry shape <- rectangle(environment_width, environment_height);
	matrix kernel <- matrix([[1, 1, 1], [1, 0, 1], [1, 1, 1]]);
	matrix<float> m_alive ;
	matrix<float> m_neighbors;

	
	reflex generation when: cycle > 1 // to avoid the first run 
	{
		// 1. Update matrix from current grid state
		m_alive <- matrix<float>(matrix_with(life_cell, "grid_value"));
		
		// 2. Apply convolution to count neighbors (single vectorized operation)
		m_neighbors <- convolution(m_alive, kernel);
		
		// 3. Apply Game of Life rules in parallel
		ask life_cell parallel: true {
			previous <- grid_value;
			float nn <- float(m_neighbors[grid_x, grid_y]);
			grid_value <- grid_value > 0 ? (((nn = 2) or (nn = 3)) ? 1.0 : 0.0): ((nn = 3) ? 1.0 : 0.0);
		}
	}

}

grid life_cell width: environment_width height: environment_height use_individual_shapes: false use_regular_agents: false use_neighbors_cache: true parallel: true {
	float grid_value <- rnd(100, true) < density ? 1.0 : 0.0;
	// Track the previous state
	float previous <- grid_value;
	
	// Dynamic 4-color state machine
	rgb color <- compute_color(grid_value, previous) update: compute_color(grid_value, previous);
	
	rgb compute_color(float c, float p) {
		return c > 0 ? (p > 0 ? livingcolor : emergingcolor) : (p > 0 ? dyingcolor : deadcolor);

	}
}

experiment "Game of Life" type: gui {
	parameter 'Width:' var: environment_width category: 'Board';
	parameter 'Height:' var: environment_height category: 'Board';
	parameter 'Torus?:' var: torus_environment category: 'Board';
	parameter 'Initial density of live cells:' var: density category: 'Cells' unit: "%";
	
	output {
		display "Game of Life" type: 3d axes: false antialias: false {
			grid life_cell;
		}
	}
}