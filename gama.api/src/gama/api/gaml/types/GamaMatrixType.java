/*******************************************************************************************************
 *
 * GamaMatrixType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
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
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;

/**
 * Type representing matrices in GAML - 2-dimensional containers that can hold any type of data.
 * <p>
 * Matrices are specialized containers organized in rows and columns, accessible either through point indices
 * (representing x,y coordinates) or through row/column access. Unlike simple 2D arrays, GAML matrices support efficient
 * spatial operations and can contain any data type.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li>2D structure with rows and columns</li>
 * <li>Indexed by {@link gama.api.types.geometry.IPoint} or row/column integers</li>
 * <li>Fixed-length container (dimensions set at creation)</li>
 * <li>Generic content type support</li>
 * <li>Efficient for grid-based and spatial data</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <pre>
 * {@code
 * // Create a 3x4 integer matrix
 * matrix<int> m <- matrix([[1,2,3,4], [5,6,7,8], [9,10,11,12]]);
 *
 * // Access by point
 * int val <- m[{1,2}];  // Gets element at column 1, row 2
 *
 * // Access by row and column
 * int val2 <- m[1,2];
 *
 * // Create from a list of lists
 * matrix<float> m2 <- matrix([[1.0, 2.0], [3.0, 4.0]]);
 *
 * // Matrix of geometries
 * matrix<geometry> grid <- matrix(list_of_shapes);
 * }
 * </pre>
 *
 * @author GAMA Development Team
 * @see GamaContainerType
 * @see IMatrix
 * @see gama.api.types.geometry.IPoint
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.MATRIX,
		id = IType.MATRIX,
		wraps = { IMatrix.class },
		kind = ISymbolKind.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER, IConcept.MATRIX },
		doc = @doc ("Matrices are 2-dimensional containers that can contain any type of date (not only floats or integers). They can be accessed with a point index or by rows / columns"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMatrixType extends GamaContainerType<IMatrix> {

	/**
	 * Constructs a new matrix type.
	 *
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaMatrixType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a matrix.
	 * <p>
	 * This method supports casting from various source types:
	 * <ul>
	 * <li>List of lists - creates a matrix with rows from the nested lists</li>
	 * <li>Single list - creates a matrix based on specified dimensions</li>
	 * <li>Another matrix - returns a copy if requested</li>
	 * <li>Container types - attempts to build a matrix from contents</li>
	 * </ul>
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a matrix
	 * @param param
	 *            optional parameter (typically dimensions as a point)
	 * @param keyType
	 *            the type of keys (always point for matrices)
	 * @param contentsType
	 *            the type of elements stored in the matrix
	 * @param copy
	 *            whether to create a copy if obj is already a matrix
	 * @return the matrix representation of the object
	 * @throws GamaRuntimeException
	 *             if the casting operation fails
	 */
	@Override
	public IMatrix cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) throws GamaRuntimeException {
		return GamaMatrixFactory.castToMatrix(scope, obj, param, contentsType, copy);
	}

	/**
	 * Returns the key type for matrices, which is always {@link gama.api.types.geometry.IPoint}.
	 * <p>
	 * Matrix indices are represented as 2D points where x is the column and y is the row.
	 * </p>
	 *
	 * @return the point type
	 */
	@Override
	public IType getKeyType() { return Types.POINT; }

	/**
	 * Indicates whether matrices have a fixed length.
	 * <p>
	 * Matrices are fixed-length containers - their dimensions are set at creation and cannot be changed. Individual
	 * elements can be modified, but rows/columns cannot be added or removed.
	 * </p>
	 *
	 * @return true, as matrices have fixed dimensions
	 */
	@Override
	public boolean isFixedLength() { return true; }

	/**
	 * Determines the content type when casting an expression to a matrix.
	 * <p>
	 * This method analyzes the structure of the expression to determine what type of elements the resulting matrix will
	 * contain:
	 * <ul>
	 * <li>For list of lists: returns the type of elements in the inner lists</li>
	 * <li>For containers: returns the container's content type</li>
	 * <li>Otherwise: returns the expression's type itself</li>
	 * </ul>
	 * </p>
	 *
	 * @param exp
	 *            the expression being cast to a matrix
	 * @return the type that matrix elements will have
	 */
	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		final IType cType = itemType.getContentType();
		if (itemType.id() == IType.LIST && cType.id() == IType.LIST) {
			// cf. issue #3792 -- the computation of type is now taken in charge by ListExpression itself
			// if (exp instanceof ListExpression) {
			// final IExpression[] array = ((ListExpression) exp).getElements();
			// if (array.length == 0) return Types.NO_TYPE;
			// return array[0].getGamlType().getContentType();
			// }
			if (!(exp instanceof IExpression.Map me)) return cType.getContentType();
			final IExpression[] array = me.getValues();
			if (array.length == 0) return Types.NO_TYPE;
			return array[0].getGamlType().getContentType();
		}
		if (Types.CONTAINER.isAssignableFrom(itemType)) return itemType.getContentType();
		return itemType;
	}

	/**
	 * Indicates whether matrices can be cast to constant values.
	 * <p>
	 * Matrices can be constant if all their elements are constant.
	 * </p>
	 *
	 * @return true, matrices can be constant
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Deserializes a matrix from a JSON representation.
	 * <p>
	 * The JSON map should contain:
	 * <ul>
	 * <li>"requested_type" - the type information including content type</li>
	 * <li>"contents" - the matrix elements as a list</li>
	 * <li>"cols" - number of columns</li>
	 * <li>"rows" - number of rows</li>
	 * </ul>
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param map2
	 *            the JSON map containing matrix data
	 * @return the deserialized matrix
	 */
	@Override
	public IMatrix deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		IType requested = (IType) map2.remove("requested_type");
		IList contents = (IList) map2.get("contents");
		Integer x = (Integer) map2.get("cols");
		Integer y = (Integer) map2.get("rows");
		IPoint size = GamaPointFactory.create(x, y);
		return GamaMatrixFactory.createFrom(scope, contents, requested.getContentType(), size);
	}

}
