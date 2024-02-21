/*******************************************************************************************************
 *
 * GamlStandaloneSetup.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gaml.compiler;

import com.google.inject.Injector;

/**
 * Initialization support for running Xtext languages without equinox extension registry
 */
public class GamlStandaloneSetup extends GamlStandaloneSetupGenerated {

	/**
	 * Do setup.
	 *
	 * @return the injector
	 */
	public static Injector doSetup() {
		return new GamlStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
	}
}
