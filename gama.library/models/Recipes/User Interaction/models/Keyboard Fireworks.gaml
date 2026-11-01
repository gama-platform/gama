/**
* Name: Keyboard Fireworks
* Author: GAMA team
* Description: A single key listener turns the keyboard into a fireworks launcher. Press a digit to
*   change the size of the next burst, press another key to launch it, and use Backspace to clear
*   the sky. A left mouse press moves the launch point. The two displays share the same simulation,
*   but each receives events only while it has focus.
* Tags: gui, event, keyboard, mouse, interaction, animation
*/
model keyboard_fireworks

global {
	geometry shape <- square(100);
	point launch_at <- {50, 50};
	int burst_size <- 4;
	string last_key <- "none";
	rgb burst_color <- #yellow;

	action react_to_key() {
		// The value remains available in this action even though it was called inside ask simulation.
		string pressed <- #key_pressed;
		last_key <- pressed;
		if pressed = "backspace" {
			ask spark { do die(); }
		} else if pressed in ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"] {
			// One listener replaces ten separate event "0", event "1", ... declarations.
			burst_size <- int(pressed);
		} else if not (pressed in [#shift, #ctrl, #alt, #cmd]) {
			do launch_firework();
		}
	}

	action launch_firework() {
		burst_color <- rgb(100 + rnd(155), 100 + rnd(155), 100 + rnd(155));
		create spark number: 10 + 4 * burst_size {
			location <- launch_at;
			ink <- burst_color;
			velocity <- {rnd(4.0) - 2.0, rnd(4.0) - 2.0};
		}
	}
}

species spark {
	point velocity <- {0, 0};
	rgb ink <- #white;
	int remaining <- 24 + rnd(15);

	reflex fly {
		location <- location + velocity;
		velocity <- velocity * 0.96 + {0, 0.10};
		remaining <- remaining - 1;
		if remaining <= 0 { do die(); }
	}

	aspect default {
		draw circle(0.8) color: rgb(ink, remaining / 40.0);
	}
}

experiment Fireworks type: gui {
	float minimum_cycle_duration <- 0.04;

	output {
		display "Fireworks Java2D" type: 2d background: #black {
			graphics "night" { draw world color: rgb(8, 10, 28); }
			species spark;

			event #key_listener {
				ask simulation { do react_to_key(); }
			}
			event #mouse_listener {
				if #mouse_action = #mouse_down and #mouse_button = "left" {
					ask simulation { launch_at <- #user_location; }
				}
			}

			overlay position: {0.02, 0.02} size: {0.96, 130#px} background: #white border: #gray {
				draw "Click this display to focus and choose a launch point" at: {12#px, 26#px} color: #black font: font("SansSerif", 14);
				draw "0-9: burst size | Other keys: launch | Backspace: clear" at: {12#px, 50#px} color: #black font: font("SansSerif", 14);
				draw "Last key: " + last_key + "   Burst size: " + burst_size at: {12#px, 78#px} color: #blue font: font("SansSerif", 14);
				draw "Built-in shortcuts remain active." at: {12#px, 106#px} color: #black font: font("SansSerif", 14);
			}
		}

		display "Fireworks OpenGL" type: opengl parent: "Fireworks Java2D" {
			camera #from_above locked: true;
		}
	}
}
