/**
* Name: HSICRobustnessTest
* Description: Bounded checks of HSIC permutation counts, observations and p-values.
* Tags: statistics, robustness, test
*/

experiment HSICRobustnessTest type: test autorun: true {

	bool handled(string problem) {
		return problem != nil and !(problem contains "Java error");
	}

	test "Non-positive permutation counts are rejected" {
		loop count over: [0, -1, #min_int] {
			string problem <- nil;
			try { float result <- hsic_p_value([1.0, 2.0, 3.0], [1.0, 2.0, 3.0], count); }
			catch { problem <- #current_error; }
			assert handled(problem);
		}
	}

	test "Invalid input lists are rejected" {
		list<float> missing <- nil;
		loop values over: [missing, [], [1.0], [1.0, 2.0, 3.0], [nil, 2.0], [#nan, 2.0], [#infinity, 2.0], [-#infinity, 2.0]] {
			string problem <- nil;
			try { float result <- hsic_p_value(values, [1.0, 2.0], 2); }
			catch { problem <- #current_error; }
			assert handled(problem);
			problem <- nil;
			try { float result <- hsic_p_value([1.0, 2.0], values, 2); }
			catch { problem <- #current_error; }
			assert handled(problem);
			problem <- nil;
			try { float result <- hsic(values, [1.0, 2.0]); }
			catch { problem <- #current_error; }
			assert handled(problem);
			problem <- nil;
			try { float result <- hsic([1.0, 2.0], values); }
			catch { problem <- #current_error; }
			assert handled(problem);
		}
	}

	test "Permutation p-values are bounded and constant samples give one" {
		loop count over: [1, 2, 100] {
			float result <- hsic_p_value([1.0, 2.0, 3.0], [3.0, 1.0, 2.0], count);
			assert result >= 1.0 / (count + 1.0) and result <= 1.0;
			assert hsic_p_value([1.0, 1.0, 1.0], [1.0, 2.0, 3.0], count) = 1.0;
		}
		assert hsic_p_value([1.0, 2.0], [1.0, 2.0], 2) = 1.0;
	}

	test "Degenerate zero-variance samples return zero HSIC and unit p-value" {
		// Both X and Y constant
		assert hsic([2.0, 2.0, 2.0], [5.0, 5.0, 5.0]) = 0.0;
		assert hsic_p_value([2.0, 2.0, 2.0], [5.0, 5.0, 5.0], 10) = 1.0;

		// One constant, one varying
		assert hsic([1.0, 1.0, 1.0, 1.0], [1.0, 2.0, 3.0, 4.0]) = 0.0;
		assert hsic([1.0, 2.0, 3.0, 4.0], [1.0, 1.0, 1.0, 1.0]) = 0.0;
		assert hsic_p_value([1.0, 1.0, 1.0], [1.0, 2.0, 3.0], 10) = 1.0;
		assert hsic_p_value([1.0, 2.0, 3.0], [1.0, 1.0, 1.0], 10) = 1.0;

		// Smallest degenerate sample (n = 2)
		assert hsic([1.0, 1.0], [2.0, 2.0]) = 0.0;
		assert hsic_p_value([1.0, 1.0], [2.0, 2.0], 2) = 1.0;
	}

	test "Seed makes hsic_p_value deterministic across RNGs" {
		list<float> sample_x <- [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0];
		list<float> sample_y <- [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0];
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 42.0;
				float p1 <- hsic_p_value(sample_x, sample_y, 100);
				int usage1 <- rng_usage;
				assert p1 < 0.05;
				assert usage1 > 0;
				seed <- 42.0;
				float p2 <- hsic_p_value(sample_x, sample_y, 100);
				assert p1 = p2;
				assert rng_usage = usage1;
			}
		}
	}

	test "Normalized hsic handles small and extreme finite samples" {
		assert hsic([1.0, 2.0, 3.0], [1.0, 2.0, 3.0]) = 1.0;
		float extreme <- hsic([-#max_float, 0.0, 1.0, #max_float], [-#max_float, 0.0, 1.0, #max_float]);
		assert extreme = 1.0;

		// Subnormal numbers near minimum non-zero double
		list<float> subnormals <- [#min_float, 2.0 * #min_float, 3.0 * #min_float];
		assert hsic(subnormals, subnormals) = 1.0;

		// Four observations with ties and subnormal values (exercises even-length medianSlice without underflow)
		list<float> subnormal_even <- [0.0, #min_float, 0.0, #min_float];
		assert hsic(subnormal_even, subnormal_even) = 1.0;
		float p_even <- hsic_p_value(subnormal_even, subnormal_even, 10);
		assert p_even >= 0.0 and p_even <= 1.0;
	}

	test "Repeated simulations calibrate Type I error rate under independence" {
		int num_trials <- 50;
		int rejections <- 0;
		float sum_p <- 0.0;
		ask simulation {
			seed <- 12345.0;
			loop i from: 1 to: num_trials {
				list<float> x_indep <- [];
				list<float> y_indep <- [];
				loop times: 10 {
					x_indep <- x_indep + rnd(100.0);
					y_indep <- y_indep + rnd(100.0);
				}
				float p <- hsic_p_value(x_indep, y_indep, 100);
				sum_p <- sum_p + p;
				if (p < 0.05) {
					rejections <- rejections + 1;
				}
			}
		}
		// Under H0, Type I error rate at alpha = 0.05 is controlled (<= 6 rejections out of 50)
		assert rejections <= 6;
		// Mean p-value under the null should be approximately 0.5 (uniformly distributed on [0, 1])
		float mean_p <- sum_p / num_trials;
		assert mean_p >= 0.35 and mean_p <= 0.65;
	}

	test "Repeated simulations calibrate statistical power under dependence" {
		int num_trials <- 20;
		int rejections <- 0;
		ask simulation {
			seed <- 54321.0;
			loop i from: 1 to: num_trials {
				list<float> x_dep <- [];
				list<float> y_dep <- [];
				loop j from: 1 to: 12 {
					float xj <- float(j);
					x_dep <- x_dep + xj;
					y_dep <- y_dep + (2.0 * xj + rnd(-0.5, 0.5));
				}
				float p <- hsic_p_value(x_dep, y_dep, 100);
				if (p < 0.05) {
					rejections <- rejections + 1;
				}
			}
		}
		// Statistical power: under clear linear dependence with noise, power should be >= 95%
		assert rejections >= 19;
	}
}
