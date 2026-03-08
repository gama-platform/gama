/*******************************************************************************************************
 *
 * ErrorToDiagnoticTranslator.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.validation;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.util.Arrays;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.FeatureBasedDiagnostic;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.validation.IValidationContext;
import gaml.compiler.gaml.GamlDefinition;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.impl.StatementImpl;
import gaml.compiler.gaml.resource.GamlResource;
import gaml.compiler.gaml.resource.GamlResourceServices;

/**
 * The Class ErrorToDiagnoticTranslator.
 */
@Singleton
public class ErrorToDiagnoticTranslator {

	/**
	 * Instantiates a new error to diagnotic translator.
	 */
	@Inject
	public ErrorToDiagnoticTranslator() {}

	/**
	 * Translate.
	 *
	 * @param errors
	 *            the errors
	 * @param r
	 *            the r
	 * @param mode
	 *            the mode
	 * @return the diagnostic
	 */
	public Diagnostic translate(final IValidationContext errors, final GamlResource r, final CheckMode mode) {
		final BasicDiagnostic chain = new BasicDiagnostic();
		for (final GamlCompilationError e : errors) {
			final Diagnostic d = translate(e, r, mode);
			if (d != null) { chain.add(d); }
		}
		return chain;
	}

	/**
	 * Translate.
	 *
	 * @param e
	 *            the e
	 * @param r
	 *            the r
	 * @param mode
	 *            the mode
	 * @return the diagnostic
	 */
	public Diagnostic translate(final GamlCompilationError e, final GamlResource r, final CheckMode mode) {
		final URI errorURI = e.uri();
		if (!GamlResourceServices.equals(errorURI, r.getURI())) // final String s = URI.decode(errorURI.lastSegment());
			// final EObject m = r.getContents().get(0);
			// final EObject eObject = findImportWith(m, s);
			// final EAttribute feature = eObject instanceof Model ? GamlPackage.Literals.GAML_DEFINITION__NAME
			// : eObject instanceof HeadlessExperiment ? GamlPackage.Literals.HEADLESS_EXPERIMENT__IMPORT_URI
			// : GamlPackage.Literals.IMPORT__IMPORT_URI;
			return null;
		// return createDiagnostic(CheckMode.NORMAL_ONLY, Diagnostic.ERROR,
		// e.toString() + " (" + ValidationContext.IMPORTED_FROM + " " + s + ")", eObject, feature,
		// ValidationMessageAcceptor.INSIGNIFICANT_INDEX, e.getCode(), e.getData());
		EStructuralFeature feature = null;
		final EObject object = e.source();
		String[] data = e.data();
		if (object instanceof GamlDefinition && data != null && data.length > 0 && IKeyword.NAME.equals(data[0])) {
			feature = GamlPackage.Literals.GAML_DEFINITION__NAME;
		} else if (object instanceof Statement) {

			final StatementImpl s = (StatementImpl) object;
			// Not sure about this access to key ? Used to STATEMENT__KEY, but this feature seems to have disappeared
			if (s.eIsSet(GamlPackage.Literals.FACETS_AND_BLOCK__KEY)) {
				feature = GamlPackage.Literals.FACETS_AND_BLOCK__KEY;
			} else if (s.eIsSet(GamlPackage.Literals.SDEFINITION__TKEY)) {
				feature = GamlPackage.Literals.SDEFINITION__TKEY;
			}
		} else if (object instanceof Model) { feature = GamlPackage.Literals.GAML_DEFINITION__NAME; }
		if (!Arrays.contains(e.data(), null)) {
			final int index = ValidationMessageAcceptor.INSIGNIFICANT_INDEX;
			return createDiagnostic(mode, toDiagnosticSeverity(e), e.toString(), object, feature, index, e.code(),
					e.data());
		}
		return null;
	}

	/**
	 * Creates the diagnostic.
	 *
	 * @param mode
	 *            the mode
	 * @param diagnosticSeverity
	 *            the diagnostic severity
	 * @param message
	 *            the message
	 * @param object
	 *            the object
	 * @param feature
	 *            the feature
	 * @param index
	 *            the index
	 * @param code
	 *            the code
	 * @param issueData
	 *            the issue data
	 * @return the diagnostic
	 */
	private Diagnostic createDiagnostic(final CheckMode mode, final int diagnosticSeverity, final String message,
			final EObject object, final EStructuralFeature feature, final int index, final String code,
			final String... issueData) {
		return new FeatureBasedDiagnostic(diagnosticSeverity, message, object, feature, index, getType(mode), code,
				issueData);
	}

	/**
	 * Gets the type.
	 *
	 * @param mode
	 *            the mode
	 * @return the type
	 */
	private CheckType getType(final CheckMode mode) {
		if (mode == CheckMode.FAST_ONLY) return CheckType.FAST;
		if (mode == CheckMode.EXPENSIVE_ONLY) return CheckType.EXPENSIVE;
		if (mode == CheckMode.ALL || mode == CheckMode.NORMAL_AND_FAST || mode != CheckMode.NORMAL_ONLY)
			return CheckType.FAST;
		return CheckType.NORMAL;
	}

	/**
	 * To diagnostic severity.
	 *
	 * @param e
	 *            the e
	 * @return the int
	 */
	protected int toDiagnosticSeverity(final GamlCompilationError e) {
		int diagnosticSeverity = -1;
		if (e.isError()) {
			diagnosticSeverity = Diagnostic.ERROR;
		} else if (e.isWarning()) {
			diagnosticSeverity = Diagnostic.WARNING;
		} else if (e.isInfo()) { diagnosticSeverity = Diagnostic.INFO; }

		return diagnosticSeverity;
	}

}
