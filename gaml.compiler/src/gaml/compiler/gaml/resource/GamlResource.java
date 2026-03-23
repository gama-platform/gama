/*******************************************************************************************************
 *
 * GamlResource.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.resource;

import static gama.api.constants.IGamlIssue.GENERAL;
import static gama.api.constants.IGamlIssue.IMPORT_ERROR;
import static gaml.compiler.gaml.indexer.GamlResourceIndexer.updateImports;
import static gaml.compiler.gaml.resource.GamlResourceServices.properlyEncodedURI;
import static gaml.compiler.gaml.resource.GamlResourceServices.updateState;
import static java.util.Collections.singleton;
import static org.eclipse.emf.ecore.util.EcoreUtil.resolveAll;
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.getNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.diagnostics.IDiagnosticConsumer;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.linking.impl.XtextLinkingDiagnostic;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.util.OnChangeEvictingCache;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.common.base.Function;

import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.validation.IDocumentationContext;
import gama.api.compilation.validation.IValidationContext;
import gama.api.runtime.scope.IExecutionContext;
import gama.dev.DEBUG;
import gaml.compiler.gaml.factories.ModelFactory;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;
import gaml.compiler.gaml.preprocessor.GamlResourceOffsetMap;

/**
 * The Class GamlResource - Represents a GAML source file resource with validation and compilation capabilities.
 *
 * <p>
 * This class extends Xtext's {@link LazyLinkingResource} to provide GAML-specific resource management, including
 * syntactic and semantic validation, model description building, and documentation generation.
 * </p>
 *
 * <p>
 * <b>Key Responsibilities:</b>
 * </p>
 * <ul>
 * <li>Parse GAML source files into syntactic elements</li>
 * <li>Build model descriptions from syntactic elements and imports</li>
 * <li>Validate models and collect syntactic and semantic errors</li>
 * <li>Manage resource lifecycle (loading, linking, unloading)</li>
 * <li>Coordinate with documentation and indexing services</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b> The {@code element} field is volatile and uses double-checked locking in
 * {@link #getSyntacticContents()} for safe lazy initialization in concurrent environments. Other operations may not be
 * thread-safe and should be externally synchronized if used concurrently.
 * </p>
 *
 * <p>
 * <b>Lifecycle:</b> Resources go through parsing → linking → validation → documentation phases. Use {@link #validate()}
 * to trigger full validation. The resource maintains caches that are invalidated when the source changes.
 * </p>
 *
 * @author drogoul
 * @since 24 avr. 2012
 */
public class GamlResource extends LazyLinkingResource implements IDiagnosticConsumer {
	/* To allow resources to strore bin files : extends StorageAwareResource */

	static {
		DEBUG.OFF();
	}

	/** Cache key for linking context. */
	private static final String LINKING_CACHE_KEY = "linking";

	/** Error message for import location failure. */
	private static final String ERROR_IMPORT_LOCATION = "Impossible to locate import";

	/** Error message template for validation failure. */
	private static final String ERROR_VALIDATION_FAILED = "Impossible to validate %s (check the logs)";

	/** The element. */
	volatile ISyntacticElement element;

	/** The offset map. */
	final GamlResourceOffsetMap offsetMap = new GamlResourceOffsetMap();

	/**
	 * Gets the validation context.
	 *
	 * @return the validation context
	 */
	public IValidationContext getValidationContext() {
		return GamlResourceServices.getOrCreateValidationContext(this);
	}

	/**
	 * Gets the documentation context.
	 *
	 * @return the documentation context
	 */
	public IDocumentationContext getDocumentationContext() {
		return GamlResourceServices.getDocumentationContext(this.getURI());
	}

	/**
	 * Checks for semantic errors.
	 *
	 * @return true, if successful
	 */
	public boolean hasSemanticErrors() {
		return getValidationContext().hasErrors();
	}

	/**
	 * Gets the encoding.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the encoding
	 * @date 13 janv. 2024
	 */
	@Override
	public String getEncoding() { return "UTF-8"; }

	/**
	 * To string.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @date 13 janv. 2024
	 */
	@Override
	public String toString() {
		final URI uri = getURI();
		return "GamlResource[" + (uri != null ? uri.lastSegment() : "unknown") + "]";
	}

