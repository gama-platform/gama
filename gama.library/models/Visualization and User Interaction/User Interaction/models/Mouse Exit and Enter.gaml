/***
* Name: Mouse Exit and Enter
* Author: Alexis Drogoul
* Description: Demonstrates the 'mouse_enter' and 'mouse_exit' display events. The model shows a pair of eyes
*   that respond to mouse movement: when the mouse cursor enters the display, the eyes open and follow the
*   cursor position. When the mouse exits the display, the eyes close again. This playful example illustrates
*   how event layers can be used to create reactive, context-aware displays that respond to the user's presence.
* Tags: gui, mouse, mouse_enter, mouse_exit, interaction, display, animation
***/

model MouseExitandEnter 


global {
	bool closed <- false;
	init {
		create eyes(location:{25,30});
		create eyes(location:{65,30});
	}
}

species eyes {
	point look_at <- location;
	
	action follow() {
		float heading <- location towards #user_location;
		look_at <- location + {5*cos(heading), 5*sin(heading)};
	}
	
	aspect outside {
			draw sphere(15) color: #white ;
	}
	
	aspect inside {
		draw sphere(5) color: #blue wireframe: false at: look_at;
	}
}

experiment Run {
	output {
		display Eyes type: 3d axes: false {
			graphics face {
				draw circle(60) color: #gamaorange;
			}

			graphics mouth {
				draw curve({20, 80}, {70, 80}, (cos(location towards #user_location))) + 5 depth: 5;
			}

			species eyes aspect: inside;
			species eyes aspect: outside transparency: closed ? 0.1 : 0.5;
			event #mouse_exit {
				closed <- true;
			}

			event #mouse_enter {
				closed <- false;
			}

			event #mouse_move {
				ask eyes {
					do follow();
				}

			}

			event #mouse_menu {
				closed <- !closed;
			}

		}
	}
}