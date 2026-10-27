/*******************************************************************************************************
 *
 * NewGamaActionWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
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
 * Creates a skill defining a single action (to be merged in an existing skill if needed).
 */
public class NewGamaActionWizard extends AbstractGamaClassWizard {

	@Override
	protected String title() { return "New GAMA Action"; }

	@Override
	protected String description() { return "Creates a skill defining a single action (to be merged in an existing skill if needed)."; }

	@Override
	protected String defaultClassName() { return "MyActionSkill"; }

	@Override
	protected String suffix() { return "Skill"; }

	@Override
	protected String gamlNameLabel() { return "Skill name"; }

	@Override
	protected Map<String, String> generate(final String pkg, final String className, final String gamlName) {
		return Map.of(className + ".java", CodeTemplates.action(pkg, className, gamlName, "do_" + gamlName));
	}

}
