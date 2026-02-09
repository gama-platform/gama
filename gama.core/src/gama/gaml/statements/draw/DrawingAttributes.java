/*******************************************************************************************************
 *
 * DrawingAttributes.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import gama.api.data.factories.GamaColorFactory;
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.gaml.constants.GamlCoreConstants;
import gama.api.kernel.agent.IAgent;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.IDrawingAttributes.Flag;
import gama.api.utils.IImageProvider;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.Scaling3D;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;

/**
 * The Class DrawingAttributes.
 */
public class DrawingAttributes implements IDrawingAttributes {

	static {
		DEBUG.OFF();
	}

	/** The Constant TEXTURED_COLOR. */
	protected IColor TEXTURED_COLOR = GamaColorFactory.WHITE;
	/** The Constant SELECTED_COLOR. */
	protected IColor SELECTED_COLOR = GamaColorFactory.RED;

	/** The index. */
	private static int INDEX = 0;

	/** The flags. */
	protected EnumSet<Flag> flags = EnumSet.of(Flag.Lighted);

	/** The unique index. */
	private final int uniqueIndex;

	/** The location. */
	protected IPoint location;

	/** The size. */
	protected Scaling3D size;

	/** The rotation. */
	protected AxisAngle rotation;

	/** The line width. */
	Double depth = null, lineWidth = GamaPreferences.Displays.CORE_LINE_WIDTH.getValue();

	/** The type. */
	public IShape.Type type;

	/** The border. */
	protected IColor fill, highlight, border;

	/** The textures. */
	protected List<?> textures;

	/** The material. */
	// GamaMaterial material;

	/**
	 * Instantiates a new drawing attributes.
	 */
	private DrawingAttributes() {
		uniqueIndex = INDEX++;

	}

