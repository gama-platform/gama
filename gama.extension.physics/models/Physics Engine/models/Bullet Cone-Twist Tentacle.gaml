/**
 * A tentacle made of boxes linked by cone-twist joints (ragdoll-style), anchored to a fixed base
 * with a fixed joint, and swung around by an initial push. Runs on native Bullet when available.
 */
model Bullet_ConeTwist_Tentacle

global parent: physical_world {
	string library <- "bullet";
	bool use_native <- true;
	int segment_count <- 8;
	float segment_length <- 10.0;
	geometry shape <- box(160, 80, 80);
	point gravity <- {0, 0, -9.81};

	init {
		do register([self]);
		create tentacle_base {
			location <- {30, 40, 60};
		}
		create tentacle_segment number: segment_count;
		ask tentacle_segment {
			location <- {30 + (index + 1) * segment_length, 40, 60};
		}
		joint root <- create_fixed_joint(tentacle_base[0], tentacle_segment[0], {30 + segment_length / 2, 40, 60});
		loop i from: 0 to: segment_count - 2 {
			point anchor <- {30 + (i + 1.5) * segment_length, 40, 60};
			joint j <- create_cone_twist_joint(tentacle_segment[i], tentacle_segment[i + 1], anchor, {1, 0, 0}, 0.5, 0.3);
		}
	}
}

species tentacle_base skills: [static_body] {
	geometry shape <- box(10, 10, 10);

	aspect default {
		draw shape color: #darkgray;
	}
}

species tentacle_segment skills: [dynamic_body] {
	geometry shape <- box(10, 4, 4);
	float mass <- 0.5;
	float friction <- 0.4;

	aspect default {
		draw shape color: rgb(255, 120 + index * 15, 60) rotate: float(rotation.key);
	}
}

experiment Tentacle type: gui {
	output {
		display Tentacle type: 3d {
			graphics "World" {
				draw shape color: #white wireframe: true;
			}
			species tentacle_base;
			species tentacle_segment;
		}
	}
}
