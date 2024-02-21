/*******************************************************************************************************
 *
 * IGamlBuilderListener.java, in gaml.compiler.gaml, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.validation;

import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.ValidationContext;

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
	void validationEnded(ModelDescription model, final Iterable<? extends IDescription> experiments,
			final ValidationContext status);
}
