/**
* Name: Simple Comodel Controler.gaml
* Author: Benoit Gaudou, Damien Philippon, Lucas Grosjean
* Description: First comodel in the Plant Growth and Weather series. Demonstrates the core comodeling mechanism:
*   instantiating a sub-model (Weather) inside a parent model, stepping it at each cycle, displaying its agents,
*   and computing indicators on them. The Weather model runs autonomously inside the parent, and the parent
*   can inspect its state. This is the simplest possible comodel configuration — one sub-model, no coupling
*   between sub-model agents and the parent.
* Tags: comodel, weather, plant, ecology, coupling, sub_model
*/

model coModel

import "Experiment_comodel/weather_comodel.experiment" as weather


global {
	
	weather weather_simu ;
		
	init {
		create weather.weathercomodel(grid_size:30,write_in_console_step:false);
		weather_simu <- first(weather.weathercomodel).simulation; 
	}

	reflex simulate_micro_models
	{
		ask weather_simu
		{
			self._step_();
		}
	}
}

experiment "CoModel" type: gui {
	output {
		display d type:2d antialias:false{
			agents "weather" value: weather_simu.plotWeather ;
		}
		
		display data  type: 2d {
			chart "rain" type: series {
				data "rainfall" value: sum(weather_simu.plotWeather accumulate (each.rain));
			}
		}
		
	}
}
