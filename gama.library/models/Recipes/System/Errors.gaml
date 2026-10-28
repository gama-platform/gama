/**
* Name: Errors
* Author: Alexis Drogoul
* Description: Runtime errors, warnings, recovery and preferences. Run each numbered experiment separately.
* Tags: GAML, syntax, error, warning, exception, try, catch, handling
*
* Three different events must not be confused:
* - Aborting an operation: a thrown error abandons the current statement. A failed action call or assignment
*   does not produce a normal result. An enclosing try block transfers control to catch.
* - Pausing a simulation: for an unhandled error, "Stop simulation at first error" (gama.pref_errors_stop)
*   decides whether execution also stops at the reporting boundary. If disabled, later statements can run;
*   the failed statement itself is not resumed. Pausing does not roll back earlier changes.
* - Recovering with a fallback: some operations deliberately report a diagnostic and return a documented
*   alternative. This is not the same as throwing an error. Read each operator's special cases.
*
* Warnings normally allow execution to continue. "Treat warnings as errors" (gama.pref_errors_warnings_errors)
* together with "Stop simulation at first error" makes reported warnings stop execution. A warning that is
* thrown rather than just reported still abandons its operation, even when warnings are not escalated.
*
* try/catch intercepts thrown errors AND warnings, independently of these preferences, before they are reported
* to the Errors view. Statements after the failing statement inside try are skipped; execution resumes after
* catch. There is no automatic rollback. Prefer a small try block around the operation you can recover from.
*
* #current_error is a message string from the current scope's recorded error or warning, not an exception
* object. It is not cleared by a later successful operation, so a non-empty value is not a success/failure
* test for a new operation. Read it in catch or together with an operation's documented failure return.
*
* is_error(expression) and is_warning(expression) detect exceptions thrown while evaluating an expression.
* They do not force report-only operations to throw, and do not by themselves record #current_error.
* They evaluate the expression, so its side effects are real. Use try/catch for sequences and assignments.
*
* Recovery contracts outside this model:
* - sparql_query: a query failure reports a non-blocking error and returns nil outside try/catch; inside try,
*   it throws to catch instead. nil is not an empty dataframe.
* - network connect: an exception from the connector's connection attempt reports a non-blocking error and
*   returns false outside try/catch; inside try it throws instead. Setup/argument errors may propagate.
* - string(date, pattern): an invalid Java pattern is reported on GAMA's runtime scope and falls back to
*   the default date formatter. This does not use the caller's try mode or reliably populate its #current_error.
*
* These examples do not change global preferences. To compare behavior, toggle the two preferences in
* Preferences > Runtime > Errors and rerun experiments 1 and 2. Display/background reporting is separate.
*/
model Errors

global {
	int divisor <- 0;
}

species error_probe {
	int value <- 5;
	int square() {
		return value * value;
	}
}

experiment "1 - Report a warning" {
	init {
		write "With warning escalation AND stopping enabled, the next warning stops execution.";
		warn "A reported warning";
		write "Execution continued after the warning.";
	}
}

experiment "2 - Report an unhandled error" {
	init {
		write "With stopping enabled, the next error stops execution.";
		error "An unhandled error";
		write "Stopping is disabled: execution continued after the failed statement.";
		write #current_error;
	}
}

experiment "3 - Catch a nil action call" {
	init {
		map<string, error_probe> samples <- [];
		bool reached <- false;
		try {
			write samples["missing"].square();
			reached <- true;
		} catch {
			write "Caught action failure: " + #current_error;
		}
		assert !reached;
		write "Execution resumes after catch, with either stopping preference.";
	}
}

experiment "4 - Catch an invalid assignment" {
	init {
		error_probe absent <- nil;
		bool reached <- false;
		try {
			absent.value <- 42;
			reached <- true;
		} catch {
			write "Caught assignment failure: " + #current_error;
		}
		assert !reached;
		write "The failed assignment was abandoned, not retried.";
	}
}

