package gama.core.util.sparql;


import java.util.Iterator;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaPair;
import gama.core.util.IList;
import gama.gaml.types.Types;


public class SPARQLOperators {

	


    @operator(
    		value = "sparql_query",
    		doc = @doc("Queries a SPARQL endpoint with the query given as first parameter, at the endpoint given in the second, with timeout of 10s returns the results as a list of pairs."),
    		can_be_const = false,
    		category = {IOperatorCategory.SPARQL},
    		concept = {IConcept.DATABASE}
    		)
    public static IList<GamaPair<String, String>> queryEndpointAsMap(final String queryStr, final String endpoint){
    	return queryEndpointAsMap(queryStr, endpoint, 10000);
    }

    @operator(
    		value = "sparql_query",
    		doc = @doc("Queries a SPARQL endpoint with the query given as first parameter, at the endpoint given in the second, with timeout in millisecond given in the third and returns the results as a list of pairs."),
    		can_be_const = false,
    		category = {IOperatorCategory.SPARQL},
    		concept = {IConcept.DATABASE}
    		)
    public static IList<GamaPair<String, String>> queryEndpointAsMap(final String queryStr, final String endpoint, int timeout){
    	
    	Query query = QueryFactory.create(queryStr);
    	IList<GamaPair<String, String>> res = null;
    	
    	try (QueryExecution qexec = QueryExecutionHTTP.create()
				.endpoint(endpoint)
				.query(query)
				.timeout(timeout)
				.build()) {
			
			ResultSet rs = qexec.execSelect();
			res = GamaListFactory.create();
			
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				Iterator<String> itVars = qs.varNames();
				while (itVars.hasNext()) {
					String var = itVars.next().toString();
					String val = qs.get(var).toString();
					res.add(new GamaPair<String, String>(var, val, Types.STRING, Types.STRING));
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
    	}
    	return res;
    }

}