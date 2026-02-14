/*******************************************************************************************************
 *
 * GamaFloatMatrix.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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

import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaMatrixFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IField;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.IImageProvider;
import gama.api.utils.random.IRandom;
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
		if (m instanceof IField f) return new GamaFloatMatrix(m.getCols(scope), m.getRows(scope), f.getMatrix());
		if (m instanceof GamaFloatMatrix) return (GamaFloatMatrix) m;
		if (m instanceof GamaObjectMatrix o)
			return new GamaFloatMatrix(scope, m.getCols(scope), m.getRows(scope), o.getMatrix());
		if (m instanceof GamaIntMatrix i) return new GamaFloatMatrix(m.getCols(scope), m.getRows(scope), i.matrix);
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
		return switch (m) {
			case GamaFloatMatrix f -> new GamaFloatMatrix(c, r, f.getMatrix());
			case GamaObjectMatrix o -> new GamaFloatMatrix(scope, c, r, o.getMatrix());
			case GamaIntMatrix i -> new GamaFloatMatrix(c, r, i.matrix);
			case null, default -> null;
		};
	}

	/** The matrix. */
	protected double[] matrix;

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param mat
	 *            the mat
	 */
	GamaFloatMatrix(final double[] mat) {
		super(1, mat.length, Types.FLOAT);
		setMatrix(mat);
	}

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param p
	 *            the p
	 */
	GamaFloatMatrix(final IPoint p) {
		this((int) p.getX(), (int) p.getY());
	}

	/**
	 * Instantiates a new gama float matrix.
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 */
	GamaFloatMatrix(final int cols, final int rows) {
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
	GamaFloatMatrix(final int cols, final int rows, final double[] objects) {
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
	GamaFloatMatrix(final int cols, final int rows, final int[] objects) {
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
	GamaFloatMatrix(final IScope scope, final int cols, final int rows, final Object[] objects) {
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
	GamaFloatMatrix(final IScope scope, final List objects, final IPoint preferredSize) throws GamaRuntimeException {
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
	GamaFloatMatrix(final IScope scope, final Object[] mat) {
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
	@Override
	public IMatrix _opAppendVertically(final IScope scope, final IMatrix b) {
		if (b instanceof GamaFloatMatrix gfm) {
			final double[] mab = ArrayUtils.addAll(getMatrix(), gfm.getMatrix());
			return new GamaFloatMatrix(numCols, numRows + gfm.getRows(scope), mab);
		}
		return this;
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
		final IMatrix aprime = _reverse(scope);
		final IMatrix bprime = b._reverse(scope);
		final IMatrix c = aprime._opAppendVertically(scope, bprime);
		return c._reverse(scope);
	}

	@Override
	public boolean _isEmpty(final IScope scope) {
		for (int i = 0; i < getMatrix().length; i++) { if (getMatrix()[i] != 0d) return false; }
		return true;
	}

	@Override
	protected IMatrix _matrixValue(final IScope scope, final IPoint preferredSize, final IType type,
			final boolean copy) {
		return GamaMatrixFactory.createFromMatrix(scope, this, type, preferredSize, copy);
	}

	@Override
	public IMatrix _reverse(final IScope scope) throws GamaRuntimeException {
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
	public IMatrix copy(final IScope scope, final IPoint size, final boolean copy) {
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

	// TODO Remove to improve performances if necessary
	@Override
	public int hashCode() {
		return Arrays.hashCode(getMatrix());
	}

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
	public void shuffleWith(final IRandom randomAgent) {
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
	public IMatrix plus(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		final GamaFloatMatrix matb = from(scope, other);
		if (matb != null && this.numCols == matb.numCols && this.numRows == matb.numRows) {
			final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
			for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] + matb.matrix[i]; }
			return nm;
		}
		throw GamaRuntimeException.error(" The dimensions of the matrices do not correspond", scope);
	}

	@Override
	public IMatrix times(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		final GamaFloatMatrix matb = from(scope, other);
		if (matb != null && this.numCols == matb.numCols && this.numRows == matb.numRows) {
			final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
			for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] * matb.matrix[i]; }
			return nm;
		}
		throw GamaRuntimeException.error(" The dimensions of the matrices do not correspond", scope);
	}

	@Override
	public IMatrix minus(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		final GamaFloatMatrix matb = from(scope, other);
		if (matb != null && this.numCols == matb.numCols && this.numRows == matb.numRows) {
			final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
			for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] - matb.matrix[i]; }
			return nm;
		}
		throw GamaRuntimeException.error(" The dimensions of the matrices do not correspond", scope);
	}

	@Override
	public IMatrix times(final Double val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] * val; }
		return nm;
	}

	@Override
	public IMatrix times(final Integer val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] * val; }
		return nm;
	}

	@Override
	public IMatrix divides(final Double val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] / val; }
		return nm;
	}

	@Override
	public IMatrix divides(final Integer val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] / val; }
		return nm;
	}

	@Override
	public IMatrix divides(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		final GamaFloatMatrix matb = from(scope, other);
		if (matb != null && this.numCols == matb.numCols && this.numRows == matb.numRows) {
			final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
			for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] / matb.matrix[i]; }
			return nm;
		}
		throw GamaRuntimeException.error(" The dimensions of the matrices do not correspond", scope);
	}

	@Override
	public IMatrix plus(final Double val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] + val; }
		return nm;
	}

	@Override
	public IMatrix plus(final Integer val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] + val; }
		return nm;
	}

	@Override
	public IMatrix minus(final Double val) throws GamaRuntimeException {
		final GamaFloatMatrix nm = new GamaFloatMatrix(this.numCols, this.numRows);
		for (int i = 0; i < matrix.length; i++) { nm.matrix[i] = matrix[i] - val; }
		return nm;
	}

	@Override
	public IMatrix minus(final Integer val) throws GamaRuntimeException {
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
	public IType<?> computeRuntimeType(final IScope scope) {
		return getGamlType();
	}

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
