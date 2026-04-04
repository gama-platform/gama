/**
* Name: Handles SPARQL Errors
* Author: Baptiste Lesquoy
* Description: Shows how to handle errors that can occur during SPARQL queries in GAMA, such as network
*   timeouts, malformed queries, or endpoint unavailability. Demonstrates using try/catch blocks around
*   the 'sparql' action and inspecting the error type to display a useful message. This is the reference
*   for writing robust SPARQL-enabled models that degrade gracefully when the endpoint is unreachable.
* Tags: database, SPARQL, error_handling, linked_data, semantic_web, exception
*/


model HandlesSPARQLerrors


global {
	
	
	init {
		
		write "The query about to be executed is supposed to fail";
		
		map<string,list<string>> result <- sparql_query("NOT A QUERY", "Not an endpoint");
		write result;
		if empty(result) and !empty(#current_error) {
			write #current_error color:#red;	
		}
	}
	
}

experiment test;