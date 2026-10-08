/**
 * A two-link robotic arm driven by Box2D revolute-joint motors.
 */
model Box2D_Articulated_Arm

global parent: physical_world {
	string library <- "box2D";
	float step <- 1.0 / 60;
	int world_width <- 200;
	int world_height <- 100;
	geometry shape <- rectangle(world_width, world_height);
	point gravity <- {0, 0, 0};
	joint shoulder_joint <- nil;
	joint elbow_joint <- nil;

	init {
		create arm_base {
			location <- {45, 50};
		}
		create arm_link number: 2 {
			location <- index = 0 ? {75, 50} : {125, 50};
		}

		shoulder_joint <- create_hinge_joint(arm_base, arm_link[0], {50, 50}, -1.2, 1.2, 0.0, 80.0);
		elbow_joint <- create_hinge_joint(arm_link[0], arm_link[1], {100, 50}, -1.2, 1.2, 0.0, 45.0);
	}

	reflex drive_arm {
		shoulder_joint.motorSpeed <- 1.5 * sin(cycle * 3);
		elbow_joint.motorSpeed <- 2.0 * sin(cycle * 3 + 90);
	}
}

species arm_base skills: [static_body] {
	geometry shape <- box(10, 14, 0.1);

	aspect default {
		draw shape color: #darkgray;
	}
}

species arm_link skills: [dynamic_body] {
	geometry shape <- box(50, 8, 0.1);
	float mass <- 2.0;
	float friction <- 0.5;

	aspect default {
		draw shape color: #steelblue rotate: float(rotation.key);
	}
}

experiment "Box2D Articulated Arm" type: gui {
	output {
		display Arm type: 2d axes: false {
			species arm_base;
			species arm_link;
		}
	}
}
