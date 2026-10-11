/**
* Name: Life
* Author: Gama Development Team
* Description: A GAMA implementation of Conway's Game of Life, the most famous cellular automaton.
* This model is a simplification of "Life Using Grid Agents.gaml". It does not allow to edit the death/birth configurations 
*/
model life

global torus: torus_environment {
	int environment_width <- 250 min: 10 max: 1000;
	int environment_height <- 250 min: 10 max: 1000;
	bool parallel <- false;
	bool torus_environment <- true;
	int density <- 15 min: 1 max: 99;
	list<int> living_conditions <- [2, 3];
	list<int> birth_conditions <- [3];
	
	rgb livingcolor <- #white;
	rgb dyingcolor <- #red;
	rgb emergingcolor <- #orange;
	rgb deadcolor <- #black;
	
	geometry shape <- rectangle(environment_width, environment_height);
	matrix kernel <- matrix([[1, 1, 1], [1, 0, 1], [1, 1, 1]]);
	
	matrix m_alive <- matrix_with(life_cell, "alive_float") update: matrix_with(life_cell, "alive_float");

	
	reflex generation when: cycle > 1 // to avoid the first run 
	{
		// 1. Save the current state as the previous state before updating
		do set_values(life_cell, "prev_alive_float", m_alive);
		
		// 2. Run convolution and calculate rules
		matrix m_living <- convolution(m_alive, kernel);
		matrix stay_alive <- (m_living = 2.0) + (m_living = 3.0);
		matrix new_born <- (m_living = 3.0);
		
		// 3. Compute next state using the fixed Java ifelse operator
		matrix next_state <- ifelse(m_alive, stay_alive, new_born);
		
		// 4. Apply the new state to the grid
		do set_values(life_cell, "alive_float", next_state);
	}

}

grid life_cell width: environment_width height: environment_height neighbors: 4 use_individual_shapes: false use_regular_agents: false use_neighbors_cache: false parallel: parallel {
	float alive_float <- (rnd(100)) < density ? 1.0 : 0.0;
	
	// Track the previous state
	float prev_alive_float <- alive_float;
	
	// Dynamic 4-color state machine
	rgb color <- alive_float > 0.0 ? 
		(prev_alive_float > 0.0 ? livingcolor : emergingcolor) : 
		(prev_alive_float > 0.0 ? dyingcolor : deadcolor) update: alive_float > 0.0 ? 
		(prev_alive_float > 0.0 ? livingcolor : emergingcolor) : 
		(prev_alive_float > 0.0 ? dyingcolor : deadcolor);
}

experiment "Game of Life" type: gui {
	parameter 'Width:' var: environment_width category: 'Board';
	parameter 'Height:' var: environment_height category: 'Board';
	parameter 'Torus?:' var: torus_environment category: 'Board';
	parameter 'Initial density of live cells:' var: density category: 'Cells';
	parameter 'Color of live cells:' var: livingcolor category: 'Colors';
	parameter 'Color of dying cells:' var: dyingcolor category: 'Colors';
	parameter 'Color of emerging cells:' var: emergingcolor category: 'Colors';
	parameter 'Color of dead cells:' var: deadcolor category: 'Colors';
	
	output {
		display Life type: 3d axes: false antialias: false {
			grid life_cell;
		}
	}
}