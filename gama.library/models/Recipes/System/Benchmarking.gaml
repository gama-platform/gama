/***
* Name: Benchmarking
* Author: Benoit Gaudou
* Description: Illustrates GAMA's built-in benchmarking and profiling capabilities. When a model becomes large
*   and slow, it is necessary to identify which parts of the code take the most time. GAML provides two mechanisms:
*   (1) the 'benchmark' statement, which measures a specific block, reports a summary, and can return each duration;
*   (2) the 'benchmark' facet of the 'experiment' statement, which instruments the entire simulation execution and
*   writes detailed timing results to a CSV file. Both approaches help modelers pinpoint performance bottlenecks
*   and compare the speed of different implementation strategies.
* Tags: benchmark, profiling, performance, experiment, timing, optimization
***/

model Benchmarking



global {
	init {
		create people number: 300;
	}
	
	reflex neighboorhood {
		// repeat: must be positive (default: 1); model state is not reset between executions.
		// returns: declares durations as list<float>, with one entry per execution in milliseconds.
		// The variable is available after the block; no predeclaration or cast is needed.
		// console: false disables only the summary, not writes inside the measured block.
		benchmark "Benchmark of closest_to operator" repeat: 100 returns: durations console: false {
			ask people {
				do get_closest_people();
			}
		}
		float first_duration <- durations[0];
		write "Average block duration: " + mean(durations) + " ms";
	}
}

species people {
	action get_closest_people (){
		people neigh <- people closest_to self;
	} 
}

// The experiment facet benchmark: true enables separate, experiment-wide profiling.
// It exports a CSV summary of statement durations and execution counts when the experiment closes.
experiment Benchmarking type: gui benchmark: true { }
