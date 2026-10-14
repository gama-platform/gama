/**
* Name: DatePropertiesTest
* Description: Checks invariants of the date operators on random dates and durations drawn from a fixed seed:
* 	shifting forth and back, consistency between the shifting operators and the arithmetic on durations, and
* 	between the order of dates and their differences. Each test lists the inputs that break the invariant
* Tags: property, date, random, test
*/

experiment DatePropertiesTest type: test autorun: true {

	int cases <- 200;

	date random_date() {
		return date([rnd(1900, 2100), rnd(1, 12), rnd(1, 28), rnd(0, 23), rnd(0, 59), rnd(0, 59)]);
	}

	test "Shifting by a fixed duration forth and back gives back the date" {
		seed <- 1.0;
		list<string> failures <- [];
		loop times: cases {
			date d <- random_date();
			int n <- rnd(-1000, 1000);
			if ((d plus_seconds n) minus_seconds n != d) { failures <+ "seconds " + n + " " + d; }
			if ((d plus_minutes n) minus_minutes n != d) { failures <+ "minutes " + n + " " + d; }
			if ((d plus_hours n) minus_hours n != d) { failures <+ "hours " + n + " " + d; }
			if ((d plus_days n) minus_days n != d) { failures <+ "days " + n + " " + d; }
			if ((d plus_weeks n) minus_weeks n != d) { failures <+ "weeks " + n + " " + d; }
			if ((d plus_ms (n * 1000)) minus_ms (n * 1000) != d) { failures <+ "ms " + n + " " + d; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "shifts: " + failures; }
		assert empty(failures);
	}

	test "Shifting by months and years forth and back keeps the date, the 28 first days of a month" {
		seed <- 2.0;
		list<string> failures <- [];
		loop times: cases {
			date d <- random_date();
			int n <- rnd(-100, 100);
			if ((d plus_months n) minus_months n != d) { failures <+ "months " + n + " " + d; }
			if ((d plus_years n) minus_years n != d) { failures <+ "years " + n + " " + d; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "calendar shifts: " + failures; }
		assert empty(failures);
	}

	test "The shifting operators agree with the arithmetic on durations" {
		seed <- 3.0;
		list<string> failures <- [];
		loop times: cases {
			date d <- random_date();
			int n <- rnd(-1000, 1000);
			if (d plus_seconds n != d + n) { failures <+ "seconds " + n + " " + d; }
			if (d plus_hours n != d + n #h) { failures <+ "hours " + n + " " + d; }
			if (d plus_days n != d + n #day) { failures <+ "days " + n + " " + d; }
			if (d plus_weeks n != d + n #week) { failures <+ "weeks " + n + " " + d; }
			if ((d + n #day) - d != n * 86400.0) { failures <+ "difference " + n + " " + d; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "durations: " + failures; }
		assert empty(failures);
	}

	test "The order of dates is the sign of their difference" {
		seed <- 4.0;
		list<string> failures <- [];
		loop times: cases {
			date a <- random_date();
			date b <- random_date();
			float difference <- a - b;
			if ((a < b) != (difference < 0)) { failures <+ string(a) + " " + b; }
			if ((a = b) != (difference = 0)) { failures <+ "equality " + a + " " + b; }
			if (milliseconds_between(b, a) != difference * 1000) { failures <+ "milliseconds_between " + a + " " + b; }
			if (signum(years_between(b, a)) * signum(difference) < 0) { failures <+ "years_between " + a + " " + b; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "ordering: " + failures; }
		assert empty(failures);
	}

	test "A date survives being formatted and parsed" {
		seed <- 5.0;
		list<string> failures <- [];
		loop times: cases {
			date d <- random_date();
			if (date(string(d, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") != d) { failures <+ string(d); }
			if (date([d.year, d.month, d.day, d.hour, d.minute, d.second]) != d) { failures <+ "components " + d; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "formatting: " + failures; }
		assert empty(failures);
	}
}
