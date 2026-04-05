/**
* Name: FIPA Query (2) - Query Refused
* Author: Gama Development Team
* Description: Demonstrates the FIPA Query protocol — refused variant. The Initiator sends a 'query'
*   ('your name?'). The Participant replies with 'refuse' (its name is a secret), ending the conversation.
*   See http://www.fipa.org/specs/fipa00027/SC00027H.html. Pair with FIPA Query (1) for the accepted variant.
* Tags: fipa, query, refuse, protocol, multi_agent, interaction
*/
model fipa_query_2

global {
	Participant p;
	
	init {
		create Initiator;
		create Participant returns: ps;
		
		 p <- ps at 0;
		
		write 'Step the simulation to observe the outcome in the console';
	}
}

species Initiator skills: [fipa] {
	reflex send_query_message when: (time = 1) {
		write name + ' sends a query message';
		do start_conversation to: [p] protocol: 'fipa-query' performative: 'query' contents: ['your name?'];
	}
	
	reflex read_refuse_messages when: !(empty(refuses)) {
		write name + ' receives refuse messages';
		loop i over: refuses {
			write 'refuse message with content: ' + string(i.contents);
		}
	}
}

species Participant skills: [fipa] {
	reflex reply_query_messages when: !(empty(queries)) {
		message queryFromInitiator  <- queries at 0;
		
		write name + ' reads a query message with content : ' + string(queryFromInitiator.contents);
		
		do refuse message: queryFromInitiator contents: ['No! That is a secret!'] ;		
	}
}

experiment test_query_interaction_protocol type: gui {}