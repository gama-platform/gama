/**
* Name: Quadratic, Linear and Constant Attenuation
* Author: Baptiste Lesquoy
* Description: Demonstrates the three light attenuation models available for point lights in GAMA's 3D display.
*   Attenuation controls how light intensity falls off with distance: constant attenuation (no fall-off),
*   linear attenuation (intensity decreases proportionally to distance), and quadratic attenuation (intensity
*   decreases with the square of the distance, matching real-world physics). The model places identical objects
*   at various distances from a point light and shows the visual difference between the three modes.
* Tags: 3d, light, attenuation, point_light, visualization, display, physics, shading
*/


model QuadraticLinearandConstantattenuation


global {

	init {
		create GAMAGeometry2D number: 1 {
			location <- {world.shape.width / 2, world.shape.height / 2, 0};
		}

	}

}

species GAMAGeometry2D {

	aspect default {
		draw sphere(10) at: location color: #white border: #gray;
	}

}



experiment Display type: gui autorun: true {
	float minimum_cycle_duration <- 0.01;
	float quad;
	float constant;
	float linear;
	int angle;
	int height;
	int distance;
	parameter "Spot light angle" var:angle <- 0 min:0 max:360 slider:true category:"Spot light location";
	parameter "Spot light height" var:height <- 0 min:-30 max:30 slider:true category:"Spot light location";
	parameter "Spot light distance from ball" var:distance <- 10 min:2 max:30 slider:true category:"Spot light location";
	parameter "Quadratic attenuation" var:quad <- 0.0001 min:0.0000001 max:0.001 slider:true category:"Light attenuation";
	parameter "Linear attenuation" var:linear <- 0.001 min:0.0000001 max:0.1 slider:true category:"Light attenuation";
	parameter "Constant attenuation" var:constant <- 0.1 min:0.0000001 max:9.0 slider:true category:"Light attenuation";
	
	output {
		layout #split;
		// display using spot lights
		// we set the ambient light to 0 to see better the directional lights (as if we were at night time)
		display SpotLights type: 3d background: rgb(10, 40, 55) {
			
			camera 'default' location: {-87.5648,126.2534,134.3343} target: {50.0,50.0,0.0};
			light #ambient intensity: 0;
			light #default intensity: 0;
			light "1" 
				type: #spot 
				location: {(world.shape.width/2 + distance) * cos(angle) + world.shape.width/2, (world.shape.height/2 + distance) * sin(angle) + world.shape.height / 2, height} 
				direction:{cos(angle + 180), sin(angle + 180), 0} 
				intensity: #red 
				show: true 
				linear_attenuation:linear 
				constant_attenuation:constant 
				quadratic_attenuation: quad 
				dynamic: true;
			
			species GAMAGeometry2D;
		}
	}
}

