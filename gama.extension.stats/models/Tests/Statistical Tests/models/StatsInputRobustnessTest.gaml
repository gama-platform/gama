/**
* Name: StatsInputRobustnessTest
* Description: Regression checks for invalid clustering, probability, binning and t-test inputs.
* Tags: statistics, robustness, test
*/

experiment StatsInputRobustnessTest type: test autorun: true {

	bool rejected(string problem) {
		return problem != nil and !(problem contains "Java error");
	}

	test "Kmeans rejects malformed and missing points" {
		list missing <- nil;
		loop values over: [[1, 2, 3], missing, [], [nil], [[]], [[1.0], [2.0, 3.0]], [[#nan]], [[#infinity]]] {
			string problem <- nil;
			try { unknown result <- kmeans(values, 1); } catch { problem <- #current_error; }
			assert rejected(problem);
		}
		loop count over: [0, -1, 3, #max_int] {
			string problem <- nil;
			try { unknown result <- kmeans([[1.0], [2.0]], count); } catch { problem <- #current_error; }
			assert rejected(problem);
		}
		assert length(kmeans([[1.0], [2.0]], 1)) = 1;
		assert length(kmeans([[1.0], [2.0]], 2, 10)) = 2;
	}

	test "Power test rejects invalid probabilities and effect sizes" {
		loop probability over: [1.5, 0.0, 1.0, -1.0, #nan, #infinity] {
			string problem <- nil;
			try { int result <- power_test([1.0, 2.0, 3.0], probability, 0.8, 0.1); }
			catch { problem <- #current_error; }
			assert rejected(problem);
			problem <- nil;
			try { int result <- power_test([1.0, 2.0, 3.0], 0.95, probability, 0.1); }
			catch { problem <- #current_error; }
			assert rejected(problem);
		}
		loop effect over: [0.0, -1.0, #nan, #infinity] {
			string problem <- nil;
			try { int result <- power_test([1.0, 2.0, 3.0], 0.95, 0.8, effect); }
			catch { problem <- #current_error; }
			assert rejected(problem);
		}
	}

	test "Power test and t test reject missing or insufficient observations" {
		list<float> missing <- nil;
		loop values over: [missing, [], [1.0], [nil, 2.0], [#nan, 2.0], [#infinity, 2.0]] {
			string problem <- nil;
			try { int result <- power_test(values, 0.95, 0.8, 0.1); } catch { problem <- #current_error; }
			assert rejected(problem);
			problem <- nil;
			try { float result <- t_test(values, [1.0, 2.0, 3.0]); } catch { problem <- #current_error; }
			assert rejected(problem);
			problem <- nil;
			try { float result <- t_test([1.0, 2.0, 3.0], values); } catch { problem <- #current_error; }
			assert rejected(problem);
		}
		assert t_test([1.0, 2.0, 3.0], [1.0, 2.0, 3.0]) = 1.0;
		assert power_test([1.0, 2.0, 3.0], 0.95, 0.8, 0.1) = 3;
	}

	test "Quantile validates probability and numeric sample size" {
		loop probability over: [1.5, -1.0, #nan, #infinity, -#infinity] {
			string problem <- nil;
			try { float result <- quantile([1, 2, 3], probability); } catch { problem <- #current_error; }
			assert rejected(problem);
		}
		list missing <- nil;
		loop values over: [missing, [], [nil]] {
			string problem <- nil;
			try { float result <- quantile(values, 0.5); } catch { problem <- #current_error; }
			assert rejected(problem);
		}
		assert quantile([1, 2, 3], 0.0) = 1.0;
		assert quantile([1, 2, 3], 0.5) = 2.0;
		assert quantile([1, 2, 3], 1.0) = 3.0;
	}

	test "Split in rejects impossible bin counts and preserves boundary rules" {
		string problem <- nil;
		try { unknown result <- split_in([1, 2, 3], #max_int, true); } catch { problem <- #current_error; }
		assert rejected(problem);
		problem <- nil;
		try { unknown result <- split_in([1, 2, 3], #max_int); } catch { problem <- #current_error; }
		assert rejected(problem);
		list<int> missing <- nil;
		assert empty(missing);
		assert split_in(missing, 1) = [[]];
		assert split_in([1, 2, 3], 1) = [[1, 2, 3]];
		assert split_in([1, 2, 3], 2, true) = [[1], [2, 3]];
		assert split_in([1, 2, 3], 2, false) = [[1, 2], [3]];
	}
}
