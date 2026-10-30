/**
* Name: BatchAdvancedTest
* Author: Antigravity
* Description: Declares batch experiments using the exploration methods (exhaustive, genetic, annealing, tabu).
*   A batch experiment cannot be run by the test runner, which only executes experiments of type 'test': this
*   file is a compilation check, done when the tests are validated ('-validate'), and contains no assertion
* Tags: batch, extension, test
*/
model BatchAdvancedTestModel

global {
	int dummy_var <- 1;
}

experiment BatchAdvancedTest type: batch keep_seed: true repeat: 1 until: (cycle = 1) {
	parameter "Var1" var: dummy_var init: 1;
	method exhaustive;
}

experiment BatchAdvancedTest2 type: batch keep_seed: true repeat: 1 until: (cycle = 1) {
	parameter "Var1" var: dummy_var min: 1 max: 10 step: 1;
	method genetic pop_dim: 5 crossover_prob: 0.7 mutation_prob: 0.1 nb_prelim_gen: 1 max_gen: 5;
}

experiment BatchAdvancedTest3 type: batch keep_seed: true repeat: 1 until: (cycle = 1) {
	parameter "Var1" var: dummy_var min: 1 max: 10 step: 1;
	method annealing temp_init: 100.0 temp_end: 1.0 temp_decrease: 0.5 nb_iter_cst_temp: 5;
}

experiment BatchAdvancedTest4 type: batch keep_seed: true repeat: 1 until: (cycle = 1) {
	parameter "Var1" var: dummy_var min: 1 max: 10 step: 1;
	method tabu iter_max: 5 tabu_list_size: 5;
}
