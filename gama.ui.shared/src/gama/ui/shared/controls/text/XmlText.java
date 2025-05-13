/*******************************************************************************************************
 *
 * XmlText.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls.text;

/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation Martin Donnelly (m2a3@eircom.net) - patch (see
 * Bugzilla #145997)
 *******************************************************************************/

import java.io.InputStream;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.eclipse.ui.internal.forms.widgets.IFocusSelectable;
import org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment;
import org.eclipse.ui.internal.forms.widgets.Locator;
import org.eclipse.ui.internal.forms.widgets.SelectionData;

import gama.core.util.GamaFont;
import gama.ui.shared.resources.GamaFonts;

/**
 * This class is a read-only text control that is capable of rendering wrapped text. Text can be rendered as-is or by
 * parsing the formatting XML tags. Independently, words that start with http:// can be converted into hyperlinks on the
 * fly.
 * <p>
 * When configured to use formatting XML, the control requires the root element <code>form</code> to be used and
 * requires any ampersand (&amp;) characters in the text to be replaced by the entity <b>&amp;amp;</b>. The following
 * tags can be children of the <code>form</code> element:
 * </p>
 * <ul>
 * <li><b>p </b>- for defining paragraphs. The following attributes are allowed:
 * <ul>
 * <li><b>vspace </b>- if set to 'false', no vertical space will be added (default is 'true')</li>
 * </ul>
 * </li>
 * <li><b>li </b>- for defining list items. The following attributes are allowed:
 * <ul>
 * <li><b>vspace </b>- the same as with the <b>p </b> tag</li>
 * <li><b>style </b>- could be 'bullet' (default), 'text' and 'image'</li>
 * <li><b>value </b>- not used for 'bullet'. For text, it is the value of the text that is rendered as a bullet. For
 * image, it is the href of the image to be rendered as a bullet.</li>
 * <li><b>indent </b>- the number of pixels to indent the text in the list item</li>
 * <li><b>bindent </b>- the number of pixels to indent the bullet itself</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * Text in paragraphs and list items will be wrapped according to the width of the control. The following tags can
 * appear as children of either <b>p </b> or <b>li </b> elements:
 * <ul>
 * <li><b>img </b>- to render an image. Element accepts attribute 'href' that is a key to the <code>Image</code> set
 * using 'setImage' method. Vertical position of image relative to surrounding text is optionally controlled by the
 * attribute <b>align</b> that can have values <b>top</b>, <b>middle</b> and <b>bottom</b></li>
 * <li><b>a </b>- to render a hyperlink. Element accepts attribute 'href' that will be provided to the hyperlink
 * listeners via HyperlinkEvent object. The element also accepts 'nowrap' attribute (default is false). When set to
 * 'true', the hyperlink will not be wrapped. Hyperlinks automatically created when 'http://' is encountered in text are
 * not wrapped.</li>
 * <li><b>b </b>- the enclosed text will use bold font.</li>
 * <li><b>br </b>- forced line break (no attributes).</li>
 * <li><b>span </b>- the enclosed text will have the color and font specified in the element attributes. Color is
 * provided using 'color' attribute and is a key to the Color object set by 'setColor' method. Font is provided using
 * 'font' attribute and is a key to the Font object set by 'setFont' method. As with hyperlinks, it is possible to block
 * wrapping by setting 'nowrap' to true (false by default).</li>
 * <li><b>control (new in 3.1)</b> - to place a control that is a child of the text control. Element accepts attribute
 * 'href' that is a key to the Control object set using 'setControl' method. Optionally, attribute 'fill' can be set to
 * <code>true</code> to make the control fill the entire width of the text. Form text is not responsible for creating or
 * disposing controls, it only places them relative to the surrounding text. Similar to <b>img</b>, vertical position of
 * the control can be set using the <b>align</b> attribute. In addition, <b>width</b> and <b>height</b> attributes can
 * be used to force the dimensions of the control. If not used, the preferred control size will be used.
 * </ul>
 * <p>
 * None of the elements can nest. For example, you cannot have <b>b </b> inside a <b>span </b>. This was done to keep
 * everything simple and transparent. Since 3.1, an exception to this rule has been added to support nesting images and
 * text inside the hyperlink tag (<b>a</b>). Image enclosed in the hyperlink tag acts as a hyperlink, can be clicked on
 * and can accept and render selection focus. When both text and image is enclosed, selection and rendering will affect
 * both as a single hyperlink.
 * </p>
 * <p>
 * Since 3.1, it is possible to select text. Text selection can be programmatically accessed and also copied to
 * clipboard. Non-textual objects (images, controls etc.) in the selection range are ignored.
 * <p>
 * Care should be taken when using this control. Form text is not an HTML browser and should not be treated as such. If
 * you need complex formatting capabilities, use Browser widget. If you need editing capabilities and font/color styles
 * of text segments is all you need, use StyleText widget. Finally, if all you need is to wrap text, use SWT Label
 * widget and create it with SWT.WRAP style.
 *
 * @see FormToolkit
 * @see TableWrapLayout
 * @since 3.0
 */
