/*******************************************************************************************************
 *
 * GamaObjectMatrix.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util.matrix;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableList;

import gama.core.common.util.RandomUtils;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.topology.grid.GamaSpatialMatrix;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.operators.Cast;
import gama.gaml.types.GamaMatrixType;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import one.util.streamex.StreamEx;

/**
 * The Class GamaObjectMatrix.
 */
public class GamaObjectMatrix extends GamaMatrix<Object> {

	/**
	 * From.
	 *
	 * @param c
	 *            the c
	 * @param r
	 *            the r
	 * @param m
	 *            the m
	 * @return the gama object matrix
	 */
	static public GamaObjectMatrix from(final int c, final int r, final IMatrix<?> m) {
		if (m instanceof GamaFloatMatrix) return new GamaObjectMatrix(c, r, ((GamaFloatMatrix) m).getMatrix());
		if (m instanceof GamaObjectMatrix)
			return new GamaObjectMatrix(c, r, ((GamaObjectMatrix) m).getMatrix(), m.getGamlType().getContentType());
		if (m instanceof GamaIntMatrix) return new GamaObjectMatrix(c, r, ((GamaIntMatrix) m).matrix);
		if (m instanceof GamaSpatialMatrix)
			return new GamaObjectMatrix(c, r, ((GamaSpatialMatrix) m).getMatrix(), m.getGamlType().getContentType());
		return null;
	}

	/** The matrix. */
	protected Object[] matrix;

	/**
	 * Instantiates a new gama object matrix.
	 *
	 * @param p
	 *            the p
	 * @param contentsType
	 *            the contents type
	 */
	public GamaObjectMatrix(final GamaPoint p, final IType<?> contentsType) {
		this((int) p.getX(), (int) p.getY(), contentsType);
	}

	/**
	 * Instantiates a new gama object matrix.
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param contentsType
	 *            the contents type
	 */
	public GamaObjectMatrix(final int cols, final int rows, final IType<?> contentsType) {
		super(cols, rows, contentsType);
		setMatrix(new Object[cols * rows]);
	}

	/**
	 * Instantiates a new gama object matrix.
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param objects
	 *            the objects
	 */
	public GamaObjectMatrix(final int cols, final int rows, final double[] objects) {
		this(cols, rows, Types.FLOAT);
		final int n = Math.min(objects.length, rows * cols);
		for (int i = 0; i < n; i++) { matrix[i] = objects[i]; }
	}

	/**
	 * Instantiates a new gama object matrix.
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param objects
	 *            the objects
	 */
	public GamaObjectMatrix(final int cols, final int rows, final int[] objects) {
		this(cols, rows, Types.INT);
		final int n = Math.min(objects.length, rows * cols);
		for (int i = 0; i < n; i++) { matrix[i] = objects[i]; }
	}

	/**
	 * Instantiates a new gama object matrix.
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param objects
	 *            the objects
	 * @param contentsType
	 *            the contents type
	 */
	public GamaObjectMatrix(final int cols, final int rows, final Object[] objects, final IType<?> contentsType) {
		this(cols, rows, contentsType);
		java.lang.System.arraycopy(objects, 0, getMatrix(), 0, Math.min(objects.length, rows * cols));
	}

	/**
	 * Instantiates a new gama object matrix.
	 *
	 * @param scope
	 *            the scope
	 * @param objects
	 *            the objects
	 * @param preferredSize
	 *            the preferred size
	 * @param contentsType
	 *            the contents type
	 */
	public GamaObjectMatrix(final IScope scope, final IList<?> objects, final GamaPoint preferredSize,
			final IType<?> contentsType) {
		super(scope, objects, preferredSize, contentsType);
		setMatrix(new Object[numRows * numCols]);
		final boolean requiresCasting = GamaType.requiresCasting(contentsType, objects.getGamlType().getContentType());
		if (preferredSize != null) {
			for (int i = 0, stop = Math.min(getMatrix().length, objects.size()); i < stop; i++) {
				getMatrix()[i] =
						requiresCasting ? contentsType.cast(scope, objects.get(i), null, false) : objects.get(i);
			}
		} else if (isFlat(objects)) {
			for (int i = 0, stop = objects.size(); i < stop; i++) {
				getMatrix()[i] = contentsType.cast(scope, objects.get(i), null, false);
			}
		} else {
			for (int i = 0; i < numRows; i++) {
				for (int j = 0; j < numCols; j++) { set(scope, j, i, ((List<?>) objects.get(j)).get(i)); }
			}
		}
	}

