/*******************************************************************************************************
 *
 * GamlResourceValidator.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IDiagnosticConverter;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.inject.Inject;

import gama.api.GAMA;
import gama.api.types.color.GamaColorFactory;
import gama.api.ui.IStatusMessage;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gaml.compiler.gaml.resource.GamlResource;
import gaml.compiler.gaml.resource.GamlResourceServices;

/**
 * Validates GAML resources by performing syntax, linking, and semantic validation.
 * 
 * <p>This validator is responsible for the complete validation pipeline of GAML resources, including:
 * <ul>
 * <li>Resolution of cross-references between model elements</li>
 * <li>Collection of syntax and linking errors from the parsed resource</li>
 * <li>Execution of semantic validation rules on the model</li>
 * <li>Translation of validation diagnostics into IDE-compatible issues</li>
 * </ul>
 * 
 * <p>The validation process respects cancellation requests through the {@link CancelIndicator} and tracks
 * the total validation duration for performance monitoring purposes.
 * 
 * @see IResourceValidator
 * @see GamlResource
 * @see ErrorToDiagnoticTranslator
 */
public class GamlResourceValidator implements IResourceValidator {

	/** 
	 * Tracks the cumulative time spent in validation operations across all resources.
	 * Used for performance monitoring and profiling. Value is in nanoseconds.
	 */
	static long VALIDATION_DURATION = 0;

	/**
	 * Reset.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 13 janv. 2024
	 */
	public static void RESET() {
		VALIDATION_DURATION = 0;
	}

	/**
	 * Duration.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the long
	 * @date 13 janv. 2024
	 */
	public static long DURATION() {
		return VALIDATION_DURATION;
	}

	static {
		DEBUG.OFF();
	}

	/** 
	 * Converts EMF diagnostics into Xtext issues that can be displayed in the IDE.
	 * Injected by the dependency injection framework.
	 */
	@Inject IDiagnosticConverter converter;

	/** 
	 * Translates GAML validation errors from the resource's validation context into EMF diagnostics.
	 * This translator bridges the gap between GAML's internal error representation and the
	 * standard EMF diagnostic model used by Xtext and Eclipse.
	 */
	private static final ErrorToDiagnoticTranslator ERROR_TRANSLATOR = new ErrorToDiagnoticTranslator();

	/**
	 * Validates a GAML resource and returns a list of validation issues.
	 * 
	 * <p>This method performs a complete validation pipeline:
	 * <ol>
	 * <li>Checks for cancellation and validates resource type</li>
	 * <li>Updates the UI with compilation status</li>
	 * <li>Resolves all lazy cross-references in the model</li>
	 * <li>Collects syntax and linking errors from the parsed resource</li>
	 * <li>Executes semantic validation on the resource</li>
	 * <li>Translates validation diagnostics into issues</li>
	 * <li>Cleans up the validation context</li>
	 * </ol>
	 * 
	 * <p>The validation respects cancellation requests at multiple points to allow
	 * early termination of long-running validations.
	 * 
	 * @param resource the GAML resource to validate (must be a {@link GamlResource})
	 * @param mode the validation mode (e.g., NORMAL_AND_FAST, ALL)
	 * @param indicator cancellation indicator to check for user cancellation requests, may be null
	 * @return a list of validation issues found, or an empty list if validation was cancelled
	 *         or the resource is not a valid GamlResource
	 */
	@Override
	public List<Issue> validate(final Resource resource, final CheckMode mode, final CancelIndicator indicator) {
		// Early cancellation check
		if (indicator != null && indicator.isCanceled()) return List.of();

		// Validate resource type
		if (!(resource instanceof GamlResource r)) return List.of();

		final String name = org.eclipse.emf.common.util.URI.decode(resource.getURI().lastSegment());
		final List<Issue> result = new ArrayList<>();
		
		updateCompilationStatus(name);
		
		DEBUG.TIMER(BANNER_CATEGORY.COMPIL, name, "in", () -> {
			final IAcceptor<Issue> acceptor = issue -> {
				if (issue.getMessage() != null && !issue.getMessage().isEmpty()) { 
					result.add(issue); 
				}
			};
			
			// Check cancellation before expensive operations
			if (indicator != null && indicator.isCanceled()) return;
			
			// Resolve cross references
			EcoreUtil2.resolveLazyCrossReferences(resource, indicator);
			
			// Collect syntax/linking issues
			collectResourceErrors(resource, acceptor);
			
			// Check cancellation before validation
			if (indicator != null && indicator.isCanceled()) return;
			
			// Validate the resource and collect semantic errors
			r.validate();
			collectValidationErrors(r, mode, acceptor);
			
			// Clean up validation context
			GamlResourceServices.discardValidationContext(r);
		}, duration -> VALIDATION_DURATION += duration);
		
		return result;
	}

	/**
	 * Updates the compilation status message in the GAMA UI.
	 * 
	 * <p>Displays a visual indicator that the specified file is being compiled.
	 * If the GUI is not available (e.g., in headless mode), the exception is
	 * silently ignored to allow validation to continue.
	 * 
	 * @param name the name of the file being compiled
	 */
	private void updateCompilationStatus(final String name) {
		try {
			GAMA.getGui().getStatus().setStatus("Compilation of " + name, IStatusMessage.COMPILE_ICON,
					GamaColorFactory.get(200, 200, 200));
		} catch (final Exception e) {
			// Ignore if GUI is not available
		}
	}

	/**
	 * Collects syntax and linking errors from the resource.
	 * 
	 * <p>These are low-level errors that occur during parsing and cross-reference resolution,
	 * such as:
	 * <ul>
	 * <li>Syntax errors (invalid GAML syntax)</li>
	 * <li>Linking errors (unresolved references to variables, types, or actions)</li>
	 * <li>Lexical errors (invalid tokens)</li>
	 * </ul>
	 * 
	 * @param resource the resource whose errors should be collected
	 * @param acceptor the acceptor that receives converted issue objects
	 */
	private void collectResourceErrors(final Resource resource, final IAcceptor<Issue> acceptor) {
		for (final org.eclipse.emf.ecore.resource.Resource.Diagnostic element : resource.getErrors()) {
			converter.convertResourceDiagnostic(element, Severity.ERROR, acceptor);
		}
	}

	/**
	 * Collects semantic validation errors from the resource's validation context.
	 * 
	 * <p>These are high-level errors detected by GAML's semantic validation rules, such as:
	 * <ul>
	 * <li>Type mismatches</li>
	 * <li>Invalid attribute values</li>
	 * <li>Constraint violations</li>
	 * <li>Model consistency issues</li>
	 * </ul>
	 * 
	 * <p>The validation context contains all errors accumulated during the resource's
	 * {@link GamlResource#validate()} execution. These errors are translated from GAML's
	 * internal representation to EMF diagnostics, then converted to Xtext issues.
	 * 
	 * @param resource the GAML resource whose validation errors should be collected
	 * @param mode the validation mode that determines which validators to run
	 * @param acceptor the acceptor that receives converted issue objects
	 */
	private void collectValidationErrors(final GamlResource resource, final CheckMode mode,
			final IAcceptor<Issue> acceptor) {
		for (final Diagnostic d : ERROR_TRANSLATOR.translate(resource.getValidationContext(), resource, mode)
				.getChildren()) {
			converter.convertValidatorDiagnostic(d, acceptor);
		}
	}

}
