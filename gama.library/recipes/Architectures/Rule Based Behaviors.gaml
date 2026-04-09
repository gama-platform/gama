/***
* Name: Rule Based Behaviors
* Author: Alexis Drogoul
* Description: Demonstrates the use of the basic rule-based control architecture available in GAML. In this architecture,
*   agents define rules instead of reflexes — each rule has a condition and a priority. At each simulation step, only
*   the rule(s) whose condition is true are fired, in decreasing order of priority. This allows modelers to write
*   reactive agents with priority-ordered behaviors without complex conditional logic. For more complex BDI reasoning,
*   see the simple_bdi control architecture.
* Tags: rule, behavior, architecture, control, priority, reactive
***/

model RuleBasedBehaviors

global {
	init {
		create simple_rules_statements;
	}
}


/**
 * In this species, two rules and one reflex are defined. 
 * The rules are fired (executed) when their condition becomes true and in the order 
 * defined by their decreasing priorities. 
 */
species simple_rules_statements control: rules {
	
	int priority_of_a <- 0 update: rnd(100);
	int priority_of_b <- 0 update: rnd(100);
	
	reflex show_priorities {
		write " Priority of rule a = " + priority_of_a + ", priority of rule b = " + priority_of_b;
	}

	do_rule a when: priority_of_a < 50 priority: priority_of_a {
		write "Rule a fired with priority: " + priority_of_a;
	}
	
	do_rule b when: priority_of_b > 25 priority: priority_of_b {
		write "Rule b fired with priority: " + priority_of_b; 
	}
}

experiment "Try it";