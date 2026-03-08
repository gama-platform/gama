/*******************************************************************************************************
 *
 * GamlRegularDocumentation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.documentation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mutable documentation implementation built around a StringBuilder with support for hierarchical sub-documentation.
 * 
 * <p>
 * This class provides a flexible, mutable implementation of {@link IGamlDocumentation} that allows
 * dynamic building of documentation content through appending and prepending operations. It also
 * supports organizing sub-documentation into categories (headers) with named entries.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * GamlRegularDocumentation is used for:
 * </p>
 * <ul>
 *   <li><strong>Dynamic Building:</strong> Constructing documentation incrementally</li>
 *   <li><strong>Hierarchical Structure:</strong> Organizing sub-documentation for parameters, facets, etc.</li>
 *   <li><strong>Structured Output:</strong> Generating formatted HTML documentation with sections</li>
 *   <li><strong>Flexibility:</strong> Supporting both simple and complex documentation needs</li>
 * </ul>
 * 
 * <h2>Features</h2>
 * 
 * <ul>
 *   <li><strong>Mutable Content:</strong> Can append/prepend text after creation</li>
 *   <li><strong>Sub-Documentation:</strong> Supports nested documentation organized by headers</li>
 *   <li><strong>HTML Formatting:</strong> Automatically formats sub-documentation as HTML lists</li>
 *   <li><strong>Efficient Building:</strong> Uses StringBuilder for performance</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Simple Documentation Building:</h3>
 * <pre>{@code
 * // Create and build documentation
 * GamlRegularDocumentation doc = new GamlRegularDocumentation();
 * doc.append("Moves the agent to the specified location.");
 * doc.append(" The movement speed depends on the agent's speed attribute.");
 * 
 * String content = doc.getContents();
 * // Returns: "Moves the agent to the specified location. The movement..."
 * }</pre>
 * 
 * <h3>Structured Documentation with Sub-Elements:</h3>
 * <pre>{@code
 * // Create action documentation with parameter documentation
 * GamlRegularDocumentation actionDoc = new GamlRegularDocumentation(
 *     "Moves the agent towards a target location."
 * );
 * 
 * // Add parameter documentation under "Parameters" header
 * actionDoc.set("Parameters", "speed", 
 *     new GamlConstantDocumentation("Movement speed in meters per second"));
 * actionDoc.set("Parameters", "target", 
 *     new GamlConstantDocumentation("Target location as a point"));
 * 
 * // Add return value documentation
 * actionDoc.set("Returns", "success", 
 *     new GamlConstantDocumentation("True if movement was successful"));
 * 
 * // Get formatted HTML output
 * String html = actionDoc.getContents();
 * // Returns HTML with main text followed by formatted parameter and return sections
 * }</pre>
 * 
 * <h3>Retrieving Sub-Documentation:</h3>
 * <pre>{@code
 * GamlRegularDocumentation doc = ...;
 * 
 * // Get specific parameter documentation
 * IGamlDocumentation speedDoc = doc.get("speed");
 * String speedDescription = speedDoc.getContents();
 * }</pre>
 * 
 * <h2>HTML Output Format</h2>
 * 
 * <p>
 * When sub-documentation exists, {@link #getContents()} produces formatted HTML:
 * </p>
 * <pre>{@code
 * Main documentation text
 * <hr/>Parameters<br/><ul>
 * <li><b>speed</b>: Movement speed in meters per second
 * <li><b>target</b>: Target location as a point
 * </ul><br/>
 * <hr/>Returns<br/><ul>
 * <li><b>success</b>: True if movement was successful
 * </ul><br/>
 * }</pre>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>
 * This class is NOT thread-safe. Instances should not be shared across threads without
 * external synchronization. For thread-safe, immutable documentation, use
 * {@link GamlConstantDocumentation}.
 * </p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see IGamlDocumentation
 * @see GamlConstantDocumentation
 */
public class GamlRegularDocumentation implements IGamlDocumentation {

	/** The StringBuilder for building the main documentation content. */
	final StringBuilder builder;

	/** Map of headers to their sub-documentation entries (organized as header -> key -> doc). */
	final Map<String, Map<String, IGamlDocumentation>> subdocs = new LinkedHashMap<>();

