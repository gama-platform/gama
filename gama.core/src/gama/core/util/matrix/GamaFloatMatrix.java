/*******************************************************************************************************
 *
 * GamaFloatMatrix.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util.matrix;

import static org.locationtech.jts.index.quadtree.IntervalSize.isZeroWidth;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.jts.index.quadtree.IntervalSize;

import com.google.common.primitives.Doubles;

import gama.core.common.interfaces.IImageProvider;
import gama.core.common.util.RandomUtils;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.operators.Cast;
import gama.gaml.types.GamaMatrixType;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import one.util.streamex.DoubleStreamEx;
import one.util.streamex.StreamEx;

/**
 * The Class GamaFloatMatrix.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaFloatMatrix extends GamaMatrix<Double> implements IImageProvider {

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param m
	 *            the m
	 * @return the gama float matrix
	 */
	static public GamaFloatMatrix from(final IScope scope, final IMatrix m) {
		// We explicitly convert it to a matrix (and not a field)
		if (m instanceof GamaField)
			return new GamaFloatMatrix(m.getCols(scope), m.getRows(scope), ((GamaField) m).matrix);
		if (m instanceof GamaFloatMatrix) return (GamaFloatMatrix) m;
		if (m instanceof GamaObjectMatrix)
			return new GamaFloatMatrix(scope, m.getCols(scope), m.getRows(scope), ((GamaObjectMatrix) m).getMatrix());
		if (m instanceof GamaIntMatrix)
			return new GamaFloatMatrix(m.getCols(scope), m.getRows(scope), ((GamaIntMatrix) m).matrix);
		return null;
	}

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param r
	 *            the r
	 * @param m
	 *            the m
	 * @return the gama float matrix
	 */
	static public GamaFloatMatrix from(final IScope scope, final int c, final int r, final IMatrix m) {
		if (m instanceof GamaFloatMatrix) return new GamaFloatMatrix(c, r, ((GamaFloatMatrix) m).getMatrix());
		if (m instanceof GamaObjectMatrix) return new GamaFloatMatrix(scope, c, r, ((GamaObjectMatrix) m).getMatrix());
		if (m instanceof GamaIntMatrix) return new GamaFloatMatrix(c, r, ((GamaIntMatrix) m).matrix);
		return null;
	}

	/** The matrix. */
	protected double[] matrix;

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param mat
	 *            the mat
	 */
	public GamaFloatMatrix(final double[] mat) {
		super(1, mat.length, Types.FLOAT);
		setMatrix(mat);
	}

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param p
	 *            the p
	 */
	public GamaFloatMatrix(final GamaPoint p) {
		this((int) p.x, (int) p.y);
	}

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 */
	public GamaFloatMatrix(final int cols, final int rows) {
		super(cols, rows, Types.FLOAT);
		setMatrix(new double[cols * rows]);
	}

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param objects
	 *            the objects
	 */
	public GamaFloatMatrix(final int cols, final int rows, final double[] objects) {
		this(cols, rows);

		java.lang.System.arraycopy(objects, 0, getMatrix(), 0, Math.min(objects.length, rows * cols));
	}

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param objects
	 *            the objects
	 */
	public GamaFloatMatrix(final int cols, final int rows, final int[] objects) {
		this(cols, rows);
		for (int i = 0, n = Math.min(objects.length, rows * cols); i < n; i++) { matrix[i] = objects[i]; }
	}

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param scope
	 *            the scope
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param objects
	 *            the objects
	 */
	public GamaFloatMatrix(final IScope scope, final int cols, final int rows, final Object[] objects) {
		this(cols, rows);
		for (int i = 0, n = Math.min(objects.length, rows * cols); i < n; i++) {
			matrix[i] = Cast.asFloat(scope, objects[i]);
		}
	}

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param scope
	 *            the scope
	 * @param objects
	 *            the objects
	 * @param preferredSize
	 *            the preferred size
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GamaFloatMatrix(final IScope scope, final List objects, final GamaPoint preferredSize)
			throws GamaRuntimeException {
		super(scope, objects, preferredSize, Types.FLOAT);
		setMatrix(new double[numRows * numCols]);
		if (preferredSize != null) {
			for (int i = 0, stop = Math.min(getMatrix().length, objects.size()); i < stop; i++) {
				getMatrix()[i] = Cast.asFloat(scope, objects.get(i));
			}
		} else if (GamaMatrix.isFlat(objects)) {
			for (int i = 0, stop = objects.size(); i < stop; i++) {
				getMatrix()[i] = Cast.asFloat(scope, objects.get(i));
			}
		} else {
			for (int i = 0; i < numRows; i++) {
				for (int j = 0; j < numCols; j++) {
					set(scope, j, i, Cast.asFloat(scope, ((List) objects.get(j)).get(i)));
				}
			}
		}
	}

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param scope
	 *            the scope
	 * @param mat
	 *            the mat
	 */
	public GamaFloatMatrix(final IScope scope, final Object[] mat) {
		this(1, mat.length);
		for (int i = 0; i < mat.length; i++) { getMatrix()[i] = Cast.asFloat(scope, mat[i]); }
	}

	@Override
	protected IList _listValue(final IScope scope, final IType contentsType, final boolean cast) {
		return cast ? GamaListFactory.create(scope, contentsType, matrix)
				: GamaListFactory.createWithoutCasting(contentsType, matrix);
	}

	@Override
	protected void _clear() {
		Arrays.fill(getMatrix(), 0d);
	}

	@Override
	public boolean _contains(final IScope scope, final Object o) {
		if (o instanceof Double d) {
			for (int i = 0; i < getMatrix().length; i++) {
				if (IntervalSize.isZeroWidth(getMatrix()[i], d)) return true;
			}
		}
		return false;
	}

	@Override
	public Double _first(final IScope scope) {
		if (getMatrix().length == 0) return 0d;
		return getMatrix()[0];
	}

	@Override
	public Double _last(final IScope scope) {
		if (getMatrix().length == 0) return 0d;
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
	public GamaFloatMatrix _opAppendVertically(final IScope scope, final GamaFloatMatrix b) {
		final double[] mab = ArrayUtils.addAll(getMatrix(), b.getMatrix());
		return new GamaFloatMatrix(numCols, numRows + b.getRows(scope), mab);
	}

	/**
	 * Take two matrices (with the same number of rows) and create a big matrix putting the second matrix on the right
	 * side of the first matrix
	 *
	 * @param two
	 *            matrix to concatenate
	 * @return the matrix concatenated
	 */
	public IMatrix _opAppendHorizontally(final IScope scope, final GamaFloatMatrix b) {
		final GamaFloatMatrix aprime = _reverse(scope);
		final GamaFloatMatrix bprime = b._reverse(scope);
		final GamaFloatMatrix c = aprime._opAppendVertically(scope, bprime);
		return c._reverse(scope);
	}

	@Override
	public boolean _isEmpty(final IScope scope) {
		for (int i = 0; i < getMatrix().length; i++) { if (getMatrix()[i] != 0d) return false; }
		return true;
	}

	@Override
	protected IMatrix _matrixValue(final IScope scope, final GamaPoint preferredSize, final IType type,
			final boolean copy) {
		return GamaMatrixType.from(scope, this, type, preferredSize, copy);
	}

	@Override
	public GamaFloatMatrix _reverse(final IScope scope) throws GamaRuntimeException {
		final GamaFloatMatrix result = new GamaFloatMatrix(numRows, numCols);
		for (int i = 0; i < numCols; i++) {
			for (int j = 0; j < numRows; j++) {
				final double val = get(scope, i, j);
				result.set(scope, j, i, val);
			}
		}
		return result;
	}

	@Override
	public GamaFloatMatrix copy(final IScope scope, final GamaPoint size, final boolean copy) {
		if (size == null) {
			if (copy) return new GamaFloatMatrix(numCols, numRows, Arrays.copyOf(getMatrix(), matrix.length));
			return this;
		}
		return new GamaFloatMatrix((int) size.getX(), (int) size.getY(), Arrays.copyOf(getMatrix(), matrix.length));
	}

	@Override
	public boolean equals(final Object m) {
		if (this == m) return true;
		if (!(m instanceof GamaFloatMatrix mat)) return false;
		return Arrays.equals(this.getMatrix(), mat.getMatrix());
	}

	// Removed to improve performances
	// @Override
	// public int hashCode() {
	// return Arrays.hashCode(getMatrix());
	// }

	@Override
	public void _putAll(final IScope scope, final Object o) throws GamaRuntimeException {
		Arrays.fill(getMatrix(), Types.FLOAT.cast(scope, o, null, false));

	}

	@Override
	public Double get(final IScope scope, final int col, final int row) {
		if (col >= numCols || col < 0 || row >= numRows || row < 0) return 0d;
		return getMatrix()[row * numCols + col];
	}

	@Override
	public void set(final IScope scope, final int col, final int row, final Object obj) throws GamaRuntimeException {
		if (col < numCols && col >= 0 && row < numRows && row >= 0) {
			final double val = Cast.asFloat(scope, obj);
			getMatrix()[row * numCols + col] = val;
		}
	}

	/**
	 * Removes the.
	 *
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	private boolean remove(final double o) {
		for (int i = 0; i < getMatrix().length; i++) {
			if (isZeroWidth(getMatrix()[i], o)) {
				getMatrix()[i] = 0d;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean _removeFirst(final IScope scope, final Double o) throws GamaRuntimeException {
		// Exception if o == null
		return remove(o);
	}

	@Override
	public Double remove(final IScope scope, final int col, final int row) {
		if (col >= numCols || col < 0 || row >= numRows || row < 0) return 0d;
		final double o = getMatrix()[row * numCols + col];
		getMatrix()[row * numCols + col] = 0d;
		return o;
	}

	/**
	 * Removes the all.
	 *
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	private boolean removeAll(final double o) {
		boolean removed = false;
		for (int i = 0; i < getMatrix().length; i++) {
			if (isZeroWidth(getMatrix()[i], o)) {
				getMatrix()[i] = 0d;
				removed = true;
			}
		}
		return removed;
	}

	@Override
	public boolean _removeAll(final IScope scope, final IContainer<?, Double> list) {
		for (final Double o : list.iterable(scope)) { removeAll(o); }
		return true;
	}

	@Override
	public void shuffleWith(final RandomUtils randomAgent) {
		randomAgent.shuffleInPlace(getMatrix());
	}

	@Override
	public java.lang.Iterable<Double> iterable(final IScope scope) {
		return Doubles.asList(getMatrix());
	}

	/**
	 * Gets the matrix.
	 *
	 * @return the matrix
	 */
	public double[] getMatrix() { return matrix; }

	/**
	 * Sets the matrix.
	 *
	 * @param matrix
	 *            the new matrix
	 */
	void setMatrix(final double[] matrix) { this.matrix = matrix; }

	@Override
	public GamaFloatMatrix plus(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		final GamaFloatMatrix matb = from(scope, other);
		if (matb != null && this.numCols == matb.numCols && this.numRows == matb.numRows) {
			final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
			for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] + matb.matrix[i]; }
			return nm;
		}
		throw GamaRuntimeException.error(" The dimensions of the matrices do not correspond", scope);
	}

	@Override
	public GamaFloatMatrix times(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		final GamaFloatMatrix matb = from(scope, other);
		if (matb != null && this.numCols == matb.numCols && this.numRows == matb.numRows) {
			final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
			for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] * matb.matrix[i]; }
			return nm;
		}
		throw GamaRuntimeException.error(" The dimensions of the matrices do not correspond", scope);
	}

	@Override
	public GamaFloatMatrix minus(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		final GamaFloatMatrix matb = from(scope, other);
		if (matb != null && this.numCols == matb.numCols && this.numRows == matb.numRows) {
			final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
			for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] - matb.matrix[i]; }
			return nm;
		}
		throw GamaRuntimeException.error(" The dimensions of the matrices do not correspond", scope);
	}

	@Override
	public GamaFloatMatrix times(final Double val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] * val; }
		return nm;
	}

	@Override
	public GamaFloatMatrix times(final Integer val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] * val; }
		return nm;
	}

	@Override
	public GamaFloatMatrix divides(final Double val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] / val; }
		return nm;
	}

	@Override
	public GamaFloatMatrix divides(final Integer val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] / val; }
		return nm;
	}

	@Override
	public GamaFloatMatrix divides(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		final GamaFloatMatrix matb = from(scope, other);
		if (matb != null && this.numCols == matb.numCols && this.numRows == matb.numRows) {
			final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
			for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] / matb.matrix[i]; }
			return nm;
		}
		throw GamaRuntimeException.error(" The dimensions of the matrices do not correspond", scope);
	}

	@Override
	public GamaFloatMatrix plus(final Double val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] + val; }
		return nm;
	}

	@Override
	public GamaFloatMatrix plus(final Integer val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] + val; }
		return nm;
	}

	@Override
	public GamaFloatMatrix minus(final Double val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] - val; }
		return nm;
	}

	@Override
	public GamaFloatMatrix minus(final Integer val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] - val; }
		return nm;
	}

	@Override
	public Double getNthElement(final Integer index) {
		if (index == null || index > getMatrix().length) return 0d;
		return getMatrix()[index];
	}

	@Override
	protected void setNthElement(final IScope scope, final int index, final Object value) {
		getMatrix()[index] = Cast.asFloat(scope, value);
	}

	@Override
	public IContainerType getGamlType() { return Types.MATRIX.of(Types.FLOAT); }

	@Override
	public StreamEx<Double> stream(final IScope scope) {
		return DoubleStreamEx.of(matrix).boxed();
	}

	@Override
	public double[] getFieldData(final IScope scope) {
		return matrix;
	}

	/**
	 * Transforms a matrix of integers into the corresponding BufferedImage. The matrix has to follow the ARGB encoding
	 *
	 * @param scope
	 * @param matrix
	 * @return
	 */
	public static BufferedImage constructBufferedImageFromMatrix(final IScope scope, final IMatrix<Integer> matrix) {
		if (!(matrix instanceof GamaIntMatrix gim)) return null;
		return gim.getImage(scope);
	}

	@Override
	public String getId() { return "matrix" + hashCode(); }

	@Override
	public BufferedImage getImage(final IScope scope, final boolean useCache) {
		int w = getCols(scope);
		int h = getRows(scope);
		BufferedImage ret = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < w; i++) { for (int j = 0; j < h; j++) { ret.setRGB(i, j, get(scope, i, j).intValue()); } }
		return ret;
	}

}
