/**
* Name: Random Scheduler
* Author: Damien Philippon
* Description: Shows how to randomize the execution order of a species' agents each simulation step using the
*   'shuffle' operator in the 'schedules' facet. Without shuffling, agents are executed in creation order
*   (a fixed deterministic sequence). With 'schedules: shuffle(species_name)', the execution order is randomized
*   at each step, producing fair asynchronous behavior. The console output shows each agent's index, and the
*   varying order across steps demonstrates the effect of the random scheduler.
* Tags: scheduling, execution, random, shuffle, schedules, asynchronous
*/

model randomscheduler

/* Insert your model definition here */

global
{
	init
	{
		int cpt <- 0;
		create no_scheduler number:10
		{
			cpt<-cpt+1;
			nb_generated<-cpt;
		}
		write "To randomize the way agents of a same species are executed,\n" 
			+ "it is possible to use the operator shuffle in the schedules facet. \n"
			+ "In this case, agents will write their number,\n"
			+ "but they are executed in a random way";
	}
	
	reflex write_new_step
	{
		write "NEW STEP";
	}
}
species no_scheduler schedules:shuffle(no_scheduler)
{
	int nb_generated;
	reflex sayHello
	{
		write "hello, i'm "+nb_generated;
	}
}

experiment "Schedule" type:gui
{
	
}
