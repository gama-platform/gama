/**
* Name: ControlStatementsTest
* Author: Antigravity
* Description: Tests control statements (if, switch, match, loop variations)
* Tags: control, statement, test
*/

experiment ControlStatementsTest type: test autorun: true {
	
	test "If Else Statement" {
		int a <- 10;
		int result <- 0;
		
		if (a > 5) {
			result <- 1;
		} else {
			result <- 2;
		}
		
		assert result = 1;
		
		if (a < 5) {
			result <- 3;
		} else if (a = 10) {
			result <- 4;
		}
		
		assert result = 4;
	}
	
	test "Switch Match Statement" {
		string state <- "run";
		int code <- 0;
		
		switch state {
			match "stop" { code <- 1; }
			match "run" { code <- 2; }
			match_one ["walk", "jog"] { code <- 3; }
			default { code <- 4; }
		}
		
		assert code = 2;
		
		switch "jog" {
			match "stop" { code <- 1; }
			match "run" { code <- 2; }
			match_one ["walk", "jog"] { code <- 3; }
			default { code <- 4; }
		}
		
		assert code = 3;
		
		int numeric_val <- 15;
		switch numeric_val {
			match_between [0, 10] { code <- 1; }
			match_between [11, 20] { code <- 2; }
			default { code <- 3; }
		}
		
		assert code = 2;
		
		switch "unknown" {
			match "stop" { code <- 1; }
			default { code <- 4; }
		}
		
		assert code = 4;
	}
	
	test "Loop Variations" {
		// Loop times
		int count <- 0;
		loop times: 5 {
			count <- count + 1;
		}
		assert count = 5;
		
		// Loop while
		count <- 0;
		loop while: count < 3 {
			count <- count + 1;
		}
		assert count = 3;
		
		// Loop from to
		count <- 0;
		loop i from: 1 to: 4 {
			count <- count + i; // 1 + 2 + 3 + 4 = 10
		}
		assert count = 10;
		
		// Loop over
		count <- 0;
		loop element over: [10, 20, 30] {
			count <- count + element;
		}
		assert count = 60;
		
		// Loop break and continue
		count <- 0;
		loop i from: 1 to: 10 {
			if (i = 5) { break; }
			if (i mod 2 = 0) { continue; }
			count <- count + i;
		}
		// i goes 1, 2, 3, 4. 1 and 3 are odd (added). 2 and 4 are even (skipped). 5 breaks.
		// Sum = 1 + 3 = 4
		assert count = 4;
	}
}
