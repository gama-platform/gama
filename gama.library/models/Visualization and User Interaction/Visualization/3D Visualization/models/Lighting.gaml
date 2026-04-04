/**
* Name: Lighting
* Author: Arnaud Grignard, Julien Mazars
* Description: Shows how to define and animate dynamic lights in a 3D GAMA display. Two types of light are
*   demonstrated: point lights (omnidirectional, like a light bulb) and spot lights (directional cone of light,
*   like a flashlight). Moving light agents carry their light sources and GAMA updates the shading on all
*   surfaces in real time as lights move. Parameters control intensity, color, and attenuation. This model is
*   the primary reference for adding dynamic illumination to 3D GAMA simulations.
* Tags: 3d, light, point_light, spot_light, dynamic, visualization, display, shading
*/
model lighting

global {

	init {
		create lightMoving number: 2;
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

species lightMoving skills: [moving] {

	reflex update {
		do wander amplitude: 180.0;
	}

}

experiment Display type: gui autorun: true {
	float minimum_cycle_duration <- 0.01;
	output {
		layout #split;
		// display using spot lights
		// we set the ambient light to 0 to see better the directional lights (as if we were at night time)
		display SpotLights type: 3d background: rgb(10, 40, 55) {
		// we define 3 lights : the blue and red turn around the scene, changing their orientation so that the scene is always lightened
		// the green light does not change its position, but the angle of the spot changes
			camera 'default' location: {-87.5648,126.2534,134.3343} target: {50.0,50.0,0.0};
			light #ambient intensity: 0;
			light #default intensity: 0;
			light "1" type: #spot location: {world.shape.width * cos(cycle) + world.shape.width / 2, world.shape.height * sin(cycle) + world.shape.height / 2, 20} direction:
			{cos(cycle + 180), sin(cycle + 180), -1} intensity: #red show: true quadratic_attenuation: 0.0001 dynamic: true;
			light "2" type: #spot location: {world.shape.width * cos(cycle + 180) + world.shape.width / 2, world.shape.height * sin(cycle + 180) + world.shape.height / 2, 20} direction:
			{cos(cycle), sin(cycle), -1} intensity: #blue show: true quadratic_attenuation: 0.0001 dynamic: true;
			light "3" type: #spot location: {world.shape.width / 2, world.shape.height / 2, world.shape.width / 2} direction: {0, 0, -1} intensity: #green show: true angle:
			30 * (1 + cos(2 * cycle)) quadratic_attenuation: 0.0001 dynamic: true;
			species GAMAGeometry2D;
		}
		// display using point lights
		// we set the ambient light to 0 to see better the directional lights (as if we were at night time)
		display PointLights type: 3d background: rgb(10, 40, 55) {
		// we define 3 lights : the blue and red turn around the scene
		// the green light change its location up and down, we can see the quadratic_attenuation effect : the farther the light is, the less power it has			
			light #ambient intensity: 0;
			light #default intensity: 0;
			light "1" type: #point location: {world.shape.width * cos(cycle) + world.shape.width / 2, world.shape.height * sin(cycle) + world.shape.height / 2, 20} intensity: #red show: true
			quadratic_attenuation: 0.0001 dynamic: true;
			light "2" type: #point location: {world.shape.width * cos(cycle + 180) + world.shape.width / 2, world.shape.height * sin(cycle + 180) + world.shape.height / 2, 20} intensity:
			#blue show: true quadratic_attenuation: 0.0001 dynamic: true;
			light "3" type: #point location: {world.shape.width / 2, world.shape.height / 2, world.shape.width * cos(cycle)} intensity: #green show: true quadratic_attenuation: 0.0001
			dynamic: true;
			species GAMAGeometry2D aspect: default;
		}
		// display using direction lights
		// we set the ambient light to 0 to see better the directional lights (as if we were at night time)
		display DirectionLights type: 3d background: rgb(10, 40, 55) {
		// we define 3 lights : the blue and red change their direction
		// the green light change its intensity
			light #ambient intensity: 0;
			light #default intensity: 0;
			light "1" type: #direction direction: {cos(cycle + 180), sin(cycle + 180), -1} intensity: #red show: true dynamic: true;
			light "2" type: #direction direction: {cos(cycle), sin(cycle), -1} intensity: #blue show: true dynamic: true;
			light "3" type: #direction direction: {0, 0, -1} intensity: rgb(0, 255 * (1 + cos(cycle)), 0) show: true dynamic: true;
			species GAMAGeometry2D aspect: default;
		}

	}

}