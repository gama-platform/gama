/*******************************************************************************************************
 *
 * IGamlBuilderListener.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.validation;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.validation.IValidationContext;

/**
 * The class IGamlBuilder.
 *
 * @author drogoul
 * @since 2 mars 2012
 *
 */
public interface IGamlBuilderListener {

	/**
	 * Validation ended.
	 *
	 * @param model
	 *
	 * @param experiments
	 *            the experiments
	 * @param status
	 *            the status
	 */
	void validationEnded(IModelDescription model, final Iterable<? extends IDescription> experiments,
			final IValidationContext status);
}
