/*******************************************************************************************************
 *
 * GamaGeometryType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IShape;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */

/**
 * The Class GamaGeometryType.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 4 nov. 2023
 */
@type (
		name = IKeyword.GEOMETRY,
		id = IType.GEOMETRY,
		wraps = { IShape.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.GEOMETRY },
		doc = @doc ("Represents geometries, i.e. the support for the shapes of agents and all the spatial operations in GAMA."))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaGeometryType extends GamaType<IShape> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaGeometryType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc ("""
			Cast the argument into a geometry. If the argument is already a geometry or an agent, returns it; \
			if it is a species, returns the union of all its agents' geometries; if it is a pair, tries to build a segment from it; \
			if it is a file containing geometries, returns the union of these geometries; \
			if it is a container and its contents are points, builds the resulting geometry, \
			otherwise cast the objects present in the container as geometries and returns their union; \
			if it is a string, interprets it as a wkt specification; otherwise, returns nil.\s""")
	public IShape cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaShapeFactory.castToShape(scope, obj, copy);
	}

	@Override
	public IShape getDefault() { return null; }

	@Override
	public boolean isDrawable() { return true; }

	@Override
	public IType getKeyType() { return Types.STRING; }

	@Override
	public boolean isFixedLength() { return false; }

	@Override
	public boolean canCastToConst() {
		return false;
	}

}
