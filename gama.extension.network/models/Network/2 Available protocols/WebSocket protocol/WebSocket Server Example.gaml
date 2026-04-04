/**
* Name: WebSocket Server Example
* Author: Huynh Quang Nghi
* Description: A minimal WebSocket server for testing with external tools. GAMA opens a WebSocket listening
*   socket and waits for client connections. A Python script ('client_ws.py') is provided to test it.
*   Each received message is echoed back. Use this to verify WebSocket server setup before integrating
*   with a browser-based client or a full GAMA client model.
* Tags: network, WebSocket, server, protocol, communication, external, Python
*/
model Socket_TCP_HelloWorld_Server

global {

	init {
		write "HOW TO USE" color: #red;
		write "1. Launch this model";
		write "2. Execute the client scripts/client_ws.py";
		write "3. Step on this server model";
		
		
		create Server {
			do connect protocol: "websocket_server" port: 3001 with_name: name raw: true;
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

experiment "WebSocket Server Test" type: gui {
	float minimum_cycle_duration <- 0.25;
	output {
	}

}
