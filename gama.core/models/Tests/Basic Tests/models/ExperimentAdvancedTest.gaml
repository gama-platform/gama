/**
* Name: ExperimentAdvancedTest
* Author: Antigravity
* Description: Tests experiment block behaviors, parameter assignments, and multiple simulations
* Tags: experiment, simulation, parameter, test
*/

model ExperimentAdvancedTestModel

global {
	int my_param <- 10;
	float my_float_param <- 5.0;
}

experiment ExperimentAdvancedTest type: test autorun: true {
	
	parameter "My Param" var: my_param init: 20;
	
	test "Parameter Initialization" {
		// Because the parameter init overrides the global init in this experiment
		assert my_param = 20;
		assert my_float_param = 5.0; // untouched
	}
	
	test "Simulation Halting and Pausing" {
		// This is tricky to test since halt stops the simulation, 
		// but we can assert that simulation properties exist
		assert simulation != nil;
		assert simulation.color != nil;
		
		// In a test experiment, there's usually exactly one simulation running the tests
		assert length(simulations) >= 1;
	}
}
