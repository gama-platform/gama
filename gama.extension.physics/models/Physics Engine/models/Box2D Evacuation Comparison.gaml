/**
* Name: Box2D Evacuation Comparison
* Author: GAMA team
* Description: Compares two Box2D evacuations side by side. In both rooms, a crowd exits through a
*   narrow opening in the right wall; one room is unobstructed and the other has a pillar in front of
*   the exit. Pedestrians steer toward the door while avoiding nearby people and obstacles.
* Tags: physics_engine, box2d, evacuation, pedestrian, bottleneck, obstacle, comparison, 2d
*/
model box2d_evacuation_comparison

global parent: physical_world {

	string library <- "box2D";
	float room_width <- 100.0;
	float room_height <- 70.0;
	float step <- 1.0 / 60;
	int max_substeps <- 1;
	point gravity <- {0, 0, 0};
	geometry shape <- rectangle(room_width, room_height);

	bool pillar_in_front <- false;
	float door_width <- 2.0;
	float pillar_diameter <- 4.0;
	float pillar_distance_from_door <- 10.0;
	int people_per_room <- 400;
	float walking_speed <- 8.0;
	float pedestrian_avoidance_distance <- 3.0;
	float obstacle_avoidance_distance <- 4.0;
	geometry exit_goal <- line([{room_width + 2, room_height / 2 - 0.2},
		{room_width + 2, room_height / 2 + 0.2}]);

	init {

		if (pillar_in_front) {
			create obstacle {
				shape <- circle(pillar_diameter);
				location <- {room_width - pillar_distance_from_door, room_height / 2};
			}
		}

		loop i from: 0 to: people_per_room - 1 {
			float spawn_radius <- rnd(0.45, 0.55);
			point spawn_location <- {0, 0};
			bool valid_spawn <- false;
			loop while: not valid_spawn {
				spawn_location <- {rnd(spawn_radius + 1.0, room_width - spawn_radius - 1.0),
					rnd(spawn_radius + 1.0, room_height - spawn_radius - 1.0)};
				valid_spawn <- empty(pedestrian where
					(each.location distance_to spawn_location < each.radius + spawn_radius + 0.2));
				if (pillar_in_front) {
					point pillar_location <- {room_width - pillar_distance_from_door, room_height / 2};
					valid_spawn <- valid_spawn
						and spawn_location distance_to pillar_location >= pillar_diameter / 2 + spawn_radius + 0.5;
				}
			}
			create pedestrian {
				location <- spawn_location;
				radius <- spawn_radius;
				preferred_speed <- walking_speed * rnd(0.6, 1.4);
				detour_side <- location.y >= room_height / 2 ? 1 : -1;
			}
		}
	}
}

species obstacle skills: [static_body] {
	geometry shape <- box(2, 2, 1);

	aspect default {
		draw shape color: rgb(211, 205, 194) border: rgb(156, 151, 142);
	}
}

