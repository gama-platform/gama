/*******************************************************************************************************
 *
 * XmlBreakSegment.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls.text;

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.forms.widgets.Locator;
import org.eclipse.ui.internal.forms.widgets.SelectionData;

/**
 * This segment serves as break within a paragraph. It has no data - just starts a new line and resets the locator.
 */

public class XmlBreakSegment extends XmlParagraphSegment {

	/**
	 * Advance locator.
	 *
	 * @param gc
	 *            the gc
	 * @param wHint
	 *            the w hint
	 * @param locator
	 *            the locator
	 * @param objectTable
	 *            the object table
	 * @param computeHeightOnly
	 *            the compute height only
	 * @return true, if successful
	 */
	@Override
	public boolean advanceLocator(final GC gc, final int wHint, final Locator locator,
			final boolean computeHeightOnly) {
		if (locator.rowHeight == 0) {
			FontMetrics fm = gc.getFontMetrics();
			locator.rowHeight = fm.getHeight();
		}
		if (computeHeightOnly) { locator.collectHeights(); }
		locator.resetCaret();
		locator.width = locator.x;
		locator.y += locator.rowHeight;
		locator.rowHeight = 0;
		locator.leading = 0;
		return true;
	}

	/**
	 * Paint.
	 *
	 * @param gc
	 *            the gc
	 * @param hover
	 *            the hover
	 * @param resourceTable
	 *            the resource table
	 * @param selected
	 *            the selected
	 * @param selData
	 *            the sel data
	 * @param repaintRegion
	 *            the repaint region
	 */
	@Override
	public void paint(final GC gc, final boolean hover, final boolean selected, final SelectionData selData,
			final Rectangle repaintRegion) {
		// nothing to paint
	}

	/**
	 * Contains.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return true, if successful
	 */
	@Override
	public boolean contains(final int x, final int y) {
		return false;
	}

	/**
	 * Intersects.
	 *
	 * @param rect
	 *            the rect
	 * @return true, if successful
	 */
	@Override
	public boolean intersects(final Rectangle rect) {
		return false;
	}

	/**
	 * Layout.
	 *
	 * @param gc
	 *            the gc
	 * @param width
	 *            the width
	 * @param locator
	 *            the locator
	 * @param ResourceTable
	 *            the resource table
	 * @param selected
	 *            the selected
	 */
	@Override
	public void layout(final GC gc, final int width, final Locator locator, final boolean selected) {
		locator.resetCaret();
		if (locator.rowHeight == 0) {
			FontMetrics fm = gc.getFontMetrics();
			locator.rowHeight = fm.getHeight();
		}
		locator.y += locator.rowHeight;
		locator.rowHeight = 0;
		locator.rowCounter++;
	}

	/**
	 * Compute selection.
	 *
	 * @param gc
	 *            the gc
	 * @param resourceTable
	 *            the resource table
	 * @param selData
	 *            the sel data
	 */
	@Override
	public void computeSelection(final GC gc, final SelectionData selData) {}
}
