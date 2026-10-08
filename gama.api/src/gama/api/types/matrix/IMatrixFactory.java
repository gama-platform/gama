/*******************************************************************************************************
 *
 * IMatrixFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.matrix;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.IList;
import gama.api.utils.interfaces.IFieldMatrixProvider;

/**
 * The internal factory interface for creating matrix and field instances.
 * 
 * <p>
 * This interface defines the contract that concrete matrix factory implementations must fulfill. It is used internally
 * by {@link GamaMatrixFactory} to delegate the actual creation of matrix and field objects. This design allows for
 * pluggable implementations while maintaining a consistent public API.
 * </p>
 * 
 * <p>
 * Implementations of this interface are responsible for:
 * </p>
 * <ul>
 * <li>Creating empty matrices of specific types and dimensions</li>
 * <li>Converting various data sources (lists, matrices, values) into matrices</li>
 * <li>Initializing matrix contents from expressions (with optional parallel evaluation)</li>
 * <li>Creating specialized field instances</li>
 * <li>Handling type conversions and resizing operations</li>
 * </ul>
 * 
 * <p>
 * This interface should not be used directly by user code. Instead, use the static methods in
 * {@link GamaMatrixFactory}.
 * </p>
 * 
 * @see GamaMatrixFactory
 * @author drogoul
 * @since GAMA 1.0
 */
public interface IMatrixFactory {

	/**
	 * Creates a matrix similar to another matrix but with potential resizing.
	 *
	 * @param scope
	 *            the scope
	 * @param matrix
	 *            the source matrix
	 * @param dimensions
	 *            the new dimensions
	 * @return the new matrix
	 */
	IMatrix createMatrixLike(final IScope scope, final IMatrix matrix, final IPoint dimensions);

	/**
	 * Creates a matrix from a list of values.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the elements to fill the matrix
	 * @param desiredType
	 *            the content type of the matrix
	 * @param preferredSize
	 *            the preferred dimensions
	 * @return the created matrix
	 */
	IMatrix createFrom(final IScope scope, final IList list, final IType desiredType, final IPoint preferredSize);

	/**
	 * Creates a matrix from another matrix, with casting/copying options.
	 *
	 * @param scope
	 *            the scope
	 * @param matrix
	 *            the source matrix
	 * @param desiredType
	 *            the target content type
	 * @param preferredSize
	 *            the preferred dimensions
	 * @param copy
	 *            whether to force a copy even if compatible
	 * @return the created matrix
	 */
	IMatrix createFromMatrix(final IScope scope, final IMatrix matrix, final IType desiredType,
			final IPoint preferredSize, final boolean copy);

	/**
	 * Creates a matrix filled with a value (determined by an expression).
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the expression providing values
	 * @param p
	 *            the dimensions
	 * @param parallel
	 *            whether evaluation should happen in parallel
	 * @return the created matrix
	 * @throws GamaRuntimeException
	 *             if an error occurs during evaluation
	 */
	IMatrix createWith(final IScope scope, final IExpression val, final IPoint p, final boolean parallel)
			throws GamaRuntimeException;

	/**
	 * Creates a matrix filled with a value, using explicit dimensions.
	 *
	 * @param scope
	 *            the scope
	 * @param fillExpr
	 *            the expression providing values
	 * @param cols
	 *            number of columns
	 * @param rows
	 *            number of rows
	 * @param parallel
	 *            whether evaluation should happen in parallel
	 * @return the created matrix
	 */
	IMatrix createWith(final IScope scope, final IExpression fillExpr, final int cols, final int rows,
			final boolean parallel);

	/**
	 * Creates a matrix filled with a single static value.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the value
	 * @param p
	 *            the dimensions
	 * @param contentsType
	 *            the content type
	 * @return the created matrix
	 * @throws GamaRuntimeException
	 *             if error occurs
	 */
	IMatrix createWithValue(final IScope scope, final Object val, final IPoint p, final IType contentsType)
			throws GamaRuntimeException;

	/**
	 * Creates a matrix filled with a single static value, using explicit dimensions.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the value
	 * @param cols
	 *            columns
	 * @param rows
	 *            rows
	 * @param contentsType
	 *            the content type
	 * @return the created matrix
	 * @throws GamaRuntimeException
	 *             if error occurs
	 */
	IMatrix createWithValue(final IScope scope, final Object val, final int cols, final int rows,
			final IType contentsType) throws GamaRuntimeException;

	/**
	 * Creates a matrix by initializing elements using a looping variable (like 'each' used in GAML).
	 *
	 * @param scope
	 *            the scope
	 * @param eachName
	 *            the name of the temporary variable for initialization
	 * @param size
	 *            the dimensions
	 * @param init
	 *            the initialization expression
	 * @return the created matrix
	 */
	IMatrix createWith(final IScope scope, final String eachName, final IPoint size, final IExpression init);

	/**
	 * Creates an empty matrix of specific dimensions and type.
	 *
	 * @param cols
	 *            number of columns
	 * @param rows
	 *            number of rows
	 * @param contentsType
	 *            the content type
	 * @return the created matrix
	 */
	IMatrix create(int cols, int rows, IType contentsType);

	/**
	 * Creates an empty matrix of specific dimensions and type ID.
	 *
	 * @param cols
	 *            columns
	 * @param rows
	 *            rows
	 * @param typeId
	 *            integer ID of the type
	 * @return the created matrix
	 */
	IMatrix create(int cols, int rows, int typeId);

	/**
	 * Creates a field (specialized grid) with data.
	 *
	 * @param scope
	 *            the scope
	 * @param cols
	 *            columns
	 * @param rows
	 *            rows
	 * @param data
	 *            double array of data
	 * @param no
	 *            no-data value
	 * @return the field
	 */
	IField createField(IScope scope, int cols, int rows, double[] data, double no);

	/**
	 * Creates an empty field.
	 *
	 * @param scope
	 *            the scope
	 * @param cols
	 *            columns
	 * @param rows
	 *            rows
	 * @return the field
	 */
	IField createField(IScope scope, int cols, int rows);

	/**
	 * Creates a field from an external data provider.
	 * 
	 * <p>
	 * This method is used to create fields from objects that can provide field data, such as images or other raster
	 * data sources. The provider must implement {@link IFieldMatrixProvider}.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param provider
	 *            the data provider containing field information
	 * @return a field created from the provider's data
	 */
	IField createFieldFromProvider(IScope scope, IFieldMatrixProvider provider);

}
