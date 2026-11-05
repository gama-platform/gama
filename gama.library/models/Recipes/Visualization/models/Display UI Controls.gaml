/**
 * Overlay controls share screen placement with draw statements.
 * Switches and sliders commit their new value on pointer release.
 */
model Display_UI_Controls

global {
	float speed <- 25.0;
	bool running <- true;
}

experiment "Overlay controls" type: gui {
	action reset_controls() {
		speed <- 25.0;
		running <- true;
	}

	output {
		display "Controls" type: java2D {
			graphics {
				draw world.shape color: #lightgray;
			}
			overlay position: {16, 16} size: {240, 300} background: #white {
				ui_button "Reset" at: {12, 12} size: {216, 28}
					background: #lightgray color: #black border: #gray action: reset_controls;
				ui_switch "Running" var: running at: {12, 52} size: {216, 28} border: #gray;
				ui_slider "Speed" var: speed min: 0.0 max: 100.0 step: 1.0
					at: {12, 92} size: {216, 40} active: running border: #gray;
				ui_slider "Explicit change handler" value: speed min: 0.0 max: 100.0 step: 1.0
					at: {12, 144} size: {216, 40} {
					speed <- ui_value;
				}
				ui_button "Running (toggle)" toggle: true var: running
					at: {12, 196} size: {216, 28} background: nil border: #gray;
				ui_button "Image toggle" toggle: true value: running
					image: "../includes/wood.jpg" image_checked: "../includes/Texture.jpg"
					image_hover: "../includes/DEM.png" image_pressed: "../includes/Texture.jpg"
					at: {12, 236} size: {216, 40} color: #white border: #black on_change: {
					running <- ui_value;
				};
			}
		}
	}
}
