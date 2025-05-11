/*******************************************************************************************************
 *
 * XmlParagraphSegment.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls.text;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.forms.widgets.Locator;
import org.eclipse.ui.internal.forms.widgets.SelectionData;

import gama.core.util.GamaFont;
import gama.ui.shared.resources.GamaFonts;

/**
 * The Class XmlParagraphSegment.
 */
public abstract class XmlParagraphSegment implements IXmlFontUser {

	/** The font. */
	GamaFont font;

	/**
	 * Instantiates a new xml paragraph segment.
	 *
	 * @param fontToUse
	 *            the font to use
	 */
	public XmlParagraphSegment(final GamaFont fontToUse) {
		this.font = fontToUse;
	}

	@Override
	public Font getFont() { return GamaFonts.getFont(font); }

	/**
	 * Moves the locator according to the content of this segment.
	 *
	 * @return <code>true</code> if text wrapped to the new line, <code>false</code> otherwise.
	 */
	public abstract boolean advanceLocator(GC gc, int wHint, Locator loc, boolean computeHeightOnly);

	/**
	 * Computes bounding rectangles and row heights of this segments.
	 */
	public abstract void layout(GC gc, int width, Locator loc, boolean selected);

	/**
	 * Paints this segment.
	 */
	public abstract void paint(GC gc, boolean hover, boolean selected, SelectionData selData, Rectangle region);

	/**
	 * Paints this segment.
	 */
	public abstract void computeSelection(GC gc, SelectionData selData);

	/**
	 * Tests if the coordinates are contained in one of the bounding rectangles of this segment.
	 *
	 * @return true if inside the bounding rectangle, false otherwise.
	 */
	public abstract boolean contains(int x, int y);

	/**
	 * Tests if the source rectangle intersects with one of the bounding rectangles of this segment.
	 *
	 * @return true if the two rectangles intersect, false otherwise.
	 */
	public abstract boolean intersects(Rectangle rect);

	/**
	 * Returns the tool tip of this segment or <code>null</code> if not defined.
	 *
	 * @return tooltip or <code>null</code>.
	 */
	public String getTooltipText() { return null; }

	/**
	 * Clears the text metrics cache for the provided font id.
	 *
	 * @param fontId
	 *            the id of the font that the cache is kept for.
	 */
	public void clearCache(final String fontId) {}
}