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
	float door_width <- 10.0;
	float pillar_size <- 6.0;
	float pillar_distance_from_door <- 9.0;
	float walking_speed <- 4.0;
	float pedestrian_avoidance_distance <- 3.5;
	float obstacle_avoidance_distance <- 6.0;

	init {
		geometry horizontal_wall <- box(room_width + 2, 2, 1);
		geometry vertical_wall <- box(2, room_height + 2, 1);
		geometry half_door_wall <- box(2, (room_height - door_width) / 2, 1);
		create obstacle from: [
			horizontal_wall at_location {room_width / 2, 0},
			horizontal_wall at_location {room_width / 2, room_height},
			vertical_wall at_location {0, room_height / 2},
			half_door_wall at_location {room_width, (room_height - door_width) / 4},
			half_door_wall at_location {room_width, room_height - (room_height - door_width) / 4}
		];

		if (pillar_in_front) {
			create obstacle {
				shape <- box(pillar_size, pillar_size, 1);
				location <- {room_width - pillar_distance_from_door, room_height / 2};
			}
		}

		loop x from: 10 to: 26 step: 4 {
			loop y from: 11 to: 59 step: 6 {
				create pedestrian {
					location <- {x, y};
					pillar_side <- (int(self) mod 2) = 0 ? -1 : 1;
					exit_target <- {room_width + 6, room_height / 2};
					if (pillar_in_front) {
						route_step <- 0;
						first_waypoint <- {room_width - pillar_distance_from_door - pillar_size / 2 - 4,
							room_height / 2 + pillar_side * (pillar_size / 2 + 3)};
						second_waypoint <- {room_width - pillar_distance_from_door + pillar_size / 2 + 4,
							first_waypoint.y};
						current_target <- first_waypoint;
					} else {
						route_step <- 2;
						current_target <- exit_target;
					}
					preferred_speed <- walking_speed;
				}
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
	int pillar_side;
	int route_step <- 2;
	int stalled_cycles <- 0;
	int escape_side <- 1;
	float preferred_speed;
	float previous_target_distance <- -1.0;
	point exit_target;
	point first_waypoint;
	point second_waypoint;
	point current_target;
	float radius <- 0.7;
	geometry shape <- circle(radius * 2);
	float mass <- 1.0;
	float friction <- 0.3;
	float restitution <- 0.0;
	float damping <- 0.0;
	float angular_damping <- 1.0;
	rgb color <- rgb(133, 177, 205);

	reflex evacuate {
		if (route_step = 2 and location.x >= room_width and abs(location.y - room_height / 2) <= door_width / 2) {
			do die();
			route_step <- 3;
		} else if (location distance_to current_target < 1.0) {
			if (route_step = 0) {
				route_step <- 1;
				current_target <- second_waypoint;
				previous_target_distance <- -1.0;
				stalled_cycles <- 0;
				escape_side <- pillar_side;
			} else if (route_step = 1) {
				route_step <- 2;
				current_target <- exit_target;
				previous_target_distance <- -1.0;
				stalled_cycles <- 0;
				escape_side <- pillar_side;
			}
		}

		if (route_step < 3) {
			point desired_direction <- current_target - location;
			float target_distance <- norm(desired_direction);
			if (target_distance > 0) {
				if (previous_target_distance >= 0 and previous_target_distance - target_distance < 0.02) {
					stalled_cycles <- stalled_cycles + 1;
				} else {
					stalled_cycles <- 0;
					escape_side <- pillar_side;
				}
				previous_target_distance <- target_distance;
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

				list<obstacle> nearby_obstacles <- obstacle where (each distance_to location < obstacle_avoidance_distance);
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

				point direction <- desired_direction + avoidance * 1.5;
				if (stalled_cycles >= 8) {
					if (stalled_cycles mod 16 = 0) {
						escape_side <- -escape_side;
					}
					point escape_direction <- {-desired_direction.y, desired_direction.x};
					direction <- desired_direction * 1.5 + escape_direction * escape_side * 5.0;
				}
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
	parameter "Pedestrian avoidance distance" var: pedestrian_avoidance_distance min: 1.0 max: 10.0;
	parameter "Obstacle avoidance distance" var: obstacle_avoidance_distance min: 1.0 max: 15.0;

	output {
		layout #split;
		display "Evacuation" type: 2d axes: false background: rgb(248, 246, 240) {
			graphics "Door" {
				draw line([{room_width, (room_height - door_width) / 2}, {room_width, (room_height + door_width) / 2}])
					color: rgb(90, 155, 125) width: 3;
				if (pillar_in_front) {
					draw ("Pillar in front of exit: " + length(pedestrian) + " remaining")
						at: {room_width / 2, room_height - 4}
						color: rgb(90, 100, 105) font: font("SansSerif", 14, #bold);
				} else {
					draw ("No pillar: " + length(pedestrian) + " remaining")
						at: {room_width / 2, room_height - 4}
						color: rgb(90, 100, 105) font: font("SansSerif", 14, #bold);
				}
			}
			species obstacle;
			species pedestrian;
		}
	}
}
