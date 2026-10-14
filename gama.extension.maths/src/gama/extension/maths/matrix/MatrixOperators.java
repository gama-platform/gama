/*******************************************************************************************************
 *
 * MatrixOperators.java, in gama.extension.maths, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.maths.matrix;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.tests;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.core.util.matrix.GamaIntMatrix;

/**
 * The Class MatrixOperators.
 */
public class MatrixOperators {

	/**
	 * Matrix multiplication.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = ".",
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			usages = @usage (
					value = "if both operands are matrix, returns the dot product of them",
					examples = @example (
							value = "matrix([[1,1],[1,2]]) . matrix([[1,1],[1,2]])",
							equals = "matrix([[2,3],[3,5]])")))
	@test ("matrix([[1,1],[1,2]]) . matrix([[1,1],[1,2]]) = matrix([[2,3],[3,5]])")
	public static IMatrix matrixMultiplication(final IScope scope, final IMatrix a, final IMatrix b)
			throws GamaRuntimeException {
		try {
			if (a instanceof GamaIntMatrix && b instanceof GamaIntMatrix)
				return toGamaIntMatrix(getRealMatrix(a).multiply(getRealMatrix(b)));
			return toGamaFloatMatrix(getRealMatrix(a).multiply(getRealMatrix(b)));
		} catch (final DimensionMismatchException e) {
			throw GamaRuntimeException.error(" The dimensions of the matrices do not correspond", scope);
		}
	}

	/**
	 * Gets the determinant.
	 *
	 * @param scope
	 *            the scope
	 * @param m
	 *            the m
	 * @return the determinant
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "determinant", "det" },
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "The determinant of the given matrix",
			masterDoc = true,
			examples = { @example (
					value = "determinant(matrix([[1,2],[3,4]]))",
					equals = "-2") })
	@tests ({
			@test ("matrix<float> diagonal <- matrix([[2.0, 0.0], [0.0, 3.0]]); det(diagonal) = 6.0"),
			@test ("matrix<float> full <- matrix([[1.0, 2.0], [3.0, 4.0]]); det(full) = -2.0"),
			@test ("matrix<float> full2 <- matrix([[1.0, 2.0], [3.0, 4.0]]); determinant(full2) = det(full2)"),
			// The determinant of [[1, 2], [3, 4]] is (1*4 - 2*3) = -2
			@test ("matrix<float> mat <- matrix([[1.0, 2.0], [3.0, 4.0]]); determinant(mat) = -2.0"),
			@test ("matrix<float> m1 <- matrix([[1.0, 2.0], [3.0, 4.0]]); (determinant(m1) with_precision 1) = -2.0")
	})
	public static Double getDeterminant(final IScope scope, final IMatrix m) throws GamaRuntimeException {
		return new LUDecomposition(getRealMatrix(m)).getDeterminant();
	}

	/**
	 * Gets the trace.
	 *
	 * @param scope
	 *            the scope
	 * @param m
	 *            the m
	 * @return the trace
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "trace",
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "The trace of the given matrix (the sum of the elements on the main diagonal).",
			masterDoc = true,
			examples = { @example (
					value = "trace(matrix([[1,2],[3,4]]))",
					equals = "5") })
	@tests ({
			@test ("matrix<float> full <- matrix([[1.0, 2.0], [3.0, 4.0]]); trace(full) = 5.0"),
			@test ("matrix<float> diagonal <- matrix([[2.0, 0.0], [0.0, 3.0]]); trace(diagonal) = 5.0"),
			@test ("matrix<float> full2 <- matrix([[1.0, 2.0], [3.0, 4.0]]); list<float> full_eigenvalues <- eigenvalues(full2); sum(full_eigenvalues) with_precision 9 = trace(full2)"),
			// Trace is the sum of elements on the main diagonal (1.0 + 4.0 = 5.0)
			@test ("matrix<float> mat <- matrix([[1.0, 2.0], [3.0, 4.0]]); trace(mat) = 5.0")
	})
	public static Double getTrace(final IScope scope, final IMatrix m) throws GamaRuntimeException {
		return getRealMatrix(m).getTrace();
	}

	/**
	 * Gets the eigen.
	 *
	 * @param scope
	 *            the scope
	 * @param m
	 *            the m
	 * @return the eigen
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "eigenvalues",
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "The list of the eigen values of the given matrix",
			masterDoc = true,
			examples = { @example (
					value = "eigenvalues(matrix([[5,-3],[6,-4]]))",
					equals = "[2.0000000000000004,-0.9999999999999998]") })
	@tests ({
			@test ("matrix<float> diagonal <- matrix([[2.0, 0.0], [0.0, 3.0]]); list<float> diagonal_eigenvalues <- eigenvalues(diagonal); length(diagonal_eigenvalues) = 2"),
			@test ("matrix<float> diagonal2 <- matrix([[2.0, 0.0], [0.0, 3.0]]); list<float> diagonal_eigenvalues2 <- eigenvalues(diagonal2); diagonal_eigenvalues2 contains_all [2.0, 3.0]"),
			@test ("matrix<float> full <- matrix([[1.0, 2.0], [3.0, 4.0]]); list<float> full_eigenvalues <- eigenvalues(full); (full_eigenvalues[0] * full_eigenvalues[1]) with_precision 9 = det(full)")
	})
	public static IList<Double> getEigen(final IScope scope, final IMatrix m) throws GamaRuntimeException {
		return fromApacheMatrixtoDiagList(scope, new EigenDecomposition(getRealMatrix(m)).getD());
	}

	/**
	 * Transpose.
	 *
	 * @param scope
	 *            the scope
	 * @param m
	 *            the m
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "transpose",
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "The transposition of the given matrix",
			masterDoc = true,
			examples = { @example (
					value = "transpose(matrix([[5,-3],[6,-4]]))",
					equals = "matrix([[5,6],[-3,-4]])") })
	@tests ({
			@test ("matrix<float> full <- matrix([[1.0, 2.0], [3.0, 4.0]]); transpose(transpose(full)) = full"),
			@test ("matrix<float> m1 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> m2 <- transpose(m1); m2[0, 1] = m1[1, 0]"),
			@test ("matrix<float> m12 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> m22 <- transpose(m12); m22[1, 0] = m12[0, 1]"),
			@test ("matrix<float> m13 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> m23 <- transpose(m13); m23[0, 1] = 3.0"),
			@test ("matrix<float> m14 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> m24 <- transpose(m14); m24[1, 0] = 2.0")
	})
	public static IMatrix transpose(final IScope scope, final IMatrix m) throws GamaRuntimeException {
		return m.reverse(scope);
	}

	/**
	 * Inverse.
	 *
	 * @param scope
	 *            the scope
	 * @param m
	 *            the m
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "inverse",
			can_be_const = true,
			content_type = IType.FLOAT,
			// ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "The inverse matrix of the given matrix. If no inverse exists, returns a matrix that has properties that resemble that of an inverse.",
			masterDoc = true,
			examples = { @example (
					value = "inverse(matrix([[4,3],[3,2]]))",
					equals = "matrix([[-2.0,3.0],[3.0,-4.0]])") })
	@tests ({
			@test ("matrix<float> diagonal <- matrix([[2.0, 0.0], [0.0, 3.0]]); matrix<float> inverted <- inverse(diagonal); inverted[0, 0] = 0.5"),
			@test ("matrix<float> diagonal2 <- matrix([[2.0, 0.0], [0.0, 3.0]]); matrix<float> inverted2 <- inverse(diagonal2); inverted2[1, 1] with_precision 6 = 0.333333"),
			@test ("matrix<float> diagonal3 <- matrix([[2.0, 0.0], [0.0, 3.0]]); matrix<float> inverted3 <- inverse(diagonal3); inverted3[1, 0] = 0.0"),
			@test ("matrix<float> mat <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> inv_mat <- inverse(mat); inv_mat != nil"),
			// 1.0
			@test ("matrix<float> mat2 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> inv_mat2 <- inverse(mat2); matrix<float> id_mat <- mat2 . inv_mat2; (id_mat[0, 0] > 0.99 and id_mat[0, 0] < 1.01)"),
			@test ("matrix<float> mat3 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> inv_mat3 <- inverse(mat3); matrix<float> id_mat2 <- mat3 . inv_mat3; (id_mat2[1, 0] > -0.01 and id_mat2[1, 0] < 0.01)")
	})
	public static IMatrix<Double> inverse(final IScope scope, final IMatrix m) throws GamaRuntimeException {
		return toGamaFloatMatrix(new LUDecomposition(getRealMatrix(m)).getSolver().getInverse());
	}

	/**
	 * Op append vertically.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i matrix
	 */
	@operator (
			value = IKeyword.APPEND_VERTICALLY,
			content_type = ITypeProvider.BOTH,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "A matrix resulting from the concatenation of the columns  of the two given matrices. ",
			masterDoc = false,
			examples = { @example (
					value = "matrix([[1,2],[3,4]]) append_vertically matrix([[1,2],[3,4]])",
					equals = "matrix([[1,2,1,2],[3,4,3,4]])") })
	@tests ({
			@test ("matrix<float> first_matrix <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> second_matrix <- matrix([[5.0, 6.0], [7.0, 8.0]]); matrix<float> stacked <- append_vertically(first_matrix, second_matrix); stacked.columns = 2"),
			@test ("matrix<float> first_matrix2 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> second_matrix2 <- matrix([[5.0, 6.0], [7.0, 8.0]]); matrix<float> stacked2 <- append_vertically(first_matrix2, second_matrix2); stacked2.rows = 4"),
			@test ("matrix<float> first_matrix3 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> second_matrix3 <- matrix([[5.0, 6.0], [7.0, 8.0]]); matrix<float> stacked3 <- append_vertically(first_matrix3, second_matrix3); stacked3 column_at 0 = [1.0, 2.0, 5.0, 6.0]"),
			@test ("matrix<float> first_matrix4 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> second_matrix4 <- matrix([[5.0, 6.0], [7.0, 8.0]]); matrix<float> stacked4 <- append_vertically(first_matrix4, second_matrix4); stacked4 column_at 1 = [3.0, 4.0, 7.0, 8.0]"),
			// matrices of different kinds (int and float) are appended too, the result holding both contents
			@test ("matrix<int> whole1 <- matrix([[1, 2], [3, 4]]); matrix<float> decimal1 <- matrix([[5.5, 6.5], [7.5, 8.5]]); append_vertically(whole1, decimal1).rows = 4"),
			@test ("matrix<int> whole2 <- matrix([[1, 2], [3, 4]]); matrix<float> decimal2 <- matrix([[5.5, 6.5], [7.5, 8.5]]); append_vertically(whole2, decimal2).columns = 2"),
			@test ("matrix<int> whole3 <- matrix([[1, 2], [3, 4]]); matrix<float> decimal3 <- matrix([[5.5, 6.5], [7.5, 8.5]]); append_vertically(whole3, decimal3)[0, 2] = 5.5"),
			@test ("matrix<int> whole4 <- matrix([[1, 2], [3, 4]]); matrix<float> decimal4 <- matrix([[5.5, 6.5], [7.5, 8.5]]); append_vertically(decimal4, whole4)[1, 3] = 4.0"),
			@test ("matrix<string> words <- matrix([[\"a\", \"b\"], [\"c\", \"d\"]]); matrix<int> whole5 <- matrix([[1, 2], [3, 4]]); append_vertically(words, whole5).rows = 4"),
			// the two matrices must have the same number of columns
			@test ("is_error(append_vertically(matrix([[1, 2], [3, 4]]), matrix([[1, 2, 3]])))")
	})
	public static IMatrix opAppendVertically(final IScope scope, final IMatrix a, final IMatrix b) {
		return a._opAppendVertically(scope, b);
	}

