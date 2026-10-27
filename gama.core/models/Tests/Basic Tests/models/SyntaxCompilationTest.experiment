/**
* Name: SyntaxCompilationTest
* Author: Antigravity
* Description: Tests the compilation and execution of GUI-specific blocks (headless sanity check)
* Tags: syntax, display, chart, test
*/

model SyntaxCompilationTestModel

species dummy_chart_agent {
	float val <- rnd(10.0);
	
	aspect default {
		draw circle(1) color: #green;
	}
}

experiment SyntaxCompilationTest type: test autorun: true {

	int action_calls <- 0;

	action my_action() {
		action_calls <- action_calls + 1;
	}

	test "Display and Chart Syntax Sanity" {
		create dummy_chart_agent number: 5;
		assert length(dummy_chart_agent) = 5;
	}

	// the action bound to the user command and to the mouse event below can be called like any other
	user_command "My Command" action: my_action;

	test "Actions bound to user commands and events remain callable" {
		assert action_calls = 0;
		do my_action();
		do my_action();
		assert action_calls = 2;
	}

	output {
		display "My Chart Display" {
			chart "Dummy Chart" {
				data "My Data" value: length(dummy_chart_agent) color: #blue;
			}
		}
		
		display "My Map Display" {
			species dummy_chart_agent; // Will use 'default' aspect
			graphics "Overlay" {
				draw circle(5) color: #red;
			}
			event "mouse_click" action: my_action;
		}
	}
}
