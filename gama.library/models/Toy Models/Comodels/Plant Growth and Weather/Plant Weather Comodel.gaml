/**
* Name: Plant Growth and Weather Comodel
* Author: Benoit Gaudou, Damien Philippon, Lucas Grosjean
* Description: Second comodel in the Plant Growth and Weather series, adding bidirectional coupling between
*   the Weather and Plant Growth sub-models. Rain water from the Weather model now fills the water reserve
*   of plants in the Plant Growth model, creating an ecological feedback loop: weather affects plant growth,
*   and plant density can influence local weather cells. This model shows how to create interactions between
*   agents of two different coupled sub-models by accessing them through the comodel interface.
* Tags: comodel, weather, plant, ecology, coupling, interaction, water, growth
*/

model coModel

import "Experiment_comodel/weather_comodel.experiment" as weather
import "Experiment_comodel/plant_comodel.experiment" as plantGrow


global {
	
	weather weather_simu ;
	plantGrow plantGrow_simu;
		
	init {
		create weather.weathercomodel(grid_size:20,write_in_console_step:false);
		weather_simu <- first(weather.weathercomodel).simulation; 
		
		create plantGrow.plantcomodel(grid_size:40);
		plantGrow_simu <- first(plantGrow.plantcomodel).simulation; 		
	}

	reflex simulate_micro_models_weather {
		ask weather_simu
		{
			self._step_();
		}	
	}
	
	reflex coupling {
		
		ask weather_simu.plotWeather {
			list<plotGrow> overlapped_plots <- plantGrow_simu.plotGrow where (each.shape.location overlaps self);
						
			ask overlapped_plots {
				available_water <- available_water + myself.rain;
			}
		}
	}
	
	reflex simulate_micro_models_plantGrow {
		ask plantGrow_simu
		{
			self._step_();
		}		
	}
}

experiment "CoModel" type: gui {
	output {
		display w type:2d antialias:false{
			agents "weather" value: weather_simu.plotWeather ;
		}
		display pG type:2d antialias:false{
			agents "plantGrowth" value: plantGrow_simu.plotGrow ;
		}		
		
		display data  type: 2d  {
			chart "rain" type: series {
				data "rainfall" value: sum(weather_simu.plotWeather accumulate (each.rain));
			}
		}
		
	}
}
