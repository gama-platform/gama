/*******************************************************************************************************
 *
 * GamaFieldType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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
import gama.api.data.factories.GamaMatrixFactory;
import gama.api.data.objects.IField;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;

/**
 * The Class GamaFieldType.
 */
@type (
		name = IKeyword.FIELD,
		id = IType.FIELD,
		wraps = { IField.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.GRID, IConcept.MATRIX },
		doc = @doc ("""
				Fields are two-dimensional matrices holding float values. They can be easily created from arbitrary sources (grid, raster or DEM files, matrices, \
				grids) and of course by hand. The values they hold are accessible by agents like grids are, using their current location. They can be the target of the \
				'diffuse' statement and can be displayed using the 'mesh' layer definition. \
				As such, they represent a lightweight alternative to grids, as they hold spatialized discrete values without having to build agents, which can be particularly \
				interesting for models with large raster data. Several fields can of course be defined, and it makes sense to define them in the global section as, for the moment, \
				they cover by default the whole environment, exactly like grids, and are created alongside them"""))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaFieldType extends GamaMatrixType {

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param contentType
	 *            the content type
	 * @param copy
	 *            the copy
	 * @return the i field
	 */

	@Override
	public IField cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) throws GamaRuntimeException {
		return GamaMatrixFactory.createFieldFrom(scope, obj, param, contentsType, copy);
	}

	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		return Types.FLOAT;
	}

	@Override
	public IType<?> getContentType() { return Types.FLOAT; }

	@Override
	public boolean isDrawable() { return true; }

	/**
	 * Constructors to be used in GAML besides the default "casting" one (i.e. field(xxx))
	 */

}
