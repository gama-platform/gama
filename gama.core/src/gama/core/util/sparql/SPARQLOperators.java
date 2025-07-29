package gama.core.util.sparql;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

/**
 * Created by Isai B. Cicourel
 */
public class SPARQLOperators {


    /**
     * Query an Endpoint using the given SPARQl query
     * @param szQuery
     * @param szEndpoint
     * @throws Exception
     */
    public void queryEndpoint(String szQuery, String szEndpoint)
    throws Exception
    {
        // Create a Query with the given String
        Query query = QueryFactory.create(szQuery);

        // Create the Execution using QueryExecutionHTTP for Jena 5.5.0
        try (QueryExecution qexec = QueryExecutionHTTP.create()
                .endpoint(szEndpoint)
                .query(query)
                .timeout(10000)
                .build()) {

            // Execute Query
            int iCount = 0;
            ResultSet rs = qexec.execSelect();
            while (rs.hasNext()) {
                // Get Result
                QuerySolution qs = rs.next();

                // Get Variable Names
                Iterator<String> itVars = qs.varNames();

                // Count
                iCount++;
                System.out.println("Result " + iCount + ": ");

                // Display Result
                while (itVars.hasNext()) {
                    String szVar = itVars.next().toString();
                    String szVal = qs.get(szVar).toString();
                    
                    System.out.println("[" + szVar + "]: " + szVal);
                }
            }
        }
    } // End of Method: queryEndpoint()

    @operator(
    		value = "query_sparql",
    		doc = @doc("Queries a SPARQL endpoint with the given query and returns the results as a list of maps."),
    		can_be_const = false,
    		category = {IOperatorCategory.SPARQL},
    		concept = {IConcept.DATABASE}
    		)
    public static List<Map<String, String>> queryEndpointAsMap(String szQuery, String szEndpoint, int timeout){
    	
    	Query query = QueryFactory.create(szQuery);
    	List<Map<String, String>> res = null;
    	
    	try (QueryExecution qexec = QueryExecutionHTTP.create()
				.endpoint(szEndpoint)
				.query(query)
				.timeout(timeout)
				.build()) {
			
			ResultSet rs = qexec.execSelect();
			res = new java.util.ArrayList<>();
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				Iterator<String> itVars = qs.varNames();
				
				while (itVars.hasNext()) {
					Map<String, String> map = new java.util.HashMap<>();
					String szVar = itVars.next().toString();
					String szVal = qs.get(szVar).toString();
					map.put(szVar, szVal);
					res.add(map);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
    	}
    	return res;
    }
//
//    public static void main(String[] args) throws IOException {
//        // SPARQL Query
//        String szQuery = "select * where {?Subject ?Predicate ?Object} LIMIT 1";
//
//        // Arguments
//        if (args != null && args.length == 1) {
//            szQuery = new String(
//                    Files.readAllBytes(Paths.get(args[0])),
//                    Charset.defaultCharset());
//        }
//
//        // DBPedia Endpoint
//        String szEndpoint = "http://dbpedia.org/sparql";
//
//        // Query DBPedia
//        try {
//            SPARQLOperators q = new SPARQLOperators();
//            q.queryEndpoint(szQuery, szEndpoint);
//        }
//        catch (Exception ex) {
//            System.err.println(ex);
//        }
//    }
}