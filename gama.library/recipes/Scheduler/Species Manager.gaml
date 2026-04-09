/**
* Name: Species Manager Scheduler
* Author: Damien Philippon
* Description: Shows how a manager species can control the execution order of other species with randomization.
*   The manager's 'schedules' facet contains a shuffled list of both species A and B agents, so all agents are
*   randomly interleaved at each step (rather than one species always going before the other). As with the
*   'Not Executed' variant, the manager itself is not scheduled anywhere, so its own reflex is never fired.
*   This pattern provides fine-grained, randomly-ordered cross-species scheduling.
* Tags: scheduling, execution, manager, schedules, shuffle, random, order
*/

model managerscheduler


global
{
	init
	{
		create manager; // if this line is ommitted, only A and B agents are scheduled
		create A number:10;
		create B number:10;
		
		write "This model shows how a species can manage other species execution process. In the facet schedules of the\n"
			+ "manager species, a shuffled list of the agents of the species B and A has been given. The agents of species B and A will be randomly executed once per step.\n"
			+ "An important point is that the manager is not executed, since it is not scheduled anywhere, but it is still scheduling other species agents.\n"
			+ "This is shown with the reflex write_new_step belonging to the manager species that is not executed, not writing its text to the console.";
	}
}

species manager schedules:manager+shuffle(B+A)
{
	reflex write_new_step
	{
		write "-----NEW STEP BY MANAGER-----";
	}
}

species A schedules:[]
{
	reflex present_itself
	{
		write "I'm A " + int(self);
	}
}
species B schedules:[]
{
	reflex present_itself
	{
		write "I'm B "+ int(self);
	}
	
}
experiment "Schedule" type:gui
{
	
}