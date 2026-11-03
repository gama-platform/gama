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
		assert hsic([1.0, 1.0, 1.0, 1.0], [1.0, 2.0, 3.0, 4.0]) = 0.0;
		float extreme <- hsic([-#max_float, 0.0, 1.0, #max_float], [-#max_float, 0.0, 1.0, #max_float]);
		assert extreme = 1.0;
	}
}
