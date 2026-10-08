/**
* Name: FIPA Request (1) - Request Refused
* Author: Gama Development Team
* Description: Demonstrates the FIPA Request protocol — refused variant. The Initiator sends a 'request'
*   ('go sleeping'). The Participant replies with 'refuse', ending the conversation.
*   See http://www.fipa.org/specs/fipa00026/index.html. Part 1 of 4 Request variants.
* Tags: fipa, request, refuse, protocol, multi_agent, interaction
*/
model fipa_request_1

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
	reflex print_debug_infor {
		write name + ' with conversations: ' + string(conversations) + '; messages: ' + string(mailbox);
	}
	
	reflex send_request when: (time = 1) {
		write 'send message';
		do start_conversation(to: [p], protocol: 'fipa-request', performative: 'request', contents: ['go sleeping']) ;
	}
	
	reflex read_refuse_message when: !(empty(refuses)) {
		write 'read refuse messages';
		loop r over: refuses {
			write 'refuse message with content: ' + string(r.contents);
		}
	}
}

species Participant skills: [fipa] {
	reflex print_debug_infor {
		write name + ' with conversations: ' + string(conversations) + '; messages: ' + string(mailbox);
	}

	reflex reply_messages when: (!empty(requests)) {
		write name + ' sends a refuse message';
		do refuse(message: (requests at 0), contents: ['I don\'t want']) ;
	}
}



experiment test_request_interaction_protocol type: gui {}