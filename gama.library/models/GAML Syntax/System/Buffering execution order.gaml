/**
* Name: Buffering Execution Order
* Author: Baptiste Lesquoy
* Description: Demonstrates the order of execution you can obtain by using different buffering strategies when
*   writing output (to the console or to files). GAML supports a 'buffering' facet on 'write' and 'save'
*   statements that controls when the data is actually flushed to the output. The 'per_cycle' strategy groups
*   all writes from the same cycle into a single flush at the end of the cycle, which changes the apparent order
*   of console messages compared to immediate (unbuffered) output. This model lets you compare the two strategies.
* Tags: buffering, execution_order, write, save, output, performance, cycle
*/


// This model presents the order of execution of different results you can obtain by using different buffering strategies.
model Bufferingexecutionorder

global {
	
	
	reflex at_cycle {
		write "at cycle " + cycle buffering:"per_cycle";
		save "at cycle " + cycle to:"data.csv" header:false rewrite:false buffering:"per_cycle" format:"csv";
	}
	
	reflex at_cycle2 {
		write "at cycle2 " + cycle + " should appear after 'at cycle "+ cycle+"' as it's asked in that order" buffering:"per_cycle";
		save "at cycle2 " + cycle + " should appear after 'at cycle "+ cycle+"' as it's asked in that order" to:"data.csv" header:false rewrite:false buffering:"per_cycle" format:"csv";
	}
	
	reflex no_buffering {
		write "at cycle " + cycle + " too, should appear before all the other, as it's executed right when the code is reached" buffering:"no_buffering";
		save "at cycle " + cycle + " too, should appear before all the other, as it's executed right when the code is reached" rewrite:false to:"data.csv" header:false buffering:"no_buffering" format:"csv";
	}
	
	reflex end_of_simulation {
		write "Run at cycle " + cycle + " but should only be visible once the simulation is killed." buffering:"per_simulation";
		save "Run at cycle " + cycle + " but should be appended at the end of the file" to:"data.csv" header:false rewrite:false buffering:"per_simulation" format:"csv";
	}
	
	reflex end_sim when:cycle=4{
		do die;
	}
	
}

experiment a type:batch until:cycle=10 autorun:true{

}