/**
* Name: FIPA Propose (1) - Proposal Accepted
* Author: Gama Development Team
* Description: Demonstrates the FIPA Propose interaction protocol — accepted variant. The Initiator sends a
*   'propose' message ('Go swimming?'). The Participant replies with 'accept_proposal', ending the conversation.
*   See http://www.fipa.org/specs/fipa00036/SC00036H.html for the full protocol specification.
*   Pair with FIPA Propose (2) which shows the rejected variant.
* Tags: fipa, propose, accept_proposal, protocol, multi_agent, interaction
*/
model fipa_propose_1

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
	reflex send_propose_message when: (time = 1) {
		write name + ' sends a propose message';
		do start_conversation(to: [p], protocol: 'fipa-propose', performative: 'propose', contents: ['Go swimming?']) ;
	}
	
	reflex read_accept_proposals when: !(empty(accept_proposals)) {
		write name + ' receives accept_proposal messages';
		loop i over: accept_proposals {
			write 'accept_proposal message with content: ' + string(i.contents);
		}
	}
}

species Participant skills: [fipa] {
	reflex accept_proposal when: !(empty(proposes)) {
		message proposalFromInitiator <- proposes at 0;
		
		do accept_proposal(message: proposalFromInitiator, contents: ['OK! It \'s hot today!']) ;
	}
}

experiment test_propose_interaction_protocol type: gui {}