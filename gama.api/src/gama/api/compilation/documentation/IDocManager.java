/*******************************************************************************************************
 *
 * IDocManager.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.documentation;

import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.utils.files.CompressionUtils;

/**
 * Manager interface for handling documentation of GAML model elements.
 * 
 * <p>
 * This interface defines the contract for managing documentation associations between EMF/XText parsed
 * objects (EObjects) and their corresponding GAML descriptions. It provides centralized documentation
 * storage, retrieval, and lifecycle management during model compilation and editing.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * IDocManager enables:
 * </p>
 * <ul>
 *   <li><strong>Documentation Generation:</strong> Creating documentation for model elements after validation</li>
 *   <li><strong>Documentation Storage:</strong> Maintaining associations between parsed objects and documentation</li>
 *   <li><strong>Documentation Retrieval:</strong> Providing documentation for UI tooltips and help views</li>
 *   <li><strong>Cache Invalidation:</strong> Managing documentation lifecycle when models change</li>
 * </ul>
 * 
 * <h2>Lifecycle</h2>
 * 
 * <ol>
 *   <li><strong>Parsing:</strong> Model is parsed into EMF EObjects</li>
 *   <li><strong>Compilation:</strong> EObjects are compiled into IDescriptions</li>
 *   <li><strong>Validation:</strong> Descriptions are validated for correctness</li>
 *   <li><strong>Documentation:</strong> {@link #doDocument} generates and stores documentation</li>
 *   <li><strong>Retrieval:</strong> UI components call {@link #getGamlDocumentation} for tooltips/help</li>
 *   <li><strong>Invalidation:</strong> {@link #invalidate} clears stale documentation when models change</li>
 * </ol>
 * 
 * <h2>Implementation</h2>
 * 
 * <p>
 * The actual implementation is typically provided by the XText framework and maintains a map from
 * EMF resource URIs to documentation nodes. A {@link NullImpl} is provided as a no-op implementation
 * for contexts where documentation is not needed (e.g., headless execution).
 * </p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IDocManager docManager = ...; // Typically injected by XText
 * 
 * // After model validation, generate documentation
 * Map<EObject, IGamlDescription> additionalExpressions = new HashMap<>();
 * docManager.doDocument(modelURI, modelDescription, additionalExpressions);
 * 
 * // Later, retrieve documentation for UI display
 * EObject element = ...; // From editor cursor position
 * IGamlDescription doc = docManager.getGamlDocumentation(element);
 * if (doc != null) {
 *     String tooltip = doc.getDocumentation().getContents();
 *     // Display tooltip in editor
 * }
 * 
 * // When model changes, invalidate cached documentation
 * docManager.invalidate(modelURI);
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see IGamlDocumentation
 * @see IGamlDescription
 * @see IModelDescription
 */
// Internal interface instantiated by XText
public interface IDocManager {

	/**
	 * Null-object implementation that performs no documentation operations.
	 * 
	 * <p>
	 * This singleton provides a no-op implementation for contexts where documentation
	 * is not needed or not available. All methods do nothing or return null.
	 * </p>
	 */
	IDocManager NULL = new NullImpl();

	/**
	 * Compressed documentation node for efficient storage.
	 * 
	 * <p>
	 * This record stores documentation in a compressed format to reduce memory usage.
	 * Documentation is compressed using {@link CompressionUtils#zip} and decompressed
	 * on retrieval. This is particularly useful for large models with extensive documentation.
	 * </p>
	 * 
	 * <h3>Usage:</h3>
	 * <pre>{@code
	 * // Create from a description
	 * IGamlDescription desc = ...;
	 * DocumentationNode node = new DocumentationNode(desc);
	 * 
	 * // Retrieve documentation
	 * String title = node.getTitle();
	 * IGamlDocumentation doc = node.getDocumentation();
	 * }</pre>
	 * 
	 * @param title the title/name of the documented element
	 * @param doc the compressed documentation bytes
	 */
	record DocumentationNode(String title, byte[] doc) implements IGamlDescription {

		/**
		 * Creates a compressed documentation node from a description.
		 * 
		 * <p>
		 * Extracts the title and compresses the documentation content for storage.
		 * </p>
		 *
		 * @param desc the description to create a node from
		 */
		public DocumentationNode(final IGamlDescription desc) {
			this(desc.getTitle(), CompressionUtils.zip(desc.getDocumentation().toString().getBytes()));
		}

