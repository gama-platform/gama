/*******************************************************************************************************
 *
 * XmlTextSegment.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls.text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.internal.forms.widgets.Locator;
import org.eclipse.ui.internal.forms.widgets.SelectionData;

/**
 * @version 1.0
 */
public class XmlTextSegment extends XmlParagraphSegment {

	/** The text. */
	private String text;

	/** The underline. */
	protected boolean underline;

	/** The wrap allowed. */
	private boolean wrapAllowed = true;

	/** The area rectangles. */
	protected Vector<AreaRectangle> areaRectangles = new Vector<>();

	/** The text fragments. */
	private TextFragment[] textFragments;

	/**
	 * The Class AreaRectangle.
	 */
	class AreaRectangle {

		/** The rect. */
		Rectangle rect;

		/** The to. */
		int from, to;

		/**
		 * Instantiates a new area rectangle.
		 *
		 * @param rect
		 *            the rect
		 * @param from
		 *            the from
		 * @param to
		 *            the to
		 */
		public AreaRectangle(final Rectangle rect, final int from, final int to) {
			this.rect = rect;
			this.from = from;
			this.to = to;
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
		public boolean contains(final int x, final int y) {
			return rect.contains(x, y);
		}

		/**
		 * Intersects.
		 *
		 * @param region
		 *            the region
		 * @return true, if successful
		 */
		public boolean intersects(final Rectangle region) {
			return rect.intersects(region);
		}

		/**
		 * Gets the text.
		 *
		 * @return the text
		 */
		public String getText() {
			if (from == 0 && to == -1) return XmlTextSegment.this.getText();
			if (from > 0 && to == -1) return XmlTextSegment.this.getText().substring(from);
			return XmlTextSegment.this.getText().substring(from, to);
		}
	}

	/**
	 * The Class SelectionRange.
	 */
	static class SelectionRange {

		/** The start. */
		public int start;

		/** The stop. */
		public int stop;

		/**
		 * Instantiates a new selection range.
		 */
		public SelectionRange() {
			reset();
		}

		/**
		 * Reset.
		 */
		public void reset() {
			start = -1;
			stop = -1;
		}
	}

	/**
	 * The Class TextFragment.
	 */
	static class TextFragment {

		/** The index. */
		short index;

		/** The length. */
		short length;

		/**
		 * Instantiates a new text fragment.
		 *
		 * @param index
		 *            the index
		 * @param length
		 *            the length
		 */
		public TextFragment(final short index, final short length) {
			this.index = index;
			this.length = length;
		}
	}

	/**
	 * Instantiates a new text segment.
	 *
	 * @param text
	 *            the text
	 * @param fontId
	 *            the font id
	 * @param colorId
	 *            the color id
	 */
	public XmlTextSegment(final String text, final boolean bold) {
		this(text, bold, true);
	}

	/**
	 * Instantiates a new text segment.
	 *
	 * @param text
	 *            the text
	 * @param fontId
	 *            the font id
	 * @param colorId
	 *            the color id
	 * @param wrapAllowed
	 *            the wrap allowed
	 */
	public XmlTextSegment(final String text, final boolean bold, final boolean wrapAllowed) {
		this.text = cleanup(text);
		this.wrapAllowed = wrapAllowed;
	}

	/**
	 * Cleanup.
	 *
	 * @param text
	 *            the text
	 * @return the string
	 */
	private String cleanup(final String text) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\n' || c == '\r' || c == '\f') {
				if (i > 0) { buf.append(' '); }
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	/**
	 * Sets the word wrap allowed.
	 *
	 * @param value
	 *            the new word wrap allowed
	 */
	public void setWordWrapAllowed(final boolean value) { wrapAllowed = value; }

	/**
	 * Checks if is word wrap allowed.
	 *
	 * @return true, if is word wrap allowed
	 */
	public boolean isWordWrapAllowed() { return wrapAllowed; }

	/**
	 * Checks if is selectable.
	 *
	 * @return true, if is selectable
	 */
	public boolean isSelectable() { return false; }

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getText() { return text; }

