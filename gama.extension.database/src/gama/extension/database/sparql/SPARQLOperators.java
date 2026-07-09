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
import gama.api.runtime.scope.IScope;
import gama.api.types.dataframe.GamaDataFrameFactory;
import gama.api.types.dataframe.IDataFrame;
import gama.api.types.list.GamaListFactory;

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
	public static IDataFrame queryEndpoint(final IScope scope, final String queryStr,
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
	public static IDataFrame queryEndpoint(final IScope scope, final String queryStr,
			final String endpoint, final int timeout) {
		IDataFrame res = null;
		QueryExecution qexec = null;

		try {
			Query query = QueryFactory.create(queryStr);

			qexec = QueryExecution.service(endpoint).query(query).timeout(timeout).build();

			ResultSet rs = qexec.execSelect();

			// We start by creating the lists for each variable
			res = GamaDataFrameFactory.create(rs.getResultVars());
			

			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				var values = GamaListFactory.create();
				for (var variable : res.getColumns()) {
					Object value = null;
					try {
						// This may fail if the variable is not present in this row
						value = qs.get(variable);
					} catch (Exception e) {
						// We just leave the value to null
					}
					values.add(value);
				}
				res.addRow(values);
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