	/**
	 * Take two matrices (with the same number of rows) and create a big matrix putting the second matrix on the right
	 * side of the first matrix
	 *
	 * @param two
	 *            matrix to concatenate
	 * @return the matrix concatenated
	 */

	@operator (
			value = IKeyword.APPEND_HORIZONTALLY,
			content_type = ITypeProvider.BOTH,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX })
	@doc (
			value = "A matrix resulting from the concatenation of the rows of the two given matrices.",
			masterDoc = false)
	@no_test
	@tests ({
			@test ("matrix<float> first_matrix <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> second_matrix <- matrix([[5.0, 6.0], [7.0, 8.0]]); matrix<float> side_by_side <- append_horizontally(first_matrix, second_matrix); side_by_side.columns = 4"),
			@test ("matrix<float> first_matrix2 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> second_matrix2 <- matrix([[5.0, 6.0], [7.0, 8.0]]); matrix<float> side_by_side2 <- append_horizontally(first_matrix2, second_matrix2); side_by_side2.rows = 2"),
			@test ("matrix<float> first_matrix3 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> second_matrix3 <- matrix([[5.0, 6.0], [7.0, 8.0]]); matrix<float> side_by_side3 <- append_horizontally(first_matrix3, second_matrix3); side_by_side3 column_at 2 = [5.0, 6.0]"),
			@test ("matrix<float> first_matrix4 <- matrix([[1.0, 2.0], [3.0, 4.0]]); matrix<float> second_matrix4 <- matrix([[5.0, 6.0], [7.0, 8.0]]); matrix<float> side_by_side4 <- append_horizontally(first_matrix4, second_matrix4); side_by_side4 row_at 0 = [1.0, 3.0, 5.0, 7.0]"),
			// matrices of different kinds (int and float) are appended too, the result holding both contents
			@test ("matrix<int> whole1 <- matrix([[1, 2], [3, 4]]); matrix<float> decimal1 <- matrix([[5.5, 6.5], [7.5, 8.5]]); append_horizontally(whole1, decimal1).columns = 4"),
			@test ("matrix<int> whole2 <- matrix([[1, 2], [3, 4]]); matrix<float> decimal2 <- matrix([[5.5, 6.5], [7.5, 8.5]]); append_horizontally(whole2, decimal2).rows = 2"),
			@test ("matrix<int> whole3 <- matrix([[1, 2], [3, 4]]); matrix<float> decimal3 <- matrix([[5.5, 6.5], [7.5, 8.5]]); append_horizontally(whole3, decimal3)[2, 0] = 5.5"),
			@test ("matrix<int> whole4 <- matrix([[1, 2], [3, 4]]); matrix<float> decimal4 <- matrix([[5.5, 6.5], [7.5, 8.5]]); append_horizontally(decimal4, whole4)[3, 1] = 4.0"),
			@test ("matrix<string> words <- matrix([[\"a\", \"b\"], [\"c\", \"d\"]]); matrix<int> whole5 <- matrix([[1, 2], [3, 4]]); append_horizontally(words, whole5).columns = 4"),
			// the two matrices must have the same number of rows
			@test ("is_error(append_horizontally(matrix([[1, 2], [3, 4]]), matrix([[1, 2, 3]])))")
	})
	public static IMatrix opAppendHorizontally(final IScope scope, final IMatrix a, final IMatrix b) {
		return a._opAppendHorizontally(scope, b);
	}

	/**
	 * Gets the real matrix.
	 *
	 * @param m
	 *            the m
	 * @return the real matrix
	 */
	public static RealMatrix getRealMatrix(final IMatrix m) {
		var rows = m.getRows(null);
		var cols = m.getCols(null);
		final RealMatrix realMatrix = new Array2DRowRealMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) { realMatrix.setEntry(i, j, Cast.asFloat(null, m.get(null, j, i))); }
		}
		return realMatrix;
	}

	/**
	 * Update matrix.
	 *
	 * @param m
	 *            the m
	 * @param realMatrix
	 *            the real matrix
	 */
	public static void updateMatrix(final IMatrix m, final RealMatrix realMatrix) {
		var rows = m.getRows(null);
		var cols = m.getCols(null);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) { m.set(null, j, i, realMatrix.getEntry(i, j)); }
		}
	}

	/**
	 * To gama int matrix.
	 *
	 * @param m
	 *            the m
	 * @return the gama int matrix
	 */
	public static GamaIntMatrix toGamaIntMatrix(final RealMatrix m) {
		GamaIntMatrix result =
				(GamaIntMatrix) GamaMatrixFactory.createIntMatrix(m.getColumnDimension(), m.getRowDimension());
		updateMatrix(result, m);
		return result;
	}

	/**
	 * To gama float matrix.
	 *
	 * @param m
	 *            the m
	 * @return the gama float matrix
	 */
	public static IMatrix toGamaFloatMatrix(final RealMatrix m) {
		IMatrix result = GamaMatrixFactory.createFloatMatrix(m.getColumnDimension(), m.getRowDimension());
		updateMatrix(result, m);
		return result;
	}

	/**
	 * From apache matrixto diag list.
	 *
	 * @param scope
	 *            the scope
	 * @param rm
	 *            the rm
	 * @return the i list
	 */
	public static IList<Double> fromApacheMatrixtoDiagList(final IScope scope, final RealMatrix rm) {
		final IList<Double> vals = GamaListFactory.create(Types.FLOAT);
		for (int i = 0; i < rm.getColumnDimension(); i++) { vals.add(rm.getEntry(i, i)); }
		return vals;
	}

}
