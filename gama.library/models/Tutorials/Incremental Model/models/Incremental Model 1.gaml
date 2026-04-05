/**
* Name: Incremental Model Tutorial - Step 01 - Simple SI Model
* Author: Gama Development Team
* Description: First step of the Incremental Model tutorial. Creates a simple SI (Susceptible-Infected) model
*   set in a city, where people agents move randomly. Infected people can transmit the disease to susceptible
*   neighbors within a given distance. This first step introduces species declaration, the 'moving' skill,
*   basic reflex-based behavior, and a simple spatial infection rule. It forms the foundation for all
*   subsequent steps of the tutorial.
* Tags: tutorial, gis, SI, epidemic, moving, skill, species, infection
*/
model SI_city

global {
	int nb_people <- 500;
    float agent_speed <- 5.0 #km/#h;	
	float infection_distance <- 2.0 #m;
	float proba_infection <- 0.05;
	int nb_infected_init <- 5;
	float step <- 1 #minutes;
	geometry shape <- envelope(square(500 #m));

	init {
		create people number: nb_people {
			speed <- agent_speed;
		}

		ask nb_infected_init among people {
			is_infected <- true;
		}

	}

}

species people skills: [moving] {
	bool is_infected <- false;

	reflex move {
		do wander();
	}

	reflex infect when: is_infected {
		ask people at_distance infection_distance {
			if (flip(proba_infection)) {
				is_infected <- true;
			}
		}
	}

	aspect default {
		draw circle(5) color: is_infected ? #red : #green;
	}
}

experiment main_experiment type: gui {
	parameter "Infection distance" var: infection_distance;
	parameter "Proba infection" var: proba_infection min: 0.0 max: 1.0;
	parameter "Nb people infected at init" var: nb_infected_init;
	
	output {
		display map {
			species people; // 'default' aspect is used automatically			
		}
	}
}