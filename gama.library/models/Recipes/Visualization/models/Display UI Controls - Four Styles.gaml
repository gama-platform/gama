/**
 * Name: Display UI Controls - Four Styles
 * Description: Four simultaneous simulations use exactly the same control panel.
 * The experiment supplies each simulation's style attributes at creation.
 * Liquid Glass is a translucent approximation, without native backdrop blur.
 * Tags: gui, multi_simulation, overlay, ui_button, ui_radio, styling
 */
model Display_UI_Control_Four_Styles

global {
	geometry shape <- square(100);
	// Inline handlers execute on the experiment; its current simulation can be another display's owner.
	Display_UI_Control_Four_Styles_model panel_owner <- self;
	string style_name <- "Liquid Glass";
	rgb scene_color <- rgb(175, 207, 232);
	rgb panel_color <- nil;
	rgb panel_border <- nil;
	rgb control_color <- nil;
	rgb outline_color <- nil;
	rgb text_color <- #black;
	rgb accent_color <- rgb(0, 122, 255);
	bool round_controls <- true;
	bool round_panel <- true;
	font panel_font <- font("Helvetica", 14);
	font title_font <- font("Helvetica", 18, #bold);
	point panel_position <- {16, 16};
	point panel_size <- {320, 440};
	point control_size <- {288, 30};
	bool moving <- true;
	bool show_trails <- false;
	bool slow_mode <- false;
	bool fast_mode <- true;
	float motion_speed <- 4.0;
	int scatter_count <- 0;

	init { create particle number: 35; }

	action preset(bool fast) {
		slow_mode <- !fast;
		fast_mode <- fast;
		motion_speed <- fast ? 4.0 : 1.0;
	}

	action scatter() {
		scatter_count <- scatter_count + 1;
		ask particle { location <- any_location_in(world.shape); }
	}

	action reset_controls() {
		moving <- true;
		show_trails <- false;
		scatter_count <- 0;
		do preset(true);
	}
}

species particle skills: [moving] {
	list<point> trail <- [];
	reflex move when: world.moving {
		do wander(speed: motion_speed, amplitude: 90.0);
		trail <- last(12, trail + [location]);
	}
	aspect default {
		if show_trails and length(trail) > 1 {
			draw polyline(trail) color: accent_color with_alpha 0.3;
		}
		draw circle(0.7) color: accent_color;
	}
}

experiment "Four styles together" type: gui {
	float minimum_cycle_duration <- 0.05;

	// Override default creation: these are exactly four simulations, not five.
	action _init_() {
		create simulation(name: "Liquid Glass", seed: 1.0,
			panel_color: rgb(255, 255, 255, 100), panel_border: rgb(255, 255, 255, 190),
			control_color: rgb(255, 255, 255, 65), outline_color: rgb(255, 255, 255, 160));
		create simulation(name: "GAMA FlatButton", seed: 1.0,
			style_name: "GAMA FlatButton", scene_color: rgb(233, 238, 242),
			panel_color: rgb(247, 249, 251), panel_border: #gray,
			control_color: rgb(222, 233, 239), outline_color: #gray,
			text_color: #black, accent_color: #teal, round_controls: false,
			round_panel: false, panel_font: font("SansSerif", 14),
			title_font: font("SansSerif", 18, #bold));
		create simulation(name: "Minimalist", seed: 1.0,
			style_name: "Minimalist", scene_color: #white,
			panel_color: nil, panel_border: nil, control_color: nil,
			outline_color: #gray, text_color: #black, accent_color: #black,
			round_controls: false, round_panel: false,
			panel_font: font("Helvetica", 13), title_font: font("Helvetica", 18));
		create simulation(name: "Midnight Arcade", seed: 1.0,
			style_name: "Midnight Arcade", scene_color: rgb(14, 12, 32),
			panel_color: rgb(29, 21, 57, 235), panel_border: rgb(255, 75, 190),
			control_color: rgb(49, 31, 78), outline_color: rgb(255, 75, 190),
			text_color: rgb(240, 230, 255), accent_color: rgb(0, 235, 210),
			round_controls: false, round_panel: true,
			panel_font: font("Monospaced", 14, #bold), title_font: font("Monospaced", 18, #bold));
	}

	output {
		layout #split tabs: false;
		// Each simulation instantiates this output using its own facet attributes.
		display "Styled controls" type: 3d background: scene_color axes: false{
			species particle;
			overlay position: panel_position size: panel_size
				background: panel_color border: panel_border rounded: round_panel {
				draw style_name at: {16, 12} anchor: #top_left color: text_color
					font: title_font;
				ui_button ("Scatter (" + string(scatter_count) + ")") action: scatter
					at: {16, 48} size: control_size rounded: round_controls
					color: text_color font: panel_font background: control_color
					border: outline_color accent: accent_color;
				ui_button (moving ? "Pause" : "Resume") toggle: true var: moving
					at: {16, 88} size: control_size rounded: round_controls
					color: text_color font: panel_font background: control_color
					border: outline_color accent: accent_color;
				ui_switch "Moving" var: moving at: {16, 128} size: control_size
					color: text_color font: panel_font background: control_color
					rounded: round_controls accent: accent_color;
				ui_switch "Trails" var: show_trails at: {16, 168} size: control_size
					color: text_color font: panel_font background: control_color
					rounded: round_controls accent: accent_color;
				ui_slider "Speed" var: motion_speed min: 0.0 max: 6.0 step: 0.25
					at: {16, 208} size: {288, 44} active: moving
					color: text_color font: panel_font background: control_color
					rounded: round_controls accent: accent_color continuous: true {
					ask panel_owner { slow_mode <- false; fast_mode <- false; }
				}
				ui_radio "Slow" var: slow_mode group: "speed" at: {16, 262} size: control_size
					color: text_color font: panel_font background: control_color
					rounded: round_controls accent: accent_color {
					if ui_value { ask panel_owner { do preset(false); } }
				}
				ui_radio "Fast" var: fast_mode group: "speed" at: {16, 302} size: control_size
					color: text_color font: panel_font background: control_color
					rounded: round_controls accent: accent_color {
					if ui_value { ask panel_owner { do preset(true); } }
				}
				ui_button "Reset this simulation" action: reset_controls
					at: {16, 342} size: control_size rounded: round_controls
					color: text_color font: panel_font background: control_color
					border: outline_color accent: accent_color;
				draw "Same controls, independent simulation state.\nPause, presets and Reset update other controls."
					at: {16, 390} anchor: #top_left color: text_color font: font("Helvetica", 11);
			}
		}
	}
}
