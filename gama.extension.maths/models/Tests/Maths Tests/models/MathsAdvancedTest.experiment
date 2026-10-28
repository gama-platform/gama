/**
* Name: MathsAdvancedTest
* Author: Antigravity
* Description: Tests advanced maths extension operators like truncated and density distributions
* Tags: maths, random, advanced, test
*/

experiment MathsAdvancedTest type: test autorun: true {
	
	test "Inverse and matrix product" {
		matrix<float> full <- matrix([[1.0, 2.0], [3.0, 4.0]]);
		matrix<float> identity <- matrix([[1.0, 0.0], [0.0, 1.0]]);

		// '.' is the matrix product, '*' multiplies cell by cell
		assert full . inverse(full) = identity;
		assert full . identity = full;
		assert (full . full)[0, 0] = 7.0;
		assert (full * full)[0, 0] = 1.0;
		assert (full * full)[1, 1] = 16.0;
		assert (full / full) = matrix([[1.0, 1.0], [1.0, 1.0]]);
		assert (full - full) = matrix([[0.0, 0.0], [0.0, 0.0]]);

	}

	test "Density operators do not consume random draws" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				float expected_next <- rnd(1.0);
				seed <- 123.0;
				float density <- exp_density(15.0, 2.0);
				assert density >= 0.0;
				assert rng_usage = 0;
				density <- gamma_density(1.0, 5.0, 2.0);
				assert density >= 0.0;
				assert rng_usage = 0;
				assert rnd(1.0) = expected_next;
			}
		}
	}

	test "Exponential sampling accounts for draws and replays from the seed" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				float value <- exp_rnd(15.0);
				int consumed <- rng_usage;
				assert value >= 0.0;
				// The sampler consumes one or more doubles, depending on its rejection path.
				assert consumed >= 2 and consumed mod 2 = 0;
				float expected_next <- rnd(1.0);
				seed <- 123.0;
				assert exp_rnd(15.0) = value;
				assert rng_usage = consumed;
				assert rnd(1.0) = expected_next;
				rng_usage <- consumed;
				assert rnd(1.0) = expected_next;
			}
		}
	}

	test "Gamma sampling accounts for draws and replays from the seed" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				float value <- gamma_rnd(1.0, 5.0);
				int consumed <- rng_usage;
				assert value >= 0.0;
				// Gamma rejection sampling may also consume a Gaussian draw.
				assert consumed >= 2 and consumed mod 2 = 0;
				float expected_next <- rnd(1.0);
				seed <- 123.0;
				assert gamma_rnd(1.0, 5.0) = value;
				assert rng_usage = consumed;
				assert rnd(1.0) = expected_next;
				rng_usage <- consumed;
				assert rnd(1.0) = expected_next;
			}
		}
	}

	test "Truncated gamma sampling accounts for rejection draws and replays from the seed" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				float value <- gamma_trunc_rnd(2.0, 3.0, 0.0, 5.0);
				int consumed <- rng_usage;
				assert value >= 0.0 and value <= 5.0;
				assert consumed >= 2 and consumed mod 2 = 0;
				float expected_next <- rnd(1.0);
				seed <- 123.0;
				assert gamma_trunc_rnd(2.0, 3.0, 0.0, 5.0) = value;
				assert rng_usage = consumed;
				assert rnd(1.0) = expected_next;
				rng_usage <- consumed;
				assert rnd(1.0) = expected_next;
			}
		}
	}

	// NB: lognormal_trunc_rnd, weibull_trunc_rnd and gamma_trunc_rnd never return when both bounds are equal,
	// which is why this case cannot be part of the tests

}
