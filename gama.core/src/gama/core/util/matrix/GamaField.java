/*******************************************************************************************************
 *
 * GamaField.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.matrix;

import static gama.gaml.types.GamaGeometryType.buildRectangle;

import java.util.Arrays;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.common.geometry.GamaEnvelopeFactory;
import gama.core.common.geometry.GeometryUtils;
import gama.core.common.geometry.IEnvelope;
import gama.core.common.interfaces.IFieldMatrixProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.shape.GamaPointFactory;
import gama.core.metamodel.shape.IPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.list.GamaListFactory;
import gama.core.util.list.IList;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Colors;
import gama.gaml.statements.draw.IMeshColorProvider;
import gama.gaml.statements.draw.MeshDrawingAttributes;
import gama.gaml.types.GamaFieldType;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import one.util.streamex.DoubleStreamEx;
import one.util.streamex.StreamEx;

/**
 * The Class GamaField.
 */
public class GamaField extends GamaFloatMatrix implements IField {

	/** The world dimensions. */
	IPoint worldDimensions = null;

	/** The cell dimensions. */
	IPoint cellDimensions = null;

	/** The no data value. */
	double noDataValue;

	/** The bands. */
	IList<GamaField> bands = GamaListFactory.create(Types.FIELD);

	/**
	 * Instantiates a new gama field.
	 *
	 * @param scope
	 *            the scope
	 * @param provider
	 *            the provider
	 */
	public GamaField(final IScope scope, final IFieldMatrixProvider provider) {
		this(scope, provider.getCols(scope), provider.getRows(scope), provider.getFieldData(scope),
				provider.getNoData(scope));
		int nbBands = provider.getBandsNumber(scope);
		// The first provider is already added to the bands in the Field constructor
		for (int i = 1; i < nbBands; i++) { bands.add(new GamaField(scope, this, provider.getBand(scope, i))); }
	}

	/**
	 * Instantiates a new gama field.
	 *
	 * @param scope
	 *            the scope
	 * @param primary
	 *            the primary
	 * @param band
	 *            the band
	 */
	private GamaField(final IScope scope, final GamaField primary, final double[] band) {
		this(scope, primary.numCols, primary.numRows, band, primary.noDataValue);
		worldDimensions = primary.worldDimensions;
		cellDimensions = primary.cellDimensions;
	}

	/**
	 * Instantiates a new gama field.
	 *
	 * @param scope
	 *            the scope
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param objects
	 *            the objects
	 * @param noDataValue
	 *            the no data value
	 */
	public GamaField(final IScope scope, final int cols, final int rows, final double[] objects,
			final double noDataValue) {
		super(objects); // no copy
		this.noDataValue = noDataValue;
		numCols = cols;
		numRows = rows;
		bands.add(this);
	}

	/**
	 * Call this method before any computation that involves the world/cell dimensions. Computed lazily to avoid
	 * deadlock problems (when the shape of the world, for instance, is computed after a field)
	 *
	 * @param scope
	 */
	private void computeDimensions(final IScope scope) {
		if (worldDimensions != null) return;
		IShape world = scope.getSimulation().getGeometry();
		worldDimensions = GamaPointFactory.create(world.getWidth(), world.getHeight());
		cellDimensions = GamaPointFactory.create(world.getWidth() / this.numCols, world.getHeight() / this.numRows);
	}

