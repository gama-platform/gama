/**
* Name: Box2D Pedestrian Flows
* Author: GAMA team
* Description: Demonstrates two continuously generated pedestrian flows crossing in a shared space.
*   Pedestrians are circular Box2D bodies with varied preferred speeds and steer to keep their distance
*   from other people and obstacles. Static obstacles and boundaries constrain movement, while route
*   waypoints guide both flows around a central obstacle. Box2D resolves physical contact.
* Tags: physics_engine, box2d, pedestrian, collision, continuous_flow, obstacle, 2d
*/
model box2d_pedestrian_flows

global parent: physical_world {

	string library <- "box2D";
	float size <- 100.0;
	float step <- 1.0 / 60;
	int max_substeps <- 1;
	point gravity <- {0, 0, 0};
	geometry shape <- rectangle(size, size);

	int max_per_flow <- 40;
	float obstacle_size <- 10.0;
	float min_pedestrian_speed <- 3.5;
	float max_pedestrian_speed <- 5.5;
	float pedestrian_avoidance_distance <- 5.0;
	float obstacle_avoidance_distance <- 8.0;
	float arrival_distance <- 3.0;
	float goal_width <- 50.0;
	geometry horizontal_goal <- line([{size - 5, (size - goal_width) / 2}, {size - 5, (size + goal_width) / 2}]);
	geometry vertical_goal <- line([{(size - goal_width) / 2, size - 5}, {(size + goal_width) / 2, size - 5}]);

	init {
		geometry boundary_x <- box(size + 4, 2, 1);
		geometry boundary_y <- box(2, size + 4, 1);
		create obstacle from: [
			boundary_x at_location {size / 2, 0},
			boundary_x at_location {size / 2, size},
			boundary_y at_location {0, size / 2},
			boundary_y at_location {size, size / 2},
			box(obstacle_size, obstacle_size, 1) at_location {size / 2, size / 2},
			box(7, 3, 1) at_location {size / 4, size / 4},
			box(7, 3, 1) at_location {3 * size / 4, 3 * size / 4}
		];
	}

	reflex generate_flows when: every(45 #cycle) {
		if (length(pedestrian where (each.horizontal_flow)) < max_per_flow) {
			create pedestrian (horizontal_flow: true) {
				location <- {2, rnd(size * 0.35, size * 0.65)};
				color <- rgb(133, 177, 205);
				destination <- {size - 5, location.y};
				detour_side <- flip(0.5) ? -1 : 1;
				first_waypoint <- {size / 2 - obstacle_size / 2 - 2, size / 2 + detour_side * (obstacle_size / 2 + 2)};
				second_waypoint <- {size / 2 + obstacle_size / 2 + 2, first_waypoint.y};
				current_target <- first_waypoint;
				preferred_speed <- rnd(min_pedestrian_speed, max_pedestrian_speed);
			}
		}
		if (length(pedestrian where (!each.horizontal_flow)) < max_per_flow) {
			create pedestrian (horizontal_flow: false) {
				location <- {rnd(size * 0.35, size * 0.65), 2};
				color <- rgb(218, 157, 164);
				destination <- {location.x, size - 5};
				detour_side <- flip(0.5) ? -1 : 1;
				first_waypoint <- {size / 2 + detour_side * (obstacle_size / 2 + 2), size / 2 - obstacle_size / 2 - 2};
				second_waypoint <- {first_waypoint.x, size / 2 + obstacle_size / 2 + 2};
				current_target <- first_waypoint;
				preferred_speed <- rnd(min_pedestrian_speed, max_pedestrian_speed);
			}
		}
	}
}

species obstacle skills: [static_body] {
	aspect default {
		draw shape color: rgb(211, 205, 194) border: rgb(156, 151, 142);
	}
}

