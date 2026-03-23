/*******************************************************************************************************
 *
 * SPARQLOperators.java, in gama.extension.database, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.database.sparql;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import gama.annotations.doc;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;

/**
 * The Class SPARQLOperators.
 */
public class SPARQLOperators {

	/**
	 * Query endpoint.
	 *
	 * @param scope
	 *            the scope
	 * @param queryStr
	 *            the query str
	 * @param endpoint
	 *            the endpoint
	 * @return the i map
	 */
	@operator (
			value = "sparql_query",
			doc = @doc (
					value = "Queries a SPARQL endpoint with the query given as first parameter, at the endpoint given in the second, with timeout of 10s returns the results as a list of lists of strings (first row is the header)",
					special_cases = {
							"If the query fails, a non-blocking error is raised, the variable #current_error is filled with the error message and an empty list is returned." }),
			can_be_const = false,
			category = { IOperatorCategory.SPARQL },
			concept = { IConcept.DATABASE })
	@no_test
	public static IMap<String, IList<String>> queryEndpoint(final IScope scope, final String queryStr,
			final String endpoint) {
		return queryEndpoint(scope, queryStr, endpoint, 10000);
	}

	/**
	 * Query endpoint.
	 *
	 * @param scope
	 *            the scope
	 * @param queryStr
	 *            the query str
	 * @param endpoint
	 *            the endpoint
	 * @param timeout
	 *            the timeout
	 * @return the i map
	 */
	@operator (
			value = "sparql_query",
			doc = @doc (
					value = "Queries a SPARQL endpoint with the query given as first parameter, at the endpoint given in the second, with timeout in millisecond given in the third and returns the results as a list of lists of strings."
							+ " The first list is the header with the variable names, the following lists are the results.",
					special_cases = {
							"If the query fails, a non-blocking error is raised, the variable #current_error is filled with the error message and an empty list is returned." }),
			can_be_const = false,
			category = { IOperatorCategory.SPARQL },
			concept = { IConcept.DATABASE })
	@no_test
	public static IMap<String, IList<String>> queryEndpoint(final IScope scope, final String queryStr,
			final String endpoint, final int timeout) {

		final IMap<String, IList<String>> res = GamaMapFactory.create(Types.STRING, Types.LIST);
		QueryExecution qexec = null;

		try {
			Query query = QueryFactory.create(queryStr);

			qexec = QueryExecution.service(endpoint).query(query).timeout(timeout).build();

			ResultSet rs = qexec.execSelect();

			// We start by creating the lists for each variable
			var variables = rs.getResultVars();
			variables.forEach(v -> res.put(v, GamaListFactory.create(Types.STRING)));

			while (rs.hasNext()) {
				QuerySolution qs = rs.next();

				for (var variable : variables) {
					String value = null;
					try {
						// This may fail if the variable is not present in this row
						value = qs.get(variable).toString();
					} catch (Exception e) {
						// We just leave the value to null
					}
					res.get(variable).add(value);
				}
			}
		} catch (Exception e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("While executing request: '" + queryStr
					+ "'\n" + "on endpoint: '" + endpoint + "'\n" + "error:'" + e.toString(), scope), false);
			return null;
		} finally {
			if (qexec != null) { qexec.close(); }
		}

		return res;
	}

}