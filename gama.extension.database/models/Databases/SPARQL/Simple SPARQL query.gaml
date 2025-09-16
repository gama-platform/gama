/**
* Name: SimpleSPARQLquery
* This model queries dbpedia to ask for a list of french philosophers then displays it in a "pretty" array
* Author: baptiste lesquoy
* Tags: 
*/


model SimpleSPARQLquery

global {
	
	string centeredString(string base, int aimedSize){
		int lBase <- length(base);
		if lBase >= aimedSize {
			return base;
		}
		int left <- floor((aimedSize-lBase)/2);
		int right <- int(ceil((aimedSize-lBase)/2));
		return concatenate(list_with(left, " ")) + base + concatenate(list_with(right, " "));
	}
	
	init {
		
		write "Asking dbpedia for a list of all French philosophers";
		
		// Those variables will be used as variable names in the query and will be the keys of the returned map
		// do not insert illegal characters into them, else the query will fail
		string headerNameCol <- "name";
		string headerBirthdateCol <- "birthDate";
		
		// Do not insert useless (double) spaces or new lines as it may result in invalid requests
		map<string,list<string>> result <- sparql_query(
			"PREFIX dbc:<http://dbpedia.org/resource/Category:>\n"
		+ 	"PREFIX dct:<http://purl.org/dc/terms/>\n"
		+	"PREFIX dbo:<http://dbpedia.org/ontology/>\n"
		+	"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
		+	"SELECT (replace(replace(STRAFTER(STR(?X), \"http://dbpedia.org/resource/\"), \"_\", \" \"), \",.*\", \"\") AS ?"+ headerNameCol +")\n"
		+	"(STR(?birthdate) as ?" + headerBirthdateCol + ")\n"
		+	"WHERE {?X dct:subject dbc:French_philosophers . ?X dbo:birthDate ?birthdate . FILTER(datatype(?birthdate) = xsd:date)}\n"
		+	"ORDER BY ASC(?birthdate)", 
		"https://dbpedia.org/sparql");

		if empty(result) {
			write "An error occured: " + #current_error color:#red;
			return;
		}

		write "Results fetched, printing them";
		int colNameWidth <- result[headerNameCol] max_of length(each) + 2; // +2 to add a space on each side
		int colBirthdateWidth <- result[headerBirthdateCol] max_of length(each) + 2;
		int totalWidth <- colNameWidth + colBirthdateWidth + 3; // 3 more characters for borders
		
		// print the header
		write concatenate(list_with(totalWidth, "-"));
		write "|" + centeredString(headerNameCol, colNameWidth) +"|" + centeredString(headerBirthdateCol, colBirthdateWidth) +"|";
		write "|" + concatenate(list_with(totalWidth-2, "-")) + "|";		
		
		// print the content
		loop i from:0 to:length(result[headerNameCol])-1{
			
			string fullName <- result[headerNameCol][i];
			string birthdate <- result[headerBirthdateCol][i];
			
			write "|" + centeredString(fullName, colNameWidth) +"|" + centeredString(birthdate, colBirthdateWidth) +"|";
		}
		write concatenate(list_with(totalWidth, "-"));
		
		
	}
	
}

experiment test;