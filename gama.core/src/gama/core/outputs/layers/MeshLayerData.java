/*******************************************************************************************************
 *
 * MeshLayerData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.Color;

import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.IImageProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.matrix.IField;
import gama.core.util.matrix.IMatrix;
import gama.gaml.operators.Cast;
import gama.gaml.types.GamaFieldType;
import gama.gaml.types.Types;

/**
 * The Class MeshLayerData.
 */
public class MeshLayerData extends LayerData {

	/** The default line color. */
	static GamaColor defaultLineColor = GamaColor.get(Color.black);

	/** The should compute values. */
	boolean shouldComputeValues = true;

	/** The Constant ABOVE. */
	public static final Double ABOVE = (double) -Integer.MAX_VALUE;

	/** The values. */
	IField values;

	/** The line. */
	final Attribute<GamaColor> line;

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
	private final GamaPoint dim = new GamaPoint();

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
			if (result instanceof Number) return new GamaPoint(1, 1, ((Number) result).doubleValue());
			return Cast.asPoint(scope, result);
		}, Types.POINT, new GamaPoint(1, 1, 1));
		line = create(IKeyword.BORDER, Types.COLOR, null);
		elevation = create(IKeyword.SOURCE, (scope, exp) -> {
			if (exp != null) return buildValues(scope, exp.value(scope));
			return null;
		}, Types.NO_TYPE, (IField) null);
		triangulation = create(IKeyword.TRIANGULATION, Types.BOOL, false);
		smooth = create(IKeyword.SMOOTH, (scope, exp) -> {
			final Object result = exp.value(scope);
			return result instanceof Boolean ? (Boolean) result ? 1 : 0 : Cast.asInt(scope, result);
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
			values = GamaFieldType.buildField(scope, from);
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
	public GamaColor getLineColor() { return line.get() == null && wireframe.get() ? defaultLineColor : line.get(); }

	/**
	 * Draw lines.
	 *
	 * @return true, if successful
	 */
	public boolean drawLines() {
		return line.get() != null || wireframe.get();
	}

	/**
	 * Gets the dimension.
	 *
	 * @return the dimension
	 */
	public GamaPoint getDimension() { return dim; }

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
