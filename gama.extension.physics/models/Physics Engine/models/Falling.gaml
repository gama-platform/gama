/**
* Name: Falling Objects
* Author: Arnaud Grignard, Alexis Drogoul
* Description: The simplest GAMA physics model: balls fall under gravity onto a floor, bounce, and exchange
*   colors on contact using the contact callback. Users can click any ball to apply an upward impulse.
*   Eventually balls slide off the floor into the void. Demonstrates the 'physics' skill, 'physical_world'
*   inheritance, gravity, restitution, and interactive impulse application.
* Tags: physics_engine, skill, 3d, collision, gravity, restitution, impulse, contact, physical_world
*/

model Falling



global parent: physical_world {
	bool use_native <- true;
	float restitution <- 0.8; // the "bounciness" of the world
	float friction <- 0.2; // the deceleration it imposes on other objects
	int environment_size <- 500; 
	int max_substeps <-0;
	float step <- 0.006; 
	geometry shape <- box(environment_size,environment_size,10);
	
	init {
		//If the world is to be considered as a physical object in the world, it must register itself
		do register([self]);
		create ball number: 100;
	}
} 


species ball skills: [dynamic_body] {
	float radius <- float(rnd(25) + 1);
	geometry shape <- flip(0.3) ? cube(radius) : (flip(0.5) ? sphere(radius) : cone3D(radius, radius));
	point location <- {rnd(environment_size), rnd(environment_size), rnd(environment_size)};
	rgb color <- #grey;
	float restitution <- 0.7;
	float mass <- 1.0;
	float damping <- 0.1;
	float angular_damping <- 0.3;
	float friction <- 0.2;

	// A callback method when a contact is made with another agent. Here, we simply take its color
	action contact_added_with (agent other) {
		if (other is ball) {
			shape <- shape * 1.01;	
			color <- rnd_color(255);		
			do update_body();
		}
	} 

	reflex manage_location when: location.z < -20 {
		do die();
	}

	aspect default {
		draw shape color: color rotate: float(rotation.key);
		// We can also draw the bounding box of the agent in the physical world
//	draw aabb color: #black wireframe: true;
	}

}

experiment Display type: gui {
	output {	
		display Falling  type: 3d background:rgb(128,128,128) axes:false{
			camera 'default' location: {177.8131,883.5764,615.7961} target: {250.0,250.0,0.0};
			graphics World refresh: false{
				 draw shape color: #white; 
			}
			event #mouse_down {
				ball target <- ball with_min_of(each distance_to #user_location);
				ask target {
					do apply(impulse: {rnd(10)-5,rnd(10)-5,50}); // vertical, with some random side moves
				}
			}
		    species ball;			
		}

	}
}

