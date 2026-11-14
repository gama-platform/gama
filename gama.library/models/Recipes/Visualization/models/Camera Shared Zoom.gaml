/**
* Name: Camera Shared Zoom
* Author: Alexis Drogoul
* Description: Four displays share a continuous zoom slider, initially 1.0 and ranging from 0.1 to 2.0.
*   Dragging the slider updates all four cameras immediately, without waiting for pointer release.
*   Each camera stays 500 / factor world units from its target. Right-click a point in a display
*   and choose World > Apply Focus here to change only that display's target, without changing the shared zoom.
*   The minimal slider and instructions appear in every display; Parameters and Consoles are hidden.
*   The model imports Building Elevation and keeps its buildings, roads and moving people unchanged.
* Tags: 3d, zoom, camera, display, synchronization, multi_view, visualization, ui_slider, user_command
*/
model Camera_Shared_Zoom

import "Building Elevation.gaml"

global { 
	map<int, point> focus_targets <- [];
	int focus_display <- 0;
	point focus_location <- nil;

	user_command "Focus here" when: focus_location != nil {
		focus_targets[focus_display] <- focus_location;
	}
}

experiment "Shared Zoom Example" type: gui {
	float factor <- 1.0;
	int target1 <- 50;
	int target2 <- 20;
	int target4 <- 75;
	int target3 <- 5;
	float distance -> 500 / factor;
	
	output {
		display base type: 3d light: true virtual: true toolbar: false {
			species building aspect: base refresh: false;
			species road aspect: base refresh: false;
			species people refresh: true;
			overlay position: {12, -0.25} size: {280, 86} {
				ui_slider "Shared zoom" var: experiment.factor min: 0.1 max: 2.0 continuous: true
					at: {0, 0} size: {280, 40};
				draw "Right-click the scene, then choose\nWorld > Apply Focus here (this display only)."
					at: {24, 48} anchor: #top_left color: #adaptive font: font("Helvetica", 11);
			}
		}

		layout #split parameters: false consoles: false;
		display "1" parent: base {
			camera #default location: #isometric
				target: focus_targets[1] != nil ? focus_targets[1] : (building at experiment.target1).location
				distance: experiment.distance dynamic: true;
			event #mouse_listener {
				if #mouse_action = #mouse_menu {
					focus_display <- 1;
					focus_location <- #user_location;
				}
			}
		}

		display "2" parent: base {
			camera #default target: focus_targets[2] != nil ? focus_targets[2] : (building at experiment.target2).location
				distance: experiment.distance dynamic: true;
			event #mouse_listener {
				if #mouse_action = #mouse_menu {
					focus_display <- 2;
					focus_location <- #user_location;
				}
			}
		}

		display "3" parent: base {
			camera #default target: focus_targets[3] != nil ? focus_targets[3] : (building at experiment.target3).location
				distance: experiment.distance dynamic: true;
			event #mouse_listener {
				if #mouse_action = #mouse_menu {
					focus_display <- 3;
					focus_location <- #user_location;
				}
			}
		}

		display "4" parent: base {
			camera #default target: focus_targets[4] != nil ? focus_targets[4] : (building at experiment.target4).location
				distance: experiment.distance dynamic: true;
			event #mouse_listener {
				if #mouse_action = #mouse_menu {
					focus_display <- 4;
					focus_location <- #user_location;
				}
			}
		}

	}

}
