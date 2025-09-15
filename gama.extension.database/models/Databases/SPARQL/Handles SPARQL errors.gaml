/**
* Name: HandlesSPARQLerrors
* This model showcases how to handle errors when doing SPARQL queries
* Author: baptiste lesquoy
* Tags: 
*/


model HandlesSPARQLerrors


global {
	
	
	init {
		
		write "The query about to be executed will fail";
		
		list<list<string>> result <- sparql_query("NOT A QUERY", "Not an endpoint");
		
		if empty(result) and !empty(#current_error) {
			write #current_error color:#red;
			
		}			
	}
	
}

experiment test;