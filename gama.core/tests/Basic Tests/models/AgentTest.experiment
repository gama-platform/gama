/**
* Name: AgentTest
* Author: Antigravity
* Description: Tests the core agent lifecycle, manipulation, and attributes
* Tags: agent, species, test
*/

model AgentTestModel

species basic_agent {
	int my_value <- 10;
	
	action multiply(int factor) {
		my_value <- my_value * factor;
	}
}

experiment AgentTest type: test autorun: true {
	
	test "Agent Creation and Death" {
		create basic_agent number: 5;
		assert length(basic_agent) = 5;
		
		ask basic_agent[0] {
			do die();
		}
		
		assert length(basic_agent) = 4;
		
		ask basic_agent {
			do die();
		}
		
		assert empty(basic_agent) = true;
	}
	
	test "Agent Attributes and Actions" {
		create basic_agent number: 1 returns: created_agents;
		basic_agent ag <- created_agents[0];
		
		assert ag.my_value = 10;
		
		ask ag {
			my_value <- 20;
		}
		assert ag.my_value = 20;
		
		ask ag {
			do multiply(factor: 3);
		}
		assert ag.my_value = 60;
		
		ask ag { do die(); }
	}
	
	test "Agent Spatial Properties" {
		create basic_agent number: 1 returns: ags;
		basic_agent ag <- ags[0];
		
		ag.location <- {10, 20, 30};
		assert ag.location.x = 10;
		assert ag.location.y = 20;
		assert ag.location.z = 30;
		
		ask ag { do die(); }
	}
	
	test "Ask Operators" {
		create basic_agent number: 3 returns: ags;
		ags[0].my_value <- 1;
		ags[1].my_value <- 2;
		ags[2].my_value <- 3;
		
		int sum <- 0;
		ask ags {
			sum <- sum + self.my_value;
		}
		assert sum = 6;
		
		list<int> values <- ags collect (each.my_value);
		assert values = [1, 2, 3];
		
		ask ags { do die(); }
	}
}
