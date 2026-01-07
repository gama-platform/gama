/*******************************************************************************************************
 *
 * CreateFromGeometriesDelegate.java, in gama.core, is part of the source code of the GAMA modeling and simulation
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

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.shape.IPoint ;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.IAddressableContainer;
import gama.core.util.file.GamaGeometryFile;
import gama.core.util.list.IList;
import gama.gaml.interfaces.ICreateDelegate;
import gama.gaml.statements.CreateStatement;
import gama.gaml.statements.IArguments;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Class CreateFromDatabaseDelegate.
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class CreateFromGeometriesDelegate implements ICreateDelegate {

	/**
	 * Method acceptSource()
	 *
	 * @see gama.gaml.interfaces.ICreateDelegate#acceptSource(IScope, java.lang.Object)
	 */
	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		// THIS CONDITION MUST BE CHECKED : bypass a condition that belong to
		// the case createFromDatabase
		return source instanceof GamaGeometryFile
				|| source instanceof IList il && Types.GEOMETRY.isAssignableFrom(il.getGamlType().getContentType());
	}

	/**
	 * Method createFrom() Method used to read initial values and attributes from a CSV values describing a synthetic
	 * population
	 *
	 * @author Alexis Drogoul
	 * @since 04-09-2012
	 * @see gama.gaml.interfaces.ICreateDelegate#createFrom(gama.core.runtime.IScope, java.util.List, int,
	 *      java.lang.Object)
	 */
	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object input, final IArguments init, final CreateStatement statement) {
		final IAddressableContainer<Integer, IShape, Integer, IShape> container =
				(IAddressableContainer<Integer, IShape, Integer, IShape>) input;
		final int num = max == null ? container.length(scope) : Math.min(container.length(scope), max);
		for (int i = 0; i < num; i++) {
			IShape g = container.get(scope, i);
			if (g instanceof IPoint ) { g = GamaGeometryType.createPoint(g); }

			final Map map = g.getAttributes(true);
			// The shape is added to the initial values
			g.setAttribute(IKeyword.SHAPE, g);
			// GIS attributes are pushed to the scope in order to be read by read/get statements
			statement.fillWithUserInit(scope, map);
			inits.add(map);
		}
		return true;
	}

	/**
	 * Method fromFacetType()
	 *
	 * @see gama.gaml.interfaces.ICreateDelegate#fromFacetType()
	 */
	@Override
	public IType fromFacetType() {
		return Types.CONTAINER.of(Types.GEOMETRY);
	}

}
