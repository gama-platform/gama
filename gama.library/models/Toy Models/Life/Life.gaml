/**
* Name: Life
* Author: Gama Development Team
* Description: A GAMA implementation of Conway's Game of Life, the most famous cellular automaton. Each cell
*   is either alive or dead. At every step, a cell's next state is determined by exactly two rules: (1) a live
*   cell with 2 or 3 live neighbors survives; otherwise it dies; (2) a dead cell with exactly 3 live neighbors
*   becomes alive. Despite these simple rules, complex and persistent patterns emerge, including oscillators,
*   spaceships, and stable structures. The model supports toroidal or bounded environments and multiple random
*   initial configurations.
* Tags: grid, cellular_automaton, life, game_of_life, emergence, Conway, complexity
*/
model life

//Declare the world as a torus or not torus environment
global torus: torus_environment {
	//Size of the environment
	int environment_width <- 200 min: 10 max: 1000;
	int environment_height <- 200 min: 10 max: 1000;
	bool parallel <- true;
	//Declare as torus or not
	bool torus_environment <- true;
	//Density 
	int density <- 25 min: 1 max: 99;
	//Conditions to live
	list<int> living_conditions <- [2, 3];
	//Conditions to birth
	list<int> birth_conditions <- [3];
	//Color for living cells
	rgb livingcolor <- #white;
	//Color for dying cells
	rgb dyingcolor <- #red;
	//Color for emerging cells
	rgb emergingcolor <- #orange;
	//Color for dead cells
	rgb deadcolor <- #black;
	//Shape of the environment
	geometry shape <- rectangle(environment_width, environment_height);
	
	matrix kernel <- matrix([[1, 1, 1], [1, 0, 1], [1, 1, 1]]);

	//Initialization of the model by writing the description of the model in the console
	init {
		do description();
	}
	
	//Ask at each life_cell to evolve and update
	reflex generation {
		matrix m_alive <- matrix_with(life_cell, "alive_float");
		matrix m_living <- convolution(m_alive, kernel);
		
		matrix stay_alive <- (m_living = 2.0) + (m_living = 3.0);
		matrix new_born <- (m_living = 3.0);
		
		matrix next_state <- ifelse(m_alive, stay_alive, new_born);
		
		do set_values(life_cell, "alive_float", next_state);
	}
	//Write the description of the model in the console
	action description() {
		write 'Description:'  ;
		write 'The Game of Life is a cellular automaton devised by the British mathematician John Horton Conway in 1970.';
		write 'It is the best-known example of a cellular automaton.';
		write 'The game is a zero-player game, meaning that its evolution is determined by its initial state, requiring no further input from humans.';
		write 'One interacts with the Game of Life by creating an initial configuration and observing how it evolves.';
		write 'The universe of the Game of Life is an infinite two-dimensional orthogonal grid of square cells, each of which is in one of two possible states, live or dead.';
		write 'Every cell interacts with its eight neighbors, which are the cells that are directly horizontally, vertically, or diagonally adjacent.';
		write 'At each step in time, the following transitions occur:';
		write '\t 1.Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.';
		write '\t 2.Any live cell with more than three live neighbours dies, as if by overcrowding.';
		write '\t 3.Any live cell with two or three live neighbours lives on to the next generation.';
		write '\t 4.Any dead cell with exactly three live neighbours becomes a live cell.';
		write 'The initial pattern constitutes the seed of the system.';
		write 'The first generation is created by applying the above rules simultaneously to every cell in the seed; births and deaths happen simultaneously,';
		write 'and the discrete moment at which this happens is sometimes called a tick (in other words, each generation is a pure function of the one before).';
		write 'The rules continue to be applied repeatedly to create further generations.';
	}

}

//Grid species representing a cellular automata
grid life_cell width: environment_width height: environment_height neighbors: 8  use_individual_shapes: false use_regular_agents: false 
use_neighbors_cache: false parallel: parallel{
	float alive_float <- (rnd(100)) < density ? 1.0 : 0.0;
	bool alive -> alive_float > 0.0;
	
	rgb color <- alive ? livingcolor : deadcolor;
}


experiment "Game of Life" type: gui {
	parameter "Run in parallel " var: parallel category: 'Board';
	parameter 'Width:' var: environment_width category: 'Board';
	parameter 'Height:' var: environment_height category: 'Board';
	parameter 'Torus?:' var: torus_environment category: 'Board';
	parameter 'Initial density of live cells:' var: density category: 'Cells';
	parameter 'Numbers of live neighbours required to stay alive:' var: living_conditions category: 'Cells';
	parameter 'Numbers of live neighbours required to become alive:' var: birth_conditions category: 'Cells';
	parameter 'Color of live cells:' var: livingcolor category: 'Colors';
	parameter 'Color of dying cells:' var: dyingcolor category: 'Colors';
	parameter 'Color of emerging cells:' var: emergingcolor category: 'Colors';
	parameter 'Color of dead cells:' var: deadcolor category: 'Colors';
	output {
		display Life type: 3d axes:false antialias:false{
			grid life_cell;
		}

	}

}