	@Override
	public void _clear() {
		Arrays.fill(getMatrix(), null);
	}

	@Override
	public boolean _contains(final IScope scope, final Object o) {
		for (int i = 0; i < getMatrix().length; i++) { if (getMatrix()[i].equals(o)) return true; }
		return false;
	}

	@Override
	public Object _first(final IScope scope) {
		if (getMatrix().length == 0) return null;
		return getMatrix()[0];
	}

	@Override
	public Object _last(final IScope scope) {
		if (getMatrix().length == 0) return null;
		return getMatrix()[getMatrix().length - 1];
	}

	@Override
	public Integer _length(final IScope scope) {
		return getMatrix().length;
	}

	/**
	 * Take two matrices (with the same number of columns) and create a big matrix putting the second matrix on the
	 * right side of the first matrix
	 *
	 * @param two
	 *            matrix to concatenate
	 * @return the matrix concatenated
	 */

	public IMatrix<?> _opAppendVertically(final IScope scope, final IMatrix<?> b) {
		final Object[] mab = ArrayUtils.addAll(getMatrix(), ((GamaObjectMatrix) b).getMatrix());
		final IType<?> newContentsType =
				GamaType.findCommonType(getGamlType().getContentType(), b.getGamlType().getContentType());
		return new GamaObjectMatrix(numCols, numRows + b.getRows(scope), mab, newContentsType);
	}

	/**
	 * Take two matrices (with the same number of rows) and create a big matrix putting the second matrix on the right
	 * side of the first matrix
	 *
	 * @param two
	 *            matrix to concatenate
	 * @return the matrix concatenated
	 */

	public GamaObjectMatrix _opAppendHorizontally(final IScope scope, final IMatrix<?> b) {
		final GamaObjectMatrix aprime = _reverse(scope);
		final GamaObjectMatrix bprime = GamaObjectMatrix.from(b.getCols(scope), b.getRows(scope), b)._reverse(scope);
		final GamaObjectMatrix c = (GamaObjectMatrix) aprime._opAppendVertically(scope, bprime);
		return c._reverse(scope);
	}

	@Override
	public boolean _isEmpty(final IScope scope) {
		for (int i = 0; i < getMatrix().length; i++) { if (getMatrix()[i] != null) return false; }
		return true;
	}

	@Override
	protected IList<Object> _listValue(final IScope scope, final IType contentsType, final boolean cast) {
		return cast ? GamaListFactory.create(scope, contentsType, getMatrix())
				: GamaListFactory.<Object> wrap(contentsType, getMatrix());
	}

	@Override
	protected IMatrix<Object> _matrixValue(final IScope scope, final GamaPoint preferredSize, final IType type,
			final boolean copy) {
		return GamaMatrixType.from(scope, this, type, preferredSize, copy);
	}

	@Override
	public GamaObjectMatrix _reverse(final IScope scope) throws GamaRuntimeException {
		final GamaObjectMatrix result = new GamaObjectMatrix(numRows, numCols, getGamlType().getContentType());
		for (int i = 0; i < numCols; i++) {
			for (int j = 0; j < numRows; j++) { result.set(scope, j, i, get(scope, i, j)); }
		}
		return result;
	}

