/**
* Name: No Scheduler
* Author: Damien Philippon
* Description: Demonstrates how to completely exclude a species from the simulation schedule in GAMA. By providing
*   an empty list '[]' as the value of the 'schedules' facet, the species is never executed during the simulation.
*   As a result, none of the agents' reflexes are ever fired, and nothing is written to the console despite the
*   agents existing in memory. This is useful when a species should be passive (e.g., only used as data containers
*   or queried by other agents), without consuming any scheduling resources.
* Tags: scheduling, execution, schedules, passive, no_schedule
*/

model schedullingagents

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
		write "With an empty list given inside the schedules facet of the species, it will not be executed \n"
			+ "so, nothing will be written inside the console";
	}
}
species no_scheduler schedules:[]
{
	int nb_generated;
	reflex sayHello
	{
		write "hello, i'm "+nb_generated;
	}
}

experiment "No Scheduling" type:gui
{
	
}


