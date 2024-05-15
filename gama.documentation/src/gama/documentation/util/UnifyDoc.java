/*******************************************************************************************************
 *
 * UnifyDoc.java, in gama.documentation, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.documentation.util;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import gama.annotations.precompiler.doc.utils.Constants;
import gama.annotations.precompiler.doc.utils.TypeConverter;
import gama.annotations.precompiler.doc.utils.XMLElements;

/**
 * The Class UnifyDoc.
 */
public class UnifyDoc {

	/** The tab elt XML. */
	private static String[] tabEltXML = { XMLElements.CONCEPT_LIST, XMLElements.OPERATORS_CATEGORIES,
			XMLElements.OPERATORS, XMLElements.SKILLS, XMLElements.ARCHITECTURES, XMLElements.SPECIESS,
			XMLElements.STATEMENTS, XMLElements.CONSTANTS_CATEGORIES, XMLElements.CONSTANTS,
			XMLElements.INSIDE_STAT_KINDS, XMLElements.INSIDE_STAT_SYMBOLS, XMLElements.STATEMENT_KINDS,
			XMLElements.TYPES, XMLElements.FILES };
	// among tebEltXML, categories do not need to have an additional projectName
	// attribute
//	private static String[] tabCategoriesEltXML = { XMLElements.OPERATORS_CATEGORIES, XMLElements.CONSTANTS_CATEGORIES,
//			XMLElements.INSIDE_STAT_KINDS, XMLElements.INSIDE_STAT_SYMBOLS, XMLElements.STATEMENT_KINDS,
//			XMLElements.CONCEPT_LIST };

	/**
	 * Unify.
	 *
	 * @param local the local
	 */
	public static void unify(boolean local) {
		try {

			WorkspaceManager ws = new WorkspaceManager(".",local);
			HashMap<String, File> hmFiles = ws.getProductDocFiles();

			Document doc = mergeFiles(hmFiles);

			System.out.println("" + hmFiles);

			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(doc, Files.newOutputStream(new File(Constants.DOCGAMA_GLOBAL_FILE).toPath()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Unify all projects.
	 *
	 * @param local the local
	 */
	public static void unifyAllProjects(boolean local) {
		try {

			WorkspaceManager ws = new WorkspaceManager(".", local);
	 		HashMap<String, File> hmFiles = local ? ws.getAllDocFilesLocal() : ws.getAllDocFiles();			

			Document doc = mergeFiles(hmFiles);

			System.out.println("" + hmFiles);

			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(doc, Files.newOutputStream(new File(Constants.DOCGAMA_GLOBAL_FILE).toPath()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Merge files.
	 *
	 * @param hmFilesPackages the hm files packages
	 * @return the document
	 */
	private static Document mergeFiles(final HashMap<String, File> hmFilesPackages) {
		try {

			SAXBuilder builder = new SAXBuilder();
			Document doc = null;

			doc = new Document(new Element(XMLElements.DOC));
			for (String elt : tabEltXML) {
				doc.getRootElement().addContent(new Element(elt));
			}

			for (Entry<String, File> fileDoc : hmFilesPackages.entrySet()) {
				Document docTemp = builder.build(fileDoc.getValue());

				for (String catXML : tabEltXML) {
					if (docTemp.getRootElement().getChild(catXML) != null) {

						List<Element> existingElt = doc.getRootElement().getChild(catXML).getChildren();

						for (Element e : docTemp.getRootElement().getChild(catXML).getChildren()) {
							// Do not add the projectName for every kinds of
							// categories
					//		if (!Arrays.asList(tabCategoriesEltXML).contains(catXML)) {
								e.setAttribute("projectName", fileDoc.getKey());
					//		}

							// Test whether the element is already in the merged
							// doc
							boolean found = false;
							for (Element exElt : existingElt) {
								boolean equals = exElt.getName().equals(e.getName());
								for (Attribute att : exElt.getAttributes()) {
									String valueExElt = exElt.getAttribute(att.getName()) != null
											? exElt.getAttributeValue(att.getName()) : "";
									String valueE = e.getAttribute(att.getName()) != null
											? e.getAttributeValue(att.getName()) : "";
									equals = equals && valueExElt.equals(valueE);
								}
								found = found || equals;
							}
							// Add if it is not already in the merged doc
							if (!found) {
								doc.getRootElement().getChild(catXML).addContent(e.clone());
							}
						}
					}
				}
			}

			// Add an element for the generated types
			doc.getRootElement().getChild(XMLElements.OPERATORS_CATEGORIES).addContent(new Element(XMLElements.CATEGORY)
					.setAttribute(XMLElements.ATT_CAT_ID, new TypeConverter().getProperCategory("Types")));

			return doc;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(final String[] args) {
		try {
			UnifyDoc.unify(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
