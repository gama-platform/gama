/**
 * A simple articulated chain driven by hinge motors.
 */
model Jointed_Snake

global parent: physical_world {
	string library <- "bullet";
	bool use_native <- true;
	int segment_count <- 8;
	float segment_length <- 16.0;
	list<joint> joints <- [];
	geometry shape <- box(160, 80, 40);
	point gravity <- {0, 0, 0};

	init {
		do register([self]);
		create snake_segment number: segment_count;
		ask snake_segment {
			location <- {30 + index * segment_length, 40, 20};
		}
		loop i from: 0 to: segment_count - 2 {
			point anchor <- {30 + (i + 1) * segment_length - segment_length / 2, 40, 20};
			joints << create_hinge_joint_with_axis(snake_segment[i], snake_segment[i + 1], anchor, {0, 0, 1},
					-0.6, 0.6, 0.0, 8.0);
		}
	}

	reflex drive_joints {
		loop i from: 0 to: length(joints) - 1 {
			joints[i] <- joints[i] with_motor_speed (1.2 * sin(cycle * 8 + i * 45));
		}
	}
}

species snake_segment skills: [dynamic_body] {
	geometry shape <- box(16, 5, 5);
	float mass <- 1.0;
	float friction <- 0.4;

	aspect default {
		draw shape color: #orange rotate: float(rotation.key);
	}
}

experiment Snake type: gui {
	output {
		display Snake type: 3d {
			graphics "World" {
				draw shape color: #white;
			}
			species snake_segment;
		}
	}
}
