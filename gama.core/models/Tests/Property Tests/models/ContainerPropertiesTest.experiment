/**
* Name: ContainerPropertiesTest
* Description: Checks invariants of the container operators on random lists drawn from a fixed seed: reversals,
* 	sorts, duplicates, set operations, shuffles and slices. Each test lists the inputs that break the invariant
* Tags: property, list, map, random, test
*/

experiment ContainerPropertiesTest type: test autorun: true {

	int cases <- 200;

	list<int> random_list() {
		list<int> result <- [];
		loop times: rnd(0, 20) {
			result <+ rnd(-10, 10);
		}
		return result;
	}

	test "Reversing twice gives back the list" {
		seed <- 1.0;
		list<string> failures <- [];
		loop times: cases {
			list<int> l <- random_list();
			if (reverse(reverse(l)) != l) { failures <+ string(l); }
			if (length(reverse(l)) != length(l)) { failures <+ "length " + l; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "reverse: " + failures; }
		assert empty(failures);
	}

	test "Sorting is idempotent and orders the elements" {
		seed <- 2.0;
		list<string> failures <- [];
		loop times: cases {
			list<int> l <- random_list();
			list<int> sorted <- l sort_by each;
			if ((sorted sort_by each) != sorted) { failures <+ "idempotence " + l; }
			if (length(sorted) != length(l)) { failures <+ "length " + l; }
			if (length(sorted) > 1) {
				loop i from: 0 to: length(sorted) - 2 {
					if (sorted[i] > sorted[i + 1]) { failures <+ "order " + l; }
				}
			}
			// sorting keeps the same elements
			if (sum(sorted) != sum(l) or !(sorted contains_all l)) { failures <+ "elements " + l; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "sort_by: " + failures; }
		assert empty(failures);
	}

	test "Removing duplicates" {
		seed <- 3.0;
		list<string> failures <- [];
		loop times: cases {
			list<int> l <- random_list();
			list<int> d <- distinct(l);
			if (length(d) > length(l)) { failures <+ "longer " + l; }
			if (!(d contains_all l) or !(l contains_all d)) { failures <+ "elements " + l; }
			if (length(d) != length(remove_duplicates(d))) { failures <+ "still duplicated " + l; }
			if (distinct(d) != d) { failures <+ "idempotence " + l; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "distinct: " + failures; }
		assert empty(failures);
	}

	test "Set operations" {
		seed <- 4.0;
		list<string> failures <- [];
		loop times: cases {
			list<int> a <- random_list();
			list<int> b <- random_list();
			list<int> u <- a union b;
			list<int> i <- a inter b;
			if (!(u contains_all a) or !(u contains_all b)) { failures <+ "union " + a + " " + b; }
			if (!(i all_match ((a contains each) and (b contains each)))) { failures <+ "inter " + a + " " + b; }
			if (length(distinct(u)) != length(u)) { failures <+ "union has duplicates " + a + " " + b; }
			if (((a inter b) sort_by each) != ((b inter a) sort_by each)) { failures <+ "inter not commutative " + a + " " + b; }
			list<int> difference <- a - b;
			if (difference contains_any b) { failures <+ "difference " + a + " " + b; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "set operations: " + failures; }
		assert empty(failures);
	}

	test "Shuffling keeps the same elements" {
		seed <- 5.0;
		list<string> failures <- [];
		loop times: cases {
			list<int> l <- random_list();
			list<int> s <- shuffle(l);
			if ((s sort_by each) != (l sort_by each)) { failures <+ string(l); }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "shuffle: " + failures; }
		assert empty(failures);
	}

	test "Slices and first / last" {
		seed <- 6.0;
		list<string> failures <- [];
		loop times: cases {
			list<int> l <- random_list();
			int n <- rnd(0, 25);
			list<int> head <- first(n, l);
			list<int> tail <- last(max(0, length(l) - n), l);
			if (head + tail != l) { failures <+ "first/last " + n + " " + l; }
			if (length(head) != min(n, length(l))) { failures <+ "first length " + n + " " + l; }
			if (copy_between(l, 0, length(l)) != l) { failures <+ "copy_between " + l; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "slices: " + failures; }
		assert empty(failures);
	}

	test "Searching" {
		seed <- 7.0;
		list<string> failures <- [];
		loop times: cases {
			list<int> l <- random_list();
			if (empty(l)) { continue; }
			int value <- one_of(l);
			int first_index <- l index_of value;
			int last_index <- l last_index_of value;
			if (l[first_index] != value or l[last_index] != value or first_index > last_index) {
				failures <+ string(value) + " in " + l;
			}
			if (length(l all_indexes_of value) != l count (each = value)) { failures <+ "all_indexes_of " + value + " in " + l; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "index_of: " + failures; }
		assert empty(failures);
	}

	test "Aggregations" {
		seed <- 8.0;
		list<string> failures <- [];
		loop times: cases {
			list<int> l <- random_list();
			if (empty(l)) { continue; }
			if (min(l) > max(l)) { failures <+ "min > max " + l; }
			if (abs(mean(l) * length(l) - sum(l)) > 1e-9) { failures <+ "mean " + l; }
			if (mean(l) < min(l) or mean(l) > max(l)) { failures <+ "mean out of range " + l; }
			if (abs(standard_deviation(l) - sqrt(variance(l))) > 1e-9) { failures <+ "standard deviation " + l; }
			if (median(l) < min(l) or median(l) > max(l)) { failures <+ "median out of range " + l; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "aggregations: " + failures; }
		assert empty(failures);
	}

	test "Maps rebuilt from their pairs are equal" {
		seed <- 9.0;
		list<string> failures <- [];
		loop times: cases {
			list<int> keys <- distinct(random_list());
			map<int, int> m <- keys as_map (each::each * 2);
			if (map(m.pairs) != m) { failures <+ string(m); }
			if (create_map(m.keys, m.values) != m) { failures <+ "create_map " + m; }
			if (length(m.keys) != length(m.values)) { failures <+ "sizes " + m; }
		}
		// the inputs that break the invariant are listed in the error
		if (!empty(failures)) { error "maps: " + failures; }
		assert empty(failures);
	}
}
