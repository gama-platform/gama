/**
* Name: Mouse Garden
* Author: GAMA team
* Description: Paint a garden with one mouse listener. Left, middle and right buttons plant
*   different flowers; dragging plants a trail. The overlay displays the current mouse action and
*   button. Digits change the brush size, and Space clears the garden. Native context menus and
*   display controls remain enabled, so a right-click may also open the platform menu.
* Tags: gui, event, keyboard, mouse, drag, interaction, art
*/
model mouse_garden

global {
	geometry shape <- square(100);
	int brush_size <- 4;
	string last_key <- "none";
	string last_action <- "none";
	string last_button <- "none";
	point last_pointer <- {0, 0};
	point last_stamp <- {-100, -100};

	action react_to_key() {
		last_key <- #key_pressed;
		if last_key in ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"] {
			brush_size <- last_key = "0" ? 1 : int(last_key);
		} else if last_key = " " {
			ask blossom { do die(); }
		}
	}

	action react_to_mouse() {
		// All these values describe this callback, not a global "last event".
		last_action <- #mouse_action;
		last_button <- #mouse_button;
		last_pointer <- #user_location;
		if last_button != "none" and (last_action = #mouse_down or
				(last_action = #mouse_drag and (last_stamp distance_to #user_location) > 2.0)) {
			last_stamp <- #user_location;
			create blossom number: 1 {
				location <- #user_location;
				radius <- brush_size / 2.0;
				tint <- #mouse_button = "left" ? #magenta :
						(#mouse_button = "middle" ? #gold : #cyan);
			}
		}
	}
}

species blossom {
	float radius <- 2.0;
	rgb tint <- #magenta;

	aspect default {
		draw circle(radius * 2.0) color: rgb(tint, 0.7);
		draw circle(radius * 0.7) color: #yellow;
	}
}

experiment Garden type: gui {
	output {
		display "Garden Java2D" type: 2d background: #black locked: true {
			graphics "grass" { draw world color: rgb(18, 45, 37); }
			species blossom;

			event #mouse_listener {
				do react_to_mouse(); 
			}
			event #key_listener {
				do react_to_key(); 
			}

			overlay coordinates: #screen position: {0.02, 0.02} size: {0.96, 150} background: #white border: #gray {
				draw "Click or drag: left = pink, middle = gold, right = blue" at: {12, 26} color: #black font: font("SansSerif", 14);
				draw "0-9: brush size | Space: clear | Right-click may open a menu" at: {12, 50} color: #black font: font("SansSerif", 14);
				draw "Mouse: " + last_action + " / " + last_button + " at " + last_pointer at: {12, 78} color: #blue font: font("SansSerif", 14);
				draw "Key: " + last_key + "   Brush size: " + brush_size at: {12, 106} color: #black font: font("SansSerif", 14);
				draw "Click a display before typing; each display observes its own input." at: {12, 132} color: #black font: font("SansSerif", 14);
			}
		}

		display "Garden OpenGL" type: opengl parent: "Garden Java2D" {
			camera #from_above locked: true;
		}
	}
}
