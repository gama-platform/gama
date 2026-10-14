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
	point gravity <- {0, 9.81, 0};
	list<joint> motors <- [];
	float drive_speed <- 8.0;
	int direction <- 1;

	init {
		create ground {
			location <- {150, 95};
		}
		create wall number: 2 {
			location <- index = 0 ? {3, 70} : {297, 70};
		}
		create chassis {
			location <- {40, 75};
		}
		create wheel number: 2 {
			location <- index = 0 ? {30, 83} : {50, 83};
		}
		loop i from: 0 to: 1 {
			point anchor <- wheel[i].location;
			motors << create_wheel_joint(chassis[0], wheel[i], anchor, {0, 1, 0}, 4.0, 0.7, 8.0, 0.2);
		}
		create trailer {
			location <- {10, 75};
		}
		joint tow <- create_rope_joint(chassis[0], trailer[0], {32, 75}, {16, 75}, 12.0);
		create load {
			location <- {40, 60};
		}
		joint spring <- create_distance_joint(chassis[0], load[0], {40, 72}, {40, 62}, 2.0, 0.3);
	}

	reflex bounce_at_borders {
		float x <- chassis[0].location.x;
		int new_direction <- direction;
		if (x > world_width - 60) { new_direction <- -1; }
		else if (x < 50) { new_direction <- 1; }
		if (new_direction != direction) {
			direction <- new_direction;
			loop i from: 0 to: length(motors) - 1 {
				motors[i] <- motors[i] with_motor_speed (direction * drive_speed);
			}
		}
	}

	float rope_length -> chassis[0].location distance_to trailer[0].location;
	float chassis_speed -> chassis[0].velocity.x;
	float suspension -> motors[0].translation;
}

species ground skills: [static_body] {
	geometry shape <- box(300, 10, 0.1);

	aspect default {
		draw shape color: rgb(211, 205, 194) border: rgb(156, 151, 142);
		loop i from: 0 to: 14 {
			draw line([{i * 20 + 4, location.y - 1}, {i * 20 + 14, location.y - 1}]) color: rgb(248, 246, 240) width: 2;
		}
	}
}

species wall skills: [static_body] {
	geometry shape <- box(6, 40, 0.1);

	aspect default {
		draw shape color: rgb(211, 205, 194) border: rgb(156, 151, 142);
		loop k from: 0 to: 3 {
			draw rectangle(6, 1.5) at: location + {0, -15 + k * 10} color: rgb(218, 157, 164);
		}
	}
}

species chassis skills: [dynamic_body] {
	geometry shape <- box(30, 6, 0.1);
	float mass <- 5.0;

	list<point> zigzag(point a, point b, int n, float amp) {
		list<point> pts <- [a];
		loop i from: 1 to: n {
			point p <- a + (b - a) * (i / (n + 1));
			pts << p + {(i mod 2 = 0 ? -amp : amp), 0};
		}
		pts << b;
		return pts;
	}

	aspect default {
		float angle <- float(rotation.key);
		draw shape color: rgb(218, 157, 164) border: #black rotate: angle;
		draw rectangle(9, 4) at: location + {7, -4.5} color: rgb(133, 177, 205) border: #black rotate: angle;
		draw "CHASSIS 5 kg" at: location + {-12, -9} color: rgb(112, 111, 103) font: font("SansSerif", 8, #bold);
		draw line([location + {-15, 0}, first(trailer).location + {7, 0}]) color: rgb(112, 111, 103) width: 2;
		draw "ROPE (max 12)" at: location + {-34, -3} color: rgb(112, 111, 103) font: font("SansSerif", 7, #plain);
		draw polyline(zigzag(location + {0, -3}, first(load).location + {0, 2}, 8, 1.2)) color: rgb(153, 83, 96) width: 1;
		draw line([location + {0, 5}, location + {velocity.x * 1.5, 5}]) color: rgb(64, 104, 132) width: 2 end_arrow: 2;
		draw "v = " + (velocity.x with_precision 1) at: location + {-4, 11} color: rgb(64, 104, 132) font: font("SansSerif", 7, #bold);
	}
}

species wheel skills: [dynamic_body] {
	geometry shape <- circle(5);
	float mass <- 1.0;
	float friction <- 1.0;

	aspect default {
		float angle <- float(rotation.key);
		draw shape color: rgb(156, 151, 142) border: #black;
		draw circle(2.6) at: location color: rgb(211, 205, 194) border: #black;
		draw rectangle(7, 0.8) at: location color: rgb(112, 111, 103) rotate: angle;
		draw rectangle(0.8, 7) at: location color: rgb(112, 111, 103) rotate: angle;
	}
}

species trailer skills: [dynamic_body] {
	geometry shape <- box(14, 4, 0.1);
	float mass <- 1.0;

	aspect default {
		draw shape color: rgb(133, 177, 205) border: #black rotate: float(rotation.key);
		draw "TRAILER 1 kg" at: location + {-8, -5} color: rgb(112, 111, 103) font: font("SansSerif", 7, #bold);
	}
}

species load skills: [dynamic_body] {
	geometry shape <- circle(2);
	float mass <- 0.5;

	aspect default {
		draw rectangle(5, 4) at: location color: rgb(211, 205, 194) border: rgb(156, 151, 142);
		draw "PAYLOAD 0.5 kg" at: location + {-9, -4} color: rgb(112, 111, 103) font: font("SansSerif", 7, #bold);
	}
}

experiment "Box2D Wheel Car" type: gui {
	float minimum_cycle_duration <- 10#ms;

	output {
		display Car type: 2d axes: false background: rgb(248, 246, 240) {
			graphics grid {
				loop x from: 0 to: 300 step: 20 {
					draw line([{x, 0}, {x, 100}]) color: rgb(224, 219, 210) width: 1;
				}
				loop y from: 0 to: 100 step: 20 {
					draw line([{0, y}, {300, y}]) color: rgb(224, 219, 210) width: 1;
				}
			}
			species ground;
			species wall;
			species trailer;
			species chassis;
			species wheel;
			species load;
			overlay position: {5, 5} size: {250 #px, 90 #px} background: rgb(248, 246, 240) transparency: 0.1 border: rgb(156, 151, 142) {
				draw "WHEEL-CAR TEST BENCH" at: {10 #px, 16 #px} color: rgb(112, 111, 103) font: font("SansSerif", 10, #bold);
				draw "Chassis speed : " + (chassis_speed with_precision 1) + " u/s" at: {10 #px, 34 #px} color: rgb(64, 104, 132);
				draw "Rope span : " + (rope_length with_precision 1) + " / 12 max" at: {10 #px, 50 #px} color: rgb(112, 111, 103);
				draw "Suspension travel : " + (suspension with_precision 2) at: {10 #px, 66 #px} color: rgb(153, 83, 96);
				draw "Motor : " + (direction > 0 ? "forward" : "reverse") + " " + drive_speed + " rad/s" at: {10 #px, 82 #px} color: rgb(112, 111, 103);
			}
		}
	}
}
