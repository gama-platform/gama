/**
* Name: modelCouplerAntPP
* Purpose: A co-model coupling the Ant Foraging logic (movement) with Prey-Predator logic (life cycle).
* Concept: Ants act as the "physical body" (movement), while Prey agents act as the "biological state" (survival).
* Author: Lucas Grosjean
*/
 
model modelCouplerAntPP

// Import the sub-models as namespaces
import "pp_comodel.gaml" as PP
import "ant_comodel.gaml" as ANT


global
{
	/** Global Environment Settings **/
	geometry shape <- square(100);
	int number_of_ants <- 20;
	
	// Lists to keep track of agents across both sub-models
	list<ant> theAnts;
	list<prey> thePreys;
	map<ant, prey> prey_by_ant;
	
	init
	{
		// Pass globals as creation arguments, before the child simulation initializes.
		create ANT.antcomodel (gridsize: 100, ants_number: number_of_ants);
		// The simulation and its populations are available after create returns.
		theAnts <- list(ANT.antcomodel[0].simulation.ant);
		
		// 2. Initialize the Prey-Predator Model
		create PP.ppcomodel (shape: square(100), preyinit: number_of_ants, predatorinit: 2);
		thePreys <- list(PP.ppcomodel[0].simulation.prey);
		if (length(theAnts) != number_of_ants or length(thePreys) != number_of_ants)
		{
			error "Both co-models must initialize with number_of_ants agents.";
		}
		// Pair agent references once; population positions change when agents die.
		prey_by_ant <- create_map(theAnts, thePreys);
	}
	
	/** Main Orchestration Reflex: Runs every simulation step **/
	reflex synchronize_models
	{
		list<ant> ants_to_delete;
		
		// --- PHASE 1: Update Ant Movement ---
		ask ANT.antcomodel[0].simulation
		{
			do _step_(); // Advance the Ant simulation one tick
		}
		
		// --- PHASE 2: Handle Predation and Mortality ---
		ask PP.ppcomodel[0].simulation
		{
			do _step_(); // Advance the PP simulation (predators might eat prey here)
			
			myself.thePreys <- list(prey);
			
			loop body over: myself.theAnts
			{
				if (dead(myself.prey_by_ant[body]))
				{
					ants_to_delete << body;
				}
			}
		}
		
		// --- PHASE 3: Clean up "Dead" Ants ---
		ask ants_to_delete
		{
			do die();
		}
		theAnts <- theAnts where (!dead(each));
		
		// --- PHASE 4: Sync Positions ---
		// Forces the 'Prey' agents to be at the exact coordinates of the 'Ants'
		ask PP.ppcomodel[0].simulation
		{
			loop body over: myself.theAnts
			{
				prey partner <- myself.prey_by_ant[body];
				partner.location <- body.location;
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
			agents "ant_grid" value: ANT.antcomodel[0].simulation.ant_grid transparency: 0.7;	
			
			// Render the Ant agents using their specific icons
			agents "ants" value: ANT.antcomodel[0].simulation.ant aspect:icon;		
				
			// Overlay the Prey and Predators from the PP Model
			agents "agentprey" value: PP.ppcomodel[0].simulation.prey;
			agents "agentpredator" value: PP.ppcomodel[0].simulation.predator;
		}
	}
}