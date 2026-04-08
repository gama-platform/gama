/**
* Name: Building Heatmap
* Author: Alexis Drogoul
* Description: Demonstrates how to overlay a heatmap on an existing model without modifying it, using GAMA's
*   model import mechanism. The heatmap is built on a field that accumulates the number of people passing
*   through each area of the city over time. The field is updated each step and displayed as a semi-transparent
*   color gradient over the road network. This model shows the power of GAMA's import system: the entire
*   Building Elevation model is reused unchanged, and only the heatmap visualization layer is added on top.
* Tags: heatmap, field, visualization, import, display, people, traffic, building
*/
model BuildingHeatmap

import "./Building Elevation.gaml"

global {
	int size <- 300;
	field instant_heatmap <- field(size, size);
	field history_heatmap <- field(size, size);
	reflex update {
		instant_heatmap[] <- 0 ;
		ask people {
			instant_heatmap[location] <- instant_heatmap[location] + 10;
			history_heatmap[location] <- history_heatmap[location] + 1;
		}
	}
}

experiment "Show heatmap" type: gui {
	output synchronized:true{
		layout #split;
		
		display city_display type: 3d {
			camera 'default' location: {1318.6512,3.5713,945.6612} target: {431.7016,495.2155,0.0};
			light #ambient intensity: 180;
			light #default intensity: 180 direction: {0.5, 0.5, -1};
			event #mouse_down {ask simulation {do resume();}} 
			species building aspect: base refresh: false;
			species road aspect: base refresh: false; 
			species people refresh: true;
		}
		display "Instant heatmap with palette" type: 3d axes: false background: #black  {
			// The field is displayed  without 3D rendering, a palettre of warm colors and a smoothness of 2 (meaning two passes of box blur are being done to "spread" the values)
			mesh instant_heatmap scale: 0 color: palette([ #black, #black, #orange, #orange, #red, #red, #red]) smooth: 2 ;
		}
		display "History heatmap with gradient" type: 3d axes: false background: #black camera: #from_up_front {
			// The field is displayed a little bit above the other layers, with a slight 3D rendering, and a smoothness of 1 (meaning one pass of box blur is being done to "spread" the values). The colors are provided by a gradient with three stops
			mesh history_heatmap scale: 0.01 color: gradient([#black::0, #cyan::0.5, #red::1]) transparency: 0.2 position: {0, 0, 0.001} smooth:1 ;
		 }
		

	}

}