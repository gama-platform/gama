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
		assert simulation.step = 1.5;
		
		// At initialization, cycle is usually 0
		assert simulation.cycle = 0;
		assert simulation.time = 0.0;
	}
	
	test "World and Agents Access" {
		assert simulation.world != nil;
		
		// In a simulation, 'agents' returns all agents in the current simulation/world
		assert length(simulation.agents) > 0;
		assert simulation.agents contains simulation = true;
	}
}
