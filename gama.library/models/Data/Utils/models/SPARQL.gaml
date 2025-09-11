
model sparql

global {
	
	init {
		write sparql_query("select * where {?Subject ?Predicate ?Object} LIMIT 1","http://dbpedia.org/sparql",3000);
	}
	
}

experiment test_sparql;