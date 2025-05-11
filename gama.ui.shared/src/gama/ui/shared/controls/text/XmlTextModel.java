/*******************************************************************************************************
 *
 * XmlTextModel.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls.text;

/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation Ralf M Petter<ralf.petter@gmail.com> - Bug 259846
 * Karsten Thoms<karsten.thoms@itemis.de> - Bug 521493
 *******************************************************************************/

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.internal.forms.widgets.BulletParagraph;
import org.eclipse.ui.internal.forms.widgets.IFocusSelectable;
import org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment;
import org.eclipse.ui.internal.forms.widgets.SWTUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import gama.core.util.GamaFont;
import gama.dev.DEBUG;
import gama.ui.shared.resources.GamaFonts;

/**
 * The Class XmlTextModel.
 */
public class XmlTextModel implements IXmlFontUser {

	static {
		DEBUG.ON();
	}

	/**
	 * The Class ParseErrorHandler.
	 */
	/*
	 * This class prevents parse errors from being written to standard output
	 */
	public static class ParseErrorHandler implements ErrorHandler {

		@Override
		public void error(final SAXParseException arg0) throws SAXException {
			DEBUG.OUT(arg0.getMessage());
		}

		@Override
		public void fatalError(final SAXParseException arg0) throws SAXException {
			DEBUG.OUT(arg0.getMessage());
		}

		@Override
		public void warning(final SAXParseException arg0) throws SAXException {
			DEBUG.OUT(arg0.getMessage());
		}
	}

	/** The Constant DOCUMENT_BUILDER_FACTORY. */
	@SuppressWarnings ("restriction") private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY =
			org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderFactoryWithErrorOnDOCTYPE();

	/** The whitespace normalized. */
	private boolean whitespaceNormalized = true;

	/** The paragraphs. */
	private List<XmlParagraph> paragraphs;

	/** The selectable segments. */
	private IFocusSelectable[] selectableSegments;

	/** The selected segment index. */
	private int selectedSegmentIndex = -1;

	/** The hyperlink settings. */
	private HyperlinkSettings hyperlinkSettings;

	/** The regular font. */
	public GamaFont font;

	/**
	 * Instantiates a new xml text model.
	 */
	public XmlTextModel(final GamaFont font) {
		this.font = font;
		reset();
	}

	@Override
	public Font getFont() { return GamaFonts.getFont(font); }

	/**
	 * Gets the paragraphs.
	 *
	 * @return the paragraphs
	 */
	public XmlParagraph[] getParagraphs() {
		if (paragraphs == null) return new XmlParagraph[0];
		return paragraphs.toArray(new XmlParagraph[paragraphs.size()]);
	}

	/**
	 * Parses the tagged text.
	 *
	 * @param taggedText
	 *            the tagged text
	 * @param expandURLs
	 *            the expand UR ls
	 */
	/*
	 * @see ITextModel#parse(String)
	 */
	public void parseTaggedText(String taggedText, final boolean expandURLs) {
		if (taggedText == null) {
			reset();
			return;
		}
		taggedText = processAmpersandEscapes(taggedText);
		InputStream stream = new ByteArrayInputStream(taggedText.getBytes(StandardCharsets.UTF_8));
		parseInputStream(stream, expandURLs);
	}

	/**
	 * Process ampersand escapes.
	 *
	 * @param pTaggedText
	 *            the tagged text
	 * @return the string
	 */
	private String processAmpersandEscapes(final String pTaggedText) {
		try {

			String taggedText = pTaggedText.replace("&quot;", "&#034;"); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("<br>", "<br/>"); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("</br>", "<br/>"); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace(" \"", " &#034;"); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("\" ", "&#034; "); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace(" '", " &#039;"); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("' ", "&#039; "); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("&apos;", "&#039;"); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("< ", "&#060; "); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("&lt;", "&#060;"); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("&gt;", "&#062;"); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace(" >", " &#062;"); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("& ", "&#038; "); //$NON-NLS-1$//$NON-NLS-2$
			taggedText = taggedText.replace("&amp;", "&#038;"); //$NON-NLS-1$//$NON-NLS-2$
			return taggedText.replaceAll("&([^#])", "&#038;$1"); //$NON-NLS-1$//$NON-NLS-2$
		} catch (Exception e) {
			return pTaggedText;
		}
	}

