/*******************************************************************************************************
 *
 * MathConstantSupplier.java, in gaml.extensions.maths, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.maths.ode;

import gama.gaml.constants.IConstantAcceptor;
import gama.gaml.constants.IConstantsSupplier;

/**
 * The Class MathConstantSupplier.
 */
public class MathConstantSupplier implements IConstantsSupplier {

	@Override
	public void supplyConstantsTo(final IConstantAcceptor acceptor) {
		browse(MathConstants.class, acceptor);
	}

}
