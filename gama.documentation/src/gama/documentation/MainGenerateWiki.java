/*******************************************************************************************************
 *
 * MainGenerateWiki.java, in gama.documentation, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.documentation;

import java.io.File;

import gama.annotations.precompiler.doc.utils.Constants;
import gama.documentation.transform.XmlToCategoryXML;
import gama.documentation.transform.XmlToWiki;
import gama.documentation.transform.XmlTransform;
import gama.documentation.util.GamaStyleGeneration;
import gama.documentation.util.PrepareEnv;
import gama.documentation.util.UnifyDoc;

/**
 * The Class MainGenerateWiki.
 */
public class MainGenerateWiki {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(final String[] args) { 
		try {

			// generate the wiki documentation
			System.out.println("GENERATION OF THE WIKI DOCUMENTATION FROM JAVA CODE");
			System.out.println("Please notice that the docGAMA.xml files should have been previously generated..");
			System.out.print("Preparation of the folders................");
			PrepareEnv.prepareDocumentation();
			System.out.println("DONE\n");

			System.out.print("Merge all the docGAMA.xml files................");
			UnifyDoc.unify((args.length > 0) ? (args[0].equals("-online") ? false : true) : true);
			System.out.println("DONE\n");

			System.out.print(
					"Transform the docGAMA.xml file into Wiki Files (md) and create/update them in the gama.wiki folder................");
			XmlToWiki.createAllWikis();
			XmlToWiki.createExtentionsWiki();
			System.out.println("DONE\n");

			System.out.print("Creation of the page for keywords.....");
			XmlToCategoryXML.createCategoryWiki();
			System.out.println("DONE\n");				
			
			
			System.out.print("GENERATION of the prism highlight JS file.....");
			// Creation of the DOM source
			XmlTransform.transformXML(Constants.DOCGAMA_GLOBAL_FILE, 
					Constants.XSL_XML2PRISM_FOLDER + File.separator + "docGama-xml2prism.xsl", 
					Constants.PRISM_GEN_FOLDER + File.separator + "prism-gaml.js");	
			System.out.println("DONE\n");
			
			System.out.print("GENERATION of latex file.....");			
			GamaStyleGeneration.generateGamaStyle();
			System.out.println("DONE\n");


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
