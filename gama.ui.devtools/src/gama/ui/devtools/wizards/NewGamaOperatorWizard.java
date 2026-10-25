/*******************************************************************************************************
 *
 * NewGamaOperatorWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.wizards;

import java.util.Map;

import gama.ui.devtools.templates.CodeTemplates;

/**
 * Creates a class defining GAML operators, with documentation and examples used as tests.
 */
public class NewGamaOperatorWizard extends AbstractGamaClassWizard {

	@Override
	protected String title() { return "New GAMA Operators"; }

	@Override
	protected String description() { return "Creates a class defining GAML operators, with documentation and examples used as tests."; }

	@Override
	protected String defaultClassName() { return "MyOperators"; }

	@Override
	protected String suffix() { return "Operators"; }

	@Override
	protected String gamlNameLabel() { return "Operator name"; }

	@Override
	protected Map<String, String> generate(final String pkg, final String className, final String gamlName) {
		return Map.of(className + ".java", CodeTemplates.operator(pkg, className, gamlName));
	}

}
