/**
* Name: Lifewithtime
* Description: A game of life in 3D with time as the z axis. Inspired by: https://www.youtube.com/shorts/5BY9N_Rp244 and https://www.instagram.com/reel/C2hoRnFsmQW/?igsh=MWluZzJqdDV1cGY1bQ==
* Author: Baptiste Lesquoy
* Tags: Life, 3D, Cellular Automaton, Conway
*/

model Lifewithtime

global  {
	//Size of the environment
	int environment_width <- 40 min: 3 ;
	int environment_height <- 40 min: 3 ;
	int history_size <- 100;
	
	//Density 
	int density <- 25 min: 1 max: 99;
	//Conditions to live
	list<int> living_conditions <- [2, 3];
	//Conditions to birth
	list<int> birth_conditions <- [3];
	
	// cell states
	int cell_state_living <- 1 const: true;
	int cell_state_dying <- -1 const: true;
	int cell_state_emerging <- 2 const: true;
	int cell_state_dead <- 0 const: true;

	
	//Color for living cells
	rgb livingcolor <- #white;
	//Color for dead cells
	rgb deadcolor <- rgb(0,0,0,0.0); // Fully transparent to see the other cells more easily
	//Shape of the environment
	geometry shape <- rectangle(environment_width, environment_height);
	
	list<cell> current_grid;
	list<list<cell>> history;
	
	//Initialization of the main grid with proper density and history filled with blank
	init {
		loop i from: 0 to: environment_width * environment_height -1  {
			int x <- i mod environment_width;
			int y <- int(i / environment_width);
			current_grid <+ cell(state: flip(density/100) ? cell_state_living : cell_state_dead, x: x, y: y, location: {x+0.5,y+0.5,0});
		}
		//Fill in neighbours
		loop i from:0 to:environment_width * environment_height -1 {
			// Rejecting out of bound by giving negative indices and filtering later
			int left <- i mod environment_width > 0 ? i - 1 : -1;
			int right <- i mod environment_width < environment_width-1 ? i + 1 : -1;
			int top <- i - environment_width;
			int bottom <- i + environment_width;
			int top_left <- i mod environment_width > 0 ? i - 1 - environment_width : -1;
			int top_right <- i mod environment_width < environment_width-1 ? i + 1 - environment_width : -1;
			int bottom_left <- i mod environment_width > 0 ? i - 1 + environment_width : -1;
			int bottom_right <- i mod environment_width < environment_width-1 ? i + 1 + environment_width : -1;
			current_grid[i].neighbours <- [left, right, top, bottom, bottom_left, bottom_right, top_left, top_right] where (each >= 0 and each < environment_width * environment_height);
		}
		history <- list_with(history_size, []);
		do update_locations();
	}
	
	action update_locations(){
		loop i from:0 to:history_size -1 {
			loop c over:history[i] {
				c.location <- c.location + {0,0,-1};
			}
		}
	}
	
	//Ask at each life_cell to evolve and update
	reflex generation {
		
		// updating the history
		remove from:history index:length(history) -1;
		add (current_grid where each.is_alive() collect copy(each)) to:history at:0; // we don't need to represent dead cells in history as they are transparent
		
		loop c over: current_grid {
			c.evolve();
		}
		loop c over: current_grid{
			c.update();
		}
		
		do update_locations();
	}
	
}

class cell {
	
	point location;
	geometry shape <- cube(1);
	bool state <- false; // true is alive, false is dead;
	bool new_state;
	list<int> neighbours;
	rgb color <- is_alive() ? livingcolor : deadcolor;
	int x;
	int y;
	
	bool is_alive(){
		return state;
	}
	
	//Action to evolve the cell considering its neighbours
	action evolve() {
		//Count the number of living neighbours of the cells
		
		int living <- neighbours count current_grid[each].is_alive();
		
		if is_alive() {
			//If the number of living respect the conditions, the cell is still alive
			new_state <- living in living_conditions;
		} else {
			//If the number of living meets the conditions, the cell go to born
			new_state <- living in birth_conditions ;
		}
		color <- new_state ? livingcolor : deadcolor;

	}
	//Action to update the new state of the cell
	action update() {
		state <- new_state;
	}
}

experiment "Game of Life" type: gui record: true {
	parameter 'Width:' var: environment_width category: 'Board';
	parameter 'Height:' var: environment_height category: 'Board';
	parameter 'Initial density of live cells:' var: density category: 'Cells';
	parameter 'History size' var:history_size;
	parameter 'Numbers of live neighbours required to stay alive:' var: living_conditions category: 'Cells';
	parameter 'Numbers of live neighbours required to become alive:' var: birth_conditions category: 'Cells';
	parameter 'Color of live cells:' var: livingcolor category: 'Colors';
	parameter 'Color of dead cells:' var: deadcolor category: 'Colors';
	output synchronized:true {
		display Life type: 3d axes:false antialias:false background:#grey{
		camera 'default' location: {38.4894,86.0603,21.9639} target: {25.3061,38.523,0.0};			
		graphics  {
				draw world.shape color:#black wireframe:true;
				loop c over:current_grid + accumulate(history, each){
					draw c.shape at:c.location color:c.color ;
				}
			}
		}
	}

}
