/**
* Name: Corridor - Multi-Level Architecture
* Author: Vo Duc An, Ngoc Anh, Jean-Daniel Zucker, Alexis Drogoul
* Description: Demonstrates the multi-level 'capture' and 'release' operators for managing pedestrian
*   transit through a corridor. When pedestrians enter the corridor zone they are captured — their species
*   changes from 'pedestrian' to 'captured_pedestrian' and they become invisible. After a fixed dwell time,
*   the corridor agent releases them back as 'pedestrian' at the exit. This pattern models areas where
*   individual movement details are unnecessary (e.g., buildings, transport vehicles) and only entry/exit
*   timing matters.
*   A clickable status light switches capture on and off. Ghost markers indicate captured agents
*   at their entry positions, not their estimated positions inside the corridor.
*   Pedestrian colors distinguish arrival cohorts; the panel reports the current step duration.
* Tags: multi_level, agent_movement, capture, release, pedestrian, corridor
*/
model corridor

global {
//Capture pedestrians parameter to define if wall will capture pedestrians
	bool capture_pedestrians <- false;
	rgb backdrop <- rgb(18, 26, 40);
	rgb floor_color <- rgb(28, 40, 56);
	rgb lettering <- rgb(220, 231, 242);
	rgb accent <- rgb(83, 218, 196);
	rgb amber <- rgb(244, 191, 100);
	int environment_width init: 8000;
	int environment_height init: 3000;
	geometry shape <- rectangle(environment_width, environment_height);

	//Pedestrians parameters
	rgb pedestrian_color <- accent;
	float pedestrian_speed <- 10.0;

	//Wall parameters
	float corridor_width <- environment_width / 1.5;
	int corridor_wall_height <- 800;
	geometry corridor_wall_0_shape <- rectangle({corridor_width, corridor_wall_height}) at_location {environment_width / 2, corridor_wall_height / 2};
	geometry corridor_wall_1_shape <- rectangle({corridor_width, corridor_wall_height}) at_location {environment_width / 2, environment_height - (corridor_wall_height / 2)};
	

	//Corridor parameters
	float corridor_left_bounds <- (location.x - (corridor_width / 2));
	float corridor_right_bounds <- (location.x + (corridor_width / 2));

	init {
		create corridor;
	}

	reflex change_color when: every(200 #cycle) {
		pedestrian_color <- rnd_color(255);
	}

	reflex generate_pedestrians when: every(4 #cycle) {
		create pedestrian(color:pedestrian_color) number: 30   {
			do init_location({0, rnd(environment_height)});
		}
	}
}

//Species pedestrian which will move from one side of the experiment to another and destroy itself once the other side is reached
species pedestrian skills: [moving] topology: (topology(shape - (corridor_wall_0_shape + corridor_wall_1_shape))) {
	point target_location;
	rgb color;

	action init_location (point loc) {
		location <- loc;
		target_location <- {environment_width, loc.y};
		speed <- rnd(pedestrian_speed - 5) + 5.0;
	}
	
	
	reflex change_speed when: every(rnd(200) #cycle) {
			speed <- rnd(pedestrian_speed - 5) + 5.0;
	}

	//Reflex to make the agent move to its target_location
	reflex move {
		point previous_location <- location;

		if (location.y < corridor_wall_height) and (location.x <= (environment_width / 2)) {
			do move (heading: self towards {(environment_width / 2) - (corridor_width / 2), corridor_wall_height});
		} else if (location.y > environment_height - corridor_wall_height) and (location.x <= (environment_width / 2)) {
			do move (heading: self towards {(environment_width / 2) - (corridor_width / 2), environment_height - corridor_wall_height});
		} else {
			do move (heading: self towards target_location);
		}
		if (location.x = previous_location.x) { // No move detected
			do move (heading: self towards {environment_width, world.shape.location.y});
		}
	}

	reflex arrived when: location.x >= target_location.x {
		do die();
	}

}

