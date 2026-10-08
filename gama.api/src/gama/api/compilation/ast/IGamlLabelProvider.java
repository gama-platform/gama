/*******************************************************************************************************
 *
 * IGamlLabelProvider.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.compilation.ast;

/**
 * Provides textual and graphical labels for GAML syntactic elements in the UI.
 * 
 * <p>
 * This interface defines the contract for generating user-facing labels and icons for syntactic
 * elements in the GAMA platform. It is primarily used by UI components to display GAML model
 * structures in editors, outlines, and other visual representations.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * The label provider serves multiple purposes:
 * </p>
 * <ul>
 *   <li><strong>Text Labels:</strong> Generate human-readable text for syntactic elements</li>
 *   <li><strong>Icons:</strong> Provide appropriate icons/images for visual identification</li>
 *   <li><strong>UI Integration:</strong> Support outline views, content assist, and navigation</li>
 *   <li><strong>Consistency:</strong> Ensure uniform presentation across different UI components</li>
 * </ul>
 * 
 * <h2>Usage Context</h2>
 * 
 * <p>
 * This interface is typically used by:
 * </p>
 * <ul>
 *   <li>GAML editor outline views to display model structure</li>
 *   <li>Content assist (auto-completion) popups</li>
 *   <li>Navigation trees and model browsers</li>
 *   <li>Quick outline and search results</li>
 *   <li>Debugging views showing execution context</li>
 * </ul>
 * 
 * <h2>Implementation Notes</h2>
 * 
 * <p>
 * Implementations should:
 * </p>
 * <ul>
 *   <li>Provide concise, informative text labels</li>
 *   <li>Return appropriate icons based on element type and state</li>
 *   <li>Handle null or invalid elements gracefully</li>
 *   <li>Cache image descriptors for performance when possible</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IGamlLabelProvider labelProvider = ...;
 * ISyntacticElement element = ...;
 * 
 * // Get text label for outline view
 * String label = labelProvider.getText(element);
 * // e.g., "species my_agent"
 * 
 * // Get icon for the element
 * Object icon = labelProvider.getImageDescriptor(element);
 * // Returns an image descriptor for species icon
 * }</pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see ISyntacticElement
 * @see gama.api.compilation.ast.ISyntacticFactory
 */
public interface IGamlLabelProvider {

	/**
	 * Returns a human-readable text label for the given syntactic element.
	 * 
	 * <p>
	 * The text label typically includes the element's keyword and name, and may include
	 * additional information such as type, parameters, or facets depending on the element.
	 * </p>
	 * 
	 * <p><b>Examples:</b></p>
	 * <ul>
	 *   <li>Species: {@code "species my_agent"}</li>
	 *   <li>Action: {@code "action move(speed: float)"}</li>
	 *   <li>Variable: {@code "int my_var <- 0"}</li>
	 *   <li>Statement: {@code "if condition"}</li>
	 * </ul>
	 *
	 * @param element the syntactic element to generate a label for (may be null)
	 * @return a human-readable text label, or an empty string/default value if element is null
	 */
	String getText(ISyntacticElement element);

	/**
	 * Returns an image descriptor for the given syntactic element.
	 * 
	 * <p>
	 * The image descriptor is used to display an icon representing the element type
	 * in UI components. The exact type of the returned object depends on the UI framework
	 * (typically Eclipse's ImageDescriptor or similar).
	 * </p>
	 * 
	 * <p><b>Icon Types:</b></p>
	 * <ul>
	 *   <li>Species: Agent icon</li>
	 *   <li>Action: Method/behavior icon</li>
	 *   <li>Variable: Attribute icon</li>
	 *   <li>Experiment: Experiment icon</li>
	 *   <li>Statement: Statement-specific icon</li>
	 * </ul>
	 *
	 * @param element the syntactic element to get an icon for (may be null)
	 * @return an image descriptor object (typically Eclipse ImageDescriptor), 
	 *         or null if no icon is available or element is null
	 */
	Object getImageDescriptor(ISyntacticElement element);

}
