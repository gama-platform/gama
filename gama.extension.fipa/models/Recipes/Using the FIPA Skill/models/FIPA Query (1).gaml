/**
* Name: FIPA Query (1) - Query Accepted and Answered
* Author: Gama Development Team
* Description: Demonstrates the FIPA Query protocol — accepted variant. The Initiator sends a 'query'
*   ('your name?'). The Participant replies with 'agree' then 'inform' with its name, ending the conversation.
*   See http://www.fipa.org/specs/fipa00027/SC00027H.html. Pair with FIPA Query (2) for the refused variant.
* Tags: fipa, query, agree, inform, protocol, multi_agent, interaction
*/
model fipa_query_1

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
		write name + ' sends query message';
		do start_conversation(to: [p], protocol: 'fipa-query', performative: 'query', contents: ['your name?']) ;
	}
	
	reflex read_inform_message when: !(empty(informs)) {
		write name + ' reads inform messages';
		loop i over: informs {
			write 'inform message with content: ' + string(i.contents);
		}
	}
}

species Participant skills: [fipa] {

	reflex reply_query_messages when: !(empty(queries)) {
		message queryFromInitiator <- queries at 0;
		
		write name + ' reads a query message with content : ' + string(queryFromInitiator.contents);
		
		do agree(message: queryFromInitiator, contents: ['OK, I will answer you']) ;		
		do inform(message: queryFromInitiator, contents: [ 'My name is ' + name ]) ;
	}
}

experiment test_query_interaction_protocol type: gui {}