public class XmlText extends Canvas implements IXmlFontUser {

	/**
	 * Value of the horizontal margin (default is 0).
	 */
	public int marginWidth = 0;

	/**
	 * Value of tue vertical margin (default is 1).
	 */
	public int marginHeight = 1;

	/** The has focus. */
	private boolean hasFocus;

	/** The paragraphs separated. */
	private boolean paragraphsSeparated = true;

	/** The model. */
	private final XmlTextModel model;

	/** The listeners. */
	private ListenerList<IHyperlinkListener> listeners;

	/** The entered. */
	private IHyperlinkSegment entered;

	/** The armed. */
	private IHyperlinkSegment armed;

	/** The in selection. */
	private boolean inSelection = false;

	/** The sel data. */
	private SelectionData selData;

	/**
	 * The Class XmlTextLayout.
	 */
	private class XmlTextLayout extends Layout implements ILayoutExtension {

		/**
		 * Instantiates a new form text layout.
		 */
		public XmlTextLayout() {}

		/**
		 * Compute maximum width.
		 *
		 * @param parent
		 *            the parent
		 * @param changed
		 *            the changed
		 * @return the int
		 */
		@Override
		public int computeMaximumWidth(final Composite parent, final boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
		}

		/**
		 * Compute minimum width.
		 *
		 * @param parent
		 *            the parent
		 * @param changed
		 *            the changed
		 * @return the int
		 */
		@Override
		public int computeMinimumWidth(final Composite parent, final boolean changed) {
			return computeSize(parent, 5, SWT.DEFAULT, true).x;
		}

		@Override
		public Point computeSize(final Composite composite, final int wHint, final int hHint, final boolean changed) {
			int innerWidth = wHint;
			if (innerWidth != SWT.DEFAULT) { innerWidth -= marginWidth * 2; }
			Point textSize = computeTextSize(innerWidth);
			int textWidth = textSize.x + 2 * marginWidth;
			int textHeight = textSize.y + 2 * marginHeight;
			return new Point(textWidth, textHeight);
		}

