/**
* Name: SIR with FSM
* Shows how to code a simple agent-based SIR model using the FSM control architecture
* Author: Alexis Drogoul
* Tags: SIR, FSM, control
*/

model SIRWithFSM

global {
	
	int number_of_people <- 1000;
	int number_of_infected <- 2;
	float contact_distance <- 1#m;
	int recovery_time <- 80;
	

	init {
		create people number: number_of_people;
		ask number_of_infected among people {
			state <- "I";
		}
	}
	
	reflex should_stop when: people count (each.state = 'I') = 0 {
		do pause;
	}
}

species people skills: [moving] control: fsm {
	
	int time_of_infection;
	
	state S initial: true {} //Nothing to do
		
	state R {} //Nothing to do
	
	state I {
		enter {
			time_of_infection <- world.cycle;
		}
		ask people at_distance contact_distance where (each.state="S") {
				state <- "I";
		}
		transition to: R when: world.cycle - time_of_infection >= recovery_time;
	}
	
	reflex moving {
		do wander amplitude: 30.0;
	}
	
}

experiment FirstModel type: gui {
	map<string, rgb> colors <- ["S"::#green,"I"::#red,"R"::#blue];
	parameter "Recovery time " var: recovery_time min: 10 max: 100;
	parameter "Number of people" var: number_of_people min: 10 max: 2000;
	parameter "Number of initially infected agents" var: number_of_infected min: 1 max: number_of_people;
	parameter "Contact radius" var: contact_distance min: 1.0 max: 10.0;
	
	output {
		display "My Display" {
			species people {
				draw circle(1) color: (state = "I")? #red : ((state="R") ? #blue : #green);
			}
		}
		display "Chart" type:2d {
			chart "Infected people" {
				loop s over: ["S","I","R"] {
					data s value: people count (each.state = s) color: colors[s];
				}

			}
		}
	}
}