	@Override
	public GamaObjectMatrix copy(final IScope scope, final GamaPoint size, final boolean copy) {
		if (size == null) {
			if (copy) return new GamaObjectMatrix(numCols, numRows, Arrays.copyOf(matrix, matrix.length),
					getGamlType().getContentType());
			return this;
		}
		return new GamaObjectMatrix((int) size.getX(), (int) size.getY(), Arrays.copyOf(matrix, matrix.length),
				getGamlType().getContentType());
	}

	@Override
	public boolean equals(final Object m) {
		if (this == m) return true;
		if (!(m instanceof GamaObjectMatrix mat)) return false;
		return Arrays.equals(this.getMatrix(), mat.getMatrix());
	}

	// Removed to improve performances
	// @Override
	// public int hashCode() {
	// return Arrays.hashCode(getMatrix());
	// }

	@Override
	public void _putAll(final IScope scope, final Object o) {
		fillWith(scope, getGamlType().getContentType().cast(scope, o, null, false));
	}

	/**
	 * Fill with.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 */
	public void fillWith(final IScope scope, final Object o) {
		Arrays.fill(getMatrix(), o);
	}

	@Override
	public Object get(final IScope scope, final int col, final int row) {
		if (col >= numCols || col < 0 || row >= numRows || row < 0) return null;
		return getMatrix()[row * numCols + col];
	}

	@Override
	public void set(final IScope scope, final int col, final int row, final Object obj) {
		if (col >= numCols || col < 0 || row >= numRows || row < 0) return;
		getMatrix()[row * numCols + col] = GamaType.toType(scope, obj, getGamlType().getContentType(), false);
	}

	@Override
	public Object remove(final IScope scope, final int col, final int row) {
		if (col >= numCols || col < 0 || row >= numRows || row < 0) return null;
		final Object o = getMatrix()[row * numCols + col];
		getMatrix()[row * numCols + col] = null;
		return o;
	}

	@Override
	public boolean _removeFirst(final IScope scope, final Object o) {
		for (int i = 0; i < getMatrix().length; i++) {
			if (getMatrix()[i].equals(o)) {
				getMatrix()[i] = null;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean _removeAll(final IScope scope, final IContainer<?, Object> list) throws GamaRuntimeException {
		boolean removed = false;
		for (int i = 0; i < getMatrix().length; i++) {
			if (list.contains(scope, getMatrix()[i])) { // VERIFY NULL SCOPE
				getMatrix()[i] = null;
				removed = true;
			}
		}
		// TODO Make a test to verify the return
		return removed;
	}

	@Override
	public void shuffleWith(final RandomUtils randomAgent) {
		randomAgent.shuffleInPlace(getMatrix());
	}

	/**
	 * Method iterator()
	 *
	 * @see gama.core.util.matrix.GamaMatrix#iterator()
	 */
	@Override
	public java.lang.Iterable<Object> iterable(final IScope scope) {
		return ImmutableList.copyOf(getMatrix());
	}

	/**
	 * Gets the matrix.
	 *
	 * @return the matrix
	 */
	public Object[] getMatrix() { return matrix; }

	/**
	 * Sets the matrix.
	 *
	 * @param matrix
	 *            the new matrix
	 */
	protected void setMatrix(final Object[] matrix) { this.matrix = matrix; }

	@Override
	protected void setNthElement(final IScope scope, final int index, final Object value) {
		getMatrix()[index] = value;
	}

	/**
	 * Method getNthElement()
	 *
	 * @see gama.core.util.matrix.GamaMatrix#getNthElement(java.lang.Integer)
	 */
	@Override
	public Object getNthElement(final Integer index) {
		if (index == null || index > getMatrix().length) return null;
		return getMatrix()[index];
	}

	@Override
	public StreamEx<Object> stream(final IScope scope) {
		return StreamEx.of(matrix);
	}

	@Override
	public double[] getFieldData(final IScope scope) {
		double[] result = new double[matrix.length];
		for (int i = 0; i < matrix.length; ++i) { result[i] = Cast.asFloat(scope, matrix[i]); }
		return result;
	}

}
