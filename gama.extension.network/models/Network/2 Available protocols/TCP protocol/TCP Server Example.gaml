/**
* Name: TCP Server Example
* Author: Huynh Quang Nghi
* Description: A minimal TCP server model for testing with external tools. GAMA opens a TCP listening
*   socket and waits for connections. An external Python client script ('client.py') is provided to test
*   the connection. Each received message is echoed back. Use this model to verify GAMA TCP server setup
*   before integrating with a full client model.
* Tags: network, TCP, socket, server, protocol, communication, external
*/
model Socket_TCP_HelloWorld_Server

global {

	init {
		write "HOW TO USE" color: #red;
		write "1. Launch this model";
		write "2. Execute the client scripts/client.py";
		write "3. Step on this server model";
		
		create Server {
			do connect protocol: "tcp_server" port: 3001 with_name: name raw: true;
		}

	}

}

species Server skills: [network] parallel: true {
	string dest;
	rgb color;

	reflex receive when: has_more_message() {
		loop while: has_more_message() {
			message mm <- fetch_message();
			write name + " received : " + mm.contents color: color;
			do send to: mm.sender contents: ("I am Server Leader " + name + ", I give order to server_group");
		}

	}

}

experiment "TCP Server Test" type: gui {
	float minimum_cycle_duration <- 0.25;
	output {
	}

}
