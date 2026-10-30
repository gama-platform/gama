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
		loop values over: [missing, [], [1.0], [nil, 2.0], [#nan, 2.0], [#infinity, 2.0], [-#infinity, 2.0]] {
			string problem <- nil;
			try { float result <- hsic_p_value(values, [1.0, 2.0], 2); }
			catch { problem <- #current_error; }
			assert handled(problem);
			problem <- nil;
			try { float result <- hsic_p_value([1.0, 2.0], values, 2); }
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
}
