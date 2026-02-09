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
package gaml.compiler.gaml.validation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.linking.impl.XtextLinkingDiagnostic;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic;
import org.eclipse.xtext.validation.EObjectDiagnosticImpl;

import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.validation.IGamlTextValidator;
import gama.api.constants.IGamlIssue;
import gaml.compiler.gaml.resource.GamlResource;
import gaml.compiler.gaml.resource.GamlResourceServices;

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
		try {
			final InputStream is = new ByteArrayInputStream(expr.getBytes());
			try {
				resource.loadSynthetic(is, null);
			} catch (final Exception e1) {
				e1.printStackTrace();
			} finally {}
			if (resource.hasErrors()) {
				for (Resource.Diagnostic d : resource.getErrors()) {
					GamlCompilationError error = switch (d) {
						case EObjectDiagnosticImpl ed -> new GamlCompilationError(ed.getMessage(),
								IGamlIssue.SYNTACTIC_ERROR, ed.getProblematicObject(),
								Severity.WARNING.equals(ed.getSeverity()) ? GamlCompilationError.Type.Warning
										: Severity.INFO.equals(ed.getSeverity()) ? GamlCompilationError.Type.Info
										: GamlCompilationError.Type.Error
							// Previously:
							// Severity.WARNING.equals(ed.getSeverity()),Severity.INFO.equals(ed.getSeverity())
								, ed.getData()); // Previously: //
													// Severity.WARNING.equals(ed.getSeverity()),Severity.INFO.equals(ed.getSeverity())
						case XtextLinkingDiagnostic ld -> new GamlCompilationError(ld.getMessage(),
								IGamlIssue.LINKING_ERROR, ld.getUriToProblem(), GamlCompilationError.Type.Error,
								ld.getData());
						case XtextSyntaxDiagnostic sd -> new GamlCompilationError(sd.getMessage(),
								IGamlIssue.SYNTACTIC_ERROR, sd.getUriToProblem(), GamlCompilationError.Type.Error,
								sd.getData());
						case null, default -> new GamlCompilationError(d.getMessage(), IGamlIssue.SYNTACTIC_ERROR,
								resource.getURI(), GamlCompilationError.Type.Error);
					};
					errors.add(error);
				}
			}
			if (syntaxOnly) return;
			resource.validate();
			if (resource.hasSemanticErrors()) {
				for (GamlCompilationError error : resource.getValidationContext().getInternalErrors()) {
					errors.add(error);
				}
			}

		} finally {
			GamlResourceServices.discardTemporaryResource(resource);
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
}
