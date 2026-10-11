/**
* Name: NetworkAdvancedTest
* Author: Antigravity
* Description: Tests the network skill without relying on any external server: the state of an agent before it
* 	connects, the failure of a connection to an unreachable server and the registration to a local one.
* 	The exchange of messages itself is covered by network_test_interaction_local, which needs an MQTT broker
* Tags: network, advanced, test
*/
model NetworkAdvancedTestModel

species net_agent skills: [network] { }

experiment NetworkAdvancedTest type: test autorun: true {

	test "An agent that is not connected has nothing to read" {
		create net_agent number: 1 returns: created_agents;
		net_agent ag <- created_agents[0];
		assert empty(ag.mailbox);
		assert not ag.has_more_message();
		assert empty(ag.network_groups);
		assert empty(ag.network_server);
		ask created_agents { do die(); }
	}

	test "Connecting to an unreachable server raises an error" {
		create net_agent number: 1 returns: created_agents;
		bool failed <- false;
		try {
			// nothing is expected to listen on port 1
			ask created_agents[0] { do connect(to: "localhost", protocol: "tcp_client", port: 1, with_name: "nobody"); }
		} catch {
			failed <- true;
		}
		assert failed;
		ask created_agents { do die(); }
	}

	test "A connected agent is named and belongs to the default group" {
		create net_agent number: 1 returns: created_agents;
		net_agent ag <- created_agents[0];
		// a local UDP server, on a port chosen at random to limit the risk of collisions
		int local_port <- 20000 + rnd(20000);
		ask ag { do connect(to: "localhost", protocol: "udp_server", port: local_port, with_name: "solo"); }
		assert ag.network_name = "solo";
		assert ag.network_groups = ["ALL"];
		assert length(ag.network_server) = 1;
		assert not ag.has_more_message();
		ask created_agents { do die(); }
	}
}
