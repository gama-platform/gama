/**
* Name: FIPA No Protocol
* Author: Gama Development Team
* Description: Demonstrates the 'no-protocol' freestyle interaction mode in GAMA's FIPA skill. In this
*   mode the modeler has full control: any message performative can be sent in a conversation, and the
*   modeler is responsible for ending the conversation by sending a message with the 'end_conversation'
*   performative. This is the most flexible FIPA interaction pattern, useful when no standard FIPA
*   protocol matches the required communication structure.
* Tags: fipa, no_protocol, freestyle, end_conversation, message, performative, multi_agent
*/
model no_protocol_1

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
		write name + ' with conversations: ' + string(conversations) + '; mailbox: ' + string(mailbox);
	}

	reflex say_hello when: (time = 1) {
		do start_conversation to: [p] protocol: 'no-protocol' performative: 'inform' contents: [ ('Hello from ' + name)] ;
	}
	
	reflex read_hello_from_participant when: (time = 3) {
		loop i over: informs {
			write name + ' receives message with content: ' + string(i.contents);
			do inform message: i contents: [ ('Goodbye from ' + name)] ;
		}
	}
	
	reflex read_rebound_goodbye when: (time = 5) {
		loop i over: mailbox {
			write name + ' receives message with content: ' + string(i.contents);
		}
	}
}

species Participant skills: [fipa] {
	reflex print_debug_infor {
		write name + ' with conversations: ' + string(conversations) + '; mailbox: ' + string(mailbox);
	}

	reflex reply_hello when: (time = 2) {
		loop m over: informs {
			write name + ' receives message with content: ' + (string(m.contents));
			do inform message: m contents: [ ('Rebound hello from ' + name) ] ;
		}
	}
	
	reflex read_goodbye when: (time = 4) {
		loop i over: informs {
			write name + ' receives message with content: ' + (string(i.contents));
			do end_conversation message: i contents: [ ('Rebound goodbye from' + name) ] ;
		}
	}
}

experiment test_no_protocol type: gui {}