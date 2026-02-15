/*******************************************************************************************************
 *
 * CreateFromNullDelegate.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.create;

import java.util.List;
import java.util.Map;

import gama.api.additions.delegates.ICreateDelegate;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.map.GamaMapFactory;

/**
 * Class CreateFromDatabaseDelegate.
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 *
 */
@SuppressWarnings ({ "rawtypes" })
public class CreateFromNullDelegate implements ICreateDelegate {

	/**
	 * Method acceptSource()
	 *
	 * @see gama.api.additions.delegates.ICreateDelegate#acceptSource(IScope, java.lang.Object)
	 */
	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return source == null;
	}

	/**
	 * Method createFrom() reads initial values decribed by the modeler (facet with)
	 *
	 * @author Alexis Drogoul
	 * @since 04-09-2012
	 * @see gama.api.additions.delegates.ICreateDelegate#createFrom(gama.api.runtime.scope.IScope, java.util.List, int,
	 *      java.lang.Object)
	 */
	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object input, final Arguments init, final IStatement.Create statement) {
		Map<String, Object> nullMap = null;
		if (init == null) { nullMap = GamaMapFactory.create(); }
		final int num = max == null ? 1 : max;
		for (int i = 0; i < num; i++) {
			final Map<String, Object> map =
					init == null ? nullMap : GamaMapFactory.create(Types.NO_TYPE, Types.NO_TYPE);
			statement.fillWithUserInit(scope, map);
			inits.add(map);
		}
		return true;
	}

	/**
	 * Method fromFacetType()
	 *
	 * @see gama.api.additions.delegates.ICreateDelegate#fromFacetType()
	 */
	@Override
	public IType<?> fromFacetType() {
		return Types.NO_TYPE; // Only delegate allowed to do this
	}

}