species pedestrian skills: [dynamic_body] {
	int detour_side <- 1;
	int stalled_cycles <- 0;
	int cycles_since_side_switch <- 0;
	float preferred_speed;
	float previous_x <- -1.0;
	float previous_y <- -1.0;
	float radius <- rnd(0.45, 0.55);
	geometry shape <- circle(radius *  rnd(0.8, 2.0));
	float mass <- 1.0;
	float friction <- 0.05;
	float restitution <- 0.0;
	float damping <- 0.0;
	float angular_damping <- 1.0;
	rgb color <- rgb(133, 177, 205);

	reflex evacuate {
		if (location.x >= room_width + radius
				and abs(location.y - room_height / 2) <= door_width / 2 - radius) {
			do die();
		} else {
			point exit_target <- (exit_goal closest_points_with location)[0];
			float safe_pillar_radius <- pillar_diameter / 2 + radius + 1.5;
			float pillar_x <- room_width - pillar_distance_from_door;
			bool needs_pillar_bypass <- pillar_in_front
				and abs(location.y - room_height / 2) < safe_pillar_radius
				and location.x < pillar_x + safe_pillar_radius;

			if (previous_x >= 0 and norm({location.x - previous_x, location.y - previous_y}) < 0.025) {
				stalled_cycles <- stalled_cycles + 1;
				cycles_since_side_switch <- cycles_since_side_switch + 1;
			} else {
				stalled_cycles <- 0;
				cycles_since_side_switch <- 0;
			}
			previous_x <- location.x;
			previous_y <- location.y;
			if (cycles_since_side_switch >= 20) {
				detour_side <- -detour_side;
				cycles_since_side_switch <- 0;
			}
			color <- stalled_cycles >= 8 ? rgb(226, 126, 91) : rgb(133, 177, 205);

			point target <- exit_target;
			if (needs_pillar_bypass) {
				target <- {pillar_x, room_height / 2 + detour_side * safe_pillar_radius};
				if (location.x >= pillar_x and abs(location.y - room_height / 2) >= safe_pillar_radius - 0.5) {
					target <- exit_target;
				}
			} else if (stalled_cycles >= 8) {
				target <- {location.x, location.y + detour_side * pedestrian_avoidance_distance};
			}
			point desired_direction <- target - location;
			float target_distance <- norm(desired_direction);
			if (target_distance > 0) {
				desired_direction <- desired_direction / target_distance;
				point avoidance <- {0, 0};

				list<pedestrian> nearby_pedestrians <- (pedestrian at_distance pedestrian_avoidance_distance) where (each != self);
				loop other over: nearby_pedestrians {
					point away <- location - other.location;
					float distance <- norm(away);
					if (distance > 0 and distance < pedestrian_avoidance_distance) {
						avoidance <- avoidance + away / distance
							* ((pedestrian_avoidance_distance - distance) / pedestrian_avoidance_distance);
					}
				}

				list<obstacle> nearby_obstacles <- obstacle
					where (each distance_to location < obstacle_avoidance_distance);
				loop other over: nearby_obstacles {
					point closest <- (other.shape.contour closest_points_with location)[0];
					point away <- location - closest;
					float distance <- other distance_to location;
					float away_distance <- norm(away);
					if (away_distance > 0 and distance < obstacle_avoidance_distance) {
						avoidance <- avoidance + away / away_distance
							* ((obstacle_avoidance_distance - distance) / obstacle_avoidance_distance);
					}
				}

				point lateral_avoidance <- avoidance
					- desired_direction * (avoidance.x * desired_direction.x + avoidance.y * desired_direction.y);
				float lateral_strength <- norm(lateral_avoidance);
				if (lateral_strength > 1.0) {
					lateral_avoidance <- lateral_avoidance / lateral_strength;
				}
				point direction <- desired_direction * 4.0 + lateral_avoidance;
				float direction_length <- norm(direction);
				if (direction_length > 0) {
					velocity <- {direction.x / direction_length * preferred_speed,
						direction.y / direction_length * preferred_speed, 0};
				}
			}
		}
	}

	aspect default {
		draw shape color: color border: rgb(82, 101, 111);
	}
}

experiment "Box2D Evacuation Comparison" type: gui {
	action _init_() {
		bool previous_append_name <- gama.pref_append_simulation_name;
		gama.pref_append_simulation_name <- true;
		create simulation(seed: 1.0, pillar_in_front: false);
		create simulation(seed: 1.0, pillar_in_front: true);
		gama.pref_append_simulation_name <- previous_append_name;
	}

	parameter "Walking speed" var: walking_speed min: 1.0 max: 8.0;
	parameter "People per room" var: people_per_room min: 50 max: 600;
	parameter "Pedestrian avoidance distance" var: pedestrian_avoidance_distance min: 1.0 max: 10.0;
	parameter "Obstacle avoidance distance" var: obstacle_avoidance_distance min: 1.0 max: 15.0;

	output {
		layout #split;
		display "Evacuation" type: 2d axes: false background: rgb(248, 246, 240) {
			graphics "Door" {
				draw line([{room_width + 0.2, room_height / 2},
					{room_width + 1.6, room_height / 2}])
					color: rgb(90, 155, 125) width: 2 end_arrow: 1;
				if (pillar_in_front) {
					draw ("Pillar in front of exit: " + length(pedestrian) + " remaining, "
						+ length(pedestrian where (each.stalled_cycles >= 8)) + " stuck")
						at: {room_width / 2, room_height - 4} anchor: #center
						color: rgb(90, 100, 105) font: font("SansSerif", 14, #bold);
				} else {
					draw ("No pillar: " + length(pedestrian) + " remaining, "
						+ length(pedestrian where (each.stalled_cycles >= 8)) + " stuck")
						at: {room_width / 2, room_height - 4} anchor: #center
						color: rgb(90, 100, 105) font: font("SansSerif", 14, #bold);
				}
			}
			species obstacle;
			species pedestrian;
		}
	}
}
