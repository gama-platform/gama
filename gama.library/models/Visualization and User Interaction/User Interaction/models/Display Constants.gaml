/**
* Name: Display Constants
* Author: Alexis Drogoul
* Description: Shows the use of built-in GAML constants that provide information about user actions in displays.
*   These constants include: #mouse_location (current mouse position), #zoom (current zoom level),
*   #display_width/#display_height (display pixel dimensions), and #view_zoom. They are useful for building
*   responsive displays that react differently based on the current view state, or for implementing
*   precise coordinate-based user interactions.
* Tags: display, constants, mouse, zoom, interaction, gui
*/


model DisplayConstants

experiment "Run" {

	output {
		display "Display2d" background: #black type: 2d {
			graphics back {
				draw rectangle(shape.width, shape.height) color: #blue;
			}
			graphics g {
				draw "In world: " + #user_location at: #user_location;
				draw "In display: " + #user_location_in_display at: #user_location + {0,5};
			}
			event #mouse_move {do update_outputs;}
			
		}
		display "Display3d" background: #black type: 3d {
			graphics back {
				draw rectangle(shape.width, shape.height) color: #blue;
			}
			graphics g {
				draw "In world: " + #user_location at: #user_location;
				draw "In display" + #user_location_in_display at: #user_location + {0,5};
			}
			event #mouse_move {do update_outputs;}
			
		}



	}
	
}

