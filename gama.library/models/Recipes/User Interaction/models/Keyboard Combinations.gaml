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
		int i <- 1000;
	}
}

experiment Keyboard_2D type: gui {
	output {
		display Keyboard type: 2d {
			graphics {
				draw circle(3) at: cursor color: #blue;
			}
			// Relative frame placement and pixel-valued text positions share an explicit screen context.
			overlay coordinates: #screen position: {0.02, 0.02} size: {0.96, 220} background: #white border: #gray {
				draw "Click the display to focus it" at: {12, 26} color: #black font: font("SansSerif", 14);
				draw "A: legacy key | CTRL+A: compact constant" at: {12, 50} color: #black font: font("SansSerif", 14);
				draw "CTRL+SHIFT+A: string | CTRL+SHIFT+B: multiple constants" at: {12, 74} color: #black font: font("SansSerif", 14);
				draw "SHIFT+B, ALT+C, CMD/WIN+C: modifier constants" at: {12, 98} color: #black font: font("SansSerif", 14);
				draw "ALT+UP: move | CMD/WIN+ENTER: reset" at: {12, 122} color: #black font: font("SansSerif", 14);
				draw "SHIFT+SPACE: message" at: {12, 146} color: #black font: font("SansSerif", 14);
				draw last_event at: {12, 174} color: #blue font: font("SansSerif", 14);
				draw "Events received: " + event_count at: {12, 202} color: #black font: font("SansSerif", 14);
			}

			event "a" { do record("Legacy A event"); } 
			// #ctrl retains its value "ctrl": this compact expression produces "ctrla".
			event #ctrl + "a" { do record("CTRL+A (compact constant syntax)"); } 
			event "ctrl+shift+a" { do record("CTRL+SHIFT+A (exact modifier match)"); }
			event #shift + "b" { do record("SHIFT+B (compact constant)"); } 
			event #alt + "c" { do record("ALT+C (compact constant)"); } 
			event #cmd + "c" { do record("Command/Windows+C (compact constant)"); } 
			event #ctrl + #shift + "b" { do record("CTRL+SHIFT+B (compact constants)"); } 
			event #alt + "+" + #arrow_up {
					cursor <- cursor + {0, -2};
					do record("ALT+UP");
			}
			event #cmd + "+enter" {
					cursor <- {50, 50};
					do record("Command/Windows+Enter");
			}
			event "shift+space" { do record("SHIFT+SPACE"); } 

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
			event #ctrl + "a" { do record("CTRL+A in OpenGL"); } 
			event "ctrl+shift+a" { do record("CTRL+SHIFT+A in OpenGL"); } 
			event #alt + "+" + #arrow_up {
					cursor <- cursor + {0, -2};
					do record("ALT+UP: camera navigation may also run");
			}
			event #cmd + "+enter" { do record("Command/Windows+Enter in OpenGL"); } 
			overlay coordinates: #screen position: {0.02, 0.02} size: {0.96, 100} background: #white border: #gray {
				draw "Click to focus. CTRL+A, CTRL+SHIFT+A" at: {12, 26} color: #black font: font("SansSerif", 14);
				draw "ALT+UP, CMD/WIN+ENTER" at: {12, 50} color: #black font: font("SansSerif", 14);
				draw last_event at: {12, 78} color: #blue font: font("SansSerif", 14);
			}
		}
	}
}
