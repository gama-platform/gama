/**
* Name: Arduino Listener
* Author: Huynh Quang Nghi, Benoit Gaudou
* Description: A GAMA model that listens for messages sent by an Arduino board over a serial/network
*   connection. The model subscribes to a network channel and receives sensor data (e.g., temperature,
*   button states) from the Arduino sketch provided in the 'Arduino script' folder. Demonstrates how GAMA
*   can interface with physical hardware for cyber-physical or IoT simulation scenarios.
* Tags: network, Arduino, serial, IoT, hardware, sensor, communication, protocol
*/

model Arduino_Listener

global {	

	init {
		create NetworkingAgent number: 1 {
		   do connect(protocol: "arduino") ;
		}		
	} 
}

species NetworkingAgent skills:[network] {

	reflex fetch when:has_more_message() {	
		loop while:has_more_message()
		{
			message s <- fetch_message();
			write s.contents;
		}
	}
}

experiment test_Arduino type: gui {
	output {
		display d {
			species NetworkingAgent;	
		}
	}
}
