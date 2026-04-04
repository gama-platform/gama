/**
* Name: SIR with FSM Architecture
* Author: Alexis Drogoul
* Description: Shows how to implement a simple agent-based SIR (Susceptible-Infected-Recovered) epidemic model
*   using the FSM (Finite State Machine) control architecture instead of reflexes. Each person agent has three
*   states: susceptible, infected, and recovered. Transitions fire when an agent is within contact distance of
*   an infected neighbor, or when the recovery time has elapsed. The FSM architecture makes the individual
*   disease progression logic explicit and easy to understand, contrasting with the reflex-based approaches
*   shown in other SIR models in the library.
* Tags: SIR, FSM, control, epidemiology, infection, agent_based, state_machine
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
		display "My Display" type:2d{
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
