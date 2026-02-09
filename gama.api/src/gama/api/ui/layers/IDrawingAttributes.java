/*******************************************************************************************************
 *
 * IDrawingAttributes.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

import java.util.List;

import gama.api.data.factories.GamaPointFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IField;
import gama.api.data.objects.IFont;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.kernel.agent.IAgent;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.Scaling3D;

/**
 *
 */
public interface IDrawingAttributes {

	/** An implementation that does not change the data. */
	IMeshSmoothProvider NULL = (cols, rows, data, noData, passes) -> data;

	/**
	 * The Enum Flag.
	 */
	public enum Flag {
		/** The Empty. */
		Empty,
		/** The Selected. */
		Selected,
		/** The Synthetic. */
		Synthetic,
		/** The Lighted. */
		Lighted,
		/** The Use cache. */
		UseCache,
		/** The Grayscaled. */
		Grayscaled,
		/** The Triangulated. */
		Triangulated,
		/** The With text. */
		WithText,
		/** The Perspective. */
		Perspective
	}

	/**
	 * The Enum DrawerType.
	 */
	public enum DrawerType {
		/** The geometry. */
		GEOMETRY,
		/** The string. */
		STRING,
		/** The mesh. */
		MESH,
		/** The resource. */
		RESOURCE
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	int getIndex();

	/**
	 * Sets the synthetic.
	 *
	 * @param s
	 *            the new synthetic
	 */
	void setSynthetic(boolean s);

	/**
	 * Checks if is synthetic.
	 *
	 * @return true, if is synthetic
	 */
	boolean isSynthetic();

	/**
	 * Sets the lighting.
	 *
	 * @param lighting
	 *            the new lighting
	 */
	void setLighting(Boolean lighting);

	/**
	 * Sets the empty.
	 *
	 * @param b
	 *            the new empty
	 */
	void setEmpty(Boolean b);

	/**
	 * Gets the agent identifier.
	 *
	 * @return the agent identifier
	 */
	IAgent getAgentIdentifier();

	/**
	 * Gets the species name.
	 *
	 * @return the species name
	 */
	String getSpeciesName();

	/**
	 * Returns the angle of the rotation in degrees (or null if no rotation is defined)
	 *
	 * @return
	 */
	Double getAngle();

	/**
	 * Sets the texture.
	 *
	 * @param o
	 *            the new texture
	 */
	void setTexture(Object o);

	/**
	 * Mark selected.
	 *
	 * @param pickedIndex
	 *            the picked index
	 */
	void markSelected(int pickedIndex);

	/**
	 * Gets the anchor.
	 *
	 * @return the anchor
	 */
	IPoint getAnchor();

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	IPoint getLocation();

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	Scaling3D getSize();

	/**
	 * Gets the depth.
	 *
	 * @return the depth
	 */
	Double getDepth();

	/**
	 * Sets the line width.
	 *
	 * @param d
	 *            the new line width
	 */
	void setLineWidth(Double d);

	/**
	 * Gets the line width.
	 *
	 * @return the line width
	 */
	Double getLineWidth();

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	IShape.Type getType();

	/**
	 * Use cache.
	 *
	 * @return true, if successful
	 */
	boolean useCache();

	/**
	 * Sets the use cache.
	 *
	 * @param b
	 *            the new use cache
	 */
	void setUseCache(boolean b);

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	void setType(IShape.Type type);

	/**
	 * Gets the rotation.
	 *
	 * @return the rotation
	 */
	AxisAngle getRotation();

	/**
	 * Sets the location.
	 *
	 * @param loc
	 *            the new location
	 */
	void setLocation(IPoint loc);

	/**
	 * Sets the size.
	 *
	 * @param size
	 *            the new size
	 */
	void setSize(Scaling3D size);

	/**
	 * Sets the rotation.
	 *
	 * @param rotation
	 *            the new rotation
	 */
	void setRotation(AxisAngle rotation);

	/**
	 * Sets the height.
	 *
	 * @param depth
	 *            the new height
	 */
	void setHeight(Double depth);

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	IColor getColor();

	/**
	 * Gets the border.
	 *
	 * @return the border
	 */
	IColor getBorder();

	/**
	 * Sets the empty.
	 */
	void setEmpty();

	/**
	 * Sets the filled.
	 */
	void setFilled();

	/**
	 * Sets the fill.
	 *
	 * @param color
	 *            the new fill
	 */
	void setFill(IColor color);

	/**
	 * Sets the border.
	 *
	 * @param border
	 *            the new border
	 */
	void setBorder(IColor border);

	/**
	 * Sets the no border.
	 */
	void setNoBorder();

	/**
	 * Sets the textures.
	 *
	 * @param textures
	 *            the new textures
	 */
	void setTextures(List<?> textures);

	/**
	 * Gets the textures.
	 *
	 * @return the textures
	 */
	List getTextures();

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	boolean isEmpty();

	/**
	 * Checks if is animated.
	 *
	 * @return true, if is animated
	 */
	boolean isAnimated();

	/**
	 * Checks if is lighting.
	 *
	 * @return true, if is lighting
	 */
	boolean isLighting();

	/**
	 * Sets the highlighted.
	 *
	 * @param color
	 *            the new highlighted
	 */
	void setHighlighted(IColor color);

	/**
	 * Checks if is selected.
	 *
	 * @return true, if is selected
	 */
	boolean isSelected();

	/**
	 * Sets the selected.
	 *
	 * @param b
	 *            the new selected
	 */
	void setSelected(boolean b);

	/**
	 * Checks if is sets the.
	 *
	 * @param value
	 *            the value
	 * @return true, if is sets the
	 */
	boolean isSet(Flag value);

	/**
	 * Sets the flag.
	 *
	 * @param value
	 *            the value
	 * @param b
	 *            the b
	 */
	void setFlag(Flag value, boolean b);

	/**
	 * @return
	 */
	default IFont getFont() { return null; }

	/**
	 * @return
	 */
	default IMeshColorProvider getColorProvider() { return null; }

	/**
	 * @param iPoint
	 * @return
	 */
	default IDrawingAttributes copyTranslatedBy(final IPoint iPoint) {
		return this;
	}

	/**
	 * @return
	 */
	default IPoint getXYDimension() { return GamaPointFactory.getNullPoint(); }

	/**
	 * @return
	 */
	default boolean isGrayscaled() { return isSet(Flag.Grayscaled); }

	/**
	 * @return
	 */
	default double getNoDataValue() { return IField.NO_NO_DATA; }

	/**
	 * @return
	 */
	default Double getAbove() { return 0d; }

	/**
	 * @return
	 */
	default boolean isWithText() { return isSet(Flag.WithText); }

	/**
	 * @return
	 */
	default boolean isTriangulated() { return isSet(Flag.Triangulated); }

	/**
	 * @return
	 */
	default Double getScale() { return 1d; }

	/**
	 * @return
	 */
	default IMeshSmoothProvider getSmoothProvider() { return NULL; }

	/**
	 * @return
	 */
	default boolean isPerspective() { return isSet(Flag.Perspective); }

	/**
	 * @return
	 */
	default Double getPrecision() { return 0d; }

	/**
	 * @return
	 */
	default int getSmooth() { return 0; }

}