/**
* Name: StatementsAdvancedTest
* Author: Antigravity
* Description: Tests advanced and obscure GAML statements (benchmark, let, trace, status)
* Tags: statement, advanced, test
*/

model StatementsAdvancedTestModel

species container_agent {
	species source_agent {
		int my_val <- 5;
	}
	
	species target_agent {
		int my_val <- 0;
	}
	
	action do_migrate() {
		migrate source: list(source_agent) target: target_agent;
	}

	action migrate_species() {
		migrate source: source_agent target: target_agent;
	}

	action migrate_one() {
		migrate source: source_agent[0] target: target_agent;
	}

	action migrate_subset() {
		migrate source: [source_agent[0]] target: target_agent;
	}

	action migrate_nil() {
		source_agent candidate <- nil;
		migrate source: candidate target: target_agent;
	}
}

experiment StatementsAdvancedTest type: test autorun: true {
	
	test "Let Statement" {
		// 'let' creates a local variable within the current scope
		let a type: int <- 10;
		assert a = 10;
		
		let b <- "hello";
		assert b = "hello";
	}
	
	test "Benchmark Statement" {
		// Benchmark statement logs the time taken by the inner block, which is executed normally.
		// Asserts are only taken into account directly inside a test, hence the variable declared outside
		int count <- 0;
		benchmark "Test Loop" {
			loop times: 100 {
				count <- count + 1;
			}
		}
		assert count = 100;

		// the block can be repeated
		int repeated <- 0;
		benchmark "Repeated" repeat: 5 {
			repeated <- repeated + 1;
		}
		assert repeated = 5;
	}

	test "Trace and Status Statements" {
		// Trace outputs execution traces for debugging
		trace "Tracing execution step";
		
		// Status sets the status bar in the GUI
		status "Testing status bar";

		// neither statement may disturb the surrounding computation
		int after <- 6 * 7;
		assert after = 42;
	}
	
	test "Migrate Statement" {
		create container_agent number: 1 returns: containers;
		container_agent c <- containers[0];

		ask c {
			create source_agent number: 2;
		}
		assert length(c.source_agent) = 2;
		assert length(c.target_agent) = 0;

		ask c {
			do do_migrate();
		}
		assert length(c.source_agent) = 0;
		assert length(c.target_agent) = 2;
		// the attributes are kept by the migrated agents
		assert c.target_agent[0].my_val = 5;
	}

	test "Migrate Statement accepts a species" {
		create container_agent number: 1 returns: containers;
		container_agent c <- containers[0];
		ask c {
			create source_agent number: 2;
			do migrate_species();
		}
		assert length(c.source_agent) = 0;
		assert length(c.target_agent) = 2;
		assert c.target_agent all_match (each.my_val = 5);
	}

	test "Migrate Statement accepts a single agent" {
		create container_agent number: 1 returns: containers;
		container_agent c <- containers[0];
		ask c {
			create source_agent number: 2;
			do migrate_one();
		}
		assert length(c.source_agent) = 1;
		assert length(c.target_agent) = 1;
		assert c.target_agent[0].my_val = 5;
	}

	test "Migrate Statement migrates only the supplied list" {
		create container_agent number: 1 returns: containers;
		container_agent c <- containers[0];
		ask c {
			create source_agent number: 2;
			do migrate_subset();
		}
		assert length(c.source_agent) = 1;
		assert length(c.target_agent) = 1;
		assert c.target_agent[0].my_val = 5;
	}

	test "Migrate Statement accepts an empty list" {
		create container_agent number: 1 returns: containers;
		container_agent c <- containers[0];
		ask c {
			do do_migrate();
		}
		assert empty(c.source_agent);
		assert empty(c.target_agent);
	}

	test "Migrate Statement reports a nil source" {
		create container_agent number: 1 returns: containers;
		container_agent c <- containers[0];
		string problem <- nil;
		try {
			ask c {
				do migrate_nil();
			}
		} catch {
			problem <- #current_error;
		}
		assert problem != nil;
		assert problem contains "the source must be an agent";
		assert empty(c.source_agent);
		assert empty(c.target_agent);
	}

	test "Highlight, Focus, and Using Statements" {
		create container_agent number: 1 returns: created_agents;
		container_agent ag <- created_agents[0];

		// Highlight sets the GUI focus
		highlight ag color: #red;

		// Using statement dynamically changes the topology/scope
		int seen_in_world_topology <- 0;
		using topology(world) {
			seen_in_world_topology <- length(container_agent); 
		}
		assert seen_in_world_topology = length(container_agent);
		assert seen_in_world_topology >= 1;
	}
}
