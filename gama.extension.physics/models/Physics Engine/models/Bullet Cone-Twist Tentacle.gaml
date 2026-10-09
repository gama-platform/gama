/**
 * A tentacle made of boxes linked by cone-twist joints (ragdoll-style), anchored to a fixed base
 * with a fixed joint, and swung around by an initial push. Runs on native Bullet when available.
 */
model Bullet_ConeTwist_Tentacle

global parent: physical_world {
	string library <- "bullet";
	bool use_native <- true;
	float step <- 1.0 / 60;
	int max_substeps <- 4;
	int segment_count <- 8;
	float segment_length <- 10.0;
	geometry shape <- box(160, 80, 80);
	point gravity <- {0, 0, -9.81};
	int selected_segment <- 0;
	float push_strength <- 3.0;
	point last_push_direction <- {0, 0, 0};
	int last_push_cycle <- -1000;

	action push (point direction) {
		last_push_direction <- direction;
		last_push_cycle <- cycle;
		ask tentacle_segment[selected_segment] {
			do apply(impulse: direction * push_strength);
		}
	}

	point grab_point <- nil;

	// The closest segment to the click becomes the selected one
	action grab (point p) {
		grab_point <- p;
		selected_segment <- (tentacle_segment with_min_of (each.location distance_to p)).index;
	}

	// Dragging pushes the selected segment from the press point towards the release point
	action release (point p) {
		if grab_point != nil {
			point drag <- {p.x - grab_point.x, p.y - grab_point.y, 0};
			if norm(drag) > 0.5 {
				do push(drag / norm(drag) * min(norm(drag) / 10, 3.0));
			}
		}
		grab_point <- nil;
	}

	float tip_height() {
		return tentacle_segment[segment_count - 1].location.z;
	}

	init {
		do register([self]);
		create tentacle_base {
			location <- {30, 40, 57};
		}
		create tentacle_segment number: segment_count;
		ask tentacle_segment {
			location <- {30 + (index + 1) * segment_length, 40, 60};
		}
		joint root <- create_fixed_joint(tentacle_base[0], tentacle_segment[0], {30 + segment_length / 2, 40, 62});
		loop i from: 0 to: segment_count - 2 {
			point anchor <- {30 + (i + 1.5) * segment_length, 40, 62};
			joint j <- create_cone_twist_joint(tentacle_segment[i], tentacle_segment[i + 1], anchor, {1, 0, 0}, 0.5, 0.3);
		}
	}
}

species tentacle_base skills: [static_body] {
	geometry shape <- box(10, 10, 10);

	aspect default {
		draw shape color: rgb(211, 205, 194) border: rgb(156, 151, 142);
		draw "FIXED BASE" at: location + {-8, 0, 8} color: rgb(112, 111, 103) font: font("SansSerif", 8, #bold) perspective: false;
	}
}

species tentacle_segment skills: [dynamic_body] {
	geometry shape <- box(10, 4, 4);
	float mass <- 0.5;
	float friction <- 0.4;
	float damping <- 0.5;
	float angular_damping <- 0.9;

	aspect default {
		bool picked <- index = selected_segment;
		draw shape color: picked ? rgb(218, 157, 164) : rgb(133, 177, 205) border: picked ? rgb(153, 83, 96) : rgb(64, 104, 132) rotate: rotation;
		draw "S" + index at: location + {0, 0, 5} color: rgb(112, 111, 103) font: font("SansSerif", 7, #bold) perspective: false;
		// weight vector (mass x g), scaled for display
		draw line([location, location + {0, 0, -mass * 9.81}]) color: rgb(153, 83, 96) end_arrow: 1.2;
		if picked and cycle - last_push_cycle < 40 {
			draw line([location - last_push_direction * 6, location]) color: rgb(64, 104, 132) width: 3 end_arrow: 2;
		}
	}
}

experiment Tentacle type: gui {
	float minimum_cycle_duration <- 10 #ms;
	parameter "Selected segment" var: selected_segment min: 0 max: 7 category: "Interaction";
	parameter "Push strength (impulse)" var: push_strength min: 0.5 max: 10.0 category: "Interaction";
	user_command "Push forward (+x)" category: "Interaction" color: rgb(133, 177, 205) {
		ask world {
			do push({1, 0, 0});
		}
	}

	user_command "Push backward (-x)" category: "Interaction" color: rgb(133, 177, 205) {
		ask world {
			do push({-1, 0, 0});
		}
	}

	user_command "Push sideways (+y)" category: "Interaction" color: rgb(133, 177, 205) {
		ask world {
			do push({0, 1, 0});
		}
	}

	user_command "Push up (+z)" category: "Interaction" color: rgb(218, 157, 164) {
		ask world {
			do push({0, 0, 1});
		}
	}

	output {
		display Tentacle type: 3d axes: false background: rgb(248, 246, 240) {
			graphics "Floor grid" {
				loop x from: 0 to: 160 step: 20 {
					draw line([{x, 0, 0}, {x, 80, 0}]) color: rgb(224, 219, 210);
				}
				loop y from: 0 to: 80 step: 20 {
					draw line([{0, y, 0}, {160, y, 0}]) color: rgb(224, 219, 210);
				}
			}

			species tentacle_base;
			species tentacle_segment;
			graphics "Joints" {
				// joints sit half-way between two linked bodies
				point root <- (tentacle_base[0].location + tentacle_segment[0].location) / 2;
				draw sphere(1.6) at: root color: rgb(153, 83, 96);
				draw "FIXED" at: root + {0, 0, 4} color: rgb(153, 83, 96) font: font("SansSerif", 7, #bold) perspective: false;
				loop i from: 0 to: segment_count - 2 {
					point jp <- (tentacle_segment[i].location + tentacle_segment[i + 1].location) / 2;
					draw sphere(1.3) at: jp color: rgb(112, 111, 103);
					draw circle(3.5) at: jp color: #transparent border: rgb(156, 151, 142);
				}
			}

			event #mouse_down {
				ask world {
					do grab(#user_location);
				}
			}

			event #mouse_up {
				ask world {
					do release(#user_location);
				}
			}
			
			camera 'default' location: {41.4967,256.6061,54.4653} target: {71.8988,40.2835,0.0};

			overlay position: {5, 5} size: {290 #px, 130 #px} background: rgb(248, 246, 240) transparency: 0.1 border: rgb(156, 151, 142) {
				draw "CONE-TWIST TENTACLE" at: {10 #px, 16 #px} color: rgb(112, 111, 103) font: font("SansSerif", 10, #bold);
				draw "Selected segment : S" + selected_segment at: {10 #px, 34 #px} color: rgb(153, 83, 96);
				draw "Segment speed : " + (norm(tentacle_segment[selected_segment].velocity) with_precision 1) + " u/s" at: {10 #px, 50 #px} color: rgb(64, 104, 132);
				draw "Tip height : " + (world.tip_height() with_precision 1) at: {10 #px, 66 #px} color: rgb(112, 111, 103);
				draw "Push impulse : " + push_strength at: {10 #px, 82 #px} color: rgb(112, 111, 103);
				draw "Mouse: press near a segment, drag to push" at: {10 #px, 100 #px} color: rgb(112, 111, 103) font: font("SansSerif", 12, #plain);
				draw "Red arrow = weight, blue arrow = last push" at: {10 #px, 116 #px} color: rgb(112, 111, 103) font: font("SansSerif", 12, #plain);
			}
		}
	}
}
