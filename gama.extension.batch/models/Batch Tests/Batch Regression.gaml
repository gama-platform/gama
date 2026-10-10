/**
* Name: Batch Regression
* Description: Manually-run regression models for batch experiments (PR #1252: autonomous simulations, per-simulation
*   'until', explicit 'with:' plans). Run each experiment and check the console: every check writes "PASS" or "FAIL".
*   See README.md in this folder for what cannot be checked from GAML (threads, status bar, reload).
*/

model batch_regression

global {
	// Each simulation has its own random target: if simulations were stepped in lock-step, or stopped by another
	// simulation's condition, some would not end exactly on their own target.
	int target <- rnd(5, 40);
	float p <- 0.0;
	bool is_over -> cycle >= target;
	bool stop_sim() { return is_over; }
}

// Writes the result of the checks on the simulations of one run (called at the end of each run).
experiment regression_abstract type: batch virtual: true until: world.stop_sim() {
	parameter "p" var: p min: 0.0 max: 1.0 step: 0.5;

	action check(string title) {
		bool until_ok <- simulations all_match (each.cycle = each.target);
		write title + " | each simulation stops at its own target: " + (until_ok ? "PASS" : "FAIL");
	}
}

// 1. Each simulation must stop on its own 'until' condition, whatever the concurrency.
experiment until_sequential parent: regression_abstract type: batch repeat: 6 parallel: 1 keep_seed: true
		until: world.stop_sim() {
	method exploration;
	reflex end_of_runs { do check("until_sequential"); }
}

experiment until_parallel parent: regression_abstract type: batch repeat: 6 parallel: 3 keep_seed: true
		until: world.stop_sim() {
	method exploration;
	reflex end_of_runs { do check("until_parallel"); }
}

// 2. Many short simulations, not kept: used to hang or leave simulations behind (#198, #282).
// The batch must finish by itself ("Batch over" in the status bar) and the experiment reflex must fire.
experiment many_not_kept parent: regression_abstract type: batch repeat: 100 parallel: 4 keep_simulations: false
		until: world.stop_sim() {
	method exploration;
	reflex end_of_runs { write "many_not_kept | end of a run (simulations kept: " + length(simulations) + ")"; }
}

// 3. 'with:' needs parameter names as strings; each run must use one of the given values.
experiment explicit_with parent: regression_abstract type: batch repeat: 3 keep_seed: true
		until: world.stop_sim() {
	method exploration with: [["p"::0.2], ["p"::0.4], ["p"::0.8]];
	reflex end_of_runs {
		bool p_ok <- simulations all_match (each.p in [0.2, 0.4, 0.8]);
		write "explicit_with | parameters taken from the plan: " + (p_ok ? "PASS" : "FAIL");
		do check("explicit_with");
	}
}

// 4. A 'permanent' section must be closed and reopened when the experiment is reloaded (#199).
experiment permanent_reload parent: regression_abstract type: batch repeat: 3 until: world.stop_sim() {
	method exploration;
	permanent {
		display Cycles type: 2d {
			chart "Cycles" type: series {
				data "Max" value: max(simulations collect each.cycle) color: #red;
			}
		}
	}
}
