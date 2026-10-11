/**
* Name: EquationTest
* Author: Antigravity
* Description: Tests the equation solving (ODE) engine
* Tags: math, equation, test
*/

model EquationTestModel

global {
	float step <- 0.1;
}

species ode_agent {
	float t <- 0.0;
	float S <- 100.0;
	
	equation decay {
		diff(S, t) = -0.1 * S;
	}
}

experiment EquationTest type: test autorun: true {
	
	test "ODE System Solving" {
		create ode_agent number: 1 returns: created_agents;
		ode_agent ag <- created_agents[0];
		
		assert ag.S = 100.0;
		
		ask ag {
			solve decay method: "rk4" step_size: 0.1;
		}
		
		// Expected theoretical decay: S(t) = S0 * exp(-k * t)
		// S(0.1) = 100 * exp(-0.1 * 0.1) = 100 * exp(-0.01) = 99.00498
		
		assert ag.S < 100.0;
		assert (ag.S > 99.0);
	}
}
