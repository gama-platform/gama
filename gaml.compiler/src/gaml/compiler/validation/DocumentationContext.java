/*******************************************************************************************************
 *
 * ValidationContext.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.validation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.documentation.IDocManager;
import gama.api.compilation.validation.IDocumentationContext;
import gama.dev.DEBUG;

/**
 * The ValidationContext class manages compilation errors, warnings, and information messages during GAML model
 * validation and compilation. It collects and categorizes errors from both the current resource and imported resources,
 * while also handling documentation generation.
 *
 * <p>
 * <strong>Key Design Improvements (2026):</strong>
 * </p>
 * <ul>
 * <li><strong>Separated Collections:</strong> Errors, warnings, and infos stored in separate lists for O(1) access
 * without filtering</li>
 * <li><strong>Memory Efficiency:</strong> No inheritance from ArrayList - uses composition for better
 * encapsulation</li>
 * <li><strong>Performance:</strong> Direct access to each category eliminates repeated Iterables.filter() calls</li>
 * <li><strong>Thread Safety:</strong> ConcurrentHashMap for documentation supports parallel validation</li>
 * </ul>
 *
 * <p>
 * This context distinguishes between:
 * <ul>
 * <li>Internal errors: errors in the current resource being validated</li>
 * <li>Imported errors: errors from resources imported by the current resource</li>
 * <li>Syntax errors: errors detected during parsing</li>
 * <li>Warnings and info messages: non-critical issues that can be filtered based on preferences</li>
 * </ul>
 * </p>
 *
 * @author GAMA Development Team
 * @since GAMA 1.0
 */
public class DocumentationContext implements IDocumentationContext {

	static {
		DEBUG.OFF();
	}

	/**
	 * A null object pattern instance for cases where no validation context is needed. This avoids null checks
	 * throughout the codebase.
	 */
	public static IDocumentationContext NULL = create(null, IDocManager.NULL);

	/**
	 * The URI of the resource being validated. Used to distinguish between errors in the current resource and errors in
	 * imported resources.
	 */
	final URI resourceURI;

	/**
	 * Delegate responsible for generating and managing documentation for GAML elements. This delegate is called to
	 * document the model and individual expressions when shouldDocument flag is set.
	 */
	private final IDocManager docDelegate;

	/**
	 * Map of EObjects to their corresponding GAML descriptions for documentation purposes. Uses a ConcurrentHashMap to
	 * support thread-safe concurrent access during parallel validation. Entries are accumulated during validation and
	 * processed when {@link #doDocument(IModelDescription)} is called.
	 *
	 * <p>
	 * Initialised eagerly in the constructor (rather than lazily on first use) to eliminate the race condition where
	 * two threads could both observe a {@code null} field and each create a new map, silently discarding one of them.
	 * </p>
	 */
	private final Map<EObject, IGamlDescription> expressionsToDocument = new ConcurrentHashMap<>();

	/**
	 * Creates the.
	 *
	 * @param uri
	 *            the uri
	 * @param syntax
	 *            the syntax
	 * @param delegate
	 *            the delegate
	 * @return the validation context
	 */
	public static IDocumentationContext create(final URI uri, final IDocManager delegate) {
		return new DocumentationContext(uri, delegate == null ? IDocManager.NULL : delegate);
	}

	/**
	 * Instantiates a new validation context for a specific resource.
	 *
	 * <p>
	 * This constructor initializes a validation context with the specified resource URI, syntax error status, and
	 * documentation delegate. The context will collect and manage compilation errors, warnings, and information
	 * messages for the resource during validation.
	 * </p>
	 *
	 * @param uri
	 *            the URI of the resource being validated. Used to distinguish between errors in the current resource
	 *            and imported resources. Can be null for test or null contexts.
	 * @param syntax
	 *            true if syntax errors have already been detected during parsing, false otherwise. When true, the
	 *            context will report that syntax errors exist via {@link #hasInternalSyntaxErrors()}
	 * @param delegate
	 *            the documentation manager delegate responsible for generating documentation. If null, a null object
	 *            pattern delegate ({@link IDocManager#NULL}) will be used instead.
	 */
	protected DocumentationContext(final URI uri, final IDocManager delegate) {
		this.resourceURI = uri;
		this.docDelegate = delegate;
	}

	/**
	 * Triggers documentation generation for the complete model.
	 *
	 * <p>
	 * Passes the accumulated {@link #expressionsToDocument} map to the delegate's
	 * {@link IDocManager#doDocument} method, which takes an immutable snapshot internally before scheduling
	 * the async documentation job. The local map is cleared afterwards so that stale entries do not accumulate
	 * across successive compilation passes.
	 * </p>
	 *
	 * <p>
	 * <strong>Note:</strong> do not call {@link IDocManager#setGamlDocumentation} on each entry here — the
	 * delegate's {@code doDocument} already processes the entire expressions map as part of the same async pass.
	 * Calling {@code setGamlDocumentation} again would result in each expression being documented twice, with the
	 * second call using a stale (already-cleared) snapshot.
	 * </p>
	 *
	 * @param description
	 *            the root model description to document
	 */
	@Override
	public void doDocument(final IModelDescription description) {
		// docDelegate.doDocument() takes an immutable snapshot of expressionsToDocument before scheduling
		// the async job, so it is safe to clear the map immediately after this call.
		docDelegate.doDocument(resourceURI, description, expressionsToDocument);
		// Do NOT iterate expressionsToDocument here and call setGamlDocumentation individually —
		// doing so would double-process each expression and could operate on a now-empty map if the
		// delegate cleared it first.
		expressionsToDocument.clear();
	}

	/**
	 * Records an EObject / description pair to be documented when {@link #doDocument(IModelDescription)} is called.
	 *
	 * <p>
	 * Called by {@code SymbolDescription} to register individual expressions during validation. Entries are
	 * accumulated in {@link #expressionsToDocument} and processed together with the model tree in
	 * {@link #doDocument(IModelDescription)}.
	 * </p>
	 *
	 * @param e
	 *            the EObject representing the expression; ignored if {@code null}
	 * @param d
	 *            the description providing documentation content; ignored if {@code null}
	 */
	@Override
	public void document(final EObject e, final IGamlDescription d) {
		// Called by SymbolDescription to document individual expressions -- they are kept in a Map<EObject,
		// IGamlDescription> and done when the whole model is documented
		if (e != null && d != null) { expressionsToDocument.put(e, d); }
	}

}