	/**
	 * Update with.
	 *
	 * @param model
	 *            the model
	 * @param newState
	 *            the new state
	 */
	public void updateWith(final IModelDescription model, final boolean newState) {
		updateState(getURI(), model, newState, getValidationContext());
	}

	/**
	 * Gets the syntactic contents.
	 *
	 * @return the syntactic contents
	 */
	public ISyntacticElement getSyntacticContents() {
		ISyntacticElement result = element;
		if (result != null) return result;
		synchronized (this) {
			result = element;
			if (result == null) { element = result = GamlResourceServices.buildSyntacticContents(this); }
		}
		return result;
	}

	/** The Constant TO_SYNTACTIC_CONTENTS. */
	final static Function<GamlResource, ISyntacticElement> TO_SYNTACTIC_CONTENTS = input -> {
		input.getResourceSet().getResource(input.getURI(), true);
		return input.getSyntacticContents();
	};

	/**
	 * Builds the model description.
	 *
	 * @param resources
	 *            the resources
	 * @return the model description
	 */
	private IModelDescription buildModelDescription(final ImportedResources resources) {
		final String model = GamlResourceServices.getModelPathOf(this);
		final String project = GamlResourceServices.getProjectPathOf(this);
		final IValidationContext context = getValidationContext();
		final IDocumentationContext doc = getDocumentationContext();
		if (resources == null) return ModelFactory.getInstance().createModelDescription(project, model,
				singleton(getSyntacticContents()), context, doc, null);
		Iterable<ISyntacticElement> imports = resources.computeDirectImports(getSyntacticContents());
		return ModelFactory.getInstance().createModelDescription(project, model, imports, context, doc,
				resources.computeMicroModels(project, model, context, doc));
	}

	/**
	 * Invalidate.
	 *
	 * @param r
	 *            the r
	 * @param s
	 *            the s
	 */
	public void invalidate(final GamlResource r, final String s) {
		final GamlCompilationError error = GamlResourceServices.equals(r.getURI(), getURI())
				? GamlCompilationError.create(s, GENERAL, r.getContents().get(0), GamlCompilationError.Type.Error)
				: GamlCompilationError.create(s, GENERAL, r.getURI(), GamlCompilationError.Type.Error);
		getValidationContext().add(error);
		updateWith(null, true);
	}

	/**
	 * Builds the complete description.
	 *
	 * @return the model description
	 */
	public IModelDescription buildCompleteDescription() {
		final ImportedResources imports = GamlResourceIndexer.validateImportsOf(this);
		if (hasAnyErrors()) return null;
		final IModelDescription model = buildModelDescription(imports);
		// If, for whatever reason, the description is null, we stop the semantic validation
		if (model == null) {
			invalidate(this, String.format(ERROR_VALIDATION_FAILED, URI.decode(getURI().lastSegment())));
		}
		return model;
	}

	/**
	 * Validates the resource by compiling its contents into a ModelDescription and discarding this ModelDescription
	 * afterwards
	 *
	 * @note The errors will be available as part of the ValidationContext, which can later be retrieved from the
	 *       resource, and which contains semantic errors (as opposed to the ones obtained via resource.getErrors(),
	 *       which are syntactic errors), This collector can be probed for compilation errors via its hasErrors(),
	 *       hasInternalErrors(), hasImportedErrors() methods
	 *
	 */
	public void validate() {
		final IModelDescription model = buildCompleteDescription();
		if (model == null) {
			updateWith(null, true);
			return;
		}
		// We then validate it and get rid of the description.
		try {
			updateWith(model.validate(), true);
		} finally {
			// make sure to get rid of the model only after its documentation has been produced
			if (GamlResourceServices.isEdited(this.getURI())) {
				GamlResourceServices.getResourceDocumenter().addDocumentationTask(getURI(), () -> model.dispose());
			} else {
				model.dispose();
			}
		}
	}

	/**
	 * Update internal state.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param oldParseResult
	 *            the old parse result
	 * @param newParseResult
	 *            the new parse result
	 * @date 13 janv. 2024
	 */
	@Override
	protected void updateInternalState(final IParseResult oldParseResult, final IParseResult newParseResult) {
		if (oldParseResult != newParseResult) {
			super.updateInternalState(oldParseResult, newParseResult);
			setElement(null);
		}
	}

