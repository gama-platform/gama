package gama.extension.database.sparql;


import java.util.Iterator;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.types.Types;


public class SPARQLOperators {

	


    @operator(
    		value = "sparql_query",
    		doc = @doc(value = "Queries a SPARQL endpoint with the query given as first parameter, at the endpoint given in the second, with timeout of 10s returns the results as a list of lists of strings (first row is the header)",
					special_cases = { "If the query fails, a non-blocking error is raised, the variable #current_error is filled with the error message and an empty list is returned." }
    				),
    		can_be_const = false,
    		category = {IOperatorCategory.SPARQL},
    		concept = {IConcept.DATABASE}
    		)
    public static IMap<String, IList<String>> queryEndpoint(final IScope scope, final String queryStr, final String endpoint){
    	return queryEndpoint(scope, queryStr, endpoint, 10000);
    }

    @operator(
    		value = "sparql_query",
    		doc = @doc(	value = "Queries a SPARQL endpoint with the query given as first parameter, at the endpoint given in the second, with timeout in millisecond given in the third and returns the results as a list of lists of strings."
    							+ " The first list is the header with the variable names, the following lists are the results.",
    					special_cases = { "If the query fails, a non-blocking error is raised, the variable #current_error is filled with the error message and an empty list is returned." }
						),
    		can_be_const = false,
    		category = {IOperatorCategory.SPARQL},
    		concept = {IConcept.DATABASE}
    		)
    public static IMap<String, IList<String>> queryEndpoint(final IScope scope, final String queryStr, final String endpoint, int timeout){
    	
    	final IMap<String, IList<String>> res = GamaMapFactory.create(Types.STRING, Types.LIST);
    	QueryExecution qexec = null;
    	
    	try {
    		Query query = QueryFactory.create(queryStr);
    		
    		qexec = QueryExecution.service(endpoint)
    				.query(query)
    				.timeout(timeout)
    				.build();
    			
			ResultSet rs = qexec.execSelect();
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				Iterator<String> itVars = qs.varNames();
				
				// We add the headers if not done yet
				if (res.size() == 0) {
					itVars.forEachRemaining(v -> res.put(v, GamaListFactory.create(Types.STRING)));
				}
				
				// We add the values
				while (itVars.hasNext()) {
					String var = itVars.next().toString();
					String val = qs.get(var).toString();
					res.get(var).add(val);
				}
			}
		} catch (Exception e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error(	"While executing request: '" + queryStr + "'\n"
																			+ "on endpoint: '" + endpoint + "'\n"
																			+ "error:'" + e.toString(), scope), false);
			
			return null; 
    	}
    	finally {
			if (qexec != null) qexec.close();
		}
    	
    	return res;
    }

}