/**
* Name: Sensitivity Polymorphism Test
* Description: Test the polymorphic behavior and consistency of sensitivity analysis operators.
*/

model sensitivity_polymorphism_test

global {
	init {
		// --- 1. FUNCTIONAL TESTS ---
		write "--- STARTING FUNCTIONAL TESTS ---";
		
		map sobol_data <- ["p1":: [0.1, 0.2, 0.3, 0.4], "out":: [1.0, 1.1, 1.2, 1.3]];
		string sobol_report <- sobolAnalysis(sobol_data, "sobol_test_report.txt", 1);
		write "Sobol Map: " + ((sobol_report != "") ? "OK" : "FAIL");

		map morris_data <- ["p1":: [0.1, 0.2, 0.3, 0.4, 0.5], "out":: [1.0, 1.1, 1.2, 1.3, 1.4]];
		string morris_report <- morrisAnalysis(morris_data, 4, 1);
		write "Morris Map: " + ((morris_report != "") ? "OK" : "FAIL");

		// Convergence test: 10 identical replicates
		list stable_p <- [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1];
		list stable_o <- [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0];
		map stoch_data <- ["p1":: stable_p, "out":: stable_o];
		
		string stoch_report <- stochanalyse(10, 0.1, stoch_data, 1);
		write "Stochanalyse Result (with 10 stable replicates): " + stoch_report;


		// --- 2. CONSISTENCY TESTS ---
		write "--- STARTING CONSISTENCY TESTS ---";
		
		map data_map <- ["col0":: [0.1, 0.2, 0.3, 0.4], "col1":: [1.0, 1.1, 1.2, 1.3]];
		matrix data_mat <- matrix([[0.1, 0.2, 0.3, 0.4], [1.0, 1.1, 1.2, 1.3]]);

		write "Checking Sobol consistency...";
		string sob_map <- sobolAnalysis(data_map, "sob_map.txt", 1);
		string sob_mat <- sobolAnalysis(data_mat, "sob_mat.txt", 1);
		
		if sob_map = sob_mat {
			write "  SUCCESS: Sobol Map == Sobol Matrix";
		} else {
			write "  FAILURE: Sobol results differ!";
		}

		write "Checking Stochanalyse consistency...";
		map st_map <- ["col0":: stable_p, "col1":: stable_o];
		matrix st_mat <- matrix([stable_p, stable_o]);
		
		string res_map <- stochanalyse(10, 0.1, st_map, 1);
		string res_mat <- stochanalyse(10, 0.1, st_mat, 1);
		
		if res_map = res_mat {
			write "  SUCCESS: Stochanalyse Map == Stochanalyse Matrix (" + res_map + ")";
		} else {
			write "  FAILURE: Stochanalyse results differ!";
		}
		
		write "--- ALL TESTS COMPLETED ---";
	}
}

experiment test_polymorphism type: gui;
