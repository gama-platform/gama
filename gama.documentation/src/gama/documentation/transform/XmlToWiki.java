/*******************************************************************************************************
 *
 * XmlToWiki.java, in gama.documentation, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.documentation.transform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gama.annotations.precompiler.doc.utils.Constants;
import gama.annotations.precompiler.doc.utils.XMLElements;
import gama.annotations.precompiler.doc.utils.XMLUtils;
import gama.documentation.util.WorkspaceManager;

/**
 * The Class XmlToWiki.
 */
public class XmlToWiki {

	/** The suffix. */
	public static final String SUFFIX = ""; // "Dev"
	
	/** The ext file name. */
	public static final String EXT_FILE_NAME = "Extension";
	
	/** The ext folder. */
	public static final String EXT_FOLDER = "PluginDocumentation/";

	/**
	 * Creates the all wikis.
	 *
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TransformerException the transformer exception
	 */
	public static void createAllWikis()
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		System.out.println("Beginning of the transformation");

		System.out.print("Creation of the wiki page for Operators.....");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-Operators-xml2md.xsl",
				Constants.XML2WIKI_FOLDER + File.separator + "Operators" + SUFFIX + ".md");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-OperatorsSplitted-xml2md.xsl",
				Constants.XML2WIKI_FOLDER + File.separator + "OperatorsSplitted" + SUFFIX + ".md");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-OperatorsAA-xml2md.xsl",
				Constants.WIKI_FOLDER_WIKI_ONLY + File.separator + "OperatorsAA" + SUFFIX + ".md");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-OperatorsBC-xml2md.xsl",
				Constants.WIKI_FOLDER_WIKI_ONLY + File.separator + "OperatorsBC" + SUFFIX + ".md");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-OperatorsDH-xml2md.xsl",
				Constants.WIKI_FOLDER_WIKI_ONLY + File.separator + "OperatorsDH" + SUFFIX + ".md");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-OperatorsIM-xml2md.xsl",
				Constants.WIKI_FOLDER_WIKI_ONLY + File.separator + "OperatorsIM" + SUFFIX + ".md");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-OperatorsNR-xml2md.xsl",
				Constants.WIKI_FOLDER_WIKI_ONLY + File.separator + "OperatorsNR" + SUFFIX + ".md");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-OperatorsSZ-xml2md.xsl",
				Constants.WIKI_FOLDER_WIKI_ONLY + File.separator + "OperatorsSZ" + SUFFIX + ".md");
		System.out.println("Done"); 
		//
		System.out.print("Creation of the wiki page for Statements.....");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-Statements-xml2md.xsl",
				Constants.XML2WIKI_FOLDER + File.separator + "Statements" + SUFFIX + ".md");
		System.out.println("Done");
		//
		System.out.print("Creation of the wiki page for Skills.......");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-Skills-xml2md.xsl",
				Constants.XML2WIKI_FOLDER + File.separator + "BuiltInSkills" + SUFFIX + ".md");
		System.out.println("Done");
		//
		System.out.print("Creation of the wiki page for the Index.......");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-Index-xml2md.xsl",
				Constants.WIKI_FOLDER + File.separator + "Exhaustive-list-of-GAMA-Keywords" + SUFFIX + ".md");
		System.out.println("Done");
		//
		System.out.print("Creation of the wiki page for Architectures.......");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-Architectures-xml2md.xsl",
				Constants.XML2WIKI_FOLDER + File.separator + "BuiltInArchitectures" + SUFFIX + ".md");
		System.out.println("Done");

		System.out.print("Creation of the wiki page for Built-in Species.....");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-Species-xml2md.xsl",
				Constants.XML2WIKI_FOLDER + File.separator + "BuiltInSpecies" + SUFFIX + ".md");
		System.out.println("Done");
		//
		System.out.print("Creation of the wiki page for Constants and units.....");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-Constants-xml2md.xsl",
				Constants.XML2WIKI_FOLDER + File.separator + "UnitsAndConstants" + SUFFIX + ".md");
		System.out.println("Done");
		//
		System.out.print("Creation of the page for Constants and units (PDF format).....");
		createWiki(Constants.DOCGAMA_GLOBAL_FILE,
				Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-ConstantsPDF-xml2md.xsl",
				Constants.XML2WIKI_FOLDER + File.separator + "UnitsAndConstantsPDF" + SUFFIX + ".md");
		System.out.println("Done");
		//
		System.out.println("End of the transformation");
	}

	/**
	 * Creates the wiki.
	 *
	 * @param xml the xml
	 * @param xsl the xsl
	 * @param wiki the wiki
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TransformerException the transformer exception
	 */
	private static void createWiki(final String xml, final String xsl, final String wiki)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		createWiki(xml, xsl, wiki, "");
	}

	/**
	 * Creates the wiki.
	 *
	 * @param xml the xml
	 * @param xsl the xsl
	 * @param wiki the wiki
	 * @param pluginName the plugin name
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TransformerException the transformer exception
	 */
	private static void createWiki(final String xml, final String xsl, final String wiki, final String pluginName)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		// Creation of the DOM source
		final DocumentBuilderFactory fabriqueD = DocumentBuilderFactory.newInstance();
		final DocumentBuilder constructeur = fabriqueD.newDocumentBuilder();
		final File fileXml = new File(xml);
		final Document document = constructeur.parse(fileXml);

		// We the pluginName in the doc root
		final NodeList nLDoc = document.getElementsByTagName(XMLElements.DOC);
		final org.w3c.dom.Element eltDoc = (org.w3c.dom.Element) nLDoc.item(0);
		eltDoc.setAttribute(XMLElements.ATT_DOC_PLUGINNAME, pluginName);

		// We add index to example to distinguish variables in the doc
		final NodeList nLOperators = document.getElementsByTagName(XMLElements.OPERATOR);
		for (int j = 0; j < nLOperators.getLength(); j++) {
			final org.w3c.dom.Element eltOperator = (org.w3c.dom.Element) nLOperators.item(j);
			final NodeList nLExamples = eltOperator.getElementsByTagName(XMLElements.EXAMPLE);
			for (int k = 0; k < nLExamples.getLength(); k++) {
				final org.w3c.dom.Element eltExample = (org.w3c.dom.Element) nLExamples.item(k);
				eltExample.setAttribute(XMLElements.ATT_EXAMPLE_INDEX, "" + k);
			}
		}

		XMLUtils.transformDocument(document, xsl, wiki);
	}

	/**
	 * Creates the extentions wiki.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws TransformerException the transformer exception
	 */
	public static void createExtentionsWiki()
			throws IOException, ParserConfigurationException, SAXException, TransformerException {
		final WorkspaceManager ws = new WorkspaceManager(".", false);
		final HashMap<String, File> hmExt = ws.getExtensionsDocFiles();

		// Create the G__Extensions.md file as a toc to each G__extensions_plugin.md files
		final String pathExtension = Constants.WIKI_FOLDER_EXT + File.separator + EXT_FILE_NAME + SUFFIX + ".md";
		final File ext = new File(pathExtension);
		try (FileWriter fw = new FileWriter(ext); BufferedWriter extBw = new BufferedWriter(fw);) {

			extBw.write("# Extensions");
			extBw.newLine();
			extBw.write("----");
			extBw.newLine();
			extBw.write("**This file is automatically generated from java files. Do Not Edit It.**");
			extBw.newLine();
			extBw.newLine();
			extBw.write("----");
			extBw.newLine();
			extBw.newLine();
			extBw.write("## Introduction");
			extBw.newLine();
			extBw.write(
					"This page provides a link to each of the extension pages. Extensions are the additional GAMA plugins that exist in the GAMA GitHub repository but will not be packaged with the release. **These extensions are not maintained by GAMA core team but by their authors.**");
			extBw.newLine();

			for (final String pluginName : hmExt.keySet()) {
				extBw.write("* [" + pluginName + "](" + EXT_FOLDER + EXT_FILE_NAME + "_" + pluginName + ")");
				extBw.newLine();
			}
		}

		// Create 1 G__extension_plugin.md file per plugin
		for (final Entry<String, File> docPlug : hmExt.entrySet()) {
			System.out.print("Creation of the wiki pages for extension: " + docPlug.getKey());
			createWiki(docPlug.getValue().getAbsolutePath(),
					Constants.XSL_XML2WIKI_FOLDER + File.separator + "docGama-Extensions-xml2md.xsl",
					Constants.WIKI_FOLDER_EXT_PLUGIN + File.separator + EXT_FILE_NAME + "_" + docPlug.getKey() + SUFFIX
							+ ".md",
					docPlug.getKey());
			System.out.println("Done");
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		createExtentionsWiki();
		// createAllWikis();
	}

}
