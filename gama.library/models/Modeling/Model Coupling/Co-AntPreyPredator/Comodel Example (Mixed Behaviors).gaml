/**
* Name: modelCouplerAntPP
* Purpose: A co-model coupling the Ant Foraging logic (movement) with Prey-Predator logic (life cycle).
* Concept: Ants act as the "physical body" (movement), while Prey agents act as the "biological state" (survival).
* Author: Lucas Grosjean
*/

model modelCouplerAntPP

// Import the sub-models as namespaces
import "../Co-PreyPredator/Prey Predator.gaml" as PP
import "../../../Toy Models/Ants (Foraging and Sorting)/models/Ant Foraging.gaml" as ANT

global
{
	/** Global Environment Settings **/
	geometry shape <- square(100);
	int number_of_ants <- 20;
	
	// Lists to keep track of agents across both sub-models
	list<ant> theAnts;
	list<prey> thePreys;
	
	init
	{
		// 1. Initialize the Ant Model
		create ANT.comodel_exp_ant
		{
			gridsize <- 100;
			ants_number <- number_of_ants;
			theAnts <- list(ant);	
		} 	
		
		// 2. Initialize the Prey-Predator Model
		create PP.comodel_exp_pp
		{
			shape <- square(100);
			preyinit <- number_of_ants; // Ensure 1:1 ratio with ants
			predatorinit <- 2;
		}
	}
	
	/** Main Orchestration Reflex: Runs every simulation step **/
	reflex synchronize_models
	{
		list<int> ant_to_delete; // Tracks which ants should die this step
		list<prey> prey_before;  // Snapshot of prey before predation occurs
		
		// --- PHASE 1: Update Ant Movement ---
		ask ANT.comodel_exp_ant[0].simulation
		{
			do _step_(); // Advance the Ant simulation one tick
			theAnts <- list(ant); // Refresh the local list
		}
		
		// --- PHASE 2: Handle Predation and Mortality ---
		ask PP.comodel_exp_pp[0].simulation
		{
			prey_before <- list(prey); // Store state before stepping
			
			do _step_(); // Advance the PP simulation (predators might eat prey here)
			
			thePreys <- list(prey);
			
			// Compare lists: if a prey is gone, the corresponding ant must die
			if(prey_before != thePreys){
				loop i from: 0 to: length(prey_before) - 1
				{
					if(dead(prey_before at i)) // Check if agent was removed from world
					{
						if(!dead(theAnts at i)) // Match by index
						{
							ant_to_delete << i;	// Mark index for deletion					
						}
					}
				}
			}
		}
		
		// --- PHASE 3: Clean up "Dead" Ants ---
		ask ANT.comodel_exp_ant[0].simulation
		{
			loop index_to_delete over: ant_to_delete
			{
				(ant at index_to_delete).die();
			}	
			theAnts <- list(ant); // Update list after deaths
		}
		
		// --- PHASE 4: Sync Positions ---
		// Forces the 'Prey' agents to be at the exact coordinates of the 'Ants'
		ask PP.comodel_exp_pp[0].simulation
		{
			loop tmp over: theAnts
			{
				loop tmp_pp over: prey
				{
					// Match agents by their index and teleport prey to ant position
					if(tmp_pp.index = tmp.index)
					{
						tmp_pp.location <- tmp.location;
					}
				}
			}
		}
	}
}

/** Visualization **/
experiment coupling_ant_pp
{
	output
	{
		display "Comodel display"
		{
			// Render the grid and pheromones from the Ant Model
			agents "ant_grid" value: ANT.comodel_exp_ant[0].simulation.ant_grid transparency: 0.7;	
			
			// Render the Ant agents using their specific icons
			agents "ants" value: ANT.comodel_exp_ant[0].simulation.ant aspect:icon;		
				
			// Overlay the Prey and Predators from the PP Model
			agents "agentprey" value: PP.comodel_exp_pp[0].simulation.prey;
			agents "agentpredator" value: PP.comodel_exp_pp[0].simulation.predator;
		}
	}
}