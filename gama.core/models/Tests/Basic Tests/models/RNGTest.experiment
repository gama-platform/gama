/**
* Name: RNGTest
* Description: Checks raw draw accounting, bounded integer sampling and rng_usage restoration.
* Tags: random, rng, seed, test
*/

model RNGTestModel

species dummy {
}

species walker skills: [moving] {
}

experiment RNGTest type: test autorun: true {

	test "Resetting usage, seed and generator" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				int first_draw <- rnd(100, true);
				assert rng_usage > 0;
				rng_usage <- 0;
				assert rng_usage = 0;
				assert rnd(100, true) = first_draw;
				seed <- 123.0;
				assert rng_usage = 0;
				assert rnd(100, true) = first_draw;
				rng <- generator;
				assert rng_usage = 0;
				assert rnd(100, true) = first_draw;
			}
		}
	}

	test "Usage is an absolute position, including odd positions" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				loop position over: [0, 1, 3, 4, 5, 6, 31, 625] {
					seed <- 123.0;
					// A power-of-two interval consumes exactly one raw draw.
					loop times: position { int ignored <- rnd(31, true); }
					assert rng_usage = position;
					float expected <- rnd(1.0);
					rng_usage <- position;
					assert rng_usage = position;
					assert rnd(1.0) = expected;
					rng_usage <- position;
					assert rng_usage = position;
					assert rnd(1.0) = expected;
				}
				rng_usage <- -6;
				assert rng_usage = 0;
				float first_draw <- rnd(1.0);
				seed <- 123.0;
				assert rnd(1.0) = first_draw;
			}
		}
	}

	test "Legacy draws consume two raw draws and keep their sequences" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 1.0;
				int first_draw <- rnd(10);
				assert rng_usage = 2;
				seed <- 1.0;
				assert rnd(10, false) = first_draw;
				assert rng_usage = 2;
				seed <- 1.0;
				first_draw <- rnd(-2, 100);
				assert rng_usage = 2;
				seed <- 1.0;
				assert rnd(-2, 100, false) = first_draw;
				assert rng_usage = 2;
				seed <- 1.0;
				float value <- rnd(10.0);
				assert rng_usage = 2;
				seed <- 1.0;
				value <- rnd(-1.0, 10.0);
				assert rng_usage = 2;
				seed <- 1.0;
				int stepped <- rnd(2, 12, 4);
				assert rng_usage = 2;
				seed <- 1.0;
				point p <- rnd({1, 2, 3}, {4, 5, 6});
				assert rng_usage = 6;
			}
		}
	}

	test "Bounded integer sampling consumes one raw draw for power-of-two ranges" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				int value <- rnd(31, true);
				assert value >= 0 and value <= 31;
				assert rng_usage = 1;
				value <- rnd(-16, 15, true);
				assert value >= -16 and value <= 15;
				assert rng_usage = 2;
				assert rnd(7, 7, true) = 7;
				assert rng_usage = 3;
				assert rnd(0, true) = 0;
				assert rng_usage = 4;
			}
		}
	}

	test "Rejection sampling counts every raw draw and remains replayable" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				bool rejected <- false;
				loop times: 64 {
					int before_draw <- rng_usage;
					int value <- rnd(1073741824, true);
					int after_draw <- rng_usage;
					assert value >= 0 and value <= 1073741824;
					assert after_draw > before_draw;
					rejected <- rejected or after_draw > before_draw + 1;
					rng_usage <- before_draw;
					assert rnd(1073741824, true) = value;
					assert rng_usage = after_draw;
				}
				assert rejected;
			}
		}
	}

	test "Weighted choices and container selections keep float-based accounting" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				int choice_index <- rnd_choice([0.2, 0.5, 0.3]);
				assert choice_index in [0, 1, 2];
				assert rng_usage = 2;
				seed <- 123.0;
				string key <- rnd_choice(["a"::0.2, "b"::0.5, "c"::0.3]);
				assert key in ["a", "b", "c"];
				assert rng_usage = 2;
				seed <- 123.0;
				int value <- one_of([1, 2, 3, 4]);
				assert rng_usage = 2;
				seed <- 123.0;
				value <- any([1, 2, 3, 4]);
				assert rng_usage = 2;
				seed <- 123.0;
				bool result <- flip(0.3);
				assert rng_usage = 2;
			}
		}
	}

	test "Agent creation and spatial sampling account for float coordinates" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				create dummy returns: created;
				// Polygon sampling draws a triangle weight and two barycentric coordinates.
				assert rng_usage = 6;
				ask created { do die(); }
				seed <- 123.0;
				point p <- any_point_in(square(10));
				assert rng_usage = 6;
				seed <- 123.0;
				assert any_location_in(square(10)) = p;
				assert rng_usage = 6;
			}
		}
	}

	test "Moving agents draw a heading at creation and another when wandering" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				// A fixed location isolates heading draws from spatial sampling.
				create walker with: [location::{50, 50}] returns: created;
				point initial_location <- created[0].location;
				float initial_heading <- created[0].heading;
				assert rng_usage = 2;
				ask created { do wander(); }
				assert rng_usage = 4;
				point final_location <- created[0].location;
				float final_heading <- created[0].heading;
				ask created { do die(); }
				seed <- 123.0;
				create walker with: [location::{50, 50}] returns: replay;
				assert replay[0].location = initial_location;
				assert replay[0].heading = initial_heading;
				assert rng_usage = 2;
				ask replay { do wander(); }
				assert replay[0].location = final_location;
				assert replay[0].heading = final_heading;
				assert rng_usage = 4;
				ask replay { do die(); }
			}
		}
	}

	test "Binomial sampling counts raw integer draws and is reproducible" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				int value <- binomial(15, 0.5);
				assert value >= 0 and value <= 15;
				assert rng_usage = 1;
				seed <- 123.0;
				value <- binomial(15, 0.6);
				int consumed <- rng_usage;
				assert value >= 0 and value <= 15;
				assert consumed > 0;
				float next_value <- rnd(1.0);
				seed <- 123.0;
				assert binomial(15, 0.6) = value;
				assert rng_usage = consumed;
				assert rnd(1.0) = next_value;
				rng_usage <- consumed;
				assert rnd(1.0) = next_value;
			}
		}
	}

	test "Rewinding clears cached Gaussian draws" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				float first_draw <- gauss(0, 1);
				assert rng_usage >= 4;
				rng_usage <- 0;
				assert rng_usage = 0;
				assert gauss(0, 1) = first_draw;
			}
		}
	}

	test "The threaded generator has no reproducible usage counter" {
		ask simulation {
			rng <- "threaded";
			int value <- rnd(31, true);
			assert value >= 0 and value <= 31;
			assert rng_usage = 0;
			rng_usage <- 5;
			assert rng_usage = 0;
			rng_usage <- -1;
			assert rng_usage = 0;
		}
	}
}
