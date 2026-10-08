/**
 * A car whose wheels are Box2D wheel joints (suspension + motor), followed by a trailer
 * attached with a rope joint and a hanging load attached with a distance (spring) joint.
 */
model Box2D_Wheel_Car

global parent: physical_world {
	string library <- "box2D";
	float step <- 1.0 / 60;
	int world_width <- 300;
	int world_height <- 100;
	geometry shape <- rectangle(world_width, world_height);
	point gravity <- {0, -9.81, 0};
	list<joint> motors <- [];

	init {
		create ground {
			location <- {150, 5};
		}
		create chassis {
			location <- {40, 25};
		}
		create wheel number: 2 {
			location <- index = 0 ? {30, 17} : {50, 17};
		}
		loop i from: 0 to: 1 {
			point anchor <- wheel[i].location;
			motors << create_wheel_joint(chassis[0], wheel[i], anchor, {0, 1, 0}, 4.0, 0.7, -8.0, 200.0);
		}
		create trailer {
			location <- {10, 25};
		}
		joint tow <- create_rope_joint(chassis[0], trailer[0], {32, 25}, {16, 25}, 12.0);
		create load {
			location <- {40, 40};
		}
		joint spring <- create_distance_joint(chassis[0], load[0], {40, 28}, {40, 38}, 2.0, 0.3);
	}

	reflex report when: every(60 #cycle) {
		write "wheel translation: " + motors[0].translation with_precision 2;
	}
}

species ground skills: [static_body] {
	geometry shape <- box(300, 10, 0.1);

	aspect default {
		draw shape color: #gray;
	}
}

species chassis skills: [dynamic_body] {
	geometry shape <- box(30, 6, 0.1);
	float mass <- 5.0;

	aspect default {
		draw shape color: #firebrick rotate: float(rotation.key);
	}
}

species wheel skills: [dynamic_body] {
	geometry shape <- circle(5);
	float mass <- 1.0;
	float friction <- 1.0;

	aspect default {
		draw shape color: #black;
	}
}

species trailer skills: [dynamic_body] {
	geometry shape <- box(14, 4, 0.1);
	float mass <- 1.0;

	aspect default {
		draw shape color: #steelblue rotate: float(rotation.key);
	}
}

species load skills: [dynamic_body] {
	geometry shape <- circle(2);
	float mass <- 0.5;

	aspect default {
		draw shape color: #gold;
	}
}

experiment "Box2D Wheel Car" type: gui {
	output {
		display Car type: 2d axes: false {
			species ground;
			species chassis;
			species wheel;
			species trailer;
			species load;
		}
	}
}
