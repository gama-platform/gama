/*******************************************************************************************************
 *
 * ProductLaunchSpec.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.launch;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/** The launch-relevant parts of a workspace product; no PDE internals are used. */
public record ProductLaunchSpec(String product, String application, String vmArguments, String programArguments,
		List<String> features, List<Startup> startups, Map<String, String> properties) {

	public record Startup(String id, String level, boolean autoStart) {}

	public static ProductLaunchSpec read(final InputStream input, final String os)
			throws IOException, ParserConfigurationException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		Element root = factory.newDocumentBuilder().parse(input).getDocumentElement();
		if (!"product".equals(root.getTagName()) || root.getAttribute("application").isBlank()
				|| root.getAttribute("id").isBlank() || !"features".equals(root.getAttribute("type"))) {
			throw new IllegalArgumentException("Expected a feature-based GAMA product with an application and ID");
		}
		String suffix = switch (os) {
			case "macosx" -> "Mac";
			case "win32" -> "Win";
			case "linux" -> "Lin";
			default -> throw new IllegalArgumentException("Unsupported launch OS: " + os);
		};
		Element arguments = child(root, "launcherArgs");
		List<String> features = new ArrayList<>();
		Element featureList = child(root, "features");
		if (featureList != null) {
			for (Node n = featureList.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element e && e.getTagName().equals("feature")) { features.add(e.getAttribute("id")); }
			}
		}
		if (features.isEmpty()) { throw new IllegalArgumentException("The product contains no features"); }
		List<Startup> startups = new ArrayList<>();
		Map<String, String> properties = new LinkedHashMap<>();
		Element config = child(root, "configurations");
		if (config != null) {
			for (Node n = config.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element e && e.getTagName().equals("plugin")) {
					startups.add(new Startup(e.getAttribute("id"), e.getAttribute("startLevel"),
							Boolean.parseBoolean(e.getAttribute("autoStart"))));
				} else if (n instanceof Element e && e.getTagName().equals("property")) {
					properties.put(e.getAttribute("name"), e.getAttribute("value").replace("${build.id}", "development"));
				}
			}
		}
		return new ProductLaunchSpec(root.getAttribute("id"), root.getAttribute("application"),
				text(arguments, "vmArgs") + "\n" + text(arguments, "vmArgs" + suffix),
				text(arguments, "programArgs") + "\n" + text(arguments, "programArgs" + suffix),
				List.copyOf(features), List.copyOf(startups), Map.copyOf(properties));
	}

	private static Element child(final Element parent, final String name) {
		if (parent == null) { return null; }
		for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element e && e.getTagName().equals(name)) { return e; }
		}
		return null;
	}

	private static String text(final Element parent, final String name) {
		Element element = child(parent, name);
		return element == null ? "" : element.getTextContent().strip();
	}
}
