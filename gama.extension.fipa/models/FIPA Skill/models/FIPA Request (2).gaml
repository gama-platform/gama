/**
* Name: FIPA Request (2) - Agree then Failure
* Author: Gama Development Team
* Description: Demonstrates the FIPA Request protocol — agree-then-failure variant. The Initiator sends a
*   'request' ('go sleeping'). The Participant replies with 'agree' (accepts), then 'failure' (cannot execute,
*   the bed is broken). See http://www.fipa.org/specs/fipa00026/index.html. Part 2 of 4 Request variants.
* Tags: fipa, request, agree, failure, protocol, multi_agent, interaction
*/
model fipa_request_2

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
		do start_conversation to: [p] protocol: 'fipa-request' performative: 'request' contents: ['go sleeping'] ;
	}
	
	reflex read_agree_message when: !(empty(agrees)) {
		write 'read agree messages';
		loop a over: agrees {
			write 'agree message with content: ' + string(a.contents);
		}
	}
	
	reflex read_failure_message when: !(empty(failures)) {
		write 'read failure messages';
		loop f over: failures {
			write 'failure message with content: ' + (string(f.contents));
		}
	}
}

species Participant skills: [fipa] {
	reflex print_debug_infor {
		write name + ' with conversations: ' + string(conversations) + '; messages: ' + string(mailbox);
	}

	reflex reply_messages when: (!empty(requests)) {
		message requestFromInitiator <- (requests at 0);
		write 'agree message';
		do agree message: requestFromInitiator contents: ['I will'];
		
		write 'inform the initiator of the failure';
		do failure message: requestFromInitiator contents: ['The bed is broken'];
	}
}



experiment test_request_interaction_protocol type: gui {}