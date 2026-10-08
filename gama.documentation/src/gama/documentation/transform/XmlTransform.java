/*******************************************************************************************************
 *
 * XmlTransform.java, in gama.documentation, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.documentation.transform;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import gama.documentation.util.XMLUtils;

/**
 * The Class XmlTransform.
 */
public class XmlTransform {

	/**
	 * Transform XML.
	 *
	 * @param xml
	 *            the xml
	 * @param xsl
	 *            the xsl
	 * @param output
	 *            the output
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws SAXException
	 *             the SAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *             the transformer exception
	 */
	public static void transformXML(final String xml, final String xsl, final String output)
			throws ParserConfigurationException, SAXException, IOException {
		// Creation of the DOM source
		DocumentBuilderFactory fabriqueD = DocumentBuilderFactory.newInstance();
		DocumentBuilder constructeur = fabriqueD.newDocumentBuilder();
		File fileXml = new File(xml);
		Document document = constructeur.parse(fileXml);

		XMLUtils.transformDocument(document, xsl, output);
	}
}