	/**
	 * Parses the input stream.
	 *
	 * @param is
	 *            the is
	 * @param expandURLs
	 *            the expand UR ls
	 */
	public void parseInputStream(final InputStream is, final boolean expandURLs) {

		DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
		DOCUMENT_BUILDER_FACTORY.setIgnoringComments(true);

		reset();
		try {
			DocumentBuilder parser = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
			parser.setErrorHandler(new ParseErrorHandler());
			InputSource source = new InputSource(is);
			Document doc = parser.parse(source);
			processDocument(doc, expandURLs);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			String text = "Error in the text: " + e.getMessage();
			parseRegularText(text, false);
		}

	}

	/**
	 * Process document.
	 *
	 * @param doc
	 *            the doc
	 * @param expandURLs
	 *            the expand UR ls
	 */
	private void processDocument(final Document doc, final boolean expandURLs) {
		Node root = doc.getDocumentElement();
		NodeList children = root.getChildNodes();
		processSubnodes(paragraphs, children, expandURLs);
	}

	/**
	 * Process subnodes.
	 *
	 * @param plist
	 *            the plist
	 * @param children
	 *            the children
	 * @param expandURLs
	 *            the expand UR ls
	 */
	private void processSubnodes(final List<XmlParagraph> plist, final NodeList children, final boolean expandURLs) {
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				// Make an implicit paragraph
				String text = getSingleNodeText(child);
				if (text != null && !isIgnorableWhiteSpace(text, true)) {
					XmlParagraph p = new XmlParagraph(true, font);
					p.parseRegularText(text, expandURLs, true, getHyperlinkSettings(), false, false);
					plist.add(p);
				}
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				String tag = child.getNodeName().toLowerCase();
				if ("p".equals(tag)) { //$NON-NLS-1$
					XmlParagraph p = processParagraph(child, expandURLs);
					if (p != null) { plist.add(p); }
				} else if ("li".equals(tag)) { //$NON-NLS-1$
					XmlParagraph p = processListItem(child, expandURLs);
					if (p != null) { plist.add(p); }
				}
			}
		}
	}

	/**
	 * Process paragraph.
	 *
	 * @param paragraph
	 *            the paragraph
	 * @param expandURLs
	 *            the expand UR ls
	 * @return the paragraph
	 */
	private XmlParagraph processParagraph(final Node paragraph, final boolean expandURLs) {
		NodeList children = paragraph.getChildNodes();
		NamedNodeMap atts = paragraph.getAttributes();
		Node addSpaceAtt = atts.getNamedItem("addVerticalSpace"); //$NON-NLS-1$
		boolean addSpace = true;

		if (addSpaceAtt == null) {
			addSpaceAtt = atts.getNamedItem("vspace"); //$NON-NLS-1$
		}

		if (addSpaceAtt != null) {
			String value = addSpaceAtt.getNodeValue();
			addSpace = "true".equalsIgnoreCase(value); //$NON-NLS-1$
		}
		XmlParagraph p = new XmlParagraph(addSpace, font);

		processSegments(p, children, expandURLs);
		return p;
	}

	/**
	 * Process list item.
	 *
	 * @param listItem
	 *            the list item
	 * @param expandURLs
	 *            the expand UR ls
	 * @return the paragraph
	 */
	private XmlParagraph processListItem(final Node listItem, final boolean expandURLs) {
		NodeList children = listItem.getChildNodes();
		NamedNodeMap atts = listItem.getAttributes();
		Node addSpaceAtt = atts.getNamedItem("addVerticalSpace");//$NON-NLS-1$
		Node styleAtt = atts.getNamedItem("style");//$NON-NLS-1$
		Node valueAtt = atts.getNamedItem("value");//$NON-NLS-1$
		Node indentAtt = atts.getNamedItem("indent");//$NON-NLS-1$
		Node bindentAtt = atts.getNamedItem("bindent");//$NON-NLS-1$
		int style = BulletParagraph.CIRCLE;
		int indent = -1;
		int bindent = -1;
		String text = null;
		boolean addSpace = true;

		if (addSpaceAtt != null) {
			String value = addSpaceAtt.getNodeValue();
			addSpace = "true".equalsIgnoreCase(value); //$NON-NLS-1$
		}
		if (styleAtt != null) {
			String value = styleAtt.getNodeValue();
			if ("text".equalsIgnoreCase(value)) { //$NON-NLS-1$
				style = BulletParagraph.TEXT;
			} else if ("image".equalsIgnoreCase(value)) { //$NON-NLS-1$
				style = BulletParagraph.IMAGE;
			} else if ("bullet".equalsIgnoreCase(value)) { //$NON-NLS-1$
				style = BulletParagraph.CIRCLE;
			}
		}
		if (valueAtt != null) {
			text = valueAtt.getNodeValue();
			if (style == BulletParagraph.IMAGE) {
				text = "i." + text; //$NON-NLS-1$
			}
		}
		if (indentAtt != null) {
			String value = indentAtt.getNodeValue();
			try {
				indent = Integer.parseInt(value);
			} catch (NumberFormatException e) {}
		}
		if (bindentAtt != null) {
			String value = bindentAtt.getNodeValue();
			try {
				bindent = Integer.parseInt(value);
			} catch (NumberFormatException e) {}
		}

		XmlLiParagraph p = new XmlLiParagraph(addSpace, font);
		p.setIndent(indent);
		p.setBulletIndent(bindent);
		p.setBulletStyle(style);
		p.setBulletText(text);

		processSegments(p, children, expandURLs);
		return p;
	}

	/**
	 * Process segments.
	 *
	 * @param p
	 *            the p
	 * @param children
	 *            the children
	 * @param expandURLs
	 *            the expand UR ls
	 */
	private void processSegments(final XmlParagraph p, final NodeList children, final boolean expandURLs) {
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			XmlParagraphSegment segment = null;

			if (child.getNodeType() == Node.TEXT_NODE) {
				String value = getSingleNodeText(child);
				if (value != null && !isIgnorableWhiteSpace(value, false)) {
					p.parseRegularText(value, expandURLs, true, getHyperlinkSettings(), false, false);
				}
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				String name = child.getNodeName();
				if ("a".equalsIgnoreCase(name)) { //$NON-NLS-1$
					segment = processHyperlinkSegment(child, getHyperlinkSettings());
				} else if ("span".equalsIgnoreCase(name)) { //$NON-NLS-1$
					processTextSegment(p, expandURLs, child);
				} else if ("b".equalsIgnoreCase(name)) { //$NON-NLS-1$
					String text = getNodeText(child);
					p.parseRegularText(text, expandURLs, true, getHyperlinkSettings(), true, false);
				} else if ("i".equalsIgnoreCase(name)) { //$NON-NLS-1$
					String text = getNodeText(child);
					p.parseRegularText(text, expandURLs, true, getHyperlinkSettings(), false, true);
				} else if ("br".equalsIgnoreCase(name)) { //$NON-NLS-1$
					segment = new XmlBreakSegment(font);
				} else {
					String value = getNodeText(child);
					if (value != null && !isIgnorableWhiteSpace(value, false)) {
						p.parseRegularText(value, expandURLs, true, getHyperlinkSettings(), false, false);
					}
				}
			}
			if (segment != null) { p.addSegment(segment); }
		}
	}

	/**
	 * Checks if is ignorable white space.
	 *
	 * @param text
	 *            the text
	 * @param ignoreSpaces
	 *            the ignore spaces
	 * @return true, if is ignorable white space
	 */
	private boolean isIgnorableWhiteSpace(final String text, final boolean ignoreSpaces) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (ignoreSpaces && c == ' ') { continue; }
			if (c == '\n' || c == '\r' || c == '\f') { continue; }
			return false;
		}
		return true;
	}

	/**
	 * Append text.
	 *
	 * @param value
	 *            the value
	 * @param buf
	 *            the buf
	 * @param spaceCounter
	 *            the space counter
	 */
	private void appendText(final String value, final StringBuilder buf, final int[] spaceCounter) {
		if (!whitespaceNormalized) {
			buf.append(value);
		} else {
			for (int j = 0; j < value.length(); j++) {
				char c = value.charAt(j);
				switch (c) {
					case ' ':
					case '\t':
						// space
						if (++spaceCounter[0] == 1) { buf.append(c); }
						break;
					case '\n':
					case '\r':
					case '\f':
						// new line
						if (++spaceCounter[0] == 1) { buf.append(' '); }
						break;
					default:
						// other characters
						spaceCounter[0] = 0;
						buf.append(c);
						break;
				}
			}
		}
	}

	/**
	 * Gets the normalized text.
	 *
	 * @param text
	 *            the text
	 * @return the normalized text
	 */
	private String getNormalizedText(final String text) {
		int[] spaceCounter = new int[1];
		StringBuilder buf = new StringBuilder();

		if (text == null) return null;
		appendText(text, buf, spaceCounter);
		return buf.toString();
	}

	/**
	 * Gets the single node text.
	 *
	 * @param node
	 *            the node
	 * @return the single node text
	 */
	private String getSingleNodeText(final Node node) {
		String text = getNormalizedText(node.getNodeValue());
		if (!whitespaceNormalized) return text;
		if (text.length() > 0 && node.getPreviousSibling() == null && isIgnorableWhiteSpace(text.substring(0, 1), true))
			return text.substring(1);
		if (text.length() > 1 && node.getNextSibling() == null
				&& isIgnorableWhiteSpace(text.substring(text.length() - 1), true))
			return text.substring(0, text.length() - 1);
		return text;
	}

	/**
	 * Gets the node text.
	 *
	 * @param node
	 *            the node
	 * @return the node text
	 */
	private String getNodeText(final Node node) {
		NodeList children = node.getChildNodes();
		StringBuilder buf = new StringBuilder();
		int[] spaceCounter = new int[1];

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				String value = child.getNodeValue();
				appendText(value, buf, spaceCounter);
			}
		}
		if (whitespaceNormalized) return buf.toString().trim();
		return buf.toString();
	}

	/**
	 * Process hyperlink segment.
	 *
	 * @param link
	 *            the link
	 * @param settings
	 *            the settings
	 * @return the paragraph segment
	 */
	private XmlParagraphSegment processHyperlinkSegment(final Node link, final HyperlinkSettings settings) {
		NamedNodeMap atts = link.getAttributes();
		String href = null;
		boolean wrapAllowed = true;
		boolean bold = false;
		boolean italic = false;

		Node hrefAtt = atts.getNamedItem("href"); //$NON-NLS-1$
		if (hrefAtt != null) { href = hrefAtt.getNodeValue(); }
		Node boldAtt = atts.getNamedItem("bold"); //$NON-NLS-1$
		if (boldAtt != null) { bold = true; }
		Node itAtt = atts.getNamedItem("italic"); //$NON-NLS-1$
		if (itAtt != null) { italic = true; }
		Node nowrap = atts.getNamedItem("nowrap"); //$NON-NLS-1$
		if (nowrap != null) {
			String value = nowrap.getNodeValue();
			if ("true".equalsIgnoreCase(value)) { //$NON-NLS-1$
				wrapAllowed = false;
			}
		}
		XmlHyperlinkSegment segment = new XmlHyperlinkSegment(getNodeText(link), settings, bold, italic, font);
		segment.setHref(href);
		Node alt = atts.getNamedItem("alt"); //$NON-NLS-1$
		if (alt != null) { segment.setTooltipText(alt.getNodeValue()); }
		segment.setWordWrapAllowed(wrapAllowed);
		return segment;
	}

	/**
	 * Process text segment.
	 *
	 * @param p
	 *            the p
	 * @param expandURLs
	 *            the expand UR ls
	 * @param textNode
	 *            the text node
	 */
	private void processTextSegment(final XmlParagraph p, final boolean expandURLs, final Node textNode) {
		String text = getNodeText(textNode);

		NamedNodeMap atts = textNode.getAttributes();
		boolean wrapAllowed = true;
		Node nowrap = atts.getNamedItem("nowrap"); //$NON-NLS-1$
		if (nowrap != null) {
			String value = nowrap.getNodeValue();
			if ("true".equalsIgnoreCase(value)) { //$NON-NLS-1$
				wrapAllowed = false;
			}
		}
		p.parseRegularText(text, expandURLs, wrapAllowed, getHyperlinkSettings(), false, false);
	}

	/**
	 * Parses the regular text.
	 *
	 * @param regularText
	 *            the regular text
	 * @param convertURLs
	 *            the convert UR ls
	 */
	public void parseRegularText(String regularText, final boolean convertURLs) {
		reset();

		if (regularText == null) return;

		regularText = getNormalizedText(regularText);

		XmlParagraph p = new XmlParagraph(true, font);
		paragraphs.add(p);
		int pstart = 0;

		for (int i = 0; i < regularText.length(); i++) {
			char c = regularText.charAt(i);
			if (p == null) {
				p = new XmlParagraph(true, font);
				paragraphs.add(p);
			}
			if (c == '\n') {
				String text = regularText.substring(pstart, i);
				pstart = i + 1;
				p.parseRegularText(text, convertURLs, true, getHyperlinkSettings(), false, false);
				p = null;
			}
		}
		if (p != null) {
			// no new line
			String text = regularText.substring(pstart);
			p.parseRegularText(text, convertURLs, true, getHyperlinkSettings(), false, false);
		}
	}

	/**
	 * Gets the hyperlink settings.
	 *
	 * @return the hyperlink settings
	 */
	public HyperlinkSettings getHyperlinkSettings() {
		// #132723 cannot have null settings
		if (hyperlinkSettings == null) { hyperlinkSettings = new HyperlinkSettings(SWTUtil.getStandardDisplay()); }
		return hyperlinkSettings;
	}

	/**
	 * Sets the hyperlink settings.
	 *
	 * @param settings
	 *            the new hyperlink settings
	 */
	public void setHyperlinkSettings(final HyperlinkSettings settings) { this.hyperlinkSettings = settings; }

	/**
	 * Reset.
	 */
	private void reset() {
		if (paragraphs == null) { paragraphs = new Vector<>(); }
		paragraphs.clear();
		selectedSegmentIndex = -1;
		selectableSegments = null;
	}

	/**
	 * Gets the focus selectable segments.
	 *
	 * @return the focus selectable segments
	 */
	IFocusSelectable[] getFocusSelectableSegments() {
		if (selectableSegments != null || paragraphs == null) return selectableSegments;
		List<XmlParagraphSegment> result = new ArrayList<>();
		for (XmlParagraph paragraph : paragraphs) {
			XmlParagraphSegment[] segments = paragraph.getSegments();
			for (XmlParagraphSegment segment : segments) {
				if (segment instanceof IFocusSelectable) { result.add(segment); }
			}
		}
		selectableSegments = result.toArray(new IFocusSelectable[result.size()]);
		return selectableSegments;
	}

	/**
	 * Gets the hyperlink.
	 *
	 * @param index
	 *            the index
	 * @return the hyperlink
	 */
	public IHyperlinkSegment getHyperlink(final int index) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		if (selectables.length > index) {
			IFocusSelectable link = selectables[index];
			if (link instanceof IHyperlinkSegment) return (IHyperlinkSegment) link;
		}
		return null;
	}

	/**
	 * Find hyperlink at.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the i hyperlink segment
	 */
	public IHyperlinkSegment findHyperlinkAt(final int x, final int y) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		for (IFocusSelectable segment : selectables) {
			if (segment instanceof IHyperlinkSegment link && link.contains(x, y)) return link;
		}
		return null;
	}

	/**
	 * Gets the hyperlink count.
	 *
	 * @return the hyperlink count
	 */
	public int getHyperlinkCount() { return getFocusSelectableSegments().length; }

	/**
	 * Index of.
	 *
	 * @param link
	 *            the link
	 * @return the int
	 */
	public int indexOf(final IHyperlinkSegment link) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		for (int i = 0; i < selectables.length; i++) {
			IFocusSelectable segment = selectables[i];
			if (segment instanceof IHyperlinkSegment l && link == l) return i;
		}
		return -1;
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
		for (XmlParagraph paragraph : paragraphs) {
			XmlParagraphSegment segment = paragraph.findSegmentAt(x, y);
			if (segment != null) return segment;
		}
		return null;
	}

	/**
	 * Gets the selected segment.
	 *
	 * @return the selected segment
	 */
	public IFocusSelectable getSelectedSegment() {
		if (selectableSegments == null || selectedSegmentIndex == -1) return null;
		return selectableSegments[selectedSegmentIndex];
	}

	/**
	 * Gets the selected segment index.
	 *
	 * @return the selected segment index
	 */
	public int getSelectedSegmentIndex() { return selectedSegmentIndex; }

	/**
	 * Link exists.
	 *
	 * @param link
	 *            the link
	 * @return true, if successful
	 */
	public boolean linkExists(final IHyperlinkSegment link) {
		if (selectableSegments == null) return false;
		for (IFocusSelectable selectableSegment : selectableSegments) { if (selectableSegment == link) return true; }
		return false;
	}

	/**
	 * Select link.
	 *
	 * @param link
	 *            the link
	 */
	public void selectLink(final IHyperlinkSegment link) {
		if (link == null) {
			// savedSelectedLinkIndex = selectedSegmentIndex;
			selectedSegmentIndex = -1;
		} else {
			select(link);

		}
	}

	/**
	 * Select.
	 *
	 * @param selectable
	 *            the selectable
	 */
	public void select(final IFocusSelectable selectable) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		selectedSegmentIndex = -1;
		if (selectables == null) return;
		for (int i = 0; i < selectables.length; i++) {
			if (selectables[i].equals(selectable)) {
				selectedSegmentIndex = i;
				break;
			}
		}
	}

	/**
	 * Checks for focus segments.
	 *
	 * @return true, if successful
	 */
	public boolean hasFocusSegments() {
		IFocusSelectable[] segments = getFocusSelectableSegments();
		if (segments.length > 0) return true;
		return false;
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		paragraphs = null;
		selectedSegmentIndex = -1;
		selectableSegments = null;
	}

	/**
	 * @return Returns the whitespaceNormalized.
	 */
	public boolean isWhitespaceNormalized() { return whitespaceNormalized; }

	/**
	 * @param whitespaceNormalized
	 *            The whitespaceNormalized to set.
	 */
	public void setWhitespaceNormalized(final boolean whitespaceNormalized) {
		this.whitespaceNormalized = whitespaceNormalized;
	}

}
