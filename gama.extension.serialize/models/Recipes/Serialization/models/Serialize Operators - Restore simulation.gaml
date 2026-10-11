/**
* Name: Serialize Operators - Restore Simulation
* Author: Benoit Gaudou
* Description: Demonstrates restoring a simulation from a binary file previously saved by 'Serialize
*   Operators - Save simulation in file and serialize'. Uses the 'serialize' and 'deserialize' operators
*   to reconstruct the complete simulation state (global variables, agent populations, and their attribute
*   values) from a serialized binary snapshot. Run the save model first to generate the file.
* Tags: serialization, restore, load_file, binary, snapshot, simulation, deserialize
*/

model Serialization

global {
	int toot <- 0;
	string s <- "test";
	
	init {
		create people number: 1;
		create other number: 2;
		write "Run the model.";
		write "Every 4 steps, the simulation is restored to its initial step.";
		write "You will thus observe the red agent going to the right side of the display and, every 4 steps, moving back to its initial location.";
	}
	
	reflex t {
		write toot;
		toot <- toot +10;
	}
}

species people {
	int t;
	list<int> lo <- [1,2,3];
	
	reflex move {
		float r <- rnd(5.0);
		write "" + cycle + " - "  + r  ;
		location <- {location.x + r, location.y};
	}
	
	aspect default {
		draw circle(1) color: #red;
	}
}

species other {
	aspect default {
		draw circle(1) color: #blue;
	}
}

experiment "Repeated Simulations" type: gui {

	string saved_step <- "";

	init {
		saved_step <- serialize(self.simulation);
	}
	
	reflex restore when: (cycle mod 4) = 0 {
		write "================ begin restore " + self + " - " + cycle;
		restore simulation from: saved_step;
		write "================ end restore " + self + " - " + cycle;			
	}

	output {
		layout #split;
		display d {
			species people aspect: default;
			species other aspect: default;
		}	
		
		display c  type: 2d {
			chart "t" {
				data "location" value: first(people).location.x;
			}
		}	
	}
}
