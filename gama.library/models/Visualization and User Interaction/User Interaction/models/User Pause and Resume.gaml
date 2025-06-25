/***
* Name: UserPauseandResume
* Author: A. Drogoul
* Description: Shows how to use the simulation actions 'pause' and 'resume' within a user interaction
* Tags: simulation, pause, resume
***/
model UserPauseandResume

global {
	geometry shape <- square(400);
	image_file play <- image_file("../images/play.png");
	image_file stop <- image_file("../images/stop.png");

	action toggle {
		if paused {
			ask sign{
				icon <- stop;
			}
			do resume;
		} else {
			
			ask sign{
				icon <- play;
				// we use a boolean to let sign stop the simulation in its own cycle, so its aspect has the time to refresh before pausing the whole simulation
				must_stop <- true; 
			}
		}
	}

	init {
		create sign;
	}

}

species sign skills: [moving] {

	point location <- centroid(world);
	image_file icon <- play;
	bool must_stop <- false;

	aspect default {
		draw icon size: {100, 100};
	}

	reflex check_if_must_stop{
		if must_stop {
			must_stop <- false;
			ask world { do pause; }
		}
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
						do toggle;
					}

				}

			}

		}

	}

}
