/*******************************************************************************************************
 *
 * GamlTextValidator.java, in gaml.compiler.gaml, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.validation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.linking.impl.XtextLinkingDiagnostic;
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic;
import org.eclipse.xtext.validation.EObjectDiagnosticImpl;

import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.compilation.IGamlTextValidator;
import gama.gaml.compilation.GamlCompilationError.GamlCompilationErrorType;
import gama.gaml.interfaces.IGamlIssue;
import gaml.compiler.gaml.resource.GamlResource;
import gaml.compiler.gaml.resource.GamlResourceServices;

/**
 * The Class GamlTextValidator.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 11 janv. 2024
 */
public class GamlTextValidator implements IGamlTextValidator {

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
					GamlCompilationError error;
					if (d instanceof EObjectDiagnosticImpl ed) {
						error = new GamlCompilationError(ed.getMessage(), IGamlIssue.SYNTACTIC_ERROR,
								ed.getProblematicObject(), 
								Severity.WARNING.equals(ed.getSeverity()) ? GamlCompilationErrorType.Warning 
										: Severity.INFO.equals(ed.getSeverity()) ? GamlCompilationErrorType.Info : GamlCompilationErrorType.Error
								//Previously: Severity.WARNING.equals(ed.getSeverity()),Severity.INFO.equals(ed.getSeverity())
								, ed.getData());
					} else if (d instanceof XtextLinkingDiagnostic ld) {
						error = new GamlCompilationError(ld.getMessage(), IGamlIssue.LINKING_ERROR,
								ld.getUriToProblem(), GamlCompilationErrorType.Error, ld.getData());
					} else if (d instanceof XtextSyntaxDiagnostic sd) {
						error = new GamlCompilationError(sd.getMessage(), IGamlIssue.SYNTACTIC_ERROR,
								sd.getUriToProblem(), GamlCompilationErrorType.Error, sd.getData());
					} else {
						error = new GamlCompilationError(d.getMessage(), IGamlIssue.SYNTACTIC_ERROR, resource.getURI(),
								GamlCompilationErrorType.Error);
					}
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
}
