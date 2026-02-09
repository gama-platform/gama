/*******************************************************************************************************
 *
 * GamaMatrixFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import java.util.Arrays;

import gama.annotations.doc;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IField;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.utils.IFieldMatrixProvider;

/**
 * A static factory for creating and manipulating {@link IMatrix} instances. This class handles matrix creation from
 * various sources (values, other matrices, expressions) and provides specialized methods for creating numeric (int,
 * float) or field matrices. It delegates to an {@link IMatrixFactory} implementation.
 */
public class GamaMatrixFactory implements IFactory<IMatrix> {

	/**
	 * The internal factory used for creating matrix instances.
	 */
	static IMatrixFactory InternalFactory;

	/**
	 * Configures the internal factory implementation.
	 *
	 * @param builder
	 *            the {@link IMatrixFactory} to be used as the internal builder.
	 */
	public static void setBuilder(final IMatrixFactory builder) { InternalFactory = builder; }

	/**
	 * Creates a matrix filled with values from a source object, resized to the specified point dimensions. Corresponds
	 * to the `as_matrix` operator in GAML.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the source object (value, list, file, etc.).
	 * @param point
	 *            the dimensions of the resulting matrix (x = cols, y = rows).
	 * @return the created {@link IMatrix}.
	 */
	@operator (
			value = "as_matrix",
			content_type = ITypeProvider.FIRST_CONTENT_TYPE_OR_TYPE,
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "casts the left operand into a matrix with right operand as preferred size",
			comment = """
					This operator is very useful to cast a file containing raster data into a matrix.\
					Note that both components of the right operand point should be positive, otherwise an exception is raised.\
					The operator as_matrix creates a matrix of preferred size. It fills in it with elements of the left operand until the matrix is full \
					If the size is to short, some elements will be omitted. Matrix remaining elements will be filled in by nil.""",
			usages = { @usage ("if the right operand is nil, as_matrix is equivalent to the matrix operator") },
			see = { IKeyword.MATRIX })
	@test ("as_matrix('a', {2,3}) = matrix(['a','a','a'],['a','a','a'])")
	@test ("as_matrix(1.0, {2,2}) = matrix([1.0,1.0],[1.0,1.0])")
	public static IMatrix createWithValue(final IScope scope, final Object obj, final IPoint point) {
		return createWithValue(scope, obj, point, Types.NO_TYPE);
	}

	/**
	 * Creates a matrix with values from an object, specifying dimensions and content type.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the source object.
	 * @param iPoint
	 *            the dimensions of the matrix.
	 * @param contentType
	 *            the target content type of matrix cells.
	 * @return the created {@link IMatrix}.
	 */
	public static IMatrix createWithValue(final IScope scope, final Object obj, final IPoint iPoint,
			final IType contentType) {
		if (InternalFactory == null) throw new IllegalStateException("Matrix builder is not set.");
		return InternalFactory.createWithValue(scope, obj, iPoint, contentType);
	}

	/**
	 * Creates a matrix from a list of values, reshaping it to the specified dimensions.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param contents
	 *            the list of values (flat).
	 * @param contentType
	 *            the target content type.
	 * @param size
	 *            the dimensions of the matrix.
	 * @return the created {@link IMatrix}.
	 */
	public static IMatrix createFrom(final IScope scope, final IList contents, final IType contentType,
			final IPoint size) {
		if (InternalFactory == null) throw new IllegalStateException("Matrix builder is not set.");
		return InternalFactory.createFrom(scope, contents, contentType, size);
	}

	/**
	 * Creates a matrix from another matrix, potentially resizing and casting.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param contents
	 *            the source matrix.
	 * @param t
	 *            the target content type.
	 * @param point
	 *            the new dimensions.
	 * @param b
	 *            deprecated/internal flag (check implementation).
	 * @return the created {@link IMatrix}.
	 */
	public static IMatrix createFromMatrix(final IScope scope, final IMatrix contents, final IType t,
			final IPoint point, final boolean b) {
		if (InternalFactory == null) throw new IllegalStateException("Matrix builder is not set.");
		return InternalFactory.createFromMatrix(scope, contents, t, point, b);
	}

	/**
	 * Creates a new empty matrix with the same type (Int, Float, Object) as the reference matrix, but with specified
	 * dimensions.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param matrix
	 *            the reference matrix to copy type from.
	 * @param dimensions
	 *            the dimensions of the new matrix.
	 * @return a new empty {@link IMatrix}.
	 */
	public static IMatrix createMatrixLike(final IScope scope, final IMatrix matrix, final IPoint dimensions) {
		return matrix.getGamlType().id() == IType.FIELD
				? createField(scope, (int) dimensions.getX(), (int) dimensions.getY())
				: switch (matrix.getGamlType().getContentType().id()) {
					case IType.INT -> createIntMatrix((int) dimensions.getX(), (int) dimensions.getY());
					case IType.FLOAT -> createFloatMatrix((int) dimensions.getX(), (int) dimensions.getY());
					default -> createObjectMatrix((int) dimensions.getX(), (int) dimensions.getY(),
							matrix.getGamlType().getContentType());
				};
	}

