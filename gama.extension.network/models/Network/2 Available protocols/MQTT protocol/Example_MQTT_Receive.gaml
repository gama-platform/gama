/**
* Name: MQTT Receive Example
* Author: Nicolas Marilleau, Arnaud Grignard
* Description: A minimal MQTT receiver model. The global agent subscribes to a topic and listens for
*   incoming messages using the 'fetch' action. Pair with 'MQTT Send Example' or any external MQTT
*   publisher. This is one half of the simplest possible MQTT communication pattern in GAMA.
* Tags: network, MQTT, receive, fetch, subscribe, messaging, protocol, communication
*/

model MQTT_HelloWorld_Receive



global {	
	init {
		write "A MQTT server should run." color: #red;
		write "Another instance of GAMA should run the model Example_MQTT_Send.gaml, so that an agent can send messages.";
		
		// The sender will send itself during the exchange (NetworkingAgent, id = 0).
		// To receive and recreate an agent, the same agent should not exist in the receiving simulation
		// So we kill the first created (id = 0) and recreate another one (id = 1). 
		create NetworkingAgent number:1 { do die(); }
		
		create NetworkingAgent number:1 {
			name <- "receiver";
			/**
			 * Demo connection based on a default free remote server (broker.mqtt.cool, with port 1883). 
			 * Using the default MQQT server requires an available internet connection. Depending on your web access, it could be slow down the simulation. 
			 * It is a free and unsecure server.
			 * Using YOUR server is thus adviced. You can download free solution such as ActiveMQ (http://activemq.apache.org) 
			 */
			do connect(with_name:"receiver");
			
			// default ActiveMQ MQTT login is "admin", the password is "admin" and the port is 1883
			// do connect(to:"localhost", port:1883, with_name:"receiver");
			// do connect(to:"localhost", port:1883, with_name:"receiver", login:"admin", password:"admin", port: 1883);
		}
	}
}

species NetworkingAgent skills:[network]{
	string dest;

	reflex fetch when:has_more_message() {	
		loop while: has_more_message() {
			message mess <- fetch_message();
			write "fetch this message: " + mess;	
			write "Number of NetworkingAgent: " + length(NetworkingAgent);			
		}
	}
}

experiment Network_receiver type: gui {
	output {
	}
}
