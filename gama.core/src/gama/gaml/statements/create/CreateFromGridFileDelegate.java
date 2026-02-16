/*******************************************************************************************************
 *
 * CreateFromGridFileDelegate.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
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
import gama.api.constants.IKeyword;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.core.util.file.GamaGridFile;

/**
 * Class CreateFromDatabaseDelegate.
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class CreateFromGridFileDelegate implements ICreateDelegate {

	/**
	 * Method acceptSource()
	 *
	 * @see gama.api.additions.delegates.ICreateDelegate#acceptSource(IScope, java.lang.Object)
	 */
	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return source instanceof GamaGridFile;
	}

	/**
	 * Method createFrom() Method used to read initial values and attributes from a GRID file.
	 *
	 * @author Alexis Drogoul
	 * @since 04-09-2012
	 * @see gama.api.additions.delegates.ICreateDelegate#createFrom(gama.api.runtime.scope.IScope, java.util.List, int,
	 *      java.lang.Object)
	 */
	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object input, final Arguments init, final IStatement.Create statement) {
		final GamaGridFile file = (GamaGridFile) input;
		final int num = max == null ? file.length(scope) : Math.min(file.length(scope), max);
		for (int i = 0; i < num; i++) {
			final IShape g = file.get(scope, i);
			final Map map = g.getAttributes(true);
			// The shape is added to the initial values
			g.setAttribute(IKeyword.SHAPE, g);
			// GIS attributes are mixed with the attributes of agents
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
	public IType fromFacetType() {
		return Types.FILE;
	}

}
