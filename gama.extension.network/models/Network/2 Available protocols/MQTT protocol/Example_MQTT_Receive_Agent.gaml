/**
* Name: MQTT Receive Agent Example
* Author: Nicolas Marilleau, Arnaud Grignard
* Description: Like 'MQTT Receive Example' but the 'network' skill is applied to the global species.
*   The global agent subscribes to a topic and listens using 'fetch'. Pair with 'MQTT Send Agent Example'
*   or any external MQTT publisher. Demonstrates the 'global skills:[network]' receiver pattern.
* Tags: network, MQTT, receive, fetch, skill, global, subscribe, messaging, protocol, communication
*/

model MQTT_Receiver

global skills:[network] {	
	init {   
		write "A MQTT server should run." color: #red;
		write "Another instance of GAMA should run the model Example_MQTT_Send_Agent.gaml, to show how agents can send messages.";
		/**
		 * Demo connection based on a default free remote server (broker.mqtt.cool, with port 1883). 
		 * Using the default MQQT server requires an available internet connection. Depending on your web access, it could be slow down the simulation. 
		 * It is a free and unsecure server.
		 * Using YOUR server is thus adviced. You can download free solution such as ActiveMQ (http://activemq.apache.org) 
		 */
		do connect(with_name:"receiver");
		
		// default ActiveMQ MQTT login is "admin", the password is "admin" and the port is 1883
		// do connect to:"localhost" with_name: "receiver" login:"admin" password:"admin" port: 1883;
	}
	reflex receiveAgent when:has_more_message(){
		// write "fetch agent on the network";
		message mess <- fetch_message();
		
		// Accessing the content of a message unserialised the received object
		// In the case of an agent:
		//  - if it does not exist (same species and id) in the receiving simulation, it is recreated.
		//  - Otherwise the existing agent is updated (update of the attribute values) 
		write name + " fecth this message: " + mess.contents;	
		
	}
}

species NetworkingAgent{
   rgb color;	
   aspect base{
   	draw shape color:color;
   }
	
}

experiment Network_receiver type: gui {
	output {
		display view type: opengl {
			species NetworkingAgent aspect:base;
		}
	}
}
