/**
* Name: Circle
* Author: Gama Development Team
* Description: Agents approach a target ring while separating from nearby neighbors. Radial attraction
*   decreases near the ring, allowing the formation to settle instead of oscillating across its radius.
*   Separation combines all neighbors within the interaction range; stronger repulsion increases its effect.
*   It acts tangentially to distribute agents around the ring without pushing them away from its radius.
*   Population, radius, attraction, repulsion, range, speed and agent size can be changed while running.
*   Environment dimensions take effect when the simulation is reinitialized.
* Tags: skill, self_organization, emergence, circle, formation, collective
*/
model circle_model

global {
	int number_of_agents <- 300 min: 1 max: 1000;
	float radius_of_circle <- 15000.0 min: 10.0 max: 20000.0;
	float repulsion_strength <- 50.0 min: 0.0 max: 100.0;
	int width_and_height_of_environment <- 40000 min: 100;
	float range_of_agents <- 2000.0 min: 1.0 max: 5000.0;
	float speed_of_agents <- 100.0 min: 0.1 max: 200.0;
	float size_of_agents <- 100.0 min: 10.0 max: 400.0;
	float attraction_strength <- 1.0 min: 0.1 max: 3.0;
	bool show_guide <- true;
	bool show_neighbors <- false;
	point center <- {width_and_height_of_environment / 2.0, width_and_height_of_environment / 2.0};
	geometry shape <- square(width_and_height_of_environment);
	rgb backdrop <- rgb(18, 26, 40);
	rgb teal <- rgb(83, 218, 196);
	rgb amber <- rgb(244, 179, 91);
	rgb lettering <- rgb(219, 231, 240);
	float mean_error;
	float settled_fraction;

	init {
		create cell number: number_of_agents;
		do measure_formation();
	}

	action measure_formation() {
		mean_error <- mean(cell collect (abs((each distance_to center) - radius_of_circle)));
		settled_fraction <- (cell count (abs((each distance_to center) - radius_of_circle)
			< max(size_of_agents * 2.0, radius_of_circle * 0.02))) / float(length(cell));
	}

	reflex evolve {
		ask cell { do plan_motion(); }
		ask cell { location <- next_location; }
		do measure_formation();
	}
}

species cell skills: [moving] schedules: [] {
	point next_location;

	// Compute from the same population snapshot, then move in a separate phase.
	action plan_motion() {
		float distance <- self distance_to center;
		point radial <- distance > 0.0 ? (center - location) / distance : {cos(heading), sin(heading)};
		float error <- distance - radius_of_circle;
		point attraction <- radial * max(-1.0, min(1.0, error / max(1.0, range_of_agents)))
			* attraction_strength;
		point separation <- {0, 0};
		list<cell> nearby <- (cell at_distance range_of_agents) - self;
		loop other over: nearby {
			float gap <- self distance_to other;
			if (gap > 0.0) {
				separation <- separation + (location - other.location) / gap
					* (1.0 - gap / range_of_agents);
			} else {
				separation <- separation + {cos(heading), sin(heading)};
			}
		}
		float radial_component <- separation.x * radial.x + separation.y * radial.y;
		separation <- separation - radial * radial_component;
		point velocity <- attraction + separation * (repulsion_strength / 50.0);
		float magnitude <- norm(velocity);
		if (magnitude > 1.0) { velocity <- velocity / magnitude; }
		next_location <- location + velocity * speed_of_agents;
		next_location <- {max(0.0, min(float(width_and_height_of_environment), next_location.x)),
			max(0.0, min(float(width_and_height_of_environment), next_location.y))};
	}

	aspect default {
		float error <- abs((self distance_to center) - radius_of_circle);
		rgb tint <- blend(amber, teal, min(1.0, error / max(1.0, radius_of_circle * 0.25)));
		if (show_neighbors and self = first(cell)) {
			draw circle(range_of_agents) color: rgb(teal, 0.06) border: rgb(teal, 0.35);
			loop other over: (cell at_distance range_of_agents) - self {
				draw line([location, other.location]) color: rgb(teal, 0.3);
			}
		}
		draw circle(size_of_agents * 2.2) color: rgb(tint, 0.09);
		draw circle(size_of_agents) color: tint border: rgb(lettering, 0.65);
	}
}

experiment main type: gui {
	parameter "Number of Agents" var: number_of_agents category: "Formation" {
		if (number_of_agents > length(cell)) {
			create cell number: number_of_agents - length(cell);
		} else if (number_of_agents < length(cell)) {
			ask (length(cell) - number_of_agents) among cell { do die(); }
		}
		do measure_formation();
	}
	parameter "Radius of Circle" var: radius_of_circle unit: "m" category: "Formation";
	parameter "Attraction to Ring" var: attraction_strength category: "Dynamics";
	parameter "Strength of Repulsion" var: repulsion_strength category: "Dynamics";
	parameter "Range of Agents" var: range_of_agents unit: "m" category: "Dynamics";
	parameter "Speed of Agents" var: speed_of_agents category: "Dynamics";
	parameter "Size of Agents" var: size_of_agents category: "Appearance";
	parameter "Show Target Ring" var: show_guide category: "Appearance";
	parameter "Show One Agent's Neighbors" var: show_neighbors category: "Appearance";
	parameter "Dimensions (restart required)" var: width_and_height_of_environment category: "Environment";

	float minimum_cycle_duration <- 0.03#s;
	output {
		monitor "Mean distance from ring (m)" value: with_precision(mean_error, 1);
		monitor "Agents on ring (%)" value: with_precision(settled_fraction * 100.0, 1);
		display Circle type: 2d background: backdrop {
			graphics guide {
				if (show_guide) {
					draw circle(radius_of_circle) at: center color: rgb(lettering, 0.3)
						wireframe: true width: 1.5;
					draw circle(width_and_height_of_environment * 0.003) at: center color: rgb(lettering, 0.5);
				}
			}
			species cell;
			overlay coordinates: #screen position: {16, 16} size: {278, 96} background: backdrop rounded: true {
				draw "CIRCULAR FORMATION" at: {14#px, 22#px} color: lettering font: font("Helvetica", 12, #bold);
				draw "Teal: on ring / Amber: approaching" at: {14#px, 48#px}
					color: lettering font: font("Helvetica", 11);
				draw "On ring: " + string(with_precision(settled_fraction * 100.0, 1)) + "%"
					at: {14#px, 74#px} color: teal font: font("Helvetica", 12);
			}
		}
	}
}
