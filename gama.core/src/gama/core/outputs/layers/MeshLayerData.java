/*******************************************************************************************************
 *
 * MeshLayerData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaColorFactory;
import gama.api.data.factories.GamaMatrixFactory;
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IField;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.layers.ILayerStatement;
import gama.api.utils.IImageProvider;

/**
 * The Class MeshLayerData.
 */
public class MeshLayerData extends LayerData {

	/** The default line color. */
	static IColor defaultLineColor = GamaColorFactory.BLACK;

	/** The should compute values. */
	boolean shouldComputeValues = true;

	/** The Constant ABOVE. */
	public static final Double ABOVE = (double) -Integer.MAX_VALUE;

	/** The values. */
	IField values;

	/** The line. */
	final Attribute<IColor> line;

	/** The texture. */
	final Attribute<IImageProvider> texture;

	/** The smooth. */
	final Attribute<Integer> smooth;

	/** The elevation. */
	final Attribute<IField> elevation;

	/** The triangulation. */
	final Attribute<Boolean> triangulation;

	/** The grayscale. */
	final Attribute<Boolean> grayscale;

	/** The text. */
	final Attribute<Boolean> text;

	/** The wireframe. */
	final Attribute<Boolean> wireframe;

	/** The no data. */
	final Attribute<Double> noData;

	/** The color. */
	final Attribute<Object> color;

	/** The scale. */
	final Attribute<Double> scale;

	/** The above. */
	final Attribute<Double> above;

	/** The dim. */
	private final IPoint dim = GamaPointFactory.create();

	/**
	 * Instantiates a new mesh layer data.
	 *
	 * @param def
	 *            the def
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("unchecked")
	public MeshLayerData(final ILayerStatement def) throws GamaRuntimeException {
		super(def);
		size = create(IKeyword.SIZE, (scope, exp) -> {
			Object result = exp.value(scope);
			if (result instanceof Number) return GamaPointFactory.create(1, 1, ((Number) result).doubleValue());
			return GamaPointFactory.toPoint(scope, result);
		}, Types.POINT, GamaPointFactory.create(1, 1, 1));
		line = create(IKeyword.BORDER, Types.COLOR, null);
		elevation = create(IKeyword.SOURCE, (scope, exp) -> {
			if (exp != null) return buildValues(scope, exp.value(scope));
			return null;
		}, Types.NO_TYPE, (IField) null);
		triangulation = create(IKeyword.TRIANGULATION, Types.BOOL, false);
		smooth = create(IKeyword.SMOOTH, (scope, exp) -> {
			final Object result = exp.value(scope);
			return result instanceof Boolean b ? b ? 1 : 0 : Cast.asInt(scope, result);
		}, Types.INT, 0);
		grayscale = create(IKeyword.GRAYSCALE, Types.BOOL, false);
		wireframe = create(IKeyword.WIREFRAME, Types.BOOL, false);
		text = create(IKeyword.TEXT, Types.BOOL, false);
		color = create(IKeyword.COLOR, (scope, exp) -> {
			final Object result = exp.value(scope);
			return result instanceof IMatrix mat ? mat.listValue(scope, Types.NO_TYPE, false) : result;
		}, Types.NO_TYPE, null);
		scale = create(IKeyword.SCALE, Types.FLOAT, null);
		noData = create("no_data", Types.FLOAT, IField.NO_NO_DATA);
		above = create("above", Types.FLOAT, ABOVE);
		texture = create(IKeyword.TEXTURE, (scope, exp) -> {
			final Object result = exp.value(scope);
			if (result instanceof IImageProvider) return (IImageProvider) result;
			throw GamaRuntimeException.error("The texture of a field must be an image file", scope);
		}, Types.FILE, null);
	}

	@Override
	public boolean compute(final IScope scope, final IGraphics g) throws GamaRuntimeException {
		boolean result = super.compute(scope, g);
		shouldComputeValues = super.getRefresh();
		return result;
	}

	/**
	 * Builds the values.
	 *
	 * @param scope
	 *            the scope
	 * @param from
	 *            the from
	 * @return the i field
	 */
	private IField buildValues(final IScope scope, final Object from) {
		if (values == null || shouldComputeValues) {
			values = GamaMatrixFactory.createFieldFrom(scope, from);
			dim.setLocation(values.getCols(scope), values.getRows(scope), 0);
		}
		return values;
	}

	/**
	 * Checks if is triangulated.
	 *
	 * @return the boolean
	 */
	public Boolean isTriangulated() { return triangulation.get(); }

	/**
	 * Checks if is gray scaled.
	 *
	 * @return the boolean
	 */
	public Boolean isGrayScaled() { return grayscale.get(); }

	/**
	 * Checks if is wireframe.
	 *
	 * @return the boolean
	 */
	public Boolean isWireframe() { return wireframe.get(); }

	/**
	 * Checks if is show text.
	 *
	 * @return the boolean
	 */
	public Boolean isShowText() { return text.get(); }

	/**
	 * Texture file.
	 *
	 * @return the gama image file
	 */
	public IImageProvider textureFile() {
		return texture.get();
	}

	/**
	 * Gets the line color.
	 *
	 * @return the line color
	 */
	public IColor getLineColor() { return line.get() == null && wireframe.get() ? defaultLineColor : line.get(); }

	/**
	 * Draw lines.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean drawLines() {
		return line.get() != null || wireframe.get();
	}

	/**
	 * Gets the dimension.
	 *
	 * @return the dimension
	 */
	public IPoint getDimension() { return dim; }

	/**
	 * Gets the elevation matrix.
	 *
	 * @param scope
	 *            the scope
	 * @return the elevation matrix
	 */
	public IField getElevationMatrix(final IScope scope) {
		return elevation.get();
	}

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	public Object getColor() { return color.get(); }

	/**
	 * Gets the smooth.
	 *
	 * @return the smooth
	 */
	public Integer getSmooth() { return smooth.get(); }

	/**
	 * Gets the scale.
	 *
	 * @return the scale
	 */
	public Double getScale() { return scale.get(); }

	/**
	 * Gets the no data value.
	 *
	 * @return the no data value
	 */
	public double getNoDataValue() { return noData.get(); }

	/**
	 * Mesh objects are not selectable
	 */
	@Override
	public Boolean isSelectable() { return false; }

	/**
	 * Gets the above.
	 *
	 * @return the above
	 */
	public double getAbove() { return above.get(); }

}
