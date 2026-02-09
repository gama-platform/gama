/*******************************************************************************************************
 *
 * TextDrawingAttributes.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import gama.api.data.objects.IColor;
import gama.api.data.objects.IFont;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape.Type;
import gama.api.ui.layers.IDrawingAttributes.Flag;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.Scaling3D;

/**
 * The Class TextDrawingAttributes.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class TextDrawingAttributes extends DrawingAttributes implements Cloneable {

	/** The font. */
	private IFont font;

	/** The anchor. */
	public IPoint anchor;

	/** The precision. */
	public Double precision;

	/**
	 * Instantiates a new text drawing attributes.
	 *
	 * @param size
	 *            the size
	 * @param rotation
	 *            the rotation
	 * @param location
	 *            the location
	 * @param color
	 *            the color
	 */
	public TextDrawingAttributes(final Scaling3D size, final AxisAngle rotation, final IPoint location,
			final IColor color) {
		super(size, rotation, location, color, null, null);
		setFlag(Flag.Perspective, true); // by default
		setType(Type.POLYGON);
	}

	/**
	 * Sets the perspective.
	 *
	 * @param perspective
	 *            the new perspective
	 */
	public void setPerspective(final Boolean perspective) {
		setFlag(Flag.Perspective, perspective == null ? true : perspective.booleanValue());
	}

	@Override
	public IColor getColor() {
		if (isEmpty() && fill == null && border != null) return border;
		return super.getColor();
	}

	/**
	 * Sets the anchor.
	 *
	 * @param anchor
	 *            the new anchor
	 */
	public void setAnchor(final IPoint anchor) { this.anchor = anchor; }

	@Override
	public IPoint getAnchor() {
		if (anchor == null) return super.getAnchor();
		return anchor;
	}

	/**
	 * Gets the font.
	 *
	 * @return the font
	 */
	@Override
	public IFont getFont() { return font; }

	/**
	 * Sets the font.
	 *
	 * @param font
	 *            the new font
	 */
	public void setFont(final IFont font) { this.font = font; }

	@Override
	public Double getDepth() { return depth == null ? 0d : depth; }

	/**
	 * Copy translated by.
	 *
	 * @param p
	 *            the p
	 * @return the text drawing attributes
	 */
	@Override
	public TextDrawingAttributes copyTranslatedBy(final IPoint p) {
		try {
			TextDrawingAttributes clone = (TextDrawingAttributes) this.clone();
			clone.setLocation(getLocation().plus(p));
			return clone;
		} catch (CloneNotSupportedException e) {}
		return new TextDrawingAttributes(getSize(), getRotation(), getLocation().plus(p), getColor());
	}

	/**
	 * Sets the precision.
	 *
	 * @param prec
	 *            the new precision
	 */
	public void setPrecision(final Double prec) { precision = Math.min(1, Math.max(0, prec)); }

	/**
	 * Gets the precision.
	 *
	 * @return the precision
	 */
	public Double getPrecision() { return precision; }

}