//Species which represents the corridor
species corridor {
	geometry shape <- ((rectangle({corridor_width, environment_height})) at_location world.location) - (corridor_wall_0_shape + corridor_wall_1_shape);

	//Subspecies for the multi-level architectures : captured pedestrians in this case
	species captured_pedestrian parent: pedestrian schedules: [] {
		float release_time;
	}

	//Reflex to capture pedestrians if the parameter is checked
	reflex aggregate when: capture_pedestrians {
	//If we have pedestrians inside the corridor, we capture them
	//We update the time during which a pedestrian is captured according to the time the pedestrian
	// should need to pass through the corridor if it wasn't captured
		capture (pedestrian where (p: p.location.x between (corridor_left_bounds, corridor_right_bounds))) as: captured_pedestrian {
			release_time <- time + ((corridor_width - (location.x - ((environment_width / 2) - (corridor_width / 2)))) / (pedestrian_speed - 2.5));
		} }

		//Reflex to release pedestrians which have already passed enough time in the corridor
	// which means if they weren't captured by the corridor, they would have finish passing through it
	reflex disaggregate {
		list tobe_released_pedestrians <- captured_pedestrian where (time >= each.release_time);
		if !(empty(tobe_released_pedestrians)) {
			release tobe_released_pedestrians as: pedestrian in: world {
				location <- {((environment_width / 2) + (corridor_width / 2)), (location).y};
			}

		}
	}
}

experiment "Corridor" type: gui autorun: true {
	point button_location;
	bool button_hover;
	geometry corridor_wall_0_display <- rectangle({corridor_width-30, corridor_wall_height-30}) at_location {environment_width / 2, corridor_wall_height / 2};
	geometry corridor_wall_1_display <- rectangle({corridor_width-30, corridor_wall_height-30}) at_location {environment_width / 2, environment_height - (corridor_wall_height / 2)};
	
	init {
		button_location <- {simulation.environment_width / 2 - 600,
			simulation.environment_height - simulation.corridor_wall_height / 2};
	}
	output {
		display defaut_display type: 3d background: backdrop axes: false {
			camera 'default' location: {4000.0,1500.1331,7628.0284} target: {4000.0,1500.0,0.0};
			graphics back {
				draw corridor_wall_0_display color: floor_color border: rgb(68, 87, 109);
				draw corridor_wall_1_display color: floor_color border: rgb(68, 87, 109);
			}

			species corridor {
				draw shape color: rgb(accent, 0.06) border: rgb(accent, 0.4);
				draw line([{corridor_left_bounds, environment_height / 2},
					{corridor_right_bounds, environment_height / 2}]) color: rgb(accent, 0.3) end_arrow: 90;
			}
			
			agents "Captured" value: list(corridor(0).captured_pedestrian) {
				draw circle(16) color: rgb(amber, 0.25) border: rgb(amber, 0.6);
			}

			species pedestrian { 
				draw circle(13) color: blend(color, lettering, 0.75) border: rgb(lettering, 0.4);
			}

			graphics front {
				draw "ENTRY" at: {corridor_left_bounds - 180, environment_height / 2}
					anchor: #right_center color: lettering font: font("Helvetica", 11);
				draw "EXIT" at: {corridor_right_bounds + 180, environment_height / 2}
					anchor: #left_center color: lettering font: font("Helvetica", 11);
			}

			graphics button {
				draw circle(65) color: button_hover ? lettering : rgb(68, 87, 109) at: button_location;
				draw circle(48) color: capture_pedestrians ? accent : amber at: button_location;
				draw (capture_pedestrians ? "CAPTURE ON" : "CAPTURE OFF") at: button_location + {120, 0}
					anchor: #left_center color: lettering font: font("Helvetica", 12, #bold);
			}

			event #mouse_down {
				if (button_location distance_to #user_location <= 65) {
					capture_pedestrians <- !capture_pedestrians;
				}
			}
			event #mouse_move {
				button_hover <- (button_location distance_to #user_location <= 65);
			}
			overlay coordinates: #screen position: {12, 8} size: {440, 70} background: backdrop rounded: true {
				draw "CORRIDOR / CAPTURE & RELEASE" at: {12#px, 19#px} color: lettering font: font("Helvetica", 12, #bold);
				draw "Walking: " + length(pedestrian) + "   Captured: " + length(corridor(0).captured_pedestrian)
					+ "   Step: " + (duration copy_between (0, 4)) + " ms"
					at: {12#px, 40#px} color: lettering font: font("Helvetica", 11);
				draw "Amber ghosts: entry positions / Click status light to toggle capture"
					at: {12#px, 59#px} color: amber font: font("Helvetica", 10);
			}
		}
	}

}
