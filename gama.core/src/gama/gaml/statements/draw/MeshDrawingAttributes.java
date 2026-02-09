/*******************************************************************************************************
 *
 * MeshDrawingAttributes.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.awt.Color;

import gama.api.data.objects.IColor;
import gama.api.data.objects.IField;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPoint;
import gama.api.ui.layers.IMeshColorProvider;
import gama.api.ui.layers.IMeshSmoothProvider;
import gama.api.utils.geometry.Scaling3D;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.outputs.layers.MeshLayerData;
import gama.core.util.color.GamaGradient;
import gama.core.util.color.GamaPalette;
import gama.core.util.color.GamaScale;

/**
 * The Class MeshDrawingAttributes.
 */
public class MeshDrawingAttributes extends AssetDrawingAttributes {

	/**
	 * For. If smooth is not asked for, we return the null implementation. If we have a complete field (i.e. no "no
	 * data" cells) we opt for the super-fast gaussian blur. If not we go back to the slow, but robust, 3x3 convolution
	 *
	 * @param smooth
	 *            the smooth
	 * @param noData
	 *            the no data
	 * @return the i mesh smooth provider
	 */
	static IMeshSmoothProvider SMOOTH_METHOD_FOR(final int smooth, final double noData) {
		return smooth == 0 ? NULL : noData == IField.NO_NO_DATA ? FAST : SLOW;
	}

	/** The fast. */
	static final IMeshSmoothProvider FAST = new GaussianBlurMeshSmoothProvider();

	/** The slow. */
	static final IMeshSmoothProvider SLOW = new ConvolutionBasedMeshSmoothProvider();

	/** The default. */
	final static IMeshColorProvider DEFAULT =
			new ColorBasedMeshColorProvider(GamaPreferences.Displays.CORE_COLOR.getValue());

	/** The grayscale. */
	final static IMeshColorProvider GRAYSCALE = new GrayscaleMeshColorProvider();

	/** The smooth provider. */
	public IMeshSmoothProvider smoothProvider;

	/** The color. */
	public IMeshColorProvider colorProvider;

	/** The species name. */
	public String speciesName;

	/** The dimensions. */
	IPoint dimensions;

	/** The scale. */
	Double scale;

	/** The no data. */
	double noData = IField.NO_NO_DATA;

	/** The above. */
	double above = MeshLayerData.ABOVE;

	/** The smooth. */
	int smooth;

	/**
	 * Instantiates a new mesh drawing attributes.
	 *
	 * @param name
	 *            the name
	 * @param border
	 *            the border
	 * @param isImage
	 *            the is image
	 */
	public MeshDrawingAttributes(final String name, final boolean isImage) {
		super(null, isImage);
		speciesName = name;
		smoothProvider = NULL;
	}

	/**
	 * Sets the species name.
	 *
	 * @param name
	 *            the new species name
	 */
	public void setSpeciesName(final String name) { speciesName = name; }

	/**
	 * Sets the colors.
	 *
	 * @param colors
	 *            the new colors
	 */
	public void setColors(final Object colors) { colorProvider = computeColors(colors, isGrayscaled()); }

	/**
	 * Compute colors.
	 *
	 * @param colors
	 *            the colors
	 * @return the i mesh color provider
	 */
	@SuppressWarnings ("unchecked")
	public static IMeshColorProvider computeColors(final Object colors, final boolean isGrayscale) {
		return switch (colors) {
			case IColor gc -> new ColorBasedMeshColorProvider(gc);
			case GamaPalette gp -> new PaletteBasedMeshColorProvider(gp);
			case GamaScale gs -> new ScaleBasedMeshColorProvider(gs);
			case GamaGradient gg -> new GradientBasedMeshColorProvider(gg);
			case IList list -> list.get(0) instanceof IField ? new BandsBasedMeshColorProvider(list)
					: new ListBasedMeshColorProvider((IList<Color>) colors);
			case null, default -> isGrayscale ? GRAYSCALE : DEFAULT;
		};
	}

	// Rules are a bit different for the fill color for fields.

