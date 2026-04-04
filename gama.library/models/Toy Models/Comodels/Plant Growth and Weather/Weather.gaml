/**
* Name: Weather
* Author: Benoit Gaudou
* Description: A simple weather model designed to be used as a sub-model in comodeling scenarios. Weather
*   cells cover a grid and each cell can generate rainfall at each simulation step based on random probability.
*   The amount of rain produced is stored per cell and can be read by a parent comodel or by a coupled
*   Plant Growth model. When run standalone the model simply prints step information to the console.
* Tags: weather, rain, grid, ecology, comodel, sub_model
*/

model weather

global {
	float env_size <- 100 #m;
	geometry shape <- square(env_size);
	int grid_size <- 20;
	
	bool write_in_console_step <- false;
	
	init {
		write "[WEATHER model] Initialization";
	}
	
	reflex write_one_step {
		if(write_in_console_step) {
			write "[WEATHER model] One step";
		}
	}
}

grid plotWeather height: grid_size width: grid_size {
	float rain <- grid_x*grid_y*rnd(10)/10 update: grid_x*grid_y*rnd(10)/10;
	rgb color <- rgb(0,0,rain) update: rgb(0,0,rain);
}

experiment "Weather" type: gui {
	output {
		display d type:2d antialias:false{
			grid plotWeather border: #black;
		}
	}
}

experiment "Weather Co-Modeling" type:gui {
	parameter "check execution step" var: write_in_console_step <- true;
}