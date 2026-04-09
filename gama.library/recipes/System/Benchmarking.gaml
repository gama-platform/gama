/***
* Name: Benchmarking
* Author: Benoit Gaudou
* Description: Illustrates GAMA's built-in benchmarking and profiling capabilities. When a model becomes large
*   and slow, it is necessary to identify which parts of the code take the most time. GAML provides two mechanisms:
*   (1) the 'benchmark' statement, which measures the execution time of a specific block and reports it in the console;
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
		// benchmark statement will compute the time spent to execute the block of code its embeds.
		// To get more reliable results, the inner statements can be executed several times (specified by the repeat: facet).
		benchmark "Benchmark of closest_to operator" repeat: 100 {
			ask people {
				do get_closest_people();
			}
		}
	}
}

species people {
	action get_closest_people (){
		people neigh <- people closest_to self;
	} 
}

// When the facet benchmark: is used in an experiment, it will produces step after step a csv file
// that summarizes the time spent in block and the number of executions of each statement.
experiment Benchmarking type: gui benchmark: true { }
