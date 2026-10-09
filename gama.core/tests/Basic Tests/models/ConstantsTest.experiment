/**
* Name: ConstantsTest
* Description: Tests the numeric constants (#nan, #infinity, #max_float, #min_float, #max_int, #min_int)
* 	and the mathematical ones (#pi, #e, #to_deg, #to_rad), with a focus on their edge-case behaviour
* Tags: constant, float, int, math, test
*/

experiment ConstantsTest type: test autorun: true {

	test "Undefined operations raise an error rather than returning a constant" {
		// by design, dividing by zero is an error and does not yield #infinity or #nan
		bool float_division_failed <- false;
		try { float f <- 1.0 / 0.0; } catch { float_division_failed <- true; }
		assert float_division_failed;

		bool zero_by_zero_failed <- false;
		try { float f <- 0.0 / 0.0; } catch { zero_by_zero_failed <- true; }
		assert zero_by_zero_failed;

		bool int_division_failed <- false;
		try { float f <- 1 / 0; } catch { int_division_failed <- true; }
		assert int_division_failed;

		bool div_failed <- false;
		try { int i <- 5 div 0; } catch { div_failed <- true; }
		assert div_failed;

		bool mod_failed <- false;
		try { int i <- 5 mod 0; } catch { mod_failed <- true; }
		assert mod_failed;

		bool sqrt_failed <- false;
		try { float f <- sqrt(-1); } catch { sqrt_failed <- true; }
		assert sqrt_failed;

		bool ln_failed <- false;
		try { float f <- ln(0); } catch { ln_failed <- true; }
		assert ln_failed;
	}
}
