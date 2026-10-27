/**
* Name: Handles SPARQL Errors
* Author: Baptiste Lesquoy
* Description: Shows how to handle errors that can occur during SPARQL queries in GAMA, such as network
*   timeouts, malformed queries, or endpoint unavailability. Demonstrates the nil fallback of
*   sparql_query outside try/catch, and catching the same failure explicitly. This is the reference
*   for writing robust SPARQL-enabled models that degrade gracefully when the endpoint is unreachable.
* Tags: database, SPARQL, error_handling, linked_data, semantic_web, exception
*/


model HandlesSPARQLerrors


global {
	
	
	init {
		
		write "The query about to be executed is supposed to fail";
		
		dataframe result <- sparql_query("NOT A QUERY", "Not an endpoint");
		write result;
		if (result = nil or empty(result)) and !empty(#current_error) {
			write #current_error color:#red;	
		}
		assert result = nil;
		bool caught <- false;
		try {
			result <- sparql_query("NOT A QUERY", "Not an endpoint");
			write "This statement is skipped on failure.";
		} catch {
			caught <- true;
			write "Caught query failure: " + #current_error;
		}
		assert caught;
	}
	
}

experiment test;