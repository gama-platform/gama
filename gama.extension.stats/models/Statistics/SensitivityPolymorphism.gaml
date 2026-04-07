/**
* Name: Sensitivity Polymorphism Test
* Author: Gemini CLI
* Description: Test the polymorphic behavior and consistency of sensitivity analysis operators (1D and 2D).
*/

model sensitivity_polymorphism_test

global {
	init {
		// --- 1. 1D FUNCTIONAL TESTS ---
		write "--- STARTING 1D TESTS ---";
		
		map sobol_data <- ["p1":: [0.1, 0.2, 0.3, 0.4], "out":: [1.0, 1.1, 1.2, 1.3]];
		string sobol_report <- sobolAnalysis(sobol_data, "sobol_test_report.txt", 1);
		write "Sobol 1D Map: " + ((sobol_report != "") ? "OK" : "FAIL");

		map morris_data <- ["p1":: [0.1, 0.2, 0.3, 0.4, 0.5, 0.6], "out":: [1.0, 1.1, 1.2, 1.3, 1.4, 1.5]];
		string morris_report <- morrisAnalysis(morris_data, 4, 1);
		write "Morris 1D Map: " + ((morris_report != "") ? "OK" : "FAIL");

		list stable_p <- [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1];
		list stable_o <- [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0];
		map stoch_data <- ["p1":: stable_p, "out":: stable_o];
		write "Stochanalyse 1D Map: " + stochanalyse(10, 0.1, stoch_data, 1);


		// --- 2. 2D FUNCTIONAL TESTS ---
		write "--- STARTING 2D TESTS ---";

		// Sobol 2D: k=2, needs multiple of (2*2+2) = 6
		map sob2d <- [
			"p1":: [0.1, 0.2, 0.3, 0.4, 0.5, 0.6],
			"p2":: [0.1, 0.1, 0.1, 0.2, 0.2, 0.2],
			"out":: [1.0, 1.1, 1.2, 1.3, 1.4, 1.5]
		];
		write "Sobol 2D Map: " + (sobolAnalysis(sob2d, "sob2d.txt", 2) != "" ? "OK" : "FAIL");

		// Morris 2D: k=2, needs multiple of (2+1) = 3
		map mor2d <- [
			"p1":: [0.1, 0.5, 0.5, 0.1, 0.1, 0.5],
			"p2":: [0.1, 0.1, 0.5, 0.5, 0.1, 0.1],
			"out":: [1.0, 1.2, 1.8, 1.5, 1.0, 1.2]
		];
		write "Morris 2D Map: " + (morrisAnalysis(mor2d, 4, 2) != "" ? "OK" : "FAIL");

		// Stochanalyse 2D: combination of p1,p2 (5 replicates each)
		map stoch2d <- [
			"p1":: [0.1, 0.1, 0.1, 0.1, 0.1, 0.2, 0.2, 0.2, 0.2, 0.2],
			"p2":: [0.5, 0.5, 0.5, 0.5, 0.5, 0.6, 0.6, 0.6, 0.6, 0.6],
			"out":: [1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0, 2.0]
		];
		write "Stochanalyse 2D Map: " + stochanalyse(10, 0.1, stoch2d, 2);


		// --- 3. CONSISTENCY TESTS ---
		write "--- STARTING CONSISTENCY TESTS ---";
		
		map data_map <- ["col0":: [0.1, 0.2, 0.3, 0.4], "col1":: [1.0, 1.1, 1.2, 1.3]];
		matrix data_mat <- matrix([[0.1, 0.2, 0.3, 0.4], [1.0, 1.1, 1.2, 1.3]]);

		if sobolAnalysis(data_map, "sob_map.txt", 1) = sobolAnalysis(data_mat, "sob_mat.txt", 1) {
			write "  SUCCESS: Sobol Map == Matrix";
		}
		
		write "--- ALL TESTS COMPLETED ---";
	}
}

experiment test_polymorphism type: gui;
