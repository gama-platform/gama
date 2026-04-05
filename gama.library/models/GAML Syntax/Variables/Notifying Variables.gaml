/**
* Name: Notifying Variables
* Author: Alexis Drogoul
* Description: Shows how certain variables, which are directly manipulated by skills (such as 'location' by the
*   'moving' skill), can notify a model when they have been changed. This notification mechanism allows modelers
*   to react immediately to an attribute change — for example logging the new value or triggering a side effect —
*   without polling the variable every step. The 'on_change' facet of a variable declaration specifies a block
*   of code to execute whenever the variable's value is updated. This model demonstrates the feature with the
*   'location' attribute and custom notifying variables.
* Tags: variables, attributes, notification, on_change, moving, skill, callback
*/
model NotifyingVariables

global {

	init {
		create a;
		create b;
		create c;
	}

}

/*
 * Agents of species a are provided with the moving skill, which, among other things, manipulates the location of the agent.
 * Thanks to the 'on_change:' facet on the variable, the agents are being notified of these changes.
 */
species a skills: [moving] {
	point location on_change: {
		write "Location of " + self + " changed to " + location;
		ask b {
			location <- myself.location;
		}
	};

	reflex wandering {
		do wander;
	}

}

/*
 * Agents of species b see their location modified by agents of species a. They can also be notified of it. 
 */
species b {
	point location on_change: {
		write "Location of " + self + " changed to " + location;
	};
}

/*
 * Agents of species c behave according to a finite state machine. One variable, 'state', indicates the current state of the agent. 
 * By adding the 'on_change:' facet to state, agents can be notified (or notify others) when they change state.
 */

species c control: fsm {
	
	string state on_change: {
		write "Transition to " + state;
	};
	
	state s1 initial: true {
		write name + " in S1";
		transition to: s2 when: flip(0.5);
	}
	
	state s2 {
		write name + " in S2";
		transition to: s1 when: flip(0.5);
	}
	
}

experiment Notification;