		/**
		 * Compute text size.
		 *
		 * @param wHint
		 *            the w hint
		 * @return the point
		 */
		private Point computeTextSize(final int wHint) {
			XmlParagraph[] paragraphs = model.getParagraphs();
			GC gc = new GC(XmlText.this);
			gc.setFont(getFont());
			Locator loc = new Locator();
			int width = wHint != SWT.DEFAULT ? wHint : 0;
			FontMetrics fm = gc.getFontMetrics();
			int lineHeight = fm.getHeight();
			boolean selectableInTheLastRow = false;
			for (int i = 0; i < paragraphs.length; i++) {
				XmlParagraph p = paragraphs[i];
				if (i > 0 && getParagraphsSeparated() && p.getAddVerticalSpace()) {
					loc.y += getParagraphSpacing(lineHeight);
				}
				loc.rowHeight = 0;
				loc.indent = p.getIndent();
				loc.x = p.getIndent();
				XmlParagraphSegment[] segments = p.getSegments();
				if (segments.length > 0) {
					selectableInTheLastRow = false;
					int pwidth = 0;
					for (XmlParagraphSegment segment : segments) {
						segment.advanceLocator(gc, wHint, loc, false);
						if (wHint != SWT.DEFAULT) {
							width = Math.max(width, loc.width);
						} else {
							pwidth = Math.max(pwidth, loc.width);
						}
						if (segment instanceof IFocusSelectable) { selectableInTheLastRow = true; }
					}
					if (wHint == SWT.DEFAULT) { width = Math.max(width, pwidth); }
					loc.y += loc.rowHeight;
				} else {
					// empty new line
					loc.y += lineHeight;
				}
			}
			gc.dispose();
			if (selectableInTheLastRow) { loc.y += 1; }
			return new Point(width, loc.y);
		}

		@Override
		protected void layout(final Composite composite, final boolean flushCache) {
			selData = null;
			Rectangle carea = composite.getClientArea();
			GC gc = new GC(composite);
			gc.setFont(getFont());
			gc.setForeground(getForeground());
			gc.setBackground(getBackground());

			Locator loc = new Locator();
			loc.marginWidth = marginWidth;
			loc.marginHeight = marginHeight;
			loc.y = marginHeight;
			FontMetrics fm = gc.getFontMetrics();
			int lineHeight = fm.getHeight();

			XmlParagraph[] paragraphs = model.getParagraphs();
			IHyperlinkSegment selectedLink = getSelectedLink();
			for (int i = 0; i < paragraphs.length; i++) {
				XmlParagraph p = paragraphs[i];
				if (i > 0 && paragraphsSeparated && p.getAddVerticalSpace()) {
					loc.y += getParagraphSpacing(lineHeight);
				}
				loc.indent = p.getIndent();
				loc.resetCaret();
				loc.rowHeight = 0;
				p.layout(gc, carea.width, loc, lineHeight, selectedLink);
			}
			gc.dispose();
		}
	}

