/**
* Name: FIPA Request (3) - Agree then Inform Done
* Author: Gama Development Team
* Description: Demonstrates the FIPA Request protocol — agree-then-inform variant. The Initiator sends a
*   'request' ('go sleeping'). The Participant replies 'agree' then 'inform' (has gone to bed), ending the
*   conversation. See http://www.fipa.org/specs/fipa00026/index.html. Part 3 of 4 Request variants.
* Tags: fipa, request, agree, inform, protocol, multi_agent, interaction
*/
model fipa_request_3

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

	reflex read_agree_message when: !(empty(agrees)) {
		write 'read agree messages';
		loop a over: agrees {
			write 'agree message with content: ' + string(a.contents);
		}
	}
	
	reflex read_inform_message when: !(empty(informs)) {
		write 'read inform messages';
		loop i over: informs {
			write 'inform message with content: ' + string(i.contents);
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
		do agree(message: requestFromInitiator, contents: ['I will']) ;
		
		write 'inform the initiator';
		do inform(message: requestFromInitiator, contents: ['I\'m in bed already']) ;
	}
}



experiment test_request_interaction_protocol type: gui {}