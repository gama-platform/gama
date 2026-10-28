/***
* Name: Reduce
* Description: Combines the elements of a container into one result with the reduce operator.
*   In the reducer body, @ is the accumulated value and each is the current element. Examples cover
*   initial values, left-to-right evaluation, strings, maps, matrices, populations, and nested reductions.
*   Run the experiment to see the results in the console. Assertions check the expected values.
* Tags: reduce, iterator, container, list, map, matrix, population, functional, aggregation
***/

model Reduce

global {
	init {
		do basic_reductions();
		do initial_values();
		do evaluation_order();
		do other_containers();
		do population_reduction();
		do nested_reductions();
		write "All reduce examples completed successfully.";
	}

	action basic_reductions() {
		list<int> numbers <- [1, 2, 3, 4];

		// Without a seed, @ starts at the first element and each starts at the second.
		int total <- numbers reduce (@ + each);
		int product <- numbers reduce (@ * each);
		assert total = 10;
		assert product = 24;
		write "Sum: " + total + "; product: " + product;

		int loop_total <- 0;
		loop number over: numbers {
			loop_total <- loop_total + number;
		}
		assert total = loop_total;

		// The function-call form is equivalent to the infix form.
		assert reduce(numbers, @ + each) = total;

		string joined <- ["red", "green", "blue"] reduce (@ + ", " + each);
		assert joined = "red, green, blue";
		write "Joined strings: " + joined;
	}

	action initial_values() {
		// With a seed, every element is processed, including the first.
		int with_seed <- [1, 2, 3] reduce (10, @ + each);
		assert with_seed = 16;
		assert reduce([1, 2, 3], 10, @ + each) = with_seed;

		// The seed determines the accumulator and result type, not the element type.
		int total_length <- ["a", "bb", "ccc"] reduce (0, @ + length(each));
		float real_total <- [1, 2, 3] reduce (0.0, @ + each);
		assert total_length = 6;
		assert real_total = 6.0;
		write "Total string length: " + total_length + "; floating-point sum: " + real_total;

		// An empty container returns its seed without evaluating the body.
		list<int> empty_numbers <- [];
		assert (empty_numbers reduce (0, @ + each)) = 0;
		assert (empty_numbers reduce (1, @ * each)) = 1;
		assert ([] reduce (0, @ + each)) = 0;

		// A singleton without a seed returns its sole element without evaluating the body.
		assert ([7] reduce (@ + each)) = 7;

		// An empty container WITHOUT a seed raises a runtime error:
		// empty_numbers reduce (@ + each)
		// Likewise, @ is not accessible outside a reducer body.
	}

	action evaluation_order() {
		// reduce is a sequential left fold: ((10 - 3) - 2), not (10 - (3 - 2)).
		int difference <- [10, 3, 2] reduce (@ - each);
		int seeded_difference <- [10, 3, 2] reduce (0, @ - each);
		assert difference = 5;
		assert seeded_difference = -15;
		write "Left-to-right subtraction: " + difference + "; starting from zero: " + seeded_difference;
	}

	action other_containers() {
		// Maps iterate over VALUES, not keys or pairs.
		map<string, int> quantities <- ["apples"::2, "pears"::3, "oranges"::4];
		int quantity_total <- quantities reduce (@ + each);
		assert quantity_total = 9;
		write "Total map values: " + quantity_total;

		matrix<int> values <- matrix([[1, 2], [3, 4]]);
		int matrix_total <- values reduce (@ + each);
		assert matrix_total = 10;
		write "Total matrix cells: " + matrix_total;

		// A reduction can also return a container. Use a typed seed to retain its content type.
		list<int> flattened <- [[1, 2], [3, 4]] reduce (list<int>([]), @ + each);
		assert flattened = [1, 2, 3, 4];
		write "Lists combined by reduce: " + flattened;

		// accumulate already provides flat-map; reduce lets us choose the combination ourselves.
		assert flattened = ([[1, 2], [3, 4]] accumulate each);
	}

	action population_reduction() {
		create contributor number: 3 {
			energy <- float(index + 1);
		}
		float energy_total <- contributor reduce (0.0, @ + each.energy);
		assert energy_total = 6.0;
		write "Total population energy: " + energy_total;
	}

	action nested_reductions() {
		list<list<int>> rows <- [[1, 2], [3, 4]];

		// In the inner body, @ and each belong to the inner reduction.
		// Once it returns, the outer bindings are restored.
		int nested_total <- rows reduce (0, @ + (each reduce (@ + each)));
		assert nested_total = 10;
		write "Sum of nested lists: " + nested_total;

		// Named iterators still work around a reduction; acc remains an ordinary identifier.
		list<int> row_totals <- rows collect (acc: acc reduce (@ + each));
		assert row_totals = [3, 7];
		write "Individual row sums: " + row_totals;
	}
}

species contributor {
	float energy;
}

experiment "Run reduce recipes" type: gui {}
