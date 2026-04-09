/**
* Name: Buffering Performances
* Author: Baptiste Lesquoy
* Description: Benchmarks the performance difference between unbuffered and buffered file writing in GAML.
*   When a model writes output data frequently (e.g., every agent every step), unbuffered writing causes a disk
*   I/O operation for each call, which is very slow at scale. Buffered writing ('buffering: per_cycle') collects
*   all writes within a cycle and flushes them to disk in a single operation at the end of the cycle, dramatically
*   reducing I/O overhead. This model uses timing measurements to quantify the speedup achieved by buffering,
*   helping modelers make informed decisions about output strategy.
* Tags: buffering, performance, file_writing, benchmark, save, I/O, optimization
*/


// This model presents the difference in terms of performance between writing many times directly in a file 
// or using a buffering strategy to have all the writing requests grouped into one big at the end of the cycle
// this benchmarking is not using the "benchmark" statement as we also want to include the time it takes for the 
// engine to actually write the files at the end of the cycle.
model BufferingPerformances


global{
	
	int state <- 0;
	int nb_rep <- 10000;
	float start_time;
	
	reflex write_duration_buffered when:state=3{
		write "Duration for buffered writing: " + (gama.machine_time - start_time) + "ms";
		do pause();
	}

	reflex write_buffered when:state = 2 {
		start_time <- gama.machine_time;
		loop times:nb_rep{
			save "some text\n" to:"someotherfile.txt" rewrite:false buffering:"per_cycle" format:"txt";
		}
		state <- 3;
	}
	
	reflex write_duration_direct when:state = 1 {
		write "Duration for direct writing: " + (gama.machine_time - start_time) + "ms";
		state <- 2;
	}
	
	
	reflex write_directly when:state=0{
		start_time <- gama.machine_time;
		loop times:nb_rep{
			save "some text\n" to:"somefile.txt" rewrite:false buffering:"no_buffering" format:"txt";
		}
		state <- 1;
	}
		
}

experiment compare;