/*******************************************************************************************************
 *
 * InternalGamaMatrixFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.matrix;

import java.util.Arrays;
import java.util.stream.IntStream;

import gama.api.data.factories.GamaMatrixFactory;
import gama.api.data.factories.IMatrixFactory;
import gama.api.data.objects.IField;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.api.utils.IFieldMatrixProvider;
import gama.api.utils.geometry.GamaPointFactory;

/**
 *
 */
public class InternalGamaMatrixFactory implements IMatrixFactory {

	@Override
	public IMatrix createWith(final IScope scope, final IExpression val, final IPoint p, final boolean parallel)
			throws GamaRuntimeException {
		return createWith(scope, val, (int) p.getX(), (int) p.getY(), parallel);
	}

	@Override
	public IMatrix createWith(final IScope scope, final IExpression fillExpr, final int cols, final int rows,
			final boolean parallel) {
		IMatrix result;
		if (fillExpr == null) return new GamaObjectMatrix(cols, rows, Types.NO_TYPE);
		switch (fillExpr.getGamlType().id()) {
			case IType.FLOAT:
				result = new GamaFloatMatrix(cols, rows);
				final double[] dd = ((GamaFloatMatrix) result).getMatrix();
				if (fillExpr.isConst()) {
					Arrays.fill(dd, Cast.asFloat(scope, fillExpr.value(scope)));
				} else if (!parallel) {
					for (int i = 0; i < dd.length; i++) { dd[i] = Cast.asFloat(scope, fillExpr.value(scope)); }
				} else {
					GamaExecutorService.executeThreaded(() -> IntStream.range(0, dd.length).parallel().forEach(i -> {
						dd[i] = Cast.asFloat(scope, fillExpr.value(scope));
					}));
				}
				break;
			case IType.INT:
				result = new GamaIntMatrix(cols, rows);
				final int[] ii = ((GamaIntMatrix) result).getMatrix();
				if (fillExpr.isConst()) {
					Arrays.fill(ii, Cast.asInt(scope, fillExpr.value(scope)));
				} else if (!parallel) {
					for (int i = 0; i < ii.length; i++) { ii[i] = Cast.asInt(scope, fillExpr.value(scope)); }
				} else {
					GamaExecutorService.executeThreaded(() -> IntStream.range(0, ii.length).parallel().forEach(i -> {
						ii[i] = Cast.asInt(scope, fillExpr.value(scope));
					}));
				}
				break;
			default:
				result = new GamaObjectMatrix(cols, rows, fillExpr.getGamlType());
				final Object[] contents = ((GamaObjectMatrix) result).getMatrix();
				if (fillExpr.isConst()) {
					Arrays.fill(contents, fillExpr.value(scope));
				} else if (!parallel) {
					for (int i = 0; i < contents.length; i++) { contents[i] = fillExpr.value(scope); }
				} else {
					GamaExecutorService
							.executeThreaded(() -> IntStream.range(0, contents.length).parallel().forEach(i -> {
								contents[i] = fillExpr.value(scope);
							}));
				}
		}
		return result;
	}

