/**
* Name: SimplestSPARQLquery
* Queries dbpedia and prints the raw result in the console
* Author: baptiste lesquoy
* Tags: 
*/


model SimplestSPARQLquery


global {
	
	init {
		write sparql_query("select * where {?Subject ?Predicate ?Object} LIMIT 1","http://dbpedia.org/sparql",3000);
	}
	
}

experiment test_sparql;
