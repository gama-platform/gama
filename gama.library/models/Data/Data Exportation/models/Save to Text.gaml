/**
* Name: Save to Text
* Author: Patrick Taillandier
* Description: Shows how to save agent data to a plain text file in GAMA. Text files are the simplest output
*   format — each 'save' call appends a line to the file. While less structured than CSV or JSON, text output
*   is useful for logs, human-readable reports, or custom formats. The model demonstrates creating a population
*   of bug agents and progressively writing their attributes to a text file during the simulation run.
* Tags: save_file, txt, text, export, log, agent
*/


model SavetoText
 
global {
	init {
		//Create the agents that will be saved in the text file.
		create bug number: 50;
	}
	
	//Reflex saving the agents every 10 steps. 
	reflex save_data when: every(10#cycles){
		//save the following text into the given text file. Note that each time the save statement is used, a new line is added at the end of the file.
		save ("cycle:" + cycle + ", mean size: " + mean(bug collect each.size)) to: "../results/data.txt" rewrite: false;
	}
	//Reflex that will pause the simulation when the number of cycles reach 100.
	reflex end_simulation when: cycle = 100 {
		do pause(); 
	}
}

//Species bug that will be saved using the skill moving
species bug skills:[moving]{
	float size <- 1.0 + rnd(4) min: 1.0 max: 5.0;
	float speed <- 1.0 + rnd(4.0);
	
	//At each step, the size of the agent is update according to the number of bugs close to the agent
	reflex update_size {
		int nb_neigh <- length(bug at_distance 20.0);
		if (nb_neigh > 5) {
			size <- size + 1;
		} else {
			size <- size - 1;
		}
	} 	
	
	//Make the agent move randomly 
	reflex move {
		do wander();
	}
	
	aspect default {
		draw circle(size) color: #red;
	}
}

experiment main type: gui {
	output {
		display map {
			species bug;
		}
	}
}
