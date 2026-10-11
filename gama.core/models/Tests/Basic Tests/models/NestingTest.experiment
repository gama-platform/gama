/**
* Name: NestingTest
* Author: Antigravity
* Description: Tests multi-level architectures (macro and micro species), capture, and release
* Tags: species, multi-level, architecture, test
*/

model NestingTestModel

species macro_agent {
	int child_count -> length(members);
	
	action create_children() {
		create micro_agent number: 3;
	}
	
	species micro_agent {
		int my_val <- 5;
	}
}

species free_agent {
	int my_val <- 10;
}

experiment NestingTest type: test autorun: true {
	
	test "Micro Species Creation" {
		create macro_agent number: 1 returns: macros;
		macro_agent ma <- macros[0];
		
		assert ma.child_count = 0;
		
		ask ma {
			do create_children();
		}
		
		assert ma.child_count = 3;
		
		// The children can be accessed via the macro agent
		assert length(ma.micro_agent) = 3;
		assert ma.micro_agent[0].my_val = 5;
	}
	
	test "Capture and Release" {
		create macro_agent number: 1 returns: macros;
		macro_agent ma <- macros[0];
		
		create free_agent number: 1 returns: frees;
		free_agent fa <- frees[0];
		
		// Capture free_agent as micro_agent
		ask ma {
			capture fa as: micro_agent;
		}
		
		assert ma.child_count = 1;
		
		// The original free_agent is "dead" (removed from free_agent population)
		assert dead(fa) = true;
		
		// Release the micro agent
		ask ma {
			release micro_agent[0] as: free_agent;
		}
		
		assert ma.child_count = 0;
		// A new free agent is created
		assert length(free_agent) = 1;
	}
}
