/**
* Name: AgentAttributesTest
* Author: Antigravity
* Description: Tests built-in agent attributes like name, host, and peers
* Tags: agent, attribute, test
*/

model AgentAttributesTestModel

species test_agent {
	
}

experiment AgentAttributesTest type: test autorun: true {
	
	test "Built-in Attributes" {
		create test_agent number: 3 returns: created_agents;
		test_agent ag1 <- created_agents[0];
		test_agent ag2 <- created_agents[1];
		
		// Names are automatically generated
		assert ag1.name != nil;
		assert ag1.name != ag2.name;
		
		// Host is the simulation by default
		assert ag1.host = simulation;
		
		// Peers include other agents of the same species
		list<test_agent> peers_list <- ag1.peers;
		assert length(peers_list) = 2; // 3 total - 1 itself
		assert peers_list contains ag2 = true;
		assert peers_list contains ag1 = false;
	}
}
