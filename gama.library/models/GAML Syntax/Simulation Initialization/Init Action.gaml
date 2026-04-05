/**
* Name: Init Action
* Author: Alexis Drogoul
* Description: Shows how to use the special '_init_' callback action of an experiment to configure a simulation
*   with specific parameters before it begins, without exposing those parameters to the user interface. The _init_
*   action is called once when an experiment is initialized and can be used to set global variables, choose
*   parameter values programmatically, or perform any setup that would otherwise require user intervention.
*   This is useful for programmatic experiments, batch runs, or when the initial configuration is computed
*   rather than user-defined.
* Tags: init, experiment, parameter, simulation, initialization, callback
*/

model InitAction

global {
	int agent_number <- 100;
	rgb agent_color <- #red;
	bool 2d <- false;
	
	init {
		create my_agents number: agent_number;
	}
}

species my_agents {
	
	int elevation <- rnd(30);
	
	
	aspect default {
		if (2d){
			draw square(5) color: agent_color;			
		}
		else {
			draw sphere(5) color: agent_color at:{location.x, location.y, elevation};			
		}
	}
	
}

experiment InitAction type: gui {
	
	action _init_ (){
		map<string, unknown> params <- user_input_dialog([enter("Number of agents",100), enter("Color",#red), enter("2D",true)]);
		create InitAction_model with: (agent_number:int(params["Number of agents"]), agent_color:rgb(params["Color"]), 2d:bool(params["2D"]));
	}
	
	output {
		display Simple type:3d{
			species my_agents aspect:default;			 
		}
	}
	
}

