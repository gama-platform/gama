/***
* Name: Scan
* Description: Keeps every intermediate accumulator with the scan operator.
*   In the body, @ is the accumulated value and each is the current element.
*   Examples cover running totals, seeds, empty inputs, maps, list-valued accumulators, and nesting.
*   Run the experiment to see the results in the console. Assertions check the expected values.
* Tags: scan, reduce, iterator, container, list, map, functional, aggregation
***/

model Scan

global {
	init {
		list<int> numbers <- [1, 2, 3, 4];

		// Without a seed, the first element is the first result. The body starts at the second.
		list<int> running_totals <- numbers scan (@ + each);
		list<int> running_products <- numbers scan (@ * each);
		assert running_totals = [1, 3, 6, 10];
		assert running_products = [1, 2, 6, 24];
		write "Running sums: " + running_totals + "; running products: " + running_products;

		// A seeded scan includes the seed, then processes every element: n inputs give n + 1 results.
		list<int> seeded_totals <- numbers scan (0, @ + each);
		assert seeded_totals = [0, 1, 3, 6, 10];
		assert scan(numbers, 0, @ + each) = seeded_totals;
		assert scan(numbers, @ + each) = running_totals;
		assert last(seeded_totals) = (numbers reduce (0, @ + each));
		write "Running totals including the seed: " + seeded_totals;

		// Evaluation is left to right, including for non-associative operations.
		assert ([10, 3, 2] scan (@ - each)) = [10, 7, 5];

		// The seed determines the accumulator type, which may differ from the element type.
		list<int> lengths <- ["a", "bb", "ccc"] scan (0, @ + length(each));
		list<float> real_totals <- numbers scan (0.0, @ + each);
		assert lengths = [0, 1, 3, 6];
		assert real_totals = [0.0, 1.0, 3.0, 6.0, 10.0];
		write "Cumulative string lengths: " + lengths;

		// Empty unseeded scans return []; seeded ones return [seed]. Neither evaluates the body.
		list<int> empty_numbers <- [];
		assert (empty_numbers scan (@ + each)) = [];
		assert ([] scan (0, @ + each)) = [0];
		assert ([7] scan (@ + each)) = [7];

		// Maps iterate over values, in their iteration order.
		map<string, int> quantities <- ["apples"::2, "pears"::3, "oranges"::4];
		assert (quantities scan (0, @ + each)) = [0, 2, 5, 9];

		// Accumulators can be lists. Intermediate results are not flattened.
		list<list<int>> prefixes <- [1, 2, 3] scan (list<int>([]), @ + each);
		assert prefixes = [[], [1], [1, 2], [1, 2, 3]];
		write "Successive prefixes: " + prefixes;

		// Nested scans and reductions bind @ and each to the innermost body.
		// Returning from the inner body restores the outer bindings.
		list<list<int>> rows <- [[1, 2], [3, 4]];
		list<int> row_totals <- rows scan (0, @ + (each reduce (@ + each)));
		assert row_totals = [0, 3, 10];
		write "Cumulative row totals: " + row_totals;
		write "All scan examples completed successfully.";
	}
}

experiment "Run scan recipes" type: gui {}
