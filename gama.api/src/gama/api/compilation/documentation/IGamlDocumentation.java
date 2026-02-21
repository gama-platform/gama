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
 * Interface for accessing and building documentation of GAML elements.
 * 
 * <p>
 * This interface provides a flexible contract for retrieving and manipulating documentation content
 * for GAML language elements such as species, actions, operators, variables, and other model components.
 * It supports both simple string-based documentation and structured documentation with sub-elements.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * IGamlDocumentation enables:
 * </p>
 * <ul>
 *   <li><strong>Documentation Retrieval:</strong> Getting documentation content as formatted strings</li>
 *   <li><strong>Hierarchical Documentation:</strong> Organizing documentation with sub-elements (e.g., action parameters)</li>
 *   <li><strong>Dynamic Building:</strong> Appending and prepending content to create composite documentation</li>
 *   <li><strong>Key-Based Access:</strong> Retrieving specific sub-documentation by key</li>
 *   <li><strong>Empty Documentation:</strong> Providing a null-object pattern for missing documentation</li>
 * </ul>
 * 
 * <h2>Implementation Types</h2>
 * 
 * <h3>Constant Documentation:</h3>
 * <p>
 * Immutable documentation that cannot be modified after creation ({@link GamlConstantDocumentation}).
 * Used for simple, pre-formatted documentation strings.
 * </p>
 * 
 * <h3>Regular Documentation:</h3>
 * <p>
 * Mutable documentation built with a StringBuilder ({@link GamlRegularDocumentation}).
 * Supports appending, prepending, and organizing sub-documentation.
 * </p>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Simple Documentation:</h3>
 * <pre>{@code
 * IGamlDocumentation doc = new GamlConstantDocumentation("Returns the distance between two points");
 * String content = doc.getContents(); // "Returns the distance between two points"
 * }</pre>
 * 
 * <h3>Building Documentation:</h3>
 * <pre>{@code
 * IGamlDocumentation doc = new GamlRegularDocumentation();
 * doc.append("Moves the agent towards the target location.");
 * doc.append(" Speed determines how fast the movement is.");
 * // Result: "Moves the agent towards the target location. Speed determines how fast..."
 * }</pre>
 * 
 * <h3>Structured Documentation with Sub-Elements:</h3>
 * <pre>{@code
 * IGamlDocumentation doc = new GamlRegularDocumentation("Main action description");
 * 
 * // Add parameter documentation
 * IGamlDocumentation speedDoc = new GamlConstantDocumentation("Movement speed in m/s");
 * doc.set("Parameters", "speed", speedDoc);
 * 
 * IGamlDocumentation targetDoc = new GamlConstantDocumentation("Target location");
 * doc.set("Parameters", "target", targetDoc);
 * 
 * // Retrieve sub-documentation
 * IGamlDocumentation paramDoc = doc.get("speed");
 * }</pre>
 * 
 * <h3>Empty Documentation:</h3>
 * <pre>{@code
 * IGamlDocumentation doc = IGamlDocumentation.EMPTY_DOC;
 * String content = doc.getContents(); // Returns ""
 * IGamlDocumentation sub = doc.get("anything"); // Returns EMPTY_DOC
 * }</pre>
 * 
 * <h2>HTML Formatting</h2>
 * 
 * <p>
 * Documentation content typically contains HTML formatting for display in UI components:
 * </p>
 * <ul>
 *   <li>Paragraphs: {@code <br/>}, {@code <p>}</li>
 *   <li>Emphasis: {@code <b>}, {@code <i>}</li>
 *   <li>Lists: {@code <ul>}, {@code <li>}</li>
 *   <li>Code: {@code <code>}</li>
 *   <li>Sections: {@code <hr/>}</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see GamlConstantDocumentation
 * @see GamlRegularDocumentation
 * @see gama.api.compilation.descriptions.IGamlDescription
 */
@FunctionalInterface
public interface IGamlDocumentation {

	/**
	 * Singleton instance representing empty documentation.
	 * 
	 * <p>
	 * This is a null-object pattern implementation that returns empty strings for all operations.
	 * Use this instead of null to avoid null checks when documentation is not available.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * IGamlDocumentation doc = element.getDocumentation();
	 * if (doc == IGamlDocumentation.EMPTY_DOC) {
	 *     // No documentation available
	 * }
	 * }</pre>
	 */
	IGamlDocumentation EMPTY_DOC = () -> "";