	/**
	 * Creates documentation with initial content.
	 * 
	 * <p>
	 * The provided character sequence becomes the initial content of the documentation.
	 * Additional content can be appended or prepended afterwards.
	 * </p>
	 *
	 * @param sb the initial documentation content
	 */
	public GamlRegularDocumentation(final CharSequence sb) {
		builder = new StringBuilder(sb);
	}

	/**
	 * Creates empty documentation.
	 * 
	 * <p>
	 * The documentation starts with no content. Use {@link #append(String)} or
	 * {@link #prepend(String)} to add content.
	 * </p>
	 */
	public GamlRegularDocumentation() {
		this("");
	}

	/**
	 * Appends a string to the documentation content.
	 * 
	 * <p>
	 * The string is added to the end of the current main documentation text.
	 * This does not affect sub-documentation.
	 * </p>
	 *
	 * @param string the string to append
	 * @return this documentation instance for method chaining
	 */
	@Override
	public IGamlDocumentation append(final String string) {
		builder.append(string);
		return this;
	}

	/**
	 * Appends a single character to the documentation content.
	 * 
	 * <p>
	 * Convenience method for appending single characters.
	 * </p>
	 *
	 * @param character the character to append
	 * @return this documentation instance for method chaining
	 */
	@Override
	public IGamlDocumentation append(final Character character) {
		builder.append(character);
		return this;
	}

	/**
	 * Prepends a string to the documentation content.
	 * 
	 * <p>
	 * The string is inserted at the beginning of the current main documentation text.
	 * </p>
	 *
	 * @param string the string to prepend
	 * @return this documentation instance for method chaining
	 */
	@Override
	public IGamlDocumentation prepend(final String string) {
		builder.insert(0, string);
		return this;
	}

	/**
	 * Returns the complete documentation content including all sub-documentation.
	 * 
	 * <p>
	 * If no sub-documentation exists, returns the main content as-is. If sub-documentation
	 * is present, generates formatted HTML with the main content followed by sections for
	 * each header containing lists of sub-documentation entries.
	 * </p>
	 * 
	 * <h3>Format with Sub-Documentation:</h3>
	 * <pre>
	 * [Main content]
	 * &lt;hr/&gt;[Header1]&lt;br/&gt;&lt;ul&gt;
	 * &lt;li&gt;&lt;b&gt;key1&lt;/b&gt;: doc1
	 * &lt;li&gt;&lt;b&gt;key2&lt;/b&gt;: doc2
	 * &lt;/ul&gt;&lt;br/&gt;
	 * &lt;hr/&gt;[Header2]&lt;br/&gt;&lt;ul&gt;
	 * ...
	 * </pre>
	 *
	 * @return the complete formatted documentation string
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

	/**
	 * Returns the string representation of this documentation.
	 * 
	 * <p>
	 * Equivalent to {@link #getContents()}.
	 * </p>
	 *
	 * @return the complete formatted documentation
	 */
	@Override
	public String toString() {
		return getContents();
	}

	/**
	 * Adds or updates sub-documentation under a specific header and key.
	 * 
	 * <p>
	 * This method organizes sub-documentation into hierarchical sections. Each header
	 * represents a category (e.g., "Parameters", "Returns"), and within each header,
	 * individual entries are identified by keys (e.g., parameter names).
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * doc.set("Parameters", "speed", speedDoc);
	 * doc.set("Parameters", "target", targetDoc);
	 * doc.set("Returns", "result", resultDoc);
	 * }</pre>
	 *
	 * @param header the category/section header under which to organize this entry
	 * @param key the specific identifier for this sub-documentation entry
	 * @param doc the documentation to associate with this header and key
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

	/**
	 * Retrieves sub-documentation by key.
	 * 
	 * <p>
	 * Searches all headers for an entry with the specified key and returns the first match.
	 * The search order follows the insertion order of headers.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * IGamlDocumentation speedDoc = actionDoc.get("speed");
	 * if (speedDoc != IGamlDocumentation.EMPTY_DOC) {
	 *     // Found documentation for 'speed' parameter
	 * }
	 * }</pre>
	 *
	 * @param key the key identifying the sub-documentation to retrieve
	 * @return the sub-documentation if found, or {@link IGamlDocumentation#EMPTY_DOC} if not found
	 */
	@Override
	public IGamlDocumentation get(final String key) {
		for (String s : subdocs.keySet()) {
			IGamlDocumentation doc = subdocs.get(s).get(key);
			if (doc != null) return doc;
		}
		return IGamlDocumentation.EMPTY_DOC;
	}

}