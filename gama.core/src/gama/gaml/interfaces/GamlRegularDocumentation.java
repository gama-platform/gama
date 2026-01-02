/*******************************************************************************************************
 *
 * GamlRegularDocumentation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.interfaces;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A documentation built around a StringBuilder, allowing to append and prepend elements
 */
public class GamlRegularDocumentation implements IGamlDocumentation {

	/** The builder. */
	final StringBuilder builder;

	/** The subdocs. */
	final Map<String, Map<String, IGamlDocumentation>> subdocs = new LinkedHashMap<>();

	/**
	 * Instantiates a new regular doc.
	 *
	 * @param sb
	 *            the sb
	 */
	public GamlRegularDocumentation(final CharSequence sb) {
		builder = new StringBuilder(sb);
	}

	/**
	 * Instantiates a new regular doc.
	 */
	public GamlRegularDocumentation() {
		this("");
	}

	@Override
	public IGamlDocumentation append(final String string) {
		builder.append(string);
		return this;
	}

	/**
	 * Append.
	 *
	 * @param string
	 *            the string
	 * @return the doc
	 */
	@Override
	public IGamlDocumentation append(final Character string) {
		builder.append(string);
		return this;
	}

	@Override
	public IGamlDocumentation prepend(final String string) {
		builder.insert(0, string);
		return this;
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @date 3 janv. 2024
	 */
	@Override
	public String getContents() {
		if (subdocs.isEmpty()) return builder.toString();
		StringBuilder sb = new StringBuilder(builder.toString());
		for (String header : subdocs.keySet()) {
			sb.append("<hr/>").append(header).append("<br/><ul>");
			subdocs.get(header).forEach((name, doc) -> {
				sb.append("<li><b>").append(name).append("</b>: ").append(doc.toString());
			});
			sb.append("</ul><br/>");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return getContents();
	}

	/**
	 * Sets a sub-documentation
	 *
	 * @param key
	 *            the key
	 * @param doc
	 *            the doc
	 */
	@Override
	public void set(final String header, final String key, final IGamlDocumentation doc) {
		Map<String, IGamlDocumentation> category = subdocs.get(header);
		if (category == null) {
			category = new LinkedHashMap<>();
			subdocs.put(header, category);
		}
		category.put(key, doc);
	}

	@Override
	public IGamlDocumentation get(final String key) {
		for (String s : subdocs.keySet()) {
			IGamlDocumentation doc = subdocs.get(s).get(key);
			if (doc != null) return doc;
		}
		return IGamlDocumentation.EMPTY_DOC;
	}

}