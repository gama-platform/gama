/***
* Name: Run Thread
* Author: Patrick Taillandier
* Description: Illustrates the ability of GAMA to run model actions concurrently in separate threads. The 'thread'
*   skill enables any species (including 'global') to execute a 'thread_action' in a dedicated system thread.
*   Three built-in actions are provided: 'start_thread' launches the thread, 'end_thread' terminates it, and the
*   overridable 'thread_action' contains the code to run concurrently. Multiple agents can each run their own
*   thread simultaneously. When an agent is killed, its thread is automatically stopped. This model attaches the
*   skill to both the global species (with a fixed rate) and to individual agents (with a fixed delay).
* Tags: system, thread, skill, concurrent, parallel, async, action
***/


model testThread 

global skills: [thread]{
	bool create_agents <- false; 
	init {			
		//create and start a new thread - the thread_action will be activated continuously with a delay of 2#s between each execution
		
		if (create_agents) {
			create thread_agent number: 2;
		}
		do run_thread(interval: 2#s);
	}
	  
  
	
	//the action run in the thread 
	action thread_action() {
		write "current time: " + #now; 
	}	 
}

species thread_agent skills: [thread] {
	//create and start a new thread - the thread_action will be activated continuously at a fixed rate every 1#s by the 2 agents
	
	init {
		do run_thread(every: 1#s);
	}
	
	//the action run in the thread
	action thread_action() {
		write " > " + self + "current time: " + #now; 
	}	
}
  
experiment "Run global thread" autorun: true;

experiment "Run several threads" autorun: true {
	action _init_() {
		create simulation(create_agents: true);
	}
} 