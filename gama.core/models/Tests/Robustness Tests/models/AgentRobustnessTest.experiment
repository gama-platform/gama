/**
* Name: AgentRobustnessTest
* Description: Feeds agents, populations and the moving skill with absurd values (null or negative numbers of
* 	agents, values out of the bounds of an attribute, absurd speeds and locations, out of range indexes).
* 	An invalid input may be rejected, but with a GAML error and not with an internal Java exception; when it is
* 	accepted, the state of the agents must remain valid
* Tags: robustness, agent, species, moving, test
*/

model AgentRobustnessTestModel

global {
	geometry shape <- square(100);
}

species bounded_agent {
	int level <- 1 min: 0 max: 10;
	float ratio <- 0.5 min: 0.0 max: 1.0;
}

species walker skills: [moving] { 
	float speed <- 0.0 min: 0.0;
}

grid cell width: 3 height: 3 { }

experiment AgentRobustnessTest type: test autorun: true {

	bool handled(string problem) {
		return problem = nil ? true : !(problem contains "Java error");
	}

	test "Creating no agent returns an empty list" {
		string problem <- nil;
		list<bounded_agent> created <- [];
		int before <- length(bounded_agent);
		try { create bounded_agent number: 0 returns: none_created; created <- none_created; } catch { problem <- #current_error; }
		assert handled(problem);
		assert created != nil;
		assert empty(created);
		assert length(bounded_agent) = before;
	}

	test "Creating a negative number of agents creates nothing" {
		string problem <- nil;
		list<bounded_agent> created <- [];
		int before <- length(bounded_agent);
		try { create bounded_agent number: -3 returns: none_created; created <- none_created; } catch { problem <- #current_error; }
		assert handled(problem);
		assert created != nil;
		assert empty(created);
		assert length(bounded_agent) = before;
	}

	test "Bounded attributes stay within their bounds" {
		create bounded_agent returns: created;
		bounded_agent ag <- created[0];
		ag.level <- 50;
		assert ag.level = 10;
		ag.level <- -50;
		assert ag.level = 0;
		ag.ratio <- #infinity;
		assert ag.ratio = 1.0;
		ag.ratio <- #nan;
		assert ag.ratio >= 0.0 and ag.ratio <= 1.0;
		ask created { do die(); }
	}

	test "A negative speed does not move an agent away from its target" {
		// Note: negative speeds are ok in the driving skill. So the test is made with a species 
		// that explicitly sets a minimum value of zero. 
		walker w <- walker(location:{50, 50}, speed: -5.0);
		ask w {
			do goto (target: {60, 50});
		}
		assert (w.location distance_to {60, 50}) <= 10.0;
		ask w { do die(); }
	}

	test "An infinite speed reaches the target without going beyond" {
		create walker returns: created;
		walker w <- created[0];
		w.location <- {50, 50};
		ask w {
			speed <- #infinity;
			do goto (target: {60, 50});
		}
		assert w.location = {60, 50};
		ask created { do die(); }
	}

	test "A speed that is not a number does not corrupt the location" {
		create walker returns: created;
		walker w <- created[0];
		w.location <- {50, 50};
		string problem <- nil;
		try {
			ask w {
				speed <- #nan;
				do goto (target: {60, 50});
			}
		} catch { problem <- #current_error; }
		assert handled(problem);
		assert is_number(w.location.x) and is_number(w.location.y);
		ask created { do die(); }
	}

	test "Going nowhere" {
		create walker returns: created;
		walker w <- created[0];
		w.location <- {50, 50};
		string problem <- nil;
		try { ask w { do goto (target: nil); } } catch { problem <- #current_error; }
		assert handled(problem);
		assert w.location = {50, 50};
		ask created { do die(); }
	}

	test "A location that is not a number is rejected" {
		create walker returns: created;
		walker w <- created[0];
		w.location <- {50, 50};
		string problem <- nil;
		try { w.location <- {#nan, #nan}; } catch { problem <- #current_error; }
		assert handled(problem);
		assert is_number(w.location.x) and is_number(w.location.y);
		ask created { do die(); }
	}

	test "Headings are normalised" {
		create walker returns: created;
		walker w <- created[0];
		w.heading <- 765.0;
		assert w.heading = 45.0;
		w.heading <- -90.0;
		assert w.heading = 270.0;
		ask created { do die(); }
	}

	test "Accessing a population out of its bounds" {
		create bounded_agent number: 2 returns: created;
		// the casting of 99 as a bounded_agent does not raise an error, but simply returns nil
		assert bounded_agent(99) = nil;
		assert bounded_agent[0] = created[0];
		assert bounded_agent[1] = created[1];
		string problem <- nil;
		try { bounded_agent ag <- bounded_agent[5]; } catch { problem <- #current_error; }
		write problem;
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Index 5 out of bounds for population 'bounded_agent' (size: 2)";
		problem <- nil;
		try { bounded_agent ag <- bounded_agent[-1]; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Index -1 out of bounds for population 'bounded_agent' (size: 2)";
		problem <- nil;
		try { bounded_agent ag <- bounded_agent[2]; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Index 2 out of bounds for population 'bounded_agent' (size: 2)";
		problem <- nil;
		try { bounded_agent ag <- bounded_agent at 2; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Index 2 out of bounds for population 'bounded_agent' (size: 2)";
		ask created { do die(); }
		problem <- nil;
		try { bounded_agent ag <- bounded_agent[0]; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Index 0 out of bounds for population 'bounded_agent' (size: 0)";
	}

	test "Accessing a grid out of its bounds" {
		assert cell[10, 10] = nil;
		assert cell[-1, 0] = nil;
		string problem <- nil;
		try { cell c <- cell grid_at {5, 5}; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
	}

	test "Dead and nil agents" {
		assert dead(nil);
		bounded_agent ag <- bounded_agent();
		ask ag { do die(); }
		assert dead(ag);
		// killing an agent twice is harmless
		string problem <- nil;
		try { ask ag { do die(); } } catch { problem <- #current_error; }
		assert handled(problem);
		problem <- nil;
		try { float d <- ag distance_to nil; } catch { problem <- #current_error; }
		write problem;
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Cannot compute distance_to: the source and target must not be nil";
	}

	test "Distances reject nil sources and targets" {
		geometry present_shape <- square(1);
		geometry absent_shape <- nil;
		point present_point <- {0, 0};
		point absent_point <- nil;
		assert present_shape distance_to present_shape = 0.0;
		assert present_point distance_to {3, 4} = 5.0;
		string problem <- nil;
		try { float d <- present_shape distance_to absent_shape; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Cannot compute distance_to: the source and target must not be nil";
		problem <- nil;
		try { float d <- absent_shape distance_to present_shape; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Cannot compute distance_to: the source and target must not be nil";
		problem <- nil;
		try { float d <- present_point distance_to absent_point; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Cannot compute distance_to: the source and target must not be nil";
		problem <- nil;
		try { float d <- absent_point distance_to present_point; } catch { problem <- #current_error; }
		assert problem != nil;
		assert handled(problem);
		assert problem contains "Cannot compute distance_to: the source and target must not be nil";
	}
}
