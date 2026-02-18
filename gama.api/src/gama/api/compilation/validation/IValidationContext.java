/*******************************************************************************************************
 *
 * IValidationContext.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.GamlCompilationError;

/**
 *
 */
public interface IValidationContext extends Iterable<GamlCompilationError> {

	/** The Constant IMPORTED_FROM. */
	String IMPORTED_FROM = "imported from";

	/**
	 * Adds the.
	 *
	 * @param error
	 *            the error
	 * @return true, if successful
	 */
	boolean add(GamlCompilationError error);

	/**
	 * Checks for internal syntax errors.
	 *
	 * @return true, if successful
	 */
	boolean hasInternalSyntaxErrors();

	/**
	 * Checks for errors.
	 *
	 * @return true, if successful
	 */
	boolean hasErrors();

	/**
	 * Checks for internal errors.
	 *
	 * @return true, if successful
	 */
	boolean hasInternalErrors();

	/**
	 * Checks for imported errors.
	 *
	 * @return true, if successful
	 */
	boolean hasImportedErrors();

	/**
	 * Gets the internal errors.
	 *
	 * @return the internal errors
	 */
	Iterable<GamlCompilationError> getInternalErrors();

	/**
	 * Gets the imported errors.
	 *
	 * @return the imported errors
	 */
	Collection<GamlCompilationError> getImportedErrors();

	/**
	 * Gets the warnings.
	 *
	 * @return the warnings
	 */
	Iterable<GamlCompilationError> getWarnings();

	/**
	 * Gets the infos.
	 *
	 * @return the infos
	 */
	Iterable<GamlCompilationError> getInfos();

	/**
	 * Clear.
	 */
	void clear();

	/**
	 * Method iterator()
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	Iterator<GamlCompilationError> iterator();

	/**
	 * Gets the imported errors as strings.
	 *
	 * @return the imported errors as strings
	 */
	Map<String, URI> getImportedErrorsAsStrings();

	/**
	 * Sets the no warning.
	 */
	void setNoWarning();

	/**
	 * Sets the no info.
	 */
	void setNoInfo();

	/**
	 * Reset info and warning.
	 */
	void resetInfoAndWarning();

	/**
	 * Checks for error on.
	 *
	 * @param objects
	 *            the objects
	 * @return true, if successful
	 */
	boolean hasErrorOn(EObject... objects);

	/**
	 * Sets the no experiment.
	 */
	void setNoExperiment();

	/**
	 * Gets the no experiment.
	 *
	 * @return the no experiment
	 */
	boolean getNoExperiment();

	/**
	 * Verify plugins. Returns true if all the plugins are present in the current platform
	 *
	 * @param list
	 *            the list
	 * @return true, if successful
	 */
	boolean verifyPlugins(List<String> list);

	/**
	 * Gets the uri.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the uri
	 * @date 10 janv. 2024
	 */
	URI getURI();

}