	/**
	 * Returns the complete documentation content as a string.
	 * 
	 * <p>
	 * This is the primary method for retrieving documentation text. For simple documentation,
	 * this returns the raw content. For structured documentation with sub-elements, this may
	 * return a formatted string combining the main content and all sub-documentation.
	 * </p>
	 * 
	 * <p>
	 * The returned string typically contains HTML formatting for display in tooltips, help
	 * views, and documentation browsers.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * IGamlDocumentation doc = action.getDocumentation();
	 * String html = doc.getContents();
	 * // Display in a browser or tooltip
	 * }</pre>
	 *
	 * @return the documentation content as a string (never null, but may be empty)
	 */
	String getContents();

	/**
	 * Returns the sub-documentation associated with the given key.
	 * 
	 * <p>
	 * This method enables hierarchical documentation structures where main elements have
	 * associated sub-documentation for their components (e.g., action parameters, facets).
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // For an action with documented parameters
	 * IGamlDocumentation actionDoc = action.getDocumentation();
	 * IGamlDocumentation speedDoc = actionDoc.get("speed");
	 * String speedDescription = speedDoc.getContents();
	 * }</pre>
	 * 
	 * <p><b>Default Implementation:</b> Returns {@link #EMPTY_DOC}.</p>
	 *
	 * @param key the key identifying the sub-documentation to retrieve
	 * @return the sub-documentation for the key, or {@link #EMPTY_DOC} if not found
	 */
	default IGamlDocumentation get(final String key) {
		return IGamlDocumentation.EMPTY_DOC;
	}

	/**
	 * Appends a string to the current documentation content.
	 * 
	 * <p>
	 * For mutable documentation implementations (like {@link GamlRegularDocumentation}),
	 * this adds content to the end of the existing documentation. For immutable
	 * implementations, this may return the same instance unchanged.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * IGamlDocumentation doc = new GamlRegularDocumentation("Initial text");
	 * doc.append(" Additional text");
	 * doc.append(" More text");
	 * // Result: "Initial text Additional text More text"
	 * }</pre>
	 * 
	 * <p><b>Default Implementation:</b> Returns {@code this} unchanged.</p>
	 *
	 * @param string the string to append to the documentation
	 * @return this documentation instance for method chaining
	 */
	default IGamlDocumentation append(final String string) {
		return this;
	}

	/**
	 * Appends a single character to the current documentation content.
	 * 
	 * <p>
	 * This is a convenience method for appending single characters without string conversion.
	 * Useful for building formatted output character by character.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * IGamlDocumentation doc = new GamlRegularDocumentation("Test");
	 * doc.append(':').append(' ').append('X');
	 * // Result: "Test: X"
	 * }</pre>
	 * 
	 * <p><b>Default Implementation:</b> Returns {@code this} unchanged.</p>
	 *
	 * @param character the character to append to the documentation
	 * @return this documentation instance for method chaining
	 */
	default IGamlDocumentation append(final Character character) {
		return this;
	}

	/**
	 * Prepends a string to the current documentation content.
	 * 
	 * <p>
	 * For mutable documentation implementations, this inserts content at the beginning
	 * of the existing documentation. This is useful for adding introductory text or
	 * headers to existing documentation.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * IGamlDocumentation doc = new GamlRegularDocumentation("original text");
	 * doc.prepend("<b>Note:</b> ");
	 * // Result: "<b>Note:</b> original text"
	 * }</pre>
	 * 
	 * <p><b>Default Implementation:</b> Returns {@code this} unchanged.</p>
	 *
	 * @param string the string to prepend to the documentation
	 * @return this documentation instance for method chaining
	 */
	default IGamlDocumentation prepend(final String string) {
		return this;
	}

	/**
	 * Adds or updates sub-documentation at the specified header and key.
	 * 
	 * <p>
	 * This method enables building structured documentation with multiple sections (headers)
	 * and named sub-elements within each section. For example, documenting action parameters
	 * under a "Parameters" header.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * IGamlDocumentation doc = new GamlRegularDocumentation("Action description");
	 * 
	 * // Add parameters section
	 * doc.set("Parameters", "speed", new GamlConstantDocumentation("Movement speed"));
	 * doc.set("Parameters", "target", new GamlConstantDocumentation("Target location"));
	 * 
	 * // Add return value section
	 * doc.set("Returns", "result", new GamlConstantDocumentation("Success status"));
	 * }</pre>
	 * 
	 * <p><b>Default Implementation:</b> Does nothing.</p>
	 *
	 * @param header the section header under which to organize the sub-documentation
	 * @param key the key identifying this specific sub-documentation entry
	 * @param doc the documentation to associate with the header and key
	 */
	default void set(final String header, final String key, final IGamlDocumentation doc) {}

}