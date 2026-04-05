/**
* Name: Abort
* Author: Alexis Drogoul
* Description: Demonstrates the usage of the 'abort' and 'init' special actions in GAML. The 'init' action is called
*   once when a simulation is created, allowing to set up the initial state. The 'abort' action is called when the
*   simulation is about to be killed or destroyed, and can be used for cleanup operations such as killing dependent
*   agents or writing final outputs. This model shows both in action and prints messages to the console to trace
*   the order of execution.
* Tags: abort, init, simulation, lifecycle
*/
model Abort

global {

	init {
		write "Simulation executes 'init' and creates one agent of species a";
		create a;
		write "Simulation kills itself";
	}

	reflex {
		write "Simulation does nothing";
	}

	abort {
		write "Simulation executes 'abort' and kills the agents of species a";
		ask a {
			do die();
		}

	}

}

species a {

	init {
		write "Agent of species a executes init";
	}

	reflex {
		write "Neither does agent of species a";
		if (user_confirm("Close simulation", "Should we close the simulation ?")) {
			ask world {
				do die();
			}

		}

	}

	abort {
		write "Agent of species a executes abort";
	}

}

experiment "Run me" {
	abort {
		do tell("You are now leaving this experiment. Hope you enjoyed it ! ", false);
	}

}

