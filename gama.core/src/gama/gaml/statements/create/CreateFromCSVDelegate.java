/*******************************************************************************************************
 *
 * CreateFromCSVDelegate.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gama.core.common.interfaces.ICreateDelegate;
import gama.core.runtime.IScope;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.file.GamaCSVFile;
import gama.core.util.matrix.IMatrix;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.CreateStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Class CreateFromDatabaseDelegate.
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 */
public class CreateFromCSVDelegate implements ICreateDelegate {

	/**
	 * Method acceptSource()
	 *
	 * @see gama.core.common.interfaces.ICreateDelegate#acceptSource(IScope, java.lang.Object)
	 */
	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return source instanceof GamaCSVFile;
	}

	/**
	 * Method createFrom() Method used to read initial values and attributes from a CSV values descring a synthetic
	 * population
	 *
	 * @author Alexis Drogoul
	 * @since 04-09-2012
	 * @see gama.core.common.interfaces.ICreateDelegate#createFrom(gama.core.runtime.IScope, java.util.List, int,
	 *      java.lang.Object)
	 */
	@SuppressWarnings ("rawtypes")
	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object input, final Arguments init, final CreateStatement statement) {
		final GamaCSVFile source = (GamaCSVFile) input;
		final IExpression header = statement.getHeader();
		if (header != null) { source.forceHeader(Cast.asBool(scope, header.value(scope))); }
		final boolean hasHeader = source.hasHeader(scope);
		final IMatrix<?> mat = source.getContents(scope);
		if (mat == null || mat.isEmpty(scope)) return false;
		int rows = mat.getRows(scope);
		final int cols = mat.getCols(scope);
		rows = max == null ? rows : Math.min(rows, max);

		List<String> headers;
		if (hasHeader) {
			headers = source.getAttributes(scope);
		} else {
			headers = new ArrayList<>();
			for (int j = 0; j < cols; j++) { headers.add(String.valueOf(j)); }
		}
		for (int i = 0; i < rows; i++) {
			final Map<String, Object> map = GamaMapFactory.create(hasHeader ? Types.STRING : Types.INT, Types.NO_TYPE);
			final IList vals = mat.getRow(i);
			for (int j = 0; j < cols; j++) {
				// see issue #3786
				String s = clean(headers.get(j));
				Object v = vals.get(j);
				map.put(s, v);
			}
			// CSV attributes are mixed with the attributes of agents
			statement.fillWithUserInit(scope, map);
			inits.add(map);
		}
		return true;
	}

	/**
	 * Clean.
	 *
	 * @param text
	 *            the text
	 * @return the string
	 */
	private static String clean(String text) {
		// // strips off all non-ASCII characters
		// text = text.replaceAll("[^\\x00-\\x7F]", "");
		// erases all the ASCII control characters
		text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
		// removes non-printable characters from Unicode
		text = text.replaceAll("\\p{C}", "");

		return text.trim();
	}

	/**
	 * Method fromFacetType()
	 *
	 * @see gama.core.common.interfaces.ICreateDelegate#fromFacetType()
	 */
	@Override
	public IType<?> fromFacetType() {
		return Types.FILE;
	}

}
