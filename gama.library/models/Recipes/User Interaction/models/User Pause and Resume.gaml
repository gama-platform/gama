/***
* Name: User Pause and Resume
* Author: Alexis Drogoul
* Description: Shows how to programmatically pause and resume a GAMA simulation from within the model using
*   the 'pause' and 'resume' simulation actions. A clickable image button switches between play and stop icons
*   based on the simulation's 'paused' state, staying synchronized with the toolbar controls. This pattern is useful for
*   building simulations with user-controlled execution flow, where pausing is tied to a specific game state
*   or user confirmation rather than just the toolbar button.
* Tags: simulation, pause, resume, gui, interaction, display, button
***/
model UserPauseandResume

global {
	geometry shape <- square(400);
	image_file play <- image_file("../images/play.png");
	image_file stop <- image_file("../images/stop.png");

	action toggle() {
		if paused {
			do resume();
		} else {
			do pause();
		}
	}

	init {
		create sign;
	}

}

species sign skills: [moving] {

	point location <- centroid(world);

	aspect default {
		draw (world.paused ? play : stop) size: {100, 100};
	}

	reflex wander {
		do wander(speed: 2.0);
	} 
	
}

experiment 'Try Me !' {
	output synchronized:true{
		display Interaction {
			species sign;
			event #mouse_down {
				if ((#user_location distance_to sign[0]) < 50) {
					ask world {
						do toggle();
					}

				}

			}

		}

	}

}