	/**
	 * Creates a matrix by evaluating an expression in parallel for each cell.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param init
	 *            the initialization expression.
	 * @param size
	 *            the dimensions of the matrix.
	 * @return the created {@link IMatrix}.
	 */
	@operator (
			value = "parallel_matrix_with",
			content_type = ITypeProvider.SECOND_CONTENT_TYPE_OR_TYPE,
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "Creates a matrix with a size provided by the first operand, and filled with the second operand. The given expression, unless constant, is evaluated for each cell and is done in parallel.",
			comment = "Note that both components of the right operand point should be positive, otherwise an exception is raised.\nIf run in parallel, some exception can happen in case the expression uses a random number generator that doesn't support parallel execution like mersenne.",
			see = { IKeyword.MATRIX, "as_matrix" })
	@test ("{2,2} matrix_with (1) = matrix([1,1],[1,1])")
	public static IMatrix parallelCreateWith(final IScope scope, final IExpression init, final IPoint size) {
		if (InternalFactory == null) throw new IllegalStateException("Matrix builder is not set.");
		return InternalFactory.createWith(scope, init, size, true);
	}

	/**
	 * Creates a matrix by evaluating an expression, optionally in parallel.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param init
	 *            the initialization expression.
	 * @param size
	 *            the dimensions of the matrix.
	 * @param b
	 *            whether to execute in parallel.
	 * @return the created {@link IMatrix}.
	 */
	public static IMatrix createWith(final IScope scope, final IExpression init, final IPoint size, final boolean b) {
		if (InternalFactory == null) throw new IllegalStateException("Matrix builder is not set.");
		return InternalFactory.createWith(scope, init, size, b);
	}

	/**
	 * Creates a matrix by evaluating an expression, with a named iterator ('each') accessible in the expression.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param eachName
	 *            the name of the variable representing the current cell index/point.
	 * @param size
	 *            the dimensions of the matrix.
	 * @param init
	 *            the initialization expression.
	 * @return the created {@link IMatrix}.
	 */
	@operator (
			value = "matrix_with",
			iterator = true,
			content_type = ITypeProvider.THIRD_CONTENT_TYPE_OR_TYPE,
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "creates a matrix with a size provided by the first operand, and filled with the second operand. The given expression, unless constant, is evaluated for each cell. As in any iterator, the value of 'each' represents the index of the current cell (a point (col, row)) and can be retrieved using 'each' or explicitly using the '(:x...' syntax ",
			comment = "Note that both components of the right operand point should be positive, otherwise an exception is raised.",
			see = { IKeyword.MATRIX, "as_matrix" })
	@test ("{2,2} matrix_with (1) = matrix([1,1],[1,1])")
	@test ("{2,2} matrix_with (p: p.x + p.y) = matrix([0.0, 1.0],[1.0,2.0])")
	public static IMatrix createWith(final IScope scope, final String eachName, final IPoint size,
			final IExpression init) {
		if (InternalFactory == null) throw new IllegalStateException("Matrix builder is not set.");
		return InternalFactory.createWith(scope, eachName, size, init);
	}

	/**
	 * Creates a matrix of a specific type (by {@link IType} instance).
	 *
	 * @param cols
	 *            number of columns.
	 * @param rows
	 *            number of rows.
	 * @param contentsType
	 *            the GAMA type of the content.
	 * @return the created {@link IMatrix}.
	 */
	public static <T> IMatrix<T> create(final int cols, final int rows, final IType<T> contentsType) {
		if (InternalFactory == null) throw new IllegalStateException("Matrix builder is not set.");
		return InternalFactory.create(cols, rows, contentsType);
	}

	/**
	 * Creates a matrix of a specific type (by type ID).
	 *
	 * @param cols
	 *            number of columns.
	 * @param rows
	 *            number of rows.
	 * @param typeId
	 *            the ID of the content type.
	 * @return the created {@link IMatrix}.
	 */
	public static IMatrix create(final int cols, final int rows, final int typeId) {
		if (InternalFactory == null) throw new IllegalStateException("Matrix builder is not set.");
		return InternalFactory.create(cols, rows, typeId);
	}

