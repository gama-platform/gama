/**
* Name: SPARQLquerywithfiles
* Reads a SPARQL query from a file, executes it then store the result into another file 
* Author: baptiste lesquoy
* Tags: 
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