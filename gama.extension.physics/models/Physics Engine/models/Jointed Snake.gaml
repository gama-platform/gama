/**
 * A simple articulated chain driven by hinge motors.
 */
model Jointed_Snake

global parent: physical_world {
	string library <- "bullet";
	bool use_native <- true;
	float step <- 1.0 / 60;
	int max_substeps <- 4;
	int segment_count <- 8;
	float segment_length <- 16.0;
	list<joint> joints <- [];
	geometry shape <- box(160, 80, 40);
	point gravity <- {0, 0, 0};

	init {
		do register([self]);
		create snake_segment number: segment_count;
		ask snake_segment {
			location <- {24 + index * segment_length, 40, 20};
		}
		loop i from: 0 to: segment_count - 2 {
			point anchor <- {24 + (i + 1) * segment_length - segment_length / 2, 40, 20};
			joints << create_hinge_joint_with_axis(snake_segment[i], snake_segment[i + 1], anchor, {0, 0, 1},
					-0.9, 0.9, 0.0, 60.0);
		}
	}

	float thrust <- 6.0;
	float wave_amplitude <- 4.0;
	float wave_speed <- 6.0;

	// The head (segment 0) is pushed along its heading, the body follows; near the borders the push goes back to the centre
	reflex swim {
		point head_pos <- snake_segment[0].location;
		point heading <- head_pos - snake_segment[1].location;
		if head_pos.x < 30 or head_pos.x > 130 or head_pos.y < 20 or head_pos.y > 60 {
			heading <- {80, 40, 20} - head_pos;
		}
		heading <- {heading.x, heading.y, 0};
		if norm(heading) > 0 {
			ask snake_segment[0] {
				do apply(force: heading / norm(heading) * myself.thrust);
			}
		}
	}

	reflex drive_joints {
		loop i from: 0 to: length(joints) - 1 {
			joints[i] <- joints[i] with_motor_speed (wave_amplitude * sin(cycle * wave_speed + i * 50));
		}
	}
}

species snake_segment skills: [dynamic_body] {
	geometry shape <- box(16, 5, 5);
	float mass <- 1.0;
	float friction <- 0.4;
	float damping <- 0.3;
	float angular_damping <- 0.2;

	aspect default {
		bool is_head <- index = 0;
		bool is_tail <- index = segment_count - 1;
		rgb body_color <- is_head ? rgb(218, 157, 164) : (is_tail ? rgb(211, 205, 194) : rgb(133, 177, 205));
		draw shape color: body_color border: rgb(112, 111, 103) rotate: rotation;
		if is_head {
			draw "HEAD" at: location + {0, 0, 8} color: rgb(153, 83, 96) font: font("SansSerif", 8, #bold) perspective: false;
			draw sphere(1.2) at: location + {5, 2.5, 3} color: #black;
			draw sphere(1.2) at: location + {5, -2.5, 3} color: #black;
		}
		if is_tail {
			draw "TAIL" at: location + {0, 0, 8} color: rgb(112, 111, 103) font: font("SansSerif", 8, #bold) perspective: false;
		}
	}
}

experiment Snake type: gui {
	float minimum_cycle_duration <- 10 #ms;
	category "Swimming" expanded: true;
	parameter "Wave amplitude (motor speed)" var: wave_amplitude min: 0.0 max: 10.0 category: "Swimming";
	parameter "Wave speed (deg / cycle)" var: wave_speed min: 1.0 max: 20.0 category: "Swimming";
	parameter "Thrust" var: thrust min: 0.0 max: 20.0 category: "Swimming";

	output {
		display Snake type: 3d axes: false background: rgb(248, 246, 240) {
			graphics "World" {
				draw shape color: rgb(156, 151, 142) wireframe: true;
			}

			species snake_segment;
		}
	}
}
