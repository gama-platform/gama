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
import gama.api.compilation.validation.IValidationContext;
import gama.api.gaml.GAML;
import gama.api.runtime.IExecutionContext;
import gama.dev.DEBUG;
import gaml.compiler.gaml.factories.ModelFactory;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;

/**
 * The Class GamlResource.
 */
/*
 *
 * The class GamlResource.
 *
 * @author drogoul
 *
 * @since 24 avr. 2012
 */
public class GamlResource extends LazyLinkingResource implements IDiagnosticConsumer {
	/* To allow resources to strore bin files : extends StorageAwareResource */

	static {
		DEBUG.OFF();
	}

	/** The element. */
	volatile ISyntacticElement element;

	/**
	 * Gets the validation context.
	 *
	 * @return the validation context
	 */
	public IValidationContext getValidationContext() {
		return GamlResourceServices.getOrCreateValidationContext(this);
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
		return "GamlResource[" + getURI().lastSegment() + "]";
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
			if (result == null) {
				element = result = GamlResourceServices.buildSyntacticContents(this);
			}
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
		GAML.getExpressionFactory().resetParser();
		final String model = GamlResourceServices.getModelPathOf(this);
		final String project = GamlResourceServices.getProjectPathOf(this);
		final IValidationContext context = getValidationContext();
		context.shouldDocument(GamlResourceServices.isEdited(this));
		// Creating a new INSTANCE solves sync problem
		if (resources == null) return ModelFactory.getInstance().createModelDescription(project, model,
				singleton(getSyntacticContents()), context, null);
		Iterable<ISyntacticElement> imports = resources.computeDirectImports(getSyntacticContents());
		return ModelFactory.getInstance().createModelDescription(project, model, imports, context,
				resources.computeMicroModels(project, model, context));
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
		GamlCompilationError error = null;
		if (GamlResourceServices.equals(r.getURI(), getURI())) {
			error = new GamlCompilationError(s, GENERAL, r.getContents().get(0), GamlCompilationError.Type.Error);
		} else {
			error = new GamlCompilationError(s, GENERAL, r.getURI(), GamlCompilationError.Type.Error);
		}
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
		final boolean hasErrors = hasErrors() || hasSemanticErrors();
		if (hasErrors) return null;
		final IModelDescription model = buildModelDescription(imports);
		// If, for whatever reason, the description is null, we stop the
		// semantic validation
		if (model == null) {
			invalidate(this, "Impossible to validate " + URI.decode(getURI().lastSegment()) + " (check the logs)");
		}
		// Map<URI, URI> doubleImports = collectMultipleImportsOf(this);
		// doubleImports.forEach((imported, importer) -> {
		// String s = imported.lastSegment() + " is already imported by " + importer.lastSegment();
		// EObject o = getImportObject(getContents().get(0), getURI(), imported);
		// GamlCompilationError error = new GamlCompilationError(s, GENERAL, o, false, true);
		// getValidationContext().add(error);
		// });
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
		// DEBUG.LOG("Resource validating itself");
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
			// }
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
			// if (oldParseResult != newParseResult) { DEBUG.OUT("===> Creating a new contents for " +
			// uri.lastSegment()); }
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
		r.getOrCreate(this).set("linking", additionalLinkingContext);
		getCache().execWithoutCacheClear(this, new IUnitOfWork.Void<GamlResource>() {

			@Override
			public void process(final GamlResource state) throws Exception {
				state.load(is, null);
				resolveAll(GamlResource.this);
			}
		});
		r.getOrCreate(this).set("linking", null);

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
			getErrors()
					.add(new XtextLinkingDiagnostic(getNode(faulty), "Impossible to locate import", IMPORT_ERROR, ""));
			return;
		}
		EObject model = getParseResult().getRootASTElement();
		if (model != null) { getLinker().linkModel(model, this); }
	}

	/**
	 * Checks for errors.
	 *
	 * @return true, if successful
	 */
	public boolean hasErrors() {
		return !getErrors().isEmpty() || getParseResult().hasSyntaxErrors();
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
		// DEBUG.LINE();
		// DEBUG.TITLE("CLEARING CACHE OF " + uri.lastSegment());
		GamlResourceServices.getResourceDocumenter().invalidate(getURI());
		super.clearCache();
	}

	@Override
	public void consume(final org.eclipse.xtext.diagnostics.Diagnostic diagnostic, final Severity severity) {
		if (isValidationDisabled()) return;
		switch (severity) {
			case ERROR -> getErrors().add(diagnostic);
			case WARNING -> getWarnings().add(diagnostic);
			default -> {}
		}
	}

	@Override
	public boolean hasConsumedDiagnostics(final Severity severity) {
		return !getErrors().isEmpty() || !getWarnings().isEmpty();
	}

}