	/**
	 * Creates a matrix of integers.
	 *
	 * @param cols
	 *            number of columns.
	 * @param rows
	 *            number of rows.
	 * @return a new integer {@link IMatrix}.
	 */
	public static IMatrix<Integer> createIntMatrix(final int cols, final int rows) {
		return create(cols, rows, Types.INT);
	}

	/**
	 * Creates a matrix of doubles (floats in GAML).
	 *
	 * @param cols
	 *            number of columns.
	 * @param rows
	 *            number of rows.
	 * @return a new double {@link IMatrix}.
	 */
	public static IMatrix<Double> createFloatMatrix(final int cols, final int rows) {
		return create(cols, rows, Types.FLOAT);
	}

	/**
	 * Creates a generic object matrix.
	 *
	 * @param cols
	 *            number of columns.
	 * @param rows
	 *            number of rows.
	 * @param type
	 *            the type of the objects contained.
	 * @return a new object {@link IMatrix}.
	 */
	public static IMatrix<Object> createObjectMatrix(final int cols, final int rows, final IType<Object> type) {
		return create(cols, rows, type);
	}

	/**
	 * Creates a field (specialized float matrix) initialized with data.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param cols
	 *            number of columns.
	 * @param rows
	 *            number of rows.
	 * @param data
	 *            the initial data (flat array of doubles).
	 * @param no
	 *            the no-data value? (Interpretation depends on implementation).
	 * @return the created {@link IField}.
	 */
	public static IField createField(final IScope scope, final int cols, final int rows, final double[] data,
			final double no) {
		return InternalFactory.createField(scope, cols, rows, data, no);
	}

	/**
	 * Creates an empty field.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param cols
	 *            number of columns.
	 * @param rows
	 *            number of rows.
	 * @return the created {@link IField}.
	 */
	public static IField createField(final IScope scope, final int cols, final int rows) {
		return InternalFactory.createField(scope, cols, rows);
	}

	/**
	 * Converts an arbitrary object into a matrix.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to convert.
	 * @param param
	 *            optional dimensions or parameter.
	 * @param contentType
	 *            the target content type.
	 * @param copy
	 *            whether to copy if object is already a matrix.
	 * @return the created {@link IMatrix}.
	 */
	public static IMatrix createFrom(final IScope scope, final Object obj, final Object param, final IType contentType,
			final boolean copy) {
		if (obj == null && param == null) return null;
		final IPoint size = param instanceof IPoint i ? i : null;
		if (size == null) {
			if (obj instanceof IContainer) return ((IContainer) obj).matrixValue(scope, contentType, copy);
			return createWithValue(scope, obj, GamaPointFactory.create(1, 1), contentType);
		}
		if (size.getX() <= 0 || size.getY() < 0)
			throw GamaRuntimeException.error("Dimensions of a matrix should be positive.", scope);
		if (obj instanceof IContainer) return ((IContainer) obj).matrixValue(scope, contentType, size, copy);
		return createWithValue(scope, obj, size, contentType);
	}

	/**
	 * Convenience method to convert an object to a matrix with default settings.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to convert.
	 * @return the created {@link IMatrix}.
	 */
	public static IMatrix createFrom(final IScope scope, final Object obj) {
		return createFrom(scope, obj, null, Types.NO_TYPE, false);
	}

	/**
	 * @param scope
	 * @param gamaImage
	 * @return
	 */
	public static IField createFieldFromProvider(final IScope scope, final IFieldMatrixProvider provider) {
		return InternalFactory.createFieldFromProvider(scope, provider);
	}

	/**
	 * Builds the field.
	 *
	 * @param scope
	 *            the scope
	 * @param object
	 *            the object
	 * @return the i field
	 */
	public static IField createFieldFrom(final IScope scope, final Object object) {
		return createFieldFrom(scope, object, null, null, false);
	}

