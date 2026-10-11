/**
* Name: BuiltInVarsTest
* Author: Antigravity
* Description: Tests the built-in variables of the GAMA platform
* Tags: variables, global, test
*/

model BuiltInVarsTestModel

global {
	float step <- 1.5;
}

experiment BuiltInVarsTest type: test autorun: true {
	
	test "Global Variables and Cycles" {
		assert step = 1.5;
		
		// At initialization, cycle is usually 0
		assert cycle = 0;
		assert time = 0.0;
	}
	
	test "World and Agents Access" {
		assert world != nil;
		
		// In an experiment, 'agents' returns all agents in the current simulation/world
		assert length(agents) > 0;
		assert agents contains world = true;
	}
}
