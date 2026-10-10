/**
* Name: UserArchitectureTest
* Author: Antigravity
* Description: Tests the user architecture structures (user_first / user_last)
* Tags: core, architecture, user, test
*/

model UserArchitectureTestModel

species user_agent control: user_first {
	
	state my_initial_state initial: true {
		// Do nothing
	}
	
	user_panel "My Panel" {
		user_command "do something" {
			// Action executed by user
		}
	}
}

species user_agent2 control: user_last {
	
	state my_initial_state initial: true {
		// Do nothing
	}
	
	user_panel "My Panel 2" {
		user_command "do something else" {
		}
	}
}

experiment UserArchitectureTest type: test autorun: true {
	
	test "User Architecture Blocks Compilation" {
		create user_agent number: 1;
		create user_agent2 number: 1;

		assert length(user_agent) = 1;
		assert length(user_agent2) = 1;
		assert user_agent[0].state = "my_initial_state";
		assert user_agent2[0].state = "my_initial_state";
	}
}
