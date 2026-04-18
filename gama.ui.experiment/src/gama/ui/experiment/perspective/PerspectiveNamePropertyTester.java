/*******************************************************************************************************
 *
 * PerspectiveNamePropertyTester.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.perspective;

import org.eclipse.core.expressions.PropertyTester;

/**
 * The Class PerspectiveNamePropertyTester.
 */
public class PerspectiveNamePropertyTester extends PropertyTester {

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		return receiver instanceof String s && expectedValue instanceof String in && s.contains(in);
	}

}
