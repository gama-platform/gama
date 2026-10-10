/**
* Name: FSMTest
* Author: Antigravity
* Description: Tests the finite state machine (FSM) architecture
* Tags: fsm, architecture, test
*/

model FSMTestModel

species fsm_agent control: fsm {
	int counter <- 0;
	bool visited_init <- false;
	bool visited_second <- false;
	
	state init_state initial: true {
		enter { visited_init <- true; }
		counter <- counter + 1;
		transition to: second_state when: counter = 1;
	}
	
	state second_state {
		enter { visited_second <- true; }
		counter <- counter + 1;
		transition to: final_state when: counter >= 2;
	}
	
	state final_state final: true {
		counter <- 100;
	}
}

experiment FSMTest type: test autorun: true {
	
	test "FSM Transitions and Enter Actions" {
		create fsm_agent number: 1 returns: created_agents;
		fsm_agent ag <- created_agents[0];
		
		assert ag.state = "init_state";
		
		// Run first step (executes init_state)
		ask ag { do _step_(); }
		
		// Because counter became 1, transition to second_state happens at the end of step
		assert ag.visited_init = true;
		assert ag.counter = 1;
		
		// After transition, the agent state updates, but the next step hasn't run yet.
		// Wait, _step_ runs the state and then handles transitions, so state should be updated.
		assert ag.state = "second_state";
		
		// Run second step
		ask ag { do _step_(); }
		assert ag.visited_second = true;
		assert ag.counter = 2;
		assert ag.state = "final_state";
		
		// Run third step (final state)
		ask ag { do _step_(); }
		assert ag.counter = 100;
		assert ag.state = "final_state";
	}
}
