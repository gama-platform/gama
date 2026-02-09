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

import gama.api.additions.delegates.ICreateDelegate;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.core.util.file.GamaGeometryFile;

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
	 * @see gama.api.additions.delegates.ICreateDelegate#acceptSource(IScope, java.lang.Object)
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
	 * @see gama.api.additions.delegates.ICreateDelegate#createFrom(gama.api.runtime.scope.IScope, java.util.List, int,
	 *      java.lang.Object)
	 */
	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object input, final Arguments init, final IStatement.Create statement) {
		final IContainer.Addressable<Integer, IShape, Integer, IShape> container =
				(IContainer.Addressable<Integer, IShape, Integer, IShape>) input;
		final int num = max == null ? container.length(scope) : Math.min(container.length(scope), max);
		for (int i = 0; i < num; i++) {
			IShape g = container.get(scope, i);
			if (g instanceof IPoint) { g = GamaShapeFactory.buildPoint(g); }

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
	 * @see gama.api.additions.delegates.ICreateDelegate#fromFacetType()
	 */
	@Override
	public IType fromFacetType() {
		return Types.CONTAINER.of(Types.GEOMETRY);
	}

}