	/**
	 * Clear internal state.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 13 janv. 2024
	 */
	@Override
	protected void clearInternalState() {
		super.clearInternalState();
		setElement(null);
		offsetMap.clear();
	}

	/**
	 * Do unload.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 13 janv. 2024
	 */
	@Override
	protected void doUnload() {
		super.doUnload();
		setElement(null);
		offsetMap.clear();
	}

	/**
	 * Sets the element.
	 *
	 * @param model
	 *            the new element
	 */
	private void setElement(final ISyntacticElement model) {
		if (model == element) return;
		if (element != null) { element.dispose(); }
		element = model;
	}

	/**
	 * In the case of synthetic resources, pass the URI they depend on
	 *
	 * @throws IOException
	 */
	public void loadSynthetic(final InputStream is, final IExecutionContext additionalLinkingContext) {
		final OnChangeEvictingCache r = getCache();
		r.getOrCreate(this).set(LINKING_CACHE_KEY, additionalLinkingContext);
		getCache().execWithoutCacheClear(this, new IUnitOfWork.Void<GamlResource>() {

			@Override
			public void process(final GamlResource state) throws Exception {
				state.load(is, null);
				resolveAll(GamlResource.this);
			}
		});
		r.getOrCreate(this).set(LINKING_CACHE_KEY, null);

	}

	/**
	 * Gets the cache.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the cache
	 * @date 13 janv. 2024
	 */
	@Override
	public OnChangeEvictingCache getCache() { return (OnChangeEvictingCache) super.getCache(); }

	/**
	 * Do linking.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 13 janv. 2024
	 */
	@Override
	protected void doLinking() {
		// If the imports are not correctly updated, we cannot proceed
		final EObject faulty = updateImports(this);
		if (faulty != null) {
			getErrors().add(new XtextLinkingDiagnostic(getNode(faulty), ERROR_IMPORT_LOCATION, IMPORT_ERROR, ""));
			return;
		}
		EObject model = getParseResult().getRootASTElement();
		if (model != null) { getLinker().linkModel(model, this); }

	}

	@Override
	protected void doLoad(final InputStream inputStream, final Map<?, ?> options) throws IOException {
		super.doLoad(inputStream, options);
		// GamlEObjectImpl model = (GamlEObjectImpl) getParseResult().getRootASTElement();
		// // if (model != null) {
		// // try {
		// // DEBUG.LOG("Serialization : \n" + model.asString());
		// // } catch (Exception e) {
		// // DEBUG.ERR("Error in serialization", e);
		// // }
		// // }
	}

	// Here we create a reader equipped with the offset map ?
	// @Override
	// protected Reader createReader(final InputStream inputStream) throws IOException {
	// Reader reader = super.createReader(inputStream);
	// return new GamlResourceReader(getURI(), reader, offsetMap);
	// }

	/**
	 * Checks for errors.
	 *
	 * @return true, if successful
	 */
	public boolean hasErrors() {
		return !getErrors().isEmpty() || getParseResult().hasSyntaxErrors();
	}

	/**
	 * Checks for any errors (syntactic or semantic).
	 *
	 * @return true, if this resource has any errors
	 */
	public boolean hasAnyErrors() {
		return hasErrors() || hasSemanticErrors();
	}

	/**
	 * Sets the uri.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param uri
	 *            the new uri
	 * @date 13 janv. 2024
	 */
	/*
	 * Javadoc copied from interface.
	 */
	@Override
	public void setURI(final URI uri) {
		super.setURI(properlyEncodedURI(uri));
	}

	/**
	 * Clear cache.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 13 janv. 2024
	 */
	@Override
	public void clearCache() {
		GamlResourceServices.getResourceDocumenter().invalidate(getURI());
		super.clearCache();
	}

	@Override
	public void consume(final org.eclipse.xtext.diagnostics.Diagnostic diagnostic, final Severity severity) {
		if (isValidationDisabled()) return;
		switch (severity) {
			case ERROR -> getErrors().add(diagnostic);
			case WARNING -> getWarnings().add(diagnostic);
			default -> {
			}
		}
	}

	@Override
	public boolean hasConsumedDiagnostics(final Severity severity) {
		return !getErrors().isEmpty() || !getWarnings().isEmpty();
	}

}
