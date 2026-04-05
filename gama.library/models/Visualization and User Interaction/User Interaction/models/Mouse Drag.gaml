/**
* Name: Mouse Drag
* Author: Breugnot
* Description: Demonstrates the use of mouse drag events to move agents interactively in a GAMA display.
*   Drag events fire continuously while the mouse button is held down and the cursor is moving; they differ
*   from mouse_move events (which fire only when the button is up). The model shows how to detect which agent
*   is under the cursor on mouse_down, then update its position continuously during the drag, and finalize
*   the placement on mouse_up. This pattern is the standard way to implement drag-and-drop agent manipulation.
* Tags: gui, event, mouse_move, mouse_drag, mouse_down, mouse_up, interaction, display
*/

model mouse_event

global {
	geometry shape <- square(20);
	DraggedAgent selected_agent <- nil;
	init {
		create DraggedAgent with: (location: {10, 10});
	}
	
	/** Insert the global definitions, variables and actions here */
	action mouse_down() {
		ask DraggedAgent {
			if( self covers #user_location) {
				// Selects the agent
				selected_agent <- self;
			}
		}
	}
	
	action mouse_up() {
		if(selected_agent != nil) {
			selected_agent <- nil;
		}
	}
	
	action mouse_drag() {
		// Makes the agent follow the mouse while the mouse button is down
		if(selected_agent != nil) {
			ask selected_agent {
				location <- #user_location;
			}
		}
	}
}

species DraggedAgent {
	init {
		shape <- circle(1);
	}
	aspect default {
		draw shape at: location;
	}
}

experiment "Mouse Drag" type: gui {
	/** Insert here the definition of the input and output of the model */
	output {
		display "Mouse Drag [in OpenGL]" type:opengl {
			camera #from_above locked:true;
			event #mouse_down {ask simulation {do mouse_down;}}
			event #mouse_up {ask simulation {do mouse_up;}}
			event #mouse_drag {ask simulation {do mouse_drag;}}
			
			graphics "world" {
				draw world color: #white border:#black;
			}

			species DraggedAgent aspect:default;
		}

		display "In Java2D, one needs to lock the surface first " type:java2D {
			event #mouse_down {ask simulation {do mouse_down;}}
			event #mouse_up {ask simulation {do mouse_up;}}
			event #mouse_drag {ask simulation {do mouse_drag;}}
			
			graphics "world" {
				draw world color: #white border:#black;
			}
			
			species DraggedAgent aspect:default;
		}
	}
}
