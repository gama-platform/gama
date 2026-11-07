/**
 * Name: Display UI Controls - Simulation Toolbar
 * Description: An overlay controls the real experiment, like GAMA's toolbar.
 * Play/Pause, Step, Sync, Reload and Stop use native command: operations.
 * Duration edits the experiment's actual minimum_cycle_duration (seconds).
 * Icons recreate the native toolbar's outlined circles and color palette.
 * Stop closes the experiment and its displays; Reload recreates the simulations.
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
	float minimum_cycle_duration <- 0.1;

	image pause_icon <- image(svg_file("../images/ui-toolbar/experiment.pause.svg"), 32,32);
	image pause_icon_hover <- pause_icon * #lightgray;

	image reload_icon <- image(svg_file("../images/ui-toolbar/experiment.reload.svg"), 32,32);
	image reload_icon_hover <- reload_icon * #lightgray;

	image run_icon <- image(svg_file("../images/ui-toolbar/experiment.run.svg"), 32,32);
	image run_icon_hover <- run_icon * #lightgray;

	image step_icon <- image(svg_file("../images/ui-toolbar/experiment.step.svg"), 32,32);
	image step_icon_hover <- step_icon * #lightgray;

	image stop_icon <- image(svg_file("../images/ui-toolbar/experiment.stop.svg"), 32,32);
	image stop_icon_hover <- stop_icon * #lightgray;

	image sync_icon <- image(svg_file("../images/ui-toolbar/experiment.sync.svg"), 32,32);
	image sync_icon_hover <- sync_icon * #lightgray;

	output {
		display "Native experiment controls" type: java2D background: #white {
			species particle;
			overlay position: {16, 16} size: {420, 210}
				background: #white with_alpha 0.95 border: #lightgray rounded: true {
				draw "Experiment toolbar" at: {16, 12} anchor: #top_left
					color: #black font: font("Helvetica", 18, #bold);
				// Image-only buttons: no automatic text, background or border.
				ui_button "Play / Pause" value: !paused
					at: {16, 48} size: {32, 32} toggle: true image: run_icon image_hover: run_icon_hover image_pressed: pause_icon {paused <- false;}
				ui_button "Step" command: "step"
					at: {104, 48} size: {32, 32} image: step_icon image_hover: step_icon_hover;
				ui_button "Sync / async" command: "sync"
					at: {176, 48} size: {32, 32} image: sync_icon image_hover: sync_icon_hover;
				ui_button "Reload" command: "reload"
					at: {260, 48} size: {32, 32} image: reload_icon image_hover: reload_icon_hover;
				ui_button "Stop" command: "stop"
					at: {348, 48} size: {32, 32} image: stop_icon image_hover: stop_icon_hover;
				draw "Play/Pause        Step       Sync/async      Reload          Stop"
					at: {16, 86} anchor: #top_left color: #black font: font("Helvetica", 11);
				//draw duration_icon at: {16, 115} size: {28, 28};
				ui_slider "Minimum cycle duration (s)" var: minimum_cycle_duration
					min: 0.0 max: 1.0 step: 0.05 at: {56, 108} size: {348, 44}
					accent: #teal font: font("Helvetica", 12);
				draw "These are experiment commands, not a motion toggle.\nStep advances one cycle; Sync waits for outputs.\nReload recreates the run; Stop closes its displays."
					at: {16, 164} anchor: #top_left color: #gray font: font("Helvetica", 11);
			}
		}
	}
}