	/**
	 * Gets the color provider.
	 *
	 * @return the color provider
	 */
	@Override
	public IMeshColorProvider getColorProvider() {
		if (isSet(Flag.Selected)) return new ColorBasedMeshColorProvider(SELECTED_COLOR);
		if (highlight != null) return new ColorBasedMeshColorProvider(highlight);
		if (isSet(Flag.Empty)) return null;
		return colorProvider;
	}

	@Override
	public IColor getColor() {
		if (isSet(Flag.Selected)) return SELECTED_COLOR;
		if (highlight != null) return highlight;
		if (isSet(Flag.Empty) || isSet(Flag.Grayscaled)) return null;
		if (textures != null) return TEXTURED_COLOR;
		return fill == null ? GamaPreferences.Displays.CORE_COLOR.getValue() : fill;
	}

	@Override
	public String getSpeciesName() { return speciesName; }

	/**
	 * Gets the XY dimension.
	 *
	 * @return the XY dimension
	 */
	@Override
	public IPoint getXYDimension() { return dimensions; }

	/**
	 * Sets the XY dimension.
	 *
	 * @param dim
	 *            the new XY dimension
	 */
	public void setXYDimension(final IPoint dim) { dimensions = dim; }

	/**
	 * Sets the cell size.
	 *
	 * @param p
	 *            the new cell size
	 */
	// public void setCellSize(final IPoint p) { cellSize = p; }

	/**
	 * Gets the cell size.
	 *
	 * @return the cell size
	 */
	// public IPoint getCellSize() { return cellSize; }

	/**
	 * Sets the scale.
	 *
	 * @param s
	 *            the new scale
	 */
	public void setScale(final Double s) { scale = s; }

	/**
	 * Returns the z-scaling factor for this field
	 *
	 * @return
	 */
	@Override
	public Double getScale() {
		if (scale == null) {
			Scaling3D size = getSize();
			return size == null ? 1d : size.getZ();
		}
		return scale;
	}

	/**
	 * A value > 1 to indicate a maximum; a value between 0 and 1 to indicate a scaling of the elevation values
	 *
	 * @return
	 */
	public double getZFactor() { return getSize().getZ(); }

	/**
	 * Sets the grayscaled.
	 *
	 * @param grayScaled2
	 *            the new grayscaled
	 */
	public void setGrayscaled(final Boolean grayScaled2) {
		if (colorProvider == null) { colorProvider = new GrayscaleMeshColorProvider(); }
		setFlag(Flag.Grayscaled, grayScaled2);
	}

	/**
	 * Sets the triangulated.
	 *
	 * @param triangulated2
	 *            the new triangulated
	 */
	public void setTriangulated(final Boolean triangulated2) {
		setFlag(Flag.Triangulated, triangulated2);
	}

	/**
	 * Sets the with text.
	 *
	 * @param showText
	 *            the new with text
	 */
	public void setWithText(final Boolean showText) {
		setFlag(Flag.WithText, showText);
	}

	/**
	 * Sets the smooth. Reinitialized the smooth provider
	 *
	 * @param smooth
	 *            the new smooth
	 */
	public void setSmooth(final int smooth) {
		this.smooth = smooth;
		smoothProvider = SMOOTH_METHOD_FOR(smooth, noData);
	}

	/**
	 * Gets the smooth.
	 *
	 * @return the smooth
	 */
	@Override
	public int getSmooth() { return smooth; }

	/**
	 * Sets the no data.
	 *
	 * @param noData
	 *            the new no data
	 */
	public void setNoData(final double noData) {
		this.noData = noData;
		smoothProvider = SMOOTH_METHOD_FOR(smooth, noData);
	}

	/**
	 * Gets the no data value.
	 *
	 * @return the no data value
	 */
	@Override
	public double getNoDataValue() { return noData; }

	/**
	 * Sets the above.
	 *
	 * @param above2
	 *            the new above
	 */
	public void setAbove(final double above2) { above = above2; }

	/**
	 * Gets the above.
	 *
	 * @return the above
	 */
	@Override
	public Double getAbove() { return above; }

	/**
	 * Gets the smooth provider.
	 *
	 * @return the smooth provider
	 */
	@Override
	public IMeshSmoothProvider getSmoothProvider() { return smoothProvider; }

}