species pedestrian skills: [dynamic_body] {
	bool horizontal_flow;
	int detour_side;
	int route_step <- 0;
	int stalled_cycles <- 0;
	int escape_side <- 1;
	float preferred_speed;
	float previous_target_distance <- -1.0;
	point destination;
	point first_waypoint;
	point second_waypoint;
	point current_target;
	float radius <- rnd(0.55, 0.8);
	geometry shape <- circle(radius * 2);
	float mass <- 1.0;
	float friction <- 0.4;
	float restitution <- 0.0;
	float damping <- 0.0;
	float angular_damping <- 1.0;
	rgb color <- horizontal_flow ? rgb(133, 177, 205) : rgb(218, 157, 164);

	reflex follow_route {
		if (route_step = 2) {
			geometry goal <- horizontal_flow ? horizontal_goal : vertical_goal;
			current_target <- (goal closest_points_with location)[0];
		}
		float target_tolerance <- route_step = 2 ? arrival_distance : 1.0;
		if (location distance_to current_target < target_tolerance) {
			if (route_step = 0) {
				route_step <- 1;
				current_target <- second_waypoint;
				previous_target_distance <- -1.0;
				stalled_cycles <- 0;
				escape_side <- detour_side;
			} else if (route_step = 1) {
				route_step <- 2;
				current_target <- destination;
				previous_target_distance <- -1.0;
				stalled_cycles <- 0;
				escape_side <- detour_side;
			} else {
				do die();
				route_step <- 3;
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
					escape_side <- detour_side;
				}
				previous_target_distance <- target_distance;
				desired_direction <- desired_direction / target_distance;
				point avoidance <- {0, 0};

				list<pedestrian> nearby_pedestrians <- (pedestrian at_distance pedestrian_avoidance_distance) where (each != self);
				loop other over: nearby_pedestrians {
					point away <- location - other.location;
					float distance <- norm(away);
					if (distance > 0 and distance < pedestrian_avoidance_distance) {
						float proximity <- (pedestrian_avoidance_distance - distance) / pedestrian_avoidance_distance;
						avoidance <- avoidance + away / distance * proximity;

						point relative <- other.location - location;
						float ahead <- relative.x * desired_direction.x + relative.y * desired_direction.y;
						if (ahead > -radius) {
							point passing_side <- horizontal_flow ? {0, 1} : {-1, 0};
							avoidance <- avoidance + passing_side * (proximity * 2.0);
						}
					}
				}

				list<obstacle> nearby_obstacles <- obstacle where (each distance_to location < obstacle_avoidance_distance);
				loop other over: nearby_obstacles {
					point closest <- (other.shape.contour closest_points_with location)[0];
					point away <- location - closest;
					float distance <- other distance_to location;
					float away_distance <- norm(away);
					if (away_distance > 0 and distance < obstacle_avoidance_distance) {
						avoidance <- avoidance + away / away_distance * ((obstacle_avoidance_distance - distance) / obstacle_avoidance_distance);
					}
				}

				point lateral_avoidance <- avoidance
					- desired_direction * (avoidance.x * desired_direction.x + avoidance.y * desired_direction.y);
				float lateral_strength <- norm(lateral_avoidance);
				if (lateral_strength > 1.0) {
					lateral_avoidance <- lateral_avoidance / lateral_strength;
				}
				point direction <- desired_direction * 3.0 + lateral_avoidance;
				if (stalled_cycles >= 8) {
					if (stalled_cycles mod 16 = 0) {
						escape_side <- -escape_side;
					}
					point escape_direction <- {-desired_direction.y, desired_direction.x};
					direction <- desired_direction * 1.5 + escape_direction * escape_side * 5.0;
				}
				float direction_length <- norm(direction);
				if (direction_length > 0) {
					velocity <- {direction.x / direction_length * preferred_speed, direction.y / direction_length * preferred_speed, 0};
				}
			}
		}
	}

	aspect default {
		draw shape color: color border: #black;
	}
}

experiment "Crossing pedestrian flows" type: gui {
	parameter "Minimum preferred speed" var: min_pedestrian_speed min: 1.0 max: 8.0;
	parameter "Maximum preferred speed" var: max_pedestrian_speed min: 1.0 max: 10.0;
	parameter "Pedestrian avoidance distance" var: pedestrian_avoidance_distance min: 1.0 max: 15.0;
	parameter "Obstacle avoidance distance" var: obstacle_avoidance_distance min: 1.0 max: 20.0;
	parameter "Arrival distance" var: arrival_distance min: 1.0 max: 8.0;

	output {
		display "Box2D pedestrian flows" type: 2d axes: false background: rgb(248, 246, 240) {
			graphics "Crossing routes" {
				draw line([{0, size / 2}, {size, size / 2}]) color: rgb(224, 219, 210) width: 1;
				draw line([{size / 2, 0}, {size / 2, size}]) color: rgb(224, 219, 210) width: 1;
				draw horizontal_goal color: rgb(133, 177, 205) width: 3;
				draw vertical_goal color: rgb(218, 157, 164) width: 3;
			}
			species obstacle;
			species pedestrian;
		}
	}
}