	@Override
	public Double getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		final int size = indices.size();
		if (size == 1) {
			final Object index = indices.get(0);
			if (index instanceof IPoint ip) return get(scope, ip);
			return matrix[Cast.asInt(scope, index)];
		}
		return get(scope, Cast.asInt(scope, indices.get(0)), Cast.asInt(scope, indices.get(1)));
	}

	/**
	 * Access through location: this corresponds to an access through world coordinates by agents. The access through
	 * grid coordinates is already taken in charge by matrices
	 */
	@Override
	public Double get(final IScope scope, final IPoint p) {
		computeDimensions(scope);
		IPoint gp = GamaPointFactory.create(p);
		// May happen in case of torus environment (see #3132)
		double gpx = gp.getX();
		double gpy = gp.getY();
		int x = gpx < 0 ? 0 : gpx >= worldDimensions.getX() ? this.numCols - 1 : (int) (gpx / cellDimensions.getX());
		int y = gpy < 0 ? 0 : gpy >= worldDimensions.getY() ? this.numRows - 1 : (int) (gpy / cellDimensions.getY());
		return matrix[y * numCols + x];
	}

	/**
	 * If the index is a list of int indices, we translate it into a point. If it is already a location, it is an access
	 * through world coordinates by agents.
	 */
	@Override
	public void setValueAtIndex(final IScope scope, final Object at, final Double value) {
		computeDimensions(scope);
		int index = -1;
		if (at instanceof Integer) {
			index = (Integer) at;
		} else if (at instanceof IList list) {
			index = (Integer) list.get(1) * numCols + (Integer) list.get(0);
		} else if (at instanceof IPoint gp) {
			double gpx = gp.getX();
			double gpy = gp.getY();
			int x = gpx < 0 ? 0 : gpx >= worldDimensions.getX() ? this.numCols - 1
					: (int) (gpx / cellDimensions.getX());
			int y = gpy < 0 ? 0 : gpy >= worldDimensions.getY() ? this.numRows - 1
					: (int) (gpy / cellDimensions.getY());
			index = y * numCols + x;
		}
		if (index > -1 && index < matrix.length) { matrix[index] = value; }
	}

	@Override
	public IPoint getCellSize(final IScope scope) {
		computeDimensions(scope);
		return cellDimensions;
	}

	@Override
	public double getNoData(final IScope scope) {
		return noDataValue;
	}

	@Override
	public void setNoData(final IScope scope, final double noData) {
		if (noData != noDataValue) { noDataValue = noData; }
		if (bands.size() > 1) { for (int i = 1; i < bands.size(); i++) { bands.get(i).setNoData(scope, noData); } }
	}

	@Override
	public IList<? extends IField> getBands(final IScope scope) {
		return bands;
	}

	@Override
	public double[] getMinMax() {
		double[] result = { Double.MAX_VALUE, -Double.MAX_VALUE };
		DoubleStreamEx.of(getMatrix()).parallel().forEach(f -> {
			if (f != noDataValue) {
				if (f > result[1]) { result[1] = f; }
				if (f < result[0]) { result[0] = f; }
			}
		});
		return result;
	}

	/**
	 * Inherited from IDiffusionTarget. The variable name (to diffuse) is not considered and the number of neighbours is
	 * 8 by default (should be set as a property of the diffuser...)
	 */

	@Override
	public int getNbNeighbours() {
		return 8; // ??? default ??
	}

	@Override
	public double getValueAtIndex(final IScope scope, final int i, final String var_diffu) {
		return matrix[i];
	}

	@Override
	public void setValueAtIndex(final IScope scope, final int i, final String var_diffu, final double val) {
		matrix[i] = val;
	}

	@Override
	public void getValuesInto(final IScope scope, final String varName, final double minValue, final double[] input) {
		System.arraycopy(matrix, 0, input, 0, input.length);
		for (int i = 0; i < input.length; i++) { if (input[i] < minValue) { input[i] = 0; } }
	}

	/**
	 * We only stream away the values different from noDataValue (should normally allow most of the algorithms in
	 * Containers to work)
	 *
	 */
	@Override
	public StreamEx<Double> stream(final IScope scope) {
		return DoubleStreamEx.of(getMatrix()).filter(d -> d != noDataValue).boxed();
	}

	/**
	 * We only iterate over the values different from noDataValue (should normally allow most of the algorithms in
	 * Statistics to work)
	 *
	 */
	@Override
	public java.lang.Iterable<Double> iterable(final IScope scope) {
		return Iterables.filter(Doubles.asList(getMatrix()), e -> e != noDataValue);
	}

	@Override
	public IShape getCellShapeAt(final IScope scope, final IPoint gp) {
		computeDimensions(scope);
		double gpx = gp.getX();
		double gpy = gp.getY();
		int x = gpx < 0 ? 0 : gpx >= worldDimensions.getX() ? this.numCols - 1 : (int) (gpx / cellDimensions.getX());
		int y = gpy < 0 ? 0 : gpy >= worldDimensions.getY() ? this.numRows - 1 : (int) (gpy / cellDimensions.getY());
		return getCellShapeAt(scope, x, y);

	}

	@Override
	public IShape getCellShapeAt(final IScope scope, final int columns, final int rows) {
		computeDimensions(scope);
		// Necessary to add the z ? Verify the translations
		double x = cellDimensions.getX();
		double y = cellDimensions.getY();
		return buildRectangle(x, y,
				GamaPointFactory.create(columns * x + x / 2, rows * y + y / 2, get(scope, columns, rows)));
	}

	@Override
	public IList<Double> getValuesIntersecting(final IScope scope, final IShape shape) {
		computeDimensions(scope);
		IEnvelope env = GamaEnvelopeFactory.of(shape);
		IList<Double> inEnv = GamaListFactory.create(Types.FLOAT);
		IPoint p = GamaPointFactory.create();
		for (double i = env.getMinX(); i < env.getMaxX(); i += cellDimensions.getX()) {
			for (double j = env.getMinY(); j < env.getMaxY(); j += cellDimensions.getY()) {
				p.setLocation(i, j, 0);
				if (GeometryUtils.POINT_LOCATOR.intersects(p.toCoordinate(), shape.getInnerGeometry())) {
					Double d = get(scope, p);
					if (d != null) { inEnv.add(d); }
				}
			}
		}
		return inEnv;
	}

	@Override
	public IList<IShape> getCellsIntersecting(final IScope scope, final IShape shape) {
		computeDimensions(scope);
		IEnvelope env = GamaEnvelopeFactory.of(shape);
		IList<IShape> inEnv = GamaListFactory.create(Types.GEOMETRY);
		IPoint p = GamaPointFactory.create();
		for (double i = env.getMinX(); i < env.getMaxX(); i += cellDimensions.getX()) {
			for (double j = env.getMinY(); j < env.getMaxY(); j += cellDimensions.getY()) {
				p.setLocation(i, j, 0);
				if (GeometryUtils.POINT_LOCATOR.intersects(p.toCoordinate(), shape.getInnerGeometry())) {
					IShape s = getCellShapeAt(scope, p);
					if (s != null) { inEnv.add(s); }
				}
			}
		}
		return inEnv;
	}

	@Override
	public IList<IShape> getCellsOverlapping(final IScope scope, final IShape shape) {
		computeDimensions(scope);
		IEnvelope env = GamaEnvelopeFactory.of(shape);
		IList<IShape> inEnv = GamaListFactory.create(Types.GEOMETRY);
		IPoint p = GamaPointFactory.create();
		for (double i = env.getMinX(); i < env.getMaxX(); i += cellDimensions.getX()) {
			for (double j = env.getMinY(); j < env.getMaxY(); j += cellDimensions.getY()) {
				p.setLocation(i, j, 0);
				IShape s = getCellShapeAt(scope, p);
				if (s != null && s.intersects(shape)) { inEnv.add(s); }
			}
		}
		return inEnv;
	}

	@Override
	public IList<IPoint> getLocationsIntersecting(final IScope scope, final IShape shape) {
		computeDimensions(scope);
		IEnvelope env = GamaEnvelopeFactory.of(shape);
		IList<IPoint> inEnv = GamaListFactory.create(Types.POINT);
		IPoint p = GamaPointFactory.create();
		for (double i = env.getMinX(); i < env.getMaxX(); i += cellDimensions.getX()) {
			for (double j = env.getMinY(); j < env.getMaxY(); j += cellDimensions.getY()) {
				p.setLocation(i, j, 0);
				if (GeometryUtils.POINT_LOCATOR.intersects(p.toCoordinate(), shape.getInnerGeometry())) {
					inEnv.add(p.copy(scope));
				}
			}
		}
		return inEnv;

	}

	@Override
	public IList<IPoint> getNeighborsOf(final IScope scope, final IPoint point) {
		computeDimensions(scope);
		IList<IPoint> result = GamaListFactory.create(Types.POINT);
		int x = (int) (point.getX() / cellDimensions.getX());
		int y = (int) (point.getY() / cellDimensions.getY());
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int x1 = x + i;
				int y1 = y + j;
				if (x1 < 0 || x1 > numCols - 1 || y1 < 0 || y1 > numRows - 1 || i == 0 && j == 0) { continue; }
				// We add the z ?
				result.add(GamaPointFactory.create(x1 * cellDimensions.getX(), y1 * cellDimensions.getY(),
						this.get(scope, x1, y1)));
			}
		}
		return result;
	}

	@Override
	public double[] getBand(final IScope scope, final int index) {
		double[] result = super.getBand(scope, index);
		if (result == null && index < bands.size()) { result = bands.get(index).getBand(scope, 0); }
		return result;
	}

	@Override
	public int getBandsNumber(final IScope scope) {
		return bands.size();
	}

	@Override
	public GamaField copy(final IScope scope, final IPoint size, final boolean copy) {
		if (size == null) {
			if (!copy) return this;
			GamaField result =
					new GamaField(scope, numCols, numRows, Arrays.copyOf(getMatrix(), getMatrix().length), noDataValue);
			if (bands.size() > 1) {
				for (GamaField f : bands) {
					result.bands.add(new GamaField(scope, numCols, numRows,
							Arrays.copyOf(f.getMatrix(), f.getMatrix().length), noDataValue));
				}
			}
			return result;
		}
		GamaField result = new GamaField(scope, (int) size.getX(), (int) size.getY(),
				Arrays.copyOf(getMatrix(), getMatrix().length), noDataValue);
		if (bands.size() > 1) {
			for (GamaField f : bands) {
				result.bands.add(new GamaField(scope, (int) size.getX(), (int) size.getY(),
						Arrays.copyOf(f.getMatrix(), f.getMatrix().length), noDataValue));
			}
		}
		return result;
	}

	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc (
					side_effects = "Modifies the left field. Use an explicit copy operation to prevent this",
					value = "Adds a matrix or a field to the left field"))
	@Override
	@no_test
	public GamaField plus(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		switch (other) {
			case GamaField gf -> {
				double otherNoDataValue = gf.noDataValue;
				for (int i = 0; i < matrix.length; i++) {
					if (matrix[i] != noDataValue && gf.matrix[i] != otherNoDataValue) { matrix[i] += gf.matrix[i]; }
				}
			}
			case GamaFloatMatrix nm -> {
				for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] += nm.matrix[i]; } }
			}
			case GamaIntMatrix nm -> {
				for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] += nm.matrix[i]; } }
			}
			case null, default -> {
			}
		}
		return this;
	}

	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc (
					side_effects = "Modifies the left field. Use an explicit copy operation to prevent this",
					value = "Subtracts a matrix or a field from the left field"))
	@Override
	@no_test
	public GamaField minus(final IScope scope, final IMatrix other) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		switch (other) {
			case GamaField gf -> {
				double otherNoDataValue = gf.noDataValue;
				for (int i = 0; i < matrix.length; i++) {
					if (matrix[i] != noDataValue && gf.matrix[i] != otherNoDataValue) { matrix[i] -= gf.matrix[i]; }
				}
			}
			case GamaFloatMatrix nm -> {
				for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] -= nm.matrix[i]; } }
			}
			case GamaIntMatrix nm -> {
				for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] -= nm.matrix[i]; } }
			}
			case null, default -> {
			}
		}
		return this;
	}

	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Modifies the field. Use an explicit copy operation to prevent this",
					value = "Scales the values in the field by the float parameter"))
	@Override
	@no_test
	public GamaField times(final Double val) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] *= val; } }
		return this;
	}

	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Modifies the field. Use an explicit copy operation to prevent this",
					value = "Scales the values in the field by the int parameter"))
	@Override
	@no_test
	public GamaField times(final Integer val) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] *= val; } }
		return this;
	}

	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Modifies the field. Use an explicit copy operation to prevent this",
					value = "Scales the values in the field by 1 on the float parameter"))
	@Override
	@no_test
	public GamaField divides(final Double val) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] /= val; } }
		return this;
	}

	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Modifies the field. Use an explicit copy operation to prevent this",
					value = "Scales the values in the field by 1 on the int parameter"))
	@Override
	@no_test
	public GamaField divides(final Integer val) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] /= val; } }
		return this;
	}

	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Modifies the field. Use an explicit copy operation to prevent this",
					value = "Adds a float value to all the values in the field"))
	@Override
	@no_test
	public GamaField plus(final Double val) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] += val; } }
		return this;
	}

	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Modifies the field. Use an explicit copy operation to prevent this",
					value = "Adds an int value to all the values in the field"))
	@Override
	@no_test
	public GamaField plus(final Integer val) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] += val; } }
		return this;
	}

	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Modifies the field. Use an explicit copy operation to prevent this",
					value = "Subtracts a float value from all the values in the field"))
	@Override
	@no_test
	public GamaField minus(final Double val) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] -= val; } }
		return this;
	}

	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Modifies the field. Use an explicit copy operation to prevent this",
					value = "Subtracts an int value from all the values in the field"))
	@Override
	@no_test
	public GamaField minus(final Integer val) throws GamaRuntimeException {
		// No check for best performances. Errors will be emitted by the various sub-operations (out of bounds, etc.)
		for (int i = 0; i < matrix.length; i++) { if (matrix[i] != noDataValue) { matrix[i] -= val; } }
		return this;
	}

	/**
	 * Flatten.
	 *
	 * @return the gama field
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "flatten",
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Does not modify the field but can return the same one. Use an explicit copy operation to prevent this",
					value = "Flattens this field into a grayscale 1-band field. The bands if they exist are supposed to represent RGB components"))
	@no_test
	public GamaField flatten(final IScope scope) throws GamaRuntimeException {
		return flatten(scope, null);
	}

	/**
	 * Flatten.
	 *
	 * @param provider
	 *            the provider
	 * @return the gama field
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "flatten",
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc (
					side_effects = "Does not modify the field",
					value = "Flattens this field into a 1-band field using the color computer passed in parameter (the same argument as the one used in mesh layers): a palette, a scale, a color. If this computer cannot be interpreted, defaults to flattening interpreting the bands as RGB components"))
	@no_test
	public GamaField flatten(final IScope scope, final Object computer) throws GamaRuntimeException {
		// if (bands.size() == 1) return this;
		IMeshColorProvider provider =
				computer instanceof IMeshColorProvider msp ? msp : MeshDrawingAttributes.computeColors(computer, true);
		GamaField result = (GamaField) GamaFieldType.buildField(scope, this.numCols, this.numRows);
		int index;
		double[] minMax = this.getMinMax();
		double[] rgb = new double[4];
		for (int i = 0; i < this.numCols; i++) {
			for (int j = 0; j < this.numRows; j++) {
				index = j * this.numCols + i;
				double[] color = provider.getColor(index, matrix[index], minMax[0], minMax[1], rgb);
				result.matrix[index] = Colors
						.rgb((int) (color[0] * 255), (int) (color[1] * 255), (int) (color[2] * 255), color[3] * 255)
						.getRGB();
			}
		}
		return result;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public IContainerType getGamlType() { return Types.FIELD; }

}
