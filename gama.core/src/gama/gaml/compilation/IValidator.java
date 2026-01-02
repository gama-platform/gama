/*******************************************************************************************************
 *
 * IValidator.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.precompiler.OkForAPI;
import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlIssue;

/**
 * 'Tagging' interface for IExpression and IDescription validators
 *
 * @author A. Drogoul
 * @since July 2018
 *
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface IValidator extends IKeyword, IGamlIssue {

	/** The null. */
	IValidator NULL = (d, c, a) -> true;

	/**
	 * Validate.
	 *
	 * @param description
	 *            the description
	 * @param emfContext
	 *            the emf context
	 * @param arguments
	 *            the arguments
	 * @return true, if successful
	 */
	boolean validate(IDescription description, EObject emfContext, IExpression... arguments);
}
