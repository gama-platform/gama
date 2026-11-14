/**
 * Name: Display UI Controls - Interactions
 * Description: Buttons call simulation actions, while simulation attributes drive
 * other controls' values, labels, availability and appearance.
 * Tags: gui, overlay, ui_button, ui_switch, ui_slider, ui_radio, interaction
 */
model Display_UI_Control_Interactions

global {
	geometry shape <- square(100);
	bool moving <- true;
	bool controls_locked <- false;
	bool slow_mode <- false;
	bool fast_mode <- true;
	float motion_speed <- 4.0;
	int resets <- 0;
	string feedback <- "Try presets, Lock controls, and Reset.";
	rgb button_accent <- #teal;

	init {
		create particle number: 30;
	}

	action set_preset(bool fast) {
		slow_mode <- !fast;
		fast_mode <- fast;
		motion_speed <- fast ? 4.0 : 1.0;
		moving <- true;
		button_accent <- fast ? #teal : #orange;
		feedback <- fast ? "Fast preset: speed, radios and toggle updated."
			: "Slow preset: speed, radios and toggle updated.";
	}

	action reset_panel() {
		resets <- resets + 1;
		controls_locked <- false;
		do set_preset(true);
		ask particle { location <- any_location_in(world.shape); }
		feedback <- "Reset " + string(resets) + ": controls unlocked.\nAgents repositioned and fast preset restored.";
	}

	action scatter() {
		ask particle { location <- any_location_in(world.shape); }
		feedback <- "Scatter repositioned agents.\nThe simulation changed, not just the widgets.";
	}
}

species particle skills: [moving] {
	reflex move when: world.moving {
		do wander(speed: motion_speed, amplitude: 90.0);
	}
	aspect default {
		draw circle(0.7) color: button_accent;
	}
}

experiment "Control interactions" type: gui {
	float minimum_cycle_duration <- 0.05;
	output {
		display "Interaction playground" type: java2D background: #white {
			species particle;
			overlay position: {16, 16} size: {360, 450}
				background: #white with_alpha 0.92 border: #lightgray rounded: true {
				draw "Simulation-driven controls" at: {16, 12}
					anchor: #top_left font: font("Helvetica", 18, #bold) color: #black;
				ui_button "Scatter agents" at: {16, 48} size: {328, 30}
					accent: button_accent active: !controls_locked {
					do scatter(); 
				}
				ui_button (moving ? "Pause motion" : "Resume motion")
					toggle: true var: moving at: {16, 88} size: {328, 30}
					accent: button_accent active: !controls_locked {
						feedback <- moving ? "Toggle resumed movement." : "Toggle paused movement.";
				}
				ui_switch "Moving (same binding)" var: moving at: {16, 128} size: {328, 30}
					accent: button_accent active: !controls_locked;
				ui_slider "Motion speed" var: motion_speed min: 0.0 max: 6.0 step: 0.25
					at: {16, 168} size: {328, 44} accent: button_accent active: !controls_locked {
						slow_mode <- false;
						fast_mode <- false;
						feedback <- "Custom speed selected.\nBoth preset radios cleared by the simulation.";
				}
				ui_radio "Slow preset" var: slow_mode group: "speed"
					at: {16, 222} size: {328, 30} accent: button_accent active: !controls_locked {
					if ui_value {  do set_preset(false); } 
				}
				ui_radio "Fast preset" var: fast_mode group: "speed"
					at: {16, 262} size: {328, 30} accent: button_accent active: !controls_locked {
					if ui_value {  do set_preset(true); } 
				}
				ui_button (controls_locked ? "Unlock controls" : "Lock controls")
					at: {16, 302} size: {158, 30} rounded: false accent: #orange {
						controls_locked <- !controls_locked;
						feedback <- controls_locked ? "Other controls disabled; Reset still works."
							: "Other controls enabled again.";
				}
				// A direct global action reference also targets the simulation.
				ui_button ("Reset (" + string(resets) + ")") action: reset_panel
					at: {186, 302} size: {158, 30} rounded: false accent: button_accent;
				draw feedback at: {16, 350} anchor: #top_left color: #black
					font: font("Helvetica", 11);
				draw "Presets change values, labels and accent.\nLock changes active:; Reset restores everything."
					at: {16, 386} anchor: #top_left color: #gray font: font("Helvetica", 12);
			}
		}
	}
}
