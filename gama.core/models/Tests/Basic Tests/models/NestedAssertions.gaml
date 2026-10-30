/**
* Name: NestedAssertions
* Description: Regression coverage for assertions reported from nested statement blocks.
* Tags: test
*/

experiment NestedAssertions type: test {
	test assertions_in_loop {
		loop i over: [1, 2, 3] {
			assert i > 0;
		}
	}

	test assertions_in_if {
		if true {
			assert true;
		}
	}

	test assertions_in_else {
		if false {
			assert false;
		} else {
			assert true;
		}
	}

	test assertions_in_catch {
		try {
			error "Expected error";
		} catch {
			assert true;
		}
	}

	test assertions_in_nested_blocks {
		loop i over: [1, 2] {
			if i = 1 {
				assert i = 1;
			} else {
				try {
					assert i = 2;
				} catch {
					assert false;
				}
			}
		}
	}
}
