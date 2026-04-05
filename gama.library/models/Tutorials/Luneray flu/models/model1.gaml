/**
* Name: Luneray's Flu - Step 1
* Author: Patrick Taillandier
* Description: First step of the Luneray's Flu tutorial. Creates a basic agent-based epidemic model set in
*   the town of Luneray, France. People agents move randomly in a 1500m × 1500m space. Infected agents can
*   transmit the flu to susceptible neighbors within a given distance. This first model introduces the basic
*   GAML concepts: species, global, experiment, the 'moving' skill, and a simple spatial infection mechanism.
* Tags: species, global, experiment, skill, moving, tutorial, epidemic, flu
*/

model model1

global {
	int nb_people <- 2147;
	int nb_infected_init <- 5;
	float step <- 5 #mn;
	geometry shape<-square(1500 #m);
	
	init{
		create people number:nb_people;
		ask nb_infected_init among people {
			is_infected <- true;
		}
	}
}

species people skills:[moving]{		
	float speed <- (2 + rnd(3)) #km/#h;
	bool is_infected <- false;
	
	reflex move{
		do wander();
	}

	reflex infect when: is_infected{
		ask people at_distance 10 #m {
			if flip(0.05) {
				is_infected <- true;
			}
		}
	}
	
	aspect circle {
		draw circle(10) color:is_infected ? #red : #green;
	}
}

experiment main type: gui {
	parameter "Nb people infected at init" var: nb_infected_init min: 1 max: 2147;

	output {
		display map {
			species people aspect:circle;	
		}
	}
}
