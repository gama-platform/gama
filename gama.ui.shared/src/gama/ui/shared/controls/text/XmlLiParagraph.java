/*******************************************************************************************************
 *
 * XmlLiParagraph.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls.text;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment;
import org.eclipse.ui.internal.forms.widgets.Locator;
import org.eclipse.ui.internal.forms.widgets.SelectionData;

/**
 * The Class XmlLiParagraph.
 */
public class XmlLiParagraph extends XmlParagraph {

	/** The Constant CIRCLE. */
	public static final int CIRCLE = 1;

	/** The Constant TEXT. */
	public static final int TEXT = 2;

	/** The Constant IMAGE. */
	public static final int IMAGE = 3;

	/** The style. */
	private int style = CIRCLE;

	/** The text. */
	private String text;

	/** The circle diam. */
	private final int CIRCLE_DIAM = 5;

	/** The spacing. */
	private final int SPACING = 10;

	/** The indent. */
	private int indent = -1;

	/** The bindent. */
	private int bindent = -1;

	/** The bbounds. */
	private Rectangle bbounds;

	/**
	 * Constructor for BulletParagraph.
	 */
	public XmlLiParagraph(final boolean addVerticalSpace) {
		super(addVerticalSpace);
	}

	@Override
	public int getIndent() {
		int ivalue = indent;
		if (ivalue != -1) return ivalue;
		ivalue = switch (style) {
			case CIRCLE -> CIRCLE_DIAM + SPACING;
			default -> 20;
		};
		return getBulletIndent() + ivalue;
	}

	/**
	 * Gets the bullet indent.
	 *
	 * @return the bullet indent
	 */
	public int getBulletIndent() {
		if (bindent != -1) return bindent;
		return 0;
	}

	/**
	 * Gets the bullet style.
	 *
	 * @return the bullet style
	 */
		/*
		 * @see IBulletParagraph#getBulletStyle()
		 */
	public int getBulletStyle() { return style; }

	/**
	 * Sets the bullet style.
	 *
	 * @param style
	 *            the new bullet style
	 */
	public void setBulletStyle(final int style) { this.style = style; }

	/**
	 * Sets the bullet text.
	 *
	 * @param text
	 *            the new bullet text
	 */
	public void setBulletText(final String text) { this.text = text; }

	/**
	 * Sets the indent.
	 *
	 * @param indent
	 *            the new indent
	 */
	public void setIndent(final int indent) { this.indent = indent; }

	/**
	 * Sets the bullet indent.
	 *
	 * @param bindent
	 *            the new bullet indent
	 */
	public void setBulletIndent(final int bindent) { this.bindent = bindent; }

	/**
	 * Gets the bullet text.
	 *
	 * @return the bullet text
	 */
	public String getBulletText() { return text; }

	/**
	 * Layout.
	 *
	 * @param gc
	 *            the gc
	 * @param width
	 *            the width
	 * @param loc
	 *            the loc
	 * @param lineHeight
	 *            the line height
	 * @param resourceTable
	 *            the resource table
	 * @param selectedLink
	 *            the selected link
	 */
	@Override
	public void layout(final GC gc, final int width, final Locator loc, final int lineHeight,
			final IHyperlinkSegment selectedLink) {
		computeRowHeights(gc, width, loc, lineHeight);
		layoutBullet(gc, loc, lineHeight);
		super.layout(gc, width, loc, lineHeight, selectedLink);
	}

	/**
	 * Paint.
	 *
	 * @param gc
	 *            the gc
	 * @param repaintRegion
	 *            the repaint region
	 * @param resourceTable
	 *            the resource table
	 * @param selectedLink
	 *            the selected link
	 * @param selData
	 *            the sel data
	 */
	@Override
	public void paint(final GC gc, final Rectangle repaintRegion, final IHyperlinkSegment selectedLink,
			final SelectionData selData) {
		paintBullet(gc, repaintRegion);
		super.paint(gc, repaintRegion, selectedLink, selData);
	}

	/**
	 * Layout bullet.
	 *
	 * @param gc
	 *            the gc
	 * @param loc
	 *            the loc
	 * @param lineHeight
	 *            the line height
	 * @param resourceTable
	 *            the resource table
	 */
	private void layoutBullet(final GC gc, final Locator loc, final int lineHeight) {
		int x = loc.x - getIndent() + getBulletIndent();
		int rowHeight = loc.heights.get(0)[0];
		if (style == CIRCLE) {
			int y = loc.y + rowHeight / 2 - CIRCLE_DIAM / 2;
			bbounds = new Rectangle(x, y, CIRCLE_DIAM, CIRCLE_DIAM);
		} else if (style == TEXT && text != null) {
			// int height = gc.getFontMetrics().getHeight();
			Point textSize = gc.textExtent(text);
			bbounds = new Rectangle(x, loc.y, textSize.x, textSize.y);
		}
	}

	/**
	 * Paint bullet.
	 *
	 * @param gc
	 *            the gc
	 * @param repaintRegion
	 *            the repaint region
	 * @param resourceTable
	 *            the resource table
	 */
	public void paintBullet(final GC gc, final Rectangle repaintRegion) {
		if (bbounds == null) return;
		int x = bbounds.x;
		int y = bbounds.y;
		if (repaintRegion != null) {
			x -= repaintRegion.x;
			y -= repaintRegion.y;
		}
		if (style == CIRCLE) {
			Color bg = gc.getBackground();
			Color fg = gc.getForeground();
			gc.setBackground(fg);
			gc.fillRectangle(x, y + 1, 5, 3);
			gc.fillRectangle(x + 1, y, 3, 5);
			gc.setBackground(bg);
		} else if (style == TEXT && text != null) { gc.drawText(text, x, y); }
	}
}
