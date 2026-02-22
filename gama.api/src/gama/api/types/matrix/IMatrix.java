/*******************************************************************************************************
 *
 * IMatrix.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.matrix;

import org.eclipse.core.runtime.ISafeRunnable;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.getter;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IContainer.Addressable;
import gama.api.types.misc.IContainer.Modifiable;
import gama.api.utils.interfaces.IFieldMatrixProvider;
import gama.api.utils.interfaces.ISafeConsumer;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;
import gama.api.utils.random.IRandom;
import one.util.streamex.StreamEx;

/**
 * The fundamental interface for two-dimensional matrices in GAMA.
 * 
 * <p>
 * A matrix is a two-dimensional addressable and modifiable container that stores elements in a grid structure indexed
 * by {column, row} coordinates (represented as {@link IPoint} instances). Matrices support element-wise mathematical
 * operations, conversions to other container types, and specialized operations for data manipulation.
 * </p>
 * 
 * <h2>Key Characteristics</h2>
 * <ul>
 * <li><strong>Generic content:</strong> Can store any GAMA type (integers, floats, objects, agents, geometries,
 * etc.)</li>
 * <li><strong>Column-row indexing:</strong> Elements are accessed using {column, row} points, with columns
 * corresponding to x-coordinates and rows to y-coordinates</li>
 * <li><strong>Fixed dimensions:</strong> Once created, matrix dimensions are typically fixed (though copy operations
 * can resize)</li>
 * <li><strong>Modifiable and addressable:</strong> Supports both reading and writing operations at specific
 * indices</li>
 * </ul>
 * 
 * <h2>Available Variables</h2>
 * <ul>
 * <li><strong>dimension</strong> (point) - Returns the dimensions as a point {columns, rows}</li>
 * <li><strong>rows</strong> (int) - The number of rows in the matrix</li>
 * <li><strong>columns</strong> (int) - The number of columns in the matrix</li>
 * </ul>
 * 
 * <h2>Supported Operations</h2>
 * <ul>
 * <li><strong>Element access:</strong> get(), set(), row_at(), column_at()</li>
 * <li><strong>Mathematical operations:</strong> +, -, *, / (with matrices or scalars)</li>
 * <li><strong>Conversions:</strong> as_list(), rows_list(), columns_list()</li>
 * <li><strong>Manipulation:</strong> reverse(), copy(), shuffleWith()</li>
 * <li><strong>Queries:</strong> contains(), containsKey(), firstValue(), lastValue(), isEmpty()</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>
 * // Access matrix elements
 * T value = matrix.get(scope, col, row);
 * matrix.set(scope, col, row, newValue);
 * 
 * // Get rows and columns
 * IList&lt;T&gt; row = matrix.getRow(2);
 * IList&lt;T&gt; column = matrix.getColumn(1);
 * 
 * // Mathematical operations
 * IMatrix result = matrix1.plus(scope, matrix2);
 * IMatrix scaled = matrix.times(2.5);
 * 
 * // Convert to list
 * IList&lt;T&gt; list = matrix.asList(scope);
 * </pre>
 * 
 * @param <T>
 *            the type of elements stored in the matrix
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
@vars ({ @variable (
		name = IMatrix.DIMENSION,
		type = IType.POINT,
		doc = { @doc ("Returns the dimension (columns x rows) of the receiver matrix") }),
		@variable (
				name = IMatrix.ROWS,
				type = IType.INT,
				doc = { @doc ("Returns the number of rows of the receiver matrix") }),
		@variable (
				name = IMatrix.COLUMNS,
				type = IType.INT,
				doc = { @doc ("Returns the number of columns of the receiver matrix") }) })

@SuppressWarnings ({ "rawtypes" })
public interface IMatrix<T> extends IContainer.Modifiable<IPoint, T, IPoint, T>,
		IContainer.Addressable<IPoint, T, IPoint, T>, IFieldMatrixProvider {

	/**
	 * Cols, rows instead of row cols because intended to work with xSize and ySize dimensions.
	 */

	String DIMENSION = "dimension";

	/** The rows. */
	String ROWS = "rows";

	/** The columns. */
	String COLUMNS = "columns";

	/**
	 * Gets the rows.
	 *
	 * @param scope
	 *            the scope
	 * @return the rows
	 */
	@Override
	@getter (ROWS)
	int getRows(IScope scope);

	/**
	 * Gets the cols.
	 *
	 * @param scope
	 *            the scope
	 * @return the cols
	 */
	@Override
	@getter (COLUMNS)
	int getCols(IScope scope);

	/**
	 * Redefined to reverse the logic (calls getFieldData())
	 */
	@Override
	default double[] getBand(final IScope scope, final int index) {
		if (index == 0) return getFieldData(scope);
		return null;
	}

	/**
	 * Gets the field data.
	 *
	 * @param scope
	 *            the scope
	 * @return the field data
	 */
	// Redefined so as to reverse the calling (getBand() now calls it)
	@Override
	double[] getFieldData(final IScope scope);

	/**
	 * Gets the dimensions.
	 *
	 * @return the dimensions
	 */
	@getter (DIMENSION)
	IPoint getDimensions();

	/**
	 * Gets the rows list.
	 *
	 * @return the rows list
	 */
	@operator (
			value = "rows_list",
			can_be_const = true,
			content_type = IType.LIST,
			content_type_content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "returns a list of the rows of the matrix, with each row as a list of elements",
			examples = { @example (
					value = "rows_list(matrix([[\"el11\",\"el12\",\"el13\"],[\"el21\",\"el22\",\"el23\"],[\"el31\",\"el32\",\"el33\"]]))",
					equals = "[[\"el11\",\"el21\",\"el31\"],[\"el12\",\"el22\",\"el32\"],[\"el13\",\"el23\",\"el33\"]]") },
			see = "columns_list")
	IList<IList<T>> getRowsList();

	/**
	 * Gets the columns list.
	 *
	 * @return the columns list
	 */
	@operator (
			value = "columns_list",
			can_be_const = true,
			content_type = IType.LIST,
			content_type_content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "returns a list of the columns of the matrix, with each column as a list of elements",
			examples = { @example (
					value = "columns_list(matrix([[\"el11\",\"el12\",\"el13\"],[\"el21\",\"el22\",\"el23\"],[\"el31\",\"el32\",\"el33\"]]))",
					equals = "[[\"el11\",\"el12\",\"el13\"],[\"el21\",\"el22\",\"el23\"],[\"el31\",\"el32\",\"el33\"]]") },
			see = "rows_list")
	IList<IList<T>> getColumnsList();

	/**
	 * As list.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 */
	@operator (
			value = "as_list",
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "returns a list of all the elements of the matrix, in row-major order",
			examples = { @example (
					value = "as_list(matrix([[\"el11\",\"el21\",\"el31\"],[\"el12\",\"el22\",\"el32\"],[\"el13\",\"el23\",\"el33\"]]))",
					equals = "[\"el11\",\"el12\",\"el13\",\"el21\",\"el22\",\"el23\",\"el31\",\"el32\",\"el33\"]") },
			see = { "rows_list", "columns_list" })
	@test ("as_list(matrix([[1,4,7],[2,5,8],[3,6,9]])) = [1,2,3,4,5,6,7,8,9]")
	IList<T> asList(final IScope scope);

	/**
	 * Gets the row.
	 *
	 * @param num_line
	 *            the num line
	 * @return the row
	 */
	@operator (
			value = "row_at",
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			can_be_const = true,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "returns the row at a num_line (right-hand operand)",
			examples = { @example (
					value = "matrix([[\"el11\",\"el12\",\"el13\"],[\"el21\",\"el22\",\"el23\"],[\"el31\",\"el32\",\"el33\"]]) row_at 2",
					equals = "[\"el13\",\"el23\",\"el33\"]") },
			see = { "column_at", "columns_list" })
	IList<T> getRow(Integer num_line);

	/**
	 * Gets the column.
	 *
	 * @param num_line
	 *            the num line
	 * @return the column
	 */
	@operator (
			value = "column_at",
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			can_be_const = true,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "returns the column at a num_col (right-hand operand)",
			examples = { @example (
					value = "matrix([[\"el11\",\"el12\",\"el13\"],[\"el21\",\"el22\",\"el23\"],[\"el31\",\"el32\",\"el33\"]]) column_at 2",
					equals = "[\"el31\",\"el32\",\"el33\"]") },
			see = { "row_at", "rows_list" })
	IList<T> getColumn(Integer num_line);

	/**
	 * Plus.
	 *
	 * @param scope
	 *            the scope
	 * @param other
	 *            the other
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc ("Returns a matrix containing the addition of  the elements of two matrices in argument "))
	@test ("matrix([[1,2],[3,4]]) + matrix([[1,2],[3,4]]) = matrix([[2,4],[6,8]])")
	IMatrix plus(IScope scope, IMatrix other) throws GamaRuntimeException;

	/**
	 * Times.
	 *
	 * @param scope
	 *            the scope
	 * @param other
	 *            the other
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc ("Multiplies the two matrices operands"))
	@test ("matrix([[1,2],[3,4]]) * matrix([[1,2],[3,4]]) = matrix([[1,4],[9,16]]) ")
	IMatrix times(IScope scope, IMatrix other) throws GamaRuntimeException;

	/**
	 * Divides.
	 *
	 * @param scope
	 *            the scope
	 * @param other
	 *            the other
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc ("Divides the two matrices operands"))
	@test ("matrix([[1,2],[3,4]]) / matrix([[1,2],[3,4]]) = matrix([[1,1],[1,1]])")
	IMatrix divides(IScope scope, IMatrix other) throws GamaRuntimeException;

	/**
	 * Minus.
	 *
	 * @param scope
	 *            the scope
	 * @param other
	 *            the other
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Performs a subtraction between the two matrix operands"))
	@test ("matrix([[1,2],[3,4]]) - matrix([[1,2],[3,4]]) = matrix([[0,0],[0,0]])")
	IMatrix minus(IScope scope, IMatrix other) throws GamaRuntimeException;

	/**
	 * Times.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Performs a multiplication between the matrix operand and the float operand"))
	@test ("matrix([[1,2],[3,4]]) * 2.5 = matrix([[2.5,5.0],[7.5,10]])")
	IMatrix times(Double val) throws GamaRuntimeException;

	/**
	 * Times.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc ("Performs a multiplication between the two matrix operands"))
	@test ("matrix([[1,2],[3,4]]) * 2 = matrix([[2,4],[6,8]])")
	IMatrix times(Integer val) throws GamaRuntimeException;

	/**
	 * Divides.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Divides all the elements of the matrix operand by the float operand"))
	@test ("matrix([[1,2],[3,4]]) / 2.5 = matrix([[0.4,0.8],[1.2,1.6]])")
	IMatrix divides(Double val) throws GamaRuntimeException;

	/**
	 * Divides.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Divides all the elements of the matrix operand by the integer operand"))
	@test ("matrix([[1,2],[3,4]]) / 2 = matrix([[0.5,1],[1.5,2]])")
	IMatrix divides(Integer val) throws GamaRuntimeException;

	/**
	 * Plus.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Adds the float operand to all the elements in the matrix"))
	@test ("matrix([[1,2],[3,4]]) + 22.5 = matrix([[23.5,24.5],[25.5,26.5]])")
	IMatrix plus(Double val) throws GamaRuntimeException;

	/**
	 * Plus.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Adds the int operand to all the elements in the matrix"))
	@test ("matrix([[1,2],[3,4]]) + 2 = matrix([[3,4],[5,6]])")
	IMatrix plus(Integer val) throws GamaRuntimeException;

	/**
	 * Minus.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Subtracts the float operand from all the elements in the matrix"))
	@test ("matrix([[1,2],[3,4]]) - 1.5 = matrix([[-0.5,0.5],[1.5,2.5]])")
	IMatrix minus(Double val) throws GamaRuntimeException;

	/**
	 * Minus.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Subtracts the int operand from all the elements in the matrix"))
	@test ("matrix([[1,2],[3,4]]) - 1 = matrix([[0,1],[2,3]])")
	IMatrix minus(Integer val) throws GamaRuntimeException;

	/**
	 * Gets the.
	 *
	 * @param scope
	 *            the scope
	 * @param col
	 *            the col
	 * @param row
	 *            the row
	 * @return the t
	 */
	T get(IScope scope, final int col, final int row);

	/**
	 * Sets the.
	 *
	 * @param scope
	 *            the scope
	 * @param col
	 *            the col
	 * @param row
	 *            the row
	 * @param obj
	 *            the obj
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void set(IScope scope, final int col, final int row, final Object obj) throws GamaRuntimeException;

	/**
	 * Stream.
	 *
	 * @param scope
	 *            the scope
	 * @return the stream ex
	 */
	@Override
	StreamEx<T> stream(final IScope scope);

	/**
	 * Contains key.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@Override
	default boolean containsKey(final IScope scope, final Object o) {
		if (o instanceof final IPoint p)
			return p.getX() >= 0 && p.getY() >= 0 && p.getX() < getCols(scope) && p.getY() < getRows(scope);
		return false;
	}

	/**
	 * Removes the.
	 *
	 * @param scope
	 *            the scope
	 * @param col
	 *            the col
	 * @param row
	 *            the row
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	Object remove(IScope scope, final int col, final int row) throws GamaRuntimeException;

	/**
	 * Shuffle with.
	 *
	 * @param randomAgent
	 *            the random agent
	 */
	void shuffleWith(IRandom randomAgent);

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	IMatrix<T> copy(IScope scope) throws GamaRuntimeException;

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @param preferredSize
	 *            the preferred size
	 * @param copy
	 *            the copy
	 * @return the i matrix
	 */
	IMatrix<T> copy(IScope scope, IPoint preferredSize, boolean copy);

	/**
	 * Reverse.
	 *
	 * @param scope
	 *            the scope
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	IMatrix<T> reverse(final IScope scope) throws GamaRuntimeException;

	/**
	 * To json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @date 28 oct. 2023
	 */
	@Override
	default IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "cols", this.getCols(null), "rows", this.getRows(null), "contents",
				this.listValue(null, this.getGamlType().getContentType(), false));

	}

	/**
	 * Compute runtime type.
	 *
	 * @param scope
	 *            the scope
	 * @return the i type
	 */
	@Override
	default IType<?> computeRuntimeType(final IScope scope) {
		return Types.MATRIX.of(
				GamaType.findCommonType(stream(scope).map(e -> GamaType.actualTypeOf(scope, e)).toArray(IType.class)));
	}

	/**
	 * Does nothing in the default case
	 *
	 * @param scope
	 * @param bprime
	 * @return
	 */
	default IMatrix _opAppendVertically(final IScope scope, final IMatrix bprime) {
		return this;
	}

	/**
	 * @param scope
	 * @return
	 */
	IMatrix _reverse(IScope scope);

	/**
	 * Gets the nth element.
	 *
	 * @param index
	 *            the index
	 * @return the nth element
	 */
	T getNthElement(Integer index);

	/**
	 * Row by row.
	 *
	 * @param scope
	 *            the scope
	 * @param forEachValue
	 *            the for each value
	 * @param afterEachValue
	 *            the after each value
	 * @param afterEachRow
	 *            the after each row
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void rowByRow(final IScope scope, final ISafeConsumer<T> forEachValue, final ISafeRunnable afterEachValue,
			final ISafeRunnable afterEachRow) throws GamaRuntimeException;

}