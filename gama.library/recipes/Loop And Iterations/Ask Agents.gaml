/***
* Name: Ask Agents
* Author: Benoit Gaudou
* Description: Illustrates the use of the 'ask' statement in GAML to iterate over and interact with a population of agents.
*   The 'ask' statement is a concise alternative to a 'loop over' when the intention is to execute actions within the
*   context of each target agent. The model also introduces the 'self' and 'myself' meta-variables: 'self' refers to
*   the agent currently executing, while 'myself' refers to the agent that initiated an 'ask' block — allowing the
*   called agent to refer back to the calling agent. A comparison between 'loop' and 'ask' is provided.
* Tags: ask, loop, self, myself, population, agent, context
***/

model Asktoloopoveragents

global {
	init {
		create dummy_species number: 5;
		
		// We want that each created agent to introduce itself.
		// We can thus loop over the population of agents using the loop statement.
		// We are in the  context of the world agent, and thus need to access attribute of each agent using: agt.name and int(agt) 
		write "Introduction in a loop statement.";
		loop agt over: dummy_species {
			// we are in the context of the world agent, we CANNOT use dummy_species actions directly.			
			write "This is " + agt.name + " and its number is " + int(agt);			
		}
		
		// Ask statement allows to iterate over a population of agents, and to execute a set of statements in the contexte OF EACH AGENT.
		write "Introduction in an ask statement.";
		ask dummy_species {
			// we are in the context of a dummy_species agent, we can thus use its actions.
			do introduce_myself();
		} 
		
		// Ask can be used with the whole species, but also a list of agents (or even a single agent).
		// For example, 2 among the dummy_species agents will kill another dummy_species agent.
		// Before dying the agent says the name of its killer.
		ask 2 among dummy_species {
			ask one_of(dummy_species - self) {
				// To display the name of the agent that kills it, the current agent (self) should use myself to refer to its killer.
				write "I, " + self.name + ", have been killed by " + myself.name ;
				do die();
			}
		}
	}
}

species dummy_species {
	action introduce_myself() {
		write "Hello! I am " + name + " and my number is " + int(self);
	}
}

experiment name type: gui {}