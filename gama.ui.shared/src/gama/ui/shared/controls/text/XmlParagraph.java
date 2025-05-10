/*******************************************************************************************************
 *
 * XmlParagraph.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls.text;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment;
import org.eclipse.ui.internal.forms.widgets.Locator;
import org.eclipse.ui.internal.forms.widgets.SelectionData;

/**
 * @version 1.0
 */
public class XmlParagraph {

	/** The Constant PROTOCOLS. */
	public static final String[] PROTOCOLS = { "http://", "https://", "ftp://" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/** The segments. */
	private List<XmlParagraphSegment> segments;

	/** The add vertical space. */
	private boolean addVerticalSpace = true;

	/**
	 * Instantiates a new xml paragraph.
	 *
	 * @param addVerticalSpace
	 *            the add vertical space
	 */
	public XmlParagraph(final boolean addVerticalSpace) {
		this.addVerticalSpace = addVerticalSpace;
	}

	/**
	 * Gets the indent.
	 *
	 * @return the indent
	 */
	public int getIndent() { return 0; }

	/**
	 * Gets the adds the vertical space.
	 *
	 * @return the adds the vertical space
	 */
	public boolean getAddVerticalSpace() { return addVerticalSpace; }

	/**
	 * Gets the segments.
	 *
	 * @return the segments
	 */
	public XmlParagraphSegment[] getSegments() {
		if (segments == null) return new XmlParagraphSegment[0];
		return segments.toArray(new XmlParagraphSegment[segments.size()]);
	}

	/**
	 * Adds the segment.
	 *
	 * @param segment
	 *            the segment
	 */
	public void addSegment(final XmlParagraphSegment segment) {
		if (segments == null) { segments = new ArrayList<>(); }
		segments.add(segment);
	}

	/**
	 * Parses the regular text.
	 *
	 * @param text
	 *            the text
	 * @param expandURLs
	 *            the expand UR ls
	 * @param wrapAllowed
	 *            the wrap allowed
	 * @param settings
	 *            the settings
	 * @param fontId
	 *            the font id
	 * @param colorId
	 *            the color id
	 */
	public void parseRegularText(final String text, final boolean expandURLs, final boolean wrapAllowed,
			final HyperlinkSettings settings, final boolean bold) {
		if (text.isEmpty()) return;
		if (expandURLs) {
			int loc = findUrl(text, 0);
			if (loc == -1) {
				addSegment(new XmlTextSegment(text, bold, wrapAllowed));
			} else {
				int textLoc = 0;
				while (loc != -1) {
					addSegment(new XmlTextSegment(text.substring(textLoc, loc), bold, wrapAllowed));
					boolean added = false;
					for (textLoc = loc; textLoc < text.length(); textLoc++) {
						char c = text.charAt(textLoc);
						if (Character.isSpaceChar(c)) {
							addHyperlinkSegment(text.substring(loc, textLoc), settings, bold);
							added = true;
							break;
						}
					}
					if (!added) {
						// there was no space - just end of text
						addHyperlinkSegment(text.substring(loc), settings, bold);
						break;
					}
					loc = findUrl(text, textLoc);
				}
				if (textLoc < text.length()) {
					addSegment(new XmlTextSegment(text.substring(textLoc), bold, wrapAllowed));
				}
			}
		} else {
			addSegment(new XmlTextSegment(text, bold, wrapAllowed));
		}
	}

	/**
	 * Find url.
	 *
	 * @param text
	 *            the text
	 * @param startIndex
	 *            the start index
	 * @return the int
	 */
	private int findUrl(final String text, final int startIndex) {
		int[] locs = new int[PROTOCOLS.length];
		for (int i = 0; i < PROTOCOLS.length; i++) { locs[i] = text.indexOf(PROTOCOLS[i], startIndex); }
		Arrays.sort(locs);
		for (int i = 0; i < PROTOCOLS.length; i++)
			if (locs[i] != -1) return locs[i];
		return -1;
	}

	/**
	 * Adds the hyperlink segment.
	 *
	 * @param text
	 *            the text
	 * @param settings
	 *            the settings
	 * @param fontId
	 *            the font id
	 */
	private void addHyperlinkSegment(final String text, final HyperlinkSettings settings, final boolean bold) {
		XmlHyperlinkSegment hs = new XmlHyperlinkSegment(text, settings, bold);
		hs.setWordWrapAllowed(false);
		hs.setHref(text);
		addSegment(hs);
	}

	/**
	 * Compute row heights.
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
	 */
	protected void computeRowHeights(final GC gc, final int width, final Locator loc, final int lineHeight) {
		// compute heights
		Locator hloc = loc.create();
		ArrayList<int[]> heights = new ArrayList<>();
		hloc.heights = heights;
		hloc.rowCounter = 0;
		for (XmlParagraphSegment segment : getSegments()) { segment.advanceLocator(gc, width, hloc, true); }
		if (hloc.rowHeight == 0) {
			FontMetrics fm = gc.getFontMetrics();
			hloc.rowHeight = fm.getHeight();
		}
		hloc.collectHeights();
		loc.heights = heights;
		loc.rowCounter = 0;
	}

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
	public void layout(final GC gc, final int width, final Locator loc, final int lineHeight,
			final IHyperlinkSegment selectedLink) {
		XmlParagraphSegment[] segs = getSegments();
		if (segs.length > 0) {
			if (loc.heights == null) { computeRowHeights(gc, width, loc, lineHeight); }
			for (XmlParagraphSegment segment : segs) {
				boolean doSelect = false;
				if (selectedLink instanceof XmlParagraphSegment sl && segment.equals(sl)) { doSelect = true; }
				segment.layout(gc, width, loc, doSelect);
			}
			loc.heights = null;
			loc.y += loc.rowHeight;
		} else {
			loc.y += lineHeight;
		}
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
	public void paint(final GC gc, final Rectangle repaintRegion, final IHyperlinkSegment selectedLink,
			final SelectionData selData) {
		for (XmlParagraphSegment segment : getSegments()) {
			if (!segment.intersects(repaintRegion)) { continue; }
			boolean doSelect = false;
			if (selectedLink instanceof XmlParagraphSegment sl && segment.equals(sl)) { doSelect = true; }
			segment.paint(gc, false, doSelect, selData, repaintRegion);
		}
	}

	/**
	 * Compute selection.
	 *
	 * @param gc
	 *            the gc
	 * @param resourceTable
	 *            the resource table
	 * @param selectedLink
	 *            the selected link
	 * @param selData
	 *            the sel data
	 */
	public void computeSelection(final GC gc, final IHyperlinkSegment selectedLink, final SelectionData selData) {
		for (XmlParagraphSegment segment : getSegments()) { segment.computeSelection(gc, selData); }
	}

	/**
	 * Gets the accessible text.
	 *
	 * @return the accessible text
	 */
	public String getAccessibleText() {
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		for (XmlParagraphSegment segment : getSegments()) {
			if (segment instanceof XmlTextSegment) {
				String text = ((XmlTextSegment) segment).getText();
				writer.print(text);
			}
		}
		writer.println();
		swriter.flush();
		return swriter.toString();
	}

	/**
	 * Find segment at.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the paragraph segment
	 */
	public XmlParagraphSegment findSegmentAt(final int x, final int y) {
		if (segments != null) {
			for (XmlParagraphSegment segment : segments) { if (segment.contains(x, y)) return segment; }
		}
		return null;
	}

	/**
	 * Clear cache.
	 *
	 * @param fontId
	 *            the font id
	 */
	public void clearCache(final String fontId) {
		if (segments != null) { for (XmlParagraphSegment segment : segments) { segment.clearCache(fontId); } }
	}
}