	/**
	 * Creates a new GamaMatrix object.
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
	public static IField createFieldFrom(final IScope scope, final Object obj, final Object param,
			final IType contentType, final boolean copy) {
		if (obj == null && param == null) return null;
		final IPoint size = param instanceof IPoint i ? i : null;

		if (size == null) {
			if (obj instanceof IField f && !copy) return f;
			if (obj instanceof IFieldMatrixProvider p) return createFieldFromProvider(scope, p);
			// Special case for grid species
			if (obj instanceof ISpecies species && species.isGrid()) return createFieldFrom(scope,
					species.getPopulation(scope).getTopology().getPlaces(), param, contentType, copy);
			if (obj instanceof IContainer) return createFieldFrom(scope,
					((IContainer) obj).matrixValue(scope, contentType, copy), null, contentType, copy);
		} else if (size.getX() <= 0 || size.getY() < 0)
			throw GamaRuntimeException.error("Dimensions of a field should be positive.", scope);
		if (obj instanceof IContainer) return createFieldFrom(scope,
				((IContainer) obj).matrixValue(scope, contentType, size, copy), null, contentType, copy);
		return createFieldWithObjectSizeAndType(scope, obj, size, contentType);

	}

	/**
	 * With.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param p
	 *            the p
	 * @param contentsType
	 *            the contents type
	 * @return the i field
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IField createFieldWithObjectSizeAndType(final IScope scope, final Object val, final IPoint p,
			final IType contentsType) throws GamaRuntimeException {
		int x = p == null ? 1 : (int) p.getX();
		int y = p == null ? 1 : (int) p.getY();
		return createFieldWithObjectSizeAndType(scope, val, x, y, contentsType);
	}

	/**
	 * With object.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param contentsType
	 *            the contents type
	 * @return the i field
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IField createFieldWithObjectSizeAndType(final IScope scope, final Object val, final int cols,
			final int rows, final IType contentsType) throws GamaRuntimeException {
		Double toStore = Cast.asFloat(scope, val);
		final IMatrix<Double> matrix = GamaMatrixFactory.createFloatMatrix(cols, rows);
		matrix.setAllValues(scope, toStore);
		return createFieldFrom(scope, matrix);
	}

	/**
	 * Builds the field with no data.
	 *
	 * @param scope
	 *            the scope
	 * @param object
	 *            the object
	 * @param noData
	 *            the no data
	 * @return the i field
	 */
	@operator (
			value = IKeyword.FIELD,
			can_be_const = false,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc ("Allows to build a field by specifying an arbitrary object (assuming this object can return a matrix of float) "
					+ "and a value representing the absence of data. ") })
	@no_test
	public static IField createFieldWithNoData(final IScope scope, final Object object, final double noData) {
		IField field = createFieldFrom(scope, object);
		field.setNoData(scope, noData);
		return field;
	}

	/**
	 * Creates a new GamaMatrix object.
	 *
	 * @param scope
	 *            the scope
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param init
	 *            the init
	 * @param no
	 *            the no
	 * @return the i field
	 */
	@operator (
			value = IKeyword.FIELD,
			can_be_const = false,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc ("Allows to build a field by specifying, in order, its number of columns, number of rows, the initial value of its cells and the value representing the absence of value") })
	@no_test
	public static IField createFieldWithSizeValueAndNoData(final IScope scope, final int cols, final int rows,
			final double init, final double no) {
		double[] data = new double[cols * rows];
		Arrays.fill(data, init);
		return GamaMatrixFactory.createField(scope, cols, rows, data, no);
	}

	/**
	 * Builds the field.
	 *
	 * @param scope
	 *            the scope
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param init
	 *            the init
	 * @return the i field
	 */
	@operator (
			value = IKeyword.FIELD,
			can_be_const = false,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc ("Allows to build a field by specifying, in order, its number of columns, "
					+ "number of rows and the initial value of its cells. The value representing the absence of value is set to #max_float") })
	@no_test
	public static IField createFieldWithSizeAndValue(final IScope scope, final int cols, final int rows,
			final double init) {
		return createFieldWithSizeValueAndNoData(scope, cols, rows, init, IField.NO_NO_DATA);
	}

	/**
	 * Builds the field with.
	 *
	 * @param scope
	 *            the scope
	 * @param size
	 *            the size
	 * @param init
	 *            the init
	 * @return the i matrix
	 */
	@operator (
			value = "field_with",
			content_type = IType.FLOAT,
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "creates a field with a size provided by the first operand, and filled by the evaluation of the second operand for each cell",
			comment = "Note that both components of the right operand point should be positive, otherwise an exception is raised.",
			see = { IKeyword.MATRIX, "as_matrix" })
	@no_test
	public static IField createFieldWithSizeAndExpression(final IScope scope, final IPoint size,
			final IExpression init) {
		if (size == null) throw GamaRuntimeException.error("A nil size is not allowed for matrices", scope);
		IField field = createFieldWithSizeAndValue(scope, (int) size.getX(), (int) size.getY(), 0d);
		double[] matrix = field.getMatrix();
		for (int i = 0; i < matrix.length; i++) { matrix[i] = Cast.asFloat(scope, init.value(scope)); }
		return field;
	}

	/**
	 * Builds the field.
	 *
	 * @param scope
	 *            the scope
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @return the i field
	 */
	@operator (
			value = IKeyword.FIELD,
			can_be_const = false,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc ("Allows to build a field by specifying, in order, its number of columns and number of rows. "
					+ "The initial value of its cells is set to 0.0 and the value representing the absence of value is set to #max_float") })
	@no_test
	public static IField createFieldWithSize(final IScope scope, final int cols, final int rows) {
		return createFieldWithSizeAndValue(scope, cols, rows, 0d);
	}

}