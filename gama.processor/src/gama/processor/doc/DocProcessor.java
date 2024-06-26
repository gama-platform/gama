/*******************************************************************************************************
 *
 * DocProcessor.java, in gama.processor, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.processor.doc;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IConstantCategory;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.constant;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.doc.utils.TypeConverter;
import gama.annotations.precompiler.doc.utils.XMLElements;
import gama.processor.Constants;
import gama.processor.ElementProcessor;
import gama.processor.ProcessorContext;

/**
 * The Class DocProcessor.
 */
public class DocProcessor extends ElementProcessor<doc> {

	/** The Constant CAST_METHOD. */
	public static final String CAST_METHOD = "cast";
	
	/** The mes. */
	Messager mes;
	
	/** The tc. */
	TypeConverter tc;
	
	/** The document. */
	public Document document;

	// boolean firstParsing;

	/** The nbr operators. */
	// Statistic values
	int nbrOperators;
		
	/** The nbr skills. */
	int nbrSkills;
	
	/** The nbr symbols. */
	int nbrSymbols;

	/**
	 * Instantiates a new doc processor.
	 */
	public DocProcessor() {
		// firstParsing = true;
		nbrOperators = 0;
		nbrSkills = 0;
		nbrSymbols = 0;
		tc = new TypeConverter();
	}

	@Override
	public boolean outputToJava() {
		return false;
	}

	@Override
	public void createElement(final StringBuilder sb, final ProcessorContext context, final Element e,
			final doc annotation) {}

	@Override
	protected Class<doc> getAnnotationClass() {
		return doc.class;
	}

	/**
	 * Gets the root name.
	 *
	 * @return the root name
	 */
	protected String getRootName() {
		return "doc";
	}

	/**
	 * Gets the root node.
	 *
	 * @param doc the doc
	 * @return the root node
	 */
	protected org.w3c.dom.Element getRootNode(final Document doc) {
		org.w3c.dom.Element root = null;
		if (doc.hasChildNodes()) {
			root = (org.w3c.dom.Element) doc.getElementsByTagName(getRootName()).item(0);
		}
		if (root == null) {
			root = doc.createElement(getRootName());
			doc.appendChild(root);
		}
		return root;
	}

	@Override
	public void process(final ProcessorContext context) {
		if (!context.shouldProduceDoc()) { return; }
		document = context.getBuilder().newDocument();
		// if (!firstParsing)
		// return;
		// firstParsing = false;
		mes = context.getMessager();
		final org.w3c.dom.Element root = getRootNode(document);

		// ////////////////////////////////////////////////
		// /// Parsing of Constants Categories
		final Set<? extends Element> setConstants = context.getElementsAnnotatedWith(constant.class);

		root.appendChild(this.processDocXMLCategories(setConstants, XMLElements.CONSTANTS_CATEGORIES));

		// ////////////////////////////////////////////////
		// /// Parsing of Concepts
		final Field[] conceptArray = IConcept.class.getFields();

		root.appendChild(this.processDocXMLConcepts(conceptArray, XMLElements.CONCEPT_LIST));

		// ////////////////////////////////////////////////
		// /// Parsing of Constants
		root.appendChild(this.processDocXMLConstants(setConstants));

		// ////////////////////////////////////////////////
		// /// Parsing of Operators Categories
		@SuppressWarnings ("unchecked") 
		final Set<? extends ExecutableElement> setOperatorsCategories =
				(Set<? extends ExecutableElement>) context.getElementsAnnotatedWith(operator.class);
		root.appendChild(this.processDocXMLCategories(setOperatorsCategories, XMLElements.OPERATORS_CATEGORIES));

		// ////////////////////////////////////////////////
		// /// Parsing of Operators
		@SuppressWarnings ("unchecked") 
		final Set<? extends ExecutableElement> setOperators =
				(Set<? extends ExecutableElement>) context.getElementsAnnotatedWith(operator.class);
		root.appendChild(this.processDocXMLOperators(setOperators));

		// ////////////////////////////////////////////////
		// /// Parsing of Skills
		final Set<? extends Element> setSkills = context.getElementsAnnotatedWith(skill.class);
		root.appendChild(this.processDocXMLSkills(setSkills, context));

		// ////////////////////////////////////////////////
		// /// Parsing of Architectures
		final Set<? extends Element> setArchitectures = context.getElementsAnnotatedWith(skill.class);
		root.appendChild(this.processDocXMLArchitectures(setArchitectures, context));

		// ////////////////////////////////////////////////
		// /// Parsing of Species
		@SuppressWarnings ("unchecked") 		
		final Set<? extends TypeElement> setSpecies = (Set<? extends TypeElement>) context.getElementsAnnotatedWith(species.class);
		root.appendChild(this.processDocXMLSpecies(setSpecies));

		// ////////////////////////////////////////////////
		// /// Parsing of Inside statements (kinds and symbols)
		final Set<? extends Element> setStatements = context.getElementsAnnotatedWith(symbol.class);
		root.appendChild(this.processDocXMLStatementsInsideKind(setStatements));
		root.appendChild(this.processDocXMLStatementsInsideSymbol(setStatements));

		// ////////////////////////////////////////////////
		// /// Parsing of Statements
		root.appendChild(this.processDocXMLStatementsKinds(setStatements));
		root.appendChild(this.processDocXMLStatements(setStatements));

		// ////////////////////////////////////////////////
		// /// Parsing of Types to get operators
		final Set<? extends Element> setOperatorsTypes = context.getElementsAnnotatedWith(type.class);
		final ArrayList<org.w3c.dom.Element> listEltOperatorsFromTypes =
				this.processDocXMLOperatorsFromTypes(setOperatorsTypes);

		final org.w3c.dom.Element eltOperators =
				(org.w3c.dom.Element) root.getElementsByTagName(XMLElements.OPERATORS).item(0);
		for (final org.w3c.dom.Element eltOp : listEltOperatorsFromTypes) {
			eltOperators.appendChild(eltOp);
		}

		// ////////////////////////////////////////////////
		// /// Parsing of Files to get operators
		final Set<? extends Element> setFilesOperators = context.getElementsAnnotatedWith(file.class);
		final ArrayList<org.w3c.dom.Element> listEltOperatorsFromFiles =
				this.processDocXMLOperatorsFromFiles(setFilesOperators);

		for (final org.w3c.dom.Element eltOp : listEltOperatorsFromFiles) {
			eltOperators.appendChild(eltOp);
		}

		// ////////////////////////////////////////////////
		// /// Parsing of Files

		// TODO : manage to get the documentation...
		final Set<? extends Element> setFiles = context.getElementsAnnotatedWith(file.class);
		root.appendChild(this.processDocXMLTypes(setFiles));

		// ////////////////////////////////////////////////
		// /// Parsing of Types
		final Set<? extends Element> setTypes = context.getElementsAnnotatedWith(type.class);
		root.appendChild(this.processDocXMLTypes(setTypes, context));

		// //////////////////////
		// Final step:
		// document.appendChild(root);

		// ////////////////////////////////////////////////

		try (final PrintWriter out = new PrintWriter(context.createWriter("docGAMA.xml"));) {
			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //"ISO-8859-1"); 
			transformer.transform(new DOMSource(document), new StreamResult(out));
		} catch (final TransformerException e) {
			context.emitError("XML Error when producing the documentation: " + e.getMessage(), e);
		}
	}

	/**
	 * Process doc XML constants.
	 *
	 * @param set the set
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLConstants(final Set<? extends Element> set) {
		final org.w3c.dom.Element eltConstants = document.createElement(XMLElements.CONSTANTS);
		for (final Element e : set) {
			if (e.getAnnotation(constant.class).value().equals(e.getSimpleName().toString())) {
				final org.w3c.dom.Element eltConstant =
						getConstantElt(e.getAnnotation(constant.class), document, e, mes, tc);

				// Concept
				org.w3c.dom.Element conceptsElt;
				if (eltConstant.getElementsByTagName(XMLElements.CONCEPTS).getLength() == 0) {
					conceptsElt = getConcepts(e, document, document.createElement(XMLElements.CONCEPTS), tc);
				} else {
					conceptsElt = getConcepts(e, document,
							(org.w3c.dom.Element) eltConstant.getElementsByTagName(XMLElements.CONCEPTS).item(0), tc);
				}

				eltConstant.appendChild(conceptsElt);

				eltConstants.appendChild(eltConstant);
			}
		}
		
		return eltConstants;
	}

	/**
	 * Process doc XML operators from types.
	 *
	 * @param set the set
	 * @return the array list
	 */
	private ArrayList<org.w3c.dom.Element> processDocXMLOperatorsFromTypes(final Set<? extends Element> set) {

		// Parcours de tous les types
		// creation d'ojets types dans le XML
		// ajout d'

		final ArrayList<org.w3c.dom.Element> eltOpFromTypes = new ArrayList<>();
		for (final Element e : set) {
			// Operators to be created:
			// - name_type: converts the parameter into the type name_type
			final Operator op_type = new Operator(document, tc.getProperCategory("Types"),
					e.getAnnotation(type.class).concept(), e.getAnnotation(type.class).name(), 
					"casts the operand in a " + e.getAnnotation(type.class).name() + " object.");
			org.w3c.dom.Element docElt = null;
		
			Operands ops = new Operands(document, ((TypeElement) e).getQualifiedName().toString(), "", e.getAnnotation(type.class).name(), "");
			ops.addOperand(new Operand(document, "val", 0, "any"));
			op_type.addOperands(ops);
			
			///////////*****************
			for( Element elt : ((TypeElement) e).getEnclosedElements()) {
//				mes.printMessage(Kind.WARNING, ((TypeElement) e).getQualifiedName().toString() + " - " + elt.getSimpleName() + " - " + elt.getSimpleName().toString().equals("staticCast") + " - " + elt.getKind().equals(ElementKind.METHOD));
				if(ElementKind.METHOD.equals(elt.getKind()) &&  CAST_METHOD.equals(elt.getSimpleName().toString()) ) {
					doc docMethod = elt.getAnnotation(doc.class);
//					mes.printMessage(Kind.WARNING, "--------" + ((TypeElement) e).getQualifiedName().toString() + " - " + elt.getSimpleName() + " " + (docMethod));
					
					
					if (docMethod != null) {
						docElt = getDocElt(docMethod, document, mes, "Operator " + ((TypeElement) e).getQualifiedName().toString(), tc, (ExecutableElement) elt, null) ;
					}	
				}				
			}
			
			////////*****************

			// op_type.setDocumentation("Casts the operand into the type " + e.getAnnotation(type.class).name());

			org.w3c.dom.Element elt = op_type.getElementDOM();
			if(docElt != null) {
				elt.appendChild(docElt);				
			}
			
			eltOpFromTypes.add(elt);
		}

		return eltOpFromTypes;
	}

