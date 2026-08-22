/**
* Name: CellularAutomatonGenerator
* Generates one dimensional cellular automata based on rules specified by the users
* Author: Baptiste Lesquoy
* Tags: Cellular Automaton, Wolfram
*/

model CellularAutomatonGenerator

global{
	int width <- 501;
	int history_size <- 100;
	list<list<int>> neighbours_conditions <- [[1,1,1],[1,1,0],[1,0,1],[1,0,0],[0,1,1],[0,1,0],[0,0,1],[0,0,0]];
	list<int> results <- [0,1,1,0,1,1,1,0];
	int rule_number <- 110 on_change:update_rule min:0 max:255;// results_to_rule_number();
	
	rgb dead_color <- #black;
	rgb live_color <- #lightblue;
	
	string border_strategy <- "assume_empty" among:["assume_empty", "torus", "copy_state"];
	
	init {
		// By default we only set the central cell alive
		automaton({shape.width/2, 0.01}).alive <- true;
		
		point init_rule_pos <- {4,-3};
		loop i from:0 to:7{
			create rule_button{
				digit <- i;
				location <- init_rule_pos;
				activated <- results[i] > 0;
			}
			init_rule_pos <- init_rule_pos + {10,0};
		}
	}
	
	int results_to_rule_number(){
		return int(sum(enumerate(results) collect (2^(8 - each.key)*int(each.value))));
	}
	

	reflex pausing when: cycle=history_size-2{
		do pause();
	}
	reflex run_automaton {
		list<list<int>> buffer <- range(width-2) collect (i: slice(list(automaton), cycle*width + i , cycle*width + i + 2) collect (a: a.alive ? 1 : 0));
		loop i from:0 to:width-3{
			int idx <- neighbours_conditions index_of buffer[i];
			automaton[i+1, cycle+1].alive <- results[idx] > 0;
		}
	}
	
	action toggle_state() {
		if #user_location overlaps shape {
			automaton cell <- automaton(#user_location);
			if cell.grid_y = cycle { // If we click on a cell from the active row we toggle it
				cell.alive <- not cell.alive;
			}
			ask experiment{
				do update_outputs();
			}
		}
	}
	
	// The rule number changed, we need to reprocess results and update the buttons
	action update_rule(){
		int nb <- rule_number;
		results <- list_with(8, 0); // reset the results list
		loop idx from:7 to:0 {
			int pow <- int(2^idx);
			bool found <- nb >= pow;
			if found {
				nb <- nb - pow;
			}
			results[idx] <- found ? 1 : 0;
			first(rule_button where (each.digit = idx)).activated <- found;
		}
		ask experiment{
			do update_outputs();
		}
	}
}

grid automaton width:width height:history_size parallel:true use_individual_shapes:false use_regular_agents:false{
	
	bool alive <- false on_change:{color <- getColor();};
	
	rgb color <- getColor();
	
	rgb getColor(){
		return alive ? live_color : dead_color;
	}
}

species rule_button{
	int digit;
	bool activated;
	geometry shape <- rectangle(2,2);
	
	action toggle(){
		activated <- ! activated;
	}
	
	aspect default {
		float idx <- -2.0;
		int i <- 0;
		loop rules over:neighbours_conditions[digit]{
			int state <- neighbours_conditions[digit][i];
			draw rectangle(2,2) at:location + {idx,-2.5} color: state = 0 ? dead_color : live_color border:#grey;
			idx <- idx + 2;
			i <- i + 1;
		}
		draw shape at:location color:activated ? live_color : dead_color border:#black;
	}
}


experiment exp {
	parameter "Rule number" var:rule_number slider:false;
	parameter "Automaton's length" var:width;
	parameter "History size" var:history_size;

	output synchronized:true {
		display main type:3d antialias:false axes:false{
			camera 'default' location: {56.1329,46.7972,129.3529} target: {56.1329,46.7949,0.0};
			species rule_button;
			graphics "Rules" {				
				draw "Rule number: " + rule_number at: {80, -2.5} color:#black font:font("Helvetica", 22, #plain);
			}
			grid automaton;
			event #mouse_down {ask world {do toggle_state();}}
		}
	}
	
}


