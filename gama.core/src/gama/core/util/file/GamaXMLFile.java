/*******************************************************************************************************
 *
 * GamaXMLFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GamaFile;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.geometry.IEnvelope;

/**
 * Class GamaXMLFile. Reads (and writes) XML files as a hierarchy of maps, waiting for a proper tree type in GAML.
 * Each element is represented by a map<string, unknown> with the keys {@link #NAME} (the tag name, a string),
 * {@link #ATTRIBUTES} (a map<string, string>), {@link #TEXT} (the trimmed text directly contained in the element, a
 * string) and {@link #CHILDREN} (the ordered list of the children elements, each represented the same way). The
 * contents of the file is the map representing the root element.
 *
 * @author drogoul
 * @since 9 janv. 2014
 *
 */
@file (
		name = IKeyword.XML,
		extensions = "xml",
		buffer_type = IType.MAP,
		buffer_index = IType.STRING,
		concept = { IConcept.FILE, IConcept.XML },
		doc = @doc ("Represents XML files. The contents is a map<string, unknown> representing the root element. Each element is represented by a map with 4 keys: '"
				+ GamaXMLFile.NAME + "' (the tag name), '" + GamaXMLFile.ATTRIBUTES
				+ "' (a map<string, string> of its attributes), '" + GamaXMLFile.TEXT
				+ "' (the trimmed text it directly contains) and '" + GamaXMLFile.CHILDREN
				+ "' (the ordered list of its children elements, represented in the same way). For instance, `xml_file(\"f.xml\").contents[\""
				+ GamaXMLFile.CHILDREN + "\"][0][\"" + GamaXMLFile.ATTRIBUTES
				+ "\"][\"id\"]` returns the 'id' attribute of the first child of the root element"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaXMLFile extends GamaFile<IMap<String, Object>, Object> {

	/** The key holding the tag name of an element */
	public static final String NAME = "name";

	/** The key holding the attributes of an element */
	public static final String ATTRIBUTES = "attributes";

	/** The key holding the text of an element */
	public static final String TEXT = "text";

	/** The key holding the children of an element */
	public static final String CHILDREN = "children";

	/**
	 * @param scope
	 * @param pathName
	 * @throws GamaRuntimeException
	 */
	@doc (
			value = "This file constructor allows to read a xml file",
			examples = { @example (
					value = "file f <-xml_file(\"file.xml\");",
					isExecutable = false) })
	public GamaXMLFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/**
	 * Instantiates a new gama XML file from a map representing its root element.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param container
	 *            the map representing the root element
	 */
	@doc (
			value = "This constructor allows to store a map representing the root element in a xml file (it does not save it). The file can then be saved later using the `save` statement",
			examples = { @example (
					value = "file f <- xml_file(\"file.xml\", map([\"name\"::\"root\", \"attributes\"::[\"version\"::\"1.0\"], \"children\"::[[\"name\"::\"child\", \"text\"::\"hello\"]]]));",
					isExecutable = false) })
	public GamaXMLFile(final IScope scope, final String pathName, final IMap<String, Object> container) {
		super(scope, pathName, container);
	}

	/**
	 * Gets the root tag.
	 *
	 * @param scope
	 *            the scope
	 * @return the root tag
	 */
	public String getRootTag(final IScope scope) {
		fillBuffer(scope);
		final IMap<String, Object> root = getBuffer();
		return root == null ? null : (String) root.get(NAME);
	}

	/**
	 * Returns the names of the attributes of the root element
	 */
	@Override
	public IList<String> getAttributes(final IScope scope) {
		fillBuffer(scope);
		final IList<String> result = GamaListFactory.create(Types.STRING);
		final IMap<String, Object> root = getBuffer();
		if (root != null && root.get(ATTRIBUTES) instanceof Map attributes) {
			for (final Object key : attributes.keySet()) { result.add(String.valueOf(key)); }
		}
		return result;
	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return null;
	}

	@Override
	protected String getHttpContentType() { return "application/xml"; }

	/**
	 * Creates a document builder protected against XXE attacks.
	 *
	 * @return the document builder
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 */
	private static DocumentBuilder createBuilder() throws ParserConfigurationException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		factory.setCoalescing(true);
		return factory.newDocumentBuilder();
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		try {
			final Document doc = createBuilder().parse(getFile(scope));
			setBuffer(toMap(doc.getDocumentElement()));
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	/**
	 * Converts a DOM element into its map representation.
	 *
	 * @param element
	 *            the element
	 * @return the map
	 */
	private static IMap<String, Object> toMap(final Element element) {
		final IMap<String, Object> result = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		final IMap<String, String> attributes = GamaMapFactory.create(Types.STRING, Types.STRING);
		final IList<IMap<String, Object>> children = GamaListFactory.create(Types.MAP);
		final StringBuilder text = new StringBuilder();
		final NamedNodeMap atts = element.getAttributes();
		for (int i = 0; i < atts.getLength(); i++) {
			final Node att = atts.item(i);
			attributes.put(att.getNodeName(), att.getNodeValue());
		}
		final NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);
			switch (node.getNodeType()) {
				case Node.ELEMENT_NODE -> children.add(toMap((Element) node));
				case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> text.append(node.getNodeValue());
				default -> {}
			}
		}
		result.put(NAME, element.getTagName());
		result.put(ATTRIBUTES, attributes);
		result.put(TEXT, text.toString().trim());
		result.put(CHILDREN, children);
		return result;
	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		final IMap<String, Object> root = getBuffer();
		if (root == null) return;
		final File file = getFile(scope);
		if (file.exists()) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning(file.getName() + " already exists", scope),
					false);
		}
		try {
			final Document doc = createBuilder().newDocument();
			doc.appendChild(toElement(scope, doc, root));
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(new DOMSource(doc), new StreamResult(file));
		} catch (final ParserConfigurationException | TransformerException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	/**
	 * Converts the map representation of an element into a DOM element.
	 *
	 * @param scope
	 *            the scope
	 * @param doc
	 *            the document
	 * @param map
	 *            the map
	 * @return the element
	 */
	private static Element toElement(final IScope scope, final Document doc, final Map map) {
		final Object name = map.get(NAME);
		if (name == null) throw GamaRuntimeException
				.error("Each element of an xml_file must define its tag name using the '" + NAME + "' key", scope);
		final Element element = doc.createElement(name.toString());
		if (map.get(ATTRIBUTES) instanceof Map attributes) {
			attributes.forEach((k, v) -> element.setAttribute(String.valueOf(k), v == null ? "" : String.valueOf(v)));
		}
		final Object text = map.get(TEXT);
		if (text != null && !text.toString().isEmpty()) { element.appendChild(doc.createTextNode(text.toString())); }
		if (map.get(CHILDREN) instanceof Iterable children) {
			for (final Object child : children) {
				if (!(child instanceof Map c)) throw GamaRuntimeException.error(
						"The children of an element in an xml_file must be maps, not " + child, scope);
				element.appendChild(toElement(scope, doc, c));
			}
		}
		return element;
	}

	@Override
	public Modifiable ensureContentsIsCompatible(final Modifiable contents) {
		if (contents instanceof IMap) return contents;
		final IMap map = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		map.put(NAME, "root");
		map.put(CHILDREN, contents);
		return map;
	}

}
