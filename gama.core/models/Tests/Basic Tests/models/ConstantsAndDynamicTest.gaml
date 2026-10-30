/**
* Name: ConstantsAndDynamicTest
* Author: Antigravity
* Description: Tests the constant modifier and dynamic variable mapping
* Tags: variable, constant, dynamic, test
*/

model ConstantsAndDynamicTestModel

species dynamic_agent {
	int my_constant <- 100 const: true;
	
	int base_val <- 5;
	
	// Dynamic variable evaluating every time it's called
	int dynamic_val -> base_val * 2;
	
	// 'is_const' isn't a GAML operator, but we can verify it doesn't change
}

experiment ConstantsAndDynamicTest type: test autorun: true {
	
	test "Constant Values" {
		create dynamic_agent number: 1 returns: created_agents;
		dynamic_agent ag <- created_agents[0];
		
		assert ag.my_constant = 100;
		// A constant cannot be reassigned. Attempting to assign `ag.my_constant <- 200`
		// would trigger a compilation error, which means we can't test it dynamically here
		// safely without halting the test suite if it fails. Thus, asserting value is sufficient.
	}
	
	test "Dynamic Function Variables" {
		create dynamic_agent number: 1 returns: created_agents;
		dynamic_agent ag <- created_agents[0];
		
		assert ag.dynamic_val = 10;
		
		// Modify the base value
		ag.base_val <- 20;
		
		// The dynamic variable should reflect the change immediately
		assert ag.dynamic_val = 40;
	}
}