	/**
	 * Creates a new InternalGamaMatrix object.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param p
	 *            the p
	 * @param contentsType
	 *            the contents type
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public IMatrix createWithValue(final IScope scope, final Object val, final IPoint p, final IType contentsType)
			throws GamaRuntimeException {
		return createWithValue(scope, val, (int) p.getX(), (int) p.getY(), contentsType);
	}

	/**
	 * Creates a new InternalGamaMatrix object.
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
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public IMatrix createWithValue(final IScope scope, final Object val, final int cols, final int rows,
			final IType contentsType) throws GamaRuntimeException {

		if (contentsType == Types.INT || val instanceof Integer) {
			final GamaIntMatrix matrix = new GamaIntMatrix(cols, rows);
			matrix.setAllValues(scope, Types.INT.cast(scope, val, null, false));
			return matrix;
		}
		if (contentsType == Types.FLOAT || val instanceof Double) {
			final GamaFloatMatrix matrix = new GamaFloatMatrix(cols, rows);
			matrix.setAllValues(scope, Types.FLOAT.cast(scope, val, null, false));
			return matrix;
		}
		final IMatrix matrix = new GamaObjectMatrix(cols, rows, contentsType);
		((GamaObjectMatrix) matrix).setAllValues(scope, contentsType.cast(scope, val, null, false));
		return matrix;

	}

	@Override
	public IMatrix createFromMatrix(final IScope scope, final IMatrix matrix, final IType desiredType,
			final IPoint preferredSize, final boolean copy) {
		final IType contentsType = matrix.getGamlType().getContentType();
		if (!GamaType.requiresCasting(desiredType, contentsType)) {
			if (matrix instanceof IField f) return GamaFloatMatrix.from(scope, f);
			return matrix.copy(scope, preferredSize, copy);
		}

		int cols, rows;
		if (preferredSize == null) {
			cols = matrix.getCols(scope);
			rows = matrix.getRows(scope);
		} else {
			cols = (int) preferredSize.getX();
			rows = (int) preferredSize.getY();
		}
		switch (desiredType.id()) {
			case IType.INT:
				return GamaIntMatrix.from(scope, cols, rows, matrix);
			case IType.FLOAT:
				return GamaFloatMatrix.from(scope, cols, rows, matrix);
			default:
				final GamaObjectMatrix m = GamaObjectMatrix.from(cols, rows, matrix);
				final Object[] array = m.getMatrix();
				for (int i = 0; i < array.length; i++) { array[i] = desiredType.cast(scope, array[i], null, false); }
				return m;
		}

	}

	@Override
	public IMatrix createMatrixLike(final IScope scope, final IMatrix matrix, final IPoint dimensions) {
		return matrix.getGamlType().id() == IType.FIELD
				? GamaMatrixFactory.createFieldWithSize(scope, (int) dimensions.getX(), (int) dimensions.getY())
				: switch (matrix.getGamlType().getContentType().id()) {
					case IType.INT -> new GamaIntMatrix(dimensions);
					case IType.FLOAT -> new GamaFloatMatrix(dimensions);
					default -> new GamaObjectMatrix(dimensions, matrix.getGamlType().getContentType());
				};
	}

	@Override
	public IMatrix createFrom(final IScope scope, final IList list, final IType desiredType,
			final IPoint preferredSize) {
		if (list == null || list.isEmpty()) return new GamaObjectMatrix(0, 0, desiredType);
		if (desiredType.id() == IType.INT) return new GamaIntMatrix(scope, list, preferredSize);
		if (desiredType.id() == IType.FLOAT) return new GamaFloatMatrix(scope, list, preferredSize);
		return new GamaObjectMatrix(scope, list, preferredSize, desiredType);

	}

	@Override
	public IMatrix createWith(final IScope scope, final String eachName, final IPoint size, final IExpression init) {
		if (init == null || size == null || size.getX() <= 0 || size.getY() <= 0)
			return new GamaObjectMatrix(0, 0, Types.NO_TYPE);
		final int cols = (int) size.getX();
		final int rows = (int) size.getY();
		int type = init.getGamlType().id();
		IMatrix result = switch (type) {
			case IType.FLOAT -> new GamaFloatMatrix(cols, rows);
			case IType.INT -> new GamaIntMatrix(cols, rows);
			default -> new GamaObjectMatrix(cols, rows, init.getGamlType());
		};
		if (init.isConst()) return switch (type) {
			case IType.FLOAT -> {
				final double[] dd = ((GamaFloatMatrix) result).getMatrix();
				Arrays.fill(dd, Cast.asFloat(scope, init.value(scope)));
				yield result;
			}
			case IType.INT -> {
				final int[] ii = ((GamaIntMatrix) result).getMatrix();
				Arrays.fill(ii, Cast.asInt(scope, init.value(scope)));
				yield result;
			}
			default -> {
				final Object[] contents = ((GamaObjectMatrix) result).getMatrix();
				Arrays.fill(contents, init.value(scope));
				yield result;
			}
		};
		IPoint each = GamaPointFactory.create();
		scope.setEach(eachName, each);
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				each.setLocation(x, y, 0);
				result.set(scope, x, y, init.value(scope));
			}
		}
		return result;

	}

	@Override
	public IMatrix create(final int cols, final int rows, final IType contentsType) {
		return switch (contentsType.id()) {
			case IType.INT -> new GamaIntMatrix(cols, rows);
			case IType.FLOAT -> new GamaFloatMatrix(cols, rows);
			default -> new GamaObjectMatrix(cols, rows, contentsType);
		};
	}

	@Override
	public IMatrix create(final int cols, final int rows, final int typeId) {
		return create(cols, rows, Types.get(typeId));
	}

	@Override
	public IField createField(final IScope scope, final int cols, final int rows, final double[] data,
			final double no) {
		return new GamaField(scope, cols, rows, data, no);
	}

	@Override
	public IField createField(final IScope scope, final int cols, final int rows) {
		double[] data = new double[cols * rows];
		Arrays.fill(data, 0d);
		return createField(scope, cols, rows, data, IField.NO_NO_DATA);
	}

	@Override
	public IField createFieldFromProvider(final IScope scope, final IFieldMatrixProvider provider) {
		return new GamaField(scope, provider);
	}

}
