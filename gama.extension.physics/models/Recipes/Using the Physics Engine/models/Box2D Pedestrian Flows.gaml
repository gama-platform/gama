/**
* Name: Box2D Pedestrian Flows
* Author: Alexis Drogoul (alexis.drogoul@ird.fr)
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
	bool show_velocity_arrows <- true;
	float velocity_arrow_scale <- 1.5;
	float min_drawn_velocity <- 0.05;
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
			box(obstacle_size, obstacle_size, 1) at_location {size / 2, size / 2}
		];
	}

	reflex generate_flows when: every(45 #cycle) {
		if (length(pedestrian where (each.horizontal_flow)) < max_per_flow) {
			create pedestrian (horizontal_flow: true) {
				location <- {2, rnd(size * 0.35, size * 0.65)};
				color <- rgb(133, 177, 205);
				detour_side <- location.y >= size / 2 ? 1 : -1;
				preferred_speed <- rnd(min_pedestrian_speed, max_pedestrian_speed);
			}
		}
		if (length(pedestrian where (!each.horizontal_flow)) < max_per_flow) {
			create pedestrian (horizontal_flow: false) {
				location <- {rnd(size * 0.35, size * 0.65), 2};
				color <- rgb(218, 157, 164);
				detour_side <- location.x >= size / 2 ? 1 : -1;
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
	int stalled_cycles <- 0;
	float preferred_speed;
	point commanded_velocity <- {0, 0};
	float previous_x <- -1.0;
	float previous_y <- -1.0;
	float radius <- rnd(0.55, 0.8);
	geometry shape <- circle(radius * 2);
	float mass <- 1.0;
	float friction <- 0.05;
	float restitution <- 0.0;
	float damping <- 0.0;
	float angular_damping <- 1.0;
	rgb color <- horizontal_flow ? rgb(133, 177, 205) : rgb(218, 157, 164);

	reflex follow_route {
		geometry goal <- horizontal_flow ? horizontal_goal : vertical_goal;
		point goal_target <- (goal closest_points_with location)[0];
		if (location distance_to goal_target < arrival_distance) {
			do die();
		} else {
			float obstacle_near_edge <- size / 2 - obstacle_size / 2 - radius - 1.0;
			float obstacle_far_edge <- size / 2 + obstacle_size / 2 + radius + 1.0;
			float bypass_offset <- obstacle_size / 2 + radius + 1.5;
			float along <- horizontal_flow ? location.x : location.y;
			float across <- horizontal_flow ? location.y : location.x;
			float goal_across <- horizontal_flow ? goal_target.y : goal_target.x;
			float goal_distance_from_center <- abs(goal_across - size / 2);
			bool route_around_obstacle <- goal_distance_from_center < obstacle_size / 2 + radius + 1.0
				and along < obstacle_far_edge;

			if (previous_x >= 0 and norm({location.x - previous_x, location.y - previous_y}) < 0.025) {
				stalled_cycles <- stalled_cycles + 1;
			} else {
				stalled_cycles <- 0;
			}
			previous_x <- location.x;
			previous_y <- location.y;
			if (route_around_obstacle and stalled_cycles >= 20) {
				detour_side <- -detour_side;
				stalled_cycles <- 0;
			}
			float stuck_intensity <- min(1.0, stalled_cycles / 60.0);
			color <- horizontal_flow
				? rgb(133 - 78 * stuck_intensity, 177 - 92 * stuck_intensity, 205 - 65 * stuck_intensity)
				: rgb(218 - 80 * stuck_intensity, 157 - 95 * stuck_intensity, 164 - 75 * stuck_intensity);

			point target <- goal_target;
			if (route_around_obstacle) {
				float bypass_across <- size / 2 + detour_side * bypass_offset;
				if (along < obstacle_near_edge) {
					target <- horizontal_flow ? {obstacle_near_edge, bypass_across}
						: {bypass_across, obstacle_near_edge};
				} else if (along < obstacle_far_edge and abs(across - size / 2) < bypass_offset - 0.5) {
					target <- horizontal_flow ? {location.x, bypass_across}
						: {bypass_across, location.y};
				} else if (along < obstacle_far_edge) {
					target <- horizontal_flow ? {obstacle_far_edge, bypass_across}
						: {bypass_across, obstacle_far_edge};
				}
			}

			point desired_direction <- target - location;
			float target_distance <- norm(desired_direction);
			if (target_distance > 0) {
				desired_direction <- desired_direction / target_distance;
				point avoidance <- {0, 0};
				list<pedestrian> nearby_pedestrians <- (pedestrian at_distance pedestrian_avoidance_distance)
					where (each != self and each != nil);
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
					if (away_distance > 0) {
						avoidance <- avoidance + away / away_distance
							* ((obstacle_avoidance_distance - distance) / obstacle_avoidance_distance);
					}
				}
				point lateral <- avoidance
					- desired_direction * (avoidance.x * desired_direction.x + avoidance.y * desired_direction.y);
				float lateral_strength <- norm(lateral);
				if (lateral_strength > 0.5) { lateral <- lateral / lateral_strength * 0.5; }
				point direction <- desired_direction * 5.0 + lateral;
				float direction_length <- norm(direction);
				if (direction_length > 0) {
					commanded_velocity <- direction / direction_length * preferred_speed;
					velocity <- commanded_velocity;
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
	parameter "Show velocity arrows" var: show_velocity_arrows;
	parameter "Velocity arrow scale" var: velocity_arrow_scale min: 0.5 max: 4.0;

	output {
		display "Box2D pedestrian flows" type: 2d axes: false background: rgb(248, 246, 240) {
			graphics "Crossing routes" {
				draw line([{0, size / 2}, {size, size / 2}]) color: rgb(224, 219, 210) width: 1;
				draw line([{size / 2, 0}, {size / 2, size}]) color: rgb(224, 219, 210) width: 1;
				draw horizontal_goal color: rgb(133, 177, 205) width: 3;
				draw vertical_goal color: rgb(218, 157, 164) width: 3;
			}
			species obstacle;
			species pedestrian {
				draw shape color: color border: #black;
				if (show_velocity_arrows) {
					point current_velocity <- velocity;
					rgb arrow_color <- horizontal_flow ? rgb(64, 104, 132) : rgb(153, 83, 96);
					if (norm(commanded_velocity) > min_drawn_velocity) {
						point command_tip <- location + commanded_velocity * velocity_arrow_scale;
						draw line([location, command_tip]) color: rgb(112, 111, 103) end_arrow: 1 width: 1;
					}
					if (norm(current_velocity) > min_drawn_velocity) {
						point velocity_tip <- location + current_velocity * velocity_arrow_scale;
						draw line([location, velocity_tip]) color: arrow_color end_arrow: 1 width: 2;
						draw string(norm(current_velocity) with_precision 1) at: velocity_tip color: arrow_color;
					} else {
						draw "0.0" at: location + {0, radius + 0.5} color: arrow_color;
					}
				}
			}
		}
	}
}