	/**
	 * Sets the text.
	 *
	 * @param text
	 *            the new text
	 */
	void setText(final String text) {
		this.text = cleanup(text);
		textFragments = null;
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
		for (int i = 0; i < areaRectangles.size(); i++) {
			AreaRectangle ar = areaRectangles.get(i);
			if (ar.contains(x, y)) return true;
			if (i < areaRectangles.size() - 1) {
				// test the gap
				Rectangle top = ar.rect;
				Rectangle bot = areaRectangles.get(i + 1).rect;
				if (y >= top.y + top.height && y < bot.y) {
					// in the gap
					int left = Math.max(top.x, bot.x);
					int right = Math.min(top.x + top.width, bot.x + bot.width);
					if (x >= left && x <= right) return true;
				}
			}
		}
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
		for (int i = 0; i < areaRectangles.size(); i++) {
			AreaRectangle ar = areaRectangles.get(i);
			if (ar.intersects(rect)) return true;
			if (i < areaRectangles.size() - 1) {
				// test the gap
				Rectangle top = ar.rect;
				Rectangle bot = areaRectangles.get(i + 1).rect;
				if (top.y + top.height < bot.y) {
					int y = top.y + top.height;
					int height = bot.y - y;
					int left = Math.max(top.x, bot.x);
					int right = Math.min(top.x + top.width, bot.x + bot.width);
					Rectangle gap = new Rectangle(left, y, right - left, height);
					if (gap.intersects(rect)) return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the bounds.
	 *
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		if (areaRectangles.isEmpty()) return new Rectangle(0, 0, 0, 0);

		AreaRectangle ar0 = areaRectangles.get(0);
		Rectangle bounds = Geometry.copy(ar0.rect);
		for (int i = 1; i < areaRectangles.size(); i++) {
			AreaRectangle ar = areaRectangles.get(i);
			bounds.add(ar.rect);
		}
		return bounds;
	}

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
		if (XmlTextModel.REGULAR_FONT != null) { gc.setFont(XmlTextModel.REGULAR_FONT); }
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		boolean newLine = false;

		if (wHint == SWT.DEFAULT || !wrapAllowed) {
			Point extent = gc.textExtent(text);
			int totalExtent = locator.x + extent.x;
			if (isSelectable()) { totalExtent += 1; }

			if (wHint != SWT.DEFAULT && totalExtent + locator.marginWidth > wHint) {
				// new line
				locator.resetCaret();
				locator.y += locator.rowHeight;
				if (computeHeightOnly) { locator.collectHeights(); }
				locator.rowHeight = 0;
				locator.leading = 0;
				newLine = true;
			}
			int width = extent.x;
			if (isSelectable()) { width += 1; }
			locator.x += width;
			locator.width = locator.x;
			locator.rowHeight = Math.max(locator.rowHeight, extent.y);
			locator.leading = Math.max(locator.leading, fm.getLeading());
			return newLine;
		}

		computeTextFragments(gc);

		int width = 0;
		Point lineExtent = new Point(0, 0);

		for (int i = 0; i < textFragments.length; i++) {
			TextFragment textFragment = textFragments[i];
			int currentExtent = locator.x + lineExtent.x;

			if (isSelectable()) { currentExtent += 1; }

			// i != 0 || locator.x > locator.getStartX() + (isSelectable() ? 1 : 0) means:
			// only wrap on the first fragment if we are not at the start of a line
			if ((i != 0 || locator.x > locator.getStartX() + (isSelectable() ? 1 : 0))
					&& currentExtent + textFragment.length > wHint) {
				// overflow
				int lineWidth = currentExtent;
				locator.rowHeight = Math.max(locator.rowHeight, lineExtent.y);
				locator.leading = Math.max(locator.leading, fm.getLeading());
				if (computeHeightOnly) { locator.collectHeights(); }
				locator.x = locator.indent;
				locator.y += locator.rowHeight;
				locator.rowHeight = 0;
				locator.leading = 0;
				lineExtent.x = 0;
				lineExtent.y = 0;
				width = Math.max(width, lineWidth);
				newLine = true;
			}
			lineExtent.x += textFragment.length;
			lineExtent.y = Math.max(lineHeight, lineExtent.y);
			width = Math.max(width, locator.x + lineExtent.x);
		}
		int lineWidth = lineExtent.x;
		if (isSelectable()) { lineWidth += 1; }
		locator.x += lineWidth;
		locator.width = width;
		locator.rowHeight = Math.max(locator.rowHeight, lineExtent.y);
		locator.leading = Math.max(locator.leading, fm.getLeading());
		return newLine;
	}

	/**
	 * Layout without wrapping.
	 *
	 * @param gc
	 *            the gc
	 * @param width
	 *            the width
	 * @param locator
	 *            the locator
	 * @param selected
	 *            the selected
	 * @param fm
	 *            the fm
	 * @param lineHeight
	 *            the line height
	 * @param descent
	 *            the descent
	 */
	private void layoutWithoutWrapping(final GC gc, final int width, final Locator locator, final boolean selected,
			final FontMetrics fm, final int lineHeight, final int descent) {
		Point extent = gc.textExtent(text);
		int ewidth = extent.x;
		if (isSelectable()) { ewidth += 1; }
		if (locator.x + ewidth > width - locator.marginWidth) {
			// new line
			locator.resetCaret();
			locator.y += locator.rowHeight;
			locator.rowHeight = 0;
			locator.rowCounter++;
		}
		int ly = locator.getBaseline(fm.getHeight() - fm.getLeading());
		// int lineY = ly + lineHeight - descent + 1;
		Rectangle br = new Rectangle(locator.x, ly, ewidth, lineHeight - descent + 3);
		areaRectangles.add(new AreaRectangle(br, 0, -1));
		locator.x += ewidth;
		locator.width = ewidth;
		locator.rowHeight = Math.max(locator.rowHeight, extent.y);
	}

	/**
	 * Convert offset to string index.
	 *
	 * @param gc
	 *            the gc
	 * @param s
	 *            the s
	 * @param x
	 *            the x
	 * @param swidth
	 *            the swidth
	 * @param selOffset
	 *            the sel offset
	 * @return the int
	 */
	protected int convertOffsetToStringIndex(final GC gc, final String s, final int x, int swidth,
			final int selOffset) {
		int index = s.length();
		while (index > 0 && x + swidth > selOffset) {
			index--;
			String ss = s.substring(0, index);
			swidth = gc.textExtent(ss).x;
		}
		return index;
	}

	/**
	 * Paint focus.
	 *
	 * @param gc
	 *            the gc
	 * @param bg
	 *            the bg
	 * @param fg
	 *            the fg
	 * @param selected
	 *            the selected
	 * @param repaintRegion
	 *            the repaint region
	 */
	public void paintFocus(final GC gc, final Color bg, final Color fg, final boolean selected,
			final Rectangle repaintRegion) {
		if (areaRectangles == null) return;
		for (AreaRectangle areaRectangle : areaRectangles) {
			Rectangle br = areaRectangle.rect;
			int bx = br.x;
			int by = br.y;
			if (repaintRegion != null) {
				bx -= repaintRegion.x;
				by -= repaintRegion.y;
			}
			if (selected) {
				gc.setBackground(bg);
				gc.setForeground(fg);
				gc.drawFocus(bx, by, br.width, br.height);
			} else {
				gc.setForeground(bg);
				gc.drawRectangle(bx, by, br.width - 1, br.height - 1);
			}
		}
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
		this.paint(gc, hover, selected, false, selData, repaintRegion);
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
	 * @param rollover
	 *            the rollover
	 * @param selData
	 *            the sel data
	 * @param repaintRegion
	 *            the repaint region
	 */
	protected void paint(final GC gc, final boolean hover, final boolean selected, final boolean rollover,
			final SelectionData selData, final Rectangle repaintRegion) {
		// apply segment-specific font, color and background
		if (XmlTextModel.REGULAR_FONT != null) { gc.setFont(XmlTextModel.REGULAR_FONT); }

		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		int descent = fm.getDescent();

		// paint area rectangles of the segment
		for (AreaRectangle areaRectangle : areaRectangles) {
			Rectangle rect = areaRectangle.rect;
			String text = areaRectangle.getText();
			Point extent = gc.textExtent(text);
			int textX = rect.x + (isSelectable() ? 1 : 0);
			int lineY = rect.y + lineHeight - descent + 1;
			paintString(gc, text, extent.x, textX, rect.y, lineY, selData, rect, hover, rollover, repaintRegion);
		}
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
	public void computeSelection(final GC gc, final SelectionData selData) {
		Font oldFont = null;

		if (XmlTextModel.REGULAR_FONT != null) { gc.setFont(XmlTextModel.REGULAR_FONT); }

		for (AreaRectangle areaRectangle : areaRectangles) {
			Rectangle rect = areaRectangle.rect;
			String text = areaRectangle.getText();
			Point extent = gc.textExtent(text);
			computeSelection(gc, text, extent.x, selData, rect);
		}
		// restore GC resources
		if (oldFont != null) { gc.setFont(oldFont); }
	}

	/**
	 * Paint string.
	 *
	 * @param gc
	 *            the gc
	 * @param s
	 *            the s
	 * @param swidth
	 *            the swidth
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param lineY
	 *            the line Y
	 * @param selData
	 *            the sel data
	 * @param bounds
	 *            the bounds
	 * @param hover
	 *            the hover
	 * @param rolloverMode
	 *            the rollover mode
	 * @param repaintRegion
	 *            the repaint region
	 */
	private void paintString(final GC gc, final String s, final int swidth, int x, final int y, final int lineY,
			final SelectionData selData, final Rectangle bounds, final boolean hover, final boolean rolloverMode,
			final Rectangle repaintRegion) {
		// repaints one area rectangle
		if (selData != null && selData.isEnclosed()) {
			Color savedBg = gc.getBackground();
			Color savedFg = gc.getForeground();
			int leftOffset = selData.getLeftOffset(bounds.height);
			int rightOffset = selData.getRightOffset(bounds.height);
			boolean firstRow = selData.isFirstSelectionRow(bounds.y, bounds.height);
			boolean lastRow = selData.isLastSelectionRow(bounds.y, bounds.height);
			boolean selectedRow = selData.isSelectedRow(bounds.y, bounds.height);

			int sstart = -1;
			int sstop = -1;

			if (firstRow && x + swidth < leftOffset || lastRow && x > rightOffset) {
				paintStringSegment(gc, s, gc.textExtent(s).x, x, y, lineY, hover, rolloverMode, repaintRegion);
				return;
			}

			if (firstRow && bounds.x + swidth > leftOffset) {
				sstart = convertOffsetToStringIndex(gc, s, bounds.x, swidth, leftOffset);
			}
			if (lastRow && bounds.x + swidth > rightOffset) {
				sstop = convertOffsetToStringIndex(gc, s, bounds.x, swidth, rightOffset);
			}

			if (firstRow && sstart != -1) {
				String left = s.substring(0, sstart);
				int width = gc.textExtent(left).x;
				paintStringSegment(gc, left, width, x, y, lineY, hover, rolloverMode, repaintRegion);
				x += width;
			}
			if (selectedRow) {
				int lindex = sstart != -1 ? sstart : 0;
				int rindex = sstop != -1 ? sstop : s.length();
				String mid = s.substring(lindex, rindex);
				Point extent = gc.textExtent(mid);
				gc.setForeground(selData.fg);
				gc.setBackground(selData.bg);
				gc.fillRectangle(x, y, extent.x, extent.y);
				paintStringSegment(gc, mid, extent.x, x, y, lineY, hover, rolloverMode, repaintRegion);
				x += extent.x;
				gc.setForeground(savedFg);
				gc.setBackground(savedBg);
			} else {
				paintStringSegment(gc, s, gc.textExtent(s).x, x, y, lineY, hover, rolloverMode, repaintRegion);
			}
			if (lastRow && sstop != -1) {
				String right = s.substring(sstop);
				paintStringSegment(gc, right, gc.textExtent(right).x, x, y, lineY, hover, rolloverMode, repaintRegion);
			}
		} else {
			paintStringSegment(gc, s, gc.textExtent(s).x, x, y, lineY, hover, rolloverMode, repaintRegion);
		}
	}

	/**
	 * Compute selection.
	 *
	 * @param gc
	 *            the gc
	 * @param s
	 *            the s
	 * @param swidth
	 *            the swidth
	 * @param selData
	 *            the sel data
	 * @param bounds
	 *            the bounds
	 */
	private void computeSelection(final GC gc, final String s, final int swidth, final SelectionData selData,
			final Rectangle bounds) {
		int leftOffset = selData.getLeftOffset(bounds.height);
		int rightOffset = selData.getRightOffset(bounds.height);
		boolean firstRow = selData.isFirstSelectionRow(bounds.y, bounds.height);
		boolean lastRow = selData.isLastSelectionRow(bounds.y, bounds.height);
		boolean selectedRow = selData.isSelectedRow(bounds.y, bounds.height);

		int sstart = -1;
		int sstop = -1;

		if (firstRow && bounds.x < leftOffset) {
			sstart = convertOffsetToStringIndex(gc, s, bounds.x, swidth, leftOffset);
		}
		if (lastRow && bounds.x + swidth > rightOffset) {
			sstop = convertOffsetToStringIndex(gc, s, bounds.x, swidth, rightOffset);
		}

		if (selectedRow) {
			int lindex = sstart != -1 ? sstart : 0;
			int rindex = sstop != -1 ? sstop : s.length();
			String mid = s.substring(lindex, rindex);
			if (mid.length() > 0) { selData.addSegment(mid); }
		}
	}

	/**
	 * Paint string segment.
	 *
	 * @param gc
	 *            the gc
	 * @param s
	 *            the s
	 * @param swidth
	 *            the swidth
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param lineY
	 *            the line Y
	 * @param hover
	 *            the hover
	 * @param rolloverMode
	 *            the rollover mode
	 * @param repaintRegion
	 *            the repaint region
	 */
	private void paintStringSegment(final GC gc, final String s, final int swidth, final int x, final int y,
			final int lineY, final boolean hover, final boolean rolloverMode, final Rectangle repaintRegion) {
		boolean reverse = false;
		int clipX = x;
		int clipY = y;
		int clipLineY = lineY;
		if (repaintRegion != null) {
			clipX -= repaintRegion.x;
			clipY -= repaintRegion.y;
			clipLineY -= repaintRegion.y;
		}
		if (rolloverMode && !hover) { reverse = true; }
		if (reverse) {
			drawUnderline(gc, swidth, clipX, clipLineY, hover, rolloverMode);
			drawText(gc, s, clipX, clipY);
		} else {
			drawText(gc, s, clipX, clipY);
			drawUnderline(gc, swidth, clipX, clipLineY, hover, rolloverMode);
		}
	}

	/**
	 * Draw text.
	 *
	 * @param gc
	 *            the gc
	 * @param s
	 *            the s
	 * @param clipX
	 *            the clip X
	 * @param clipY
	 *            the clip Y
	 */
	protected void drawText(final GC gc, final String s, final int clipX, final int clipY) {
		gc.drawText(s, clipX, clipY, true);
	}

	/**
	 * Draw underline.
	 *
	 * @param gc
	 *            the gc
	 * @param swidth
	 *            the swidth
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param hover
	 *            the hover
	 * @param rolloverMode
	 *            the rollover mode
	 */
	private void drawUnderline(final GC gc, final int swidth, final int x, final int y, final boolean hover,
			final boolean rolloverMode) {
		if (underline || rolloverMode) {
			Color saved = null;
			if (rolloverMode && !hover) {
				saved = gc.getForeground();
				gc.setForeground(gc.getBackground());
			}
			gc.drawLine(x, y, x + swidth - 1, y);
			if (saved != null) { gc.setForeground(saved); }
		}
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
	 * @param resourceTable
	 *            the resource table
	 * @param selected
	 *            the selected
	 */
	@Override
	public void layout(final GC gc, final int width, final Locator locator, final boolean selected) {
		Font oldFont = null;

		areaRectangles.clear();

		if (XmlTextModel.REGULAR_FONT != null) { gc.setFont(XmlTextModel.REGULAR_FONT); }
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		int descent = fm.getDescent();

		if (!wrapAllowed) {
			layoutWithoutWrapping(gc, width, locator, selected, fm, lineHeight, descent);
		} else {
			int lineStart = 0;
			int lastLoc = 0;
			Point lineExtent = new Point(0, 0);
			computeTextFragments(gc);
			int rightEdge = width - locator.marginWidth;
			for (int i = 0; i < textFragments.length; i++) {
				TextFragment fragment = textFragments[i];
				int breakLoc = fragment.index;
				if (breakLoc == 0) { continue; }
				// (i != 0 || locator.x > locator.getStartX() + (isSelectable() ? 1 : 0)) means:
				// only wrap on the first fragment if we are not at the start of a line
				if ((i != 0 || locator.x > locator.getStartX() + (isSelectable() ? 1 : 0))
						&& locator.x + lineExtent.x + fragment.length > rightEdge) {
					// overflow
					if (i != 0) {
						int ly = locator.getBaseline(lineHeight - fm.getLeading());
						Rectangle br = new Rectangle(isSelectable() ? locator.x - 1 : locator.x, ly,
								isSelectable() ? lineExtent.x + 1 : lineExtent.x, lineHeight - descent + 3);
						areaRectangles.add(new AreaRectangle(br, lineStart, lastLoc));
					}

					locator.rowHeight = Math.max(locator.rowHeight, lineExtent.y);
					locator.resetCaret();
					if (isSelectable()) { locator.x += 1; }
					locator.y += locator.rowHeight;
					locator.rowCounter++;
					locator.rowHeight = 0;
					lineStart = lastLoc;
					lineExtent.x = 0;
					lineExtent.y = 0;
				}
				lastLoc = breakLoc;
				lineExtent.x += fragment.length;
				lineExtent.y = Math.max(lineHeight, lineExtent.y);
			}
			// String lastLine = text.substring(lineStart, lastLoc);
			int ly = locator.getBaseline(lineHeight - fm.getLeading());
			int lastWidth = lineExtent.x;
			if (isSelectable()) { lastWidth += 1; }
			Rectangle br = new Rectangle(isSelectable() ? locator.x - 1 : locator.x, ly,
					isSelectable() ? lineExtent.x + 1 : lineExtent.x, lineHeight - descent + 3);
			// int lineY = ly + lineHeight - descent + 1;
			areaRectangles.add(new AreaRectangle(br, lineStart, lastLoc));
			locator.x += lastWidth;
			locator.rowHeight = Math.max(locator.rowHeight, lineExtent.y);
		}
		if (oldFont != null) { gc.setFont(oldFont); }
	}

	/**
	 * Compute text fragments.
	 *
	 * @param gc
	 *            the gc
	 */
	private void computeTextFragments(final GC gc) {
		if (textFragments != null) return;
		ArrayList<TextFragment> list = new ArrayList<>();
		BreakIterator wb = BreakIterator.getLineInstance();
		wb.setText(getText());
		int cursor = 0;
		for (int loc = wb.first(); loc != BreakIterator.DONE; loc = wb.next()) {
			if (loc == 0) { continue; }
			String word = text.substring(cursor, loc);
			Point extent = gc.textExtent(word);
			list.add(new TextFragment((short) loc, (short) extent.x));
			cursor = loc;
		}
		textFragments = list.toArray(new TextFragment[list.size()]);
	}

}
