/**
 *
 */
package gaml.compiler.gaml.descriptions;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;

/**
 *
 */
public abstract class DescriptionErrorManager implements IDescription {

	/** Constant for empty data arrays. */
	final static String[] EMPTY_DATA = {};

	/**
	 * Internal method to handle error, warning, and info flags during validation. Determines the proper reporting
	 * method based on the error type and context.
	 *
	 * @param s
	 *            the message text
	 * @param code
	 *            the issue code
	 * @param type
	 *            the error type (Error, Warning, or Info)
	 * @param source
	 *            the source object where the issue occurred
	 * @param data
	 *            additional data for the issue
	 * @throws GamaRuntimeException
	 *             if there's no way to report the error in compile time
	 */
	protected void flagError(final String s, final String code, final GamlCompilationError.Type type,
			final EObject source, final String... data) throws GamaRuntimeException {

		if (type == GamlCompilationError.Type.Warning && !GamaPreferences.Modeling.WARNINGS_ENABLED.getValue()
				|| type == GamlCompilationError.Type.Info && !GamaPreferences.Modeling.INFO_ENABLED.getValue())
			return;

		EObject e = source;
		IDescription desc = this;
		if (e == null) { e = getUnderlyingElement(); }
		while (e == null && desc != null) {
			desc = desc.getEnclosingDescription();
			if (desc != null) { e = desc.getUnderlyingElement(); }
		}
		// throws a runtime exception if there is no way to signal the error in the source
		// (i.e. we are probably in a runtime scenario)
		if (e == null || e.eResource() == null
				|| e.eResource().getURI().path().contains(IKeyword.SYNTHETIC_RESOURCES_PREFIX)) {
			if (type == GamlCompilationError.Type.Error)
				throw GamaRuntimeException.error(s, gama.api.GAMA.getRuntimeScope());
			return;

		}
		final IValidationContext c = getValidationContext();
		if (c == null) {
			DEBUG.ERR((type == GamlCompilationError.Type.Warning ? "Warning" : "Error") + ": " + s);
			return;
		}
		c.add(GamlCompilationError.create(s, code, e, type, data));
	}

	/**
	 * Reports a general error with this description.
	 *
	 * @param message
	 *            the error message
	 */
	@Override
	public void error(final String message) {
		error(message, IGamlIssue.GENERAL);
	}

	/**
	 * Reports an error with a specific issue code.
	 *
	 * @param message
	 *            the error message
	 * @param code
	 *            the issue code
	 */
	@Override
	public void error(final String message, final String code) {
		flagError(message, code, GamlCompilationError.Type.Error, getUnderlyingElement(), EMPTY_DATA);
	}

	/**
	 * Reports an error related to a specific EMF object.
	 *
	 * @param s
	 *            the error message
	 * @param code
	 *            the issue code
	 * @param facet
	 *            the EMF object associated with the error
	 * @param data
	 *            additional data for the error
	 */
	@Override
	public void error(final String s, final String code, final EObject facet, final String... data) {
		flagError(s, code, GamlCompilationError.Type.Error, facet, data);
	}

	/**
	 * Reports an error related to a specific facet.
	 *
	 * @param s
	 *            the error message
	 * @param code
	 *            the issue code
	 * @param facet
	 *            the name of the facet with the error
	 * @param data
	 *            additional data for the error
	 */
	@Override
	public void error(final String s, final String code, final String facet, final String... data) {
		flagError(s, code, GamlCompilationError.Type.Error,
				getUnderlyingElement(facet, IGamlIssue.UNKNOWN_FACET.equals(code)),
				data == null || data.length == 0 ? new String[] { facet } : data);
	}

	/**
	 * Reports an informational message with a specific issue code.
	 *
	 * @param message
	 *            the info message
	 * @param code
	 *            the issue code
	 */
	@Override
	public void info(final String message, final String code) {
		flagError(message, code, GamlCompilationError.Type.Info, getUnderlyingElement(), EMPTY_DATA);
	}

	/**
	 * Reports an informational message related to a specific EMF object.
	 *
	 * @param s
	 *            the info message
	 * @param code
	 *            the issue code
	 * @param facet
	 *            the EMF object associated with the info
	 * @param data
	 *            additional data for the info
	 */
	@Override
	public void info(final String s, final String code, final EObject facet, final String... data) {
		flagError(s, code, GamlCompilationError.Type.Info, facet, data);
	}

	/**
	 * Reports an informational message related to a specific facet.
	 *
	 * @param s
	 *            the info message
	 * @param code
	 *            the issue code
	 * @param facet
	 *            the name of the facet
	 * @param data
	 *            additional data for the info
	 */
	@Override
	public void info(final String s, final String code, final String facet, final String... data) {
		flagError(s, code, GamlCompilationError.Type.Info, this.getUnderlyingElement(facet, false),
				data == null || data.length == 0 ? new String[] { facet } : data);
	}

	/**
	 * Warning.
	 *
	 * @param message
	 *            the message
	 */
	public void warning(final String message) {
		warning(message, IGamlIssue.GENERAL);
	}

	/**
	 * Reports a warning with a specific issue code.
	 *
	 * @param message
	 *            the warning message
	 * @param code
	 *            the issue code
	 */
	@Override
	public void warning(final String message, final String code) {
		flagError(message, code, GamlCompilationError.Type.Warning, null, EMPTY_DATA);
	}

	/**
	 * Reports a warning related to a specific EMF object.
	 *
	 * @param s
	 *            the warning message
	 * @param code
	 *            the issue code
	 * @param object
	 *            the EMF object associated with the warning
	 * @param data
	 *            additional data for the warning
	 */
	@Override
	public void warning(final String s, final String code, final EObject object, final String... data) {
		flagError(s, code, GamlCompilationError.Type.Warning, object, data);
	}

	/**
	 * Reports a warning related to a specific facet.
	 *
	 * @param s
	 *            the warning message
	 * @param code
	 *            the issue code
	 * @param facet
	 *            the name of the facet
	 * @param data
	 *            additional data for the warning
	 */
	@Override
	public void warning(final String s, final String code, final String facet, final String... data) {
		flagError(s, code, GamlCompilationError.Type.Warning,
				this.getUnderlyingElement(facet, IGamlIssue.UNKNOWN_FACET.equals(code)),
				data == null || data.length == 0 ? new String[] { facet } : data);
	}

}
