/**
* Name: UDP Emitter Example
* Author: Benoit Gaudou, Nicolas Marilleau
* Description: A UDP sender model that emits an agent's XY coordinates as a UDP datagram to a listening
*   server. An example Processing sketch (UDPServer) is provided in the model library to act as the
*   receiver. Demonstrates GAMA's UDP socket support — useful for streaming real-time agent data to
*   external visualization or control applications.
* Tags: network, UDP, socket, emitter, coordinates, protocol, communication, Processing
*/

model SocketUDP_Emitter

global {	

	int port <- 9876;
	string url <- "localhost";

	init {
		write "Before running this model, run an UDP server (on the same port)." color: #red;
		write "1 server example is provided using Processing 3.";
		write "Processing 3 can be downloaded from: https://processing.org/";
		write "First run one server (e.g. UDPServer.pde), then launch and run the GAMA simulation" color: #red;
		
		
		create NetworkingAgent number:1 {
		   do connect(to: url, protocol: "udp_emitter", port: port) ;
		}		
	} 
}

species NetworkingAgent skills:[network] {

	reflex move {
		location <- any_location_in(world);
	}

	reflex send {
		write "sending message ";
		do send(contents: "" + location.x + ";" + location.y);
	}
	
	aspect default {
		draw circle(1) color: #red border: #blue;
	}
}

experiment Client_testdd type: gui {

	output {
		display "My display" { 
			species NetworkingAgent;
		}

	}
}