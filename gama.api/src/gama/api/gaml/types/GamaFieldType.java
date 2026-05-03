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
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IField;

/**
 * Represents the GAML field type.
 * <p>
 * Fields are two-dimensional matrices holding float values. They provide a lightweight alternative to grids for models
 * with large raster data, as they hold spatialized discrete values without needing to build agents. Key features:
 * <ul>
 * <li>Can be created from grids, raster/DEM files, matrices, or manual definitions</li>
 * <li>Values are accessible by agents using their current location</li>
 * <li>Can be the target of the 'diffuse' statement</li>
 * <li>Can be displayed using the 'mesh' layer definition</li>
 * <li>By default cover the whole environment like grids</li>
 * </ul>
 * </p>
 *
 * @author GAMA Development Team
 * @since GAMA 1.8
 * @see IField
 * @see GamaMatrixType
 */
@type (
		name = IKeyword.FIELD,
		id = IType.FIELD,
		wraps = { IField.class },
		kind = ISymbolKind.REGULAR,
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
	 * Constructs a new GamaFieldType.
	 *
	 * @param typesManager
	 *            the types manager for type resolution
	 */
	public GamaFieldType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a field with specified types.
	 * <p>
	 * Fields are specialized matrices that hold float values in a two-dimensional spatial grid. This method handles
	 * conversion from various sources including matrices, files, and grids.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to cast
	 * @param param
	 *            optional casting parameter
	 * @param keyType
	 *            the key type (not used for fields, as they use spatial coordinates)
	 * @param contentsType
	 *            the content type (should be float)
	 * @param copy
	 *            whether to copy the result
	 * @return the field instance
	 * @throws GamaRuntimeException
	 *             if casting fails
	 */
	@Override
	public IField cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) throws GamaRuntimeException {
		return GamaMatrixFactory.castToField(scope, obj, param, contentsType, copy);
	}

	/**
	 * Determines the content type when casting an expression to a field.
	 * <p>
	 * Fields always contain float values.
	 * </p>
	 *
	 * @param exp
	 *            the expression to analyze
	 * @return the FLOAT type
	 */
	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		return Types.FLOAT;
	}

	/**
	 * Gets the content type for fields.
	 *
	 * @return the FLOAT type, as fields hold float values
	 */
	@Override
	public IType<?> getContentType() { return Types.FLOAT; }

	/**
	 * Indicates whether field values can be drawn/visualized.
	 *
	 * @return true, as fields can be visualized using mesh layers
	 */
	@Override
	public boolean isDrawable() { return true; }

	/**
	 * Constructors to be used in GAML besides the default "casting" one (i.e. field(xxx))
	 */

}
