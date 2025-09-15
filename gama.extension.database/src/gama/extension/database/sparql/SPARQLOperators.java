package gama.extension.database.sparql;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import gama.core.util.IList;
import gama.gaml.types.Types;
import one.util.streamex.StreamEx;


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
    public static IList<IList<String>> queryEndpoint(final IScope scope, final String queryStr, final String endpoint){
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
    public static IList<IList<String>> queryEndpoint(final IScope scope, final String queryStr, final String endpoint, int timeout){
    	
    	Query query = QueryFactory.create(queryStr);
    	final IList<IList<String>> res = GamaListFactory.create();
    	
    	try (QueryExecution qexec = QueryExecution.service(endpoint)
				.query(query)
				.timeout(timeout)
				.build()) {
			
			ResultSet rs = qexec.execSelect();
			Map<String, Integer> colIndex = new HashMap<>();
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				Iterator<String> itVars = qs.varNames();
				// We add the header row
				if (res.size() == 0) {
					res.add(GamaListFactory.create());
					res.get(0).addAll(StreamEx.of(itVars).toList());
					StreamEx.of(itVars).forEach(v -> colIndex.put(v, res.get(0).indexOf(v)));// We store the index of each column
				}
				final IList<String> row = GamaListFactory.create(Types.STRING, colIndex.size());
				
				while (itVars.hasNext()) {
					String var = itVars.next().toString();
					String val = qs.get(var).toString();
					row.addValueAtIndex(scope, colIndex.get(var), val);
				}
				res.add(row);
				
			}
		} catch (Exception e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error(	"While executing request: '" + queryStr + "'\n"
																			+ "on endpoint: '" + endpoint + "'\n"
																			+ "error:'" + e.toString(), scope), false);
			return null; // will be casted to an empty list
    	}
    	
    	return res;
    }

}