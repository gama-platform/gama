/*******************************************************************************************************
 *
 * Constants.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.annotations.precompiler.doc.utils;

import java.io.File;

/**
 * The Class Constants.
 */
public class Constants {

	/** The Constant GAMA_VERSION. */
	public final static String GAMA_VERSION = "0.0.0-SNAPSHOT";


	/** The Constant RELEASE_APPLICATION. */
	//
	public final static String RELEASE_APPLICATION = "gama.product";

	/** The Constant RELEASE_PRODUCT. */
	public final static String RELEASE_PRODUCT = "gama.product";

	// Repositories containing used files


	/** The Constant SRC_FOLDER. */
	public final static String SRC_FOLDER = "files";

	/** The Constant GEN_FOLDER. */
	public final static String GEN_FOLDER = SRC_FOLDER + File.separator + "gen";

	/** The Constant INPUT_FOLDER. */
	public final static String INPUT_FOLDER = SRC_FOLDER + File.separator + "input";

	/** The Constant TEST_FOLDER. */
	public final static String TEST_FOLDER = SRC_FOLDER + File.separator + ".." + File.separator + ".." + File.separator
			+ "gama.library" + File.separator + "models" + File.separator + "Tests";

	/** The Constant WIKI_FOLDER. */
	public final static String WIKI_FOLDER = SRC_FOLDER + File.separator + ".." + File.separator + ".." + File.separator
			+ ".." + File.separator + "gama.wiki" + File.separator + "gama.wiki";
	// public static String WIKI_FOLDER = "C:/git/gama.wiki";

	/** The Constant WIKI_FOLDER_EXT. */
	// Generation folders
	public final static String WIKI_FOLDER_EXT = WIKI_FOLDER + File.separator + "References";

	/** The Constant WIKI_FOLDER_REF. */
	public final static String WIKI_FOLDER_REF =
			WIKI_FOLDER + File.separator + "References" + File.separator + "GAMLReferences";

	/** The Constant WIKI_FOLDER_WIKI_ONLY. */
	public final static String WIKI_FOLDER_WIKI_ONLY = WIKI_FOLDER + File.separator + "WikiOnly";

	/** The Constant PATH_TO_KEYWORDS_XML. */
	public final static String PATH_TO_KEYWORDS_XML = WIKI_FOLDER + File.separator + "keywords.xml";

	/** The Constant WIKI_FOLDER_EXT_PLUGIN. */
	public final static String WIKI_FOLDER_EXT_PLUGIN = WIKI_FOLDER_EXT + File.separator + "PluginDocumentation";

	/** The Constant XML2WIKI_FOLDER. */
	public final static String XML2WIKI_FOLDER = WIKI_FOLDER_REF;

	/** The Constant JAVA2XML_FOLDER. */
	public final static String JAVA2XML_FOLDER = GEN_FOLDER + File.separator + "java2xml";

	/** The Constant PDF_FOLDER. */
	public final static String PDF_FOLDER = GEN_FOLDER + File.separator + "pdf";

	/** The Constant TOC_GEN_FOLDER. */
	public final static String TOC_GEN_FOLDER = GEN_FOLDER + File.separator + "toc2pdf";

	/** The Constant XML_KEYWORD_GEN_FOLDER. */
	public final static String XML_KEYWORD_GEN_FOLDER = GEN_FOLDER + File.separator + "xmlKeywords";

	/** The Constant CATALOG_GEN_FOLDER. */
	public final static String CATALOG_GEN_FOLDER = GEN_FOLDER + File.separator + "catalog";

	/** The Constant PRISM_GEN_FOLDER. */
	public final static String PRISM_GEN_FOLDER = GEN_FOLDER + File.separator + "prism";
	
	/** The Constant LATEX_STYLE_GEN_FOLDER */
	public final static String LATEX_STYLE_GEN_FOLDER = GEN_FOLDER + File.separator + "latexStyle";	

	/** The Constant XSL_XML2WIKI_FOLDER. */
	// Inputs Folders
	public final static String XSL_XML2WIKI_FOLDER = INPUT_FOLDER + File.separator + "xsl" + File.separator + "xml2md";

	/** The Constant XSL_XML2JSON_FOLDER. */
	public final static String XSL_XML2JSON_FOLDER =
			INPUT_FOLDER + File.separator + "xsl" + File.separator + "xml2json";

	/** The Constant XSL_XML2TEST_FOLDER. */
	public final static String XSL_XML2TEST_FOLDER =
			INPUT_FOLDER + File.separator + "xsl" + File.separator + "xml2test";

	/** The Constant XSL_XML2KEYWORDS_XML_FOLDER. */
	public final static String XSL_XML2KEYWORDS_XML_FOLDER =
			INPUT_FOLDER + File.separator + "xsl" + File.separator + "xml2keywordsXml";

	/** The Constant XSL_XML2PRISM_FOLDER. */
	public final static String XSL_XML2PRISM_FOLDER =
			INPUT_FOLDER + File.separator + "xsl" + File.separator + "xml2prism";

	/** The Constant XSL_XML2LLM_FOLDER. */
	public final static String XSL_XML2LLM_FOLDER =
			INPUT_FOLDER + File.separator + "xsl" + File.separator + "xml2docLLM";
	
	
	/** The Constant DOCGAMA_FILE. */
	public final static String DOCGAMA_FILE = "target" + File.separator + "docGAMA.xml";

	/** The Constant DOCGAMA_FILE_LOCAL. */
	public final static String DOCGAMA_FILE_LOCAL = "gaml" + File.separator + "docGAMA.xml";

	/** The Constant DOCGAMA_GLOBAL_FILE. */
	public final static String DOCGAMA_GLOBAL_FILE = JAVA2XML_FOLDER + File.separator + "docGAMAglobal.xml";

	/** The Constant TEST_PLUGIN_FOLDER. */
	// Tests
	public final static String TEST_PLUGIN_FOLDER = "tests";

	/** The Constant TEST_PLUGIN_GEN_FOLDER. */
	public final static String TEST_PLUGIN_GEN_FOLDER = TEST_PLUGIN_FOLDER + File.separator + "Generated";

	/** The Constant TEST_PLUGIN_GEN_MODELS. */
	public final static String TEST_PLUGIN_GEN_MODELS = TEST_PLUGIN_GEN_FOLDER + File.separator + "models";

	/** The Constant TEST_OPERATORS_FOLDER. */
	public final static String TEST_OPERATORS_FOLDER = "Operators";

	/** The Constant TEST_STATEMENTS_FOLDER. */
	public final static String TEST_STATEMENTS_FOLDER = "Statements";

	/** The Constant PROJECT_FILE. */
	public final static String PROJECT_FILE = INPUT_FOLDER + File.separator + "project" + File.separator + ".project";
}
