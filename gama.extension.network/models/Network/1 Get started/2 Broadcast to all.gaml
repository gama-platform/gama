/**
* Name: broad cast to all agents
* Author: Nicolas Marilleau
* Description: It is a simple model showing few agents that broadcast a message to others.
* Tags: Network, MQTT
*/

/**
 * Demo connection based on a default free remote server (broker.mqtt.cool, with port 1883). 
 * Using the default MQQT server requires an available internet connection. Depending on your web access, it could be slow down the simulation. 
 * It is a free and unsecure server.
 * Using YOUR server is thus adviced. You can download free solution such as ActiveMQ (http://activemq.apache.org) 
 */
model broad_cast_to_all_agent

global {
	int nb_agents <- 10;

	init {
		write "The default connect accesses a default free remote server (broker.mqtt.cool, with port 1883). An internet connection is thus required." color: #red;
		write "The default broket is for test only, limit the number of connections otherwise connection will be refused." color: #red;
		write "To connect to your local/remote server, change the parameters of the connect statement" color: #blue;		
		
	//create Ping agent
		create People number: nb_agents {
		// The name attribute of each agent being unique, we use it as an id in the connection to the server	
			do connect with_name: name;
			//default ActiveMQ MQTT login is "admin", the password is "admin" and the port is 1883
			// do connect to:"localhost" with_name:name login:"admin" password:"admin" port: 1883;
		}

		ask one_of(People) {
			is_sender <- true;
		}
	}
}

species People skills: [network] {
	bool is_sender <- false;

	reflex fetch when: has_more_message() {
	//read a message
		message mess <- fetch_message();
		//display the message 
		write name + " fecth a message from: " + mess.sender + " -- " + mess.contents;
	}

	reflex send when: is_sender {
	//send a message to all agents (even him). "ALL" is the built in group 
	//for which each agent participates. 
		do send to: "ALL" contents: "This message is sent by " + name + " to All";
	}

}

experiment start type: gui {
	float minimum_cycle_duration <- 1 #s;
	
	output {
	}
}