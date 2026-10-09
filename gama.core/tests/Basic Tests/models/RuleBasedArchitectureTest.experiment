/**
* Name: RuleBasedArchitectureTest
* Author: Antigravity
* Description: Tests the rule-based control architecture: conditions, priorities and coexistence with reflexes
* Tags: architecture, rules, test
*/

model RuleBasedArchitectureTestModel

species rule_agent control: rules {
	int step_count <- 0;
	bool rule1_executed <- false;

	// A rule is executed if its condition is true
	do_rule rule1 when: step_count = 0 {
		rule1_executed <- true;
		step_count <- step_count + 1;
	}

	do_rule rule2 when: step_count > 0 {
		step_count <- step_count + 1;
	}
}

species prioritised_agent control: rules {
	list<string> fired <- [];
	int energy <- 0;
	float urgency <- 5.0;

	do_rule low when: energy < 5 priority: 1 {
		fired <+ "low";
	}

	do_rule high when: energy < 5 priority: 10 {
		fired <+ "high";
	}

	// the priority is an expression, evaluated at each step
	do_rule variable when: energy < 5 priority: urgency {
		fired <+ "variable";
	}

	do_rule never when: false priority: 100 {
		fired <+ "never";
	}
}

species mixed_agent control: rules {
	list<string> fired <- [];

	do_rule a_rule when: true {
		fired <+ "rule";
	}

	reflex a_reflex {
		fired <+ "reflex";
	}
}

experiment RuleBasedArchitectureTest type: test autorun: true {

	test "Rule Execution" {
		create rule_agent number: 1 returns: created_agents;
		rule_agent ag <- created_agents[0];

		assert ag.rule1_executed = false;
		assert ag.step_count = 0;

		// the conditions are all evaluated before any rule runs: only rule1 is eligible
		ask ag { do _step_(); }
		assert ag.rule1_executed = true;
		assert ag.step_count = 1;

		// now only rule2 is
		ask ag { do _step_(); }
		assert ag.step_count = 2;
		ask ag { do _step_(); }
		assert ag.step_count = 3;
	}

	test "Rules run by decreasing priority" {
		create prioritised_agent number: 1 returns: created_agents;
		prioritised_agent ag <- created_agents[0];

		ask ag { do _step_(); }
		assert ag.fired = ["high", "variable", "low"];

		ag.fired <- [];
		ag.urgency <- 50.0;
		ask ag { do _step_(); }
		assert ag.fired = ["variable", "high", "low"];

		ag.fired <- [];
		ag.urgency <- 0.0;
		ask ag { do _step_(); }
		assert ag.fired = ["high", "low", "variable"];
	}

	test "A rule whose condition does not hold is skipped whatever its priority" {
		create prioritised_agent number: 1 returns: created_agents;
		prioritised_agent ag <- created_agents[0];
		ask ag { do _step_(); }
		assert not (ag.fired contains "never");

		ag.fired <- [];
		ag.energy <- 10;
		ask ag { do _step_(); }
		assert empty(ag.fired);
	}

	test "Reflexes run before the rules" {
		create mixed_agent number: 1 returns: created_agents;
		mixed_agent ag <- created_agents[0];
		ask ag { do _step_(); }
		assert ag.fired = ["reflex", "rule"];
	}
}