		/**
		 * Returns the decompressed documentation.
		 * 
		 * <p>
		 * Decompresses the stored bytes and wraps them in a constant documentation object.
		 * </p>
		 *
		 * @return the decompressed documentation
		 */
		@Override
		public IGamlDocumentation getDocumentation() {
			return new GamlConstantDocumentation(new String(CompressionUtils.unzip(doc)));

		}

		/**
		 * Returns the title of the documented element.
		 *
		 * @return the title string
		 */
		@Override
		public String getTitle() { return title; }

	}

	/**
	 * Null-object implementation of IDocManager.
	 * 
	 * <p>
	 * All methods do nothing or return null. Used in headless mode or when
	 * documentation is disabled.
	 * </p>
	 */
	public static class NullImpl implements IDocManager {

		/**
		 * Does nothing - no documentation is generated.
		 *
		 * @param uri the resource URI (ignored)
		 * @param description the model description (ignored)
		 * @param additionalExpressions additional expressions to document (ignored)
		 */
		@Override
		public void doDocument(final URI uri, final IModelDescription description,
				final Map<EObject, IGamlDescription> additionalExpressions) {}

		/**
		 * Always returns null - no documentation is available.
		 *
		 * @param o the EMF object (ignored)
		 * @return always null
		 */
		@Override
		public IGamlDescription getGamlDocumentation(final EObject o) {
			return null;
		}

		/**
		 * Does nothing - no documentation to set.
		 *
		 * @param openResource the resource URI (ignored)
		 * @param object the EMF object (ignored)
		 * @param description the documentation (ignored)
		 */
		@Override
		public void setGamlDocumentation(final URI openResource, final EObject object,
				final IGamlDescription description) {}

		/**
		 * Does nothing - no cache to invalidate.
		 *
		 * @param key the resource URI (ignored)
		 */
		@Override
		public void invalidate(final URI key) {}

	}

	/**
	 * Generates and stores documentation for all elements in a model.
	 * 
	 * <p>
	 * This method should be called after model validation completes successfully. It:
	 * </p>
	 * <ol>
	 *   <li>Traverses the model description and all its children</li>
	 *   <li>Generates documentation for each statement and symbol</li>
	 *   <li>Processes additional expressions provided in the map</li>
	 *   <li>Stores documentation associations for later retrieval</li>
	 * </ol>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // After successful model compilation
	 * IModelDescription model = ...;
	 * Map<EObject, IGamlDescription> additionalExprs = new HashMap<>();
	 * 
	 * // Add any expression-level descriptions that need documentation
	 * additionalExprs.put(expressionEObject, expressionDescription);
	 * 
	 * // Generate and store all documentation
	 * docManager.doDocument(modelResource.getURI(), model, additionalExprs);
	 * }</pre>
	 *
	 * @param resource the URI of the model resource being documented
	 * @param description the root model description
	 * @param additionalExpressions map of expression EObjects to their descriptions for additional documentation
	 */
	void doDocument(URI resource, IModelDescription description, Map<EObject, IGamlDescription> additionalExpressions);

	/**
	 * Retrieves the GAML description associated with an EMF object.
	 * 
	 * <p>
	 * This method is called by UI components (editors, tooltips, etc.) to get documentation
	 * for the model element at a specific position in the source code.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // Get EObject at cursor position in editor
	 * EObject element = editorService.getElementAt(cursorOffset);
	 * 
	 * // Retrieve documentation
	 * IGamlDescription desc = docManager.getGamlDocumentation(element);
	 * if (desc != null) {
	 *     String tooltip = desc.getDocumentation().getContents();
	 *     // Show tooltip in editor
	 * }
	 * }</pre>
	 *
	 * @param o the EMF object to get documentation for
	 * @return the associated GAML description, or null if not documented
	 */
	IGamlDescription getGamlDocumentation(EObject o);

	/**
	 * Manually sets documentation for a specific EMF object.
	 * 
	 * <p>
	 * This method allows explicit documentation association, typically used for
	 * synthetic or dynamically created elements.
	 * </p>
	 *
	 * @param openResource the URI of the resource containing the object
	 * @param object the EMF object to document
	 * @param description the documentation to associate with the object
	 */
	void setGamlDocumentation(URI openResource, final EObject object, final IGamlDescription description);

	/**
	 * Invalidates cached documentation for a resource.
	 * 
	 * <p>
	 * This method should be called when a model file changes, ensuring that stale
	 * documentation is removed and will be regenerated on the next compilation.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // When model file is modified
	 * URI modelURI = modelResource.getURI();
	 * docManager.invalidate(modelURI);
	 * // Documentation will be regenerated on next compilation
	 * }</pre>
	 *
	 * @param key the URI of the resource whose documentation should be invalidated
	 */
	void invalidate(URI key);

}