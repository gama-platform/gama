/*******************************************************************************************************
 *
 * XmlToCategoryXML.java, in gama.documentation, is part of the source code of the GAMA modeling and simulation platform
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

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import gama.documentation.util.Constants;

/**
 * The Class XmlToCategoryXML.
 */
public class XmlToCategoryXML {

	/**
	 * Creates the category wiki.
	 *
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws SAXException
	 *             the SAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void createCategoryWiki() throws ParserConfigurationException, SAXException, IOException {
		XmlTransform.transformXML(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2KEYWORDS_XML_FOLDER + File.separator + "docGama-KeywordsXML.xsl",
				Constants.PATH_TO_KEYWORDS_XML);
	}

}