experiment "5 - Catch division by zero" {
	init {
		bool reached <- false;
		try {
			write 5 mod divisor;
			reached <- true;
		} catch {
			write "Caught arithmetic failure: " + #current_error;
		}
		assert !reached;
		write "The same pattern works for a failed assert.";
		try {
			assert false;
		} catch {
			write "Caught assertion: " + #current_error;
		}
	}
}

experiment "6 - Evaluate GAML safely" {
	init {
		bool reached <- false;
		try {
			write eval_gaml("5 mod divisor");
			reached <- true;
		} catch {
			write "Caught evaluation failure: " + #current_error;
		}
		assert !reached;
		assert eval_gaml("nil") = nil;
		write "nil can be a valid evaluation result; use catch to distinguish failure.";
	}
}

experiment "7 - Inspect thrown errors and warnings" {
	init {
		assert is_error(5 mod divisor);
		assert !is_error(nil);
		assert is_warning(set_z(triangle(3), -1, 0.0));
		write "Inspection catches thrown exceptions, without pausing or forcing report-only failures to throw.";
	}
}

experiment "8 - Default date formatting fallback" {
	init {
		date moment <- date("2026-01-02T03:04:05");
		write "An invalid Java pattern is reported, then the default formatter is used:";
		write string(moment, "invalid[");
		write "Execution continues, but the requested pattern was NOT applied.";
	}
}

experiment "9 - Deliberately ignore a failure" {
	init {
		bool reached <- false;
		try {
			error "Deliberately ignored";
			reached <- true;
		}
		assert !reached;
		write "A try without catch discards the thrown failure. Use only if ignoring it is safe.";
	}
}

experiment "Error handling regression checks" type: test {
	test "Nil action calls are catchable" {
		error_probe absent <- nil;
		bool caught <- false;
		bool reached <- false;
		int changed <- 0;
		try {
			changed <- 1;
			write absent.square();
			reached <- true;
		} catch {
			caught <- true;
			assert #current_error != "nil";
		}
		assert caught and !reached;
		assert changed = 1; // catch does not roll back earlier changes
		string previous_error <- #current_error;
		assert is_error(absent.square());
		assert #current_error = previous_error;
		assert eval_gaml("2 + 3") = 5;
		assert #current_error = previous_error;
	}

	test "Invalid assignments are catchable" {
		error_probe absent <- nil;
		bool caught <- false;
		bool reached <- false;
		try {
			absent.value <- 42;
			reached <- true;
		} catch {
			caught <- true;
			assert #current_error != "nil";
		}
		assert caught and !reached;
	}

	test "Modulo fails before computing a result" {
		bool caught <- false;
		bool reached <- false;
		try {
			write 5 mod divisor;
			reached <- true;
		} catch {
			caught <- true;
			assert #current_error = "Division by zero";
		}
		assert caught and !reached;
		assert is_error(5 mod divisor);
		assert 5 mod 2 = 1;
	}

	test "Evaluation errors are not nil results" {
		bool caught <- false;
		bool reached <- false;
		try {
			write eval_gaml("5 mod divisor");
			reached <- true;
		} catch {
			caught <- true;
			assert #current_error = "Division by zero";
		}
		assert caught and !reached;
		assert is_error(eval_gaml("5 mod divisor"));
		assert eval_gaml("nil") = nil;
		assert eval_gaml("2 + 3") = 5;
		assert is_error(eval_gaml(nil));
		assert is_error(eval_gaml("missing_symbol_820"));
		assert is_warning(eval_gaml("set_z(triangle(3), -1, 0.0)"));
	}

	test "Warnings can be caught" {
		bool caught_warning <- false;
		try {
			warn "A handled warning";
		} catch {
			caught_warning <- true;
		}
		assert caught_warning;
		assert is_warning(set_z(triangle(3), -1, 0.0));
		assert !is_error(nil);
	}
}
