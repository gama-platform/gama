/*******************************************************************************************************
 *
 * CreateFromDatabaseDelegate.java, in gama.extension.database, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.database.utils;

import java.util.List;
import java.util.Map;

import gama.api.additions.delegates.ICreateDelegate;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.dataframe.IDataFrame;
import gama.api.types.map.GamaMapFactory;

/**
 * Delegate that lets the 'create' statement build agents from the dataframe returned by a database 'select'. Each row of
 * the dataframe becomes one agent; the 'init' facet maps an agent attribute to a column name. Geometry columns are
 * already GAMA geometries in the dataframe, so no extra conversion is needed here.
 *
 * @author drogoul
 * @since 27 mai 2015
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class CreateFromDatabaseDelegate implements ICreateDelegate {

	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return source instanceof IDataFrame;
	}

	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object source, final Arguments init, final IStatement.Create statement) {
		final IDataFrame df = (IDataFrame) source;
		final int rows = df.getRows();
		final int num = max == null ? rows : Math.min(max, rows);
		for (int i = 0; i < num; i++) {
			final Map map = GamaMapFactory.create(Types.NO_TYPE, Types.NO_TYPE);
			computeInits(scope, map, df, i, init);
			inits.add(map);
		}
		return true;
	}

	/**
	 * Fills the init map of a single agent by reading, for each argument of the 'init' facet, the dataframe cell of the
	 * given row at the column named by the argument's expression.
	 *
	 * @param scope
	 *            the scope
	 * @param values
	 *            the init map to fill
	 * @param df
	 *            the dataframe
	 * @param rowIndex
	 *            the index of the row used for this agent
	 * @param init
	 *            the 'init' facet arguments
	 */
	private void computeInits(final IScope scope, final Map values, final IDataFrame df, final int rowIndex,
			final Arguments init) throws GamaRuntimeException {
		if (init == null) return;
		init.forEachArgument((s, e) -> {
			final IExpression valueExpr = e.getExpression();
			final String columnName = valueExpr.value(scope).toString().toUpperCase();
			if (!df.getColumns().contains(columnName)) throw GamaRuntimeException.error(
					"Create from DB: " + columnName + " is not a correct column name in the DB query results", scope);
			values.put(s, df.getCellValue(rowIndex, columnName));
			return true;
		});
	}

	@Override
	public IType fromFacetType() {
		return Types.DATAFRAME;
	}

}
