/*******************************************************************************************************
 *
 * XmlHyperlinkSegment.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls.text;

import java.util.Hashtable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment;
import org.eclipse.ui.internal.forms.widgets.SelectionData;

/**
 * @version 1.0
 * @author
 */
public class XmlHyperlinkSegment extends XmlTextSegment implements IHyperlinkSegment {

	/** The href. */
	private String href;

	/** The tooltip text. */
	private String tooltipText;

	/** The settings. */
	private final HyperlinkSettings settings;

	/**
	 * Instantiates a new text hyperlink segment.
	 *
	 * @param text
	 *            the text
	 * @param settings
	 *            the settings
	 * @param fontId
	 *            the font id
	 */
	public XmlHyperlinkSegment(final String text, final HyperlinkSettings settings, final boolean bold) {
		super(text, bold);
		this.settings = settings;
	}

	/**
	 * Gets the href.
	 *
	 * @return the href
	 */
	/*
	 * @see IObjectReference#getObjectId()
	 */
	@Override
	public String getHref() { return href; }

	/**
	 * Sets the href.
	 *
	 * @param href
	 *            the new href
	 */
	public void setHref(final String href) { this.href = href; }

	@Override
	public void paint(final GC gc, final boolean hover, final boolean selected, final SelectionData selData,
			final Rectangle repaintRegion) {
		boolean rolloverMode = settings.getHyperlinkUnderlineMode() == HyperlinkSettings.UNDERLINE_HOVER;
		underline = settings.getHyperlinkUnderlineMode() == HyperlinkSettings.UNDERLINE_ALWAYS;
		Color savedFg = gc.getForeground();
		Color newFg = hover ? settings.getActiveForeground() : settings.getForeground();
		if (newFg != null) { gc.setForeground(newFg); }
		super.paint(gc, hover, selected, rolloverMode, selData, repaintRegion);
		gc.setForeground(savedFg);
	}

	@Override
	protected void drawText(final GC gc, final String s, final int clipX, final int clipY) {
		gc.drawText(s, clipX, clipY, false);
	}

	@Override
	public String getTooltipText() { return tooltipText; }

	/**
	 * Sets the tooltip text.
	 *
	 * @param tooltip
	 *            the new tooltip text
	 */
	public void setTooltipText(final String tooltip) { this.tooltipText = tooltip; }

	@Override
	public boolean isSelectable() { return true; }

	/**
	 * Checks if is focus selectable.
	 *
	 * @param resourceTable
	 *            the resource table
	 * @return true, if is focus selectable
	 */
	@Override
	public boolean isFocusSelectable(final Hashtable<String, Object> resourceTable) {
		return true;
	}

	/**
	 * Sets the focus.
	 *
	 * @param resourceTable
	 *            the resource table
	 * @param direction
	 *            the direction
	 * @return true, if successful
	 */
	@Override
	public boolean setFocus(final Hashtable<String, Object> resourceTable, final boolean direction) {
		return true;
	}
}