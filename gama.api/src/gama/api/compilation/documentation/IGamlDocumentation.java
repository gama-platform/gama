/*******************************************************************************************************
 *
 * IGamlDocumentation.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.documentation;

/**
 * The Interface IGamlDocumentation. A simple interface that allows retrieving the documentation of a description,
 * either directly or using a key. Used for actions and their arguments
 */
@FunctionalInterface
public interface IGamlDocumentation {

	/** The empty doc. */
	IGamlDocumentation EMPTY_DOC = () -> "";

	/**
	 * Gets the string value of the documentation. Never null.
	 *
	 * @return the string
	 */
	String getContents();

	/**
	 * Gets the string value of the documentation of the sub-element corresponding to the key.
	 *
	 * @param key
	 *            the key
	 * @return the string
	 */
	default IGamlDocumentation get(final String key) {
		return IGamlDocumentation.EMPTY_DOC;
	}

	/**
	 * Append a string to the current string value of the documentation.
	 *
	 * @param string
	 *            the string
	 */
	default IGamlDocumentation append(final String string) {
		return this;
	}

	/**
	 * Append.
	 *
	 * @param string
	 *            the string
	 * @return the doc
	 */
	default IGamlDocumentation append(final Character string) {
		return this;
	}

	/**
	 * Prepend a string to the current string value of the documentation.
	 *
	 * @param string
	 *            the string
	 * @return the doc
	 */
	default IGamlDocumentation prepend(final String string) {
		return this;
	}

	/**
	 * Adds a subdocumentation at the specific key.
	 *
	 * @param key
	 *            the key
	 * @param doc
	 *            the doc
	 */
	default void set(final String header, final String key, final IGamlDocumentation doc) {}

}