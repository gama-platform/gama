/*******************************************************************************************************
 *
 * IValidationContext.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IModelDescription;

/**
 * Context interface for generating and managing GAML model documentation.
 * 
 * <p>This interface defines the contract for documentation generation during the model compilation
 * process. Implementations are responsible for extracting documentation information from model
 * descriptions and EMF objects, then generating appropriate documentation artifacts.</p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>The documentation context provides:</p>
 * <ul>
 *   <li><strong>Model Documentation:</strong> High-level documentation for entire models</li>
 *   <li><strong>Element Documentation:</strong> Documentation for individual model elements</li>
 *   <li><strong>Metadata Extraction:</strong> Extracting documentation from annotations and comments</li>
 *   <li><strong>Documentation Generation:</strong> Creating structured documentation artifacts</li>
 * </ul>
 * 
 * <h2>Documentation Sources</h2>
 * 
 * <p>Documentation can be extracted from:</p>
 * <ul>
 *   <li>JavaDoc-style comments in GAML source files</li>
 *   <li>Inline comments and annotations</li>
 *   <li>{@code @doc} annotations on model elements</li>
 *   <li>Facet descriptions and metadata</li>
 *   <li>Built-in documentation for operators, statements, and types</li>
 * </ul>
 * 
 * <h2>Implementation Responsibilities</h2>
 * 
 * <p>Implementations must:</p>
 * <ul>
 *   <li>Process model descriptions to extract documentation</li>
 *   <li>Associate documentation with EMF objects for IDE support</li>
 *   <li>Generate documentation in appropriate formats (HTML, Markdown, etc.)</li>
 *   <li>Handle documentation inheritance for species and behaviors</li>
 * </ul>
 * 
 * <h2>Usage in Compilation Pipeline</h2>
 * 
 * <p>The documentation context is typically invoked during the semantic validation phase,
 * after the model structure is established but before runtime initialization. This allows
 * documentation to reference fully-resolved types and symbols.</p>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>Implementations should be thread-safe if used in concurrent compilation scenarios.</p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see IModelDescription
 * @see IGamlDescription
 * @see IValidationContext
 */
public interface IDocumentationContext {

	/**
	 * Generates documentation for an entire model.
	 * 
	 * <p>This method processes a complete model description to extract and generate
	 * documentation for all model elements including species, actions, variables,
	 * experiments, and global definitions.</p>
	 * 
	 * <p>The generated documentation typically includes:</p>
	 * <ul>
	 *   <li>Model overview and description</li>
	 *   <li>List of defined species and their attributes</li>
	 *   <li>Available actions and their parameters</li>
	 *   <li>Global variables and their types</li>
	 *   <li>Experiment configurations</li>
	 *   <li>Dependencies and imports</li>
	 * </ul>
	 *
	 * @param description the model description to document (must not be null)
	 */
	void doDocument(IModelDescription description);

	/**
	 * Associates documentation with a specific model element.
	 * 
	 * <p>This method links documentation extracted from a GAML description to its
	 * corresponding EMF object. This association enables IDE features such as:</p>
	 * <ul>
	 *   <li>Hover tooltips showing documentation</li>
	 *   <li>Content assist with documentation hints</li>
	 *   <li>Help view integration</li>
	 *   <li>Documentation export and generation</li>
	 * </ul>
	 * 
	 * <p>The EMF object provides the syntactic representation and location information,
	 * while the GAML description provides the semantic documentation content.</p>
	 *
	 * @param e the EMF object representing the model element (may be null for synthetic elements)
	 * @param d the GAML description containing documentation metadata (must not be null)
	 */
	void document(EObject e, IGamlDescription d);

}