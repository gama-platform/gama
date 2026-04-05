/**
* Name: Create and Kill Sub-Models
* Author: Huynh Quang Nghi
* Description: A minimal comodel syntax demonstration showing how to instantiate and destroy micro-models
*   at runtime without using coupling adapters. Sub-models are created with 'create' and can be killed
*   with 'ask' or during a reflex. This direct approach (without adapters) is simpler but provides less
*   encapsulation than the adapter pattern. Useful as the simplest possible comodeling example to understand
*   the core 'import ... as' mechanism.
* Tags: comodel, create, kill, sub_model, import, syntax, flies, mosquitos
*/
model simple_comodeling_example

import "Adapters/Flies Adapter.gaml" as Flies
import "Adapters/Mosquitos Adapter.gaml" as Mosquitos


global
{
	init
	{
	//micro_model must be instantiated by create statement. We create an experiment inside the micro-model and the simulation will be created implicitly (1 experiment have only 1 simulation).
		create Flies.Simple  number: 2;
		create Mosquitos.Generic;
	}

	reflex simulate_micro_models
	{

	//tell the first experiment of micro_model_1 do 1 step;
		ask first(Flies.Simple).simulation
		{
			do _step_;
		}

		//tell all experiments of micro_model_1 do 1 step;
		ask (Flies.Simple collect each.simulation)
		{
			do _step_;
		}

		//tell all experiments of micro_model_2 do 1 step;
		ask (Mosquitos.Generic collect each.simulation)
		{
			do _step_;
		}
		
		//ask  simulation of micro_model to kill all agents every 100 cycles and recreate them
		if(cycle mod 100 = 0){			
			ask  (Mosquitos.Generic collect each.simulation){
				ask Mosquito{				
					do die;
				}
			}
			ask  (Mosquitos.Generic collect each.simulation){
				seed<-float(rnd(100));
				do _init_;
			}
		}
	}
}

experiment main type: gui
{
}