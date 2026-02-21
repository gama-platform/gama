/*******************************************************************************************************
 *
 * GamlConstantDocumentation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.documentation;

/**
 * Immutable documentation implementation that stores a constant string value.
 * 
 * <p>
 * This record provides a simple, immutable implementation of {@link IGamlDocumentation} that wraps
 * a constant string. Once created, the documentation content cannot be modified. This is suitable for
 * pre-formatted documentation that doesn't need to be built dynamically.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * GamlConstantDocumentation is used for:
 * </p>
 * <ul>
 *   <li><strong>Simple Documentation:</strong> Single-string documentation without structure</li>
 *   <li><strong>Immutability:</strong> Documentation that should not change after creation</li>
 *   <li><strong>Performance:</strong> Lightweight storage without builder overhead</li>
 *   <li><strong>Caching:</strong> Safe to cache and share across multiple references</li>
 * </ul>
 * 
 * <h2>Characteristics</h2>
 * 
 * <ul>
 *   <li><strong>Immutable:</strong> Content cannot be changed after construction</li>
 *   <li><strong>Thread-Safe:</strong> Safe to share across threads</li>
 *   <li><strong>Lightweight:</strong> Minimal memory overhead (just the string)</li>
 *   <li><strong>No Sub-Documentation:</strong> Does not support hierarchical documentation</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Create constant documentation
 * IGamlDocumentation doc = new GamlConstantDocumentation(
 *     "Returns the distance between two points in meters."
 * );
 * 
 * // Retrieve content
 * String content = doc.getContents();
 * // Returns: "Returns the distance between two points in meters."
 * 
 * // Mutations have no effect (returns same instance)
 * doc.append(" Additional text"); // Does nothing
 * String stillSame = doc.getContents();
 * // Still: "Returns the distance between two points in meters."
 * 
 * // Use in species/action descriptions
 * @Override
 * public IGamlDocumentation getDocumentation() {
 *     return new GamlConstantDocumentation("This is my action's documentation");
 * }
 * }</pre>
 * 
 * <h2>When to Use</h2>
 * 
 * <p>
 * Use GamlConstantDocumentation when:
 * </p>
 * <ul>
 *   <li>Documentation is static and known at creation time</li>
 *   <li>No dynamic building or modification is needed</li>
 *   <li>No sub-documentation or hierarchical structure is required</li>
 *   <li>Documentation is loaded from external sources (files, annotations)</li>
 * </ul>
 * 
 * <p>
 * Use {@link GamlRegularDocumentation} instead when:
 * </p>
 * <ul>
 *   <li>Documentation needs to be built dynamically</li>
 *   <li>Sub-documentation for parameters/facets is required</li>
 *   <li>Content needs to be appended or prepended</li>
 * </ul>
 * 
 * @param value the constant documentation string (never null, but may be empty)
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see IGamlDocumentation
 * @see GamlRegularDocumentation
 */
public record GamlConstantDocumentation(String value) implements IGamlDocumentation {

	/**
	 * Returns the constant documentation content.
	 * 
	 * <p>
	 * This method simply returns the immutable string value provided at construction time.
	 * </p>
	 *
	 * @return the constant documentation string
	 */
	@Override
	public String getContents() { return value; }

	/**
	 * Returns the string representation of this documentation.
	 * 
	 * <p>
	 * This is equivalent to {@link #getContents()}.
	 * </p>
	 *
	 * @return the documentation content as a string
	 */
	@Override
	public String toString() {
		return value;
	}

}