	/**
	 * Process doc XML types.
	 *
	 * @param setFiles the set files
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLTypes(final Set<? extends Element> setFiles) {
		final org.w3c.dom.Element files = document.createElement(XMLElements.FILES);

		/*
		 * @file ( name = "csv", extensions = { "csv", "tsv" }, buffer_type = IType.MATRIX, buffer_index = IType.POINT,
		 * concept = { IConcept.CSV, IConcept.FILE }, doc = @doc
		 * ("A type of text file that contains comma-separated values"))
		 */

		for (final Element e : setFiles) {
			if (e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
				// Just omit it
			} else {
				final org.w3c.dom.Element file = document.createElement(XMLElements.FILE);
				file.setAttribute(XMLElements.ATT_FILE_NAME, e.getAnnotation(file.class).name());
				file.setAttribute(XMLElements.ATT_FILE_BUFFER_TYPE,
						tc.getProperType("" + e.getAnnotation(file.class).buffer_type()));
				file.setAttribute(XMLElements.ATT_FILE_BUFFER_INDEX,
						tc.getProperType("" + e.getAnnotation(file.class).buffer_index()));
				file.setAttribute(XMLElements.ATT_FILE_BUFFER_CONTENT,
						tc.getProperType("" + e.getAnnotation(file.class).buffer_content()));

				// Parsing extensions
				final org.w3c.dom.Element extensions = document.createElement(XMLElements.EXTENSIONS);
				for (final String ext : e.getAnnotation(file.class).extensions()) {
					final org.w3c.dom.Element extElt = document.createElement(XMLElements.EXTENSION);
					extElt.setAttribute(XMLElements.ATT_NAME, ext);
					extensions.appendChild(extElt);
				}
				file.appendChild(extensions);

				// Parsing of concept
				org.w3c.dom.Element conceptsElt;
				if (file.getElementsByTagName(XMLElements.CONCEPTS).getLength() == 0) {
					conceptsElt = getConcepts(e, document, document.createElement(XMLElements.CONCEPTS), tc);
				} else {
					conceptsElt = getConcepts(e, document,
							(org.w3c.dom.Element) file.getElementsByTagName(XMLElements.CONCEPTS).item(0), tc);
				}
				file.appendChild(conceptsElt);

				files.appendChild(file);
			}
		}
		return files;
	}

	/**
	 * Process doc XML operators from files.
	 *
	 * @param set the set
	 * @return the array list
	 */
	private ArrayList<org.w3c.dom.Element> processDocXMLOperatorsFromFiles(final Set<? extends Element> set) {

		final ArrayList<org.w3c.dom.Element> eltOpFromTypes = new ArrayList<>();
		for (final Element e : set) {
			// Operators to be created:
			// - "is_"+name : test whether the operand parameter is of the given
			// kind of file
			// - name+"_file": converts the parameter into the type name_type
			
			////////////////////////////////////////
			// Operator "is_" + name
			final Operator op_is = new Operator(document, tc.getProperCategory("Files"),
					e.getAnnotation(file.class).concept(), "is_" + e.getAnnotation(file.class).name(),
					"Tests whether the operand is a " + e.getAnnotation(file.class).name() + " file.");

			Operands ops_is = new Operands(document, ((TypeElement) e).getQualifiedName().toString(), "", "bool", "");
			ops_is.addOperand(new Operand(document, "val", 0, "any"));
			op_is.addOperands(ops_is);
			
			
			////////////////////////////////////////
			// Operator  name + "_file"
			final Operator op_file = new Operator(document, tc.getProperCategory("Files"),
					e.getAnnotation(file.class).concept(), e.getAnnotation(file.class).name() + "_file");
			
			for( Element elt : ((TypeElement) e).getEnclosedElements()) {
				// Parse all the constructors to define:
				// - the possible uses
				// - a documentation for each constructor as an usage
				if(elt.getKind().equals(ElementKind.CONSTRUCTOR)) {
					Operands ops_file = new Operands(document, ((TypeElement) e).getQualifiedName().toString(), "", "file", "");
					int arity = 0;
					StringBuilder str = new StringBuilder();
					str.append(e.getAnnotation(file.class).name());
					str.append("_file(");
					
					// Define the various possible uses 
					// Parse the constructor parameters
					for(VariableElement var : ((ExecutableElement) elt).getParameters()) {
						String operandName = var.getSimpleName().toString();
						String typeName = tc.getProperType(var.asType().toString());
						if(!typeName.contains("IScope")) {
							ops_file.addOperand(new Operand(document, operandName, arity, tc.getProperType(var.asType().toString())));	
							arity++;		
							str.append(tc.getProperType(var.asType().toString()));
							str.append(",");
						}						
					}
					op_file.addOperands(ops_file);	
					
					final String op_name_usage = str.substring(0, str.length()-1) + ")";
					
					// Create the documentation for each constructor as an usage
					// when reading a @doc annotation:
					// - value : becomes a usage name
					// - examples : become example of THIS usage
					doc docConstructor = elt.getAnnotation(doc.class) ;
					
					org.w3c.dom.Element exElt = null;
					if (docConstructor != null) {
						exElt = getExamplesElt(docConstructor.examples(), document, (ExecutableElement) elt, tc, null);					
					}	
					op_file.addUsage(op_name_usage + ": " + ( (docConstructor != null) ? docConstructor.value() : ""), exElt );
					
				}
			}
			
			final String[] tabExtension = e.getAnnotation(file.class).extensions();
			StringBuilder listExtensions = new StringBuilder();
			if (tabExtension.length > 0) {
				listExtensions.append(tabExtension[0]);
				if (tabExtension.length > 1) {
					for (int i = 1; i < tabExtension.length; i++) {
						listExtensions.append(", ");
						listExtensions.append(tabExtension[i]);
					}
				}
			}
			op_file.setDocumentation("Constructs a file of type " + e.getAnnotation(file.class).name()
					+ ". Allowed extensions are limited to " + listExtensions.toString());

			op_file.addSeeAlso("is_" + e.getAnnotation(file.class).name());
			op_is.addSeeAlso(e.getAnnotation(file.class).name() + "_file");

			eltOpFromTypes.add(op_is.getElementDOM());
			eltOpFromTypes.add(op_file.getElementDOM());
		}

		return eltOpFromTypes;
	}

	/**
	 * Process doc XML categories.
	 *
	 * @param set the set
	 * @param typeElement the type element
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLCategories(final Set<? extends Element> set, final String typeElement) {
		final org.w3c.dom.Element categories = document.createElement(typeElement);

		// When we parse categories of operators, we add the iterator category.
		if (XMLElements.OPERATORS_CATEGORIES.equals(typeElement)) {
			org.w3c.dom.Element category;
			category = document.createElement(XMLElements.CATEGORY);
			category.setAttribute(XMLElements.ATT_CAT_ID, IOperatorCategory.ITERATOR);
			final org.w3c.dom.Element child = category;
			categories.appendChild(child);
		}

		for (final Element e : set) {
			String[] categoryNames = new String[1];
			// String categoryName;
			if (e.getAnnotation(operator.class) != null && e.getAnnotation(operator.class).category().length > 0) {
				categoryNames = e.getAnnotation(operator.class).category();
			} else if (e.getAnnotation(constant.class) != null
					&& e.getAnnotation(constant.class).category().length > 0) {
				categoryNames = e.getAnnotation(constant.class).category();
			} else {
				categoryNames[0] = tc.getProperCategory(e.getEnclosingElement().getSimpleName().toString());
			}

			final NodeList nL = categories.getElementsByTagName(XMLElements.CATEGORY);

			for (final String categoryName : categoryNames) {
				if (!IOperatorCategory.DEPRECATED.equals(categoryName)) {
					int i = 0;
					boolean found = false;
					while (!found && i < nL.getLength()) {
						final org.w3c.dom.Element elt = (org.w3c.dom.Element) nL.item(i);
						if (categoryName.equals(tc.getProperCategory(elt.getAttribute(XMLElements.ATT_CAT_ID)))) {
							found = true;
						}
						i++;
					}

					if (!found) {
						org.w3c.dom.Element category;
						category = document.createElement(XMLElements.CATEGORY);
						category.setAttribute(XMLElements.ATT_CAT_ID, categoryName);
						final org.w3c.dom.Element child = category;
						categories.appendChild(child);
					}
				}
			}
		}
		return categories;
	}

	/**
	 * Process doc XML concepts.
	 *
	 * @param conceptArray the concept array
	 * @param typeElement the type element
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLConcepts(final Field[] conceptArray, final String typeElement) {
		final org.w3c.dom.Element concepts = document.createElement(typeElement);
		for (final Field field : conceptArray) {
			org.w3c.dom.Element conceptElem;
			conceptElem = document.createElement(XMLElements.CONCEPT);
			try {
				conceptElem.setAttribute(XMLElements.ATT_CAT_ID, field.get(new Object()).toString());
			} catch (final DOMException e) {
				e.printStackTrace();
			} catch (final IllegalArgumentException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			}
			concepts.appendChild(conceptElem);
		}
		return concepts;
	}

	/**
	 * Process doc XML operators.
	 *
	 * @param set the set
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLOperators(final Set<? extends ExecutableElement> set) {
		final org.w3c.dom.Element operators = document.createElement(XMLElements.OPERATORS);

		for (final ExecutableElement e : set) {
			if (e.getAnnotation(operator.class).internal() == true
					|| e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
				// Just omit it
			} else {

				nbrOperators++;
				final List<? extends VariableElement> args = e.getParameters();
				final Set<Modifier> m = e.getModifiers();
				final boolean isStatic = m.contains(Modifier.STATIC);
				int arity = 0;

				if (e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
					// We just omit it
				} else {
					// Look for an already parsed operator with the same name
					org.w3c.dom.Element operator =
							getOperatorElement(operators, e.getAnnotation(operator.class).value()[0]);
					if (operator == null) {
						operator = document.createElement(XMLElements.OPERATOR);
						operator.setAttribute(XMLElements.ATT_OP_ID,
								tc.getProperOperatorName(e.getAnnotation(operator.class).value()[0]));
						operator.setAttribute(XMLElements.ATT_OP_NAME,
								tc.getProperOperatorName(e.getAnnotation(operator.class).value()[0]));

						operator.setAttribute(XMLElements.ATT_ALPHABET_ORDER,
								Constants.getAlphabetOrder(e.getAnnotation(operator.class).value()[0]));
					}
					// Parse the alternative names of the operator
					// we will create one operator markup per alternative name
					for (final String name : e.getAnnotation(operator.class).value()) {
						if (!"".equals(name) && !name.equals(e.getAnnotation(operator.class).value()[0])) {
							// Look for an already parsed operator with the same
							// name
							org.w3c.dom.Element altElt = getOperatorElement(operators, name);
							if (altElt == null) {
								altElt = document.createElement(XMLElements.OPERATOR);
								altElt.setAttribute(XMLElements.ATT_OP_ID, name);
								altElt.setAttribute(XMLElements.ATT_OP_NAME, name);
								altElt.setAttribute(XMLElements.ATT_OP_ALT_NAME,
										e.getAnnotation(operator.class).value()[0]);
								altElt.setAttribute(XMLElements.ATT_ALPHABET_ORDER, Constants.getAlphabetOrder(name));

								altElt.appendChild(getCategories(e, document, tc));
								operators.appendChild(altElt);
							} else {
								// Show an error in the case where two
								// alternative names do not refer to
								// the same operator
								// if (!e.getAnnotation(operator.class).value()[0]
								// .equals(altElt.getAttribute(XMLElements.ATT_OP_ALT_NAME))) {
								// mes.printMessage(Kind.WARNING,
								// "The alternative name __" + name
								// + "__ is used for two different operators: "
								// + e.getAnnotation(operator.class).value()[0] + " and "
								// + altElt.getAttribute("alternativeNameOf"));
								// }
							}
						}
					}

					// Parse of categories

					// Category
					org.w3c.dom.Element categoriesElt;
					if (operator.getElementsByTagName(XMLElements.OPERATOR_CATEGORIES).getLength() == 0) {
						categoriesElt =
								getCategories(e, document, document.createElement(XMLElements.OPERATOR_CATEGORIES), tc);
					} else {
						categoriesElt = getCategories(e, document, (org.w3c.dom.Element) operator
								.getElementsByTagName(XMLElements.OPERATOR_CATEGORIES).item(0), tc);
					}
					operator.appendChild(categoriesElt);

					// Concept
					org.w3c.dom.Element conceptsElt;

					if (operator.getElementsByTagName(XMLElements.CONCEPTS).getLength() == 0) {
						conceptsElt = getConcepts(e, document, document.createElement(XMLElements.CONCEPTS), tc);
					} else {
						conceptsElt = getConcepts(e, document,
								(org.w3c.dom.Element) operator.getElementsByTagName(XMLElements.CONCEPTS).item(0), tc);
					}
					operator.appendChild(conceptsElt);

					// Parse the combination operands / result
					org.w3c.dom.Element combinaisonOpResElt;
					if (operator.getElementsByTagName(XMLElements.COMBINAISON_IO).getLength() == 0) {
						combinaisonOpResElt = document.createElement(XMLElements.COMBINAISON_IO);
					} else {
						combinaisonOpResElt =
								(org.w3c.dom.Element) operator.getElementsByTagName(XMLElements.COMBINAISON_IO).item(0);
					}

					final org.w3c.dom.Element operands = document.createElement(XMLElements.OPERANDS);
					operands.setAttribute("returnType", tc.getProperType(e.getReturnType().toString()));
					operands.setAttribute("contentType", "" + e.getAnnotation(operator.class).content_type());
					operands.setAttribute("type", "" + e.getAnnotation(operator.class).type());

					// To specify where we can find the source code of the class
					// defining the operator
					String pkgName = "" + ((TypeElement) e.getEnclosingElement()).getQualifiedName();
					// Now we have to deal with Spatial operators, that are
					// defined in inner classes
					if (pkgName.contains("Spatial")) {
						// We do not take into account what is after 'Spatial'
						pkgName = pkgName.split("Spatial")[0] + "Spatial";
					}
					pkgName = pkgName.replace('.', '/');
					pkgName = pkgName + ".java";
					operands.setAttribute("class", pkgName);

					if (!isStatic) {
						final org.w3c.dom.Element operand = document.createElement(XMLElements.OPERAND);
						operand.setAttribute(XMLElements.ATT_OPERAND_TYPE,
								tc.getProperType(e.getEnclosingElement().asType().toString()));
						operand.setAttribute(XMLElements.ATT_OPERAND_POSITION, "" + arity);
						arity++;
						operand.setAttribute(XMLElements.ATT_OPERAND_NAME,
								e.getEnclosingElement().asType().toString().toLowerCase());
						operands.appendChild(operand);
					}
					if (args.size() > 0) {
						final int first_index = args.get(0).asType().toString().contains("IScope") ? 1 : 0;
						for (int i = first_index; i <= args.size() - 1; i++) {
							final org.w3c.dom.Element operand = document.createElement(XMLElements.OPERAND);
							operand.setAttribute(XMLElements.ATT_OPERAND_TYPE,
									tc.getProperType(args.get(i).asType().toString()));
							operand.setAttribute(XMLElements.ATT_OPERAND_POSITION, "" + arity);
							arity++;
							operand.setAttribute(XMLElements.ATT_OPERAND_NAME, args.get(i).getSimpleName().toString());
							operands.appendChild(operand);
						}
					}
					// operator.setAttribute("arity", ""+arity);
					combinaisonOpResElt.appendChild(operands);
					operator.appendChild(combinaisonOpResElt);

					// /////////////////////////////////////////////////////
					// Parsing of the documentation
					org.w3c.dom.Element docElt;
					if (operator.getElementsByTagName(XMLElements.DOCUMENTATION).getLength() == 0) {
						docElt = getDocElt(e.getAnnotation(doc.class), document, mes,
								"Operator " + operator.getAttribute("name"), tc, e, operator);
					} else {
						docElt = getDocElt(e.getAnnotation(doc.class), document,
								(org.w3c.dom.Element) operator.getElementsByTagName(XMLElements.DOCUMENTATION).item(0),
								mes, "Operator " + operator.getAttribute("name"), tc, e, operator);
					}

					if (docElt != null) {
						operator.appendChild(docElt);
					}

					operators.appendChild(operator);
				}
			}
		}
		
		// To manage see Also bidirectional, we need to go through all of the operators
		NodeList ops = operators.getElementsByTagName(XMLElements.OPERATOR);
		for(int i =0; i < ops.getLength();i++) {		
			org.w3c.dom.Element eltOperator = (org.w3c.dom.Element) ops.item(i);
			String eltName = eltOperator.getAttribute(XMLElements.ATT_OP_NAME);
			NodeList sees = eltOperator.getElementsByTagName(XMLElements.SEE);
			
			// For each see, 
			//	find the element with the proper name
			//	add to each of them a see to the current element, if it is not present yet
			for(int j =0; j < sees.getLength();j++) {	
				List<Node> l_elts = searchForElementOfInterest(operators, ((org.w3c.dom.Element) sees.item(j)).getAttribute(XMLElements.ATT_SEE_ID));
				if(l_elts.size() > 0) {
					org.w3c.dom.Element targetOperatorelt = (org.w3c.dom.Element) l_elts.get(0);
					org.w3c.dom.Element seeAlsoElt;
					if (targetOperatorelt.getElementsByTagName(XMLElements.SEEALSO).getLength() != 0) {
						seeAlsoElt = (org.w3c.dom.Element) targetOperatorelt.getElementsByTagName(XMLElements.SEEALSO).item(0);
					} else {
						seeAlsoElt = document.createElement(XMLElements.SEEALSO);
					}
					
					DocProcessor.addIfNotExistSee(document, seeAlsoElt, eltName);					
				}
			}
		}
		
		return operators;
	}
	
	// from https://stackoverflow.com/questions/23360278/how-to-use-org-w3c-dom-nodelist-with-java-8-stream-api
	public List<Node> searchForElementOfInterest(org.w3c.dom.Element elt, String eltName) {
	        NodeList nList = elt.getElementsByTagName(XMLElements.OPERATOR);

	        // since NodeList does not have stream implemented, then use this hack
	        Stream<Node> nodeStream = IntStream.range(0, nList.getLength()).mapToObj(nList::item);
	        // search for element of interest in the NodeList
	        List<Node> l = nodeStream.parallel()
	        						.filter( x -> DocProcessor.isElementOfInterestWithName(x,eltName))
	        						.collect(Collectors.toList());

	        return l;
	}

	private static boolean isElementOfInterestWithName(Node nNode, String n) {
	        boolean bFound=false;
	        if ((nNode != null) && (nNode.getNodeType() == Node.ELEMENT_NODE)) {
	        	org.w3c.dom.Element eElement = (org.w3c.dom.Element) nNode;
	        	
	            if(n.equals(eElement.getAttribute("name"))) {
                    bFound = true;	            	
	            }
	        }
	        return bFound;
	}

	
	

	/**
	 * Process doc XML architectures.
	 *
	 * @param setArchis the set archis
	 * @param env the env
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLArchitectures(final Set<? extends Element> setArchis,
			final RoundEnvironment env) {
		final org.w3c.dom.Element archis = document.createElement(XMLElements.ARCHITECTURES);

		for (final Element e : setArchis) {
			if (e.getAnnotation(skill.class).internal() == true
					|| e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
				// Just omit it
			} else if (ElementTypeUtils.isArchitectureElement((TypeElement) e, mes)) {
				final org.w3c.dom.Element archiElt = document.createElement(XMLElements.ARCHITECTURE);

				archiElt.setAttribute(XMLElements.ATT_ARCHI_ID, e.getAnnotation(skill.class).name());
				archiElt.setAttribute(XMLElements.ATT_ARCHI_NAME, e.getAnnotation(skill.class).name());

				final org.w3c.dom.Element docEltArchi = getDocElt(e.getAnnotation(doc.class), document, mes,
						e.getSimpleName().toString(), tc, null, archiElt);
				if (docEltArchi != null) {
					archiElt.appendChild(docEltArchi);
				}

				// Parsing of vars
				final org.w3c.dom.Element varsElt =
						getVarsElt(e.getAnnotation(vars.class), document, mes, archiElt.getAttribute("name"), tc);

				if (varsElt != null) {
					archiElt.appendChild(varsElt);
				}

				// Parsing of actions
				final org.w3c.dom.Element actionsElt = document.createElement(XMLElements.ACTIONS);

				for (final Element eltMethod : e.getEnclosedElements()) {
					final org.w3c.dom.Element actionElt =
							getActionElt(eltMethod.getAnnotation(action.class), document, mes, eltMethod, tc);

					if (actionElt != null) {
						actionsElt.appendChild(actionElt);
					}
				}
				archiElt.appendChild(actionsElt);

				// Concept
				org.w3c.dom.Element conceptsElt;

				if (archiElt.getElementsByTagName(XMLElements.CONCEPTS).getLength() == 0) {
					conceptsElt = getConcepts(e, document, document.createElement(XMLElements.CONCEPTS), tc);
				} else {
					conceptsElt = getConcepts(e, document,
							(org.w3c.dom.Element) archiElt.getElementsByTagName(XMLElements.CONCEPTS).item(0), tc);
				}
				archiElt.appendChild(conceptsElt);

				archis.appendChild(archiElt);
			}
		}

		return archis;
	}

	/**
	 * Process doc XML skills.
	 *
	 * @param setSkills the set skills
	 * @param env the env
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLSkills(final Set<? extends Element> setSkills,
			final RoundEnvironment env) {

		final org.w3c.dom.Element skills = document.createElement(XMLElements.SKILLS);

		for (final Element e : setSkills) {
			boolean emptySkill = true;

			if (e.getAnnotation(skill.class).internal() == true
					|| e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
				// Just omit it
			} else if (!ElementTypeUtils.isArchitectureElement((TypeElement) e, mes)) {

				nbrSkills++;
				final org.w3c.dom.Element skillElt = document.createElement(XMLElements.SKILL);

				skillElt.setAttribute(XMLElements.ATT_SKILL_ID, e.getAnnotation(skill.class).name());
				skillElt.setAttribute(XMLElements.ATT_SKILL_NAME, e.getAnnotation(skill.class).name());

				// get extends
				skillElt.setAttribute(XMLElements.ATT_SKILL_CLASS, ((TypeElement) e).getQualifiedName().toString());
				skillElt.setAttribute(XMLElements.ATT_SKILL_EXTENDS, ((TypeElement) e).getSuperclass().toString());

				final org.w3c.dom.Element docEltSkill = getDocElt(e.getAnnotation(doc.class), document, mes,
						e.getSimpleName().toString(), tc, null, skillElt);
				if (docEltSkill != null) {
					skillElt.appendChild(docEltSkill);
					emptySkill = false;
				}

				// Parsing of vars
				final org.w3c.dom.Element varsElt =
						getVarsElt(e.getAnnotation(vars.class), document, mes, skillElt.getAttribute("name"), tc);

				if (varsElt != null) {
					skillElt.appendChild(varsElt);

					if (varsElt.getElementsByTagName(XMLElements.VAR).getLength() != 0) {
						emptySkill = false;
					}
				}

				// Parsing of actions
				final org.w3c.dom.Element actionsElt = document.createElement(XMLElements.ACTIONS);

				for (final Element eltMethod : e.getEnclosedElements()) {
					final org.w3c.dom.Element actionElt =
							getActionElt(eltMethod.getAnnotation(action.class), document, mes, eltMethod, tc);

					if (actionElt != null) {
						actionsElt.appendChild(actionElt);
						emptySkill = false;
					}
				}
				skillElt.appendChild(actionsElt);

				// Concept
				org.w3c.dom.Element conceptsElt;

				if (skillElt.getElementsByTagName(XMLElements.CONCEPTS).getLength() == 0) {
					conceptsElt = getConcepts(e, document, document.createElement(XMLElements.CONCEPTS), tc);
				} else {
					conceptsElt = getConcepts(e, document,
							(org.w3c.dom.Element) skillElt.getElementsByTagName(XMLElements.CONCEPTS).item(0), tc);
				}
				skillElt.appendChild(conceptsElt);

				if (!emptySkill) {
					skills.appendChild(skillElt);
				}
			}
		}
		// check the inheritance between Skills
		final NodeList nlSkill = skills.getElementsByTagName(XMLElements.SKILL);
		for (int i = 0; i < nlSkill.getLength(); i++) {
			final org.w3c.dom.Element elt = (org.w3c.dom.Element) nlSkill.item(i);
			if (elt.hasAttribute(XMLElements.ATT_SKILL_EXTENDS)) {
				if (BASIC_SKILL.equals(elt.getAttribute(XMLElements.ATT_SKILL_EXTENDS))) {
					elt.setAttribute(XMLElements.ATT_SKILL_EXTENDS, "");
				} else {
					for (int j = 0; j < nlSkill.getLength(); j++) {
						final org.w3c.dom.Element testedElt = (org.w3c.dom.Element) nlSkill.item(j);
						if (testedElt.getAttribute(XMLElements.ATT_SKILL_CLASS)
								.equals(elt.getAttribute(XMLElements.ATT_SKILL_EXTENDS))) {
							elt.setAttribute(XMLElements.ATT_SKILL_EXTENDS,
									testedElt.getAttribute(XMLElements.ATT_SKILL_NAME));
						}
					}
				}
			}
		}

		return skills;
	}

	/**
	 * Process doc XML species.
	 *
	 * @param setSpecies the set species
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLSpecies(final Set<? extends TypeElement> setSpecies) {
		final org.w3c.dom.Element species = document.createElement(XMLElements.SPECIESS);

		for (final TypeElement e : setSpecies) {
			if (e.getAnnotation(species.class).internal() == true
					|| e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
				// Just omit it
			} else {
				final org.w3c.dom.Element spec = document.createElement(XMLElements.SPECIES);
				spec.setAttribute(XMLElements.ATT_SPECIES_ID, e.getAnnotation(species.class).name());
				spec.setAttribute(XMLElements.ATT_SPECIES_NAME, e.getAnnotation(species.class).name());

				final org.w3c.dom.Element docEltSpecies = getDocElt(e.getAnnotation(doc.class), document, mes,
						e.getSimpleName().toString(), tc, null, spec);
				if (docEltSpecies != null) {
					spec.appendChild(docEltSpecies);
				}

				// Parsing of actions
				final org.w3c.dom.Element actionsElt = document.createElement(XMLElements.ACTIONS);
				for (final Element eltMethod : e.getEnclosedElements()) {
					final org.w3c.dom.Element actionElt =
							getActionElt(eltMethod.getAnnotation(action.class), document, mes, eltMethod, tc);

					if (actionElt != null) {
						actionsElt.appendChild(actionElt);
					}
				}
				spec.appendChild(actionsElt);

				// Parsing of skills
				final org.w3c.dom.Element skillsElt = document.createElement(XMLElements.SPECIES_SKILLS);
				for (final String eltSkill : e.getAnnotation(species.class).skills()) {
					final org.w3c.dom.Element skillElt = document.createElement(XMLElements.SPECIES_SKILL);
					skillElt.setAttribute(XMLElements.ATT_SPECIES_SKILL, eltSkill);
					skillsElt.appendChild(skillElt);
				}
				spec.appendChild(skillsElt);

				// Parsing of vars
				org.w3c.dom.Element varsElt =
						getVarsElt(e.getAnnotation(vars.class), document, mes, spec.getAttribute("name"), tc);
				if (varsElt != null) {
					spec.appendChild(varsElt);
				} else { 
					// When no attribute is defined in a species, it gets the vars of the closest parent with @vars annotation
					// This is particularly useful for the agent species.
					varsElt =
							getVarsElt(ElementTypeUtils.getFirstImplementingInterfacesWithVars(e, mes).getAnnotation(vars.class), document, mes, spec.getAttribute("name"), tc);
					if (varsElt != null) {
						spec.appendChild(varsElt);
					} else {
						mes.printMessage(Kind.WARNING, "GAML Processor of @species: even parents do not have attributes...");
					}			
				}

				// Parsing of concept
				org.w3c.dom.Element conceptsElt;
				if (spec.getElementsByTagName(XMLElements.CONCEPTS).getLength() == 0) {
					conceptsElt = getConcepts(e, document, document.createElement(XMLElements.CONCEPTS), tc);
				} else {
					conceptsElt = getConcepts(e, document,
							(org.w3c.dom.Element) spec.getElementsByTagName(XMLElements.CONCEPTS).item(0), tc);
				}
				spec.appendChild(conceptsElt);

				species.appendChild(spec);
			}
		}
		return species;
	}

	/**
	 * Process doc XML statements inside symbol.
	 *
	 * @param setStatement the set statement
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLStatementsInsideSymbol(final Set<? extends Element> setStatement) {
		final org.w3c.dom.Element statementsInsideSymbolElt = document.createElement(XMLElements.INSIDE_STAT_SYMBOLS);
		final ArrayList<String> insideStatementSymbol = new ArrayList<>();

		for (final Element e : setStatement) {
			if (e.getAnnotation(symbol.class).internal() == true
					|| e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
				// We just omit it
			} else {
				final inside insideAnnot = e.getAnnotation(inside.class);

				if (insideAnnot != null) {
					for (final String sym : insideAnnot.symbols()) {
						if (!insideStatementSymbol.contains(sym)) {
							insideStatementSymbol.add(sym);
						}
					}
				}
			}
		}

		for (final String insName : insideStatementSymbol) {
			final org.w3c.dom.Element insideStatElt = document.createElement(XMLElements.INSIDE_STAT_SYMBOL);
			insideStatElt.setAttribute(XMLElements.ATT_INSIDE_STAT_SYMBOL, insName);
			statementsInsideSymbolElt.appendChild(insideStatElt);
		}

		return statementsInsideSymbolElt;
	}

	/**
	 * Process doc XML statements inside kind.
	 *
	 * @param setStatement the set statement
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLStatementsInsideKind(final Set<? extends Element> setStatement) {
		final org.w3c.dom.Element statementsInsideKindElt = document.createElement(XMLElements.INSIDE_STAT_KINDS);
		final ArrayList<String> insideStatementKind = new ArrayList<>();

		for (final Element e : setStatement) {
			if (e.getAnnotation(symbol.class).internal() == true
					|| e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
				// We just omit it
			} else {
				final inside insideAnnot = e.getAnnotation(inside.class);

				if (insideAnnot != null) {
					for (final int kind : insideAnnot.kinds()) {
						final String kindStr = tc.getSymbolKindStringFromISymbolKind(kind);
						if (!insideStatementKind.contains(kindStr)) {
							insideStatementKind.add(kindStr);
						}
					}
				}
			}
		}

		for (final String insName : insideStatementKind) {
			final org.w3c.dom.Element insideStatElt = document.createElement(XMLElements.INSIDE_STAT_KIND);
			insideStatElt.setAttribute(XMLElements.ATT_INSIDE_STAT_SYMBOL, insName);
			statementsInsideKindElt.appendChild(insideStatElt);
		}

		return statementsInsideKindElt;
	}

	/**
	 * Process doc XML statements kinds.
	 *
	 * @param setStatements the set statements
	 * @return the node
	 */
	private Node processDocXMLStatementsKinds(final Set<? extends Element> setStatements) {
		final org.w3c.dom.Element statementsKindsElt = document.createElement(XMLElements.STATEMENT_KINDS);
		final ArrayList<String> statementKinds = new ArrayList<>();

		for (final Element e : setStatements) {
			if (e.getAnnotation(symbol.class).internal() == true
					|| e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
				// We just omit it
			} else {
				final int kindAnnot = e.getAnnotation(symbol.class).kind();
				final String kindStr = tc.getSymbolKindStringFromISymbolKind(kindAnnot);
				if (!statementKinds.contains(kindStr)) {
					statementKinds.add(kindStr);
				}
			}
		}

		for (final String kindName : statementKinds) {
			final org.w3c.dom.Element kindStatElt = document.createElement(XMLElements.KIND);
			kindStatElt.setAttribute(XMLElements.ATT_KIND_STAT, kindName);
			statementsKindsElt.appendChild(kindStatElt);
		}

		return statementsKindsElt;
	}

	/**
	 * Process doc XML types.
	 *
	 * @param setTypes the set types
	 * @param env the env
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLTypes(final Set<? extends Element> setTypes, final RoundEnvironment env) {
		final org.w3c.dom.Element types = document.createElement(XMLElements.TYPES);

		for (final Element t : setTypes) {
			if (!t.getAnnotation(type.class).internal()) {
				final org.w3c.dom.Element typeElt = document.createElement(XMLElements.TYPE);

				typeElt.setAttribute(XMLElements.ATT_TYPE_NAME, t.getAnnotation(type.class).name());
				typeElt.setAttribute(XMLElements.ATT_TYPE_ID, "" + t.getAnnotation(type.class).id());
				typeElt.setAttribute(XMLElements.ATT_TYPE_KIND, "" + t.getAnnotation(type.class).kind());

				// /////////////////////////////////////////////////////
				// Parsing of the documentation
				if (t.getAnnotation(type.class).doc().length != 0) {
					final org.w3c.dom.Element docElt = getDocElt(t.getAnnotation(type.class).doc()[0], document, mes,
							t.getAnnotation(type.class).name(), tc, null, typeElt);
					typeElt.appendChild(docElt);
				}

				// Parsing of concept
				org.w3c.dom.Element conceptsElt;
				if (typeElt.getElementsByTagName(XMLElements.CONCEPTS).getLength() == 0) {
					conceptsElt = getConcepts(t, document, document.createElement(XMLElements.CONCEPTS), tc);
				} else {
					conceptsElt = getConcepts(t, document,
							(org.w3c.dom.Element) typeElt.getElementsByTagName(XMLElements.CONCEPTS).item(0), tc);
				}
				typeElt.appendChild(conceptsElt);

				types.appendChild(typeElt);
			}
		}

		return types;
	}

	/**
	 * Process doc XML statements.
	 *
	 * @param setStatement the set statement
	 * @return the org.w 3 c.dom. element
	 */
	private org.w3c.dom.Element processDocXMLStatements(final Set<? extends Element> setStatement) {
		final org.w3c.dom.Element statementsElt = document.createElement(XMLElements.STATEMENTS);

		for (final Element e : setStatement) {
			if (e.getAnnotation(symbol.class).internal() == true
					|| e.getAnnotation(doc.class) != null && !"".equals(e.getAnnotation(doc.class).deprecated())) {
				// We just omit it
			} else {
				nbrSymbols++;
				
				final org.w3c.dom.Element statElt = document.createElement(XMLElements.STATEMENT);
				if (e.getAnnotation(symbol.class).name().length != 0) {
					statElt.setAttribute(XMLElements.ATT_STAT_ID, e.getAnnotation(symbol.class).name()[0]);
					statElt.setAttribute(XMLElements.ATT_STAT_NAME, e.getAnnotation(symbol.class).name()[0]);
				} else {
					final String kind = tc.getSymbolKindStringFromISymbolKind(e.getAnnotation(symbol.class).kind())
							.replace("(", "").replace(")", "").replace(" ", "_");
					statElt.setAttribute(XMLElements.ATT_STAT_ID, kind);
					statElt.setAttribute(XMLElements.ATT_STAT_NAME, kind);
				}
				statElt.setAttribute(XMLElements.ATT_STAT_KIND,
						tc.getSymbolKindStringFromISymbolKind(e.getAnnotation(symbol.class).kind()));

				// Parsing of facets
				final org.w3c.dom.Element facetsElt = getFacetsElt(e.getAnnotation(facets.class), document, mes,
						statElt.getAttribute(XMLElements.ATT_STAT_NAME), tc);
				if (facetsElt != null) {
					statElt.appendChild(facetsElt);
				}

				// Parsing of documentation
				final org.w3c.dom.Element docstatElt = getDocElt(e.getAnnotation(doc.class), document, mes,
						"Statement " + statElt.getAttribute(XMLElements.ATT_STAT_NAME), tc, null, statElt);
				if (docstatElt != null) {
					statElt.appendChild(docstatElt);
				}

				// Parsing of inside
				final org.w3c.dom.Element insideElt = getInsideElt(e.getAnnotation(inside.class), document, tc);
				if (insideElt != null) {
					statElt.appendChild(insideElt);
				}

				// Parsing of concept
				org.w3c.dom.Element conceptsElt;
				if (statElt.getElementsByTagName(XMLElements.CONCEPTS).getLength() == 0) {
					conceptsElt = getConcepts(e, document, document.createElement(XMLElements.CONCEPTS), tc);
				} else {
					conceptsElt = getConcepts(e, document,
							(org.w3c.dom.Element) statElt.getElementsByTagName(XMLElements.CONCEPTS).item(0), tc);
				}
				statElt.appendChild(conceptsElt);

				statementsElt.appendChild(statElt);
				
				
				// Alternative names of the statement
				if (e.getAnnotation(symbol.class).name().length > 1) {

					for(int  i = 1; i < e.getAnnotation(symbol.class).name().length ; i++) {
						final org.w3c.dom.Element alternateElt = document.createElement(XMLElements.STATEMENT);

						alternateElt.setAttribute(XMLElements.ATT_STAT_ID, e.getAnnotation(symbol.class).name()[i]);
						alternateElt.setAttribute(XMLElements.ATT_STAT_NAME, e.getAnnotation(symbol.class).name()[i]);		
						alternateElt.setAttribute(XMLElements.ATT_STAT_ALT_NAME_OF, e.getAnnotation(symbol.class).name()[0]);
						alternateElt.setAttribute(XMLElements.ATT_STAT_KIND, 
								tc.getSymbolKindStringFromISymbolKind(e.getAnnotation(symbol.class).kind()));
						
						statementsElt.appendChild(alternateElt);
					}
				} 
				
				
				
			}
		}
		return statementsElt;
	}

	/**
	 * Gets the doc elt.
	 *
	 * @param docAnnot the doc annot
	 * @param doc the doc
	 * @param mes the mes
	 * @param eltName the elt name
	 * @param tc the tc
	 * @param e the e
	 * @param parentElement the parent element
	 * @return the doc elt
	 */
	public org.w3c.dom.Element getDocElt(final doc docAnnot, final Document doc, final Messager mes,
			final String eltName, final TypeConverter tc, final ExecutableElement e,
			final org.w3c.dom.Element parentElement) {
		return getDocElt(docAnnot, doc, null, mes, eltName, tc, e, parentElement);
	}

	/**
	 * Gets the doc elt.
	 *
	 * @param docAnnot the doc annot
	 * @param doc the doc
	 * @param docElement the doc element
	 * @param mes the mes
	 * @param eltName the elt name
	 * @param tc the tc
	 * @param e the e
	 * @param parentElement the parent element
	 * @return the doc elt
	 */
	public org.w3c.dom.Element getDocElt(final doc docAnnot, final Document doc, final org.w3c.dom.Element docElement,
			final Messager mes, final String eltName, final TypeConverter tc, final ExecutableElement e,
			final org.w3c.dom.Element parentElement) { // e.getSimpleName()
		org.w3c.dom.Element docElt = docElement;
	
		if (docAnnot == null) {
			// mes.printMessage(Kind.WARNING, "The element __" + eltName + "__ is not documented.");
		} else {
			if (docElt == null) {
				docElt = doc.createElement(XMLElements.DOCUMENTATION);
			}
	
			// Parse result
			final String value = docAnnot.value();
			final boolean masterDoc = docAnnot.masterDoc();
			if (! "".equals(value)) {
				if (docElt.getElementsByTagName(XMLElements.RESULT).getLength() != 0) {
					final org.w3c.dom.Element resultElt =
							(org.w3c.dom.Element) docElt.getElementsByTagName(XMLElements.RESULT).item(0);
					if ("true".equals(resultElt.getAttribute(XMLElements.ATT_RES_MASTER)) && masterDoc
							|| !"true".equals(resultElt.getAttribute(XMLElements.ATT_RES_MASTER)) && !masterDoc) {
						resultElt.setTextContent(resultElt.getTextContent() + "\n" + value);
					} else if (!resultElt.hasAttribute(XMLElements.ATT_RES_MASTER) && masterDoc) {
						resultElt.setTextContent(value);
						resultElt.setAttribute(XMLElements.ATT_RES_MASTER, "true");
					}
				} else {
					final org.w3c.dom.Element resultElt = doc.createElement(XMLElements.RESULT);
					resultElt.setTextContent(value);
					if (masterDoc) {
						resultElt.setAttribute(XMLElements.ATT_RES_MASTER, "true");
					}
					docElt.appendChild(resultElt);
				}
			}
			
			// Parse returns
			final String returns = docAnnot.returns();
			if ( ! "".equals(returns)) {
				if (docElt.getElementsByTagName(XMLElements.RETURNS).getLength() != 0) {
					final org.w3c.dom.Element returnstElt =
							(org.w3c.dom.Element) docElt.getElementsByTagName(XMLElements.RETURNS).item(0);
					if ("true".equals(returnstElt.getAttribute(XMLElements.ATT_RET_MASTER)) && masterDoc
							|| !"true".equals(returnstElt.getAttribute(XMLElements.ATT_RET_MASTER)) && !masterDoc) {
						returnstElt.setTextContent(returnstElt.getTextContent() + "\n" + returns);
					} else if (!returnstElt.hasAttribute(XMLElements.ATT_RET_MASTER) && masterDoc) {
						returnstElt.setTextContent(returns);
						returnstElt.setAttribute(XMLElements.ATT_RET_MASTER, "true");
					}
				} else {
					final org.w3c.dom.Element returnstElt = doc.createElement(XMLElements.RETURNS);
					returnstElt.setTextContent(returns);
					if (masterDoc) {
						returnstElt.setAttribute(XMLElements.ATT_RET_MASTER, "true");
					}
					docElt.appendChild(returnstElt);
				}
			}
			
	
			// Parse comment
			final String comment = docAnnot.comment();
			if (!"".equals(comment)) {
				if (docElt.getElementsByTagName(XMLElements.COMMENT).getLength() != 0) {
					docElt.getElementsByTagName(XMLElements.COMMENT).item(0).setTextContent(
							docElt.getElementsByTagName(XMLElements.COMMENT).item(0).getTextContent() + comment);
				} else {
					final org.w3c.dom.Element commentElt = doc.createElement(XMLElements.COMMENT);
					commentElt.setTextContent(comment);
					docElt.appendChild(commentElt);
				}
			}
	
			// Parse: seeAlso
			org.w3c.dom.Element seeAlsoElt;
			if (docElt.getElementsByTagName(XMLElements.SEEALSO).getLength() != 0) {
				seeAlsoElt = (org.w3c.dom.Element) docElt.getElementsByTagName(XMLElements.SEEALSO).item(0);
			} else {
				seeAlsoElt = doc.createElement(XMLElements.SEEALSO);
			}
			for (final String see : docAnnot.see()) {
				DocProcessor.addIfNotExistSee(doc, seeAlsoElt, see);
			}
			if (docAnnot.see().length != 0) {
				docElt.appendChild(seeAlsoElt);
			}
	
			// Parse: usages
	
			org.w3c.dom.Element usagesElt;
			org.w3c.dom.Element usagesExampleElt;
			org.w3c.dom.Element usagesNoExampleElt;
			if (docElt.getElementsByTagName(XMLElements.USAGES).getLength() != 0) {
				usagesElt = (org.w3c.dom.Element) docElt.getElementsByTagName(XMLElements.USAGES).item(0);
			} else {
				usagesElt = doc.createElement(XMLElements.USAGES);
			}
			if (docElt.getElementsByTagName(XMLElements.USAGES_EXAMPLES).getLength() != 0) {
				usagesExampleElt =
						(org.w3c.dom.Element) docElt.getElementsByTagName(XMLElements.USAGES_EXAMPLES).item(0);
			} else {
				usagesExampleElt = doc.createElement(XMLElements.USAGES_EXAMPLES);
			}
			if (docElt.getElementsByTagName(XMLElements.USAGES_NO_EXAMPLE).getLength() != 0) {
				usagesNoExampleElt =
						(org.w3c.dom.Element) docElt.getElementsByTagName(XMLElements.USAGES_NO_EXAMPLE).item(0);
			} else {
				usagesNoExampleElt = doc.createElement(XMLElements.USAGES_NO_EXAMPLE);
			}
			int numberOfUsages = 0;
			int numberOfUsagesWithExamplesOnly = 0;
			int numberOfUsagesWithoutExample = 0;
			for (final usage usage : docAnnot.usages()) {
				final org.w3c.dom.Element usageElt = doc.createElement(XMLElements.USAGE);
	
				// Among the usages, we consider the ones without value
				if ("".equals(usage.value())) {
					numberOfUsagesWithExamplesOnly++;
	
					final org.w3c.dom.Element examplesUsageElt =
							getExamplesElt(usage.examples(), doc, e, tc, parentElement);
					usageElt.appendChild(examplesUsageElt);
					usagesExampleElt.appendChild(usageElt);
				}
				// Among the usages, we consider the ones with only the value
				else if (usage.examples().length == 0) {
					numberOfUsagesWithoutExample++;
	
					usageElt.setAttribute(XMLElements.ATT_USAGE_DESC, usage.value());
					usagesNoExampleElt.appendChild(usageElt);
				}
				// Otherwise, when we have both value and examples
				else {
					numberOfUsages++;
	
					usageElt.setAttribute(XMLElements.ATT_USAGE_DESC, usage.value());
					final org.w3c.dom.Element examplesUsageElt =
							getExamplesElt(usage.examples(), doc, e, tc, parentElement);
					usageElt.appendChild(examplesUsageElt);
					usagesElt.appendChild(usageElt);
				}
			}
	
			// Let's continue with examples and special cases
			// - special cases are equivalent to usage without examples
			// - examples are equivalent to usage with only examples
			// Parse examples
			if (docAnnot.examples().length != 0) {
				final org.w3c.dom.Element usageExElt = doc.createElement(XMLElements.USAGE);
	
				final org.w3c.dom.Element examplesElt = getExamplesElt(docAnnot.examples(), doc, e, tc, parentElement);
	
				numberOfUsagesWithExamplesOnly += docAnnot.examples().length;
				usageExElt.appendChild(examplesElt);
				usagesExampleElt.appendChild(usageExElt);
			}
	
			// Parse specialCases
			for (final String cases : docAnnot.special_cases()) {
				if (!"".equals(cases)) {
					final org.w3c.dom.Element caseElt = doc.createElement(XMLElements.USAGE);
					caseElt.setAttribute(XMLElements.ATT_USAGE_DESC, cases);
					usagesNoExampleElt.appendChild(caseElt);
					numberOfUsagesWithoutExample++;
				}
			}
	
			if (numberOfUsagesWithExamplesOnly != 0) {
				docElt.appendChild(usagesExampleElt);
			}
			if (numberOfUsagesWithoutExample != 0) {
				docElt.appendChild(usagesNoExampleElt);
			}
			if (numberOfUsages != 0) {
				docElt.appendChild(usagesElt);
			}
	
		}
		return docElt;
	}

	public static void addIfNotExistSee(final Document doc, org.w3c.dom.Element seeAlsoElt, String opName) {
		final NodeList nLSee = seeAlsoElt.getElementsByTagName(XMLElements.SEE);
		int i = 0;
		boolean seeAlreadyInserted = false;
		while (i < nLSee.getLength() && !seeAlreadyInserted) {
			if (((org.w3c.dom.Element) nLSee.item(i)).getAttribute(XMLElements.ATT_SEE_ID).equals(opName)) {
				seeAlreadyInserted = true;
			}
			i++;
		}
		if (!seeAlreadyInserted) {
			final org.w3c.dom.Element seesElt = doc.createElement(XMLElements.SEE);
			seesElt.setAttribute(XMLElements.ATT_SEE_ID, opName);
			seeAlsoElt.appendChild(seesElt);
		}
	}
	
	
	/**
	 * Gets the doc elt.
	 *
	 * @param docAnnotTab the doc annot tab
	 * @param doc the doc
	 * @param mes the mes
	 * @param eltName the elt name
	 * @param tc the tc
	 * @param e the e
	 * @param parentElement the parent element
	 * @return the doc elt
	 */
	public org.w3c.dom.Element getDocElt(final doc[] docAnnotTab, final Document doc, final Messager mes,
			final String eltName, final TypeConverter tc, final ExecutableElement e,
			final org.w3c.dom.Element parentElement) { // e.getSimpleName()
		if (docAnnotTab == null
				|| docAnnotTab.length == 0) { return getDocElt(null, doc, null, mes, eltName, tc, e, parentElement); }
		return getDocElt(docAnnotTab[0], doc, null, mes, eltName, tc, e, parentElement);
	}

	/**
	 * Gets the examples elt.
	 *
	 * @param examples the examples
	 * @param doc the doc
	 * @param e the e
	 * @param tc the tc
	 * @param parentElement the parent element
	 * @return the examples elt
	 */
	public org.w3c.dom.Element getExamplesElt(final example[] examples, final Document doc, final ExecutableElement e,
			final TypeConverter tc, final org.w3c.dom.Element parentElement) {
		final org.w3c.dom.Element examplesElt = doc.createElement(XMLElements.EXAMPLES);
		for (final example example : examples) {
			examplesElt.appendChild(getExampleElt(example, doc, e, tc, parentElement));
		}
		return examplesElt;
	}

	/**
	 * Gets the example elt.
	 *
	 * @param example the example
	 * @param doc the doc
	 * @param e the e
	 * @param tc the tc
	 * @param parentElement the parent element
	 * @return the example elt
	 */
	public org.w3c.dom.Element getExampleElt(final example example, final Document doc, final ExecutableElement e,
			final TypeConverter tc, final org.w3c.dom.Element parentElement) {
		final org.w3c.dom.Element exampleElt = doc.createElement(XMLElements.EXAMPLE);
		exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_CODE, example.value());
		if (!"".equals(example.var())) {
			exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_VAR, example.var());
		}
		if (!"".equals(example.equals())) {
			exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_EQUALS, example.equals());
		}
		if (!"".equals(example.isNot())) {
			exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_IS_NOT, example.isNot());
		}
		if (!"".equals(example.raises())) {
			exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_RAISES, example.raises());
		}
		exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_IS_TEST_ONLY, "" + example.isTestOnly());
		exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_IS_EXECUTABLE, "" + example.isExecutable());
		if (!example.isExecutable()) {
			exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_IS_TESTABLE, "false");
		} else {
			exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_IS_TESTABLE, "" + example.test());
		}
		if ( (parentElement != null) && example.isExecutable() && example.test()) {
			parentElement.setAttribute("HAS_TESTS", "true");
		}
		if (!"".equals(example.returnType())) {
			exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_TYPE, example.returnType());
		} else {
			if (e != null) {
				exampleElt.setAttribute(XMLElements.ATT_EXAMPLE_TYPE, tc.getProperType(e.getReturnType().toString()));
			}
		}
		return exampleElt;
	}

	/**
	 * Gets the constant elt.
	 *
	 * @param constant the constant
	 * @param doc the doc
	 * @param e the e
	 * @param mes the mes
	 * @param tc the tc
	 * @return the constant elt
	 */
	public org.w3c.dom.Element getConstantElt(final constant constant, final Document doc, final Element e,
			final Messager mes, final TypeConverter tc) {
		final org.w3c.dom.Element constantElt = doc.createElement(XMLElements.CONSTANT);

		constantElt.setAttribute(XMLElements.ATT_CST_NAME, IConstantCategory.PREFIX_CONSTANT + constant.value());
		// constantElt.setAttribute(XMLElements.ATT_CST_VALUE,
		// ((VariableElement)e).getConstantValue().toString());
		final Object valCst = ((VariableElement) e).getConstantValue();
		final String valCstStr = valCst == null ? "No Default Value" : valCst.toString();
		constantElt.setAttribute(XMLElements.ATT_CST_VALUE, valCstStr);

		StringBuilder str = new StringBuilder();
		boolean first = true;
		for (final String n : constant.altNames()) {
			if (first) {
				first=false;
			}
			else {
				str.append(",");
			}
			str.append(IConstantCategory.PREFIX_CONSTANT);
			str.append(n); 
		}
		
		final String names = str.toString();
		if (!"".equals(names)) {
			constantElt.setAttribute(XMLElements.ATT_CST_NAMES, names);
		}

		constantElt.appendChild(getCategories(e, doc, doc.createElement(XMLElements.CATEGORIES), tc));

		final org.w3c.dom.Element docConstantElt =
				getDocElt(constant.doc(), doc, mes, e.getSimpleName().toString(), null, null, constantElt);
		if (docConstantElt != null) {
			constantElt.appendChild(docConstantElt);
		}

		return constantElt;
	}

	/**
	 * Gets the vars elt.
	 *
	 * @param varsAnnot the vars annot
	 * @param doc the doc
	 * @param mes the mes
	 * @param skillName the skill name
	 * @param tc the tc
	 * @return the vars elt
	 */
	public org.w3c.dom.Element getVarsElt(final vars varsAnnot, final Document doc, final Messager mes,
			final String skillName, final TypeConverter tc) {
		if (varsAnnot == null) {
			return null;
		}
		final org.w3c.dom.Element varsElt = doc.createElement(XMLElements.VARS);
		for (final variable v : varsAnnot.value()) {
			final org.w3c.dom.Element varElt = doc.createElement(XMLElements.VAR);
			varElt.setAttribute(XMLElements.ATT_VAR_NAME, v.name());
			varElt.setAttribute(XMLElements.ATT_VAR_TYPE, tc.getTypeString(Integer.valueOf(v.type())));
			varElt.setAttribute(XMLElements.ATT_VAR_CONSTANT, "" + v.constant());

			final org.w3c.dom.Element docEltVar =
					getDocElt(v.doc(), doc, mes, "Var " + v.name() + " from " + skillName, tc, null, varElt);
			if (docEltVar != null) {
				varElt.appendChild(docEltVar);
			}


			StringBuilder strBuilder = new StringBuilder();
			for (int i = 0 ; i < v.depends_on().length; i++ ) {
				final String dependElement = v.depends_on()[i];
				strBuilder.append(dependElement);
				if (i < v.depends_on().length - 1) {
					strBuilder.append(",");
				}
			}
			final String dependsOn = strBuilder.toString();				
			varElt.setAttribute(XMLElements.ATT_VAR_DEPENDS_ON, dependsOn);
			varsElt.appendChild(varElt);
		}
		return varsElt;
	}

	/**
	 * Gets the action elt.
	 *
	 * @param actionAnnot the action annot
	 * @param doc the doc
	 * @param mes the mes
	 * @param e the e
	 * @param tc the tc
	 * @return the action elt
	 */
	public org.w3c.dom.Element getActionElt(final action actionAnnot, final Document doc, final Messager mes,
			final Element e, final TypeConverter tc) {
		if (!(e instanceof ExecutableElement) || actionAnnot == null) { return null; }

		final ExecutableElement eltMethod = (ExecutableElement) e;
		final org.w3c.dom.Element actionElt = doc.createElement(XMLElements.ACTION);
		actionElt.setAttribute(XMLElements.ATT_ACTION_NAME, actionAnnot.name());
		actionElt.setAttribute(XMLElements.ATT_ACTION_RETURNTYPE,
				tc.getProperType(eltMethod.getReturnType().toString()));

		final org.w3c.dom.Element argsElt = doc.createElement(XMLElements.ARGS);
		for (final arg eltArg : actionAnnot.args()) {
			final org.w3c.dom.Element argElt = doc.createElement(XMLElements.ARG);
			argElt.setAttribute(XMLElements.ATT_ARG_NAME, eltArg.name());

			final String tabType = tc.getTypeString(eltArg.type());
			// for (int i = 0; i < eltArg.type().length; i++) {
			// tabType = tabType + (i < eltArg.type().length - 1 ? tc.getTypeString(eltArg.type()[i]) + ","
			// : tc.getTypeString(eltArg.type()[i]));
			// }
			argElt.setAttribute(XMLElements.ATT_ARG_TYPE, tabType);
			argElt.setAttribute(XMLElements.ATT_ARG_OPTIONAL, "" + eltArg.optional());
			final org.w3c.dom.Element docEltArg = getDocElt(eltArg.doc(), doc, mes,
					"Arg " + eltArg.name() + " from " + eltMethod.getSimpleName(), tc, null, argElt);
			if (docEltArg != null) {
				argElt.appendChild(docEltArg);
			}

			argsElt.appendChild(argElt);
		}
		actionElt.appendChild(argsElt);

		final org.w3c.dom.Element docEltAction =
				getDocElt(actionAnnot.doc(), doc, mes, eltMethod.getSimpleName().toString(), tc, null, actionElt);
		if (docEltAction != null) {
			actionElt.appendChild(docEltAction);
		}

		return actionElt;
	}

	/**
	 * Gets the facets elt.
	 *
	 * @param facetsAnnot the facets annot
	 * @param doc the doc
	 * @param mes the mes
	 * @param statName the stat name
	 * @param tc the tc
	 * @return the facets elt
	 */
	public org.w3c.dom.Element getFacetsElt(final facets facetsAnnot, final Document doc, final Messager mes,
			final String statName, final TypeConverter tc) {
		if (facetsAnnot == null) { return null; }

		final org.w3c.dom.Element facetsElt = doc.createElement(XMLElements.FACETS);

		for (final facet f : facetsAnnot.value()) {
			// if the facet is deprecated, it is not get in the docGAMA.mxl
			if( ! ((f.doc() != null) && (f.doc().length > 0) && (!"".equals(f.doc()[0].deprecated()))) ) {
				final org.w3c.dom.Element facetElt = doc.createElement(XMLElements.FACET);
				facetElt.setAttribute(XMLElements.ATT_FACET_NAME, f.name());
				facetElt.setAttribute(XMLElements.ATT_FACET_TYPE, tc.getTypeString(f.type()));
				facetElt.setAttribute(XMLElements.ATT_FACET_OPTIONAL, "" + f.optional());
				if (f.values().length != 0) {
					StringBuilder valuesTaken = new StringBuilder();
					valuesTaken.append(", takes values in: {");
					valuesTaken.append(f.values()[0]);
					for (int i = 1; i < f.values().length; i++) {
						valuesTaken.append(", ");
						valuesTaken.append(f.values()[i]);
					}
					valuesTaken.append("}");
					facetElt.setAttribute(XMLElements.ATT_FACET_VALUES, valuesTaken.toString());
				}
				facetElt.setAttribute(XMLElements.ATT_FACET_OMISSIBLE,
						f.name().equals(facetsAnnot.omissible()) ? "true" : "false");
				final org.w3c.dom.Element docFacetElt = getDocElt(f.doc(), doc, mes,
						"Facet " + f.name() + " from Statement" + statName, tc, null, facetElt);
				if (docFacetElt != null) {
					facetElt.appendChild(docFacetElt);
				}
	
				facetsElt.appendChild(facetElt);
			}
		}
		return facetsElt;
	}

	/**
	 * Gets the inside elt.
	 *
	 * @param insideAnnot the inside annot
	 * @param doc the doc
	 * @param tc the tc
	 * @return the inside elt
	 */
	public org.w3c.dom.Element getInsideElt(final inside insideAnnot, final Document doc, final TypeConverter tc) {
		if (insideAnnot == null) { return null; }

		final org.w3c.dom.Element insideElt = doc.createElement(XMLElements.INSIDE);

		final org.w3c.dom.Element symbolsElt = doc.createElement(XMLElements.SYMBOLS);
		for (final String sym : insideAnnot.symbols()) {
			final org.w3c.dom.Element symElt = doc.createElement(XMLElements.SYMBOL);
			symElt.setTextContent(sym);
			symbolsElt.appendChild(symElt);
		}
		insideElt.appendChild(symbolsElt);

		final org.w3c.dom.Element kindsElt = doc.createElement(XMLElements.KINDS);
		for (final int kind : insideAnnot.kinds()) {
			final org.w3c.dom.Element kindElt = doc.createElement(XMLElements.KIND);
			kindElt.setTextContent(tc.getSymbolKindStringFromISymbolKind(kind));
			kindsElt.appendChild(kindElt);
		}
		insideElt.appendChild(kindsElt);

		return insideElt;
	}

	/**
	 * Gets the operator element.
	 *
	 * @param operators the operators
	 * @param eltName the elt name
	 * @return the operator element
	 */
	public org.w3c.dom.Element getOperatorElement(final org.w3c.dom.Element operators, final String eltName) {
		final NodeList nL = operators.getElementsByTagName(XMLElements.OPERATOR);
		int i = 0;
		final boolean found = false;
		while (!found && i < nL.getLength()) {
			final org.w3c.dom.Element elt = (org.w3c.dom.Element) nL.item(i);
			if (eltName.equals(elt.getAttribute(XMLElements.ATT_OP_ID))) { return elt; }
			i++;
		}
		return null;
	}

	/**
	 * Gets the categories.
	 *
	 * @param e the e
	 * @param doc the doc
	 * @param categoriesElt the categories elt
	 * @param tc the tc
	 * @return the categories
	 */
	public org.w3c.dom.Element getCategories(final Element e, final Document doc,
			final org.w3c.dom.Element categoriesElt, final TypeConverter tc) {
		final ArrayList<String> categories = new ArrayList<>();
		String[] categoriesTab = null;
		final NodeList nL = categoriesElt.getElementsByTagName(XMLElements.CATEGORY);
		for (int i = 0; i < nL.getLength(); i++) {
			categories.add(((org.w3c.dom.Element) nL.item(i)).getAttribute(XMLElements.ATT_CAT_ID));
		}

		// To be able to deal with various annotations....
		if (e.getAnnotation(operator.class) != null) {
			categoriesTab = e.getAnnotation(operator.class).category();
		} else if (e.getAnnotation(constant.class) != null) {
			categoriesTab = e.getAnnotation(constant.class).category();
		}

		if (e.getAnnotation(operator.class) != null && e.getAnnotation(operator.class).category().length > 0
				|| e.getAnnotation(constant.class) != null && e.getAnnotation(constant.class).category().length > 0) {
			if (categoriesTab != null) {
				for (final String categoryName : categoriesTab) {
					if (!categories.contains(categoryName)) {
						categories.add(categoryName);

						final org.w3c.dom.Element catElt = doc.createElement(XMLElements.CATEGORY);
						catElt.setAttribute(XMLElements.ATT_CAT_ID, categoryName);
						categoriesElt.appendChild(catElt);
					}
				}
			}
		} else {
			if (!categories.contains(tc.getProperCategory(e.getEnclosingElement().getSimpleName().toString()))) {
				final org.w3c.dom.Element catElt = doc.createElement(XMLElements.CATEGORY);
				catElt.setAttribute(XMLElements.ATT_CAT_ID,
						tc.getProperCategory(e.getEnclosingElement().getSimpleName().toString()));
				categoriesElt.appendChild(catElt);
			}
		}

		// We add a particular category that is read from the iterator
		if (e.getAnnotation(operator.class) != null && e.getAnnotation(operator.class).iterator()) {
			final org.w3c.dom.Element catElt = doc.createElement(XMLElements.CATEGORY);
			catElt.setAttribute(XMLElements.ATT_CAT_ID, IOperatorCategory.ITERATOR);
			categoriesElt.appendChild(catElt);
		}

		return categoriesElt;
	}

	/**
	 * Gets the categories.
	 *
	 * @param e the e
	 * @param doc the doc
	 * @param tc the tc
	 * @return the categories
	 */
	public org.w3c.dom.Element getCategories(final Element e, final Document doc, final TypeConverter tc) {
		final org.w3c.dom.Element categoriesElt = doc.createElement(XMLElements.OPERATORS_CATEGORIES);

		return getCategories(e, doc, categoriesElt, tc);
	}

	/**
	 * Gets the concepts.
	 *
	 * @param e the e
	 * @param doc the doc
	 * @param conceptElt the concept elt
	 * @param tc the tc
	 * @return the concepts
	 */
	public org.w3c.dom.Element getConcepts(final Element e, final Document doc, final org.w3c.dom.Element conceptElt,
			final TypeConverter tc) {
		final ArrayList<String> concepts = new ArrayList<>();
		String[] conceptsTab = null;
		final NodeList nL = conceptElt.getElementsByTagName(XMLElements.CONCEPT);
		for (int i = 0; i < nL.getLength(); i++) {
			concepts.add(((org.w3c.dom.Element) nL.item(i)).getAttribute(XMLElements.ATT_CAT_ID));
		}

		// To be able to deal with various annotations....
		if (e.getAnnotation(operator.class) != null) {
			conceptsTab = e.getAnnotation(operator.class).concept();
		} else if (e.getAnnotation(constant.class) != null) {
			conceptsTab = e.getAnnotation(constant.class).concept();
		} else if (e.getAnnotation(type.class) != null) {
			conceptsTab = e.getAnnotation(type.class).concept();
		} else if (e.getAnnotation(species.class) != null) {
			conceptsTab = e.getAnnotation(species.class).concept();
		} else if (e.getAnnotation(symbol.class) != null) {
			conceptsTab = e.getAnnotation(symbol.class).concept();
		} else if (e.getAnnotation(skill.class) != null) {
			conceptsTab = e.getAnnotation(skill.class).concept();
		}

		if (e.getAnnotation(operator.class) != null && e.getAnnotation(operator.class).concept().length > 0
				|| e.getAnnotation(constant.class) != null && e.getAnnotation(constant.class).concept().length > 0
				|| e.getAnnotation(type.class) != null && e.getAnnotation(type.class).concept().length > 0
				|| e.getAnnotation(skill.class) != null && e.getAnnotation(skill.class).concept().length > 0
				|| e.getAnnotation(species.class) != null && e.getAnnotation(species.class).concept().length > 0
				|| e.getAnnotation(symbol.class) != null && e.getAnnotation(symbol.class).concept().length > 0) {
			if (conceptsTab != null) {
				for (final String conceptName : conceptsTab) {
					if (!concepts.contains(conceptName)) {
						concepts.add(conceptName);

						final org.w3c.dom.Element catElt = doc.createElement(XMLElements.CONCEPT);
						catElt.setAttribute(XMLElements.ATT_CAT_ID, conceptName);
						conceptElt.appendChild(catElt);
					}
				}
			}
		}

		// We had a particular category that is red from the iterator
		if (e.getAnnotation(operator.class) != null && e.getAnnotation(operator.class).iterator()) {
			final org.w3c.dom.Element catElt = doc.createElement(XMLElements.CONCEPT);
			catElt.setAttribute(XMLElements.ATT_CAT_ID, IOperatorCategory.ITERATOR);
			conceptElt.appendChild(catElt);
		}

		return conceptElt;
	}

}