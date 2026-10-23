/**
* Name: Random Number Generators
* Description: Compares legacy and bounded integer draws, then replays a uniform random stream using rng_usage.
*   The boolean operand opts into integer sampling without changing existing models' seeded sequences.
*   rng_usage counts raw generator draws, not operator calls: float-based draws consume two, bounded integer
*   draws usually one (rejection sampling can consume more). Assigning rng_usage resets the generator using
*   its current seed and advances it to the requested position. Negative usage resets to zero.
*   This is not a full state snapshot: cached Gaussian values are not restored. The threaded generator is
*   not seedable, always reports zero usage, and cannot replay a stream.
* Tags: random, rng, seed, reproducibility, simulation
*/

model RandomNumberGenerators

global {
	init {
		rng <- "mersenne"; // "java" and "parallel" also support replay.
		seed <- 123.0;
		int legacy_draw <- rnd(31);
		write "Legacy draw: " + legacy_draw + ", raw draws: " + rng_usage;

		seed <- 123.0;
		int integer_draw <- rnd(31, true);
		write "Bounded integer draw: " + integer_draw + ", raw draws: " + rng_usage;

		int saved_usage <- rng_usage;
		list expected <- [rnd(-10, 10, true), rnd(1.0), one_of(["a", "b", "c"])];
		write "Next draws: " + expected;

		rng_usage <- saved_usage;
		list replay <- [rnd(-10, 10, true), rnd(1.0), one_of(["a", "b", "c"])];
		write "Replayed draws: " + replay + ", identical: " + (replay = expected);

		rng_usage <- 0;
		write "Back to the first bounded integer draw: " + rnd(31, true);
	}
}

experiment RandomNumberGenerators type: gui {
}
