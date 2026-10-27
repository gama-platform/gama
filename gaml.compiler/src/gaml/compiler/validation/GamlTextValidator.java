/*******************************************************************************************************
 *
 * GamlTextValidator.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.validation;

import static gama.api.compilation.GamlCompilationError.Type.Error;
import static gama.api.compilation.GamlCompilationError.Type.Info;
import static gama.api.compilation.GamlCompilationError.Type.Warning;
import static gama.api.constants.IGamlIssue.GENERAL;
import static gama.api.constants.IGamlIssue.LINKING_ERROR;
import static gama.api.constants.IGamlIssue.SYNTACTIC_ERROR;
import static org.eclipse.xtext.diagnostics.Severity.INFO;
import static org.eclipse.xtext.diagnostics.Severity.WARNING;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.linking.impl.XtextLinkingDiagnostic;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic;
import org.eclipse.xtext.validation.EObjectDiagnosticImpl;

import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.constants.IGamlIssue;
import gama.api.compilation.validation.IGamlTextValidator;
import gaml.compiler.prototypes.SymbolArtefact;
import gaml.compiler.resource.GamlResource;
import gaml.compiler.resource.GamlResourceServices;

/**
 * The Class GamlTextValidator.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 11 janv. 2024
 */
public class GamlTextValidator implements IGamlTextValidator {

	/** The instance. */
	private static GamlTextValidator INSTANCE = new GamlTextValidator();

	/**
	 * Gets the single instance of GamlTextValidator.
	 *
	 * @return single instance of GamlTextValidator
	 */
	public static GamlTextValidator getInstance() { return INSTANCE; }

	/**
	 * Instantiates a new gaml text validator.
	 */
	protected GamlTextValidator() {}

	static GamlCompilationError compilationError(final Resource.Diagnostic diagnostic, final URI uri) {
		GamlCompilationError error = switch (diagnostic) {
			case EObjectDiagnosticImpl ed -> GamlCompilationError.create(ed.getMessage(), SYNTACTIC_ERROR,
					ed.getProblematicObject(), WARNING.equals(ed.getSeverity()) ? Warning
							: INFO.equals(ed.getSeverity()) ? Info : Error,
					ed.getData());
			case XtextLinkingDiagnostic ld -> GamlCompilationError.create(ld.getMessage(), LINKING_ERROR,
					uri, Error, ld.getData());
			case XtextSyntaxDiagnostic sd -> GamlCompilationError.create(sd.getMessage(), SYNTACTIC_ERROR,
					uri, Error, sd.getData());
			default -> GamlCompilationError.create(diagnostic.getMessage(), SYNTACTIC_ERROR, uri, Error);
		};
		int offset = diagnostic instanceof org.eclipse.xtext.diagnostics.Diagnostic d ? d.getOffset() : 0;
		return error.withLocation(new GamlCompilationError.Location(diagnostic.getLine(), diagnostic.getColumn(), offset));
	}

	/**
	 * Syntactic validation of model.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param expr
	 *            the expr
	 * @param errors
	 *            the errors
	 * @date 11 janv. 2024
	 */
	@Override
	public void validateModel(final String expr, final List<GamlCompilationError> errors, final boolean syntaxOnly) {
		final GamlResource resource = GamlResourceServices.getTemporaryResource(null);
		resource.setValidationOnly(true);
		try {
			final InputStream is = new ByteArrayInputStream(expr.getBytes(StandardCharsets.UTF_8));
			try {
				resource.loadSynthetic(is, null);
			} catch (final Exception e1) {
				e1.printStackTrace();
			} finally {}
			if (resource.hasErrors()) {
				for (Resource.Diagnostic d : resource.getErrors()) {
					errors.add(compilationError(d, resource.getURI()));
				}
			}
			if (syntaxOnly) {
				if (errors.isEmpty()) { validatePlacement(resource, errors); }
				return;
			}
			try {
				resource.validate();
			} catch (Throwable t) {
				// DescriptionErrorManager throws for errors it cannot properly report. So we have to catch it here
				// and register it as a regular error.
				errors.add(GamlCompilationError.create(t.getMessage(), GENERAL, resource.getURI(), Error));
			}
			if (resource.hasSemanticErrors()) {
				for (GamlCompilationError error : resource.getValidationContext().getInternalErrors()) {
					errors.add(error);
				}
			}

		} finally {
			GamlResourceServices.discardTemporaryResource(resource);
		}
	}

	/**
	 * The grammar accepts any statement in any block, so syntax-only validation also checks that each statement is
	 * allowed in its enclosing context. Descriptions are built but not validated; other semantic errors are ignored.
	 */
	private void validatePlacement(final GamlResource resource, final List<GamlCompilationError> errors) {
		IModelDescription model = null;
		try {
			model = resource.buildCompleteDescription();
			if (model == null) return;
			model.visitOwnChildrenRecursively(desc -> {
				final IDescription enclosing = desc.getEnclosingDescription();
				if (enclosing != null && !desc.isBuiltIn() && desc.getArtefact() instanceof SymbolArtefact artefact
						&& !artefact.canBeDefinedIn(enclosing)) {
					errors.add(GamlCompilationError.create(
							desc.getKeyword() + " cannot be defined in " + enclosing.getKeyword(),
							IGamlIssue.WRONG_CONTEXT, desc.getUnderlyingElement(), Error));
				}
				return true;
			});
		} catch (final Throwable t) {
			// Placement cannot be determined; syntax-only validation reports grammar errors only.
		} finally {
			if (model != null) { model.dispose(); }
		}
	}

	@Override
	public int[] getStartLineAndOffsetInFileInfo(final EObject source) {
		int[] result = new int[2];
		ICompositeNode node = NodeModelUtils.getNode(source);
		if (node != null) {
			result[0] = node.getStartLine();
			result[1] = node.getTotalOffset();
		}
		return result;
	}

	@Override
	public GamlCompilationError.Location getSourceLocation(final EObject source) {
		final ICompositeNode node = NodeModelUtils.getNode(source);
		if (node == null) return GamlCompilationError.Location.UNKNOWN;
		final var position = NodeModelUtils.getLineAndColumn(node.getRootNode(), node.getOffset());
		return new GamlCompilationError.Location(position.getLine(), position.getColumn(), node.getOffset());
	}
}