	/**
	 * Instantiates a new drawing attributes.
	 *
	 * @param size
	 *            the size
	 * @param rotation
	 *            the rotation
	 * @param location
	 *            the location
	 * @param color
	 *            the color
	 * @param border
	 *            the border
	 * @param lighting
	 *            the lighting
	 */
	public DrawingAttributes(final Scaling3D size, final AxisAngle rotation, final IPoint location, final IColor color,
			final IColor border, final Boolean lighting) {
		this();
		setBorder(border);
		setFill(color);
		setSize(size);
		setLocation(location == null ? null : GamaPointFactory.create(location));
		setRotation(rotation);
		setLighting(lighting);
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	@Override
	public int getIndex() { return uniqueIndex; }

	/**
	 * Sets the synthetic.
	 *
	 * @param s
	 *            the new synthetic
	 */
	@Override
	public void setSynthetic(final boolean s) {
		setFlag(Flag.Synthetic, s);
	}

	/**
	 * Checks if is synthetic.
	 *
	 * @return true, if is synthetic
	 */
	@Override
	public boolean isSynthetic() { return isSet(Flag.Synthetic); }

	/**
	 * Sets the lighting.
	 *
	 * @param lighting
	 *            the new lighting
	 */
	@Override
	public void setLighting(final Boolean lighting) {
		if (lighting == null) return;
		setFlag(Flag.Lighted, lighting);
	}

	/**
	 * Sets the empty.
	 *
	 * @param b
	 *            the new empty
	 */
	@Override
	public void setEmpty(final Boolean b) {
		if (b == null || !b) {
			setFilled();
		} else {
			setEmpty();
		}
	}

	/**
	 * Gets the agent identifier.
	 *
	 * @return the agent identifier
	 */
	@Override
	public IAgent getAgentIdentifier() { return null; }

	/**
	 * Gets the species name.
	 *
	 * @return the species name
	 */
	@Override
	public String getSpeciesName() { return null; }

	/**
	 * Returns the angle of the rotation in degrees (or null if no rotation is defined)
	 *
	 * @return
	 */
	@Override
	public Double getAngle() {
		if (getRotation() == null) return null;
		return getRotation().angle;
	}

	/**
	 * Sets the texture.
	 *
	 * @param o
	 *            the new texture
	 */
	@Override
	public void setTexture(final Object o) {
		if (o == null) {
			setTextures(null);
		} else {
			setTextures(Collections.singletonList(o));
		}
	}

	/**
	 * Mark selected.
	 *
	 * @param pickedIndex
	 *            the picked index
	 */
	@Override
	public void markSelected(final int pickedIndex) {
		setSelected(pickedIndex == uniqueIndex);
	}

	/**
	 * Gets the anchor.
	 *
	 * @return the anchor
	 */
	@Override
	public IPoint getAnchor() { return GamlCoreConstants.bottom_left; }

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	@Override
	public IPoint getLocation() { return location; }

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	@Override
	public Scaling3D getSize() { return size; }

	/**
	 * Gets the depth.
	 *
	 * @return the depth
	 */
	@Override
	public Double getDepth() { return depth; }

	/**
	 * Sets the line width.
	 *
	 * @param d
	 *            the new line width
	 */
	@Override
	public void setLineWidth(final Double d) {
		if (d == null) {
			lineWidth = GamaPreferences.Displays.CORE_LINE_WIDTH.getValue();
		} else {
			lineWidth = d;
		}
	}

	/**
	 * Gets the line width.
	 *
	 * @return the line width
	 */
	@Override
	public Double getLineWidth() { return lineWidth; }

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	@Override
	public IShape.Type getType() { return type; }

	/**
	 * Use cache.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean useCache() {
		return isSet(Flag.UseCache);
	}

	/**
	 * Sets the use cache.
	 *
	 * @param b
	 *            the new use cache
	 */
	@Override
	public void setUseCache(final boolean b) {
		setFlag(Flag.UseCache, b);
	}

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	@Override
	public void setType(final IShape.Type type) { this.type = type; }

	/**
	 * Gets the rotation.
	 *
	 * @return the rotation
	 */
	@Override
	public AxisAngle getRotation() { return rotation; }

	/**
	 * Sets the location.
	 *
	 * @param loc
	 *            the new location
	 */
	@Override
	public void setLocation(final IPoint loc) { location = loc; }

	/**
	 * Sets the size.
	 *
	 * @param size
	 *            the new size
	 */
	@Override
	public void setSize(final Scaling3D size) { this.size = size; }

	/**
	 * Sets the rotation.
	 *
	 * @param rotation
	 *            the new rotation
	 */
	@Override
	public void setRotation(final AxisAngle rotation) {
		if (rotation == null) return;
		this.rotation = rotation;
	}

	/**
	 * Sets the height.
	 *
	 * @param depth
	 *            the new height
	 */
	@Override
	public void setHeight(final Double depth) {
		if (depth == null) return;
		this.depth = depth;
	}

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	@Override
	public IColor getColor() {
		if (isSelected()) // DEBUG.OUT("Selected agent: " + getAgentIdentifier() + " / index : " + uniqueIndex);
			return SELECTED_COLOR;
		if (highlight != null) return highlight;
		if (isSet(Flag.Empty)) return null;
		if (fill == null) {
			if (textures != null) return TEXTURED_COLOR;
			// Always returns the color as we are solid; so null cannot be an option
			// see issue #2724
			return GamaPreferences.Displays.CORE_COLOR.getValue();
			// }
			// return null;
		}
		return fill;
	}

	/**
	 * Gets the border.
	 *
	 * @return the border
	 */
	@Override
	public IColor getBorder() {
		if (isSet(Flag.Empty) && border == null) return fill;
		return border;
	}

	/**
	 * Sets the empty.
	 */
	@Override
	public void setEmpty() {
		setFlag(Flag.Empty, true);
	}

	/**
	 * Sets the filled.
	 */
	@Override
	public void setFilled() {
		setFlag(Flag.Empty, false);
	}

	/**
	 * Sets the fill.
	 *
	 * @param color
	 *            the new fill
	 */
	@Override
	public void setFill(final IColor color) { fill = color; }

	/**
	 * Sets the border.
	 *
	 * @param border
	 *            the new border
	 */
	@Override
	public void setBorder(final IColor border) { this.border = border; }

	/**
	 * Sets the lighting.
	 *
	 * @param lighting
	 *            the new lighting
	 */
	void setLighting(final boolean lighting) {
		setFlag(Flag.Lighted, lighting);
	}

	/**
	 * Sets the no border.
	 */
	@Override
	public void setNoBorder() {
		border = null;
	}

	/**
	 * Sets the textures.
	 *
	 * @param textures
	 *            the new textures
	 */
	@Override
	public void setTextures(final List<?> textures) { this.textures = textures; }

	/**
	 * Gets the textures.
	 *
	 * @return the textures
	 */
	@Override
	public List getTextures() { return textures; }

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	@Override
	public boolean isEmpty() { return isSet(Flag.Empty); }

	/**
	 * Checks if is animated.
	 *
	 * @return true, if is animated
	 */
	@Override
	public boolean isAnimated() {
		if (!useCache()) return true;
		if (textures == null) return false;
		final Object o = textures.get(0);
		if (!(o instanceof IImageProvider iip)) return false;
		return iip.isAnimated();
	}

	// /**
	// * Gets the frame count.
	// *
	// * @return the frame count
	// */
	// public int getFrameCount() {
	// if (textures == null) return 1;
	// final Object o = textures.get(0);
	// if (!(o instanceof GamaGifFile)) return 1;
	// return ((GamaGifFile) o).getFrameCount();
	//
	// }
	//
	// /**
	// * Gets the average delay.
	// *
	// * @return the average delay
	// */
	// public int getAverageDelay() {
	// if (textures == null) return 0;
	// final Object o = textures.get(0);
	// if (!(o instanceof GamaGifFile)) return 0;
	// return ((GamaGifFile) o).getAverageDelay();
	//
	// }

	/**
	 * Checks if is lighting.
	 *
	 * @return true, if is lighting
	 */
	@Override
	public boolean isLighting() { return isSet(Flag.Lighted); }

	/**
	 * Sets the highlighted.
	 *
	 * @param color
	 *            the new highlighted
	 */
	@Override
	public void setHighlighted(final IColor color) { highlight = color; }

	/**
	 * Checks if is selected.
	 *
	 * @return true, if is selected
	 */
	@Override
	public boolean isSelected() { return isSet(Flag.Selected); }

	/**
	 * Sets the selected.
	 *
	 * @param b
	 *            the new selected
	 */
	@Override
	public void setSelected(final boolean b) {
		setFlag(Flag.Selected, b);
	}

	/**
	 * Sets the material.
	 *
	 * @param m
	 *            the new material
	 */
	// public void setMaterial(final GamaMaterial m) {
	// material = m;
	//
	// }

	/**
	 * Checks if is sets the.
	 *
	 * @param value
	 *            the value
	 * @return true, if is sets the
	 */
	@Override
	public boolean isSet(final Flag value) {
		return flags.contains(value);
	}

	/**
	 * Sets the flag.
	 *
	 * @param value
	 *            the value
	 * @param b
	 *            the b
	 */
	@Override
	public void setFlag(final Flag value, final boolean b) {
		if (b) {
			flags.add(value);
		} else {
			flags.remove(value);
		}
	}

}