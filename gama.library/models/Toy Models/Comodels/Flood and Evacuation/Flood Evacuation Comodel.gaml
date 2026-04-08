/**
* Name: Flood and Evacuation Comodel
* Author: Huynh Quang Nghi, Lucas Grosjean
* Description: A comodel coupling a flood simulation with an evacuation model to study their interactions.
*   The flood model (based on the Hydrological Model) raises water levels from an upstream source, while the
*   evacuation model (based on City Escape) has people fleeing toward exits. The coupling checks which people
*   are reached by water and removes them from the simulation, creating a race between the rising flood and
*   the escaping population. This model illustrates how independently developed GAMA models can be coupled
*   to study emergent cross-domain dynamics.
* Tags: comodel, flood, evacuation, coupling, water, emergency, gis
*/
model flood_evacuation_comodeling

import "Experiment_comodel/hydro_comodel.experiment" as Flooding
import "Experiment_comodel/move_comodel.experiment" as Evacuation
 


global
{
	//set the bound of the environment
	geometry shape <- envelope(file("../../../Toy Models/Flood Simulation/includes/mnt50.asc"));
	geometry the_free_shape<-copy(shape);
	//counter for casualties
	int casualty <- 0;
	
	list<point> offset <- [{ 50, 1700 }, { 800, 3400 }, { 2900, 0 }, { 4200, 2900 }, { 5100, 1300 }];
	list<point> exits <- [{250, 1600 }, { 400, 4400 }, { 4100, 1900 }, { 6100, 2900 }, { 5700, 900 }];
	
	init
	{
		//create experiment from micro-model myFlood with corresponding parameters
		create Flooding.hydrocomodel;
	
		//create the Evacuation micro-model's experiment
		create Evacuation.movecomodel number:length(offset);// with:(nb_people:1);
		ask Evacuation.movecomodel
		{
			centroid <- myself.offset[int(self)];
			target_point <- myself.exits[int(self)];
			//transform the environment and the agents to new location (near the river)
			self.transform_environment();
			loop t over: list(building)
			{
				the_free_shape <- myself.the_free_shape - (t.shape+ people_size);
			}
			
			free_space<-copy(myself.the_free_shape);			
			free_space <- free_space simplification(1.0);
		}

	}

	reflex doing_cosimulation
	{
		//do a step of Flooding
		ask Flooding.hydrocomodel collect each.simulation
		{
			self._step_();
		}

		//evacuate people
		ask Evacuation.movecomodel collect each.simulation
		{
			//depending on the real plan of evacuation, we can test the speed of the evacuation with the flooding speed by doing more or less simulation steps 
			self._step_();
		}
		
		//loop over the population
		loop thePeople over: Evacuation.movecomodel  accumulate each.get_people()
		{
			//get the cell at people's location 
			cell theWater <- cell(first(Flooding.hydrocomodel).get_cell_at(thePeople));
			//if the water level is higher than 8 meters and the cell overlaps people, kill the people
			if (theWater.grid_value > 8.0)
			{
				ask thePeople
				{
					self.die();
				}
				//increase the counting variable
				casualty <- casualty + 1;
			}

		}

	}

}

experiment simple type: gui
{
	output synchronized: true
	{
		display "Comodel Display"  type:3d
		{
			agents "building" value: Evacuation.movecomodel  accumulate each.get_building();
			agents "people" value:  Evacuation.movecomodel  accumulate each.get_people();
			graphics "exits" refresh:false{
				loop e over: exits
				{
					draw sphere(100) at: e color: # green;
				}

			}
			agents "cell" value: first(Flooding.hydrocomodel).get_cell();
			agents "cell" value: first(Flooding.hydrocomodel).get_buildings()  aspect: geometry;
			agents "dyke" value: first(Flooding.hydrocomodel).get_dyke() aspect: geometry ;
			graphics 'CasualtyView' 
			{
				draw ('Casualty: ' + casualty +"/"+sum(Evacuation.movecomodel  accumulate (each.simulation.nb_people))) at: { 1000, 5200 } font: font("Arial", 24, # bold) color: # red;
			}
		}

	}

}
