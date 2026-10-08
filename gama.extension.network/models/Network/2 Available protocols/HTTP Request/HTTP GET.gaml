/**
* Name: HTTP GET
* Author: Benoit Gaudou
* Description: Demonstrates how to make HTTP GET requests from a GAMA model. Connects to a remote HTTPS
*   server (port 443) and sends a GET request, then parses the response. Shows the 'network' skill HTTP
*   protocol mode: setting the server, port, and SSL parameters, sending the request, and reading the
*   response body as a string. Useful for fetching data from web APIs or REST services during simulation.
* Tags: network, HTTP, GET, REST, web_service, protocol, communication, API
*/


model HTTPGET

global {
	int port <- 443;       // for HTPP : 80 http, for HTTPS : 443 
	string url <- "https://openlibrary.org";	
	
	init {
		create NetworkingAgent number: 1 {
		   do connect(to: url, protocol: "http", port: port, raw: true);
		}		
	} 

}

species NetworkingAgent skills:[network] {
	
	reflex send when:  cycle = 0 {
		write "sending message ";		
		do send(to: "/search/authors.json?q=j%20k%20rowling", contents: ["GET"]);
	//	do send(to: "/search/authors.json?q=j%20k%20rowling", contents: ["GET", ["Content-Type"::"application/json"]]);		
	}

	reflex get_message {
		loop while:has_more_message()
		{
			//read a message
			message mess <- fetch_message();
			//display the message 
			write name + " fecth this message: " + mess.contents;	
			write sample(map(mess.contents)["CODE"]);
			write sample(map(mess.contents)["BODY"]);
			write sample(map(mess.contents)["HEADERS"]);			
		}
		
	}

	
	aspect default {
		draw circle(1) color: #red border: #black; 
	}
}

experiment Server_testdd type: gui {
	output {
		display d {
			species NetworkingAgent;	
		}
	}
}
