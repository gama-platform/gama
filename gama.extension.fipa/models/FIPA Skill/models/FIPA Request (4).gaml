/**
* Name: FIPA Request (4) - Protocol Violation
* Author: Gama Development Team
* Description: Demonstrates the FIPA Request protocol — protocol violation variant. The Initiator sends a
*   'request'. The Participant replies directly with 'inform' (skipping the required 'agree'/'refuse'), which
*   violates the specification. GAMA raises a GamaRuntimeException and automatically ends the conversation.
*   Use this model to understand how GAMA enforces FIPA protocol conformance.
* Tags: fipa, request, inform, protocol_violation, exception, multi_agent, interaction
*/
model fipa_request_4

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
		write name + ' sends an inform message';
		
		
		write 'A GamaRuntimeException is raised to inform that the message\'s performative doesn\'t respect the \'request\' interaction protocol\' specification';
		do inform message: (requests at 0) contents: ['I don\'t want'] ; // Attention: note that GAMA will raise an exception because an 'inform' message is not appropriate here.
	}
}


experiment test_request_interaction_protocol type: gui {}