	/**
	 * Contructs a new form text widget in the provided parent and using the styles.
	 * <p>
	 * The only valid style bit for <code>XmlText</code> is <code>SWT.NO_FOCUS</code>. This will cause the widget to
	 * always refuse focus.
	 *
	 * @param parent
	 *            form text parent control
	 * @param style
	 *            the widget style
	 */
	public XmlText(final Composite parent, final int style, final GamaFont font) {
		super(parent, SWT.NO_BACKGROUND | SWT.WRAP | style);
		setLayout(new XmlTextLayout());
		model = new XmlTextModel(font == null ? GamaFonts.getFont(getFont()) : font);
		addDisposeListener(e -> { model.dispose(); });
		addPaintListener(this::paint);
		addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {}

			@Override
			public void mouseDown(final MouseEvent e) {
				// select a link
				handleMouseClick(e, true);
			}

			@Override
			public void mouseUp(final MouseEvent e) {
				// activate a link
				handleMouseClick(e, false);
			}
		});
		addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseEnter(final MouseEvent e) {
				handleMouseMove(e);
			}

			@Override
			public void mouseExit(final MouseEvent e) {
				if (entered != null) {
					exitLink(entered, e.stateMask);
					entered = null;
					setCursor(null);
				}
			}

			@Override
			public void mouseHover(final MouseEvent e) {
				handleMouseHover(e);
			}
		});
		addMouseMoveListener(this::handleMouseMove);
	}

	/**
	 * Test for focus.
	 *
	 * @return <samp>true </samp> if the widget has focus.
	 */
	public boolean getFocus() { return hasFocus; }

	/**
	 * If paragraphs are separated, spacing will be added between them. Otherwise, new paragraphs will simply start on a
	 * new line with no spacing.
	 *
	 * @param value
	 *            <code>true</code> if paragraphs are separated, <code>false
	 *              </code> otherwise.
	 */
	public void setParagraphsSeparated(final boolean value) { paragraphsSeparated = value; }

	/**
	 * Tests if there is some inter-paragraph spacing.
	 *
	 * @return <samp>true </samp> if paragraphs are separated, <samp>false </samp> otherwise.
	 */
	public boolean getParagraphsSeparated() { return paragraphsSeparated; }

	/**
	 * Sets the provided text. Text can be rendered as-is, or by parsing the formatting tags. Optionally, sections of
	 * text starting with http:// will be converted to hyperlinks.
	 *
	 * @param text
	 *            the text to render
	 * @param parseTags
	 *            if <samp>true </samp>, formatting tags will be parsed. Otherwise, text will be rendered as-is.
	 * @param expandURLs
	 *            if <samp>true </samp>, URLs found in the untagged text will be converted into hyperlinks.
	 */
	public void setText(final String text, final boolean parseTags, final boolean expandURLs) {
		entered = null;
		if (parseTags) {
			model.parseTaggedText(text, expandURLs);
		} else {
			model.parseRegularText(text, expandURLs);
		}
		layout();
		redraw();
	}

	/**
	 * Sets the contents of the stream. Optionally, URLs in untagged text can be converted into hyperlinks. The caller
	 * is responsible for closing the stream.
	 *
	 * @param is
	 *            stream to render
	 * @param expandURLs
	 *            if <samp>true </samp>, URLs found in untagged text will be converted into hyperlinks.
	 */
	public void setContents(final InputStream is, final boolean expandURLs) {
		entered = null;
		model.parseInputStream(is, expandURLs);
		layout();
		redraw();
	}

	/**
	 * Controls whether whitespace inside paragraph and list items is normalized. Note that the new value will not
	 * affect the current text in the control, only subsequent calls to <code>setText</code> or
	 * <code>setContents</code>.
	 * <p>
	 * If normalized:
	 * </p>
	 * <ul>
	 * <li>all white space characters will be condensed into at most one when between words.</li>
	 * <li>new line characters will be ignored and replaced with one white space character</li>
	 * <li>white space characters after the opening tags and before the closing tags will be trimmed</li>
	 * </ul>
	 *
	 * @param value
	 *            <code>true</code> if whitespace is normalized, <code>false</code> otherwise.
	 */
	public void setWhitespaceNormalized(final boolean value) {
		model.setWhitespaceNormalized(value);
	}

	/**
	 * Tests whether whitespace inside paragraph and list item is normalized.
	 *
	 * @see #setWhitespaceNormalized(boolean)
	 * @return <code>true</code> if whitespace is normalized, <code>false</code> otherwise.
	 */
	public boolean isWhitespaceNormalized() { return model.isWhitespaceNormalized(); }

	/**
	 * Returns the hyperlink settings that are in effect for this control.
	 *
	 * @return current hyperlinks settings
	 */
	public HyperlinkSettings getHyperlinkSettings() { return model.getHyperlinkSettings(); }

	/**
	 * Sets the hyperlink settings to be used for this control. Settings will affect things like hyperlink color,
	 * rendering style, cursor etc.
	 *
	 * @param settings
	 *            hyperlink settings for this control
	 */
	public void setHyperlinkSettings(final HyperlinkSettings settings) {
		model.setHyperlinkSettings(settings);
	}

	/**
	 * Adds a listener that will handle hyperlink events.
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addHyperlinkListener(final IHyperlinkListener listener) {
		if (listeners == null) { listeners = new ListenerList<>(); }
		listeners.add(listener);
	}

	/**
	 * Removes the hyperlink listener.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeHyperlinkListener(final IHyperlinkListener listener) {
		if (listeners == null) return;
		listeners.remove(listener);
	}

	/**
	 * Adds a selection listener. A Selection event is sent by the widget when the selection has changed.
	 * <p>
	 * <code>widgetDefaultSelected</code> is not called for XmlText.
	 * </p>
	 *
	 * @param listener
	 *            the listener
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT when listener is null</li>
	 *                </ul>
	 * @since 3.1
	 */
	public void addSelectionListener(final SelectionListener listener) {
		addTypedListener(listener, SWT.Selection);
	}

	/**
	 * Removes the specified selection listener.
	 *
	 * @param listener
	 *            the listener
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT when listener is null</li>
	 *                </ul>
	 * @since 3.1
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		removeTypedListener(SWT.Selection, listener);
	}

	/**
	 * Returns the selected text.
	 *
	 * @return selected text, or an empty String if there is no selection.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 * @since 3.1
	 */

	public String getSelectionText() {
		checkWidget();
		if (selData != null) return selData.getSelectionText();
		return ""; //$NON-NLS-1$
	}

	/**
	 * Tests if the text is selected and can be copied into the clipboard.
	 *
	 * @return <code>true</code> if the selected text can be copied into the clipboard, <code>false</code> otherwise.
	 * @since 3.1
	 */
	public boolean canCopy() {
		return selData != null && selData.canCopy();
	}

	/**
	 * Copies the selected text into the clipboard. Does nothing if no text is selected or the text cannot be copied for
	 * any other reason.
	 *
	 * @since 3.1
	 */

	public void copy() {
		if (!canCopy()) return;
		Clipboard clipboard = new Clipboard(getDisplay());
		Object[] o = { getSelectionText() };
		Transfer[] t = { TextTransfer.getInstance() };
		clipboard.setContents(o, t);
		clipboard.dispose();
	}

	/**
	 * Returns the reference of the hyperlink that currently has keyboard focus, or <code>null</code> if there are no
	 * hyperlinks in the receiver or no hyperlink has focus at the moment.
	 *
	 * @return href of the selected hyperlink or <code>null</code> if none selected.
	 * @since 3.1
	 */
	public Object getSelectedLinkHref() {
		IHyperlinkSegment link = getSelectedLink();
		return link != null ? link.getHref() : null;
	}

	/**
	 * Returns the text of the hyperlink that currently has keyboard focus, or <code>null</code> if there are no
	 * hyperlinks in the receiver or no hyperlink has focus at the moment.
	 *
	 * @return text of the selected hyperlink or <code>null</code> if none selected.
	 * @since 3.1
	 */
	public String getSelectedLinkText() {
		IHyperlinkSegment link = getSelectedLink();
		return link != null ? link.getText() : null;
	}

	/**
	 * Gets the selected link.
	 *
	 * @return the selected link
	 */
	private IHyperlinkSegment getSelectedLink() {
		IFocusSelectable segment = model.getSelectedSegment();
		if (segment instanceof IHyperlinkSegment) return (IHyperlinkSegment) segment;
		return null;
	}

	/**
	 * Start selection.
	 *
	 * @param e
	 *            the e
	 */
	private void startSelection(final MouseEvent e) {
		inSelection = true;
		selData = new SelectionData(e);
		redraw();
	}

	/**
	 * End selection.
	 *
	 * @param e
	 *            the e
	 */
	private void endSelection(final MouseEvent e) {
		inSelection = false;
		if (selData != null) {
			if (!selData.isEnclosed()) {
				selData = null;
			} else {
				computeSelection();
			}
		}
		notifySelectionChanged();
	}

	/**
	 * Compute selection.
	 */
	private void computeSelection() {
		GC gc = new GC(this);
		XmlParagraph[] paragraphs = model.getParagraphs();
		IHyperlinkSegment selectedLink = getSelectedLink();
		if (getDisplay().getFocusControl() != this) { selectedLink = null; }
		for (XmlParagraph p : paragraphs) { p.computeSelection(gc, selectedLink, selData); }
		gc.dispose();
	}

	/**
	 * Clear selection.
	 */
	void clearSelection() {
		selData = null;
		if (!isDisposed()) {
			redraw();
			notifySelectionChanged();
		}
	}

	/**
	 * Notify selection changed.
	 */
	private void notifySelectionChanged() {
		Event event = new Event();
		event.widget = this;
		event.display = this.getDisplay();
		event.type = SWT.Selection;
		notifyListeners(SWT.Selection, event);
		// A listener could have caused the widget to be disposed
		if (!isDisposed()) { getAccessible().selectionChanged(); }
	}

	/**
	 * Handle drag.
	 *
	 * @param e
	 *            the e
	 */
	private void handleDrag(final MouseEvent e) {
		if (selData != null) {
			ScrolledComposite scomp = FormUtil.getScrolledComposite(this);
			if (scomp != null) { FormUtil.ensureVisible(scomp, this, e); }
			selData.update(e);
			redraw();
		}
	}

	/**
	 * Handle mouse click.
	 *
	 * @param e
	 *            the e
	 * @param down
	 *            the down
	 */
	private void handleMouseClick(final MouseEvent e, final boolean down) {
		if (down) {
			// select a hyperlink
			IHyperlinkSegment segmentUnder = model.findHyperlinkAt(e.x, e.y);
			if (segmentUnder != null) {
				// IHyperlinkSegment oldLink = getSelectedLink();
				if (getDisplay().getFocusControl() != this) { setFocus(); }
				model.selectLink(segmentUnder);
				enterLink(segmentUnder, e.stateMask);
				// paintFocusTransfer(oldLink, segmentUnder);
			}
			if (e.button == 1) {
				startSelection(e);
				armed = segmentUnder;
			} else {}
		} else if (e.button == 1) {
			endSelection(e);
			if (isDisposed()) return;
			IHyperlinkSegment segmentUnder = model.findHyperlinkAt(e.x, e.y);
			if (segmentUnder != null && armed == segmentUnder && selData == null) {
				activateLink(segmentUnder, e.stateMask);
				armed = null;
			}
		}
	}

	/**
	 * Handle mouse hover.
	 *
	 * @param e
	 *            the e
	 */
	private void handleMouseHover(final MouseEvent e) {}

	/**
	 * Update tooltip text.
	 *
	 * @param segment
	 *            the segment
	 */
	private void updateTooltipText(final XmlParagraphSegment segment) {
		String tooltipText = null;
		if (segment != null) { tooltipText = segment.getTooltipText(); }
		String currentTooltipText = getToolTipText();

		if (currentTooltipText != null == (tooltipText == null)) { setToolTipText(tooltipText); }
	}

	/**
	 * Handle mouse move.
	 *
	 * @param e
	 *            the e
	 */
	private void handleMouseMove(final MouseEvent e) {
		if (inSelection) {
			handleDrag(e);
			return;
		}
		XmlParagraphSegment segmentUnder = model.findSegmentAt(e.x, e.y);
		updateTooltipText(segmentUnder);
		if (segmentUnder == null) {
			if (entered != null) {
				exitLink(entered, e.stateMask);
				entered = null;
			}
			setCursor(null);
		} else if (segmentUnder instanceof IHyperlinkSegment linkUnder) {
			if (entered != null && linkUnder != entered) {
				// Special case: links are so close that there are 0 pixels between.
				// Must exit the link before entering the next one.
				exitLink(entered, e.stateMask);
				entered = null;
			}
			if (entered == null) {
				entered = linkUnder;
				enterLink(linkUnder, e.stateMask);
				setCursor(model.getHyperlinkSettings().getHyperlinkCursor());
			}
		} else {
			if (entered != null) {
				exitLink(entered, e.stateMask);
				entered = null;
			}
			if (segmentUnder instanceof XmlTextSegment) {
				setCursor(model.getHyperlinkSettings().getTextCursor());
			} else {
				setCursor(null);
			}
		}
	}

	/**
	 * Enter link.
	 *
	 * @param link
	 *            the link
	 * @param stateMask
	 *            the state mask
	 */
	private void enterLink(final IHyperlinkSegment link, final int stateMask) {
		if (link == null || listeners == null) return;
		HyperlinkEvent he = new HyperlinkEvent(this, link.getHref(), link.getText(), stateMask);
		for (IHyperlinkListener listener : listeners) { listener.linkEntered(he); }
	}

	/**
	 * Exit link.
	 *
	 * @param link
	 *            the link
	 * @param stateMask
	 *            the state mask
	 */
	private void exitLink(final IHyperlinkSegment link, final int stateMask) {
		if (link == null || listeners == null) return;
		HyperlinkEvent he = new HyperlinkEvent(this, link.getHref(), link.getText(), stateMask);
		for (IHyperlinkListener listener : listeners) { listener.linkExited(he); }
	}

	/**
	 * Activate link.
	 *
	 * @param link
	 *            the link
	 * @param stateMask
	 *            the state mask
	 */
	private void activateLink(final IHyperlinkSegment link, final int stateMask) {
		setCursor(model.getHyperlinkSettings().getBusyCursor());
		if (listeners != null) {
			int size = listeners.size();
			HyperlinkEvent e = new HyperlinkEvent(this, link.getHref(), link.getText(), stateMask);
			Object[] listenerList = listeners.getListeners();
			for (int i = 0; i < size; i++) {
				IHyperlinkListener listener = (IHyperlinkListener) listenerList[i];
				listener.linkActivated(e);
			}
		}
		if (!isDisposed() && model.linkExists(link)) { setCursor(model.getHyperlinkSettings().getHyperlinkCursor()); }
	}

	/**
	 * Paint.
	 *
	 * @param e
	 *            the e
	 */
	private void paint(final PaintEvent e) {
		GC gc = e.gc;
		gc.setFont(getFont());
		gc.setForeground(getForeground());
		gc.setBackground(getBackground());
		repaint(gc, e.x, e.y, e.width, e.height);
	}

	/**
	 * Repaint.
	 *
	 * @param gc
	 *            the gc
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	private void repaint(final GC gc, final int x, final int y, final int width, final int height) {
		Image textBuffer = new Image(getDisplay(), width, height);
		GC textGC = new GC(textBuffer, gc.getStyle());
		textGC.setForeground(getForeground());
		textGC.setBackground(getBackground());
		textGC.setFont(getFont());
		textGC.fillRectangle(0, 0, width, height);
		Rectangle repaintRegion = new Rectangle(x, y, width, height);

		XmlParagraph[] paragraphs = model.getParagraphs();
		IHyperlinkSegment selectedLink = getSelectedLink();
		if (getDisplay().getFocusControl() != this) { selectedLink = null; }
		for (XmlParagraph p : paragraphs) { p.paint(textGC, repaintRegion, selectedLink, selData); }
		textGC.dispose();
		gc.drawImage(textBuffer, x, y);
		textBuffer.dispose();
	}

	/**
	 * Gets the paragraph spacing.
	 *
	 * @param lineHeight
	 *            the line height
	 * @return the paragraph spacing
	 */
	private int getParagraphSpacing(final int lineHeight) {
		return lineHeight / 2;
	}

	/**
	 * Overrides the method by fully trusting the layout manager (computed width or height may be larger than the
	 * provider width or height hints). Callers should be prepared that the computed width is larger than the provided
	 * wHint.
	 *
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		Point size;
		XmlTextLayout layout = (XmlTextLayout) getLayout();
		if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
			size = layout.computeSize(this, wHint, hHint, changed);
		} else {
			size = new Point(wHint, hHint);
		}
		Rectangle trim = computeTrim(0, 0, size.x, size.y);
		return new Point(trim.width, trim.height);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		redraw();
	}

}
