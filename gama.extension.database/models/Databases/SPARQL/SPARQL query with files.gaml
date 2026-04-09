/**
* Name: SPARQL Query with Files
* Author: Baptiste Lesquoy
* Description: Demonstrates file-based SPARQL query workflow in GAMA: reads a SPARQL SELECT query from
*   an external .sparql file, sends it to a remote endpoint, then serializes the result set to another file
*   in CSV format. This is the recommended approach for complex queries that are too long to embed as strings
*   and for archiving query results for later analysis.
* Tags: database, SPARQL, linked_data, load_file, save_file, query, semantic_web
*/


model SPARQLquerywithfiles


global {

	text_file query <- text_file("../includes/SPARQL/SPARQL query philosophers and influences.txt");
	string endpoint <- "https://dbpedia.org/sparql";
	string output_file <- "../includes/SPARQL/result.csv";
	
	init {
		// we use concatenate to rebuild the string of the query
		string full_query_text <- concatenate(query.contents, "\n");
		
		// running the query
		map<string, list<string>> result <- sparql_query(full_query_text, endpoint);
		
		// we check for errors
		if empty(result){
			write #current_error color:#red;
			return;
		}
		
		// we check for results
		if empty(first(result)){
			write "No result found, check the query again" color:#red;
		}
		
		// we save the result as a csv that we manage "manually"
		loop i from:0 to:length(first(result))-1 {
			list<string> values <- [];
			loop col over:result.keys {
				values <+  result[col][i];
			}
			
			// write headers if it's the first line
			if i = 0 {
				save concatenate(result.keys, ",") + "\n" to:output_file format:"txt" rewrite:true; 
			}
			//save content
			save concatenate(values, ",") + "\n" to:output_file format:"txt" rewrite:false; 
			
		}
		write "request executed successfully, the result has been saved in a file following csv format";
	}

}

experiment query;