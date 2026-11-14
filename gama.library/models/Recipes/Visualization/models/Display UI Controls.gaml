/**
 * Overlay controls share screen placement with draw statements.
 * Controls commit on pointer release by default; continuous sliders update while dragging.
 */
model Display_UI_Controls

global {

}

experiment "Overlay controls" type: gui {
	float speed <- 25.0;
	bool running <- true;
	action reset_controls() {
		speed <- 25.0;
		running <- true;
	}

	output of: experiment {
		display "Controls" type: java2D {
			overlay position: {16, 16} size: {240, 300} background: rgb(242, 247, 255, 190)
				border: rgb(255, 255, 255, 225) rounded: true {
				ui_button "Reset" at: {12, 12} size: {216, 28}
					accent: #teal action: reset_controls;
				ui_switch "Running" var: running at: {12, 52} size: {216, 28} accent: #teal;
				ui_slider "Speed" var: speed min: 0.0 max: 100.0 step: 1.0
					at: {12, 92} size: {216, 40} active: running accent: #teal;
				ui_slider "Explicit change handler" value: speed min: 0.0 max: 100.0 step: 1.0
					at: {12, 144} size: {216, 40} continuous: true {
					speed <- ui_value;
				}
				ui_button "Running (toggle)" toggle: true var: running
					at: {12, 196} size: {216, 28};
				ui_button "Image toggle" toggle: true value: running
					image: "../includes/wood.jpg" image_checked: "../includes/Texture.jpg"
					image_hover: "../includes/DEM.png" image_pressed: "../includes/Texture.jpg"
					at: {12, 236} size: {216, 40} on_change: {
					running <- ui_value;
				};
			}
		}
	}
}
