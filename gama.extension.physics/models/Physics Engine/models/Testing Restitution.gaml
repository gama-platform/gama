/**
* Name: Testing Restitution
* Author: Alexis Drogoul
* Description: A technical demonstration of the 'restitution' physics parameter and other advanced engine
*   features. Shows: how restitution (bounciness) values from 0 to 1 affect collision behavior; how to
*   display contact points between colliding objects in the 3D view; how to compare the native Bullet
*   library and the Java Bullet binding. Multiple experiments let users isolate each feature.
* Tags: physics_engine, 3d, restitution, collision, contact, bullet, comparison, physical_world
*/


model Restitution

global parent: physical_world {
	string library_name <- use_native ? "Native" : "Java";
	// A small step keeps the engine accurate; the outcome of a collision depends on the restitution of both bodies.
	float step <- 1.0 / 60;
	float wall_restitution <- 1.0 min: 0.0 max: 1.0;
	float ball_restitution <- 0.8 min: 0.0 max: 1.0;
	point ball_impulse <- {100, 100, 0};
	geometry shape <- box(100, 100, 0.001);
	float friction <- 0.0;
	float restitution <- 0.0;
	point ball_contact <- nil;
	int ball_timer <- 0;
	point wall_contact <- nil;
	int wall_timer <- 0;
	int ball_hits <- 0;
	int wall_hits <- 0;

	float kinetic_energy() {
		return ball sum_of (0.5 * each.mass * norm(each.velocity) ^ 2);
	}

	init {
		do register([self]);
		geometry box <- box(103, 3, 10);
		create wall from: [box at_location ({50, 0}), box rotated_by 90 at_location ({0, 50}), box at_location ({50, 100}), box rotated_by 90 at_location ({100, 50})];
		create ball from: [sphere(5) at_location {50, 50, 5}, sphere(5) at_location {20, 20, 5}];
	}

	reflex r1 when: ball_timer > 0 {
		ball_timer <- ball_timer - 1;
		if (ball_timer = 0) { ball_contact <- nil; }
	}

	reflex r2 when: wall_timer > 0 {
		wall_timer <- wall_timer - 1;
		if (wall_timer = 0) { wall_contact <- nil; }
	}
}

species wall skills: [static_body] {
	float restitution <- wall_restitution;
	float friction <- 0.0;

	aspect default {
		draw shape color: rgb(211, 205, 194) border: rgb(156, 151, 142);
	}
}

species ball skills: [dynamic_body] {
	float contact_damping <- 0.0;
	float damping <- 0.0;
	float angular_damping <- 0.1;
	float mass <- 5.0;
	float restitution <- ball_restitution;
	float friction <- 0.0;

	action contact_added_with(agent other) {
		if (other is ball) {
			ball_contact <- location;
			ball_timer <- 20;
			ball_hits <- ball_hits + 1;
		} else if (other is wall) {
			wall_contact <- location;
			wall_timer <- 20;
			wall_hits <- wall_hits + 1;
		}
	}

	reflex manage_location when: location.z < -20 {
		do die();
	}

	aspect default {
		rgb fill <- index = 0 ? rgb(218, 157, 164) : rgb(133, 177, 205);
		rgb edge <- index = 0 ? rgb(153, 83, 96) : rgb(64, 104, 132);
		draw sphere(5) at: location color: fill border: edge;
		// the line shows the spin of the ball
		draw line([location, location + {5 * cos(float(rotation.key)), 5 * sin(float(rotation.key)), 0}]) color: edge width: 1;
		// velocity vector, scaled for display
		draw line([location, location + velocity * 0.25]) color: rgb(112, 111, 103) width: 2 end_arrow: 2;
		draw string(norm(velocity) with_precision 0) at: location + {-3, 10} color: rgb(112, 111, 103) font: font("SansSerif", 7, #bold);
	}
}

experiment "Test Restitution !" type: gui {
	image_file bang <- image_file("../images/bang.png");
	image_file bam <- image_file("../images/bam.png");
	float minimum_cycle_duration <- 1.0 / 60;
	parameter "Impulse" var: ball_impulse;

	user_command "  Move balls" color: rgb(133, 177, 205) {
		ask simulations {
			ask ball { do apply(impulse: ball_impulse); }
		}
	}

	user_command "  Reset balls" color: rgb(218, 157, 164) {
		ask simulations {
			ask ball { do die(); }
			ball_hits <- 0;
			wall_hits <- 0;
			create ball from: [sphere(5) at_location {50, 50, 5}, sphere(5) at_location {20, 20, 5}];
		}
	}

	parameter "Restitution of the walls" var: wall_restitution  { 
		ask simulations {
			ask wall {
				restitution <- wall_restitution;
			}

		}

	}
	parameter "Restitution of the balls" var: ball_restitution  {
		ask simulations {
			ask ball {
				restitution <- ball_restitution;
			}

		}

	}
	
	action _init_() {
		bool prev0 <- gama.pref_experiment_expand_params;
		bool prev1 <- gama.pref_append_simulation_name;
		gama.pref_append_simulation_name <- true;
		gama.pref_experiment_expand_params <- true;
		create simulation(seed: 1.0, use_native: true);
		create simulation(seed: 1.0, use_native: false);
		gama.pref_experiment_expand_params <- prev0;
		gama.pref_append_simulation_name <- prev1;
	}

	output {
		layout #split;
		display "Restitution" type: 3d axes: false background: rgb(248, 246, 240) {
			graphics "Grid" refresh: false {
				loop x from: 0 to: 100 step: 10 {
					draw line([{x, 0}, {x, 100}]) color: rgb(224, 219, 210) width: 1;
				}
				loop y from: 0 to: 100 step: 10 {
					draw line([{0, y}, {100, y}]) color: rgb(224, 219, 210) width: 1;
				}
			}
			species wall refresh: false;
			species ball;
			graphics "Bang" {
				if (ball_contact != nil) {
					draw bang at: ball_contact size: {16, 16};
				}
				if (wall_contact != nil) {
					draw bam at: wall_contact size: {16, 16};
				}
			}
			overlay position: {5, 5} size: {250 #px, 106 #px} background: rgb(248, 246, 240) transparency: 0.1 border: rgb(156, 151, 142) {
				draw "RESTITUTION TEST BENCH" at: {10 #px, 16 #px} color: rgb(112, 111, 103) font: font("SansSerif", 10, #bold);
				draw "Engine : " + library_name + "  (step 1/" + round(1 / step) + ")" at: {10 #px, 34 #px} color: rgb(64, 104, 132);
				draw "Restitution  walls " + wall_restitution + " / balls " + ball_restitution at: {10 #px, 50 #px} color: rgb(112, 111, 103);
				draw "Click: push all balls toward the pointer" at: {10 #px, 66 #px} color: rgb(112, 111, 103) font: font("SansSerif", 8, #plain);
				draw "Ball-ball hits : " + ball_hits + "   Ball-wall hits : " + wall_hits at: {10 #px, 82 #px} color: rgb(112, 111, 103);
				draw "Kinetic energy : " + (world.kinetic_energy() with_precision 0) at: {10 #px, 98 #px} color: rgb(153, 83, 96);
			}
			event "mouse_down" {
				point target <- #user_location;
				ask simulations {
					ask ball {
						point direction <- target - location;
						if norm(direction) > 0 {
							direction <- direction / norm(direction);
							do apply(impulse: {norm(ball_impulse) * direction.x, norm(ball_impulse) * direction.y, 0});
						}
						angular_velocity <- {rnd(10), rnd(10), rnd(10)};
					}
				}
			}
		}
	}
}
