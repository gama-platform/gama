/**
* Name: Keyboard Combinations
* Author: GAMA team
* Description: Demonstrates explicit keyboard combinations in display event layers, including string
*   syntax, existing modifier constants, compact concatenation, named keys and exact modifier matching.
*   Run an experiment, click inside its display and use the shortcuts listed in the overlay.
*   Event blocks execute in the experiment context, so simulation state is changed through ask simulation.
*   Built-in shortcuts remain active; operating-system or workbench shortcuts may intercept some keys.
*   In OpenGL/OpenGL4, camera navigation uses CMD only on macOS (not CTRL), and CTRL on Windows/Linux.
*   macOS users migrating from CTRL-based camera gestures must use CMD instead; CTRL-click still opens the menu.
* Tags: gui, event, keyboard, modifiers, ctrl, cmd, shift, alt, interaction
*/
model keyboard_combinations

global {
	point cursor <- {50, 50};
	string last_event <- "Click inside the display, then use a shortcut";
	int event_count <- 0;

	action record(string event_label) {
		last_event <- event_label;
		event_count <- event_count + 1;
	}
}

experiment Keyboard_2D type: gui {
	output {
		display Keyboard type: 2d {
			graphics {
				draw circle(3) at: cursor color: #blue;
			}
			// Overlay contents use view/model units, not fractions of the overlay: use pixels for text.
			overlay position: {0.02, 0.02} size: {0.96, 220#px} background: #white border: #gray {
				draw "Click the display to focus it" at: {12#px, 26#px} color: #black font: font("SansSerif", 14);
				draw "A: legacy key | CTRL+A: compact constant" at: {12#px, 50#px} color: #black font: font("SansSerif", 14);
				draw "CTRL+SHIFT+A: string | CTRL+SHIFT+B: multiple constants" at: {12#px, 74#px} color: #black font: font("SansSerif", 14);
				draw "SHIFT+B, ALT+C, CMD/WIN+C: modifier constants" at: {12#px, 98#px} color: #black font: font("SansSerif", 14);
				draw "ALT+UP: move | CMD/WIN+ENTER: reset" at: {12#px, 122#px} color: #black font: font("SansSerif", 14);
				draw "SHIFT+SPACE: message" at: {12#px, 146#px} color: #black font: font("SansSerif", 14);
				draw last_event at: {12#px, 174#px} color: #blue font: font("SansSerif", 14);
				draw "Events received: " + event_count at: {12#px, 202#px} color: #black font: font("SansSerif", 14);
			}

			event "a" { ask simulation { do record("Legacy A event"); } }
			// #ctrl retains its value "ctrl": this compact expression produces "ctrla".
			event #ctrl + "a" { ask simulation { do record("CTRL+A (compact constant syntax)"); } }
			event "ctrl+shift+a" { ask simulation { do record("CTRL+SHIFT+A (exact modifier match)"); } }
			event #shift + "b" { ask simulation { do record("SHIFT+B (compact constant)"); } }
			event #alt + "c" { ask simulation { do record("ALT+C (compact constant)"); } }
			event #cmd + "c" { ask simulation { do record("Command/Windows+C (compact constant)"); } }
			event #ctrl + #shift + "b" { ask simulation { do record("CTRL+SHIFT+B (compact constants)"); } }
			event #alt + "+" + #arrow_up {
				ask simulation {
					cursor <- cursor + {0, -2};
					do record("ALT+UP");
				}
			}
			event #cmd + "+enter" {
				ask simulation {
					cursor <- {50, 50};
					do record("Command/Windows+Enter");
				}
			}
			event "shift+space" { ask simulation { do record("SHIFT+SPACE"); } }

			// Standalone modifier constants remain valid, subject to native event delivery.
			event #ctrl { write "Standalone CTRL"; }
			event #shift { write "Standalone SHIFT"; }
			event #alt { write "Standalone ALT"; }
			event #cmd { write "Standalone Command/Windows"; }
		}
	}
}

experiment Keyboard_3D type: gui {
	output {
		display Keyboard type: 3d {
			graphics { draw sphere(3) at: cursor color: #blue; }
			// CTRL warnings apply on Windows; camera and display shortcuts remain active on every platform.
			event #ctrl + "a" { ask simulation { do record("CTRL+A in OpenGL"); } }
			event "ctrl+shift+a" { ask simulation { do record("CTRL+SHIFT+A in OpenGL"); } }
			event #alt + "+" + #arrow_up {
				ask simulation {
					cursor <- cursor + {0, -2};
					do record("ALT+UP: camera navigation may also run");
				}
			}
			event #cmd + "+enter" { ask simulation { do record("Command/Windows+Enter in OpenGL"); } }
			overlay position: {0.02, 0.02} size: {0.96, 100#px} background: #white border: #gray {
				draw "Click to focus. CTRL+A, CTRL+SHIFT+A" at: {12#px, 26#px} color: #black font: font("SansSerif", 14);
				draw "ALT+UP, CMD/WIN+ENTER" at: {12#px, 50#px} color: #black font: font("SansSerif", 14);
				draw last_event at: {12#px, 78#px} color: #blue font: font("SansSerif", 14);
			}
		}
	}
}
