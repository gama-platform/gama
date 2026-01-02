/*******************************************************************************************************
 *
 * ValidNameValidator.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation;

import gama.gaml.descriptions.IDescription;

/**
 * The Class ValidNameValidator.
 */
public class ValidNameValidator implements IDescriptionValidator {

	/**
	 * Verifies that the name is valid (non reserved, non type and non species)
	 *
	 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
	 */
	@Override
	public void validate(final IDescription cd) {
		Assert.nameIsValid(cd);
	}
}