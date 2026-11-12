/**
* Name: Life
* Author: Gama Development Team
* Description: A GAMA implementation of Conway's Game of Life, the most famous cellular automaton.
* This model is a simplification of "Life Using Grid Agents.gaml". It does not allow to edit the death/birth configurations
* Grid attribute brackets preserve cell coordinates and infer the matrix type for reads and assignments.
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
		m_alive <- life_cell[grid_value];
		
		// 2. Apply convolution to count neighbors (single vectorized operation)
		m_neighbors <- convolution(m_alive, kernel);
		
		// 3. Apply the rules element-wise, then write both snapshots back to the grid
		matrix<float> next_alive <- (m_neighbors = 3.0) + ifelse(m_neighbors = 2.0, m_alive, 0.0);
		life_cell[previous] <- m_alive;
		life_cell[grid_value] <- next_alive;
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