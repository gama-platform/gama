/*******************************************************************************************************
 *
 * IGamlDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.function.Consumer;

import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.utils.GamlProperties;
import gama.api.utils.interfaces.INamed;

/**
 * Base interface for GAML objects that can be presented in documentation and UI contexts.
 * 
 * <p>
 * This interface defines the contract for objects that provide documentation metadata, titles,
 * and contextual actions for display in the GAMA platform. It serves as the foundation for
 * all GAML elements that need to be documented or presented to users.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * IGamlDescription enables:
 * </p>
 * <ul>
 *   <li><strong>Documentation Generation:</strong> Automatic extraction of element documentation</li>
 *   <li><strong>UI Presentation:</strong> Consistent display of GAML elements in editors and views</li>
 *   <li><strong>Plugin Tracking:</strong> Identification of which plugin defines an element</li>
 *   <li><strong>Contextual Actions:</strong> Support for element-specific UI actions</li>
 *   <li><strong>Metadata Collection:</strong> Gathering compilation and documentation metadata</li>
 * </ul>
 * 
 * <h2>Implementation Hierarchy</h2>
 * 
 * <p>
 * This interface is implemented by:
 * </p>
 * <ul>
 *   <li>{@link IDescription} - All semantic descriptions (species, actions, variables, etc.)</li>
 *   <li>Operator prototypes and signatures</li>
 *   <li>Type descriptions and definitions</li>
 *   <li>Documentation elements</li>
 * </ul>
 * 
 * <h2>Documentation System</h2>
 * 
 * <p>
 * The documentation returned by {@link #getDocumentation()} can include:
 * </p>
 * <ul>
 *   <li>Natural language descriptions of the element</li>
 *   <li>Usage examples and code samples</li>
 *   <li>Parameter and facet descriptions</li>
 *   <li>Return value documentation</li>
 *   <li>See also links and cross-references</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IGamlDescription element = ...;
 * 
 * // Get title for UI display
 * String title = element.getTitle(); // e.g., "species my_agent"
 * 
 * // Get documentation for help system
 * IGamlDocumentation doc = element.getDocumentation();
 * if (doc != IGamlDocumentation.EMPTY_DOC) {
 *     String docText = doc.get();
 *     // Display in help panel
 * }
 * 
 * // Check which plugin defines this element
 * String plugin = element.getDefiningPlugin();
 * if (plugin != null) {
 *     System.out.println("Defined in: " + plugin);
 * }
 * 
 * // Collect metadata
 * GamlProperties meta = new GamlProperties();
 * element.collectMetaInformation(meta);
 * }</pre>
 * 
 * @author drogoul
 * @since 27 avr. 2012
 * @version 2025-03
 * 
 * @see IDescription
 * @see IGamlDocumentation
 * @see gama.api.utils.GamlProperties
 */
public interface IGamlDescription extends INamed {

	/**
	 * Returns the title of this element for display in documentation and UI contexts.
	 * 
	 * <p>
	 * The title is typically the first line shown in documentation, tooltips, and outline views.
	 * It should be concise and descriptive, often including the element type and name.
	 * </p>
	 * 
	 * <p><b>Examples:</b></p>
	 * <ul>
	 *   <li>Species: "species my_agent"</li>
	 *   <li>Action: "action move(float speed)"</li>
	 *   <li>Operator: "+ (int, int) → int"</li>
	 * </ul>
	 * 
	 * <p><b>Default Implementation:</b> Returns the element's {@link #getName()}.</p>
	 *
	 * @return a string representing the title of this object (never null)
	 */
	default String getTitle() { return getName(); }

	/**
	 * Returns the documentation attached to this element.
	 * 
	 * <p>
	 * The documentation object contains detailed information about the element including:
	 * </p>
	 * <ul>
	 *   <li>Natural language description</li>
	 *   <li>Usage examples</li>
	 *   <li>Parameter and return value documentation</li>
	 *   <li>Related elements and cross-references</li>
	 *   <li>Deprecation notices</li>
	 * </ul>
	 * 
	 * <p>
	 * The documentation is used by:
	 * </p>
	 * <ul>
	 *   <li>Help views and hover tooltips</li>
	 *   <li>Online documentation generation</li>
	 *   <li>Content assist popups</li>
	 *   <li>API reference documentation</li>
	 * </ul>
	 * 
	 * <p><b>Default Implementation:</b> Returns {@link IGamlDocumentation#EMPTY_DOC}.</p>
	 *
	 * @return documentation object for this element (never null, but may be empty)
	 */
	default IGamlDocumentation getDocumentation() { return IGamlDocumentation.EMPTY_DOC; }

	/**
	 * Returns the identifier of the plugin that defines this element.
	 * 
	 * <p>
	 * This method identifies the source plugin/extension that provides this GAML element.
	 * It's used for:
	 * </p>
	 * <ul>
	 *   <li>Tracking element origins in the platform</li>
	 *   <li>Generating plugin-specific documentation</li>
	 *   <li>Managing dependencies between models and plugins</li>
	 *   <li>Filtering elements by plugin in UI views</li>
	 * </ul>
	 * 
	 * <p><b>Examples:</b></p>
	 * <ul>
	 *   <li>Built-in elements: "gama.core"</li>
	 *   <li>Extension elements: "gama.extension.physics"</li>
	 *   <li>Model-defined elements: null (not from a plugin)</li>
	 * </ul>
	 * 
	 * <p><b>Default Implementation:</b> Returns null (element not from a plugin).</p>
	 *
	 * @return the plugin identifier string, or null if not defined by a plugin
	 */
	default String getDefiningPlugin() { return null; }

	/**
	 * Collects metadata information about this element into the provided properties object.
	 * 
	 * <p>
	 * This method populates a {@link GamlProperties} object with metadata about the element.
	 * Metadata can include:
	 * </p>
	 * <ul>
	 *   <li>Defining plugin information</li>
	 *   <li>Element categories and classifications</li>
	 *   <li>Compilation-related properties</li>
	 *   <li>Custom metadata from extensions</li>
	 * </ul>
	 * 
	 * <p>
	 * The collected metadata is used for:
	 * </p>
	 * <ul>
	 *   <li>Documentation generation</li>
	 *   <li>Search and filtering in UI</li>
	 *   <li>Dependency analysis</li>
	 *   <li>Platform statistics and reporting</li>
	 * </ul>
	 * 
	 * <p><b>Default Implementation:</b> Adds the defining plugin to the metadata.</p>
	 *
	 * @param meta the properties object to populate with metadata (must not be null)
	 */
	default void collectMetaInformation(final GamlProperties meta) {
		meta.put(GamlProperties.PLUGINS, getDefiningPlugin());
	}

	/**
	 * Returns a contextual action that can be performed on this element.
	 * 
	 * <p>
	 * Contextual actions are UI actions that can be triggered when the user interacts with
	 * this element in the editor or other views. Examples include:
	 * </p>
	 * <ul>
	 *   <li>Navigate to definition</li>
	 *   <li>Show documentation</li>
	 *   <li>Refactor/rename</li>
	 *   <li>Quick fixes for errors</li>
	 * </ul>
	 * 
	 * <p>
	 * The action receives the IGamlDescription as context and can perform operations
	 * based on the element's properties.
	 * </p>
	 * 
	 * <p><b>Default Implementation:</b> Returns null (no contextual action available).</p>
	 *
	 * @return a consumer that performs the contextual action, or null if no action is available
	 */
	default Consumer<IGamlDescription> getContextualAction() { return null; }

}
