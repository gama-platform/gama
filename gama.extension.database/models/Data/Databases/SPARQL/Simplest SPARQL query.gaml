/**
* Name: Simplest SPARQL Query
* Author: Baptiste Lesquoy
* Description: The minimal SPARQL query example in GAMA. Sends a simple SELECT query to the DBpedia SPARQL
*   endpoint over HTTP and prints the raw result map to the console. Shows the basic 'sparql' action syntax
*   with an endpoint URL and a query string. This is the entry point for all SPARQL/linked-data integration
*   in GAMA — requires an internet connection.
* Tags: database, SPARQL, linked_data, DBpedia, semantic_web, query
*/


model SimplestSPARQLquery


global {
	
	init {
		write sparql_query("select * where {?Subject ?Predicate ?Object} LIMIT 1","http://dbpedia.org/sparql",3000);
	}
	
}

experiment test_sparql;
