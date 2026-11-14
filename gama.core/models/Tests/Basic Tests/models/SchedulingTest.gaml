/**
* Name: SchedulingTest
* Author: Antigravity
* Description: Tests what happens when the simulation steps its agents: the 'schedules' facet, the order of
* 	the reflexes, their conditions and the 'update' facet of the attributes
* Tags: scheduling, species, reflex, update, test
*/

model SchedulingTestModel

global {
	/** Filled by the agents as they are executed, to observe the order of execution */
	list<string> execution_log <- [];
}

species default_agent {
	int run_count <- 0;
	reflex act {
		run_count <- run_count + 1;
	}
}

species never_agent schedules: [] {
	int run_count <- 0;
	reflex act {
		run_count <- run_count + 1;
	}
}

species filtered_agent schedules: filtered_agent where each.enabled {
	bool enabled <- true;
	int run_count <- 0;
	reflex act {
		run_count <- run_count + 1;
	}
}

species ranked_agent schedules: ranked_agent sort_by (-each.rank) {
	int rank;
	reflex act {
		execution_log <+ "rank " + rank;
	}
}

species two_reflexes_agent {
	int value <- 1;
	// reflexes are executed in their order of definition: (1 * 2) + 1 and not (1 + 1) * 2
	reflex double_it {
		value <- value * 2;
	}
	reflex increment_it {
		value <- value + 1;
	}
}

species conditional_agent {
	bool enabled <- false;
	int run_count <- 0;
	reflex act when: enabled {
		run_count <- run_count + 1;
	}
}

species updated_agent {
	int counter <- 0 update: counter + 10;
	int seen_by_reflex <- -1;
	reflex look {
		seen_by_reflex <- counter;
	}
}

species mortal_agent {
	int age <- 0 update: age + 1;
	reflex pass_away when: age >= 2 {
		do die();
	}
}

species parent_agent {
	reflex give_birth {
		create child_agent number: 1;
	}
}

species child_agent { }

experiment SchedulingTest type: test autorun: true {

	test "A freshly created agent has not run yet" {
		list<default_agent> created_agents;
		ask simulation {create default_agent number: 1 returns: created_agents;}
		assert created_agents[0].run_count = 0;
		ask created_agents { do die(); }
	}

	test "Stepping the simulation runs the reflexes of its agents" {
		list<default_agent> created_agents;
		ask simulation {
		create default_agent number: 3 returns: created_agents;}
		ask simulation { do _step_(); }
		assert created_agents all_match (each.run_count = 1);
		ask simulation { do _step_(); }
		ask simulation { do _step_(); }
		assert created_agents all_match (each.run_count = 3);
		ask created_agents { do die(); }
	}

	test "An empty schedule prevents the agents from running" {
		list<never_agent> created_agents;
		ask simulation {
		create never_agent number: 2 returns: created_agents;}
		ask simulation { do _step_(); }
		ask simulation { do _step_(); }
		assert created_agents all_match (each.run_count = 0);

		// they can still be stepped manually: '_step_' executes the agent directly
		ask created_agents[0] { do _step_(); }
		assert created_agents[0].run_count = 1;
		assert created_agents[1].run_count = 0;
		ask created_agents { do die(); }
	}

	test "The schedules facet is evaluated at each step" {
				list<filtered_agent> created_agents;
		ask simulation {
		create filtered_agent number: 2 returns: created_agents;}
		filtered_agent kept <- created_agents[0];
		filtered_agent dropped <- created_agents[1];

		ask simulation { do _step_(); }
		assert kept.run_count = 1;
		assert dropped.run_count = 1;

		dropped.enabled <- false;
		ask simulation { do _step_(); }
		assert kept.run_count = 2;
		assert dropped.run_count = 1;

		dropped.enabled <- true;
		ask simulation { do _step_(); }
		assert kept.run_count = 3;
		assert dropped.run_count = 2;
		ask created_agents { do die(); }
	}

	test "The schedules facet drives the order of execution" {
		list<ranked_agent> created_agents;
		ask simulation {
		create ranked_agent number: 3 returns: created_agents;}
		created_agents[0].rank <- 1;
		created_agents[1].rank <- 3;
		created_agents[2].rank <- 2;
		simulation.execution_log <- [];
		ask simulation { do _step_(); }
		assert simulation.execution_log = ["rank 3", "rank 2", "rank 1"];
		ask created_agents { do die(); }
	}

	test "Reflexes run in their order of definition" {
		list<two_reflexes_agent> created_agents;
		ask simulation {
		create two_reflexes_agent number: 1 returns: created_agents;}
		ask simulation { do _step_(); }
		assert created_agents[0].value = 3;
		ask simulation { do _step_(); }
		assert created_agents[0].value = 7;
		ask created_agents { do die(); }
	}

	test "A reflex only runs when its condition holds" {
		list<conditional_agent> created_agents;
		ask simulation {
		create conditional_agent number: 1 returns: created_agents;}
		conditional_agent ag <- created_agents[0];
		ask simulation { do _step_(); }
		assert ag.run_count = 0;
		ag.enabled <- true;
		ask simulation { do _step_(); }
		assert ag.run_count = 1;
		ag.enabled <- false;
		ask simulation { do _step_(); }
		assert ag.run_count = 1;
		ask created_agents { do die(); }
	}

	test "Attributes are updated before the reflexes run" {
		list<updated_agent> created_agents;
		ask simulation {		
		create updated_agent number: 1 returns: created_agents;}
		updated_agent ag <- created_agents[0];
		assert ag.counter = 0;
		ask simulation { do _step_(); }
		assert ag.counter = 10;
		// the reflex already sees the updated value
		assert ag.seen_by_reflex = 10;
		ask simulation { do _step_(); }
		assert ag.counter = 20;
		assert ag.seen_by_reflex = 20;

		// stepping the agent itself only executes its behaviours, the 'update' facet is the scheduler's job
		ask ag { do _step_(); }
		assert ag.counter = 20;
		ask created_agents { do die(); }
	}

	test "Agents can die while being scheduled" {
				list<mortal_agent> created_agents;
		ask simulation { create mortal_agent number: 3 returns: created_agents; }
		ask simulation { do _step_(); }
		assert length(simulation.mortal_agent) = 3;
		assert created_agents all_match (each.age = 1);
		ask simulation { do _step_(); }
		assert empty(simulation.mortal_agent);
		assert created_agents all_match dead(each);
	}

	test "Agents can be created while the simulation steps" {
		list<parent_agent> created_agents;
		ask simulation { create parent_agent number: 2 returns: created_agents; }
		assert empty(simulation.child_agent);
		ask simulation { do _step_(); }
		assert length(simulation.child_agent) = 2;
		ask simulation { do _step_(); }
		assert length(simulation.child_agent) = 4;
		ask created_agents { do die(); }
		ask simulation.child_agent { do die(); }
	}
}
