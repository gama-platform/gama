/**
 * Name: Display UI Controls - Simulation Toolbar
 * Description: An overlay controls the real experiment, like GAMA's toolbar.
 * Play/Pause, Step, Sync, Reload and Stop use the experiment's own actions.
 * Duration edits the experiment's actual minimum_cycle_duration (seconds).
 * Icons recreate the native toolbar's outlined circles and color palette.
 * Stop closes this experiment and its displays; Reload recreates its simulations.
 * Tags: gui, overlay, ui_button, ui_slider, experiment, toolbar
 */
model Display_UI_Control_Simulation_Toolbar

global {
	geometry shape <- square(100);
	init { create particle number: 50; }
}

species particle skills: [moving] {
	reflex move { do wander(speed: 2.0, amplitude: 90.0); }
	aspect default {
		draw circle(0.8) color: #teal border: false;
	}
}

experiment "Simulation toolbar in the display" type: gui {
	
	bool show <- false;
	bool opaque <- true;
	bool autohide <- true;
	
	
	int y_of_controls <- 32;
	int y_of_texts <- y_of_controls + 38;
	
	
	point location_of_controls <- {16,16};
	point size_of_controls <- {440, 130};
	float minimum_cycle_duration <- 0.1;

	image pause_icon <- image(svg_file("../images/ui-toolbar/experiment.pause.svg"), 32,32);

	image reload_icon <- image(svg_file("../images/ui-toolbar/experiment.reload.svg"), 32,32);

	image run_icon <- image(svg_file("../images/ui-toolbar/experiment.run.svg"), 32,32);

	image step_icon <- image(svg_file("../images/ui-toolbar/experiment.step.svg"), 32,32);

	image stop_icon <- image(svg_file("../images/ui-toolbar/experiment.stop.svg"), 32,32);

	image sync_icon <- image(svg_file("../images/ui-toolbar/experiment.sync.svg"), 32,32);

	output {
		display "Native experiment controls" type: opengl background: #white axes: false {
			species particle;
			event #mouse_move {
				show <- autohide ? (#mouse_location_in_display > location_of_controls) and #mouse_location_in_display < location_of_controls + size_of_controls : true;
			}
			
			overlay position: location_of_controls size: size_of_controls visible: show background: opaque ? #black with_alpha 0.6: nil
				{
				
				// Image-only buttons: no automatic text, background or border.
				ui_button "Play / Pause" at: {16, y_of_controls} size: {32, 32} toggle: true value: !is_paused
					image: run_icon image_pressed: pause_icon {
					if is_paused { do play(); } else { experiment.pause(); }
				}
				draw is_paused ? "Play" : "Pause" at: {32, y_of_texts} anchor: #top_center color: #adaptive;
				
				
				ui_button "Step" at: {64, y_of_controls} size: {32, 32}
					image: step_icon { do step_once(); }
				draw "Step" at: {80, y_of_texts} anchor: #top_center color: #adaptive;
					
				ui_slider "Min. cycle duration" var: minimum_cycle_duration
					min: 0.0 max: 1.0 step: 0.05 at: {96, y_of_controls} size: {200, 44}
					accent: #gray font: font("Helvetica", 12);
					
				
				ui_button "Sync / async" at: {296, y_of_controls} size: {32, 32}
					image: sync_icon {
					if is_synchronized { do desynchronize(); } else { do synchronize(); }
				}
				draw is_synchronized ? "Unsync" : "Sync" at: {312, y_of_texts} anchor: #top_center color: #adaptive;
				
				
				ui_button "Reload" at: {344, y_of_controls} size: {32, 32}
					image: reload_icon { do reload(); }
				draw "Reload" at: {360, y_of_texts} anchor: #top_center color: #adaptive;


				ui_button "Stop" at: {392, y_of_controls} size: {32, 32}
					image: stop_icon { do die(); }
				draw "Exit" at: {408, y_of_texts} anchor: #top_center color: #adaptive;
				
				ui_switch "OPAQUE" at: {8, -32} size: {140, 32} var: opaque font: font("Helvetica", 10, #bold);
				ui_switch "HIDE" at: {-140, -32} size: {140, 32} var: autohide font: font("Helvetica", 10, #bold);

			}
		}
	}
}
