/**
* Name: StatsOperatorsTest
* Description: Tests the descriptive statistics (median, variance, quantiles, moments...), the probability
* 	distributions (normal, chi-square, gamma, binomial, Student), the comparison of series and the
* 	clustering operators on small data sets whose results are known
* Tags: statistics, distribution, clustering, test
*/

experiment StatsOperatorsTest type: test autorun: true {

	list<float> series <- [1.0, 2.0, 3.0, 4.0, 5.0];
	list<float> doubled <- [2.0, 4.0, 6.0, 8.0, 10.0];
	// mean 5, population variance 4
	list<float> spread <- [2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0];

	test "Central tendency" {
		assert mean(series) = 3.0;
		assert mean(spread) = 5.0;

	}

	test "Chi-square, Student and binomial distributions" {

		// Student's distribution is symmetric
		assert student_area(0.0, 5) = 0.5;
		assert student_area(1.0, 5) > 0.5;
		assert student_area(1.0, 5) + student_area(-1.0, 5) = 1.0;
		assert student_t_inverse(0.9, 3) with_precision 2 = 1.64;
		// NB: student_t_inverse(0.05, 10) never returns, which is why it cannot be part of this test
	}

	test "Series analysis" {
		assert t_test(series, doubled) > 0.1 and t_test(series, doubled) < 0.11;
		// two identical samples cannot be told apart
		assert t_test(series, series) = 1.0;
	}

	test "Gamma distribution functions do not consume random draws" {
		loop generator over: ["java", "mersenne", "parallel"] {
			ask simulation {
				rng <- generator;
				seed <- 123.0;
				float expected_next <- rnd(1.0);
				seed <- 123.0;
				// Variables keep these constant-capable operators evaluated at runtime.
				float shape_parameter <- 2.0;
				float rate_parameter <- 3.0;
				float bound <- 0.9;
				float cumulative <- gamma_distribution(shape_parameter, rate_parameter, bound);
				assert cumulative >= 0.0 and cumulative <= 1.0;
				assert rng_usage = 0;
				float complemented <- gamma_distribution_complemented(shape_parameter, rate_parameter, bound);
				assert complemented >= 0.0 and complemented <= 1.0;
				assert rng_usage = 0;
				assert (cumulative + complemented) with_precision 12 = 1.0;
				assert rnd(1.0) = expected_next;
			}
		}
	}

}
